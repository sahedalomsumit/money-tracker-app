package com.sahed.money_tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.designsystem.components.bouncyClickable
import com.sahed.money_tracker.ui.designsystem.theme.DialogShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme

@Composable
fun YearPickerDialog(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = DialogShape,
        containerColor = EmeraldTheme.extended.surfaceTier2,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EmeraldPalette.SoftEmerald.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = EmeraldPalette.SoftEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Select Year",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldTheme.extended.surfaceTier3)
                        .bouncyClickable(
                            pressedScale = 0.92f,
                            onClick = onDismissRequest
                        ),
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
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose a year to view overview & breakdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldTheme.extended.subText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Horizontally placed years with smooth scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableYears.forEach { year ->
                        val isSelected = year == selectedYear
                        val targetBg = if (isSelected) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.surfaceTier1
                        val targetBorder = if (isSelected) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.glassBorder
                        val targetText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        val animatedBg by animateColorAsState(
                            targetValue = targetBg,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "year_bg_$year"
                        )
                        val animatedBorder by animateColorAsState(
                            targetValue = targetBorder,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "year_border_$year"
                        )
                        val animatedText by animateColorAsState(
                            targetValue = targetText,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "year_text_$year"
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = animatedBg,
                            border = BorderStroke(1.dp, animatedBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .bouncyClickable(
                                    pressedScale = 0.94f,
                                    onClick = {
                                        onYearSelected(year)
                                        onDismissRequest()
                                    }
                                )
                        ) {
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedText,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
