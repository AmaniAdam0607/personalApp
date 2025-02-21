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

            // This is the key fix - establish connection before using transactions
            org.jetbrains.exposed.sql.Database.connect(
                url = url,
                driver = "org.sqlite.JDBC",
                user = "",
                password = ""
            )

            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Products, Transactions, TransactionItems, StockAdjustments, Users, Debts)

                // Create default admin if none exists
                if (Users.selectAll().count() == 0L) {
                    Users.insert {
                        it[username] = "admin"
                        it[passwordHash] = hashPassword("admin123") // Default password
                        it[isAdmin] = true
                        it[createdAt] = System.currentTimeMillis()
                    }
                }

                // Add sample data if needed
                addSampleData()
            }

            isInitialized = true
            println("Database initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun ensureInitialized() {
        if (!isInitialized) {
            init()
        }
    }

    private fun addSampleData() {
        //if (Products.selectAll().count().toInt() == 0) {
        //    val now = System.currentTimeMillis()
        //    Products.insert {
        //        it[name] = "Laptop"
        //        it[sellingPrice] = 999.99
        //        it[buyingPrice] = 800.00
        //        it[stockQuantity] = 5
        //        it[minimumStock] = 2
        //        it[createdAt] = now
        //        it[updatedAt] = now
        //    }   // ...add more sample products...
            // ...add more sample products...
        
    }

    private fun hashPassword(password: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }
}

object Products : IntIdTable() {
    val name = varchar("name", 255)
    val sellingPrice = double("selling_price")
    val buyingPrice = double("buying_price")
    val stockQuantity = integer("stock_quantity")
    val minimumStock = integer("minimum_stock").default(5)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Transactions : IntIdTable() {
    val timestamp = long("timestamp")
    val total = double("total")
    val profit = double("profit")
    val paymentMethod = varchar("payment_method", 50)
    val type = varchar("type", 20) // SALE or PURCHASE
    val notes = varchar("notes", 500).nullable()
}

object TransactionItems : IntIdTable() {
    val transactionId = reference("transaction_id", Transactions)
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    val priceAtTime = double("price_at_time")
    val costAtTime = double("cost_at_time")
}

object StockAdjustments : IntIdTable() {
    val productId = reference("product_id", Products)
    val timestamp = long("timestamp")
    val previousQuantity = integer("previous_quantity")
    val newQuantity = integer("new_quantity")
    val reason = varchar("reason", 500)
    val adjustedBy = varchar("adjusted_by", 100)
}

object Users : IntIdTable() {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val isAdmin = bool("is_admin")
    val createdAt = long("created_at")
}

object Debts : IntIdTable() {
    val customerName = varchar("customer_name", 255)
    val transactionId = reference("transaction_id", Transactions)
    val amount = double("amount")
    val isPaid = bool("is_paid").default(false)
    val createdAt = long("created_at")
    val paidAt = long("paid_at").nullable()
}
