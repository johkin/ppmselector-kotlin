package se.acrend.ppm.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction

/**
 *
 */
interface SelectedFundRepository : ReactiveCrudRepository<SelectedFund, String> {

    fun findByStrategy(strategy: Strategy): Mono<SelectedFund>


}