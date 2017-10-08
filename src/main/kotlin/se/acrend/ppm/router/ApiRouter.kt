package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.handler.ApiHandler

/**
 *
 */
@Configuration
class ApiRouter(val apiHandler: ApiHandler) {

    @Bean
    fun apiRoutes() = router {

        ("/api" and accept(MediaType.APPLICATION_JSON)).nest {
            GET("/transactions", apiHandler::getTransactions)

            GET("/strategies", { request ->

                ServerResponse.ok().body(BodyInserters.fromPublisher(
                        Flux.fromIterable(Strategy.values().asList()), Strategy::class.java))
            })

            GET("/selectedFunds", apiHandler::getSelectedFunds)
        }

    }

}