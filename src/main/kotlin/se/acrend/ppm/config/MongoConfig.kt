package se.acrend.ppm.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import se.acrend.ppm.repository.TransactionRepository

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = arrayOf(TransactionRepository::class))
class MongoConfig : AbstractReactiveMongoConfiguration() {

    @Autowired
    lateinit var environment: Environment

    @Bean
    fun mongoEventListener(): LoggingEventListener {
        return LoggingEventListener()
    }

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        val uri = environment.getProperty("MONGODB_URI")
        return MongoClients.create(uri)
    }

    override fun getDatabaseName(): String {
        return "heroku_gtpjl1qw"
    }
}