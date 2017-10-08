package se.acrend.ppm.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.domain.api.ApiSelectedFund
import se.acrend.ppm.domain.api.ApiTransaction
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository
import java.util.stream.Collectors

/**
 *
 */
@Component
class ApiHandler(val transactionRepository: TransactionRepository,
                 val selectedFundRepository: SelectedFundRepository) {

    fun getTransactions(request: ServerRequest): Mono<ServerResponse> {

        val apiTransactionMono = transactionRepository.findAll()
                .toStream()
                .sorted(Comparator.comparing(Transaction::buyDate))
                .collect(Collectors.groupingBy<Transaction, Strategy> { transation -> transation.strategy })
                .mapValues { entry ->
                    entry.value
                            .map { transaction ->
                                ApiTransaction(transaction.fund.name, transaction.fund.ppmNumber,
                                        transaction.buyDate, transaction.buyPrice,
                                        transaction.sellDate, transaction.sellPrice,
                                        calculateReturn(transaction))
                            }
                }

        return ServerResponse.ok().body(BodyInserters.fromObject(apiTransactionMono))
    }


    fun getSelectedFunds(request: ServerRequest): Mono<ServerResponse> {

        val fundsFlux = selectedFundRepository.findAll()
                .sort(Comparator.comparing<SelectedFund, Int> { selectedFund ->
                    selectedFund.strategy.sortOrder})
                .map { selected ->
                    ApiSelectedFund(selected.fund.name, selected.fund.url,
                            selected.fund.ppmNumber ?: "", selected.date,
                            selected.strategy, selected.strategy.description)
                }

        return ServerResponse.ok().body(BodyInserters.fromPublisher(fundsFlux, ApiSelectedFund::class.java))

    }

    fun calculateReturn(transaction: Transaction): Float? {
        val returnPercent =
                if (transaction.buyPrice != null && transaction.sellPrice != null) {
                    (transaction.sellPrice - transaction.buyPrice) / transaction.buyPrice * 100
                } else {
                    null
                }
        return returnPercent
    }



}