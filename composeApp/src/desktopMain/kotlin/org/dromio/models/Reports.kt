package org.dromio.models

import java.time.LocalDateTime

enum class ReportType {
    SALES,
    INVENTORY,
    PROFITS
}

data class SaleDetail(
    val id: String,
    val date: LocalDateTime,
    val productName: String,
    val quantity: Int,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val total: Double,
    val profit: Double
)
