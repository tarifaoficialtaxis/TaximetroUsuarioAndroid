package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.User
import kotlinx.coroutines.tasks.await

class LoginViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    // Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // One Tap client
    private val oneTapClient: SignInClient = Identity.getSignInClient(appContext)

    // Example username/password states
    var userName by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login() {
        // Do your normal email/password login...
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        onResult: (androidx.activity.result.IntentSenderRequest?) -> Unit
    ) {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            val intentSenderRequest =
                androidx.activity.result.IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                    .build()
            onResult(intentSenderRequest)
        } catch (e: Exception) {
            Log.e(TAG, "One Tap Sign-In failed: ${e.localizedMessage}")
            onResult(null)
        }
    }


    fun handleSignInResult(data: Intent?, onResult: (Boolean) -> Unit) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken, onResult)
            } else {
                Log.e(TAG, "No ID token!")
                onResult(false)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: ${e.localizedMessage}")
            onResult(false)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "Firebase Sign-In success. User: ${user?.displayName}")

                    val localUser = user?.displayName?.let {
                        user.email?.let { email ->
                            User(user.uid, it, email)
                        }
                    }
                    val userJson = Gson().toJson(localUser)

                    val sharedPref =
                        appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("USER_OBJECT", userJson)
                        apply()
                    }

                    onResult(true)
                } else {
                    Log.e(TAG, "Firebase Sign-In failed: ${task.exception}")
                    onResult(false)
                }
            }
    }
}

/**
 * A simple factory to provide the LoginViewModel with a context.
 */
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
