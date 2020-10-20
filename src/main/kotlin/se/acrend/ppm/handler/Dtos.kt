package se.acrend.ppm.handler

import se.acrend.ppm.domain.Strategy
import java.time.LocalDate


data class ApiSelectedFund(
    val fundName: String,
    val uri: String = "",
    val ppmNumber: String,
    val selectedDate: LocalDate,
    val strategy: Strategy,
    val strategyDesc: String
)

data class ApiStrategy(
    val name: String,
    val description: String
)

data class ApiTransaction(
    val fundName: String,
    val ppmNumber: String?,
    val buyDate: LocalDate?,
    val buyPrice: Float?,
    val sellDate: LocalDate?,
    val sellPrice: Float?,
    val returnPercent: Float?
)
