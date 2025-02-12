package org.dromio.repository

import org.dromio.models.CartItem
import org.dromio.models.Product
import org.dromio.models.Transaction
import org.dromio.database.Products
import org.dromio.database.TransactionItems
import org.dromio.database.Transactions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.dromio.models.SaleDetail

class TransactionRepository {
    fun createTransaction(
        items: List<CartItem>,
        total: Double,
        paymentMethod: String
    ): Int = transaction {
        val transactionId = Transactions.insert {
            it[timestamp] = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            it[Transactions.total] = total
            it[Transactions.paymentMethod] = paymentMethod
            it[type] = "SALE"
            it[profit] = items.sumOf { item ->
                item.quantity * (item.product.sellingPrice - item.product.buyingPrice)
            }
        } get Transactions.id

        items.forEach { item ->
            TransactionItems.insert {
                it[TransactionItems.transactionId] = transactionId
                it[productId] = item.product.id
                it[quantity] = item.quantity
                it[priceAtTime] = item.product.sellingPrice
                it[costAtTime] = item.product.buyingPrice
            }

            // Update stock
            Products.update({ Products.id eq item.product.id }) {
                with(SqlExpressionBuilder) {
                    it[stockQuantity] = stockQuantity - item.quantity
                }
            }
        }

        transactionId.value  // Return the integer ID
    }

    fun getRecentTransactions(limit: Int = 10): List<Transaction> = transaction {
        val transactions = (Transactions leftJoin TransactionItems leftJoin Products)
            .select { Transactions.id eq TransactionItems.transactionId }
            .orderBy(Transactions.timestamp to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                Transaction(
                    id = row[Transactions.id].toString(),
                    timestamp = LocalDateTime.ofEpochSecond(
                        row[Transactions.timestamp],
                        0,
                        ZoneOffset.UTC
                    ),
                    items = getTransactionItems(row[Transactions.id]),
                    total = row[Transactions.total],
                    paymentMethod = row[Transactions.paymentMethod]
                )
            }
        transactions
    }

    private fun getTransactionItems(transactionId: EntityID<Int>): List<CartItem> =
        (TransactionItems innerJoin Products)
            .select { TransactionItems.transactionId eq transactionId }
            .map { row ->
                CartItem(
                    product = Product(
                        id = row[Products.id].value,
                        name = row[Products.name],
                        sellingPrice = row[Products.sellingPrice],
                        buyingPrice = row[Products.buyingPrice],
                        stockQuantity = row[Products.stockQuantity]
                    ),
                    quantity = row[TransactionItems.quantity]
                )
            }

    fun getSalesReport(startTime: Long, endTime: Long) = transaction {
        Transactions
            .select {
                (Transactions.timestamp greaterEq startTime) and
                (Transactions.timestamp lessEq endTime) and
                (Transactions.type eq "SALE")
            }
            .map {
                SaleReport(
                    timestamp = it[Transactions.timestamp],
                    total = it[Transactions.total],
                    profit = it[Transactions.profit]
                )
            }
    }

    fun getProfitReport(startTime: Long, endTime: Long) = transaction {
        val query = """
            SELECT
                p.id,
                p.name,
                SUM(ti.quantity) as units_sold,
                SUM(ti.quantity * (ti.price_at_time - ti.cost_at_time)) as total_profit
            FROM transactions t
            JOIN transaction_items ti ON t.id = ti.transaction_id
            JOIN products p ON ti.product_id = p.id
            WHERE t.timestamp BETWEEN ? AND ? AND t.type = 'SALE'
            GROUP BY p.id, p.name
            ORDER BY total_profit DESC
        """.trimIndent()

        exec(query) { rs ->
            buildList {
                while (rs.next()) {
                    add(ProductProfitReport(
                        productId = rs.getInt("id"),
                        productName = rs.getString("name"),
                        unitsSold = rs.getInt("units_sold"),
                        totalProfit = rs.getDouble("total_profit")
                    ))
                }
            }
        }
    }

    fun recordPurchase(
        products: List<Pair<Product, Int>>, // Product and quantity
        total: Double,
        notes: String? = null
    ) = transaction {
        val transactionId = Transactions.insert {
            it[timestamp] = System.currentTimeMillis()
            it[Transactions.total] = total
            it[profit] = 0.0 // Purchase has no profit
            it[paymentMethod] = "PURCHASE"
            it[type] = "PURCHASE"
            it[Transactions.notes] = notes
        } get Transactions.id

        products.forEach { (product, quantity) ->
            TransactionItems.insert {
                it[this.transactionId] = transactionId
                it[productId] = product.id
                it[this.quantity] = quantity
                it[priceAtTime] = product.buyingPrice
                it[costAtTime] = product.buyingPrice
            }

            Products.update({ Products.id eq product.id }) {
                with(SqlExpressionBuilder) {
                    it[stockQuantity] = stockQuantity + quantity
                    it[updatedAt] = System.currentTimeMillis()
                }
            }
        }
    }

    fun getDetailedSales(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        search: String = ""
    ): List<SaleDetail> = transaction {
        (TransactionItems innerJoin Transactions innerJoin Products)
            .select {
                (Transactions.timestamp greaterEq startDate.toEpochSecond(ZoneOffset.UTC)) and
                (Transactions.timestamp lessEq endDate.toEpochSecond(ZoneOffset.UTC)) and
                (Products.name.lowerCase() like "%${search.lowercase()}%")
            }
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

    // Add methods for profit and debts reports as needed
}

data class SaleReport(
    val timestamp: Long,
    val total: Double,
    val profit: Double
)

data class ProductProfitReport(
    val productId: Int,
    val productName: String,
    val unitsSold: Int,
    val totalProfit: Double
)
