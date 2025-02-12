package org.dromio.repository

import org.dromio.database.*
import org.dromio.models.SaleDetail
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReportsRepository {
    fun getSalesReport(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        searchQuery: String
    ): List<SaleDetail> = transaction {
        (TransactionItems innerJoin Transactions innerJoin Products)
            .slice(
                Transactions.id,
                Products.name,
                TransactionItems.quantity,
                TransactionItems.costAtTime,
                TransactionItems.priceAtTime,
                Transactions.timestamp
            )
            .select {
                (Transactions.timestamp greaterEq startDate.toEpochSecond(ZoneOffset.UTC)) and
                (Transactions.timestamp lessEq endDate.toEpochSecond(ZoneOffset.UTC)) and
                (Products.name.lowerCase() like "%${searchQuery.lowercase()}%") and
                (Transactions.type eq "SALE")
            }
            .map { row ->
                val quantity = row[TransactionItems.quantity]
                val buyingPrice = row[TransactionItems.costAtTime]
                val sellingPrice = row[TransactionItems.priceAtTime]

                SaleDetail(
                    id = row[Transactions.id].toString(),
                    productName = row[Products.name],
                    quantity = quantity,
                    buyingPrice = buyingPrice,
                    sellingPrice = sellingPrice,
                    total = quantity * sellingPrice,
                    profit = quantity * (sellingPrice - buyingPrice),
                    date = LocalDateTime.ofEpochSecond(
                        row[Transactions.timestamp],
                        0,
                        ZoneOffset.UTC
                    )
                )
            }
            .also { println("Debug - Found ${it.size} sales records") }  // Debug line
    }

    fun getProfitSummary(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) = transaction {
        TransactionItems
            .join(Transactions, JoinType.INNER) {
                TransactionItems.transactionId eq Transactions.id
            }
            .slice(
                TransactionItems.quantity,
                TransactionItems.priceAtTime,
                TransactionItems.costAtTime
            )
            .select {
                (Transactions.timestamp greaterEq startDate.toEpochSecond(ZoneOffset.UTC)) and
                (Transactions.timestamp lessEq endDate.toEpochSecond(ZoneOffset.UTC)) and
                (Transactions.type eq "SALE")
            }
            .map { row ->
                val quantity = row[TransactionItems.quantity]
                val revenue = quantity * row[TransactionItems.priceAtTime]
                val cost = quantity * row[TransactionItems.costAtTime]
                Triple(revenue, cost, revenue - cost)
            }
            .fold(Triple(0.0, 0.0, 0.0)) { acc, value ->
                Triple(
                    acc.first + value.first,
                    acc.second + value.second,
                    acc.third + value.third
                )
            }
    }
}

data class ProfitSummary(
    val totalRevenue: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val profitMargin: Double
)
