package se.acrend.ppm.domain

/**
 *
 */
data class FundInfo(val name: String, val url: String) {

    var ppmNumber: String? = null
    var fee: Double? = null
    var growthDay: String? = null
    var growthWeek: String? = null
    var growthMonth: String? = null
    var growth3Month: String? = null
    var growth6Month: String? = null
    var growthYear: String? = null
    var date: String? = null

}
