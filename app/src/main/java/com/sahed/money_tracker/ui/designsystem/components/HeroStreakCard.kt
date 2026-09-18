package com.sahed.money_tracker.ui.designsystem.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette

@Composable
fun EmeraldHeroBanner(
    title: String,
    value: String,
    unit: String = "",
    subtitle: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFFFB74D),
            modifier = Modifier.size(36.dp)
        )
    }
) {
    val gradient = Brush.linearGradient(
        colors = listOf(EmeraldPalette.DeepGreen, EmeraldPalette.SoftEmerald)
    )

    val baseModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = EmeraldPalette.SoftEmerald.copy(alpha = 0.3f),
                spotColor = EmeraldPalette.SoftEmerald.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .clickable(onClick = onClick)
            .padding(24.dp)
    } else {
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = EmeraldPalette.SoftEmerald.copy(alpha = 0.3f),
                spotColor = EmeraldPalette.SoftEmerald.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .padding(24.dp)
    }

    Box(
        modifier = baseModifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldPalette.LightText.copy(alpha = 0.90f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = EmeraldPalette.LightText
                    )
                    if (unit.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPalette.LightText.copy(alpha = 0.90f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPalette.LightText.copy(alpha = 0.88f)
                    )
                }

                if (onClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View breakdown",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = EmeraldPalette.LightText.copy(alpha = 0.95f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = EmeraldPalette.LightText.copy(alpha = 0.95f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    }
}
