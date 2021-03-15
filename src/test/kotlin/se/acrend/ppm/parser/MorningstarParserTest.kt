package se.acrend.ppm.parser

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.MongoDBContainer
import se.acrend.ppm.Application
import se.acrend.ppm.IntegrationTestApplication
import se.acrend.ppm.config.IntegrationTestConfig
import se.acrend.ppm.service.MongoDbExtension
import java.net.URI


@DataMongoTest
@ContextConfiguration(classes=[Application::class])
@Import(IntegrationTestConfig::class)
@ActiveProfiles("test")
class MorningstarParserTest {


    @Autowired
    lateinit var client: WebClient

    @Test
    fun testParse() {

//        val client = WebClient.builder().codecs(Jackson2ObjectMapperBuilderCustomizer)

        val response = client.get()
            .uri(URI.create("https://lt.morningstar.com/api/rest.svc/klr5zyak8x/security/screener?page=1&pageSize=5&sortOrder=ReturnW1%20desc&outputType=json&version=1&languageId=sv-SE&currencyId=SEK&universeIds=FOSWE%24%24ALL_5498&securityDataPoints=SecId%7CName%7CPriceCurrency%7CTenforeId%7CReturnM0%7CReturnM60%7CStandardDeviationM60%7COngoingCharge%7CFundTNAV%7CStarRatingM255%7CQuantitativeRating%7CSustainabilityRank%7CTrailingDate%7CClosePrice%7CReturnD1%7CReturnW1%7CReturnM1%7CReturnM3%7CReturnM6%7CReturnM12%7CReturnM36%7CReturnM120%7CReturnM180%7CMaxFrontEndLoad%7CMaximumExitCostAcquired%7CPerformanceFeeActual%7CFeeLevel%7CInitialPurchase%7CEquityStyleBox%7CBondStyleBox%7CAverageCreditQualityCode%7CEffectiveDuration%7CPortfolioDate%7CCollectedSRRI%7CMorningstarRiskM255%7CStandardDeviationM12%7CStandardDeviationM36%7CstandardDeviationM120%7CIsin%7CppmCode&filters=PPM%3AEQ%3Atrue&term=&subUniverseId="))
            .exchangeToMono { responseHandler -> responseHandler.bodyToMono(FundsResponse::class.java) }
            .block()!!

        println(response)


    }

}

