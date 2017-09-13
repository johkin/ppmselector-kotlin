package se.acrend.ppm.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.parser.MorningstarParser
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 *
 */
@Service
class FundReaderService {


    val client = WebClient.create()

    @Autowired
    lateinit var parser: MorningstarParser
    @Autowired
    lateinit var mailer: FundMailer

    fun readFunds() {

        createFundInfoStream("Month_3")
                .switchIfEmpty(createFundInfoStream("Month_1"))
                .collectList()
                .subscribe(Consumer { fundList ->
                    mailer.sendMail(fundList)
                })
    }

    fun createFundInfoStream(sortOrder: String): Flux<FundInfo> {

        val builder = UriComponentsBuilder.fromHttpUrl("http://www.morningstar.se/Funds/Quickrank.aspx")
        builder.queryParam("ppm", "on")
        builder.queryParam("adv", "1")
        builder.queryParam("mngmtf", "lt_1.5")
        builder.queryParam("sort", sortOrder)
        builder.queryParam("ascdesc", "Desc")
        builder.queryParam("view", "returns")

        val returnsList: Flux<FundInfo> = createRequestStream(builder, parser::parseReturns)

        builder.replaceQueryParam("view", "misc")

        val miscList: Flux<FundInfo> = createRequestStream(builder, parser::parseMisc)

        builder.replaceQueryParam("view", "fees")

        val feesList: Flux<FundInfo> = createRequestStream(builder, parser::parseFees)

        val zipped = returnsList
                .zipWith<FundInfo, FundInfo>(miscList, BiFunction { returnsInfo, miscInfo ->
                    returnsInfo.ppmNumber = miscInfo.ppmNumber
                    returnsInfo
                }).zipWith<FundInfo, FundInfo>(feesList, BiFunction { returnsInfo, feesInfo ->
            returnsInfo.fee = feesInfo.fee
            returnsInfo
        })

                .filter({ fundInfo ->
                    fundInfo.growthMonth > 0 &&
                            fundInfo.growthWeek > 0
                })

        return zipped
    }

    fun createRequestStream(builder: UriComponentsBuilder, parseMethod: (String) -> List<FundInfo>): Flux<FundInfo> {
        return client.get()
                .uri(builder.build().toUri())
                .exchange()
                .flatMap { response -> response.bodyToMono(String::class.java) }
                .flatMapMany { body -> Flux.fromIterable(parseMethod(body)) }
    }
}

