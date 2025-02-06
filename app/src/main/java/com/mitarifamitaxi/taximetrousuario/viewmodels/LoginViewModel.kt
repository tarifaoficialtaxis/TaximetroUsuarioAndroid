package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    // Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Example username/password states
    var userName by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)


    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(onResult: (Pair<Boolean, String?>) -> Unit) {

        if (userName.isEmpty() || password.isEmpty()) {
            Log.e(TAG, "Error: Email and password are required")
            onResult(Pair(false, appContext.getString(R.string.all_fields_required)))
            return
        }

        appViewModel.isLoading = true

        viewModelScope.launch {
            try {
                val userCredential =
                    auth.signInWithEmailAndPassword(userName.trim(), password).await()
                val user = userCredential.user
                if (user != null) {
                    getUserInformation(user.uid) { userExists ->
                        appViewModel.isLoading = false
                        if (userExists) {
                            Log.d(TAG, "Login successful")
                            onResult(Pair(true, null))
                        } else {
                            Log.d(TAG, "User needs to complete profile")
                            onResult(Pair(false, null))
                        }
                    }
                }
            } catch (e: Exception) {
                appViewModel.isLoading = false
                Log.e(TAG, "Login Error: ${e.message}")
                onResult(Pair(false, e.message))
            }
        }

    }

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(appContext, gso)
    }

    fun handleSignInResult(data: Intent?, onResult: (Pair<String, LocalUser?>) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!, onResult)
            } else {
                Log.e(TAG, "Google Sign-In failed: No account found")
                onResult(Pair("Error signing in with Google", null))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: ${e.localizedMessage}")
            onResult(Pair("Error signing in with Google", null))
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onResult: (Pair<String, LocalUser?>) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "Firebase Sign-In success. User: ${user?.displayName}")

                    viewModelScope.launch {
                        getUserInformation(user?.uid ?: "", userExistsCallback = {
                            if (it) {
                                onResult(Pair("home", null))
                            } else {
                                user?.let {
                                    val userData = LocalUser(
                                        id = it.uid,
                                        email = it.email,
                                        firstName = it.displayName?.split(" ")?.get(0),
                                        lastName = it.displayName?.split(" ")?.get(1),
                                        mobilePhone = it.phoneNumber
                                    )
                                    onResult(Pair("complete_profile", userData))
                                }
                            }
                        })
                    }
                } else {
                    Log.e(TAG, "Firebase Sign-In failed: ${task.exception}")
                    onResult(Pair("Error signing in with Google", null))
                }
            }
    }

    private suspend fun getUserInformation(userId: String, userExistsCallback: (Boolean) -> Unit) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val userData = userDoc.toObject<LocalUser>()
                if (userData != null) {
                    saveUserState(userData)
                    userExistsCallback(true)
                } else {
                    throw Exception("User data not found in Firestore")
                }
            } else {
                userExistsCallback(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user information", e)
        }
    }

    private fun saveUserState(user: LocalUser) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_OBJECT", Gson().toJson(user))
            apply()
        }
    }
}

/**
 * A simple factory to provide the LoginViewModel with a context.
 */
class LoginViewModelFactory(private val context: Context, private val appViewModel: AppViewModel) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
