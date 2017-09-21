package se.acrend.ppm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 *
 */
@Document
data class Transaction(@Id val id: String?, val fund: FundInfo,
                       val buyDate: LocalDate, val buyPrice: Float?,
                       val sellDate: LocalDate?, val sellPrice: Float?,
                       val returnPercent: Float?)