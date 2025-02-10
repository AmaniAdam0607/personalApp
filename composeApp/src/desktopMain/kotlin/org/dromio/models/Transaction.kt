package org.dromio.models

import java.time.LocalDateTime

data class Transaction(
    val id: String,
    val timestamp: LocalDateTime,
    val items: List<CartItem>,
    val total: Double,
    val paymentMethod: String
)
