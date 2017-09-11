package se.acrend.ppm.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.mail.FundMailer
import se.acrend.ppm.parser.MorningstarParser
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

/**
 *
 */
@Service
class FundReaderService {


    val client = WebClient.create("http://www.morningstar.se/")

    @Autowired
    lateinit var parser: MorningstarParser
    @Autowired
    lateinit var mailer: FundMailer

    fun readFunds() {

        val returns = client.get().uri("Funds/Quickrank.aspx?ppm=on&adv=1&mngmtf=lt_1.0&sort=Month_3&ascdesc=Desc&view=returns")

        val returnsList: Flux<FundInfo> = returns.exchange()
                .flatMap { response -> response.bodyToMono(String::class.java) }
                .flatMapMany { body -> Flux.fromIterable(parser.parseReturns(body)) }


        val misc = client.get().uri("Funds/Quickrank.aspx?ppm=on&adv=1&mngmtf=lt_1.0&sort=Month_3&ascdesc=Desc&view=misc")

        val miscList: Flux<FundInfo> = misc.exchange()
                .flatMap { response -> response.bodyToMono(String::class.java) }
                .flatMapMany { body -> Flux.fromIterable(parser.parseMisc(body)) }

        val zipped = returnsList.zipWith<FundInfo, FundInfo>(miscList, BiFunction {returnsInfo, miscInfo ->
            returnsInfo.ppmNumber = miscInfo.ppmNumber
            returnsInfo
        }).collectList().subscribe(Consumer { fundList ->
            mailer.sendMail(fundList)
        })
    }
}

