package se.acrend.ppm.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import se.acrend.ppm.client.MorningstarClient
import se.acrend.ppm.client.toDocument
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Strategy.CurrentYear
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate

/**
 *
 */
@Service
class FundReaderService(
    private val mailer: FundMailer,
    private val selectedFundRepository: SelectedFundRepository,
    private val transactionRepository: TransactionRepository,
    private val morningstarClient: MorningstarClient,
) {


    val logger = LoggerFactory.getLogger(FundReaderService::class.java)

    val FUND_CHANGE_DAYS = 3

    fun readFunds(): Flux<Pair<Strategy, FundInfo>> {

        return Strategy.values().toFlux()
            .flatMap { strategy ->


                logger.info("Läser fonder för strategi ${strategy.description}")


                // Hämta den lagrade fonden från databasen,
                // om det inte finns någon tidigare lagrad, lagra en dummy-post
                val selectedFundMono: Mono<SelectedFund> = selectedFundRepository.findByStrategy(strategy)
                    .switchIfEmpty(
                        selectedFundRepository.save(
                            SelectedFund(null, FundInfo("Dummy Fund", ""), LocalDate.now(), strategy)
                        )
                    )

                // Hämta information från Morningstar
                val fundInfoMono: Mono<FundInfo> = when (strategy) {
                    Strategy.ThreeMonthOneMonthPredicate -> readFundFromMorningstar(strategy)
                        .switchIfEmpty(readFundFromMorningstar(Strategy.OneMonth))

                    Strategy.ThreeMonth -> readFundFromMorningstar(strategy)
                        .switchIfEmpty(readFundFromMorningstar(Strategy.OneMonth))
                        .switchIfEmpty(readFundFromMorningstar(Strategy.OneWeek))

                    Strategy.OneMonth -> readFundFromMorningstar(strategy)
                        .switchIfEmpty(readFundFromMorningstar(Strategy.OneWeek))

                    else -> readFundFromMorningstar(strategy)
                }

                val strategyFund = selectedFundMono.zipWith(fundInfoMono) { selected, fund ->
                    CompositeFund(selected, fund)
                }
                    .flatMap { composite ->
                        if (composite.isFundEqual()) {
                            logger.info("För strategi ${strategy.description} är det samma fond: ${composite.fund.name}, avslutar.")

                            Mono.empty()
                        } else {

                            logger.info("För strategi ${strategy.description} är det ny fond: ${composite.fund.name}.")

                            updateTransactionInfo(composite)

                            val newSelected = composite.selected.copy(fund = composite.fund, date = LocalDate.now())

                            selectedFundRepository.save(newSelected)
                        }
                    }
                    .flatMap { selectedFund ->
                        logger.info("Skapar meddelande för ${strategy.description}")

//                        val message = mailer.createHtmlMessage(selectedFund.fund, strategy)
//                        mailer.sendMail("Nytt val för PPM-fonder, ${strategy.description}!", message)

                        Mono.just(strategy to selectedFund.fund)
                    }
                strategyFund


            }
    }

    private fun updateTransactionInfo(composite: CompositeFund) {

        transactionRepository.save(
            Transaction(
                id = null, fund = composite.fund, strategy = composite.selected.strategy,
                buyDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS),
                buyPrice = null, sellDate = null, sellPrice = null, returnPercent = null
            )
        )
            .flatMap {
                transactionRepository.findByFundNameAndStrategyAndSellDateNull(
                    composite.selected.fund.name,
                    composite.selected.strategy
                )
                    .log()
                    .flatMap { previousTransaction ->
                        transactionRepository.save(
                            previousTransaction.copy(sellDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS))
                        )
                    }
            }
            .subscribe()
    }


    fun readFundFromMorningstar(strategy: Strategy): Mono<FundInfo> {

        return morningstarClient.getFundList(strategy)
            .map { funds ->
                funds.map { f -> f.toDocument() }
                    .filter(strategy::includeFund)
                    .first()
            }
    }

    fun updatePrice(): Flux<Transaction> {

        logger.info("Updaterar priser")

        val boughtFunds = readPrice(transactionRepository.findByBuyDateAndBuyPriceNull(LocalDate.now()))
            .flatMap { pair ->
                logger.info("Uppdaterar köp-pris för fond ${pair.first.fund.name} till ${pair.second.price}")

                val updatedTransaction = pair.first.copy(buyPrice = pair.second.price)
                transactionRepository.save(updatedTransaction)
            }


        val soldFunds = readPrice(transactionRepository.findBySellDateAndSellPriceNull(LocalDate.now()))
            .flatMap { pair ->

                logger.info("Uppdaterar sälj-pris för fond ${pair.first.fund.name} till ${pair.second.price}")

                val transaction = pair.first
                val fundInfo = pair.second

                val returnPercent =
                    if (fundInfo.price != null && transaction.buyPrice != null) {
                        val sellPrice = fundInfo.price ?: 0f
                        (sellPrice - transaction.buyPrice) / transaction.buyPrice * 100
                    } else {
                        null
                    }

                val updatedTransaction = transaction.copy(
                    sellPrice = fundInfo.price,
                    returnPercent = returnPercent
                )

                transactionRepository.save(updatedTransaction)
            }

        return boughtFunds.concatWith(soldFunds)
    }

    fun readPrice(transactions: Flux<Transaction>): Flux<Pair<Transaction, FundInfo>> {
        return transactions.flatMap { transaction ->
            logger.info("Hämtar detaljer för fond ${transaction.fund.name}")
            morningstarClient.getFundList(strategy = CurrentYear, term = transaction.fund.isin)
                .map { funds ->
                    Pair(transaction, funds.first().toDocument())
                }
        }
    }
}

fun LocalDate.plusWeekDays(weekDays: Int): LocalDate {
    if (weekDays < 1) {
        return this
    }
    if (this.dayOfWeek == DayOfWeek.SATURDAY || this.dayOfWeek == DayOfWeek.SUNDAY) {
        return this.plusDays(1).plusWeekDays(weekDays)
    }
    return this.plusDays(1).plusWeekDays(weekDays - 1)
}

class CompositeFund(val selected: SelectedFund, val fund: FundInfo) {
    fun isFundEqual() = selected.fund.name.contentEquals(fund.name)
}
