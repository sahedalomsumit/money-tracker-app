package com.sahed.money_tracker.util

import java.util.Locale

/**
 * Evaluates mathematical expressions directly in amount/salary input fields.
 * Supports +, -, *, /, (, ), decimals, and operator precedence (PEDMAS).
 */
object MathExpressionEvaluator {

    /**
     * Checks if the string contains any math operator or parenthesis.
     */
    fun containsOperator(expression: String?): Boolean {
        if (expression.isNullOrBlank()) return false
        return expression.any { it == '+' || it == '-' || it == '*' || it == '×' || it == '/' || it == '÷' || it == '(' || it == ')' }
    }

    /**
     * Evaluates a mathematical expression and returns the resulting Double,
     * or null if the expression is invalid, blank, or causes division by zero.
     */
    fun evaluate(expression: String?): Double? {
        if (expression.isNullOrBlank()) return null

        val sanitized = expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace(" ", "")

        if (sanitized.isEmpty()) return null

        return try {
            val parser = ExpressionParser(sanitized)
            val result = parser.parse()
            if (result.isNaN() || result.isInfinite()) null else result
        } catch (e: Exception) {
            tryRelaxedRecovery(sanitized)
        }
    }

    /**
     * Formats evaluated number cleanly: 50.0 -> "50", 50.75 -> "50.75".
     */
    fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", value)
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }

    private fun tryRelaxedRecovery(input: String): Double? {
        var trimmed = input.trimEnd('+', '-', '*', '/')
        if (trimmed.isEmpty()) return null

        val openCount = trimmed.count { it == '(' }
        val closeCount = trimmed.count { it == ')' }
        if (openCount > closeCount) {
            trimmed += ")".repeat(openCount - closeCount)
        }

        return try {
            val parser = ExpressionParser(trimmed)
            val result = parser.parse()
            if (result.isNaN() || result.isInfinite()) null else result
        } catch (e: Exception) {
            null
        }
    }

    private class ExpressionParser(private val input: String) {
        private var pos = 0
        private val len = input.length

        private fun peek(): Char = if (pos < len) input[pos] else '\u0000'
        private fun get(): Char = if (pos < len) input[pos++] else '\u0000'

        fun parse(): Double {
            val result = parseExpression()
            if (pos < len) {
                throw IllegalArgumentException("Unexpected character '${peek()}' at index $pos")
            }
            return result
        }

        // expression = term (('+' | '-') term)*
        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                when (peek()) {
                    '+' -> {
                        get()
                        value += parseTerm()
                    }
                    '-' -> {
                        get()
                        value -= parseTerm()
                    }
                    else -> return value
                }
            }
        }

        // term = factor (('*' | '/') factor)*
        private fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                when (peek()) {
                    '*' -> {
                        get()
                        value *= parseFactor()
                    }
                    '/' -> {
                        get()
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        value /= divisor
                    }
                    else -> return value
                }
            }
        }

        // factor = ('+' | '-')? (number | '(' expression ')')
        private fun parseFactor(): Double {
            if (peek() == '+') {
                get()
                return parseFactor()
            }
            if (peek() == '-') {
                get()
                return -parseFactor()
            }
            if (peek() == '(') {
                get()
                val value = parseExpression()
                if (peek() == ')') {
                    get()
                } else {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                return value
            }

            return parseNumber()
        }

        private fun parseNumber(): Double {
            val start = pos
            if (peek() == '.') {
                get()
            }
            while (peek().isDigit() || peek() == '.') {
                get()
            }
            if (start == pos) {
                throw IllegalArgumentException("Expected number at index $pos")
            }
            val numStr = input.substring(start, pos)
            return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        }
    }
}
