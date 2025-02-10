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
        search: String = ""
    ): List<SaleDetail> = transaction {
        (TransactionItems innerJoin Transactions innerJoin Products)
            .select {
                (Transactions.timestamp greaterEq startDate.toEpochSecond(ZoneOffset.UTC)) and
                (Transactions.timestamp lessEq endDate.toEpochSecond(ZoneOffset.UTC)) and
                (Products.name.lowerCase() like "%${search.lowercase()}%") and
                (Transactions.type eq "SALE")
            }
            .orderBy(Transactions.timestamp to SortOrder.DESC)
            .map { row ->
                SaleDetail(
                    id = row[Transactions.id].toString(),
                    productName = row[Products.name],
                    quantity = row[TransactionItems.quantity],
                    buyingPrice = row[TransactionItems.costAtTime],
                    sellingPrice = row[TransactionItems.priceAtTime],
                    total = row[TransactionItems.quantity] * row[TransactionItems.priceAtTime],
                    profit = row[TransactionItems.quantity] *
                        (row[TransactionItems.priceAtTime] - row[TransactionItems.costAtTime]),
                    date = LocalDateTime.ofEpochSecond(
                        row[Transactions.timestamp],
                        0,
                        ZoneOffset.UTC
                    )
                )
            }
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
