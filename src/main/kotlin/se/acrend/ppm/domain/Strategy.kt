package se.acrend.ppm.domain

/**
 *
 */
enum class Strategy(val parameterName: String) {
    OneWeek("Week_1"), OneMonth("Month_1"), ThreeMonth("Month_3")
}