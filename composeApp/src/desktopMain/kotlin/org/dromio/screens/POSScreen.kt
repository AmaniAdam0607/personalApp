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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.Constants.CURRENCY_SYMBOL
import org.dromio.utils.rotate
import org.dromio.models.Product
import org.dromio.repository.ProductRepository
import org.dromio.repository.TransactionRepository
import org.dromio.models.CartItem
import org.dromio.repository.DebtRepository

private val productRepository = ProductRepository()
private val transactionRepository = TransactionRepository()
private val debtRepository = DebtRepository() // Add repository instance

@Composable
fun POSScreen(onLogout: () -> Unit = {}) {  // Add logout callback
    var searchQuery by remember { mutableStateOf("") }
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var products by remember { mutableStateOf(emptyList<Product>()) }

    // Load products initially and when search changes
    LaunchedEffect(searchQuery) {
        products = if (searchQuery.isEmpty()) {
            productRepository.getAllProducts()
        } else {
            productRepository.searchProducts(searchQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Add logout button in header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Point of Sale", style = MaterialTheme.typography.h5)
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = MaterialTheme.colors.onError)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Rest of existing POS layout
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Panel - Product Search and Grid (66% of space)
            Column(
                modifier = Modifier.fillMaxWidth(0.66f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProductSearch(searchQuery, onSearchChange = { searchQuery = it })
                ProductGrid(
                    products = products,  // Use the loaded products
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
                    onProcessSale = {
                        val total = cartItems.sumOf {
                            it.product.sellingPrice * it.quantity.toDouble()
                        }

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
                    "$CURRENCY_SYMBOL${String.format("%.2f", product.sellingPrice)}",
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
            Text("Qty: ${item.quantity} x ${CURRENCY_SYMBOL}${String.format("%.2f", item.product.sellingPrice)}")
        }
        IconButton(onClick = { onRemove(item) }) {
            Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colors.error)
        }
    }
}

@Composable
private fun PaymentDialog(
    total: Double,
    onDismiss: () -> Unit,
    onProcessSale: (isDebt: Boolean, customerName: String?) -> Unit
) {
    var isDebt by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Process Sale") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount:")
                    Text("$CURRENCY_SYMBOL${String.format("%.2f", total)}")
                }

                if (isDebt) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = { isDebt = true },
                    enabled = !isDebt,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Colors.Secondary
                    )
                ) {
                    Text("Add as Debt")
                }
                Button(
                    onClick = { onProcessSale(isDebt, if (isDebt) customerName else null) },
                    enabled = !isDebt || customerName.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Colors.Primary
                    )
                ) {
                    Text(if (isDebt) "Save Debt" else "Receive Payment")
                }
            }
        }
    )
}

@Composable
private fun PaymentSection(
    items: List<CartItem>,
    onProcessSale: () -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    val total = items.sumOf { it.product.sellingPrice * it.quantity }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Total:",
                style = MaterialTheme.typography.h6
            )
            Text(
                "$CURRENCY_SYMBOL${String.format("%.2f", total)}",
                style = MaterialTheme.typography.h6
            )
        }

        Button(
            onClick = { showPaymentDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Primary),
            enabled = items.isNotEmpty()
        ) {
            Text("Process Sale", color = Color.White)
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            total = total,
            onDismiss = { showPaymentDialog = false },
            onProcessSale = { isDebt, customerName ->
                if (isDebt && customerName != null) {
                    // Process as debt
                    val transactionId = transactionRepository.createTransaction(
                        items = items,
                        total = total,
                        paymentMethod = "DEBT"
                    )
                    debtRepository.createDebt(customerName, transactionId, total) // Use the instance
                } else {
                    // Process as normal sale
                    transactionRepository.createTransaction(
                        items = items,
                        total = total,
                        paymentMethod = "CASH"
                    )
                }
                onProcessSale()
                showPaymentDialog = false
            }
        )
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
