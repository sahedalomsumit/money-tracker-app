package com.sahed.money_tracker.ui.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Adds a gentle staggered entrance animation (fade + subtle upward slide).
 * Perfect for page loads, card entries, and list items to create a smooth, premium feel.
 *
 * @param index Index used for staggering the entrance. Capped internally so late items never feel delayed.
 * @param delayPerItemMs Milliseconds delay between consecutive staggered items.
 * @param initialOffsetY Starting vertical offset in pixels before gliding to 0.
 * @param durationMillis Duration of the fade & slide animation.
 */
fun Modifier.gentleEntrance(
    index: Int = 0,
    delayPerItemMs: Long = 35L,
    initialOffsetY: Float = 24f,
    durationMillis: Int = 380
): Modifier = composed {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val cappedIndex = index.coerceIn(0, 6)
        if (cappedIndex > 0) {
            delay(cappedIndex * delayPerItemMs)
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * initialOffsetY
    }
}

/**
 * Composable wrapper that applies [gentleEntrance] to its content.
 */
@Composable
fun GentleEntranceEffect(
    index: Int = 0,
    delayPerItemMs: Long = 35L,
    initialOffsetY: Float = 24f,
    durationMillis: Int = 380,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.gentleEntrance(
            index = index,
            delayPerItemMs = delayPerItemMs,
            initialOffsetY = initialOffsetY,
            durationMillis = durationMillis
        )
    ) {
        content()
    }
}
