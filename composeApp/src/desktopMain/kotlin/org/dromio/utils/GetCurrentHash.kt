package org.dromio.utils

import org.dromio.database.Database
import org.dromio.database.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    // Get current hash from database
    Database.ensureInitialized()
    transaction {
        val currentHash = Users.select { Users.username eq "admin" }
            .firstOrNull()?.get(Users.passwordHash)
        println("Current admin password hash: $currentHash")
    }
    
    // Generate hash for a new password
    val newPassword = "admin123"
    val newHash = hashPassword(newPassword)
    println("\nFor password '$newPassword':")
    println("New hash: $newHash")
}

private fun hashPassword(password: String): String {
    return java.security.MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, byte -> str + "%02x".format(byte) }
}
