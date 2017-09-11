package se.acrend.ppm

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.router.Router


@SpringBootApplication(scanBasePackages = arrayOf("se.acrend.ppm"))
@Configuration
class PpmselectorKotlinApplication {


}

fun main(args: Array<String>) {
    SpringApplication.run(PpmselectorKotlinApplication::class.java, *args)
}
