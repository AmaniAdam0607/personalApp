package org.dromio.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDateTime

@Composable
fun CalendarDialog(
    selected: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simple number inputs for now (can be enhanced with calendar UI later)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // Day
                    OutlinedTextField(
                        value = selected.dayOfMonth.toString(),
                        onValueChange = { day ->
                            day.toIntOrNull()?.let { d ->
                                if (d in 1..31) {
                                    onDateSelected(selected.withDayOfMonth(d))
                                }
                            }
                        },
                        label = { Text("Day") },
                        modifier = Modifier.width(80.dp)
                    )

                    // Month
                    OutlinedTextField(
                        value = selected.monthValue.toString(),
                        onValueChange = { month ->
                            month.toIntOrNull()?.let { m ->
                                if (m in 1..12) {
                                    onDateSelected(selected.withMonth(m))
                                }
                            }
                        },
                        label = { Text("Month") },
                        modifier = Modifier.width(80.dp)
                    )

                    // Year
                    OutlinedTextField(
                        value = selected.year.toString(),
                        onValueChange = { year ->
                            year.toIntOrNull()?.let { y ->
                                onDateSelected(selected.withYear(y))
                            }
                        },
                        label = { Text("Year") },
                        modifier = Modifier.width(100.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
