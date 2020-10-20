package se.acrend.ppm.client

import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.parser.Fund

fun Fund.toDocument(): FundInfo {

    return FundInfo(
        name = Name,
        price = ClosePrice,
        isin = Isin,
        ppmNumber = ppmCode,
        growthDay = ReturnD1,
        growthWeek = ReturnW1,
        growthMonth = ReturnM1,
        growth3Month = ReturnM3,
        growth6Month = ReturnM6,
        growthYear = ReturnM12
    )
}
