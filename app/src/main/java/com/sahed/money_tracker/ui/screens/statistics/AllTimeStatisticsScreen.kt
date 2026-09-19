package com.sahed.money_tracker.ui.screens.statistics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import com.sahed.money_tracker.ui.designsystem.components.gentleEntrance
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahed.money_tracker.ui.components.AllocationCategoryCard
import com.sahed.money_tracker.ui.components.AllocationCategoryItem
import com.sahed.money_tracker.ui.components.SourceBreakdownCard
import com.sahed.money_tracker.ui.designsystem.components.EmeraldGlassCard
import com.sahed.money_tracker.ui.designsystem.components.EmeraldHeroBanner
import com.sahed.money_tracker.ui.designsystem.theme.BottomSheetShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.ui.theme.DonateColor
import com.sahed.money_tracker.ui.theme.InvestColor
import com.sahed.money_tracker.ui.theme.RestColor
import com.sahed.money_tracker.ui.theme.SavingColor
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.viewmodel.AllTimeSourceSummary
import com.sahed.money_tracker.viewmodel.AllTimeStatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTimeStatisticsScreen(
    viewModel: AllTimeStatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategoryForDetail by remember { mutableStateOf<AllocationCategoryItem?>(null) }
    var showNetSalaryDetail by remember { mutableStateOf(false) }

    val activeCategories = remember(uiState.allocationSettings, uiState.allTimeNetSalary) {
        listOf(
            AllocationCategoryItem(
                key = "saving",
                label = uiState.allocationSettings.savingLabel,
                percent = uiState.allocationSettings.savingPercent,
                amount = uiState.allTimeSavings,
                color = SavingColor,
                icon = Icons.Default.Savings
            ),
            AllocationCategoryItem(
                key = "invest",
                label = uiState.allocationSettings.investLabel,
                percent = uiState.allocationSettings.investPercent,
                amount = uiState.allTimeInvesting,
                color = InvestColor,
                icon = Icons.AutoMirrored.Filled.ShowChart
            ),
            AllocationCategoryItem(
                key = "donate",
                label = uiState.allocationSettings.donateLabel,
                percent = uiState.allocationSettings.donatePercent,
                amount = uiState.allTimeDonate,
                color = DonateColor,
                icon = Icons.Default.VolunteerActivism
            ),
            AllocationCategoryItem(
                key = "rest",
                label = uiState.allocationSettings.restLabel,
                percent = uiState.allocationSettings.restPercent,
                amount = uiState.allTimeRestOfMoney,
                color = RestColor,
                icon = Icons.Default.Weekend
            )
        ).filter { it.percent > 0.0 }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    Text(
                        text = "All Time Statistics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lifetime income & allocation overview",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTheme.extended.subText
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading && uiState.totalEntriesCount == 0) {
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: All Time Net Salary (Top Hero Banner)
                item(key = "all_time_net_salary_card") {
                    EmeraldHeroBanner(
                        title = "All Time Net Salary",
                        value = CurrencyHelper.format(
                            uiState.allTimeNetSalary,
                            uiState.userProfile.currencySymbol,
                            uiState.userProfile.currencyCode
                        ),
                        subtitle = "Lifetime total earnings across ${uiState.activeYearsCount} recorded year${if (uiState.activeYearsCount != 1) "s" else ""}",
                        onClick = { showNetSalaryDetail = true },
                        modifier = Modifier.gentleEntrance(0),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = EmeraldPalette.EmeraldGlow,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    )
                }

                // Cards 2, 3, 4, 5: The 4 Allocation Cards in 2x2 Grid (Savings, Investing, Donate, Rest of Money)
                item(key = "all_time_allocation_grid") {
                    if (activeCategories.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .gentleEntrance(1),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            activeCategories.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        AllocationCategoryCard(
                                            label = item.label,
                                            percent = item.percent,
                                            amount = item.amount,
                                            currencySymbol = uiState.userProfile.currencySymbol,
                                            currencyCode = uiState.userProfile.currencyCode,
                                            color = item.color,
                                            icon = item.icon,
                                            onClick = { selectedCategoryForDetail = item },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1 && activeCategories.size > 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Lifetime Quick Metrics (Entries, Active Years, Average per Year)
                item(key = "lifetime_metrics") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gentleEntrance(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricSmallCard(
                            title = "Total Entries",
                            value = "${uiState.totalEntriesCount}",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallCard(
                            title = "Recorded Years",
                            value = "${uiState.activeYearsCount}",
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallCard(
                            title = "Avg / Year",
                            value = CurrencyHelper.format(
                                uiState.averageYearlyIncome,
                                uiState.userProfile.currencySymbol,
                                uiState.userProfile.currencyCode
                            ),
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 4: Year-by-Year Income Breakdown
                if (uiState.yearlyTotals.isNotEmpty()) {
                    item(key = "yearly_breakdown_card") {
                        val maxYearIncome = uiState.yearlyTotals.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                        var playProgressAnimation by remember { mutableStateOf(false) }
                        LaunchedEffect(uiState.yearlyTotals) {
                            playProgressAnimation = true
                        }

                        EmeraldGlassCard(
                            cornerRadius = 18.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .gentleEntrance(3)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(EmeraldPalette.SoftEmerald.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = EmeraldPalette.SoftEmerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Yearly Income Distribution",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Total income earned by year",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = EmeraldTheme.extended.subText
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                uiState.yearlyTotals.forEach { (year, yearTotal) ->
                                    val ratio = (yearTotal / maxYearIncome).toFloat().coerceIn(0f, 1f)
                                    val animatedRatio by animateFloatAsState(
                                        targetValue = if (playProgressAnimation) ratio else 0f,
                                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                                        label = "yearly_ratio_$year"
                                    )
                                    val percentOfLifetime = if (uiState.allTimeNetSalary > 0) {
                                        (yearTotal / uiState.allTimeNetSalary) * 100.0
                                    } else 0.0

                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$year",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${String.format(java.util.Locale.US, "%.1f", percentOfLifetime)}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldTheme.extended.subText
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = CurrencyHelper.format(
                                                        yearTotal,
                                                        uiState.userProfile.currencySymbol,
                                                        uiState.userProfile.currencyCode
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = EmeraldPalette.SoftEmerald
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        LinearProgressIndicator(
                                            progress = { animatedRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = EmeraldPalette.SoftEmerald,
                                            trackColor = EmeraldTheme.extended.surfaceTier2,
                                            strokeCap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: Lifetime Top Income Sources & Sub-Sources (Same as Dashboard)
                if (uiState.allEntries.isNotEmpty()) {
                    item(key = "all_time_sources_card") {
                        Box(modifier = Modifier.gentleEntrance(4)) {
                            SourceBreakdownCard(
                                entries = uiState.allEntries,
                                totalYearIncome = uiState.allTimeNetSalary,
                                currencySymbol = uiState.userProfile.currencySymbol,
                                currencyCode = uiState.userProfile.currencyCode
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet when tapping on any of the 4 allocation cards
    if (selectedCategoryForDetail != null) {
        AllTimeAllocationCategoryDetailSheet(
            category = selectedCategoryForDetail!!,
            yearlyTotals = uiState.yearlyTotals,
            totalAllocationAmount = selectedCategoryForDetail!!.amount,
            currencySymbol = uiState.userProfile.currencySymbol,
            currencyCode = uiState.userProfile.currencyCode,
            onDismissRequest = { selectedCategoryForDetail = null }
        )
    }

    // Modal Bottom Sheet when tapping on the All Time Net Salary card
    if (showNetSalaryDetail) {
        AllTimeNetSalaryDetailSheet(
            allTimeNetSalary = uiState.allTimeNetSalary,
            yearlyTotals = uiState.yearlyTotals,
            activeYearsCount = uiState.activeYearsCount,
            currencySymbol = uiState.userProfile.currencySymbol,
            currencyCode = uiState.userProfile.currencyCode,
            onDismissRequest = { showNetSalaryDetail = false }
        )
    }
}

@Composable
private fun AllTimeSourceItem(
    index: Int,
    source: AllTimeSourceSummary,
    currencySymbol: String,
    currencyCode: String
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "sourceSubArrow"
    )
    val hasSubSources = source.subSources.isNotEmpty()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EmeraldTheme.extended.surfaceTier1,
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTheme.extended.glassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Main source header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTheme.extended.subText,
                        modifier = Modifier.width(22.dp)
                    )
                    Column {
                        Text(
                            text = source.mainSourceName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${source.subSourcesCount} sub-source${if (source.subSourcesCount != 1) "s" else ""} • ${source.entriesCount} entr${if (source.entriesCount != 1) "ies" else "y"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyHelper.format(
                            source.totalAmount,
                            currencySymbol,
                            currencyCode
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPalette.SoftEmerald
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", source.percentageOfTotal)}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldTheme.extended.subText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var playSourceAnimation by remember { mutableStateOf(false) }
            LaunchedEffect(source) {
                playSourceAnimation = true
            }
            val targetRatio = (source.percentageOfTotal / 100f).toFloat().coerceIn(0f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = if (playSourceAnimation) targetRatio else 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                label = "source_ratio_${source.mainSourceName}"
            )

            LinearProgressIndicator(
                progress = { animatedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = EmeraldPalette.SoftEmerald,
                trackColor = EmeraldTheme.extended.surfaceTier2,
                strokeCap = StrokeCap.Round
            )

            // Sub-sources numbers section
            if (hasSubSources) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Sub-sources numbers" else "View ${source.subSourcesCount} sub-sources numbers",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpanded) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.subText
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isExpanded) EmeraldPalette.SoftEmerald else EmeraldTheme.extended.subText,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(arrowRotation)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HorizontalDivider(
                            color = EmeraldTheme.extended.glassBorder.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        source.subSources.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "•  ${sub.subSourceName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${sub.entriesCount})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldTheme.extended.subText
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.0f", sub.percentageOfMainSource)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldTheme.extended.subText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = CurrencyHelper.format(
                                            sub.totalAmount,
                                            currencySymbol,
                                            currencyCode
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPalette.SoftEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTimeNetSalaryDetailSheet(
    allTimeNetSalary: Double,
    yearlyTotals: Map<Int, Double>,
    activeYearsCount: Int,
    currencySymbol: String,
    currencyCode: String,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = BottomSheetShape,
        containerColor = EmeraldTheme.extended.surfaceTier2,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EmeraldTheme.extended.subText.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(EmeraldPalette.SoftEmerald.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = EmeraldPalette.SoftEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All Time Net Salary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$activeYearsCount recorded year${if (activeYearsCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = EmeraldTheme.extended.surfaceTier1,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onDismissRequest() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = EmeraldTheme.extended.subText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero summary box in bottom sheet
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmeraldPalette.SoftEmerald.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPalette.SoftEmerald.copy(alpha = 0.30f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Lifetime Net Income",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldTheme.extended.subText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.format(allTimeNetSalary, currencySymbol, currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPalette.SoftEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Yearly Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (yearlyTotals.isEmpty()) {
                Text(
                    text = "No yearly data available",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldTheme.extended.subText
                )
            } else {
                yearlyTotals.forEach { (year, yearTotalIncome) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$year",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyHelper.format(yearTotalIncome, currencySymbol, currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPalette.SoftEmerald
                        )
                    }
                    HorizontalDivider(
                        color = EmeraldTheme.extended.glassBorder.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSmallCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    EmeraldGlassCard(
        cornerRadius = 14.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldPalette.SoftEmerald,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically { it / 3 } + fadeIn(tween(280, easing = FastOutSlowInEasing)))
                        .togetherWith(slideOutVertically { -it / 3 } + fadeOut(tween(180, easing = FastOutSlowInEasing)))
                },
                label = "metricValue"
            ) { targetValue ->
                Text(
                    text = targetValue,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = EmeraldTheme.extended.subText,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTimeAllocationCategoryDetailSheet(
    category: AllocationCategoryItem,
    yearlyTotals: Map<Int, Double>,
    totalAllocationAmount: Double,
    currencySymbol: String,
    currencyCode: String,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = BottomSheetShape,
        containerColor = EmeraldTheme.extended.surfaceTier2,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EmeraldTheme.extended.subText.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(category.color.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.0f", category.percent)}% lifetime allocation",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTheme.extended.subText
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = EmeraldTheme.extended.surfaceTier1,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onDismissRequest() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = EmeraldTheme.extended.subText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero summary box in bottom sheet
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = category.color.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, category.color.copy(alpha = 0.30f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Allocated (${category.label})",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldTheme.extended.subText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.format(totalAllocationAmount, currencySymbol, currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = category.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Yearly Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (yearlyTotals.isEmpty()) {
                Text(
                    text = "No yearly data available",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldTheme.extended.subText
                )
            } else {
                yearlyTotals.forEach { (year, yearTotalIncome) ->
                    val allocatedYearAmount = yearTotalIncome * (category.percent / 100.0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$year",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyHelper.format(allocatedYearAmount, currencySymbol, currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = category.color
                        )
                    }
                    HorizontalDivider(
                        color = EmeraldTheme.extended.glassBorder.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
