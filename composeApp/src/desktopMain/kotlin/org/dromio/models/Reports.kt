package org.dromio.models

import java.time.LocalDateTime

data class SaleDetail(
    val id: String,
    val productName: String,
    val quantity: Int,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val total: Double,
    val profit: Double,
    val date: LocalDateTime
)

enum class ReportType {
    SALES, PROFIT, DEBTS
}
