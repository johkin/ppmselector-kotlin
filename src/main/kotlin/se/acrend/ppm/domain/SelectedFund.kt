package se.acrend.ppm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 *
 */
@Document
data class SelectedFund(@Id val id: String?, val fund: FundInfo, val date: LocalDate, val strategy: Strategy)