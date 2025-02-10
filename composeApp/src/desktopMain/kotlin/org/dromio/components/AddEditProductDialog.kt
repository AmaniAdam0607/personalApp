package org.dromio.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.dromio.models.Product

@Composable
fun AddEditProductDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, sellingPrice: Double, buyingPrice: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var buyingPrice by remember { mutableStateOf(product?.buyingPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (product == null) "Add Product" else "Edit Product",
                    style = MaterialTheme.typography.h6
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") }
                )

                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") }
                )

                OutlinedTextField(
                    value = buyingPrice,
                    onValueChange = { buyingPrice = it },
                    label = { Text("Buying Price") }
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Quantity") }
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(
                                name,
                                sellingPrice.toDoubleOrNull() ?: 0.0,
                                buyingPrice.toDoubleOrNull() ?: 0.0,
                                stock.toIntOrNull() ?: 0
                            )
                        },
                        enabled = name.isNotEmpty() &&
                                 sellingPrice.toDoubleOrNull() != null &&
                                 buyingPrice.toDoubleOrNull() != null &&
                                 stock.toIntOrNull() != null
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
