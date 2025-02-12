package org.dromio.models

import java.time.LocalDateTime

data class Debt(
    val id: Int,
    val customerName: String,
    val transactionId: String,
    val amount: Double,
    val isPaid: Boolean,
    val createdAt: LocalDateTime,
    val paidAt: LocalDateTime?
)
