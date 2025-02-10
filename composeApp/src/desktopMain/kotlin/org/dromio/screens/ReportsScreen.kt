package org.dromio.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.repository.TransactionRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Transaction(
    val id: String,
    val timestamp: LocalDateTime,
    val items: List<CartItem>,
    val total: Double,
    val paymentMethod: String
)

private val transactionRepository = TransactionRepository()

@Composable
fun ReportsScreen() {
    var selectedPeriod by remember { mutableStateOf("Today") }
    val transactions = remember { mutableStateListOf<Transaction>() }
    val periods = listOf("Today", "Week", "Month", "Year")

    LaunchedEffect(selectedPeriod) {
        val recentTransactions = transactionRepository.getRecentTransactions()
        transactions.clear()
        transactions.addAll(recentTransactions)
    }

    // Calculate statistics
    val totalSales = transactions.sumOf { it.total }
    val itemsSold = transactions.sumOf { transaction ->
        transaction.items.sumOf { it.quantity }
    }
    val averageSale = if (transactions.isNotEmpty()) {
        totalSales / transactions.size
    } else 0.0

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sales Reports", style = MaterialTheme.typography.h5)
            Box {
                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedPeriod)
                    Icon(Icons.Default.ArrowDropDown, "Select period")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(onClick = {
                            selectedPeriod = period
                            expanded = false
                        }) {
                            Text(period)
                        }
                    }
                }
            }
        }

        // Statistics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportCard(
                title = "Total Sales",
                value = "$${String.format("%.2f", totalSales)}",
                icon = Icons.Default.TrendingUp
            )
            ReportCard(
                title = "Items Sold",
                value = "$itemsSold",
                icon = Icons.Default.ShoppingBasket
            )
            ReportCard(
                title = "Average Sale",
                value = "$${String.format("%.2f", averageSale)}",
                icon = Icons.Default.Payment
            )
            ReportCard(
                title = "Customers",
                value = "32",
                icon = Icons.Default.Person
            )
        }

        // Charts Section
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            backgroundColor = Colors.Surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Sales Overview", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                // Placeholder for chart
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sales Chart Coming Soon", color = Colors.TextSecondary)
                }
            }
        }

        // Recent Transactions
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            backgroundColor = Colors.Surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Recent Transactions", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionRow(transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        backgroundColor = Colors.Surface,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = Colors.Primary)
            Column {
                Text(title, style = MaterialTheme.typography.caption, color = Colors.TextSecondary)
                Text(value, style = MaterialTheme.typography.h6)
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Order ${transaction.id}",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = transaction.timestamp.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                ),
                style = MaterialTheme.typography.caption,
                color = Colors.TextSecondary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${String.format("%.2f", transaction.total)}",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = transaction.paymentMethod,
                style = MaterialTheme.typography.caption,
                color = Colors.TextSecondary
            )
        }
    }
}
