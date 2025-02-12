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
import androidx.compose.ui.unit.Dp
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Total Sales",
                value = Formatters.formatMoney(sales.sumOf { it.total }),
                color = Colors.Primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Profit",
                value = Formatters.formatMoney(sales.sumOf { it.profit }),
                color = Colors.Secondary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Items Sold",
                value = sales.sumOf { it.quantity }.toString(),
                color = Colors.Primary,
                modifier = Modifier.weight(1f)
            )
        }

        // Sales Table with scrollbars
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            backgroundColor = Colors.Surface,
            elevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val horizontalScrollState = rememberScrollState()
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
                                .horizontalScroll(horizontalScrollState)
                                .background(Colors.Primary.copy(alpha = 0.1f))
                                .padding(8.dp),
                        ) {
                            TableHeaderCell("Date", 180.dp)
                            TableHeaderCell("Product", 300.dp)
                            TableHeaderCell("Qty", 100.dp)
                            TableHeaderCell("Buy Price", 150.dp)
                            TableHeaderCell("Sell Price", 150.dp)
                            TableHeaderCell("Total", 150.dp)
                            TableHeaderCell("Profit", 150.dp)
                        }
                        Divider()
                    }

                    // Content
                    items(sales) { sale ->
                        Row(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .background(
                                    if (sale.profit < 0) Colors.Primary.copy(alpha = 0.05f)
                                    else Color.Transparent
                                ),
                        ) {
                            TableCell(Formatters.formatDate(sale.date), 180.dp)
                            TableCell(sale.productName, 300.dp)
                            TableCell(sale.quantity.toString(), 100.dp)
                            TableCell(Formatters.formatMoney(sale.buyingPrice), 150.dp)
                            TableCell(Formatters.formatMoney(sale.sellingPrice), 150.dp)
                            TableCell(Formatters.formatMoney(sale.total), 150.dp)
                            TableCell(
                                Formatters.formatMoney(sale.profit),
                                150.dp,
                                if (sale.profit < 0) MaterialTheme.colors.error else Colors.TextPrimary
                            )
                        }
                        Divider(color = Colors.Primary.copy(alpha = 0.1f))
                    }
                }

                // Scrollbars
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(verticalScrollState)
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle2,
        color = Colors.Primary,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Start,
        maxLines = 1
    )
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    textColor: Color = Colors.TextPrimary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Start,
        maxLines = 1,
        color = textColor
    )
}
