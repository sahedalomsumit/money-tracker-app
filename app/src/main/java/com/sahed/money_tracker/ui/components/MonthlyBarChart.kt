package com.sahed.money_tracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.theme.ChartBestMonth
import com.sahed.money_tracker.ui.theme.ChartDefaultBar
import com.sahed.money_tracker.ui.theme.ChartMutedBar
import com.sahed.money_tracker.ui.theme.ChartWorstMonth
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.util.DateUtils

@Composable
fun MonthlyBarChart(
    monthlyTotals: Map<Int, Double>,
    currencySymbol: String,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(monthlyTotals) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    val maxAmount = remember(monthlyTotals) {
        (monthlyTotals.values.maxOrNull() ?: 0.0).coerceAtLeast(100.0)
    }

    // Best month has the highest income (> 0)
    val bestMonth = remember(monthlyTotals) {
        monthlyTotals.filter { it.value > 0 }.maxByOrNull { it.value }?.key
    }

    // Worst month has the lowest non-zero income, or if all > 0 have variation
    val worstMonth = remember(monthlyTotals, bestMonth) {
        val positiveMonths = monthlyTotals.filter { it.value > 0 }
        if (positiveMonths.size > 1) {
            positiveMonths.minByOrNull { it.value }?.key
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Chart Header with Selected Tooltip or Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Monthly Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedMonth != null) {
                    val selM = selectedMonth!!
                    val amt = monthlyTotals[selM] ?: 0.0
                    Text(
                        text = "${DateUtils.getMonthFullName(selM)}: ${CurrencyHelper.format(amt, currencySymbol, currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Tap a bar to inspect details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend tags
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(color = ChartBestMonth, text = "Peak")
                Spacer(modifier = Modifier.width(8.dp))
                if (worstMonth != null) {
                    LegendDot(color = ChartWorstMonth, text = "Low")
                    Spacer(modifier = Modifier.width(8.dp))
                }
                LegendDot(color = ChartDefaultBar, text = "Avg")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Bar Chart
        val axisColor = MaterialTheme.colorScheme.outline
        val textColor = MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(monthlyTotals) {
                        detectTapGestures { offset ->
                            val leftPadding = 50f
                            val rightPadding = 10f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val barSlot = chartWidth / 12f

                            if (offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val clickedIndex = ((offset.x - leftPadding) / barSlot).toInt().coerceIn(0, 11)
                                val month = clickedIndex + 1
                                selectedMonth = if (selectedMonth == month) null else month
                            }
                        }
                    }
            ) {
                val leftPadding = 80f
                val bottomPadding = 45f
                val topPadding = 20f
                val chartHeight = size.height - bottomPadding - topPadding
                val chartWidth = size.width - leftPadding - 20f

                // Draw horizontal guide lines (3 steps: 0, 50%, 100%)
                val steps = 3
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    isAntiAlias = true
                }

                for (i in 0..steps) {
                    val ratio = i.toFloat() / steps
                    val y = topPadding + chartHeight * (1f - ratio)
                    val value = maxAmount * ratio

                    // Dash line
                    drawLine(
                        color = axisColor.copy(alpha = 0.35f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Y-axis compact label
                    drawIntoCanvas { canvas ->
                        val label = CurrencyHelper.formatCompact(value, currencySymbol)
                        canvas.nativeCanvas.drawText(label, 0f, y + 8f, textPaint)
                    }
                }

                // Draw 12 month bars
                val slotWidth = chartWidth / 12f
                val barWidth = (slotWidth * 0.55f).coerceAtLeast(8f)

                for (month in 1..12) {
                    val amount = monthlyTotals[month] ?: 0.0
                    val normalizedHeight = ((amount / maxAmount) * chartHeight * animatedProgress.value).toFloat()
                    val barHeight = normalizedHeight.coerceAtLeast(6f) // Small minimum pill

                    val barX = leftPadding + (month - 1) * slotWidth + (slotWidth - barWidth) / 2f
                    val barY = topPadding + chartHeight - barHeight

                    val barColor = when {
                        month == selectedMonth -> Color.White
                        month == bestMonth && amount > 0 -> ChartBestMonth
                        month == worstMonth && amount > 0 -> ChartWorstMonth
                        amount > 0 -> ChartDefaultBar
                        else -> ChartMutedBar.copy(alpha = 0.4f)
                    }

                    // Draw bar with rounded top corners
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    // Month label at bottom
                    val monthLabel = DateUtils.getMonthShortName(month).take(1) // Single letter for compact mobile
                    drawIntoCanvas { canvas ->
                        val labelPaint = android.graphics.Paint().apply {
                            color = if (month == selectedMonth) {
                                android.graphics.Color.WHITE
                            } else {
                                android.graphics.Color.LTGRAY
                            }
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            isFakeBoldText = (month == selectedMonth || month == bestMonth)
                        }
                        canvas.nativeCanvas.drawText(
                            DateUtils.getMonthShortName(month),
                            barX + barWidth / 2f,
                            size.height - 10f,
                            labelPaint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
