package se.acrend.ppm.client

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.SignalType
import reactor.util.Loggers
import se.acrend.ppm.Application
import se.acrend.ppm.config.IntegrationTestConfig
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Strategy.OneMonth
import se.acrend.ppm.service.FundReaderService
import se.acrend.ppm.test.IntegrationTest
import java.util.logging.Level

@IntegrationTest
class MorningstarClientTest {

    @Autowired
    lateinit var client: MorningstarClient

    val logger = LoggerFactory.getLogger(MorningstarClientTest::class.java)

    @Test
    @Disabled
    fun getFundList() {
        runBlocking {

            val list = client.getFundList(OneMonth)

            println(list)
        }
    }

    @Test
    //@Disabled
    fun getFundListParallell() {

        runBlocking {


            Strategy.values().map { s ->
                client.getFundList(s, "LU0359201612")
                    .onEach { l ->
                        println(l)
                    }
            }
        }

        //Thread.sleep(10000)


    }
}
