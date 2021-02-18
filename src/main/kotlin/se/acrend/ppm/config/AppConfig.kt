package se.acrend.ppm.config

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.time.Duration
import java.time.temporal.ChronoUnit

@Configuration
class AppConfig {

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient {

        val sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        val tcpClient = TcpClient.create()
            .secure { sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext) }
        val httpClient = HttpClient.from(tcpClient).responseTimeout(Duration.ofSeconds(30))

        return WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient)).build()
    }

}
