package org.dromio.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dromio.Colors

data class StoreSettings(
    val storeName: String,
    val address: String,
    val phone: String,
    val email: String,
    val taxRate: Double,
    val currency: String,
    val receiptFooter: String,
    val printerName: String
)

@Composable
fun SettingsScreen() {
    var settings by remember {
        mutableStateOf(
            StoreSettings(
                storeName = "My Store",
                address = "123 Main St",
                phone = "(555) 123-4567",
                email = "store@example.com",
                taxRate = 8.5,
                currency = "USD",
                receiptFooter = "Thank you for shopping!",
                printerName = "Default Printer"
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.h5)

        // Store Information Card
        SettingsCard(title = "Store Information") {
            SettingItem("Store Name", settings.storeName) { settings = settings.copy(storeName = it) }
            SettingItem("Address", settings.address) { settings = settings.copy(address = it) }
            SettingItem("Phone", settings.phone) { settings = settings.copy(phone = it) }
            SettingItem("Email", settings.email) { settings = settings.copy(email = it) }
        }

        // Financial Settings Card
        SettingsCard(title = "Financial Settings") {
            SettingItem(
                "Tax Rate (%)",
                settings.taxRate.toString(),
                numberOnly = true
            ) { settings = settings.copy(taxRate = it.toDoubleOrNull() ?: settings.taxRate) }
            SettingItem("Currency", settings.currency) { settings = settings.copy(currency = it) }
        }

        // Receipt Settings Card
        SettingsCard(title = "Receipt Settings") {
            SettingItem("Footer Message", settings.receiptFooter) {
                settings = settings.copy(receiptFooter = it)
            }
            SettingItem("Printer Name", settings.printerName) {
                settings = settings.copy(printerName = it)
            }
        }

        // Save Button
        Button(
            onClick = { /* Save settings */ },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Primary)
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save")
            Spacer(Modifier.width(8.dp))
            Text("Save Settings", color = MaterialTheme.colors.onPrimary)
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Colors.Surface,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.h6)
            content()
        }
    }
}

@Composable
private fun SettingItem(
    label: String,
    value: String,
    numberOnly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.3f),
            color = Colors.TextPrimary
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (!numberOnly || newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.weight(0.7f),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Colors.Secondary,
                unfocusedBorderColor = Colors.TextSecondary.copy(alpha = 0.5f)
            )
        )
    }
}
