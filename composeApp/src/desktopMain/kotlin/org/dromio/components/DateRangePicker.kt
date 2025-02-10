package org.dromio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.dromio.utils.Formatters
import org.dromio.Colors

@Composable
fun DateRangePicker(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick select buttons
        QuickDateButton("Today", onClick = {
            val now = LocalDateTime.now()
            onStartDateChange(now.withHour(0).withMinute(0))
            onEndDateChange(now.withHour(23).withMinute(59))
        })

        QuickDateButton("Week", onClick = {
            val now = LocalDateTime.now()
            onStartDateChange(now.minusWeeks(1))
            onEndDateChange(now)
        })

        QuickDateButton("Month", onClick = {
            val now = LocalDateTime.now()
            onStartDateChange(now.minusMonths(1))
            onEndDateChange(now)
        })

        // Date inputs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("From:")
            Text(
                Formatters.formatDateOnly(startDate),
                modifier = Modifier.background(
                    color = Colors.Primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ).padding(8.dp)
            )

            Text("To:")
            Text(
                Formatters.formatDateOnly(endDate),
                modifier = Modifier.background(
                    color = Colors.Primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ).padding(8.dp)
            )
        }
    }
}

@Composable
private fun QuickDateButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Colors.Primary.copy(alpha = 0.1f)
        )
    ) {
        Text(text)
    }
}
