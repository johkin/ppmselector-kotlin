package se.acrend.ppm.control

import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import se.acrend.ppm.domain.SelectedFund
import se.acrend.ppm.domain.Strategy
import se.acrend.ppm.domain.Transaction
import se.acrend.ppm.handler.ApiSelectedFund
import se.acrend.ppm.handler.ApiTransaction
import se.acrend.ppm.repository.SelectedFundRepository
import se.acrend.ppm.repository.TransactionRepository

@RestController
class ApiController(
    val transactionRepository: TransactionRepository,
    val selectedFundRepository: SelectedFundRepository
) {

    @GetMapping("/api/transactions")
    suspend fun getTransactions(): Map<Strategy, List<ApiTransaction>> {

        val result = transactionRepository.findAll().toList()
            .sortedByDescending(Transaction::buyDate)
            .groupBy(
                { transaction -> transaction.strategy },
                { transaction: Transaction -> transaction.toApi() }
            )

        return result

    }

    @GetMapping("/api/selectedFunds")
    suspend fun getSelectedFunds(): List<ApiSelectedFund> {

        val result = selectedFundRepository.findAll().toList()
            .sortedWith(Comparator.comparing<SelectedFund, Int> { selectedFund ->
                selectedFund.strategy.sortOrder
            })
            .map { selected ->
                selected.toApi()
            }

        return result

    }


    fun Transaction.toApi(): ApiTransaction {
        return ApiTransaction(
            fundName = fund.name,
            ppmNumber = fund.ppmNumber,
            buyDate = buyDate,
            buyPrice = buyPrice,
            sellDate = sellDate,
            sellPrice = sellPrice,
            returnPercent = calculateReturn()
        )
    }

    fun Transaction.calculateReturn(): Float {
        return if (buyPrice != null && sellPrice != null) {
            (sellPrice - buyPrice) / buyPrice * 100
        } else {
            0f
        }
    }

    fun SelectedFund.toApi(): ApiSelectedFund {
        return ApiSelectedFund(
            fundName = fund.name,
            uri = "https://www.morningstar.se/se/funds/snapshot/snapshot.aspx?id=${fund.secId}",
            ppmNumber = fund.ppmNumber ?: "",
            selectedDate = date,
            strategy = strategy,
            strategyDesc = strategy.description
        )
    }
}