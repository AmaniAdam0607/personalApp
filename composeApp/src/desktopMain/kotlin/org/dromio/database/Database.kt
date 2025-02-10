package org.dromio.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Database {
    private var isInitialized = false

    fun init() {
        if (isInitialized) return

        try {
            val dbFile = File("pos_database.db")
            val url = "jdbc:sqlite:${dbFile.absolutePath}"

            org.jetbrains.exposed.sql.Database.connect(url, "org.sqlite.JDBC")

            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Products, Transactions, TransactionItems)

                // Add sample data if tables are empty
                if (Products.selectAll().count().toInt() == 0) {
                    Products.insert {
                        it[name] = "Laptop"
                        it[price] = 999.99
                        it[stockQuantity] = 5
                    }
                    Products.insert {
                        it[name] = "Mouse"
                        it[price] = 25.50
                        it[stockQuantity] = 20
                    }
                    Products.insert {
                        it[name] = "Keyboard"
                        it[price] = 49.99
                        it[stockQuantity] = 15
                    }
                    Products.insert {
                        it[name] = "Monitor"
                        it[price] = 199.95
                        it[stockQuantity] = 8
                    }
                }
            }

            isInitialized = true
            println("Database initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize database: ${e.message}")
            throw e
        }
    }

    fun ensureInitialized() {
        if (!isInitialized) {
            init()
        }
    }
}

object Products : IntIdTable() {
    val name = varchar("name", 255)
    val price = double("price")
    val stockQuantity = integer("stock_quantity")
}

object Transactions : IntIdTable() {
    val timestamp = long("timestamp")
    val total = double("total")
    val paymentMethod = varchar("payment_method", 50)
}

object TransactionItems : IntIdTable() {
    val transactionId = reference("transaction_id", Transactions)
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    val priceAtTime = double("price_at_time")
}
