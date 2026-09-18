package com.sahed.money_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahed.money_tracker.data.model.UserProfile
import com.sahed.money_tracker.data.repository.AllocationRepository
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import com.sahed.money_tracker.data.repository.SourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val googleName: String = "",
    val googleEmail: String = "",
    val googlePhotoUrl: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = currencyCode.isNotBlank() && currencySymbol.isNotBlank()
}

class OnboardingViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val allocationRepository: AllocationRepository = AllocationRepository(),
    private val sourceRepository: SourceRepository = SourceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        val user = authRepository.currentUser
        if (user != null) {
            _uiState.update {
                it.copy(
                    googleName = user.displayName ?: "",
                    googleEmail = user.email ?: "",
                    googlePhotoUrl = user.photoUrl?.toString() ?: ""
                )
            }
        }
    }

    fun selectCurrency(code: String, symbol: String) {
        _uiState.update {
            it.copy(
                currencyCode = code,
                currencySymbol = symbol
            )
        }
    }

    fun overrideCurrency(code: String, symbol: String) {
        selectCurrency(code, symbol)
    }

    fun confirmAndContinue() {
        val user = authRepository.currentUser ?: return
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = UserProfile(
                    name = state.googleName,
                    email = state.googleEmail,
                    photoUrl = state.googlePhotoUrl,
                    currencyCode = state.currencyCode,
                    currencySymbol = state.currencySymbol,
                    onboarded = true
                )
                profileRepository.updateProfile(user.uid, profile)
                allocationRepository.seedDefaultAllocationIfMissing(user.uid)
                sourceRepository.seedDefaultSourcesIfMissing(user.uid)

                _uiState.update { it.copy(isLoading = false, isComplete = true) }
            } catch (e: Exception) {
                val rawMsg = e.localizedMessage ?: "Failed to save profile"
                val friendlyMsg = if (rawMsg.contains("PERMISSION_DENIED", ignoreCase = true) || rawMsg.contains("permission", ignoreCase = true)) {
                    "Permission denied: Cloud Firestore security rules are blocking writes. Please publish the project's firestore.rules in Firebase Console."
                } else {
                    rawMsg
                }
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = friendlyMsg)
                }
            }
        }
    }
}
