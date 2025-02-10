package org.dromio.models

data class User(
    val id: Int,
    val username: String,
    val isAdmin: Boolean
)

data class UserCredentials(
    val username: String,
    val password: String
)
