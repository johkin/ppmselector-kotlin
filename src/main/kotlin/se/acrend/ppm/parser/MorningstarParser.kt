package se.acrend.ppm.parser

import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import se.acrend.ppm.domain.FundInfo
import java.text.DecimalFormat
import java.util.*

/**
 *
 */
@Component
class MorningstarParser {

    val decimalFormat = DecimalFormat.getInstance(
            Locale("sv", "SE"))


    fun parseReturns(contents: String): List<FundInfo> {

        val result = ArrayList<FundInfo>()

        val document = Jsoup.parse(contents, "http://www.morningstar.se/")

        val rows = document.select("table[class=rgMasterTable] > tbody > tr")


        for (row in rows) {

            val link = row.select("td > a")
                    .first()

            val info = FundInfo(link.text(), "http://www.morningstar.se" + link.attr("href")
                    .substring(2))

            info.growthDay = parseGrowth(row.select("td:eq(5)").text())
            info.growthWeek = parseGrowth(row.select("td:eq(6)").text())
            info.growthMonth = parseGrowth(row.select("td:eq(7)").text())
            info.growth3Month = parseGrowth(row.select("td:eq(8)").text())
            info.growth6Month = parseGrowth(row.select("td:eq(9)").text())
            info.growthYear = parseGrowth(row.select("td:eq(10)").text())

            info.date = row.select("td:eq(11)")
                    .text()

            result.add(info)
        }

        return result
    }

    fun parseGrowth(text: String): Float {
        if ("-".contentEquals(text)) {
            return -1f
        }
        return decimalFormat.parse(text).toFloat()
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


    fun parseFees(contents: String): List<FundInfo> {

        val result = ArrayList<FundInfo>()

        val document = Jsoup.parse(contents, "http://www.morningstar.se/")

        val rows = document.select("table[class=rgMasterTable] > tbody > tr")

        for (row in rows) {

            val link = row.select("td > a")
                    .first()

            val info = FundInfo(link.text(), "http://www.morningstar.se" + link.attr("href")
                    .substring(2))

            info.fee = decimalFormat.parse(row.select("td:eq(8)").text()).toFloat()

            result.add(info)
        }

        return result

    }

}
