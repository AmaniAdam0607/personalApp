package org.dromio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.components.DateRangePicker
import org.dromio.components.SummaryCard
import org.dromio.models.ReportType
import org.dromio.models.SaleDetail
import org.dromio.repository.ReportsRepository
import org.dromio.utils.Formatters
import java.time.LocalDateTime
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.horizontalScroll

private val reportsRepository = ReportsRepository()

@Composable
fun ReportsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedReport by remember { mutableStateOf(ReportType.SALES) }
    var startDate by remember { mutableStateOf(LocalDateTime.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDateTime.now()) }

    val sales by produceState(initialValue = emptyList<SaleDetail>(), startDate, endDate, searchQuery) {
        value = reportsRepository.getSalesReport(startDate, endDate, searchQuery)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Title and Date Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sales Report", style = MaterialTheme.typography.h5)
            DateRangePicker(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it }
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search products...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Summary Cards with fixed width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryCard(
                title = "Total Sales",
                value = Formatters.formatMoney(sales.sumOf { it.total }),
                color = Colors.Primary,
                modifier = Modifier.width(280.dp)
            )
            SummaryCard(
                title = "Total Profit",
                value = Formatters.formatMoney(sales.sumOf { it.profit }),
                color = Colors.Secondary,
                modifier = Modifier.width(280.dp)
            )
            SummaryCard(
                title = "Items Sold",
                value = sales.sumOf { it.quantity }.toString(),
                color = Colors.Primary,
                modifier = Modifier.width(280.dp)
            )
        }

        // Sales Table with scrollbars
        Card(
            modifier = Modifier.weight(1f),
            backgroundColor = Colors.Surface,
            elevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val verticalScrollState = rememberLazyListState()

                LazyColumn(
                    state = verticalScrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 12.dp) // Space for scrollbar
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Colors.Primary.copy(alpha = 0.1f))
                                .padding(8.dp),
                        ) {
                            TableHeaderCell("Date", 0.15f)
                            TableHeaderCell("Product", 0.25f) // Increased width
                            TableHeaderCell("Qty", 0.1f)
                            TableHeaderCell("Buy Price", 0.125f)
                            TableHeaderCell("Sell Price", 0.125f)
                            TableHeaderCell("Total", 0.125f)
                            TableHeaderCell("Profit", 0.125f)
                        }
                        Divider()
                    }

                    // Content
                    items(sales) { sale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .background(
                                    if (sale.profit < 0) Colors.Primary.copy(alpha = 0.05f)
                                    else Color.Transparent
                                ),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(Formatters.formatDate(sale.date), 0.15f)
                            TableCell(sale.productName, 0.25f) // Increased width
                            TableCell(sale.quantity.toString(), 0.1f)
                            TableCell(Formatters.formatMoney(sale.buyingPrice), 0.125f)
                            TableCell(Formatters.formatMoney(sale.sellingPrice), 0.125f)
                            TableCell(Formatters.formatMoney(sale.total), 0.125f)
                            TableCell(
                                Formatters.formatMoney(sale.profit),
                                0.125f,
                                if (sale.profit < 0) MaterialTheme.colors.error else Colors.TextPrimary
                            )
                        }
                        Divider(color = Colors.Primary.copy(alpha = 0.1f))
                    }

                    // Footer with totals
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Colors.Primary.copy(alpha = 0.1f))
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTALS",
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.fillMaxWidth(0.5f),
                                color = Colors.Primary
                            )
                            TableCell("Items: ${sales.sumOf { it.quantity }}", 0.125f)
                            Spacer(Modifier.width(24.dp))
                            TableCell(
                                "Total: ${Formatters.formatMoney(sales.sumOf { it.total })}",
                                0.125f
                            )
                            TableCell(
                                "Profit: ${Formatters.formatMoney(sales.sumOf { it.profit })}",
                                0.25f,
                                Colors.Primary
                            )
                        }
                    }
                }

                // Vertical scrollbar
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(verticalScrollState)
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, widthFraction: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle2,
        color = Colors.Primary,
        modifier = Modifier.fillMaxWidth(widthFraction),
        textAlign = TextAlign.Start,
        maxLines = 1
    )
}

@Composable
private fun TableCell(
    text: String,
    widthFraction: Float,
    textColor: Color = Colors.TextPrimary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.fillMaxWidth(widthFraction),
        textAlign = TextAlign.Start,
        maxLines = 1,
        color = textColor
    )
}
