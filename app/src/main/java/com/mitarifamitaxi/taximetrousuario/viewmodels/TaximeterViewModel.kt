package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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

    var isFabExpanded by mutableStateOf(false)

    // Taximeter values
    var total by mutableStateOf(0)
    var distanceMade by mutableStateOf(0)
    var units by mutableStateOf(0)

    var timeElapsed by mutableStateOf(0)

    var isAirportSurcharge by mutableStateOf(false)
    var isHolidaySurcharge by mutableStateOf(false)
    var isDoorToDoorSurcharge by mutableStateOf(false)

    init {
        //setDefaultHeights()
    }

    fun openGoogleMapsApp(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        onIntentReady: (Intent) -> Unit
    ) {
        val url =
            "comgooglemaps://?saddr=$originLat,$originLng&daddr=$destLat,$destLng&directionsmode=driving"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps")
            onIntentReady(intent)

        } catch (e: Exception) {
            val webUrl =
                "https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            onIntentReady(webIntent)

        }
    }

    fun openWazeApp(destLat: Double, destLng: Double, onIntentReady: (Intent) -> Unit) {
        val wazeUrl = "waze://?ll=$destLat,$destLng&navigate=yes"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wazeUrl))
            intent.setPackage("com.waze")
            onIntentReady(intent)
        } catch (e: Exception) {
            val webUrl = "https://waze.com/ul?ll=$destLat,$destLng&navigate=yes"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            onIntentReady(webIntent)
        }
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
