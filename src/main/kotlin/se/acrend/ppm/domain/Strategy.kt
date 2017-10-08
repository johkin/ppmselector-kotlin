package se.acrend.ppm.domain

/**
 *
 */
enum class Strategy(val parameterName: String, val description: String, val sortOrder: Int) {
    OneWeek("Week_1", "En vecka", 0), OneMonth("Month_1", "En månad", 1), ThreeMonth("Month_3", "Tre månader", 2)
}