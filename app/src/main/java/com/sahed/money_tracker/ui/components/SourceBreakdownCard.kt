package com.sahed.money_tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.util.CurrencyHelper

data class MainSourceSummary(
    val mainSourceName: String,
    val totalAmount: Double,
    val percentage: Double,
    val subSourceBreakdown: Map<String, Double>
)

@Composable
fun SourceBreakdownCard(
    entries: List<IncomeEntry>,
    totalYearIncome: Double,
    currencySymbol: String,
    currencyCode: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    // Compute breakdown by main source
    val breakdown = remember(entries, totalYearIncome) {
        if (totalYearIncome <= 0.0) emptyList()
        else {
            entries.groupBy { it.mainSourceName.ifBlank { "Other" } }
                .map { (mainSource, sourceEntries) ->
                    val mainTotal = sourceEntries.sumOf { it.netSalary }
                    val percent = (mainTotal / totalYearIncome) * 100.0
                    val subBreakdown = sourceEntries.groupBy {
                        it.subSourceName?.ifBlank { "Direct / Unspecified" } ?: "Direct / Unspecified"
                    }.mapValues { (_, subEntries) ->
                        subEntries.sumOf { it.netSalary }
                    }
                    MainSourceSummary(
                        mainSourceName = mainSource,
                        totalAmount = mainTotal,
                        percentage = percent,
                        subSourceBreakdown = subBreakdown
                    )
                }.sortedByDescending { it.totalAmount }
        }
    }

    if (breakdown.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = EmeraldTheme.extended.surfaceTier1,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            EmeraldTheme.extended.glassBorder
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row (Clickable toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EmeraldPalette.SoftEmerald.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = EmeraldPalette.SoftEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Income Sources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isExpanded) {
                                "Breakdown by source & sub-source"
                            } else {
                                "${breakdown.size} source${if (breakdown.size > 1) "s" else ""} • ${CurrencyHelper.format(totalYearIncome, currencySymbol, currencyCode)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                // Expand / Collapse Chevron Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isExpanded) EmeraldPalette.SoftEmerald.copy(alpha = 0.15f) else EmeraldTheme.extended.surfaceTier2,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse income sources" else "Expand income sources",
                        tint = if (isExpanded) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.subText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Collapsible Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        color = EmeraldTheme.extended.glassBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        breakdown.forEach { summary ->
                            SourceBreakdownItem(
                                summary = summary,
                                currencySymbol = currencySymbol,
                                currencyCode = currencyCode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceBreakdownItem(
    summary: MainSourceSummary,
    currencySymbol: String,
    currencyCode: String
) {
    var expanded by rememberSaveable(summary.mainSourceName) { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = EmeraldTheme.extended.surfaceTier2,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            EmeraldTheme.extended.glassBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.mainSourceName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyHelper.format(summary.totalAmount, currencySymbol, currencyCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = EmeraldTheme.extended.subText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (summary.percentage / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldPalette.SoftEmerald,
                trackColor = EmeraldTheme.extended.surfaceTier3,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${summary.subSourceBreakdown.size} sub-source${if (summary.subSourceBreakdown.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldTheme.extended.subText
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", summary.percentage)}% of income",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPalette.SoftEmerald
                )
            }

            // Drill-down sub-sources list
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(
                        color = EmeraldTheme.extended.glassBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    summary.subSourceBreakdown.forEach { (subName, subAmount) ->
                        val subPercent = if (summary.totalAmount > 0) (subAmount / summary.totalAmount) * 100.0 else 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•  $subName",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldTheme.extended.subText
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.0f", subPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldTheme.extended.subText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = CurrencyHelper.format(subAmount, currencySymbol, currencyCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
