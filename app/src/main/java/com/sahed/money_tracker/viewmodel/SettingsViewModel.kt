package com.sahed.money_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.AllocationSettings
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.preferences.AppPreferences
import com.sahed.money_tracker.data.preferences.ThemeMode
import com.sahed.money_tracker.data.repository.AllocationRepository
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val allocationSettings: AllocationSettings = AllocationSettings(),
    val themeMode: ThemeMode = ThemeMode.DARK,
    val notificationsEnabled: Boolean = true,
    val timeZone: String = "",
    val isSavingAllocation: Boolean = false,
    val allocationErrorMessage: String? = null,
    val allocationSuccess: Boolean = false
)

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val allocationRepository: AllocationRepository = AllocationRepository()
) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application.applicationContext)

    private val _uiState = MutableStateFlow(
        SettingsUiState(timeZone = preferences.getTimeZoneId())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val uid = authRepository.currentUser?.uid ?: return

        // Listen to profile
        viewModelScope.launch {
            try {
                profileRepository.getProfileFlow(uid).collectLatest { profile ->
                    if (profile != null) {
                        _uiState.update { it.copy(userProfile = profile) }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Listen to allocation settings
        viewModelScope.launch {
            try {
                allocationRepository.getAllocationFlow(uid).collectLatest { settings ->
                    _uiState.update { it.copy(allocationSettings = settings) }
                }
            } catch (_: Exception) {
            }
        }

        // Listen to theme preferences
        viewModelScope.launch {
            try {
                preferences.themeMode.collectLatest { mode ->
                    _uiState.update { it.copy(themeMode = mode) }
                }
            } catch (_: Exception) {
            }
        }

        // Listen to notifications preferences
        viewModelScope.launch {
            try {
                preferences.notificationsEnabled.collectLatest { enabled ->
                    _uiState.update { it.copy(notificationsEnabled = enabled) }
                }
            } catch (_: Exception) {
            }
        }
    }



    fun updateCurrencyOnly(currencyCode: String, currencySymbol: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val updated = _uiState.value.userProfile.copy(
                currencyCode = currencyCode,
                currencySymbol = currencySymbol
            )
            profileRepository.updateProfile(uid, updated)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun updateAllocation(
        savingPercent: Double,
        savingLabel: String,
        investPercent: Double,
        investLabel: String,
        donatePercent: Double,
        donateLabel: String,
        restLabel: String
    ) {
        val uid = authRepository.currentUser?.uid ?: return
        val total = savingPercent + investPercent + donatePercent

        if (total > 100.0) {
            _uiState.update {
                it.copy(
                    allocationErrorMessage = "Total allocation exceeds 100% (currently ${String.format(java.util.Locale.US, "%.1f", total)}%). Please adjust the percentages.",
                    allocationSuccess = false
                )
            }
            return
        }

        val newSettings = AllocationSettings(
            savingPercent = savingPercent,
            savingLabel = savingLabel.ifBlank { "Saving" },
            investPercent = investPercent,
            investLabel = investLabel.ifBlank { "Investing" },
            donatePercent = donatePercent,
            donateLabel = donateLabel.ifBlank { "Donate" },
            restLabel = restLabel.ifBlank { "Rest" }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAllocation = true, allocationErrorMessage = null) }
            try {
                allocationRepository.updateAllocation(uid, newSettings)
                _uiState.update { it.copy(isSavingAllocation = false, allocationSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSavingAllocation = false, allocationErrorMessage = e.localizedMessage ?: "Failed to save settings")
                }
            }
        }
    }

    fun clearAllocationStatus() {
        _uiState.update { it.copy(allocationErrorMessage = null, allocationSuccess = false) }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
