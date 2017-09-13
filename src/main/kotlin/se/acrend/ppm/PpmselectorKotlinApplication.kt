package se.acrend.ppm

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import se.acrend.ppm.config.MongoConfig


@SpringBootApplication(scanBasePackages = arrayOf("se.acrend.ppm"))
@Import(value = MongoConfig::class)
@Configuration
class PpmselectorKotlinApplication  {


}



fun main(args: Array<String>) {
    SpringApplication.run(PpmselectorKotlinApplication::class.java, *args)
}
