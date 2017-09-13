package se.acrend.ppm.domain

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 *
 */
@Document
data class Transaction(val id:String, val name: String, val buyDate: LocalDate, val sellDate: LocalDate)