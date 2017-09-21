package se.acrend.ppm.domain

/**
 *
 */
data class FundInfo(val name: String, val url: String) {

    var ppmNumber: String? = null
    var price: Float? = null
    var fee: Float = 0f
    var growthDay: Float = 0f
    var growthWeek: Float = 0f
    var growthMonth: Float = 0f
    var growth3Month: Float = 0f
    var growth6Month: Float = 0f
    var growthYear: Float = 0f
    var date: String? = null

}
