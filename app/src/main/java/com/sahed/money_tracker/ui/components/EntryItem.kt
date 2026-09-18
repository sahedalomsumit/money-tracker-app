package com.sahed.money_tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.ui.designsystem.components.EmeraldGlassCard
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.util.DateUtils

@Composable
fun EntryItemCard(
    entry: IncomeEntry,
    currencySymbol: String,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmeraldGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Leading Month Badge (spec 3.A: 50x50 with 14dp rounded corners)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldTheme.extended.surfaceTier2)
                        .border(
                            width = 1.dp,
                            color = EmeraldPalette.SoftEmerald.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = DateUtils.getMonthShortName(entry.month),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPalette.SoftEmerald
                        )
                        Text(
                            text = "${entry.year}".takeLast(2),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    val sourceText = buildString {
                        append(entry.mainSourceName.ifBlank { "Income" })
                        if (!entry.subSourceName.isNullOrBlank()) {
                            append(" • ")
                            append(entry.subSourceName)
                        }
                    }

                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${DateUtils.getMonthFullName(entry.month)} ${entry.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTheme.extended.subText
                    )
                }
            }

            // Amount
            Text(
                text = CurrencyHelper.format(entry.netSalary, currencySymbol, currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPalette.SoftEmerald
            )
        }
    }
}
