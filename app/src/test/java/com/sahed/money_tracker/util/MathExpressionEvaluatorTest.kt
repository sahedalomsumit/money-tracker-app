package com.sahed.money_tracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MathExpressionEvaluatorTest {

    @Test
    fun testUserRequestedParenthesesAndSubtraction() {
        // Specifically requested: (30+30)-10 should evaluate to 50
        val result = MathExpressionEvaluator.evaluate("(30+30)-10")
        assertNotNull(result)
        assertEquals(50.0, result!!, 0.0001)
        assertEquals("50", MathExpressionEvaluator.formatResult(result))
    }

    @Test
    fun testWithSpaces() {
        val result = MathExpressionEvaluator.evaluate("( 30 + 30 ) - 10")
        assertNotNull(result)
        assertEquals(50.0, result!!, 0.0001)
    }

    @Test
    fun testOperatorPrecedence() {
        // PEDMAS: 10 + 20 * 3 = 70, not 90
        val result = MathExpressionEvaluator.evaluate("10 + 20 * 3")
        assertEquals(70.0, result!!, 0.0001)

        // (10 + 20) * 3 = 90
        val resultParens = MathExpressionEvaluator.evaluate("(10 + 20) * 3")
        assertEquals(90.0, resultParens!!, 0.0001)
    }

    @Test
    fun testDivisionAndDecimals() {
        val result = MathExpressionEvaluator.evaluate("100 / 4 + 2.5 * 2")
        assertEquals(30.0, result!!, 0.0001)

        val resultDecimals = MathExpressionEvaluator.evaluate("10.5 + 2.25")
        assertEquals(12.75, resultDecimals!!, 0.0001)
        assertEquals("12.75", MathExpressionEvaluator.formatResult(resultDecimals))
    }

    @Test
    fun testAlternativeMultiplicationAndDivisionSymbols() {
        // Support unicode × and ÷
        val result = MathExpressionEvaluator.evaluate("(30 + 30) × 2 ÷ 4")
        assertEquals(30.0, result!!, 0.0001)
    }

    @Test
    fun testNegativeNumbersAndUnarySigns() {
        val result = MathExpressionEvaluator.evaluate("-10 + 50")
        assertEquals(40.0, result!!, 0.0001)

        val nestedUnary = MathExpressionEvaluator.evaluate("50 + (-10)")
        assertEquals(40.0, nestedUnary!!, 0.0001)
    }

    @Test
    fun testIncompleteTypingRecovery() {
        // Unclosed parenthesis while user is still typing: "(30+30"
        val unclosed = MathExpressionEvaluator.evaluate("(30+30")
        assertEquals(60.0, unclosed!!, 0.0001)

        // Trailing operator while user is still typing: "30+"
        val trailing = MathExpressionEvaluator.evaluate("30+")
        assertEquals(30.0, trailing!!, 0.0001)
    }

    @Test
    fun testContainsOperator() {
        assertTrue(MathExpressionEvaluator.containsOperator("(30+30)-10"))
        assertTrue(MathExpressionEvaluator.containsOperator("50*2"))
        assertTrue(MathExpressionEvaluator.containsOperator("50/2"))
        assertTrue(MathExpressionEvaluator.containsOperator("50×2"))
        assertTrue(MathExpressionEvaluator.containsOperator("50÷2"))
        assertTrue(!MathExpressionEvaluator.containsOperator("5000"))
        assertTrue(!MathExpressionEvaluator.containsOperator("5000.50"))
    }

    @Test
    fun testInvalidOrZeroDivisionHandling() {
        // Division by zero should safely return null
        assertNull(MathExpressionEvaluator.evaluate("50 / 0"))

        // Blank or non-math gibberish
        assertNull(MathExpressionEvaluator.evaluate(""))
        assertNull(MathExpressionEvaluator.evaluate("   "))
        assertNull(MathExpressionEvaluator.evaluate("abc"))
    }
}
