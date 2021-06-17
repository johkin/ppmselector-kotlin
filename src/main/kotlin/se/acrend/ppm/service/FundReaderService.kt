package se.acrend.ppm.service

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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

    suspend fun readFunds(): List<Pair<Strategy, FundInfo>> {


        return coroutineScope {

            Strategy.values().map { strategy ->
                async {
                    readForStrategy(strategy)
                }
            }.map {
                it.await()
            }
        }
    }


    private suspend fun readForStrategy(
        strategy: Strategy
    ): Pair<Strategy, FundInfo> {

        logger.info("Läser fonder för strategi ${strategy.description}")

        val currentSelectedFund = selectedFundRepository.findByStrategy(strategy) ?: selectedFundRepository.save(
            SelectedFund(null, FundInfo("Dummy Fund", ""), LocalDate.now(), strategy)
        )

        // Hämta information från Morningstar
        val fundInfo: FundInfo? = when (strategy) {
            Strategy.ThreeMonthOneMonthPredicate -> readFundFromMorningstar(strategy)
                ?: readFundFromMorningstar(Strategy.OneMonth)

            Strategy.ThreeMonth -> readFundFromMorningstar(strategy)
                ?: readFundFromMorningstar(Strategy.OneMonth)
                ?: readFundFromMorningstar(Strategy.OneWeek)

            Strategy.OneMonth -> readFundFromMorningstar(strategy)
                ?: readFundFromMorningstar(Strategy.OneWeek)

            else -> readFundFromMorningstar(strategy)
        }

        val composite = CompositeFund(currentSelectedFund, fundInfo!!)

        return if (!composite.isFundEqual()) {
            logger.info("För strategi ${strategy.description} är det ny fond: ${composite.fund.name}.")

            updateTransactionInfo(composite)

            val newSelected = composite.selected.copy(fund = composite.fund, date = LocalDate.now())

            selectedFundRepository.save(newSelected)

            logger.info("Skapar meddelande för ${strategy.description}")

            //                        val message = mailer.createHtmlMessage(selectedFund.fund, strategy)
            //                        mailer.sendMail("Nytt val för PPM-fonder, ${strategy.description}!", message)
            strategy to newSelected.fund
        } else {
            logger.info("För strategi ${strategy.description} är det samma fond: ${composite.fund.name}, avslutar.")
            strategy to composite.fund
        }

    }

    private suspend fun updateTransactionInfo(composite: CompositeFund) {

        val newTransaction = transactionRepository.save(
            Transaction(
                id = null, fund = composite.fund, strategy = composite.selected.strategy,
                buyDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS),
                buyPrice = null, sellDate = null, sellPrice = null, returnPercent = null
            )
        )


        val previousTransactions = transactionRepository.findByFundNameAndStrategyAndSellDateNullOrderByBuyDateAsc(
            composite.selected.fund.name,
            composite.selected.strategy
        )

        previousTransactions.onEach { previousTransaction ->
            transactionRepository.save(
                previousTransaction.copy(sellDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS))
            )
        }
    }


    suspend fun readFundFromMorningstar(strategy: Strategy): FundInfo? {

        return morningstarClient.getFundList(strategy)
            .map { fund -> fund.toDocument() }
            .firstOrNull(strategy::includeFund)
    }


    suspend fun updatePrice(): List<Transaction> {


        val result = mutableListOf<Transaction>()

        logger.info("Updaterar priser")

        transactionRepository.findByBuyDateAndBuyPriceNull(LocalDate.now())
            .map(::readPrice)
            .map { pair ->
                logger.info("Uppdaterar köp-pris för fond ${pair.first.fund.name} till ${pair.second.price}")

                val updatedTransaction = pair.first.copy(buyPrice = pair.second.price)
                transactionRepository.save(updatedTransaction)

                result.add(updatedTransaction)
            }


        transactionRepository.findBySellDateAndSellPriceNull(LocalDate.now())
            .map(::readPrice)
            .map { pair ->

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
                result.add(updatedTransaction)
            }

        return result
    }


    suspend fun readPrice(transaction: Transaction): Pair<Transaction, FundInfo> {


        logger.info("Hämtar detaljer för fond ${transaction.fund.name}")
        val fund = morningstarClient.getFundList(strategy = CurrentYear, term = transaction.fund.isin).firstOrNull()

        return transaction to fund!!.toDocument()

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
