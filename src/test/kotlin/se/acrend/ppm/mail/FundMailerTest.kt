package se.acrend.ppm.mail

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.rythmengine.RythmEngine
import org.springframework.core.env.Environment
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.Strategy.OneMonth
import java.util.ArrayList

/**
 *
 */
class FundMailerTest {
    private var mailer: FundMailer? = null

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        val environment = Mockito.mock(
            Environment::class.java
        )
        mailer = FundMailer(RythmEngine(), environment)
    }

    @Test
    @Disabled
    @Throws(Exception::class)
    fun createHtmlMessage() {
        val funds: MutableList<FundInfo> = ArrayList()
        val fund = FundInfo("Fond 1", "url-1")
        fund.ppmNumber = "123456"
        funds.add(fund)
        val message = mailer!!.createHtmlMessage(fund, OneMonth).block()
        println(message)
    }
}
