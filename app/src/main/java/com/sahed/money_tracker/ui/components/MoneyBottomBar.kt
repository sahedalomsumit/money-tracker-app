package com.sahed.money_tracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme

@Composable
fun MoneyBottomBar(
    currentRoute: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToEntries: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarHeight = 66.dp
    val fabSize = 52.dp
    val cutoutRadius = 34.dp
    val cornerRadius = 8.dp

    val barColor = EmeraldTheme.extended.surfaceTier1
    val borderColor = EmeraldTheme.extended.glassBorder

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Bottom bar body with cradle cutout + seamless navigation bar fill
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarHeight)
                    .drawWithCache {
                        val cutoutRadiusPx = cutoutRadius.toPx()
                        val cornerRadiusPx = cornerRadius.toPx()

                        val (bgPath, borderPath) = buildCradlePaths(
                            size = size,
                            cutoutRadius = cutoutRadiusPx,
                            cornerRadius = cornerRadiusPx
                        )

                        onDrawBehind {
                            // Draw navbar body leaving circular cutout empty
                            drawPath(path = bgPath, color = barColor)
                            // Draw subtle top divider line following cradle contour
                            drawPath(
                                path = borderPath,
                                color = borderColor,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navBarHeight)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Dashboard Tab
                    NavPillItem(
                        label = "Dashboard",
                        selected = currentRoute == "dashboard",
                        activeIcon = Icons.Filled.Dashboard,
                        inactiveIcon = Icons.Outlined.Dashboard,
                        onClick = onNavigateToDashboard,
                        modifier = Modifier.weight(1f)
                    )

                    // 2. Entries Tab
                    NavPillItem(
                        label = "Entries",
                        selected = currentRoute == "entries",
                        activeIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                        inactiveIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        onClick = onNavigateToEntries,
                        modifier = Modifier.weight(1f)
                    )

                    // Center cradle gap for floating Add (+) button
                    Spacer(modifier = Modifier.width(cutoutRadius * 2 + 10.dp))

                    // 3. All Time Statistics Tab
                    NavPillItem(
                        label = "Stats",
                        selected = currentRoute == "statistics",
                        activeIcon = Icons.Filled.BarChart,
                        inactiveIcon = Icons.Outlined.BarChart,
                        onClick = onNavigateToStatistics,
                        modifier = Modifier.weight(1f)
                    )

                    // 4. Settings Tab
                    NavPillItem(
                        label = "Settings",
                        selected = currentRoute == "settings",
                        activeIcon = Icons.Filled.Settings,
                        inactiveIcon = Icons.Outlined.Settings,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Seamlessly fill system navigation bar area below the navbar
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barColor)
                    .navigationBarsPadding()
            )
        }

        // 2. Floating "+" Action Button with radiant ambient glow, nestled in the cradle cutout
        // Uses layout { ... layout(placeable.width, 0) } so this floating element reports 0 height
        // to parent Box, ensuring it does NOT expand bottomBar height or push screen content up.
        val glowSize = 76.dp
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, 0) {
                        placeable.place(0, 0)
                    }
                }
                .offset(y = -(glowSize / 2))
                .size(glowSize),
            contentAlignment = Alignment.Center
        ) {
            // Radiant ambient glow ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to Color(0xFF3DBFA0).copy(alpha = 0.85f),
                                0.40f to Color(0xFF3DBFA0).copy(alpha = 0.65f),
                                0.55f to Color(0xFF2E9C7E).copy(alpha = 0.45f),
                                0.80f to Color(0xFF2E9C7E).copy(alpha = 0.15f),
                                1.0f to Color.Transparent
                            ),
                            radius = size.minDimension / 2f
                        )
                    }
            )

            Surface(
                onClick = onOpenAddEntry,
                shape = CircleShape,
                color = EmeraldPalette.SoftEmerald,
                contentColor = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier.size(fabSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Income Entry",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun buildCradlePaths(
    size: Size,
    cutoutRadius: Float,
    cornerRadius: Float
): Pair<Path, Path> {
    val cx = size.width / 2f
    val R = cutoutRadius
    val r = cornerRadius

    val borderPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(cx - R - r, 0f)
        // Left shoulder curving smoothly into the circular cutout
        cubicTo(
            cx - R - r * 0.4f, 0f,
            cx - R, r * 0.15f,
            cx - R * 0.96f, R * 0.28f
        )
        // Circular arc dipping around the lower half of the FAB
        arcTo(
            rect = Rect(
                left = cx - R,
                top = -R,
                right = cx + R,
                bottom = R
            ),
            startAngleDegrees = 164f,
            sweepAngleDegrees = -148f,
            forceMoveTo = false
        )
        // Right shoulder curving smoothly back out to the top line
        cubicTo(
            cx + R, r * 0.15f,
            cx + R + r * 0.4f, 0f,
            cx + R + r, 0f
        )
        lineTo(size.width, 0f)
    }

    val bgPath = Path().apply {
        addPath(borderPath)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    return Pair(bgPath, borderPath)
}

@Composable
private fun NavPillItem(
    label: String,
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald.copy(alpha = 0.22f) else Color.Transparent,
        label = "navPillBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.subText,
        label = "navPillContent"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pill Indicator behind icon (stadium shape)
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .background(
                    color = indicatorColor,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Label below pill
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
