package se.acrend.ppm.router

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.service.FundReaderService

/**
 *
 */
@Configuration
class Router {

    @Autowired
    lateinit var service: FundReaderService

    @Bean
    fun routes() = router {

        System.out.println("hello1")

        path("/hello-world").invoke { request ->

            ServerResponse.ok().body(BodyInserters.fromObject("Hello World"))
        }

        path("/readFunds").invoke { request ->

            service.readFunds()

            ServerResponse.ok().body(BodyInserters.fromObject("Reading funds"))
        }
    }

}