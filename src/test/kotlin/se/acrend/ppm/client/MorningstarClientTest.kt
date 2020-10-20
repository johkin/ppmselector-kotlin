package se.acrend.ppm.client

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import se.acrend.ppm.Application
import se.acrend.ppm.config.IntegrationTestConfig
import se.acrend.ppm.domain.Strategy.OneMonth
import se.acrend.ppm.test.IntegrationTest

@IntegrationTest
class MorningstarClientTest {

    @Autowired
    lateinit var client: MorningstarClient

    @Test
    fun getFundList() {

        val list = client.getFundList(OneMonth).block()

        println(list)

    }
}
