package org.dromio.utils

import org.dromio.Constants.CURRENCY_SYMBOL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Formatters {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun formatMoney(amount: Double): String = "$CURRENCY_SYMBOL ${String.format("%.2f", amount)}"
    fun formatDate(date: LocalDateTime): String = date.format(dateFormatter)
    fun formatDateOnly(date: LocalDateTime): String = date.format(dateOnlyFormatter)
}
