package se.acrend.ppm.domain

/**
 *
 */
enum class Strategy(val parameterName: String, val description: String, val sortOrder: Int) : IncludeFundPredicate {
    OneWeek("Week_1", "En vecka", 0) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    }, OneMonth("Month_1", "En månad", 1) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0
        }
    }, ThreeMonth("Month_3", "Tre månader med veckobegränsning", 4) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0 && fund.growthMonth >= 0
        }
    }, ThreeMonthNoPrecidate("Month_3", "Tre månader utan begräsning", 2) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    }, ThreeMonthOneMonthPredicate("Month_3", "Tre månader med månadsbegränsning", 3) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthMonth >= 0
        }
    },
}

interface IncludeFundPredicate {
    fun includeFund(fund: FundInfo): Boolean
}