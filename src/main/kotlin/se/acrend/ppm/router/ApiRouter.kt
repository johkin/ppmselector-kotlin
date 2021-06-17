package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.handler.ApiHandler

/**
 *
 */
@Configuration
class ApiRouter(val apiHandler: ApiHandler) {

    @Bean
    fun apiRoutes() = router {

        ("/api" and accept(MediaType.APPLICATION_JSON)).nest {
            //GET("/transactions", apiHandler::getTransactions)

            GET("/strategies", apiHandler::getStrategies)

            // GET("/selectedFunds", apiHandler::getSelectedFunds)
        }

    }

}