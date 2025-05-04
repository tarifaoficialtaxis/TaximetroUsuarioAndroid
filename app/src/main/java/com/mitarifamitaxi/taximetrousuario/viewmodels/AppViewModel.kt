package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser

class AppViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    var isLoading by mutableStateOf(false)

    var userData: LocalUser? by mutableStateOf(null)


    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogButtonText: String? = null

    var dialogOnDismiss: (() -> Unit)? = null
    var dialogOnPrimaryActionClicked: (() -> Unit)? = null

    init {
        loadUserData()
    }

    fun reloadUserData() {
        loadUserData()
    }

    private fun loadUserData() {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)
        userData = Gson().fromJson(userJson, LocalUser::class.java)
    }

    fun showMessage(
        type: DialogType,
        title: String,
        message: String,
        buttonText: String? = null,
    ) {
        dialogType = type
        dialogTitle = title
        dialogMessage = message
        showDialog = true
        dialogButtonText = buttonText
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