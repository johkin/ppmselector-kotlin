package se.acrend.ppm.client

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.parser.Fund
import se.acrend.ppm.parser.FundsResponse

@Component
class MorningstarClient(val webClient: WebClient) {


    suspend fun getFundList(strategy: Strategy, term: String = ""): List<Fund> {

        val builder =
            UriComponentsBuilder.fromHttpUrl("https://lt.morningstar.com/api/rest.svc/klr5zyak8x/security/screener")
        builder.queryParam("page", "1")
        builder.queryParam("pageSize", "20")
        builder.queryParam("sortOrder", strategy.parameterName)
        builder.queryParam("outputType", "json")
        builder.queryParam("version", "1")
        builder.queryParam("languageId", "sv-SE")
        builder.queryParam("currencyId", "SEK")
        builder.queryParam("universeIds", "FOSWE\$\$ALL_5498")
        builder.queryParam(
            "securityDataPoints",
            "SecId|Name|PriceCurrency|TenforeId|ReturnM0|ReturnM60|StandardDeviationM60|OngoingCharge|FundTNAV|StarRatingM255|QuantitativeRating|SustainabilityRank|TrailingDate|ClosePrice|ReturnD1|ReturnW1|ReturnM1|ReturnM3|ReturnM6|ReturnM12|ReturnM36|ReturnM120|ReturnM180|MaxFrontEndLoad|MaximumExitCostAcquired|PerformanceFeeActual|FeeLevel|InitialPurchase|EquityStyleBox|BondStyleBox|AverageCreditQualityCode|EffectiveDuration|PortfolioDate|CollectedSRRI|MorningstarRiskM255|StandardDeviationM12|StandardDeviationM36|standardDeviationM120|Isin|ppmCode"
        )
        builder.queryParam("filters", "PPM:EQ:true")
        builder.queryParam("term", term)
        builder.queryParam("subUniverseId", "")

        val response = webClient.get()
            .uri(builder.build().toUri())
            .retrieve()
            .bodyToMono(FundsResponse::class.java)
            .map { response -> response.rows }
            .awaitSingle()

        return response
    }

}
