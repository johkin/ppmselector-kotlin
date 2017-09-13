package se.acrend.ppm.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import se.acrend.ppm.domain.Transaction

/**
 *
 */
interface TransactionRepository : ReactiveCrudRepository<Transaction, String>