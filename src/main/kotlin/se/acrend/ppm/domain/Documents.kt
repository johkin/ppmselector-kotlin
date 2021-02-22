package se.acrend.ppm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document
data class FundInfo(
    val name: String,
    var isin: String,
    var ppmNumber: String? = null,
    var price: Float? = null,
    var fee: Float = 0f,
    var growthDay: Float = 0f,
    var growthWeek: Float = 0f,
    var growthMonth: Float = 0f,
    var growth3Month: Float = 0f,
    var growth6Month: Float = 0f,
    var growthYear: Float = 0f,
    var date: String? = null,
    var secId: String? = null
)

@Document
data class Transaction(
    @Id val id: String?, val fund: FundInfo,
    val strategy: Strategy,
    @Indexed
    val buyDate: LocalDate, val buyPrice: Float?,
    @Indexed(sparse = true)
    val sellDate: LocalDate?, val sellPrice: Float?,
    val returnPercent: Float?
)

@Document
data class SelectedFund(
    @Id val id: String?,
    val fund: FundInfo,
    val date: LocalDate,
    @Indexed
    val strategy: Strategy
)

@Document
data class PpmFund(
    @Field("FONDNAMN") val name: String,
    @Field("FONDNUMMER") val ppmNumber: String,
    @Field("VALUTA") val currency: String,
    @Field("EXTERID") val isin: String
)

enum class Strategy(val parameterName: String, val description: String, val sortOrder: Int) : IncludeFundPredicate {
    OneWeek("ReturnW1 desc", "En vecka", 0) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    OneMonth("ReturnM1 desc", "En månad med veckobegräsning", 1) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0
        }
    },
    OneMonthNoPredicate("ReturnM1 desc", "En månad utan begränsning", 2) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    ThreeMonthNoPrecidate("ReturnM3 desc", "Tre månader utan begräsning", 3) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    ThreeMonthOneMonthPredicate("ReturnM3 desc", "Tre månader med månadsbegränsning", 4) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthMonth >= 0
        }
    },
    ThreeMonth("ReturnM3 desc", "Tre månader med veckobegränsning", 5) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0 && fund.growthMonth >= 0
        }
    },
    SixMonthNoPrecidate("ReturnM6 desc", "Sex månader utan begräsning", 6) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    CurrentYear("ReturnM0 desc", "Innevarande år", 7) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    }
}

fun interface IncludeFundPredicate {
    fun includeFund(fund: FundInfo): Boolean
}
