package org.dromio.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.Constants.CURRENCY_SYMBOL
import org.dromio.Constants.TAX_RATE
import org.dromio.utils.rotate
import org.dromio.models.Product
import org.dromio.repository.ProductRepository
import org.dromio.repository.TransactionRepository

private val productRepository = ProductRepository()
private val transactionRepository = TransactionRepository()

data class CartItem(val product: Product, val quantity: Int = 1)

@Composable
fun POSScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var paymentMethod by remember { mutableStateOf("Cash") }
    val products = remember { mutableStateListOf<Product>() }

    LaunchedEffect(searchQuery) {
        products.clear()
        products.addAll(
            if (searchQuery.isEmpty()) {
                productRepository.getAllProducts()
            } else {
                productRepository.searchProducts(searchQuery)
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel - Product Search and Grid (66% of space)
        Column(
            modifier = Modifier.fillMaxWidth(0.66f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductSearch(searchQuery, onSearchChange = { searchQuery = it })
            ProductGrid(
                products = products.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                },
                onAddToCart = { product ->
                    if (product.stockQuantity > 0) {
                        cartItems.addOrUpdate(product)
                    }
                }
            )
        }

        // Right Panel - Cart and Payment
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Cart(
                items = cartItems,
                onRemoveItem = { cartItems.remove(it) },
                modifier = Modifier.weight(1f)
            )

            PaymentSection(
                items = cartItems,
                paymentMethod = paymentMethod,
                onPaymentMethodChange = { paymentMethod = it },
                onProcessSale = {
                    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
                    val tax = subtotal * TAX_RATE
                    val total = subtotal + tax

                    transactionRepository.createTransaction(
                        items = cartItems.toList(),
                        total = total,
                        paymentMethod = paymentMethod
                    )
                    cartItems.clear()
                }
            )
        }
    }
}

@Composable
private fun ProductSearch(query: String, onSearchChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onSearchChange,
        label = { Text("Search products...") },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Colors.Secondary,
            cursorColor = Colors.TextPrimary
        )
    )
}

@Composable
private fun ProductGrid(products: List<Product>, onAddToCart: (Product) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product -> ProductItem(product, onAddToCart) }
    }
}

@Composable
private fun ProductItem(product: Product, onAddToCart: (Product) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Colors.Secondary.copy(0.1f)),
        elevation = 2.dp,
        backgroundColor = Colors.Surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(product.name, style = MaterialTheme.typography.h6, color = Colors.TextPrimary)
                Text(
                    "$CURRENCY_SYMBOL${String.format("%.2f", product.price)}",
                    color = Colors.TextSecondary
                )
                Text(
                    "In Stock: ${product.stockQuantity}",
                    color = if (product.stockQuantity > 0) Colors.TextSecondary else MaterialTheme.colors.error
                )
            }
            IconButton(
                onClick = { onAddToCart(product) },
                modifier = Modifier.background(Colors.Primary, RoundedCornerShape(8.dp)),
                enabled = product.stockQuantity > 0
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add to cart",
                    tint = if (product.stockQuantity > 0) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun Cart(
    items: List<CartItem>,
    onRemoveItem: (CartItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Colors.Surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text("Shopping Cart", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cart is empty")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item -> CartItemRow(item, onRemoveItem) }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onRemove: (CartItem) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            Text(item.product.name)
            Text("Qty: ${item.quantity} x $${String.format("%.2f", item.product.price)}")
        }
        IconButton(onClick = { onRemove(item) }) {
            Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colors.error)
        }
    }
}

@Composable
private fun PaymentSection(
    items: List<CartItem>,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onProcessSale: () -> Unit
) {
    val subtotal = items.sumOf { it.product.price * it.quantity }
    val tax = subtotal * TAX_RATE
    val total = subtotal + tax

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Colors.Surface,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple Payment Method Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                var expanded by remember { mutableStateOf(false) }
                val methods = listOf("Cash", "Card", "Mobile Payment")

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                "Select payment method",
                                Modifier.rotate(if (expanded) 180f else 0f)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    methods.forEach { method ->
                        DropdownMenuItem(
                            onClick = {
                                onPaymentMethodChange(method)
                                expanded = false
                            }
                        ) {
                            Text(method)
                        }
                    }
                }
            }

            // Totals
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TotalRow("Subtotal:", subtotal)
                TotalRow("Tax (${(TAX_RATE * 100).toInt()}%):", tax)
                Divider()
                TotalRow("Total:", total, true)
            }

            // Process Sale Button
            Button(
                onClick = onProcessSale,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Primary),
                enabled = items.isNotEmpty()
            ) {
                Text(
                    "Process Sale (${CURRENCY_SYMBOL}${String.format("%.2f", total)})",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: Double, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.h6 else MaterialTheme.typography.body1
        )
        Text(
            "$CURRENCY_SYMBOL${String.format("%.2f", amount)}",
            style = if (isTotal) MaterialTheme.typography.h6 else MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun DiscountInput(code: String, onDiscountChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = code,
            onValueChange = onDiscountChange,
            label = { Text("Discount Code") },
            modifier = Modifier.fillMaxWidth(0.7f)
        )
        Button(
            onClick = { /* Validate discount code */ },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("Apply")
        }
    }
}

@Composable
private fun SplitSelector(count: Int, onSplitChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Split Payment:", modifier = Modifier.padding(end = 8.dp))
        NumberSelector(count, onSplitChange)
    }
}

@Composable
private fun PaymentMethodSelector(method: String, onPaymentMethodChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val methods = listOf("Cash", "Card", "Mobile")

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = method,
            onValueChange = {},
            label = { Text("Payment Method") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Select Payment Method")
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            methods.forEach { methodOption ->
                DropdownMenuItem(
                    onClick = {
                        onPaymentMethodChange(methodOption)
                        expanded = false
                    }
                ) {
                    Text(text = methodOption)
                }
            }
        }
    }
}

@Composable
private fun NumberSelector(count: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (count > 1) onChange(count - 1) }) {
            Icon(Icons.Default.Close, "Decrease")
        }
        Text("$count", modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { onChange(count + 1) }) {
            Icon(Icons.Default.Add, "Increase")
        }
    }
}

private fun MutableList<CartItem>.addOrUpdate(product: Product) {
    val existing = find { it.product.id == product.id }
    if (existing != null) {
        val index = indexOf(existing)
        set(index, existing.copy(quantity = existing.quantity + 1))
    } else {
        add(CartItem(product))
    }
}
