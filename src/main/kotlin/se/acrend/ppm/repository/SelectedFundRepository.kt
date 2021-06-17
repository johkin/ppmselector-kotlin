package se.acrend.ppm.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy

/**
 *
 */
interface SelectedFundRepository : CoroutineCrudRepository<SelectedFund, String> {

    suspend fun findByStrategy(strategy: Strategy): SelectedFund?


}