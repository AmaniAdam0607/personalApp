package org.dromio.models

data class CartItem(
    val product: Product,
    val quantity: Int = 1
)
