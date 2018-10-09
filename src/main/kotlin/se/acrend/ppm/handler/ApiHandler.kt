package se.acrend.ppm.handler

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.domain.api.ApiSelectedFund
import se.acrend.ppm.domain.api.ApiTransaction
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository
import java.util.function.Function
import java.util.stream.Collectors.*

/**
 *
 */
@Component
class ApiHandler(val transactionRepository: TransactionRepository,
                 val selectedFundRepository: SelectedFundRepository) {

    fun getTransactions(request: ServerRequest): Mono<ServerResponse> {

        val apiTransactionMono = transactionRepository.findAll()
                .sort(Comparator.comparing(Transaction::buyDate))
                .collect(groupingBy(

                        Function<Transaction, Strategy> { transaction -> transaction.strategy },
                        mapping(
                                Function<Transaction, ApiTransaction> { transaction: Transaction ->
                                    ApiTransaction(transaction.fund.name, transaction.fund.ppmNumber,
                                            transaction.buyDate, transaction.buyPrice,
                                            transaction.sellDate, transaction.sellPrice,
                                            calculateReturn(transaction)) },
                                        toList<ApiTransaction>())))

        class TransactionMapType : ParameterizedTypeReference<Map<Strategy, List<ApiTransaction>>>()

        return ServerResponse.ok().body(BodyInserters.fromPublisher(apiTransactionMono,
                TransactionMapType()))
    }


    fun getSelectedFunds(request: ServerRequest): Mono<ServerResponse> {

        val fundsFlux = selectedFundRepository.findAll()
                .sort(Comparator.comparing<SelectedFund, Int> { selectedFund ->
                    selectedFund.strategy.sortOrder
                })
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

    fun getStrategies(request: ServerRequest): Mono<ServerResponse> {

        val strategiesMap = mutableMapOf<String, String>()

        Strategy.values().asList().forEach({ s ->
            strategiesMap.put(s.name, s.description)
        })

        return ServerResponse.ok().body(BodyInserters.fromObject(strategiesMap))
    }

}