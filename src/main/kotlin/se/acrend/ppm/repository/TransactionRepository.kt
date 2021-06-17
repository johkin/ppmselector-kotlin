package se.acrend.ppm.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import java.time.LocalDate

/**
 *
 */
interface TransactionRepository : CoroutineCrudRepository<Transaction, String> {

    fun findByBuyDateAndBuyPriceNull(buyDate: LocalDate): Flow<Transaction>
    fun findBySellDateAndSellPriceNull(sellDate: LocalDate): Flow<Transaction>
    fun findByFundNameAndStrategyAndSellDateNullOrderByBuyDateAsc(fundName: String, strategy: Strategy): Flow<Transaction>

}