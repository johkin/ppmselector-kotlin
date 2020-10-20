package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerResponse.ok
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
            GET("/readFunds") { _ ->

                fundReaderService.readFunds()

                ok().body(fromValue("Reading funds"))
            }
            GET("/updatePrice", { _ ->

                fundReaderService.updatePrice()

                ok().body(fromValue("Updating price"))
            })
        }

    }

}
