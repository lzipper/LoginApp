package com.example.loginapp.data.repositories.auth

import android.app.PendingIntent
import android.content.Intent
import com.example.loginapp.R
import com.example.loginapp.data.model.login.LoginResult
import com.example.loginapp.data.model.login.UserData
import com.example.loginapp.data.repositories.task.TaskRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [TaskRepository]. Single entry point for managing tasks' data.
 *
 * @param networkDataSource - The network data source
 * @param localDataSource - The local data source
 * @param dispatcher - The dispatcher to be used for long running or complex operations, such as ID
 * generation or mapping many models.
 * @param scope - The coroutine scope used for deferred jobs where the result isn't important, such
 * as sending data to the network.
 */
@Singleton
class DefaultLoginRepository @Inject constructor(
    private val oneTapClient: SignInClient,
    //private val networkDataSource: NetworkDataSource,
    //private val localDataSource: TaskDao,
    //@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    //@ApplicationScope private val scope: CoroutineScope,
) : AuthRepository {

    private val auth = Firebase.auth

    override suspend fun signInWithPhoneNumber(
        phone: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)       // Telefonnummer des Benutzers
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout-Dauer
            //.setActivity(activity)             // Aktivität, die das OTP-Verifizierungsdialog anzeigt
            .setCallbacks(callbacks)           // Callbacks für den Authentifizierungsstatus
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithCredential(
        credential: AuthCredential,
        onCompleteListener: OnCompleteListener<AuthResult>
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(onCompleteListener)
            .await()
    }

    override suspend fun signInWithGoogle(completionListener: OnCompleteListener<BeginSignInResult>) {
        try {
            oneTapClient.beginSignIn(
                signInWithGoogleBuilder()
            ).addOnCompleteListener(completionListener)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override suspend fun signInWithIntent(intent: Intent): LoginResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            LoginResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            LoginResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    override suspend fun signInWithGoogleBuilder(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(R.string.web_client.toString())
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
    }

    override suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override suspend fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}
