package com.sahed.money_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.repository.AllocationRepository
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.EntryRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllTimeSubSourceSummary(
    val subSourceName: String,
    val totalAmount: Double,
    val percentageOfMainSource: Double,
    val percentageOfTotal: Double,
    val entriesCount: Int
)

data class AllTimeSourceSummary(
    val mainSourceName: String,
    val totalAmount: Double,
    val percentageOfTotal: Double,
    val entriesCount: Int,
    val subSourcesCount: Int,
    val subSources: List<AllTimeSubSourceSummary>
)

data class AllTimeStatisticsUiState(
    val allEntries: List<IncomeEntry> = emptyList(),
    val allTimeNetSalary: Double = 0.0,
    val allTimeSavings: Double = 0.0,
    val allTimeInvesting: Double = 0.0,
    val allTimeDonate: Double = 0.0,
    val allTimeRestOfMoney: Double = 0.0,
    val yearlyTotals: Map<Int, Double> = emptyMap(),
    val sourceTotals: Map<String, Double> = emptyMap(),
    val allTimeSources: List<AllTimeSourceSummary> = emptyList(),
    val totalSubSourcesCount: Int = 0,
    val totalEntriesCount: Int = 0,
    val activeYearsCount: Int = 0,
    val averageYearlyIncome: Double = 0.0,
    val userProfile: UserProfile = UserProfile(),
    val allocationSettings: AllocationSettings = AllocationSettings(),
    val isLoading: Boolean = false
)

class AllTimeStatisticsViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val entryRepository: EntryRepository = EntryRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val allocationRepository: AllocationRepository = AllocationRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AllTimeStatisticsUiState(isLoading = true))
    val uiState: StateFlow<AllTimeStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val uid = authRepository.currentUser?.uid ?: return

        // Combine entries, allocation, and profile flows for unified reactive calculation
        viewModelScope.launch {
            try {
                combine(
                    entryRepository.getAllEntriesFlow(uid),
                    allocationRepository.getAllocationFlow(uid),
                    profileRepository.getProfileFlow(uid)
                ) { entries, allocation, profile ->
                    calculateStats(entries, allocation, profile ?: UserProfile())
                }.collectLatest { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateStats(
        entries: List<IncomeEntry>,
        allocation: AllocationSettings,
        profile: UserProfile
    ): AllTimeStatisticsUiState {
        val totalNetSalary = entries.sumOf { it.netSalary }
        val savings = totalNetSalary * (allocation.savingPercent / 100.0)
        val investing = totalNetSalary * (allocation.investPercent / 100.0)
        val donate = totalNetSalary * (allocation.donatePercent / 100.0)
        val rest = totalNetSalary * (allocation.restPercent / 100.0)

        // Group by year and calculate year totals
        val yearlyTotals = entries.groupBy { it.year }
            .mapValues { (_, yearEntries) -> yearEntries.sumOf { it.netSalary } }
            .toSortedMap(compareByDescending { it })

        // Group by source and calculate main and sub-source totals
        val allTimeSources = entries.groupBy { it.mainSourceName.ifBlank { "Uncategorized" } }
            .map { (mainSource, srcEntries) ->
                val mainTotal = srcEntries.sumOf { it.netSalary }
                val mainPercent = if (totalNetSalary > 0) (mainTotal / totalNetSalary) * 100.0 else 0.0
                val subSources = srcEntries.groupBy {
                    it.subSourceName?.ifBlank { "Direct / Unspecified" } ?: "Direct / Unspecified"
                }.map { (subName, subEntries) ->
                    val subTotal = subEntries.sumOf { it.netSalary }
                    val subPercentOfMain = if (mainTotal > 0) (subTotal / mainTotal) * 100.0 else 0.0
                    val subPercentOfTotal = if (totalNetSalary > 0) (subTotal / totalNetSalary) * 100.0 else 0.0
                    AllTimeSubSourceSummary(
                        subSourceName = subName,
                        totalAmount = subTotal,
                        percentageOfMainSource = subPercentOfMain,
                        percentageOfTotal = subPercentOfTotal,
                        entriesCount = subEntries.size
                    )
                }.sortedByDescending { it.totalAmount }

                AllTimeSourceSummary(
                    mainSourceName = mainSource,
                    totalAmount = mainTotal,
                    percentageOfTotal = mainPercent,
                    entriesCount = srcEntries.size,
                    subSourcesCount = subSources.size,
                    subSources = subSources
                )
            }.sortedByDescending { it.totalAmount }

        val sourceTotals = allTimeSources.associate { it.mainSourceName to it.totalAmount }

        val totalUniqueSubSources = entries
            .mapNotNull { it.subSourceName?.takeIf { name -> name.isNotBlank() } }
            .distinct()
            .size

        val activeYears = yearlyTotals.keys.size
        val avgYearly = if (activeYears > 0) totalNetSalary / activeYears else 0.0

        return AllTimeStatisticsUiState(
            allEntries = entries,
            allTimeNetSalary = totalNetSalary,
            allTimeSavings = savings,
            allTimeInvesting = investing,
            allTimeDonate = donate,
            allTimeRestOfMoney = rest,
            yearlyTotals = yearlyTotals,
            sourceTotals = sourceTotals,
            allTimeSources = allTimeSources,
            totalSubSourcesCount = totalUniqueSubSources,
            totalEntriesCount = entries.size,
            activeYearsCount = activeYears,
            averageYearlyIncome = avgYearly,
            userProfile = profile,
            allocationSettings = allocation,
            isLoading = false
        )
    }
}
