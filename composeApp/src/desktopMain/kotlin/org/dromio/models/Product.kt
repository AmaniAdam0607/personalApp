package org.dromio.models

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val stockQuantity: Int
)
