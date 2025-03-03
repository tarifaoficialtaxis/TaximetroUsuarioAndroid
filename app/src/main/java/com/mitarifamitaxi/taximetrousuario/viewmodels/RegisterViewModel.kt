package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.isValidEmail
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var mobilePhone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")

    fun register(onResult: (Pair<Boolean, String?>) -> Unit) {
        // Login logic
        if (firstName.isEmpty() || lastName.isEmpty() || mobilePhone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.all_fields_required)
            )
            return
        }

        if (!email.isValidEmail()) {
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_invalid_email)
            )
            return
        }

        if (password != confirmPassword) {
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.passwords_do_not_match)
            )
            return
        }


        viewModelScope.launch {
            try {
                // Show loading indicator
                appViewModel.isLoading = true

                // Create user with email and password in Firebase Auth
                val authResult =
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.trim(), password.trim())
                        .await()
                val user = authResult.user ?: throw Exception("User creation failed")

                // Save user information in Firestore
                val userMap = hashMapOf(
                    "id" to user.uid,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "mobilePhone" to mobilePhone.trim(),
                    "email" to email.trim()
                )
                FirebaseFirestore.getInstance().collection("users").document(user.uid).set(userMap)
                    .await()

                // Hide loading indicator
                appViewModel.isLoading = false

                // Save user in SharedPreferences
                val localUser = LocalUser(
                    id = user.uid,
                    firstName = firstName,
                    lastName = lastName,
                    mobilePhone = mobilePhone.trim(),
                    email = email.trim()
                )
                saveUserState(localUser)

                onResult(Pair(true, null))

            } catch (e: Exception) {
                // Hide loading indicator
                appViewModel.isLoading = false
                showErrorMessage(
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_registering_user)
                )
            }

        }
    }

    private fun saveUserState(user: LocalUser) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_OBJECT", Gson().toJson(user))
            apply()
        }
    }

    private fun showErrorMessage(title: String, message: String) {
        showDialog = true
        dialogType = DialogType.ERROR
        dialogTitle = title
        dialogMessage = message
    }
}

class RegisterViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}