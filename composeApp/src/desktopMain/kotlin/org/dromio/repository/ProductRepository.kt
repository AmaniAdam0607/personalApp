package org.dromio.repository

import org.dromio.database.Database
import org.dromio.database.Products
import org.dromio.models.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductRepository {
    fun getAllProducts(): List<Product> = try {
        Database.ensureInitialized()
        transaction {
            Products.selectAll().map { row ->
                Product(
                    id = row[Products.id].value,
                    name = row[Products.name],
                    sellingPrice = row[Products.sellingPrice],
                    buyingPrice = row[Products.buyingPrice],
                    stockQuantity = row[Products.stockQuantity],
                    minimumStock = row[Products.minimumStock],
                    createdAt = row[Products.createdAt],
                    updatedAt = row[Products.updatedAt]
                )
            }
        }
    } catch (e: Exception) {
        println("Error fetching products: ${e.message}")
        emptyList()
    }

    fun updateStock(productId: Int, newQuantity: Int) = try {
        Database.ensureInitialized()
        transaction {
            Products.update({ Products.id eq productId }) {
                it[stockQuantity] = newQuantity
            }
        }
    } catch (e: Exception) {
        println("Error updating stock: ${e.message}")
    }

    fun addProduct(
        name: String,
        sellingPrice: Double,
        buyingPrice: Double,
        stock: Int
    ): Product = try {
        Database.ensureInitialized()
        transaction {
            val now = System.currentTimeMillis()
            val insertStatement = Products.insert {
                it[Products.name] = name
                it[Products.sellingPrice] = sellingPrice
                it[Products.buyingPrice] = buyingPrice
                it[Products.stockQuantity] = stock
                it[Products.createdAt] = now
                it[Products.updatedAt] = now
            }

            val id = insertStatement[Products.id].value
            Product(id, name, sellingPrice, buyingPrice, stock)
        }
    } catch (e: Exception) {
        println("Error adding product: ${e.message}")
        Product(-1, name, sellingPrice, buyingPrice, stock)
    }

    fun searchProducts(query: String): List<Product> = try {
        Database.ensureInitialized()
        transaction {
            Products.select { Products.name.lowerCase() like "%${query.lowercase()}%" }
                .map { row ->
                    Product(
                        id = row[Products.id].value,
                        name = row[Products.name],
                        sellingPrice = row[Products.sellingPrice],
                        buyingPrice = row[Products.buyingPrice],
                        stockQuantity = row[Products.stockQuantity],
                        minimumStock = row[Products.minimumStock],
                        createdAt = row[Products.createdAt],
                        updatedAt = row[Products.updatedAt]
                    )
                }
        }
    } catch (e: Exception) {
        println("Error searching products: ${e.message}")
        emptyList()
    }

    fun deleteProduct(id: Int) = try {
        Database.ensureInitialized()
        transaction {
            Products.deleteWhere { Products.id eq id }
        }
    } catch (e: Exception) {
        println("Error deleting product: ${e.message}")
    }

    fun updateProduct(
        id: Int,
        name: String,
        sellingPrice: Double,
        buyingPrice: Double,
        stock: Int
    ) = try {
        Database.ensureInitialized()
        transaction {
            Products.update({ Products.id eq id }) {
                it[Products.name] = name
                it[Products.sellingPrice] = sellingPrice
                it[Products.buyingPrice] = buyingPrice
                it[Products.stockQuantity] = stock
                it[Products.updatedAt] = System.currentTimeMillis()
            }
        }
    } catch (e: Exception) {
        println("Error updating product: ${e.message}")
    }
}
