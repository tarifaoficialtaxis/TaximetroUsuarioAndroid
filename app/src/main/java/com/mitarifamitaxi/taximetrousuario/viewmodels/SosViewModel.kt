package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.DialogType


class SosViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    init {
        Handler(Looper.getMainLooper()).postDelayed({
            showCustomDialog(
                DialogType.WARNING,
                appContext.getString(R.string.warning),
                appContext.getString(R.string.article_35_message),
                appContext.getString(R.string.confirm),
                showCloseButton = false
            )
        }, 700)
    }


    private fun showCustomDialog(
        type: DialogType,
        title: String,
        message: String,
        primaryAction: String? = null,
        showCloseButton: Boolean = true
    ) {
        showDialog = true
        dialogType = type
        dialogTitle = title
        dialogMessage = message
        dialogPrimaryAction = primaryAction
        dialogShowCloseButton = showCloseButton
    }


}

class SosViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SosViewModel::class.java)) {
            return SosViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}