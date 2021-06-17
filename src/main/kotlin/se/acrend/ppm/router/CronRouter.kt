package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.service.FundReaderService

/**
 *
 */
@Configuration
class CronRouter(val fundReaderService: FundReaderService) {
/*
    @Bean
    fun cronRoutes() = router {

        (accept(MediaType.TEXT_PLAIN) and "/cron").nest {
            GET("/readFunds") { _ ->

                val funds = fundReaderService.readFunds()

                ok().body(funds)
            }
            GET("/updatePrice", { _ ->

                val prices = fundReaderService.updatePrice()


                ok().body(prices)
            })
        }

    }
*/
}
