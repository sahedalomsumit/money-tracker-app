package com.sahed.money_tracker.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.ui.components.AllocationBreakdownGrid
import com.sahed.money_tracker.ui.components.DismissibleEntryItem
import com.sahed.money_tracker.ui.components.EditEntryDialog
import com.sahed.money_tracker.ui.components.MonthlyBarChart
import com.sahed.money_tracker.ui.components.SourceBreakdownCard
import com.sahed.money_tracker.ui.components.TotalIncomeCard
import com.sahed.money_tracker.ui.theme.TealPrimary
import com.sahed.money_tracker.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    // Dialog for editing/deleting entry
    if (uiState.selectedEntryForEdit != null) {
        EditEntryDialog(
            entry = uiState.selectedEntryForEdit!!,
            mainSources = uiState.mainSources,
            subSources = uiState.subSources,
            currencySymbol = uiState.userProfile.currencySymbol,
            onFetchSubSources = { mainId ->
                viewModel.fetchSubSourcesForMainSource(mainId)
            },
            onSave = { updated ->
                viewModel.updateEntry(updated)
            },
            onDelete = { id ->
                viewModel.deleteEntry(id)
            },
            onDismissRequest = {
                viewModel.dismissEditEntryDialog()
            }
        )
    }

    Scaffold(
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
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track your income & allocations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Year selector dropdown button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { yearDropdownExpanded = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = uiState.selectedYear.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Year",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            uiState.availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = year.toString(),
                                            fontWeight = if (year == uiState.selectedYear) FontWeight.Bold else FontWeight.Normal,
                                            color = if (year == uiState.selectedYear) TealPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSelectedYear(year)
                                        yearDropdownExpanded = false
                                    }
                                )
                            }
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Monthly Bar Chart
            item {
                MonthlyBarChart(
                    monthlyTotals = uiState.monthlyTotals,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode
                )
            }

            // 2. Total Income Card
            item {
                TotalIncomeCard(
                    totalSalary = uiState.totalYearIncome,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode,
                    year = uiState.selectedYear
                )
            }

            // 3. 4-Category Allocation Breakdown Grid (Saving, Investing, Donate, Rest)
            item {
                AllocationBreakdownGrid(
                    totalSalary = uiState.totalYearIncome,
                    allocation = uiState.allocationSettings,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode
                )
            }

            // 4. Source Breakdown Card with Drilldown
            item {
                SourceBreakdownCard(
                    entries = uiState.entries,
                    totalYearIncome = uiState.totalYearIncome,
                    currencySymbol = uiState.userProfile.currencySymbol,
                    currencyCode = uiState.userProfile.currencyCode
                )
            }

            // 5. Entries List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Entries (${uiState.entries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.entries.isNotEmpty()) {
                        Text(
                            text = "Swipe left to delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. Entries List items
            if (uiState.entries.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.height(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No income logged for ${uiState.selectedYear}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below to add an entry",
                                style = MaterialTheme.typography.bodySmall,
                                color = TealPrimary
                            )
                        }
                    }
                }
            } else {
                items(uiState.entries, key = { it.id }) { entry ->
                    DismissibleEntryItem(
                        entry = entry,
                        currencySymbol = uiState.userProfile.currencySymbol,
                        currencyCode = uiState.userProfile.currencyCode,
                        onClick = {
                            viewModel.openEditEntryDialog(entry)
                        },
                        onDelete = {
                            viewModel.deleteEntry(entry.id)
                        }
                    )
                }
            }
        }
    }
}
