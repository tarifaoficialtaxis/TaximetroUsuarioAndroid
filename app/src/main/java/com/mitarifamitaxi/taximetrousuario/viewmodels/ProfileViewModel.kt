package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.isValidEmail
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    var firstName by mutableStateOf(appViewModel.userData?.firstName)
    var lastName by mutableStateOf(appViewModel.userData?.lastName)
    var mobilePhone by mutableStateOf(appViewModel.userData?.mobilePhone)
    var email by mutableStateOf(appViewModel.userData?.email)
    var familyNumber by mutableStateOf(appViewModel.userData?.familyNumber)
    var supportNumber by mutableStateOf(appViewModel.userData?.supportNumber)

    var tripsCount by mutableIntStateOf(0)
    var distanceCount by mutableIntStateOf(0)

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    init {
        viewModelScope.launch {
            getTripsByUserId(appViewModel.userData?.id ?: "")
        }
    }

    private suspend fun getTripsByUserId(userId: String) {
        try {
            appViewModel.isLoading = true
            val tripsSnapshot = FirebaseFirestore.getInstance()
                .collection("trips")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            tripsCount = tripsSnapshot.size()

            if (!tripsSnapshot.isEmpty) {
                val trips = tripsSnapshot.documents
                val distance = trips.sumOf { it.getDouble("distance") ?: 0.0 }
                distanceCount = distance.toInt()
            }
            appViewModel.isLoading = false
        } catch (error: Exception) {
            Log.e("ProfileViewModel", "Error fetching trips: ${error.message}")
            appViewModel.isLoading = false
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_fetching_trips)
            )

        }
    }

    fun handleUpdate() {

        if ((firstName ?: "").isEmpty() ||
            (lastName ?: "").isEmpty() ||
            (mobilePhone ?: "").isEmpty() ||
            (email ?: "").isEmpty() ||
            (familyNumber ?: "").isEmpty() ||
            (supportNumber ?: "").isEmpty()
        ) {
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.all_fields_required)
            )
            return
        }

        if (!(email ?: "").isValidEmail()) {
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_invalid_email)
            )
            return
        }


        viewModelScope.launch {
            appViewModel.isLoading = true
            val updatedUser = appViewModel.userData?.copy(
                firstName = firstName,
                lastName = lastName,
                mobilePhone = mobilePhone,
                email = email,
                familyNumber = familyNumber,
                supportNumber = supportNumber
            )

            try {
                updatedUser?.let { user ->
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(appViewModel.userData?.id ?: "")
                        .update(
                            mapOf(
                                "firstName" to user.firstName,
                                "lastName" to user.lastName,
                                "mobilePhone" to user.mobilePhone,
                                "email" to user.email,
                                "familyNumber" to user.familyNumber,
                                "supportNumber" to user.supportNumber
                            )
                        ).await()

                    appViewModel.userData = user
                    saveUserState(user)
                    appViewModel.isLoading = false
                    showSuccessMessage(
                        appContext.getString(R.string.profile_updated),
                        appContext.getString(R.string.user_updated_successfully),
                        appContext.getString(R.string.accept)
                    )
                }
            } catch (error: Exception) {
                Log.e("ProfileViewModel", "Error updating user: ${error.message}")
                appViewModel.isLoading = false
                showErrorMessage(
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_fetching_trips)
                )
            } finally {
                appViewModel.isLoading = false
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
        dialogShowCloseButton = true
    }

    private fun showSuccessMessage(title: String, message: String, primaryAction: String) {
        showDialog = true
        dialogType = DialogType.SUCCESS
        dialogTitle = title
        dialogMessage = message
        dialogPrimaryAction = primaryAction
        dialogShowCloseButton = false
    }

    fun logOut(onLogoutComplete: () -> Unit) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_OBJECT")
            apply()
        }
        onLogoutComplete()
    }

}

class ProfileViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}