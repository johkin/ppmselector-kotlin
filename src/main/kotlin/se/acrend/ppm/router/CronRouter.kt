package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.service.FundReaderService

/**
 *
 */
@Configuration
class CronRouter(val fundReaderService: FundReaderService) {

    @Bean
    fun cronRoutes() = router {

        (accept(MediaType.TEXT_PLAIN) and "/cron").nest {
            GET("/readFunds", { request ->

                fundReaderService.readFunds()

                ServerResponse.ok().body(BodyInserters.fromObject("Reading funds"))
            })
            GET("/updatePrice", { request ->

                fundReaderService.updatePrice()

                ServerResponse.ok().body(BodyInserters.fromObject("Updating price"))
            })
        }

    }

}