package com.sahed.money_tracker.ui.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Progress bars, handles
    small      = RoundedCornerShape(12.dp),  // Buttons, chips
    medium     = RoundedCornerShape(14.dp),  // Option tiles, sub-cards
    large      = RoundedCornerShape(18.dp),  // Glass cards
    extraLarge = RoundedCornerShape(24.dp)   // Bottom sheets, hero containers
)

val DialogShape = RoundedCornerShape(20.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
