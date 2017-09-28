package se.acrend.ppm.domain.api

import java.time.LocalDate

/**
 *
 */
data class ApiTransaction(val fundName: String, val ppmNumber: String?,
                          val buyDate: LocalDate?, val buyPrice: Float?,
                          val sellDate: LocalDate?, val sellPrice: Float?,
                          val returnPercent: Float?)
