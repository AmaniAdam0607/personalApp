package org.dromio.repository

import org.dromio.database.Database
import org.dromio.database.Users
import org.dromio.models.User
import org.dromio.models.UserCredentials
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepository {
    fun authenticate(credentials: UserCredentials): User? {
        Database.ensureInitialized()
        return transaction {
            val passwordHash = hashPassword(credentials.password)
            Users.select {
                (Users.username eq credentials.username) and
                (Users.passwordHash eq passwordHash)
            }.firstOrNull()?.let {
                User(
                    id = it[Users.id].value,
                    username = it[Users.username],
                    isAdmin = it[Users.isAdmin]
                )
            }
        }
    }

    fun createUser(credentials: UserCredentials, isAdmin: Boolean = false): Int = transaction {
        (Users.insert {
            it[username] = credentials.username
            it[passwordHash] = hashPassword(credentials.password)
            it[Users.isAdmin] = isAdmin
            it[createdAt] = System.currentTimeMillis()
        } get Users.id).value
    }

    fun getAllUsers(): List<User> = transaction {
        Users.selectAll().map {
            User(
                id = it[Users.id].value,
                username = it[Users.username],
                isAdmin = it[Users.isAdmin]
            )
        }
    }

    fun deleteUser(id: Int) = transaction {
        Users.deleteWhere { Users.id eq id }
    }

    fun resetPassword(userId: Int, newPassword: String) = transaction {
        Users.update({ Users.id eq userId }) {
            it[passwordHash] = hashPassword(newPassword)
        }
    }

    fun changePassword(userId: Int, oldPassword: String, newPassword: String): Boolean = transaction {
        val user = Users.select { Users.id eq userId }.firstOrNull()
        if (user != null && user[Users.passwordHash] == hashPassword(oldPassword)) {
            Users.update({ Users.id eq userId }) {
                it[passwordHash] = hashPassword(newPassword)
            }
            true
        } else {
            false
        }
    }

    private fun hashPassword(password: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }
}
