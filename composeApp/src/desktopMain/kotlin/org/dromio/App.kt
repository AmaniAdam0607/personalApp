// Main.kt
package org.dromio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Add screen enum
enum class Screen {
    POS,
    REPORTS,
    INVENTORY,
    SETTINGS
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.POS) }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Colors.Primary,
            secondary = Colors.Secondary,
            background = Colors.Background,
            surface = Colors.Surface,
            onPrimary = Color.White,
            onSecondary = Color.White
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationBar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    Screen.POS -> POSInterface()
                    Screen.REPORTS -> ReportsScreen()
                    Screen.INVENTORY -> InventoryScreen()
                    Screen.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun NavigationBar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Colors.Surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NavItem(
            icon = Icons.Default.ShoppingCart,
            label = "POS",
            selected = currentScreen == Screen.POS,
            onClick = { onScreenChange(Screen.POS) }
        )
        NavItem(
            icon = Icons.Default.Assessment,
            label = "Reports",
            selected = currentScreen == Screen.REPORTS,
            onClick = { onScreenChange(Screen.REPORTS) }
        )
        NavItem(
            icon = Icons.Default.Inventory,
            label = "Inventory",
            selected = currentScreen == Screen.INVENTORY,
            onClick = { onScreenChange(Screen.INVENTORY) }
        )
        NavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onScreenChange(Screen.SETTINGS) }
        )
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Colors.Primary else Colors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = if (selected) Colors.Primary else Colors.TextSecondary
        )
    }
}

@Composable
fun ReportsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Reports", style = MaterialTheme.typography.h5)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportCard("Today's Sales", "$1,234.56", Icons.Default.TrendingUp)
            ReportCard("Items Sold", "45", Icons.Default.ShoppingBasket)
            ReportCard("Average Sale", "$27.43", Icons.Default.Payment)
        }

        // Sales Chart
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            backgroundColor = Colors.Surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sales Overview", style = MaterialTheme.typography.h6)
                // Chart implementation would go here
            }
        }

        // Recent Transactions
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            backgroundColor = Colors.Surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recent Transactions", style = MaterialTheme.typography.h6)
                // Transaction list would go here
            }
        }
    }
}

@Composable
fun InventoryScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Inventory Management", style = MaterialTheme.typography.h5)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Search inventory") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { /* Add new product */ },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(Icons.Default.Add, "Add product")
                Text("Add Product")
            }
        }

        // Inventory Table
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            backgroundColor = Colors.Surface
        ) {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Colors.Primary.copy(0.1f))
                    ) {
                        TableHeader("ID", 0.1f)
                        TableHeader("Product", 0.3f)
                        TableHeader("Price", 0.2f)
                        TableHeader("Stock", 0.2f)
                        TableHeader("Actions", 0.2f)
                    }
                }
                // Table content would go here
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.h5)

        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Colors.Surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("General Settings", style = MaterialTheme.typography.h6)
                SettingItem("Store Name", "My Store")
                SettingItem("Tax Rate", "8.5%")
                SettingItem("Currency", "USD")
                SettingItem("Receipt Footer", "Thank you for shopping!")
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.weight(1f).height(120.dp),
        backgroundColor = Colors.Surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = Colors.Primary)
            Text(title, style = MaterialTheme.typography.caption)
            Text(value, style = MaterialTheme.typography.h6)
        }
    }
}

@Composable
private fun TableHeader(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(8.dp),
        style = MaterialTheme.typography.subtitle2
    )
}

@Composable
private fun SettingItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
fun POSInterface() {
  var searchQuery by remember { mutableStateOf("") }
  val cartItems = remember { mutableStateListOf<CartItem>() }
  var discountCode by remember { mutableStateOf("") }
  var splitCount by remember { mutableStateOf(1) }
  var paymentMethod by remember { mutableStateOf("Cash") }

  Row(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Left Panel - Product Search and Grid
    Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      ProductSearch(searchQuery, onSearchChange = { searchQuery = it })
      ProductGrid(
              products = sampleProducts.filter { it.name.contains(searchQuery, true) },
              onAddToCart = { product -> cartItems.addOrUpdate(product) }
      )
    }

    // Right Panel - Cart and Payment
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Cart(
              items = cartItems,
              onRemoveItem = { cartItems.remove(it) },
              modifier = Modifier.weight(1f)
      )

      DiscountInput(discountCode, onDiscountChange = { discountCode = it })
      SplitSelector(splitCount, onSplitChange = { splitCount = it })
      PaymentMethodSelector(paymentMethod, onPaymentMethodChange = { paymentMethod = it })
      TotalSection(items = cartItems, splitCount = splitCount, discountCode = discountCode)
    }
  }
}

