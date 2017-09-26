package se.acrend.ppm.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.PpmFund
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy

/**
 *
 */
interface PpmFundRepository : ReactiveCrudRepository<PpmFund, String> {


}