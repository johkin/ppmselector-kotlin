package se.acrend.ppm.domain

/**
 *
 */
enum class Strategy(val parameterName: String, val description: String, val sortOrder: Int) : IncludeFundPredicate {
    OneWeek("Week_1", "En vecka", 0) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    OneMonth("Month_1", "En månad med veckobegräsning", 1) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0
        }
    },
    OneMonthNoPredicate("Month_1", "En månad utan begränsning", 2) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    ThreeMonthNoPrecidate("Month_3", "Tre månader utan begräsning", 3) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    ThreeMonthOneMonthPredicate("Month_3", "Tre månader med månadsbegränsning", 4) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthMonth >= 0
        }
    },
    ThreeMonth("Month_3", "Tre månader med veckobegränsning", 5) {
        override fun includeFund(fund: FundInfo): Boolean {
            return fund.growthWeek >= 0 && fund.growthMonth >= 0
        }
    },
    SixMonthNoPrecidate("Month_6", "Sex månader utan begräsning", 6) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    },
    CurrentYear("YTD", "Innevarande år", 7) {
        override fun includeFund(fund: FundInfo): Boolean {
            return true
        }
    }
}

interface IncludeFundPredicate {
    fun includeFund(fund: FundInfo): Boolean
}