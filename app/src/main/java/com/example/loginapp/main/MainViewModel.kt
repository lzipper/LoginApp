package com.example.loginapp.main

import androidx.lifecycle.ViewModel
import com.example.loginapp.data.model.login.Profile
import com.example.loginapp.data.repositories.auth.AuthRepository
import com.example.loginapp.login.AuthUiState
import com.example.loginapp.login.UIStateTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class MainUiState {
    object Base : MainUiState()
    object Loading : MainUiState()
    object Success : MainUiState()
    data class Error(val message: String?) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val authRepository: AuthRepository
) : ViewModel() {

    private val _authUiState = MutableStateFlow<MainUiState>(MainUiState.Base)
    val authUiState: StateFlow<MainUiState> = _authUiState

    fun onSwipeLeft(profile: Profile) {

    }

    fun onSwipeRight(profile: Profile) {

    }

}