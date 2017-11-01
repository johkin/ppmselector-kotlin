package se.acrend.ppm.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.control.PensionsMyndighetenController
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.parser.MorningstarParser
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.function.BiFunction

/**
 *
 */
@Service
class FundReaderService(val parser: MorningstarParser, val mailer: FundMailer,
                        val pensionsMyndighetenController: PensionsMyndighetenController,
                        val selectedFundRepository: SelectedFundRepository, val transactionRepository: TransactionRepository) {

    val logger = LoggerFactory.getLogger(FundReaderService::class.java)

    val FUND_CHANGE_DAYS = 3

    val client = WebClient.create()

    fun readFunds() {

//        val strategy = Strategy.ThreeMonth
        for (strategy in Strategy.values()) {

            logger.info("Läser fonder för strategi ${strategy.description}")


            // Hämta den lagrade fonden från databasen,
            // om det inte finns någon tidigare lagrad, lagra en dummy-post
            val selectedFundMono: Mono<SelectedFund> = selectedFundRepository.findByStrategy(strategy)
                    .switchIfEmpty(
                            selectedFundRepository.save(
                                    SelectedFund(null, FundInfo("Dummy Fund", ""), LocalDate.now(), strategy)))

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

            Flux.zip(selectedFundMono, fundInfoMono, BiFunction<SelectedFund, FundInfo, CompositeFund> { selected, fund ->
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
                Transaction(id = null, fund = composite.fund, strategy = composite.selected.strategy,
                        buyDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS),
                        buyPrice = null, sellDate = null, sellPrice = null, returnPercent = null))
                .flatMap { newTransaction ->
                    transactionRepository.findByFundNameAndStrategyAndSellDateNull(composite.selected.fund.name, composite.selected.strategy)
                            .log()
                            .flatMap { previousTransaction ->
                                transactionRepository.save(
                                        previousTransaction.copy(sellDate = LocalDate.now().plusWeekDays(FUND_CHANGE_DAYS)))
                            }
                }
                .subscribe()
    }


    fun createFundInfoMono(strategy: Strategy): Mono<FundInfo> {

        val builder = UriComponentsBuilder.fromHttpUrl("http://www.morningstar.se/Funds/Quickrank.aspx")
        builder.queryParam("ppm", "on")
        builder.queryParam("adv", "1")
        builder.queryParam("sort", strategy.parameterName)
        builder.queryParam("ascdesc", "Desc")
        builder.queryParam("view", "returns")

        val returnsFlux: Flux<FundInfo> = createRequestStream(builder, parser::parseReturns)

        builder.replaceQueryParam("view", "misc")

        val miscFlux: Flux<FundInfo> = createRequestStream(builder, parser::parseMisc)

        builder.replaceQueryParam("view", "fees")

        val feesFlux: Flux<FundInfo> = createRequestStream(builder, parser::parseFees)

        val fundInfoMono = returnsFlux
                .zipWith<FundInfo, FundInfo>(miscFlux, BiFunction { returnsInfo, miscInfo ->
                    returnsInfo.ppmNumber = miscInfo.ppmNumber
                    returnsInfo
                }).zipWith<FundInfo, FundInfo>(feesFlux, BiFunction { returnsInfo, feesInfo ->
            returnsInfo.fee = feesInfo.fee
            returnsInfo
        })
                .filter(strategy::includeFund)
                .filterWhen(pensionsMyndighetenController::existsInPensionsMyndigheten)
                .next()

        return fundInfoMono
    }

    fun createRequestStream(builder: UriComponentsBuilder, parseMethod: (String) -> List<FundInfo>): Flux<FundInfo> {
        return client.get()
                .uri(builder.build().toUri())
                .exchange()
                .flatMap { response -> response.bodyToMono(String::class.java) }
                .flatMapMany { body -> Flux.fromIterable(parseMethod(body)) }
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

                    val updatedTransaction = transaction.copy(sellPrice = fundInfo.price,
                            returnPercent = returnPercent)

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
            client.get()
                    .uri(transaction.fund.url)
                    .exchange()
                    .flatMap { response -> response.bodyToMono(String::class.java) }
                    .flatMap { body ->
                        val fundInfo = parser.parseDetails(body)

                        Mono.just(Pair(transaction, fundInfo))
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
    fun isFundEqual() = if (selected != null && fund != null) {
        selected.fund.name.contentEquals(fund.name)
    } else {
        false
    }
}
