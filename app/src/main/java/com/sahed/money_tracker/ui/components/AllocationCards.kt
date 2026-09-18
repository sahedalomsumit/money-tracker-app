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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.ui.theme.DonateColor
import com.sahed.money_tracker.ui.theme.InvestColor
import com.sahed.money_tracker.ui.theme.RestColor
import com.sahed.money_tracker.ui.theme.SavingColor
import com.sahed.money_tracker.ui.theme.TealPrimary
import com.sahed.money_tracker.util.CurrencyHelper

@Composable
fun TotalIncomeCard(
    totalSalary: Double,
    currencySymbol: String,
    currencyCode: String,
    year: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Total Net Income",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = year.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = CurrencyHelper.format(totalSalary, currencySymbol, currencyCode),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AllocationBreakdownGrid(
    totalSalary: Double,
    allocation: AllocationSettings,
    currencySymbol: String,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val savingAmount = totalSalary * (allocation.savingPercent / 100.0)
    val investAmount = totalSalary * (allocation.investPercent / 100.0)
    val donateAmount = totalSalary * (allocation.donatePercent / 100.0)
    val restAmount = totalSalary * (allocation.restPercent / 100.0)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AllocationCategoryCard(
                label = allocation.savingLabel,
                percent = allocation.savingPercent,
                amount = savingAmount,
                currencySymbol = currencySymbol,
                currencyCode = currencyCode,
                color = SavingColor,
                icon = Icons.Default.Savings,
                modifier = Modifier.weight(1f)
            )

            AllocationCategoryCard(
                label = allocation.investLabel,
                percent = allocation.investPercent,
                amount = investAmount,
                currencySymbol = currencySymbol,
                currencyCode = currencyCode,
                color = InvestColor,
                icon = Icons.AutoMirrored.Filled.ShowChart,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AllocationCategoryCard(
                label = allocation.donateLabel,
                percent = allocation.donatePercent,
                amount = donateAmount,
                currencySymbol = currencySymbol,
                currencyCode = currencyCode,
                color = DonateColor,
                icon = Icons.Default.VolunteerActivism,
                modifier = Modifier.weight(1f)
            )

            AllocationCategoryCard(
                label = allocation.restLabel,
                percent = allocation.restPercent,
                amount = restAmount,
                currencySymbol = currencySymbol,
                currencyCode = currencyCode,
                color = RestColor,
                icon = Icons.Default.Weekend,
                modifier = Modifier.weight(1f)
            )
        }
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.0f", percent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = CurrencyHelper.format(amount, currencySymbol, currencyCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
