package com.mitarifamitaxi.taximetrousuario.viewmodels

import com.mitarifamitaxi.taximetrousuario.BuildConfig
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.AppVersion
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.content.Intent
import android.net.Uri

class AppViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    var isLoading by mutableStateOf(false)

    var userData: LocalUser? by mutableStateOf(null)

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogButtonText: String? = null

    var dialogShowCloseButton: Boolean = true
    var dialogOnDismiss: (() -> Unit)? = null
    var dialogOnPrimaryActionClicked: (() -> Unit)? = null

    init {
        loadUserData()
        validateAppVersion()
    }

    fun reloadUserData() {
        loadUserData()
    }

    private fun loadUserData() {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)
        userData = Gson().fromJson(userJson, LocalUser::class.java)
    }

    fun validateAppVersion() {

        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val querySnapshot = withContext(Dispatchers.IO) {
                    firestore.collection("appVersion")
                        .document("android")
                        .get()
                        .await()
                }

                if (querySnapshot != null && querySnapshot.exists()) {
                    try {
                        val appVersionObj =
                            querySnapshot.toObject(AppVersion::class.java) ?: AppVersion()

                        if (BuildConfig.VERSION_NAME != appVersionObj.version && appVersionObj.show == true) {
                            showMessage(
                                type = DialogType.WARNING,
                                title = appContext.getString(R.string.attention),
                                message = appContext.getString(R.string.new_app_version_message),
                                buttonText = appContext.getString(R.string.update),
                                showCloseButton = false,
                                onButtonClicked = {
                                    appVersionObj.urlStore?.let { openUrlStore(it) }
                                }
                            )

                        } else {
                            Log.i("AppViewModel", "App Version is up to date")
                        }


                    } catch (e: Exception) {
                        Log.e("AppViewModel", "Error parsing rates: ${e.message}")
                        showMessage(
                            type = DialogType.ERROR,
                            title = appContext.getString(R.string.something_went_wrong),
                            message = appContext.getString(R.string.general_error)
                        )
                    }
                } else {
                    showMessage(
                        type = DialogType.ERROR,
                        title = appContext.getString(R.string.something_went_wrong),
                        message = appContext.getString(R.string.general_error)
                    )
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching contacts: ${e.message}")
                showMessage(
                    type = DialogType.ERROR,
                    title = appContext.getString(R.string.something_went_wrong),
                    message = appContext.getString(R.string.general_error)
                )
            }

        }

    }

    fun openUrlStore(urlStore: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStore))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppViewModel", "Could not open store URL: $urlStore", e)
            showMessage(
                type = DialogType.ERROR,
                title = appContext.getString(R.string.error),
                message = appContext.getString(R.string.cannot_open_store_url)
            )
        }
    }


    fun showMessage(
        type: DialogType,
        title: String,
        message: String,
        buttonText: String? = null,
        showCloseButton: Boolean = true,
        onDismiss: (() -> Unit)? = null,
        onButtonClicked: (() -> Unit)? = null
    ) {
        dialogType = type
        dialogTitle = title
        dialogMessage = message
        showDialog = true
        dialogButtonText = buttonText
        dialogShowCloseButton = showCloseButton
        dialogOnDismiss = onDismiss
        dialogOnPrimaryActionClicked = onButtonClicked
    }

}

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}