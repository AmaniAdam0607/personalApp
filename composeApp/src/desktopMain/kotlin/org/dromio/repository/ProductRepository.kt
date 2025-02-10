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
                    price = row[Products.price],
                    stockQuantity = row[Products.stockQuantity]
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

    fun addProduct(name: String, price: Double, stock: Int): Product = try {
        Database.ensureInitialized()
        transaction {
            val insertStatement = Products.insert {
                it[Products.name] = name
                it[Products.price] = price
                it[Products.stockQuantity] = stock
            }

            val id = insertStatement[Products.id].value
            Product(id, name, price, stock)
        }
    } catch (e: Exception) {
        println("Error adding product: ${e.message}")
        Product(-1, name, price, stock)
    }

    fun searchProducts(query: String): List<Product> = try {
        Database.ensureInitialized()
        transaction {
            Products.select { Products.name.lowerCase() like "%${query.lowercase()}%" }
                .map { row ->
                    Product(
                        id = row[Products.id].value,
                        name = row[Products.name],
                        price = row[Products.price],
                        stockQuantity = row[Products.stockQuantity]
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

    fun updateProduct(id: Int, name: String, price: Double, stock: Int) = try {
        Database.ensureInitialized()
        transaction {
            Products.update({ Products.id eq id }) {
                it[Products.name] = name
                it[Products.price] = price
                it[Products.stockQuantity] = stock
            }
        }
    } catch (e: Exception) {
        println("Error updating product: ${e.message}")
    }
}
