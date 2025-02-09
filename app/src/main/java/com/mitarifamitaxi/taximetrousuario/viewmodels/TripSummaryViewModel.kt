package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class TripSummaryViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    var tripData by mutableStateOf(Trip())

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