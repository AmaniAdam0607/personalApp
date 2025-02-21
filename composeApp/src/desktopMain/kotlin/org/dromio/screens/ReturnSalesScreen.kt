package org.dromio.screens

import androidx.compose.foundation.clickable // Add this import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import org.dromio.Colors
import org.dromio.Constants.CURRENCY_SYMBOL
import org.dromio.models.Transaction
import org.dromio.repository.TransactionRepository

@Composable
fun ReturnSalesScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    val transactionRepository = remember { TransactionRepository() }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(searchQuery) {
        transactions = transactionRepository.getReturnableTransactions(50)
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Return Sales", style = MaterialTheme.typography.h5)

        OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search transactions") },
                modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth().weight(1f), elevation = 4.dp) {
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(
                            transaction = transaction,
                            onClick = { selectedTransaction = transaction }
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        ReturnDialog(
                transaction = transaction,
                onDismiss = { selectedTransaction = null },
                onConfirm = {
                    transactionRepository.returnTransaction(transaction)
                    selectedTransaction = null
                    // Refresh the transactions list
                    transactions = transactionRepository.getRecentTransactions(50)
                }
        )
    }
}

@Composable
private fun TransactionCard(transaction: Transaction, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp, backgroundColor = Colors.Surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable(onClick = onClick)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transaction #${transaction.id}", style = MaterialTheme.typography.h6)
                Text(transaction.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            }
            Spacer(Modifier.height(8.dp))
            transaction.items.forEach { item ->
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.product.name} x${item.quantity}")
                    Text("$CURRENCY_SYMBOL${item.product.sellingPrice * item.quantity}")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.subtitle1)
                Text(
                        "$CURRENCY_SYMBOL${transaction.total}",
                        style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}

@Composable
private fun ReturnDialog(transaction: Transaction, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Return") },
            text = {
                Column {
                    Text("Are you sure you want to return this transaction?")
                    Text("This will:")
                    Text("• Return all items to inventory")
                    Text("• Mark the transaction as returned")
                    Text("• Cannot be undone")
                }
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.error
                                )
                ) { Text("Confirm Return", color = MaterialTheme.colors.onError) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
