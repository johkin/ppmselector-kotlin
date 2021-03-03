package se.acrend.ppm.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import java.time.LocalDate

/**
 *
 */
interface TransactionRepository : ReactiveCrudRepository<Transaction, String> {

    fun findByBuyDateAndBuyPriceNull(buyDate: LocalDate): Flux<Transaction>
    fun findBySellDateAndSellPriceNull(sellDate: LocalDate): Flux<Transaction>
    fun findByFundNameAndStrategyAndSellDateNullOrderByBuyDateAsc(fundName: String, strategy: Strategy): Flux<Transaction>

}