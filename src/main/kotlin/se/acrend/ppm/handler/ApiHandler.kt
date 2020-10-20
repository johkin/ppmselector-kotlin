package se.acrend.ppm.handler

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository
import java.util.stream.Collectors.groupingBy
import java.util.stream.Collectors.mapping
import java.util.stream.Collectors.toList

/**
 *
 */
@Component
class ApiHandler(
    val transactionRepository: TransactionRepository,
    val selectedFundRepository: SelectedFundRepository
) {

    fun getTransactions(request: ServerRequest): Mono<ServerResponse> {

        val apiTransactionMono = transactionRepository.findAll()
            .sort(Comparator.comparing(Transaction::buyDate))
            .collect(
                groupingBy(
                    { transaction -> transaction.strategy },
                    mapping(
                        { transaction: Transaction ->
                            transaction.toApi()
                        },
                        toList<ApiTransaction>()
                    )
                )
            )

        class TransactionMapType : ParameterizedTypeReference<Map<Strategy, List<ApiTransaction>>>()

        return ServerResponse.ok().body(
            BodyInserters.fromPublisher(
                apiTransactionMono,
                TransactionMapType()
            )
        )
    }

    fun Transaction.toApi(): ApiTransaction {
        return ApiTransaction(
            fundName = fund.name,
            ppmNumber = fund.ppmNumber,
            buyDate = buyDate,
            buyPrice = buyPrice,
            sellDate = sellDate,
            sellPrice = sellPrice,
            returnPercent = calculateReturn()
        )
    }

    fun getSelectedFunds(request: ServerRequest): Mono<ServerResponse> {

        val fundsFlux = selectedFundRepository.findAll()
            .sort(Comparator.comparing<SelectedFund, Int> { selectedFund ->
                selectedFund.strategy.sortOrder
            })
            .map { selected ->
                ApiSelectedFund(
                    selected.fund.name, "",
                    selected.fund.ppmNumber ?: "", selected.date,
                    selected.strategy, selected.strategy.description
                )
            }

        return ServerResponse.ok().body(BodyInserters.fromPublisher(fundsFlux, ApiSelectedFund::class.java))

    }

    fun Transaction.calculateReturn(): Float {
        return if (buyPrice != null && sellPrice != null) {
            (sellPrice - buyPrice) / buyPrice * 100
        } else {
            0f
        }
    }

    fun getStrategies(request: ServerRequest): Mono<ServerResponse> {


        val strategiesMap = Strategy.values().associate { s ->
            s.name to s.description
        }

        return ServerResponse.ok().body(fromValue(strategiesMap))
    }

}
