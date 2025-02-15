package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.UserLocation


class TaximeterViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext
    private val localConfiguration: Configuration = appContext.resources.configuration

    var startAddress by mutableStateOf("")
    var startLocation by mutableStateOf(UserLocation())

    var endAddress by mutableStateOf("")
    var endLocation by mutableStateOf(UserLocation())

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")



    // Taximeter values
    var total by mutableStateOf(0)

    init {
        //setDefaultHeights()
    }




}

class TaximeterViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaximeterViewModel::class.java)) {
            return TaximeterViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
