package se.acrend.ppm.domain

/**
 *
 */
enum class Strategy(val parameterName: String, val description: String) {
    OneWeek("Week_1", "En vecka"), OneMonth("Month_1", "En månad"), ThreeMonth("Month_3", "Tre månader")
}