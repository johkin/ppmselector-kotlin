package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.handler.ApiHandler
import se.acrend.ppm.service.FundReaderService
import java.net.URI

/**
 *
 */
@Configuration
class WebRouter {

    @Bean
    fun webRoutes() = router {

        ("/" and accept(TEXT_HTML)).invoke { _ ->
            ServerResponse.permanentRedirect(URI.create("/index.html")).build()
        }

    }

}