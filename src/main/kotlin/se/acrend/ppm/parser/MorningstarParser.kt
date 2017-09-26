package se.acrend.ppm.parser

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import se.acrend.ppm.domain.FundInfo
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 *
 */
@Component
class MorningstarParser {

    val logger = LoggerFactory.getLogger(MorningstarParser::class.java)


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
        try {
            if ("-".contentEquals(text) || text.isEmpty()) {
                return -1f
            }
            return getDecimalFormat().parse(text).toFloat()
        } catch (e: NumberFormatException) {
            logger.error("Kunde inte avläsa utveckling: $text", e)
            return -1f
        }
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

            info.fee = getDecimalFormat().parse(row.select("td:eq(8)").text()).toFloat()

            result.add(info)
        }

        return result

    }


    fun parseDetails(body: String): FundInfo {

        val document = Jsoup.parse(body, "http://www.morningstar.se/")

        val priceCell = document.select("div#ctl00_ctl01_cphContent_cphMain_quicktake1_col333_OverviewGeneralItem1_ctl04 > table > tbody > tr:eq(0) > td:eq(1)")
        val text = priceCell.text()
        val numbers = text.substring(0, text.length - 3).trim()
        val price = getDecimalFormat().parse(numbers)

        val fundInfo = FundInfo("", "")
        fundInfo.price = price.toFloat()

        return fundInfo
    }

    fun getDecimalFormat(): NumberFormat {
        return DecimalFormat.getInstance(
                Locale("sv", "SE"))
    }

}
