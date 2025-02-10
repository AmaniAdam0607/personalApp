package org.dromio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.repository.ProductRepository
import org.dromio.models.Product
import org.dromio.components.AddEditProductDialog
import org.dromio.Constants.CURRENCY_SYMBOL

private val productRepository = ProductRepository()

@Composable
fun InventoryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val products = remember { mutableStateListOf<Product>() }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        val updatedProducts = if (searchQuery.isEmpty()) {
            productRepository.getAllProducts()
        } else {
            productRepository.searchProducts(searchQuery)
        }
        products.clear()
        products.addAll(updatedProducts)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Inventory Management", style = MaterialTheme.typography.h5)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search inventory") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingProduct = null
                    showAddEditDialog = true
                },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(Icons.Default.Add, "Add product")
                Spacer(Modifier.width(8.dp))
                Text("Add Product")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            backgroundColor = Colors.Surface
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Colors.Primary.copy(0.1f))
                        .padding(8.dp)
                ) {
                    TableCell("ID", 0.1f)
                    TableCell("Product", 0.3f)
                    TableCell("Price", 0.2f)
                    TableCell("Stock", 0.2f)
                    TableCell("Actions", 0.2f)
                }

                // Content
                LazyColumn {
                    items(products) { product ->
                        InventoryRow(
                            item = product,
                            onEdit = {
                                editingProduct = it
                                showAddEditDialog = true
                            },
                            onDelete = {
                                productRepository.deleteProduct(it.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add product dialog
    if (showAddEditDialog) {
        AddEditProductDialog(
            product = editingProduct,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, sellingPrice, buyingPrice, stock ->
                if (editingProduct != null) {
                    productRepository.updateProduct(
                        editingProduct!!.id,
                        name,
                        sellingPrice,
                        buyingPrice,
                        stock
                    )
                } else {
                    productRepository.addProduct(
                        name,
                        sellingPrice,
                        buyingPrice,
                        stock
                    )
                }
                showAddEditDialog = false
            }
        )
    }
}

@Composable
private fun TableCell(text: String, widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun InventoryRow(
    item: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${item.id}",
            modifier = Modifier.fillMaxWidth(0.1f)
        )
        Text(
            text = item.name,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Text(
            text = "$CURRENCY_SYMBOL${item.sellingPrice}", // Changed here
            modifier = Modifier.fillMaxWidth(0.2f)
        )
        Text(
            text = "${item.stockQuantity}",  // Changed from stock to stockQuantity
            modifier = Modifier.fillMaxWidth(0.2f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.2f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colors.error)
            }
        }
    }
}
