package se.acrend.ppm.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.FundInfo

import java.util.ArrayList

/**
 *
 */
@Component
class MorningstarParser {

    fun parseReturns(contents: String): List<FundInfo> {

        val result = ArrayList<FundInfo>()

        val document = Jsoup.parse(contents, "http://www.morningstar.se/")

        val rows = document.select("table[class=rgMasterTable] > tbody > tr")


        for (row in rows) {

            val link = row.select("td > a")
                    .first()

            val info = FundInfo(link.text(), "http://www.morningstar.se" + link.attr("href")
                                                                              .substring(2))


            info.growthDay = row.select("td:eq(5)")
                    .text()
            info.growthWeek = row.select("td:eq(6)")
                    .text()
            info.growthMonth = row.select("td:eq(7)")
                    .text()
            info.growth3Month = row.select("td:eq(8)")
                    .text()
            info.growth6Month = row.select("td:eq(9)")
                    .text()
            info.growthYear = row.select("td:eq(10)")
                    .text()
            info.date = row.select("td:eq(11)")
                    .text()

            result.add(info)
        }

        return result
    }

    fun parseFund(contents: String, info: FundInfo): FundInfo {

        val document = Jsoup.parse(contents, "http://www.morningstar.se/")

        info.ppmNumber = document.select("span[title=PPM-nummer]")
                .text()
        val elements = document.select("span[title='Årlig avgift']")
        if (!elements.isEmpty()) {
            info.fee = elements.last()
                    .text().toDouble()
        }
        return info
    }

    fun parseMisc(contents: String): List<FundInfo> {

        val result = ArrayList<FundInfo>()

        val document = Jsoup.parse(contents, "http://www.morningstar.se/")

        val rows = document.select("table[class=rgMasterTable] > tbody > tr")


        for (row in rows) {

            val link = row.select("td > a")
                    .first()

            val info = FundInfo(link.text(), "http://www.morningstar.se" + link.attr("href")
                    .substring(2))

            info.ppmNumber = row.select("td:eq(7) > a")
                    .text()

            result.add(info)
        }

        return result


    }

}
