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
import com.mitarifamitaxi.taximetrousuario.helpers.formatDigits
import com.mitarifamitaxi.taximetrousuario.helpers.formatNumberWithDots
import com.mitarifamitaxi.taximetrousuario.helpers.shareFormatDate
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder

class TripSummaryViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    var isDetails by mutableStateOf(false)

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    var tripData by mutableStateOf(Trip())

    var showShareDialog by mutableStateOf(false)
    var shareNumber = mutableStateOf("")
    var isShareNumberError = mutableStateOf(false)

    fun deleteTrip(tripId: String) {

        viewModelScope.launch {
            try {
                appViewModel.isLoading = true
                FirebaseFirestore.getInstance().collection("trips").document(tripId).delete()
                    .await()
                appViewModel.isLoading = false
                showCustomDialog(
                    DialogType.SUCCESS,
                    appContext.getString(R.string.success),
                    appContext.getString(R.string.trip_deleted_successfully),
                    appContext.getString(R.string.accept),
                    false
                )
            } catch (error: Exception) {
                Log.e("TripSummaryViewModel", "Error deleting trip: ${error.message}")
                appViewModel.isLoading = false
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_on_delete_trip),
                )

            }
        }
    }

    fun showCustomDialog(
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

    fun sendWatsAppMessage(onIntentReady: (Intent) -> Unit) {

        if (shareNumber.value.isEmpty()) {
            isShareNumberError.value = true
            return
        }

        showShareDialog = false

        val message = buildString {
            append("*Esta es la información de mi viaje:*\n")
            append("*Dirección de origen:* ${tripData.startAddress}\n")
            append("*Dirección de destino:* ${tripData.endAddress}\n")
            append("*Fecha de recogida:* ${tripData.startHour?.let { shareFormatDate(it) }}\n")
            append("*Fecha de llegada:* ${tripData.endHour?.let { shareFormatDate(it) }}\n")
            append(
                "*Distancia recorrida:* ${
                    tripData.distance?.let { (it / 1000).toDouble().formatDigits(1) }
                } KM\n"
            )
            append("*Unidades:* ${tripData.units}\n")

            if (tripData.airportSurchargeEnabled == true) {
                append("*Recargo aeropuerto:* ${tripData.airportSurcharge?.formatNumberWithDots()} COP\n")
            }

            if (tripData.holidaySurchargeEnabled == true) {
                append("*Recargo nocturno dominical o festivo:* ${tripData.holidaySurcharge?.formatNumberWithDots()} COP\n")
            }

            if (tripData.doorToDoorSurchargeEnabled == true) {
                append("*Recargo puerta a puerta:* ${tripData.doorToDoorSurcharge?.formatNumberWithDots()} COP\n")
            }

            append(
                "*${appContext.getString(R.string.fare)}* ${
                    tripData.total?.toInt()?.formatNumberWithDots()
                } COP"
            )
        }

        val messageToSend = URLEncoder.encode(message, "UTF-8").replace("%0A", "%0D%0A")
        val whatsappURL =
            "whatsapp://send?text=$messageToSend&phone=${appViewModel.userData?.countryCodeWhatsapp}${shareNumber.value}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(whatsappURL)
        }

        if (intent.resolveActivity(appContext.packageManager) != null) {
            onIntentReady(intent)
        } else {
            showCustomDialog(
                DialogType.ERROR,
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.whatsapp_not_installed)
            )
        }
    }


}

class TripSummaryViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripSummaryViewModel::class.java)) {
            return TripSummaryViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}