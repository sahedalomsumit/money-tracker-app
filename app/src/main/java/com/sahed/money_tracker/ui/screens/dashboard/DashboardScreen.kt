package com.sahed.money_tracker.ui.screens.dashboard

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.ui.components.AllocationBreakdownGrid
import com.sahed.money_tracker.ui.components.MonthlyBarChart
import com.sahed.money_tracker.ui.components.SourceBreakdownCard
import com.sahed.money_tracker.ui.components.TotalIncomeCard
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showYearPickerDialog by remember { mutableStateOf(false) }

    // Intercept system back press on Dashboard to exit the application cleanly
    BackHandler {
        (context as? Activity)?.finish()
    }

    if (showYearPickerDialog) {
        com.sahed.money_tracker.ui.components.YearPickerDialog(
            selectedYear = uiState.selectedYear,
            availableYears = uiState.availableYears,
            onYearSelected = { year ->
                viewModel.setSelectedYear(year)
            },
            onDismissRequest = { showYearPickerDialog = false }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track your income & allocations",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }

                    // Year selector popup button (Surface Tier 2)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldTheme.extended.surfaceTier2,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            EmeraldTheme.extended.glassBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showYearPickerDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = uiState.selectedYear.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPalette.SoftEmerald
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Year",
                                tint = EmeraldTheme.extended.subText
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Total Income Card (Emerald Hero Banner)
            item(key = "total_income_card") {
                TotalIncomeCard(
                    totalSalary = uiState.totalYearIncome,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode,
                    year = uiState.selectedYear
                )
            }

            // 2. 4-Category Allocation Breakdown Grid (Saving, Investing, Donate, Rest)
            item(key = "allocation_grid") {
                AllocationBreakdownGrid(
                    totalSalary = uiState.totalYearIncome,
                    allocation = uiState.allocationSettings,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode,
                    monthlyTotals = uiState.monthlyTotals,
                    selectedYear = uiState.selectedYear
                )
            }

            // 3. Monthly Bar Chart (Monthly Overview)
            item(key = "monthly_bar_chart") {
                MonthlyBarChart(
                    monthlyTotals = uiState.monthlyTotals,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode
                )
            }

            // 4. Source Breakdown Card
            item(key = "source_breakdown_card") {
                SourceBreakdownCard(
                    entries = uiState.entries,
                    totalYearIncome = uiState.totalYearIncome,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode
                )
            }
        }
    }
}
