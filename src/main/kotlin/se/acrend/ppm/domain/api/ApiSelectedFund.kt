package se.acrend.ppm.domain.api

import se.acrend.ppm.domain.Strategy
import java.time.LocalDate

/**
 *
 */
data class ApiSelectedFund(val fundName: String, val uri: String, val ppmNumber: String,
                           val selectedDate: LocalDate, val strategy: Strategy, val strategyDesc: String)