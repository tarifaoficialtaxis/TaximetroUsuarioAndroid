package com.mitarifamitaxi.taximetrousuario.viewmodels.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.LocalUserManager
import com.mitarifamitaxi.taximetrousuario.helpers.isValidEmail
import com.mitarifamitaxi.taximetrousuario.models.AuthProvider
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompleteProfileViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext

    var userId by mutableStateOf("")
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var mobilePhone by mutableStateOf("")
    var email by mutableStateOf("")
    var authProvider by mutableStateOf(AuthProvider.google)

    fun completeProfile(onResult: (Pair<Boolean, String?>) -> Unit) {
        if (firstName.isEmpty() || lastName.isEmpty() || mobilePhone.isEmpty() || email.isEmpty()) {

            appViewModel.showMessage(
                type = DialogType.ERROR,
                title = appContext.getString(R.string.something_went_wrong),
                message = appContext.getString(R.string.all_fields_required),
            )

            return
        }

        if (!email.isValidEmail()) {
            appViewModel.showMessage(
                type = DialogType.ERROR,
                title = appContext.getString(R.string.something_went_wrong),
                message = appContext.getString(R.string.error_invalid_email),
            )
            return
        }

        viewModelScope.launch {
            try {
                // Show loading indicator
                appViewModel.isLoading = true

                // Save user information in Firestore
                val userMap = hashMapOf(
                    "id" to userId,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "mobilePhone" to mobilePhone.trim(),
                    "email" to email.trim()
                )
                FirebaseFirestore.getInstance().collection("users").document(userId).set(userMap)
                    .await()

                // Hide loading indicator
                appViewModel.isLoading = false

                // Save user in SharedPreferences
                val localUser = LocalUser(
                    id = userId,
                    firstName = firstName,
                    lastName = lastName,
                    mobilePhone = mobilePhone,
                    email = email,
                    authProvider = authProvider
                )

                LocalUserManager(appContext).saveUserState(localUser)

                onResult(Pair(true, null))

            } catch (e: Exception) {
                // Hide loading indicator
                appViewModel.isLoading = false
                // Show error message
                onResult(Pair(false, e.message))
            }

        }

    }

}

class CompleteProfileViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompleteProfileViewModel::class.java)) {
            return CompleteProfileViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}