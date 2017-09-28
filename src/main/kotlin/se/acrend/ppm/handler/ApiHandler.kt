package se.acrend.ppm.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.domain.api.ApiTransaction
import se.acrend.ppm.repository.TransactionRepository

/**
 *
 */
@Component
class ApiHandler(val transactionRepository: TransactionRepository) {

    fun getTransactions(request: ServerRequest): Mono<ServerResponse> {

        val apiTransactionFlux = transactionRepository.findAll()
                .flatMap { transaction ->
                    Mono.just(ApiTransaction(transaction.fund.name, transaction.fund.ppmNumber,
                            transaction.buyDate, transaction.buyPrice,
                            transaction.sellDate, transaction.sellPrice,
                            calculateReturn(transaction)))
                }

        return ServerResponse.ok().body(apiTransactionFlux, ApiTransaction::class.java)
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