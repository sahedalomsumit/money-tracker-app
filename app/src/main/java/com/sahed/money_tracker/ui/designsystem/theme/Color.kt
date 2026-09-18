package com.sahed.money_tracker.ui.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object EmeraldPalette {
    // Brand
    val SoftEmerald = Color(0xFF2E9C7E)
    val DeepGreen   = Color(0xFF1B3B34)
    val EmeraldGlow = Color(0xFF3DBFA0)
    val InvestBlue  = Color(0xFF2563EB) // Vibrant Sapphire Blue for Investing

    // Text
    val DarkText    = Color(0xFF1A1A1A)
    val LightText   = Color(0xFFFFFFFF)
    val SubTextGrey = Color(0xFF8A9A96)
    val LightSubText= Color(0xFF546E65)

    // Status
    val SuccessGreen = Color(0xFF4CAF50)
    val WarningAmber = Color(0xFFFF9800)
    val ErrorRed     = Color(0xFFF44336)
    val AccentPurple = Color(0xFFAB47BC)
    val InactiveGrey = Color(0xFF546E65)

    // Dark Surfaces
    val DarkBackground = Color(0xFF0A0F0D)
    val Surface1Dark   = Color(0xFF111A17)
    val Surface2Dark   = Color(0xFF182420)
    val Surface3Dark   = Color(0xFF1F2E29)
    val DarkGlassFill  = Color(0x1AFFFFFF) // 10% white
    val DarkGlassBorder= Color(0x33FFFFFF) // 20% white

    // Light Surfaces
    val LightBackground = Color(0xFFF4F7F5)
    val Surface1Light   = Color(0xFFFFFFFF)
    val Surface2Light   = Color(0xFFEDF4F1)
    val Surface3Light   = Color(0xFFDCEDE8)
    val LightGlassFill  = Color(0xB3FFFFFF) // 70% white
    val LightGlassBorder= Color(0x66FFFFFF) // 40% white
    val WarmBeige       = Color(0xFFF5F3EF)
}

@Immutable
data class ExtendedColors(
    val surfaceTier1: Color,
    val surfaceTier2: Color,
    val surfaceTier3: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val subText: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val accent: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceTier1 = EmeraldPalette.Surface1Dark,
        surfaceTier2 = EmeraldPalette.Surface2Dark,
        surfaceTier3 = EmeraldPalette.Surface3Dark,
        glassFill = EmeraldPalette.DarkGlassFill,
        glassBorder = EmeraldPalette.DarkGlassBorder,
        subText = EmeraldPalette.SubTextGrey,
        success = EmeraldPalette.SuccessGreen,
        warning = EmeraldPalette.WarningAmber,
        error = EmeraldPalette.ErrorRed,
        accent = EmeraldPalette.AccentPurple
    )
}
