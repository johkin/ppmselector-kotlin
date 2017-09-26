package se.acrend.ppm.control

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.repository.PpmFundRepository
import javax.annotation.PostConstruct

/**
 *
 */
@Component
class PensionsMyndighetenController(val repository: PpmFundRepository) {


    val logger = LoggerFactory.getLogger(PensionsMyndighetenController::class.java)

    val ppmNumberSet = HashSet<String>()

    @PostConstruct
    fun init() {

        repository.findAll()
                .toStream().forEach { fund ->
            ppmNumberSet.add(fund.ppmNumber)
        }

    }

    fun existsInPensionsMyndigheten(fund: FundInfo): Mono<Boolean> {

        return Mono.just(ppmNumberSet.contains(fund.ppmNumber))
    }
}