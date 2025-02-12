package org.dromio.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.Constants.CURRENCY_SYMBOL
import org.dromio.repository.DebtRepository
import org.dromio.utils.Formatters

@Composable
fun DebtsScreen() {
    val debtRepository = remember { DebtRepository() }
    var debts by remember { mutableStateOf(debtRepository.getAllDebts()) }
    var showPaymentDialog by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Debts Management", style = MaterialTheme.typography.h5)

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(debts) { debt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(debt.customerName, style = MaterialTheme.typography.h6)
                                Text(
                                    "$CURRENCY_SYMBOL${Formatters.formatMoney(debt.amount)}",
                                    color = Colors.TextSecondary
                                )
                                Text(
                                    "Status: ${if (debt.isPaid) "Paid" else "Unpaid"}",
                                    color = if (debt.isPaid) Colors.Secondary else MaterialTheme.colors.error
                                )
                            }
                            if (!debt.isPaid) {
                                Button(
                                    onClick = { showPaymentDialog = debt.id },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Colors.Primary
                                    )
                                ) {
                                    Text("Receive Payment")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showPaymentDialog?.let { debtId ->
        AlertDialog(
            onDismissRequest = { showPaymentDialog = null },
            title = { Text("Confirm Payment") },
            text = { Text("Are you sure you want to mark this debt as paid?") },
            confirmButton = {
                Button(
                    onClick = {
                        debtRepository.markAsPaid(debtId)
                        debts = debtRepository.getAllDebts()
                        showPaymentDialog = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
