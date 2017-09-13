package se.acrend.ppm.router

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.repository.TransactionRepository
import se.acrend.ppm.service.FundReaderService
import java.time.LocalDate

/**
 *
 */
@Configuration
class Router {

    @Autowired
    lateinit var transactionRepo: TransactionRepository

    @Autowired
    lateinit var service: FundReaderService

    @Bean
    fun routes() = router {

        path("/load").invoke { request ->

            ServerResponse.ok().body(BodyInserters.fromPublisher(transactionRepo.findAll(), Transaction::class.java))
        }
        path("/store").invoke { request ->

            val result = transactionRepo.saveAll(listOf(
                    Transaction("1", "Fund1", LocalDate.of(2016, 2, 22), LocalDate.of(2016, 5, 20))))

            ServerResponse.ok().body(BodyInserters.fromPublisher(result, Transaction::class.java))
        }

        path("/readFunds").invoke { request ->

            service.readFunds()

            ServerResponse.ok().body(BodyInserters.fromObject("Reading funds"))
        }
    }

}