package se.acrend.ppm.config

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.time.Duration
import java.util.concurrent.TimeUnit




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
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
            }
        val httpClient = HttpClient.from(tcpClient)
            .responseTimeout(Duration.ofSeconds(30))
            .observe { connection, newState ->
            println(newState)
            println(connection)
        }

        return webClientBuilder.clientConnector(ReactorClientHttpConnector(httpClient)).build()
        //return webClientBuilder.build()
    }

}
