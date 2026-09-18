package com.sahed.money_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.SourceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageSourcesUiState(
    val mainSources: List<MainSource> = emptyList(),
    val selectedMainSource: MainSource? = null,
    val subSourcesForSelected: List<SubSource> = emptyList(),
    val isLoading: Boolean = false
)

class ManageSourcesViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val sourceRepository: SourceRepository = SourceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageSourcesUiState())
    val uiState: StateFlow<ManageSourcesUiState> = _uiState.asStateFlow()

    private var subSourcesJob: Job? = null

    init {
        loadSources()
    }

    private fun loadSources() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            sourceRepository.seedDefaultSourcesIfMissing(uid)
        }
        viewModelScope.launch {
            sourceRepository.getMainSourcesFlow(uid).collectLatest { sources ->
                _uiState.update { current ->
                    val selected = current.selectedMainSource?.let { sel ->
                        sources.find { it.id == sel.id }
                    } ?: sources.firstOrNull()

                    current.copy(
                        mainSources = sources,
                        selectedMainSource = selected
                    )
                }

                _uiState.value.selectedMainSource?.let { sel ->
                    loadSubSources(sel.id)
                }
            }
        }
    }

    fun selectMainSource(source: MainSource) {
        _uiState.update { it.copy(selectedMainSource = source) }
        loadSubSources(source.id)
    }

    private fun loadSubSources(mainSourceId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        subSourcesJob?.cancel()
        subSourcesJob = viewModelScope.launch {
            sourceRepository.getSubSourcesFlow(uid, mainSourceId).collectLatest { subs ->
                _uiState.update { it.copy(subSourcesForSelected = subs) }
            }
        }
    }

    fun addMainSource(name: String) {
        val uid = authRepository.currentUser?.uid ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val order = _uiState.value.mainSources.size
            sourceRepository.addMainSource(uid, name.trim(), order)
        }
    }

    fun renameMainSource(source: MainSource, newName: String) {
        val uid = authRepository.currentUser?.uid ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            sourceRepository.updateMainSource(uid, source.copy(name = newName.trim()))
        }
    }

    fun deleteMainSource(sourceId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            sourceRepository.deleteMainSource(uid, sourceId)
        }
    }

    fun addSubSource(name: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val mainSource = _uiState.value.selectedMainSource ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val order = _uiState.value.subSourcesForSelected.size
            sourceRepository.addSubSource(uid, mainSource.id, name.trim(), order)
        }
    }

    fun renameSubSource(subSource: SubSource, newName: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val mainSource = _uiState.value.selectedMainSource ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            sourceRepository.updateSubSource(uid, mainSource.id, subSource.copy(name = newName.trim()))
        }
    }

    fun deleteSubSource(subSourceId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val mainSource = _uiState.value.selectedMainSource ?: return
        viewModelScope.launch {
            sourceRepository.deleteSubSource(uid, mainSource.id, subSourceId)
        }
    }

    fun moveMainSource(fromIndex: Int, toIndex: Int) {
        val uid = authRepository.currentUser?.uid ?: return
        val currentList = _uiState.value.mainSources.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) return

        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        _uiState.update { it.copy(mainSources = currentList) }

        viewModelScope.launch {
            sourceRepository.reorderMainSources(uid, currentList)
        }
    }

    fun moveSubSource(fromIndex: Int, toIndex: Int) {
        val uid = authRepository.currentUser?.uid ?: return
        val mainSource = _uiState.value.selectedMainSource ?: return
        val currentList = _uiState.value.subSourcesForSelected.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) return

        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        _uiState.update { it.copy(subSourcesForSelected = currentList) }

        viewModelScope.launch {
            sourceRepository.reorderSubSources(uid, mainSource.id, currentList)
        }
    }
}
