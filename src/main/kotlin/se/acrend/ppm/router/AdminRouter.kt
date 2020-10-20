package se.acrend.ppm.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router
import se.acrend.ppm.handler.AdminHandler

/**
 *
 */
@Configuration
class AdminRouter(val adminHandler: AdminHandler) {

//    @Bean
//    fun adminRoutes() = router {
//        "/admin".nest {
//            accept(MediaType.MULTIPART_FORM_DATA).nest {
//                POST("/uploadFundList", adminHandler::handleFundList)
//            }
//
//        }
//    }
}

