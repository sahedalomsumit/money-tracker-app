package com.sahed.money_tracker.ui.theme

import androidx.compose.runtime.Composable
import com.sahed.money_tracker.data.preferences.ThemeMode
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldDesignTheme

@Composable
fun MoneyTrackerTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    EmeraldDesignTheme(
        themeMode = themeMode,
        content = content
    )
}
