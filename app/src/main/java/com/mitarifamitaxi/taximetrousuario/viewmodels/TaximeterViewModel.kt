package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Rates
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class TaximeterViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext

    var startAddress by mutableStateOf("")
    var startLocation by mutableStateOf(UserLocation())

    var endAddress by mutableStateOf("")
    var endLocation by mutableStateOf(UserLocation())

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    var isFabExpanded by mutableStateOf(false)

    // Taximeter values
    var total by mutableStateOf(0)
    var distanceMade by mutableStateOf(0)
    var units by mutableStateOf(0.0)

    var timeElapsed by mutableStateOf(0)
    var formattedTime by mutableStateOf("0")

    var isAirportSurcharge by mutableStateOf(false)
    var isHolidaySurcharge by mutableStateOf(false)
    var isDoorToDoorSurcharge by mutableStateOf(false)

    val ratesObj = mutableStateOf(Rates())

    var isTaximeterStarted by mutableStateOf(false)

    init {
        getCityRates(appViewModel.userData?.city)
    }

    private fun getCityRates(userCity: String?) {

        viewModelScope.launch {
            if (userCity != null) {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val ratesQuerySnapshot = withContext(Dispatchers.IO) {
                        firestore.collection("rates")
                            .whereEqualTo("city", userCity)
                            .get()
                            .await()
                    }

                    if (!ratesQuerySnapshot.isEmpty) {
                        val contactsDoc = ratesQuerySnapshot.documents[0]
                        try {
                            ratesObj.value =
                                contactsDoc.toObject(Rates::class.java) ?: Rates()
                        } catch (e: Exception) {
                            showCustomDialog(
                                DialogType.ERROR,
                                appContext.getString(R.string.something_went_wrong),
                                appContext.getString(R.string.general_error)
                            )
                        }
                    } else {
                        showCustomDialog(
                            DialogType.ERROR,
                            appContext.getString(R.string.something_went_wrong),
                            appContext.getString(R.string.general_error)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("TaximeterViewModel", "Error fetching contacts: ${e.message}")
                    showCustomDialog(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.general_error)
                    )
                }
            } else {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_no_city_set)
                )
            }
        }

    }

    fun startTaximeter() {
        isTaximeterStarted = true
        units = ratesObj.value.startRateUnits ?: 0.0
        startTimer()
    }

    fun stopTaximeter() {
        isTaximeterStarted = false
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (isTaximeterStarted) {
                timeElapsed++
                val hours = timeElapsed / 3600
                val minutes = (timeElapsed % 3600) / 60
                val seconds = timeElapsed % 60

                formattedTime = when {

                    timeElapsed < 3600 -> {
                        String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    }

                    else -> {
                        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                    }
                }

                delay(1000)
            }
        }
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
