package com.sahed.money_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.sahed.money_tracker.data.repository.AuthRepository
import com.sahed.money_tracker.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Initial : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser, val isOnboarded: Boolean) : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentAuthStatus()
    }

    private fun checkCurrentAuthStatus() {
        val user = authRepository.currentUser
        if (user != null) {
            viewModelScope.launch {
                handleUserAuthenticated(user)
            }
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { user ->
                handleUserAuthenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Sign in failed")
            }
        }
    }

    private suspend fun handleUserAuthenticated(user: FirebaseUser) {
        val profile = profileRepository.getProfileOnce(user.uid)
        // Re-sync Google profile info (name, email, photoUrl)
        val name = user.displayName ?: ""
        val email = user.email ?: ""
        val photoUrl = user.photoUrl?.toString() ?: ""
        profileRepository.syncGoogleProfile(user.uid, name, email, photoUrl)

        val isOnboarded = profile?.onboarded == true
        _uiState.value = AuthUiState.Authenticated(user = user, isOnboarded = isOnboarded)
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState.Unauthenticated
    }

    fun clearError() {
        _uiState.value = AuthUiState.Unauthenticated
    }
}
