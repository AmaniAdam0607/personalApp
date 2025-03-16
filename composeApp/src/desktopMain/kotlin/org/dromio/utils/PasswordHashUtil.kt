package org.dromio.utils

fun main() {
    val password = "admin123"
    val hash = hashPassword(password)
    println("Password: $password")
    println("Hash: $hash")
}

private fun hashPassword(password: String): String {
    return java.security.MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, byte -> str + "%02x".format(byte) }
}
