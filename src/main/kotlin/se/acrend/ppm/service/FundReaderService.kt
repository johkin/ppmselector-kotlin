package se.acrend.ppm.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.parser.MorningstarParser
import se.acrend.ppm.repository.SelectedFundRepository
import java.time.LocalDate
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

    @Autowired
    lateinit var selectedFundRepository: SelectedFundRepository

    fun readFunds() {

        // Hämta den lagrade fonden från databasen,
        // om det inte finns någon tidigare lagrad, lagra en dummy-post
        val selectedFund: Mono<SelectedFund> = selectedFundRepository.findByStrategy(Strategy.ThreeMonth)
                .switchIfEmpty(
                        selectedFundRepository.save(
                                SelectedFund("", FundInfo("Dummy Fund", ""), LocalDate.now(), Strategy.ThreeMonth)))

        // Hämta information från Morningstar
        val fundInfo: Mono<FundInfo> = createFundInfoStream("Month_3")
                .switchIfEmpty(createFundInfoStream("Month_1"))

        Flux.zip(selectedFund, fundInfo, BiFunction<SelectedFund, FundInfo, CompositeFund> { selected, fund ->
            CompositeFund(selected, fund)
        })
                .next()
                .flatMap { composite ->
                    if (composite.isFundEqual()) {
                        Mono.empty<SelectedFund>()
                    } else {
                        val newSelected = composite.selected.copy(fund = composite.fund, date = LocalDate.now())
                        selectedFundRepository.save(newSelected)
                    }
                }
                .subscribe(Consumer { selected ->
                    mailer.sendMail(selected.fund)
                })
    }


    fun createFundInfoStream(sortOrder: String): Mono<FundInfo> {

        val builder = UriComponentsBuilder.fromHttpUrl("http://www.morningstar.se/Funds/Quickrank.aspx")
        builder.queryParam("ppm", "on")
        builder.queryParam("adv", "1")
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
                .next()

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

class CompositeFund(val selected: SelectedFund, val fund: FundInfo) {
    fun isFundEqual() =
            selected.fund.name.contentEquals(fund.name)

}
