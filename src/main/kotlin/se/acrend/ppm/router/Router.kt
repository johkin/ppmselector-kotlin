package se.acrend.ppm.router

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.repository.TransactionRepository
import se.acrend.ppm.service.FundReaderService

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

        (accept(TEXT_PLAIN) and "/cron").nest {
            GET("/readFunds", { request ->

                service.readFunds()

                ServerResponse.ok().body(BodyInserters.fromObject("Reading funds"))
            })
            GET("/updatePrice", { request ->

                service.updatePrice()

                ServerResponse.ok().body(BodyInserters.fromObject("Updating price"))
            })
        }
//        path("/readFunds").invoke { request ->
//
//            service.readFunds()
//
//            ServerResponse.ok().body(BodyInserters.fromObject("Reading funds"))
//        }
//        path("/updatePrice").invoke { request ->
//
//            service.updatePrice()
//
//            ServerResponse.ok().body(BodyInserters.fromObject("Updating price"))
//        }

        (accept(APPLICATION_JSON) and "/api").nest {
            GET("/strategies", { request ->

                ServerResponse.ok().body(BodyInserters.fromPublisher(
                        Flux.fromIterable(Strategy.values().asList()), Strategy::class.java))
            })
        }
    }

}