// Product Components
@Composable
private fun ProductSearch(query: String, onSearchChange: (String) -> Unit) {
  OutlinedTextField(
          value = query,
          onValueChange = onSearchChange,
          label = { Text("Search products...") },
          modifier = Modifier.fillMaxWidth(),
          colors =
                  TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = Colors.Secondary,
                          cursorColor = Colors.TextPrimary
                  )
  )
}

@Composable
private fun ProductGrid(products: List<Product>, onAddToCart: (Product) -> Unit) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
    items(products) { product -> ProductItem(product, onAddToCart) }
  }
}

@Composable
private fun ProductItem(product: Product, onAddToCart: (Product) -> Unit) {
  Card(
          modifier = Modifier.fillMaxWidth().border(1.dp, Colors.Secondary.copy(0.1f)),
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
        Text("$${"%.2f".format(product.price)}", color = Colors.TextSecondary)
      }
      IconButton(
              onClick = { onAddToCart(product) },
              modifier = Modifier.background(Colors.Primary, RoundedCornerShape(8.dp))
      ) { Icon(Icons.Default.Add, "Add to cart", tint = Color.White) }
    }
  }
}

// Cart Components
@Composable
private fun Cart(
        items: List<CartItem>,
        onRemoveItem: (CartItem) -> Unit,
        modifier: Modifier = Modifier
) {
  Card(modifier = modifier, elevation = 4.dp, backgroundColor = Colors.Surface) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Shopping Cart", style = MaterialTheme.typography.h6)

      if (items.isEmpty()) {
        Spacer(modifier = Modifier.weight(1f))
        Text("Cart is empty", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.weight(1f))
      } else {
        LazyColumn(modifier = Modifier.weight(1f)) {
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
    Column(modifier = Modifier.weight(1f)) {
      Text(item.product.name)
      Text("Qty: ${item.quantity} x $${"%.2f".format(item.product.price)}")
    }
    IconButton(onClick = { onRemove(item) }) {
      Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colors.error)
    }
  }
}

// Payment Components
@Composable
private fun TotalSection(items: List<CartItem>, splitCount: Int, discountCode: String) {
  val subtotal = items.sumOf { it.product.price * it.quantity }
  val taxRate = 0.085
  val tax = subtotal * taxRate
  val discount = if (discountCode == "SALE10") subtotal * 0.10 else 0.0
  val total = subtotal + tax - discount
  val splitAmount = total / splitCount

  Column(
          modifier =
                  Modifier.fillMaxWidth()
                          .background(Colors.Surface, RoundedCornerShape(8.dp))
                          .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    TotalRow("Subtotal:", subtotal)
    TotalRow("Tax (8.5%):", tax)
    TotalRow("Discount:", -discount)
    Divider()
    TotalRow("Total:", total)
    if (splitCount > 1) {
      Text(
              "Split into $splitCount: $${"%.2f".format(splitAmount)} each",
              style = MaterialTheme.typography.caption
      )
    }
    Button(
            onClick = { /* Handle checkout */},
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Primary)
    ) { Text("Complete Payment") }
  }
}

@Composable
private fun TotalRow(label: String, amount: Double) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label)
    Text("$${"%.2f".format(amount)}")
  }
}

// Helper Components
@Composable
private fun DiscountInput(code: String, onDiscountChange: (String) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    OutlinedTextField(
            value = code,
            onValueChange = onDiscountChange,
            label = { Text("Discount Code") },
            modifier = Modifier.weight(1f)
    )
    Button(onClick = { /* Validate discount code */}, modifier = Modifier.padding(start = 8.dp)) {
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
                Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Payment Method"
                )
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
        ) { Text(text = methodOption) }
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
    IconButton(onClick = { onChange(count + 1) }) { Icon(Icons.Default.Add, "Increase") }
  }
}

// Data Classes
data class Product(val id: Int, val name: String, val price: Double)

data class CartItem(val product: Product, val quantity: Int = 1)

private fun MutableList<CartItem>.addOrUpdate(product: Product) {
  val existing = find { it.product.id == product.id }
  if (existing != null) {
    val index = indexOf(existing)
    set(index, existing.copy(quantity = existing.quantity + 1))
  } else {
    add(CartItem(product))
  }
}

// Sample Data
private val sampleProducts =
        listOf(
                Product(1, "Laptop", 999.99),
                Product(2, "Mouse", 25.50),
                Product(3, "Keyboard", 49.99),
                Product(4, "Monitor", 199.95)
        )
