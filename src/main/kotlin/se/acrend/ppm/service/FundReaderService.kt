package se.acrend.ppm.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
import java.util.function.BiFunction

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

    fun readFunds() {

//        val strategy = Strategy.ThreeMonth
        for (strategy in Strategy.values()) {

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
                Strategy.ThreeMonthOneMonthPredicate -> createFundInfoMono(strategy)
                    .switchIfEmpty(createFundInfoMono(Strategy.OneMonth))

                Strategy.ThreeMonth -> createFundInfoMono(strategy)
                    .switchIfEmpty(createFundInfoMono(Strategy.OneMonth))
                    .switchIfEmpty(createFundInfoMono(Strategy.OneWeek))

                Strategy.OneMonth -> createFundInfoMono(strategy)
                    .switchIfEmpty(createFundInfoMono(Strategy.OneWeek))

                else -> createFundInfoMono(strategy)
            }

            Flux.zip(
                selectedFundMono,
                fundInfoMono,
                BiFunction<SelectedFund, FundInfo, CompositeFund> { selected, fund ->
                    CompositeFund(selected, fund)
                })
                .next()
                .flatMap { composite ->
                    if (composite.isFundEqual()) {
                        logger.info("För strategi ${strategy.description} är det samma fond: ${composite.fund.name}, avslutar.")

                        Mono.empty<SelectedFund>()
                    } else {

                        logger.info("För strategi ${strategy.description} är det ny fond: ${composite.fund.name}.")

                        updateTransactionInfo(composite)

                        val newSelected = composite.selected.copy(fund = composite.fund, date = LocalDate.now())

                        selectedFundRepository.save(newSelected)
                    }
                }
                .flatMap { selectedFund ->
                    logger.info("Skapar meddelande för ${strategy.description}")

                    mailer.createHtmlMessage(selectedFund.fund, strategy)
                }
                .flatMap { message ->
                    logger.info("Skickar meddelande för ${strategy.description}")

                    mailer.sendMail("Nytt val för PPM-fonder, ${strategy.description}!", message)
                }
                .subscribe({ result ->
                    logger.info("För strategi ${strategy.description}, resultat:", result)
                }, { error ->
                    logger.error("För strategi ${strategy.description}, fel:", error)
                })
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


    fun createFundInfoMono(strategy: Strategy): Mono<FundInfo> {

        return morningstarClient.getFundList(strategy)
            .map { funds ->
                funds.map { f -> f.toDocument() }
                    .filter(strategy::includeFund)
                    .first()
            }
    }

    fun updatePrice() {

        logger.info("Updaterar priser")

        readPrice(transactionRepository.findByBuyDateAndBuyPriceNull(LocalDate.now()))
            .flatMap { pair ->
                logger.info("Uppdaterar köp-pris för fond ${pair.first.fund.name} till ${pair.second.price}")

                val updatedTransaction = pair.first.copy(buyPrice = pair.second.price)
                transactionRepository.save(updatedTransaction)
            }
            .subscribe({ result ->
                logger.info("Köp-pris för fond ${result.fund.name} uppdaterad.")
            }, { error ->
                logger.error("Kunde inte hämta köp-pris.", error)
            })


        readPrice(transactionRepository.findBySellDateAndSellPriceNull(LocalDate.now()))
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
            .subscribe({ result ->
                logger.info("Sälj-pris för fond ${result.fund.name} uppdaterad.")
            }, { error ->
                logger.error("Kunde inte hämta sälj-pris.", error)
            })


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
