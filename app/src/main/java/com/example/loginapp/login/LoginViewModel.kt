package com.example.loginapp.login

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.data.model.login.LoginResult
import com.example.loginapp.data.repositories.auth.AuthRepository
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Base : AuthUiState()
    object Loading : AuthUiState()
    object Facebook : AuthUiState()
    object Google : AuthUiState()
    object Telefon : AuthUiState()
    object Success : AuthUiState()
    object Send : AuthUiState()
    data class Error(val message: String?) : AuthUiState()
}

data class UIStateTest(
    val isSignedInSuccessfull: Boolean = false,
    val signInError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _testUiState = MutableStateFlow(UIStateTest())
    val testUiState = _testUiState.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Base)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    private val _pendingIntent = MutableStateFlow<IntentSender?>(null)
    val pendingIntent: StateFlow<IntentSender?> = _pendingIntent

    private val _signInResult = MutableLiveData<LoginResult?>()
    val signInResult: LiveData<LoginResult?> = _signInResult

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    val resendToken: StateFlow<String?> = _verificationId

    fun selectPhoneLogin() {
        _authUiState.value = AuthUiState.Telefon
    }

    fun selectFacebookLogin() {
        _authUiState.value = AuthUiState.Facebook
    }

    fun selectGoogleLogin() {
        //_authUiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val googleCompletionListener = OnCompleteListener<BeginSignInResult> { task ->
                    if (task.isSuccessful) {
                        //signInWithIntent(task.result.pendingIntent)
                        //_authUiState.value = AuthUiState.Success
                        _pendingIntent.value = task.result.pendingIntent.intentSender
                        _authUiState.value = AuthUiState.Google
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            _authUiState.value = AuthUiState.Error(task.exception?.message ?: "An error occured")
                        }
                    }
                }
                authRepository.signInWithGoogle(googleCompletionListener)
                //_authUiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "An error occured")
            }
        }
    }

    suspend fun signInWithIntent(intent: Intent) {
        viewModelScope.launch {
            _signInResult.value = authRepository.signInWithIntent(intent)
        }
    }

    fun onGoogleLoginResult(result: LoginResult) {
        _testUiState.update {it.copy(
            isSignedInSuccessfull = result.data != null,
            signInError = result.errorMessage
        )
        }
    }


    fun signInWithPhoneNumber(phone: String) {
        _authUiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val authCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credentials: PhoneAuthCredential) {
                        _authUiState.value = AuthUiState.Success
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _authUiState.value = AuthUiState.Error(e.message ?: "An error occured")
                    }

                    override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                        _verificationId.value = p0
                        _resendToken.value = p1
                        _authUiState.value = AuthUiState.Send
                    }

                }
                val formattedPhoneNumber = formatPhoneNumber(phone)
                authRepository.signInWithPhoneNumber(
                    formattedPhoneNumber,
                    callbacks = authCallbacks
                )
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "An error occured")
            }
        }
    }

    fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _authUiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val authCompletionListener = OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user
                        _authUiState.value = AuthUiState.Success
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            _authUiState.value = AuthUiState.Error(task.exception?.message ?: "An error occured")
                        }
                    }
                }

                authRepository.signInWithCredential(
                    credential = credential,
                    onCompleteListener = authCompletionListener
                )
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "An error occured")
            }
        }

    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(" ", "")
    }
}