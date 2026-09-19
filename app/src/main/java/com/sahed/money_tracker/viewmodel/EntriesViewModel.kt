package com.sahed.money_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.EntryRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import com.sahed.money_tracker.data.repository.SourceRepository
import com.sahed.money_tracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EntrySortOrder(val label: String) {
    AMOUNT_DESC("Highest to Lowest"),
    AMOUNT_ASC("Lowest to Highest"),
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First")
}

data class EntriesUiState(
    val allEntries: List<IncomeEntry> = emptyList(),
    val filteredEntries: List<IncomeEntry> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYearFilter: Int? = DateUtils.getCurrentYear(), // Current year selected by default
    val sortOrder: EntrySortOrder = EntrySortOrder.DATE_DESC,
    val searchQuery: String = "",
    val totalFilteredIncome: Double = 0.0,
    val totalAllIncome: Double = 0.0,
    val userProfile: UserProfile = UserProfile(),
    val mainSources: List<MainSource> = emptyList(),
    val subSources: List<SubSource> = emptyList(),
    val selectedEntryForEdit: IncomeEntry? = null,
    val isLoading: Boolean = false
)

class EntriesViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val entryRepository: EntryRepository = EntryRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val sourceRepository: SourceRepository = SourceRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EntriesUiState(isLoading = true))
    val uiState: StateFlow<EntriesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val uid = authRepository.currentUser?.uid ?: return

        // Listen to User Profile for currency display
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

        // Listen to Main Sources for Edit Dialog
        viewModelScope.launch {
            try {
                sourceRepository.getMainSourcesFlow(uid).collectLatest { sources ->
                    _uiState.update { it.copy(mainSources = sources) }
                }
            } catch (e: Exception) {
                // Safe catch
            }
        }

        // Listen to All Entries
        viewModelScope.launch {
            try {
                entryRepository.getAllEntriesFlow(uid).collectLatest { entries ->
                    val currentYear = DateUtils.getCurrentYear()
                    val distinctYears = (entries.map { it.year } + currentYear).distinct().sortedDescending()
                    val totalAll = entries.sumOf { it.netSalary }

                    _uiState.update { current ->
                        val filtered = filterAndSortEntries(
                            entries = entries,
                            yearFilter = current.selectedYearFilter,
                            searchQuery = current.searchQuery,
                            sortOrder = current.sortOrder
                        )
                        current.copy(
                            allEntries = entries,
                            filteredEntries = filtered,
                            availableYears = distinctYears,
                            totalAllIncome = totalAll,
                            totalFilteredIncome = filtered.sumOf { it.netSalary },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setSelectedYearFilter(year: Int?) {
        _uiState.update { current ->
            val filtered = filterAndSortEntries(
                entries = current.allEntries,
                yearFilter = year,
                searchQuery = current.searchQuery,
                sortOrder = current.sortOrder
            )
            current.copy(
                selectedYearFilter = year,
                filteredEntries = filtered,
                totalFilteredIncome = filtered.sumOf { it.netSalary }
            )
        }
    }

    fun setSortOrder(order: EntrySortOrder) {
        _uiState.update { current ->
            val filtered = filterAndSortEntries(
                entries = current.allEntries,
                yearFilter = current.selectedYearFilter,
                searchQuery = current.searchQuery,
                sortOrder = order
            )
            current.copy(
                sortOrder = order,
                filteredEntries = filtered
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = filterAndSortEntries(
                entries = current.allEntries,
                yearFilter = current.selectedYearFilter,
                searchQuery = query,
                sortOrder = current.sortOrder
            )
            current.copy(
                searchQuery = query,
                filteredEntries = filtered,
                totalFilteredIncome = filtered.sumOf { it.netSalary }
            )
        }
    }

    private fun filterAndSortEntries(
        entries: List<IncomeEntry>,
        yearFilter: Int?,
        searchQuery: String,
        sortOrder: EntrySortOrder
    ): List<IncomeEntry> {
        val filtered = entries.filter { entry ->
            val matchesYear = yearFilter == null || entry.year == yearFilter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val query = searchQuery.trim().lowercase()
                entry.mainSourceName.lowercase().contains(query) ||
                        (entry.subSourceName?.lowercase()?.contains(query) == true)
            }
            matchesYear && matchesSearch
        }

        return when (sortOrder) {
            EntrySortOrder.AMOUNT_DESC -> filtered.sortedWith(
                compareByDescending<IncomeEntry> { it.netSalary }
                    .thenByDescending { it.year }
                    .thenByDescending { it.month }
                    .thenByDescending { it.createdAt }
            )
            EntrySortOrder.AMOUNT_ASC -> filtered.sortedWith(
                compareBy<IncomeEntry> { it.netSalary }
                    .thenByDescending { it.year }
                    .thenByDescending { it.month }
                    .thenByDescending { it.createdAt }
            )
            EntrySortOrder.DATE_DESC -> filtered.sortedWith(
                compareByDescending<IncomeEntry> { it.year }
                    .thenByDescending { it.month }
                    .thenByDescending { it.createdAt }
            )
            EntrySortOrder.DATE_ASC -> filtered.sortedWith(
                compareBy<IncomeEntry> { it.year }
                    .thenBy { it.month }
                    .thenBy { it.createdAt }
            )
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
