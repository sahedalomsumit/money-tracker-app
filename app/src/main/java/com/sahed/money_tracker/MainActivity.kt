package com.sahed.money_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.sahed.money_tracker.data.preferences.AppPreferences
import com.sahed.money_tracker.data.preferences.ThemeMode
import com.sahed.money_tracker.ui.navigation.AppNavGraph
import com.sahed.money_tracker.ui.theme.MoneyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = AppPreferences(applicationContext)

        setContent {
            val themeMode by preferences.themeMode.collectAsState(initial = ThemeMode.DARK)

            MoneyTrackerTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }
        }
    }
}
