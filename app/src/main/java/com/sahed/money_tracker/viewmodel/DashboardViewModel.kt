package com.sahed.money_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.preferences.AppPreferences
import com.sahed.money_tracker.data.repository.AllocationRepository
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.EntryRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import com.sahed.money_tracker.data.repository.SourceRepository
import com.sahed.money_tracker.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val availableYears: List<Int> = DateUtils.getAvailableYears(),
    val entries: List<IncomeEntry> = emptyList(),
    val monthlyTotals: Map<Int, Double> = (1..12).associateWith { 0.0 },
    val totalYearIncome: Double = 0.0,
    val userProfile: UserProfile = UserProfile(),
    val allocationSettings: AllocationSettings = AllocationSettings(),
    val mainSources: List<MainSource> = emptyList(),
    val subSources: List<SubSource> = emptyList(),
    val isLoading: Boolean = false,
    val selectedEntryForEdit: IncomeEntry? = null,
    val isIncomeSourcesExpanded: Boolean = false,
    val visibleEntriesCount: Int = 5
)

class DashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val entryRepository: EntryRepository = EntryRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val allocationRepository: AllocationRepository = AllocationRepository(),
    private val sourceRepository: SourceRepository = SourceRepository(),
    private val preferences: AppPreferences = AppPreferences(application.applicationContext)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var entriesJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val uid = authRepository.currentUser?.uid ?: return

        // Listen to profile
        viewModelScope.launch {
            try {
                profileRepository.getProfileFlow(uid).collectLatest { profile ->
                    if (profile != null) {
                        _uiState.update { it.copy(userProfile = profile) }
                    }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Listen to allocation settings
        viewModelScope.launch {
            try {
                allocationRepository.getAllocationFlow(uid).collectLatest { settings ->
                    _uiState.update { it.copy(allocationSettings = settings) }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Listen to main sources
        viewModelScope.launch {
            try {
                sourceRepository.getMainSourcesFlow(uid).collectLatest { sources ->
                    _uiState.update { it.copy(mainSources = sources) }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Listen to all entries to find years with data
        viewModelScope.launch {
            try {
                entryRepository.getAllEntriesFlow(uid).collectLatest { allEntries ->
                    val defaultYears = DateUtils.getAvailableYears().toSet()
                    val entryYears = allEntries.map { it.year }.toSet()
                    val years = (defaultYears + entryYears).sortedDescending()
                    _uiState.update { it.copy(availableYears = years) }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Listen to income sources expanded preference
        viewModelScope.launch {
            try {
                preferences.incomeSourcesExpanded.collectLatest { expanded ->
                    _uiState.update { it.copy(isIncomeSourcesExpanded = expanded) }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Load entries for current selected year
        observeEntriesForYear(_uiState.value.selectedYear)
    }

    fun setSelectedYear(year: Int) {
        if (_uiState.value.selectedYear == year) return
        _uiState.update { it.copy(selectedYear = year, visibleEntriesCount = 5) }
        observeEntriesForYear(year)
    }

    fun toggleIncomeSourcesExpanded() {
        val next = !_uiState.value.isIncomeSourcesExpanded
        viewModelScope.launch {
            try {
                preferences.setIncomeSourcesExpanded(next)
            } catch (e: Exception) {
                // Safe catch
            }
        }
    }

    fun loadMoreEntries() {
        _uiState.update { it.copy(visibleEntriesCount = it.entries.size) }
    }

    fun collapseEntries() {
        _uiState.update { it.copy(visibleEntriesCount = 5) }
    }

    private fun observeEntriesForYear(year: Int) {
        val uid = authRepository.currentUser?.uid ?: return
        entriesJob?.cancel()
        entriesJob = viewModelScope.launch {
            try {
                entryRepository.getEntriesForYearFlow(uid, year).collectLatest { yearEntries ->
                    // Calculate monthly totals 1..12
                    val totals = (1..12).associateWith { month ->
                        yearEntries.filter { it.month == month }.sumOf { it.netSalary }
                    }
                    val totalIncome = yearEntries.sumOf { it.netSalary }

                    _uiState.update {
                        it.copy(
                            entries = yearEntries,
                            monthlyTotals = totals,
                            totalYearIncome = totalIncome
                        )
                    }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }
    }

    fun openEditEntryDialog(entry: IncomeEntry) {
        _uiState.update { it.copy(selectedEntryForEdit = entry) }
    }

    fun dismissEditEntryDialog() {
        _uiState.update { it.copy(selectedEntryForEdit = null) }
    }

    fun fetchSubSourcesForMainSource(mainSourceId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            sourceRepository.getSubSourcesFlow(uid, mainSourceId).collectLatest { subs ->
                _uiState.update { it.copy(subSources = subs) }
            }
        }
    }

    fun updateEntry(entry: IncomeEntry) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            entryRepository.updateEntry(uid, entry)
            _uiState.update { it.copy(selectedEntryForEdit = null) }
        }
    }

    fun deleteEntry(entryId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            entryRepository.deleteEntry(uid, entryId)
            _uiState.update { it.copy(selectedEntryForEdit = null) }
        }
    }
}
