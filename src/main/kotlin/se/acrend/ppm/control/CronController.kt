package se.acrend.ppm.control

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.service.FundReaderService

/**
 *
 */
@RestController
class CronController(val fundReaderService: FundReaderService) {

    @GetMapping("/cron/readFunds")
    suspend fun readFunds(): List<Pair<Strategy, FundInfo>> {

        val funds = fundReaderService.readFunds()

        return funds
    }

    @GetMapping("/cron/updatePrice")
    suspend fun updatePrice(): List<Transaction> {

        val prices = fundReaderService.updatePrice()

        return prices
    }


}
