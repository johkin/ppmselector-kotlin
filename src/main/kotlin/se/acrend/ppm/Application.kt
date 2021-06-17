package se.acrend.ppm

import kotlinx.coroutines.debug.CoroutinesBlockHoundIntegration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.blockhound.BlockHound
import se.acrend.ppm.config.AppConfig
import java.util.HashMap


@SpringBootApplication
//@Import(value = [AppConfig::class])
//@EnableReactiveMongoRepositories
//@EnableWebFlux
//@Configuration
class Application {

//    @Bean
//    fun rythmEngine(): RythmEngine {
//        val conf = HashMap<String, Any>()
//        conf.put("engine.file_write.enabled", false)
//        conf.put("engine.gae.enabled", true)
//
//        return RythmEngine(conf)
//    }


}

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        BlockHound.builder()
            .with(CoroutinesBlockHoundIntegration())
            //.allowBlockingCallsInside(

            //)
    }
}
