package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mitarifamitaxi.taximetrousuario.helpers.getAddressFromCoordinates
import com.mitarifamitaxi.taximetrousuario.helpers.getShortAddress

class RoutePlannerViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext
    private val localConfiguration: Configuration = appContext.resources.configuration


    var startAddress by mutableStateOf("")
    var endAddress by mutableStateOf("")

    var isSheetExpanded by mutableStateOf(true)


    var mainColumnHeight by mutableStateOf(0.dp)
    var sheetPeekHeight by mutableStateOf(0.dp)


    init {

        if (appViewModel.userData?.location?.latitude != null && appViewModel.userData?.location?.longitude != null) {
            getAddressFromCoordinates(
                latitude = appViewModel.userData?.location?.latitude!!,
                longitude = appViewModel.userData?.location?.longitude!!,
                callbackSuccess = { address ->
                    startAddress = getShortAddress(address)
                },
                callbackError = { error ->

                }
            )
        }


        mainColumnHeight = (localConfiguration.screenHeightDp * 0.4).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.65).dp


    }


    fun setPointOnMap() {

        isSheetExpanded = false
        mainColumnHeight = (localConfiguration.screenHeightDp * 0.75).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.3).dp
    }

}

class RoutePlannerViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutePlannerViewModel::class.java)) {
            return RoutePlannerViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

