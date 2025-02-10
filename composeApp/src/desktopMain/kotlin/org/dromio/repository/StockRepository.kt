package org.dromio.repository

import org.dromio.database.Products
import org.dromio.database.StockAdjustments
import org.dromio.models.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class StockRepository {
    fun adjustStock(
        productId: Int,
        newQuantity: Int,
        reason: String,
        adjustedBy: String
    ) = transaction {
        val product = Products.select { Products.id eq productId }.first()
        val previousQuantity = product[Products.stockQuantity]

        StockAdjustments.insert {
            it[this.productId] = productId
            it[timestamp] = System.currentTimeMillis()
            it[this.previousQuantity] = previousQuantity
            it[this.newQuantity] = newQuantity
            it[this.reason] = reason
            it[this.adjustedBy] = adjustedBy
        }

        Products.update({ Products.id eq productId }) {
            it[stockQuantity] = newQuantity
            it[updatedAt] = System.currentTimeMillis()
        }
    }

    fun getLowStockProducts(): List<Product> = transaction {
        Products.select { Products.stockQuantity lessEq Products.minimumStock }
            .map { it.toProduct() }
    }

    fun getStockHistory(productId: Int) = transaction {
        StockAdjustments
            .select { StockAdjustments.productId eq productId }
            .orderBy(StockAdjustments.timestamp to SortOrder.DESC)
            .map {
                StockAdjustment(
                    timestamp = it[StockAdjustments.timestamp],
                    previousQuantity = it[StockAdjustments.previousQuantity],
                    newQuantity = it[StockAdjustments.newQuantity],
                    reason = it[StockAdjustments.reason],
                    adjustedBy = it[StockAdjustments.adjustedBy]
                )
            }
    }
}

private fun ResultRow.toProduct() = Product(
    id = this[Products.id].value,
    name = this[Products.name],
    sellingPrice = this[Products.sellingPrice],
    buyingPrice = this[Products.buyingPrice],
    stockQuantity = this[Products.stockQuantity],
    minimumStock = this[Products.minimumStock],
    createdAt = this[Products.createdAt],
    updatedAt = this[Products.updatedAt]
)

data class StockAdjustment(
    val timestamp: Long,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String,
    val adjustedBy: String
)
