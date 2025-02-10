package org.dromio.models

data class Product(
    val id: Int,
    val name: String,
    val sellingPrice: Double,
    val buyingPrice: Double,
    val stockQuantity: Int,
    val minimumStock: Int = 5,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profit: Double get() = sellingPrice - buyingPrice
    val profitMargin: Double get() = (profit / buyingPrice) * 100
    val lowStock: Boolean get() = stockQuantity <= minimumStock
}
