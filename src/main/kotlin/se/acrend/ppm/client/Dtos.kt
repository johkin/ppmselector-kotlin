package se.acrend.ppm.parser

data class FundsResponse(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val rows: List<Fund>
)

data class Fund(
    val SecId: String,
    val Name: String,
    val PriceCurrency: String,
    val TenforeId: String,
    val ReturnM0: Float,
    val ReturnM60: Float,
    val StandardDeviationM60: Float,
    val OngoingCharge: Float,
    val FundTNAV: String?,
    val StarRatingM255: Int,
    val SustainabilityRank: Int,
    val TrailingDate: String,
    val ClosePrice: Float,
    val ReturnD1: Float,
    val ReturnW1: Float,
    val ReturnM1: Float,
    val ReturnM3: Float,
    val ReturnM6: Float,
    val ReturnM12: Float,
    val ReturnM36: Float,
    val ReturnM120: Float,
    val MaxFrontEndLoad: Int,
    val FeeLevel: String,
    val EquityStyleBox: Int,
    val PortfolioDate: String,
    val CollectedSRRI: Int,
    val MorningstarRiskM255: Int,
    val StandardDeviationM12: Float,
    val StandardDeviationM36: Float,
    val standardDeviationM120: Float,
    val Isin: String,
    val ppmCode: String
)
