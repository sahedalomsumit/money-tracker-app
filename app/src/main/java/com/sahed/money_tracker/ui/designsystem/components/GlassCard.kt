package com.sahed.money_tracker.ui.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme

@Composable
fun EmeraldGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlightColor: Color = Color.Transparent,
    containerColor: Color? = null,
    backgroundBrush: Brush? = null,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColor by animateColorAsState(
        targetValue = if (highlightColor != Color.Transparent) {
            highlightColor.copy(alpha = 0.45f)
        } else {
            EmeraldTheme.extended.glassBorder
        },
        animationSpec = tween(durationMillis = 200),
        label = "glassBorderAnim"
    )

    val bgModifier = when {
        backgroundBrush != null -> Modifier.background(backgroundBrush)
        containerColor != null -> Modifier.background(containerColor)
        else -> Modifier.background(EmeraldTheme.extended.glassFill)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlightColor != Color.Transparent) 8.dp else 0.dp,
                shape = shape,
                ambientColor = highlightColor.copy(alpha = 0.15f),
                spotColor = highlightColor.copy(alpha = 0.25f)
            )
            .clip(shape)
            .then(bgModifier)
            .border(width = 1.2.dp, color = borderColor, shape = shape)
            .then(
                if (onClick != null) Modifier.bouncyClickable(pressedScale = 0.98f, onClick = onClick) else Modifier
            ),
        content = content
    )
}
