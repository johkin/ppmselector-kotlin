package se.acrend.ppm.domain

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

/**
 *
 */
@Document
data class PpmFund(@Field("FONDNAMN") val name: String,
                   @Field("FONDNUMMER") val ppmNumber: String,
                   @Field("VALUTA") val currency: String,
                   @Field("EXTERID") val isin: String)