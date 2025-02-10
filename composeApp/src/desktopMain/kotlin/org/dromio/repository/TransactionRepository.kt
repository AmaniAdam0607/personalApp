package org.dromio.repository

import org.dromio.database.Products
import org.dromio.database.TransactionItems
import org.dromio.database.Transactions
import org.dromio.screens.CartItem
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class TransactionRepository {
    fun createTransaction(items: List<CartItem>, total: Double, paymentMethod: String) = transaction {
        val transactionId = Transactions.insert {
            it[timestamp] = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            it[Transactions.total] = total
            it[Transactions.paymentMethod] = paymentMethod
        } get Transactions.id

        items.forEach { item ->
            TransactionItems.insert {
                it[TransactionItems.transactionId] = transactionId
                it[productId] = item.product.id
                it[quantity] = item.quantity
                it[priceAtTime] = item.product.price
            }

            // Update stock
            Products.update({ Products.id eq item.product.id }) {
                with(SqlExpressionBuilder) {
                    it[stockQuantity] = stockQuantity - item.quantity
                }
            }
        }
    }

    fun getRecentTransactions(limit: Int = 10): List<org.dromio.screens.Transaction> = transaction {
        (Transactions leftJoin TransactionItems leftJoin Products)
            .select { Transactions.id eq TransactionItems.transactionId }
            .orderBy(Transactions.timestamp to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                org.dromio.screens.Transaction(
                    id = row[Transactions.id].toString(),
                    timestamp = LocalDateTime.ofEpochSecond(
                        row[Transactions.timestamp],
                        0,
                        ZoneOffset.UTC
                    ),
                    items = listOf(), // You might want to fetch items separately
                    total = row[Transactions.total],
                    paymentMethod = row[Transactions.paymentMethod]
                )
            }
    }
}
