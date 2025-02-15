package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.fetchRoute
import com.mitarifamitaxi.taximetrousuario.helpers.getAddressFromCoordinates
import com.mitarifamitaxi.taximetrousuario.helpers.getPlaceDetails
import com.mitarifamitaxi.taximetrousuario.helpers.getPlacePredictions
import com.mitarifamitaxi.taximetrousuario.helpers.getShortAddress
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.PlacePrediction
import com.mitarifamitaxi.taximetrousuario.models.UserLocation

class RoutePlannerViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext
    private val localConfiguration: Configuration = appContext.resources.configuration


    var startAddress by mutableStateOf("")
    var startLocation by mutableStateOf(UserLocation())

    var endAddress by mutableStateOf("")
    var endLocation by mutableStateOf(UserLocation())

    var tempAddressOnMap by mutableStateOf("")
    private var tempLocationOnMap by mutableStateOf(UserLocation())

    var isSelectingStart by mutableStateOf(true)
    var isSheetExpanded by mutableStateOf(true)

    var mainColumnHeight by mutableStateOf(0.dp)
    var sheetPeekHeight by mutableStateOf(0.dp)


    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")

    private val _places = mutableStateOf<List<PlacePrediction>>(emptyList())
    val places: State<List<PlacePrediction>> = _places


    var routePoints by mutableStateOf<List<LatLng>>(emptyList())

    init {

        if (appViewModel.userData?.location?.latitude != null && appViewModel.userData?.location?.longitude != null) {
            startLocation = appViewModel.userData?.location!!
            getAddressFromCoordinates(
                latitude = appViewModel.userData?.location?.latitude!!,
                longitude = appViewModel.userData?.location?.longitude!!,
                callbackSuccess = { address ->
                    startAddress = getShortAddress(address)
                    isSelectingStart = false
                },
                callbackError = {
                    showCustomDialog(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.error_getting_address)
                    )
                }
            )
        }

        setDefaultHeights()

    }

    private fun setDefaultHeights() {
        isSheetExpanded = true
        mainColumnHeight = (localConfiguration.screenHeightDp * 0.4).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.65).dp
    }


    fun setPointOnMap() {
        isSheetExpanded = false
        mainColumnHeight = (localConfiguration.screenHeightDp * 0.75).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.3).dp
    }


    fun loadAddressBasedOnCoordinates(latitude: Double, longitude: Double) {
        getAddressFromCoordinates(
            latitude = latitude,
            longitude = longitude,
            callbackSuccess = { address ->
                tempAddressOnMap = getShortAddress(address)
                tempLocationOnMap = UserLocation(latitude = latitude, longitude = longitude)
            },
            callbackError = {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_getting_address)
                )
            }
        )
    }

    fun setPontOnMapComplete() {
        setDefaultHeights()

        if (isSelectingStart) {
            startAddress = getShortAddress(tempAddressOnMap)
            startLocation = tempLocationOnMap
        } else {
            endAddress = getShortAddress(tempAddressOnMap)
            endLocation = tempLocationOnMap
        }
    }

    fun validateAddressStates() {

        if (startAddress.isEmpty() && endAddress.isEmpty()) {
            isSelectingStart = true
        } else if (startAddress.isNotEmpty() && endAddress.isEmpty()) {
            isSelectingStart = false
        } else if (startAddress.isEmpty() && endAddress.isNotEmpty()) {
            isSelectingStart = true
        }

    }

    fun getRoutePreview() {

        if (startLocation.latitude == null || startLocation.longitude == null
            || endLocation.latitude == null || endLocation.longitude == null
        ) {
            return
        }

        fetchRoute(
            originLongitude = startLocation.longitude!!,
            originLatitude = startLocation.latitude!!,
            destinationLongitude = endLocation.longitude!!,
            destinationLatitude = endLocation.latitude!!,
            callbackSuccess = { points ->
                routePoints = points
            },
            callbackError = {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_getting_route)
                )
            }
        )
    }

    fun loadPlacePredictions(input: String) {
        if (input.isEmpty() || input.length < 2) {
            _places.value = emptyList()
            return
        }

        getPlacePredictions(
            input = input,
            latitude = appViewModel.userData?.location?.latitude ?: 0.0,
            longitude = appViewModel.userData?.location?.longitude ?: 0.0,
            callbackSuccess = { predictions ->
                _places.value = predictions
            },
            callbackError = {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_getting_places)
                )
            }
        )
    }

    fun setPlacePrediction(placePrediction: PlacePrediction) {

        _places.value = emptyList()
        placePrediction.placeId?.let {
            getPlaceDetails(
                placeId = it,
                callbackSuccess = { location ->
                    if (isSelectingStart) {
                        startAddress = getShortAddress(placePrediction.description)
                        startLocation = location
                    } else {
                        endAddress = getShortAddress(placePrediction.description)
                        endLocation = location
                    }
                },
                callbackError = {
                    showCustomDialog(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.error_getting_address)
                    )
                }
            )
        }


    }

    fun validateStartTrip() {
        if (startAddress.isEmpty() || startLocation.latitude == null || startLocation.longitude == null
            || endAddress.isEmpty() || endLocation.latitude == null || endLocation.longitude == null
        ) {
            showCustomDialog(
                DialogType.WARNING,
                appContext.getString(R.string.attention),
                appContext.getString(R.string.select_start_and_end_points)
            )
            return
        }

        showCustomDialog(
            DialogType.SUCCESS,
            appContext.getString(R.string.success),
            "Now you can start the trip"
        )

    }

    private fun showCustomDialog(
        type: DialogType,
        title: String,
        message: String,
    ) {
        showDialog = true
        dialogType = type
        dialogTitle = title
        dialogMessage = message
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

