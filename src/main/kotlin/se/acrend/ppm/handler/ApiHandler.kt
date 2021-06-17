package se.acrend.ppm.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository

/**
 *
 */
@Component
class ApiHandler(
    val transactionRepository: TransactionRepository,
    val selectedFundRepository: SelectedFundRepository
) {

    /*suspend fun getTransactions(request: ServerRequest): ServerResponse {

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

    fun SelectedFund.toApi(): ApiSelectedFund {
        return ApiSelectedFund(
            fundName = fund.name,
            uri = "https://www.morningstar.se/se/funds/snapshot/snapshot.aspx?id=${fund.secId}",
            ppmNumber = fund.ppmNumber ?: "",
            selectedDate = date,
            strategy = strategy,
            strategyDesc = strategy.description
        )
    }

    fun getSelectedFunds(request: ServerRequest): Mono<ServerResponse> {

        val fundsFlux = selectedFundRepository.findAll()
            .sort(Comparator.comparing<SelectedFund, Int> { selectedFund ->
                selectedFund.strategy.sortOrder
            })
            .map { selected ->
                selected.toApi()
            }

        return ServerResponse.ok().body(BodyInserters.fromPublisher(fundsFlux, ApiSelectedFund::class.java))

    }

    fun Transaction.calculateReturn(): Float {
        return if (buyPrice != null && sellPrice != null) {
            (sellPrice - buyPrice) / buyPrice * 100
        } else {
            0f
        }
    }*/

    fun getStrategies(request: ServerRequest): Mono<ServerResponse> {


        val strategiesMap = Strategy.values().associate { s ->
            s.name to s.description
        }

        return ServerResponse.ok().body(fromValue(strategiesMap))
    }

}
