package com.sahed.money_tracker.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyHelper {
    private val standardFormat: DecimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val wholeNumberFormat: DecimalFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    fun format(amount: Double, symbol: String, code: String = ""): String {
        val formatted = if (amount % 1.0 == 0.0) {
            wholeNumberFormat.format(amount)
        } else {
            standardFormat.format(amount)
        }
        return if (symbol.isNotBlank()) {
            "$symbol $formatted"
        } else if (code.isNotBlank()) {
            "$code $formatted"
        } else {
            formatted
        }
    }

    fun formatCompact(amount: Double, symbol: String): String {
        val sym = if (symbol.isNotBlank()) "$symbol " else ""
        return when {
            amount >= 1_000_000 -> "$sym${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$sym${String.format(Locale.US, "%.1fk", amount / 1_000)}"
            else -> "$sym${wholeNumberFormat.format(amount)}"
        }
    }
}
