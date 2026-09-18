package com.sahed.money_tracker.ui.components

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.sin
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.ui.designsystem.components.EmeraldGlassCard
import com.sahed.money_tracker.ui.designsystem.components.EmeraldHeroBanner
import com.sahed.money_tracker.ui.designsystem.theme.BottomSheetShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.ui.theme.DonateColor
import com.sahed.money_tracker.ui.theme.InvestColor
import com.sahed.money_tracker.ui.theme.RestColor
import com.sahed.money_tracker.ui.theme.SavingColor
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.util.DateUtils

data class AllocationCategoryItem(
    val key: String,
    val label: String,
    val percent: Double,
    val amount: Double,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun TotalIncomeCard(
    totalSalary: Double,
    currencySymbol: String,
    currencyCode: String,
    year: Int,
    modifier: Modifier = Modifier
) {
    EmeraldHeroBanner(
        title = "Total Net Income • $year",
        value = CurrencyHelper.format(totalSalary, currencySymbol, currencyCode),
        subtitle = "Annual earnings logged for $year",
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = EmeraldPalette.EmeraldGlow,
                modifier = Modifier.size(32.dp)
            )
        }
    )
}

@Composable
fun AllocationBreakdownGrid(
    totalSalary: Double,
    allocation: AllocationSettings,
    currencySymbol: String,
    currencyCode: String,
    monthlyTotals: Map<Int, Double>,
    selectedYear: Int,
    modifier: Modifier = Modifier
) {
    var selectedCategoryForDetail by remember { mutableStateOf<AllocationCategoryItem?>(null) }

    // Filter out any categories where the allocation percentage is 0%
    val activeCategories = remember(allocation, totalSalary) {
        listOf(
            AllocationCategoryItem(
                key = "saving",
                label = allocation.savingLabel,
                percent = allocation.savingPercent,
                amount = totalSalary * (allocation.savingPercent / 100.0),
                color = SavingColor,
                icon = Icons.Default.Savings
            ),
            AllocationCategoryItem(
                key = "invest",
                label = allocation.investLabel,
                percent = allocation.investPercent,
                amount = totalSalary * (allocation.investPercent / 100.0),
                color = InvestColor,
                icon = Icons.AutoMirrored.Filled.ShowChart
            ),
            AllocationCategoryItem(
                key = "donate",
                label = allocation.donateLabel,
                percent = allocation.donatePercent,
                amount = totalSalary * (allocation.donatePercent / 100.0),
                color = DonateColor,
                icon = Icons.Default.VolunteerActivism
            ),
            AllocationCategoryItem(
                key = "rest",
                label = allocation.restLabel,
                percent = allocation.restPercent,
                amount = totalSalary * (allocation.restPercent / 100.0),
                color = RestColor,
                icon = Icons.Default.Weekend
            )
        ).filter { it.percent > 0.0 }
    }

    if (activeCategories.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            activeCategories.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        AllocationCategoryCard(
                            label = item.label,
                            percent = item.percent,
                            amount = item.amount,
                            currencySymbol = currencySymbol,
                            currencyCode = currencyCode,
                            color = item.color,
                            icon = item.icon,
                            onClick = { selectedCategoryForDetail = item },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If odd number of items, add a spacer to maintain uniform grid width
                    if (rowItems.size == 1 && activeCategories.size > 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Monthly breakdown bottom sheet when any card is tapped
    if (selectedCategoryForDetail != null) {
        AllocationMonthlyBreakdownBottomSheet(
            category = selectedCategoryForDetail!!,
            monthlyTotals = monthlyTotals,
            selectedYear = selectedYear,
            currencySymbol = currencySymbol,
            currencyCode = currencyCode,
            onDismissRequest = { selectedCategoryForDetail = null }
        )
    }
}

@Composable
fun AllocationCategoryCard(
    label: String,
    percent: Double,
    amount: Double,
    currencySymbol: String,
    currencyCode: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fillFraction = (percent.toFloat() / 100f).coerceIn(0f, 1f)

    EmeraldGlassCard(
        modifier = modifier,
        onClick = onClick,
        highlightColor = color,
        containerColor = EmeraldTheme.extended.surfaceTier2,
        cornerRadius = 18.dp
    ) {
        // Percentage-based color fill background with water wave effect waving at top
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(18.dp))
        ) {
            // Subtle base tint on the entire card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color.copy(alpha = 0.04f))
            )
            // Animated water wave filling from bottom to top
            WaterWaveBackground(
                fillFraction = fillFraction,
                color = color,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.0f", percent)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldTheme.extended.subText,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = CurrencyHelper.format(amount, currencySymbol, currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { fillFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap for details",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color.copy(alpha = 0.90f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WaterWaveBackground(
    fillFraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (fillFraction <= 0.001f) return

    val infiniteTransition = rememberInfiniteTransition(label = "waterWaveAnim")
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase1"
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase2"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val targetWaterHeight = h * fillFraction
        val baseY = h - targetWaterHeight

        // Amplitude smoothly dampens near 0% and 100% so it fits within card bounds
        val maxAmplitude = 5.dp.toPx()
        val ampFactor = (fillFraction * 4f).coerceAtMost(1f) * ((1f - fillFraction) * 4f).coerceAtMost(1f)
        val amplitude = maxAmplitude * ampFactor.coerceIn(0f, 1f)

        // 1. Secondary back wave (lighter alpha for 3D liquid depth)
        if (amplitude > 0.5f) {
            val backWavePath = Path().apply {
                moveTo(0f, h)
                val startY = baseY + (amplitude * 0.7f * sin(wavePhase2))
                lineTo(0f, startY)
                val step = 5f
                var x = 0f
                while (x <= w) {
                    val y = baseY + (amplitude * 0.7f * sin((2 * Math.PI * x / (w * 0.85f)).toFloat() + wavePhase2))
                    lineTo(x, y)
                    x += step
                }
                lineTo(w, h)
                close()
            }
            drawPath(
                path = backWavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.12f),
                        color.copy(alpha = 0.20f)
                    ),
                    startY = (baseY - amplitude).coerceAtLeast(0f),
                    endY = h
                )
            )
        }

        // 2. Primary front wave (richer alpha)
        val frontWavePath = Path().apply {
            moveTo(0f, h)
            val startY = baseY + (amplitude * sin(wavePhase1))
            lineTo(0f, startY)
            val step = 5f
            var x = 0f
            while (x <= w) {
                val y = baseY + (amplitude * sin((2 * Math.PI * x / w).toFloat() + wavePhase1))
                lineTo(x, y)
                x += step
            }
            lineTo(w, h)
            close()
        }

        drawPath(
            path = frontWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.20f),
                    color.copy(alpha = 0.34f)
                ),
                startY = (baseY - amplitude).coerceAtLeast(0f),
                endY = h
            )
        )

        // 3. Subtle crest highlight along the front wave edge
        if (amplitude > 0.5f) {
            val crestPath = Path().apply {
                val startY = baseY + (amplitude * sin(wavePhase1))
                moveTo(0f, startY)
                val step = 5f
                var x = 0f
                while (x <= w) {
                    val y = baseY + (amplitude * sin((2 * Math.PI * x / w).toFloat() + wavePhase1))
                    lineTo(x, y)
                    x += step
                }
            }
            drawPath(
                path = crestPath,
                color = color.copy(alpha = 0.45f),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocationMonthlyBreakdownBottomSheet(
    category: AllocationCategoryItem,
    monthlyTotals: Map<Int, Double>,
    selectedYear: Int,
    currencySymbol: String,
    currencyCode: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentMonth = DateUtils.getCurrentMonth()
    val currentYear = DateUtils.getCurrentYear()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = BottomSheetShape,
        containerColor = EmeraldTheme.extended.surfaceTier2,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EmeraldTheme.extended.subText.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row with Category Icon, Title, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(category.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${category.label} Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.0f", category.percent)}% of net salary • $selectedYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldTheme.extended.surfaceTier3)
                        .clickable(onClick = onDismissRequest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = EmeraldTheme.extended.subText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Allocated Banner
            EmeraldGlassCard(
                modifier = Modifier.fillMaxWidth(),
                highlightColor = category.color,
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total ${category.label} in $selectedYear",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldTheme.extended.subText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyHelper.format(category.amount, currencySymbol, currencyCode),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = category.color
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = category.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.0f", category.percent)}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = category.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💡 Transfer these monthly amounts from your main salary account to your ${category.label} account or vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTheme.extended.subText,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly Breakdown List Title
            Text(
                text = "Monthly Transfer Breakdown (12 Months)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List of 12 Months
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..12).forEach { month ->
                    val monthIncome = monthlyTotals[month] ?: 0.0
                    val monthAllocated = monthIncome * (category.percent / 100.0)
                    val monthName = DateUtils.getMonthFullName(month)
                    val isCurrentMonth = (selectedYear == currentYear && month == currentMonth)
                    val formattedAllocated = CurrencyHelper.format(monthAllocated, currencySymbol, currencyCode)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrentMonth) category.color.copy(alpha = 0.08f) else EmeraldTheme.extended.surfaceTier3,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrentMonth) category.color.copy(alpha = 0.4f) else EmeraldTheme.extended.glassBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCurrentMonth) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = category.color.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = "Current",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = category.color
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (monthIncome > 0) {
                                        "Net Salary: ${CurrencyHelper.format(monthIncome, currencySymbol, currencyCode)}"
                                    } else {
                                        "No salary logged"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldTheme.extended.subText
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedAllocated,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (monthAllocated > 0) category.color else EmeraldTheme.extended.subText
                                    )
                                    Text(
                                        text = "to transfer",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = EmeraldTheme.extended.subText
                                    )
                                }

                                if (monthAllocated > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(category.color.copy(alpha = 0.12f))
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(String.format(java.util.Locale.US, "%.2f", monthAllocated)))
                                                Toast.makeText(context, "Copied amount ($formattedAllocated) to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy amount",
                                            tint = category.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
