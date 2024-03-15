package com.example.loginapp.data.repositories.auth

import android.app.PendingIntent
import android.content.Intent
import com.example.loginapp.data.model.login.LoginResult
import com.example.loginapp.data.model.login.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks

interface AuthRepository {
    suspend fun signInWithPhoneNumber(phone: String, callbacks: OnVerificationStateChangedCallbacks)
    suspend fun signInWithCredential(credential: AuthCredential, onCompleteListener: OnCompleteListener<AuthResult>)
    suspend fun signInWithGoogle(completionListener: OnCompleteListener<BeginSignInResult>)
    suspend fun signInWithIntent(intent: Intent): LoginResult
    suspend fun signInWithGoogleBuilder(): BeginSignInRequest
    suspend fun signOut()
    suspend fun getSignedInUser(): UserData?
}