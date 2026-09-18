package com.sahed.money_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.repository.AllocationRepository
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.EntryRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import com.sahed.money_tracker.data.repository.SourceRepository
import com.sahed.money_tracker.util.DateUtils
import com.sahed.money_tracker.util.MathExpressionEvaluator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEntryUiState(
    val month: Int = DateUtils.getCurrentMonth(),
    val year: Int = DateUtils.getCurrentYear(),
    val salaryInput: String = "",
    val selectedMainSource: MainSource? = null,
    val selectedSubSource: SubSource? = null,
    val mainSources: List<MainSource> = emptyList(),
    val subSources: List<SubSource> = emptyList(),
    val allocationSettings: AllocationSettings = AllocationSettings(),
    val userProfile: UserProfile = UserProfile(),
    val isSaving: Boolean = false,
    val entrySaved: Boolean = false,
    val errorMessage: String? = null
) {
    val salaryAmount: Double
        get() = MathExpressionEvaluator.evaluate(salaryInput) ?: salaryInput.toDoubleOrNull() ?: 0.0

    val isValid: Boolean
        get() = salaryAmount > 0.0 && selectedMainSource != null
}

class AddEntryViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val entryRepository: EntryRepository = EntryRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val allocationRepository: AllocationRepository = AllocationRepository(),
    private val sourceRepository: SourceRepository = SourceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    private var subSourcesJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = authRepository.currentUser?.uid ?: return

        // Listen to profile
        viewModelScope.launch {
            profileRepository.getProfileFlow(uid).collectLatest { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(userProfile = profile) }
                }
            }
        }

        // Listen to allocation settings
        viewModelScope.launch {
            allocationRepository.getAllocationFlow(uid).collectLatest { settings ->
                _uiState.update { it.copy(allocationSettings = settings) }
            }
        }

        // Listen to main sources
        viewModelScope.launch {
            sourceRepository.seedDefaultSourcesIfMissing(uid)
        }
        viewModelScope.launch {
            sourceRepository.getMainSourcesFlow(uid).collectLatest { sources ->
                _uiState.update { current ->
                    val defaultMain = current.selectedMainSource ?: sources.firstOrNull()
                    current.copy(
                        mainSources = sources,
                        selectedMainSource = defaultMain
                    )
                }
                if (_uiState.value.selectedMainSource != null) {
                    loadSubSources(_uiState.value.selectedMainSource!!.id)
                }
            }
        }
    }

    fun setMonth(month: Int) {
        _uiState.update { it.copy(month = month) }
    }

    fun setYear(year: Int) {
        _uiState.update { it.copy(year = year) }
    }

    fun setSalaryInput(input: String) {
        val allowed = "0123456789.+-*×/÷() "
        val filtered = input.filter { it in allowed }
        _uiState.update { it.copy(salaryInput = filtered, errorMessage = null) }
    }

    /**
     * Resolves the current math expression in the salary input and replaces it
     * with the evaluated numeric result (e.g. "(30+30)-10" -> "50").
     */
    fun evaluateAndApplySalary() {
        val current = _uiState.value.salaryInput
        val evaluated = MathExpressionEvaluator.evaluate(current) ?: current.toDoubleOrNull()
        if (evaluated != null && evaluated > 0.0) {
            val formatted = MathExpressionEvaluator.formatResult(evaluated)
            _uiState.update { it.copy(salaryInput = formatted, errorMessage = null) }
        }
    }

    fun selectMainSource(source: MainSource) {
        _uiState.update {
            it.copy(
                selectedMainSource = source,
                selectedSubSource = null
            )
        }
        loadSubSources(source.id)
    }

    fun selectSubSource(subSource: SubSource?) {
        _uiState.update { it.copy(selectedSubSource = subSource) }
    }

    private fun loadSubSources(mainSourceId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        subSourcesJob?.cancel()
        subSourcesJob = viewModelScope.launch {
            sourceRepository.getSubSourcesFlow(uid, mainSourceId).collectLatest { subs ->
                _uiState.update { it.copy(subSources = subs) }
            }
        }
    }

    fun addNewSubSourceInline(name: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val mainSource = _uiState.value.selectedMainSource ?: return
        if (name.isBlank()) return

        viewModelScope.launch {
            val order = _uiState.value.subSources.size
            val newId = sourceRepository.addSubSource(uid, mainSource.id, name.trim(), order)
            val newSub = SubSource(id = newId, name = name.trim(), order = order)
            _uiState.update { it.copy(selectedSubSource = newSub) }
        }
    }

    fun saveEntry() {
        val uid = authRepository.currentUser?.uid ?: return
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val entry = IncomeEntry(
                    month = state.month,
                    year = state.year,
                    netSalary = state.salaryAmount,
                    mainSourceId = state.selectedMainSource!!.id,
                    mainSourceName = state.selectedMainSource.name,
                    subSourceId = state.selectedSubSource?.id,
                    subSourceName = state.selectedSubSource?.name,
                    createdAt = System.currentTimeMillis()
                )
                entryRepository.addEntry(uid, entry)
                _uiState.update { it.copy(isSaving = false, entrySaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Failed to save entry")
                }
            }
        }
    }

    fun resetState() {
        _uiState.update {
            it.copy(
                salaryInput = "",
                selectedSubSource = null,
                entrySaved = false,
                errorMessage = null
            )
        }
    }
}
