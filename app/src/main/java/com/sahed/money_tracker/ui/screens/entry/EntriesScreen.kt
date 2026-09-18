package com.sahed.money_tracker.ui.screens.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.components.EditEntryDialog
import com.sahed.money_tracker.ui.components.EntryItemCard
import com.sahed.money_tracker.ui.designsystem.components.EmeraldGlassCard
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.viewmodel.EntriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesScreen(
    viewModel: EntriesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }

    // Dialog for editing or deleting entry
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Entries",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "All recorded income entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldTheme.extended.subText
                            )
                        }

                        // Search Toggle Button
                        Surface(
                            shape = CircleShape,
                            color = if (isSearchVisible) EmeraldPalette.SoftEmerald.copy(alpha = 0.2f) else EmeraldTheme.extended.surfaceTier2,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSearchVisible) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.glassBorder
                            ),
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isSearchVisible = !isSearchVisible
                                    if (!isSearchVisible) {
                                        viewModel.setSearchQuery("")
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Entries",
                                    tint = if (isSearchVisible) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.subText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Optional expandable search bar
                    if (isSearchVisible) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "Search by source name...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldTheme.extended.subText.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = EmeraldPalette.SoftEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = EmeraldTheme.extended.subText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = EmeraldTheme.extended.surfaceTier2,
                                unfocusedContainerColor = EmeraldTheme.extended.surfaceTier2,
                                focusedBorderColor = EmeraldPalette.SoftEmerald,
                                unfocusedBorderColor = EmeraldTheme.extended.glassBorder,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading && uiState.allEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPalette.SoftEmerald)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Summary Card
                item(key = "entries_summary_card") {
                    EmeraldGlassCard(
                        cornerRadius = 18.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (uiState.selectedYearFilter != null) "Income • ${uiState.selectedYearFilter}" else "Total Logged Income",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldTheme.extended.subText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyHelper.format(
                                        uiState.totalFilteredIncome,
                                        uiState.userProfile.currencySymbol,
                                        uiState.userProfile.currencyCode
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPalette.SoftEmerald
                                )
                            }

                            // Entry count badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldTheme.extended.surfaceTier2,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    EmeraldTheme.extended.glassBorder
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = EmeraldPalette.SoftEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.filteredEntries.size} entries",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Year Filter Chips Row
                if (uiState.availableYears.isNotEmpty()) {
                    item(key = "year_filter_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "All" chip
                            FilterChip(
                                label = "All Years",
                                isSelected = uiState.selectedYearFilter == null,
                                onClick = { viewModel.setSelectedYearFilter(null) }
                            )

                            // Each individual year chip
                            uiState.availableYears.forEach { year ->
                                FilterChip(
                                    label = year.toString(),
                                    isSelected = uiState.selectedYearFilter == year,
                                    onClick = { viewModel.setSelectedYearFilter(year) }
                                )
                            }
                        }
                    }
                }

                // 3. Entries List or Empty State
                if (uiState.filteredEntries.isEmpty()) {
                    item(key = "empty_entries_state") {
                        EmeraldGlassCard(
                            cornerRadius = 18.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = EmeraldTheme.extended.subText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (uiState.allEntries.isEmpty()) "No income logged yet" else "No matching entries found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.allEntries.isEmpty()) "Tap the + button below to add your first income" else "Try clearing your search or year filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldTheme.extended.subText
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredEntries, key = { it.id }) { entry ->
                        EntryItemCard(
                            entry = entry,
                            currencySymbol = uiState.userProfile.currencySymbol,
                            currencyCode = uiState.userProfile.currencyCode,
                            onClick = {
                                viewModel.openEditEntryDialog(entry)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.surfaceTier2,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.glassBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else EmeraldTheme.extended.subText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
