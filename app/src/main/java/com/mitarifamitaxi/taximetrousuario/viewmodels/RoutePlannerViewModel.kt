package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.RoutePlannerActivity
import com.mitarifamitaxi.taximetrousuario.activities.TaximeterActivity
import com.mitarifamitaxi.taximetrousuario.activities.TaximeterByRegionActivity
import com.mitarifamitaxi.taximetrousuario.helpers.fetchRoute
import com.mitarifamitaxi.taximetrousuario.helpers.getAddressFromCoordinates
import com.mitarifamitaxi.taximetrousuario.helpers.getPlaceDetails
import com.mitarifamitaxi.taximetrousuario.helpers.getPlacePredictions
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.PlacePrediction
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import java.util.concurrent.Executor

class RoutePlannerViewModel(context: Context, private val appViewModel: AppViewModel) :
    ViewModel() {

    private val appContext = context.applicationContext
    private val localConfiguration: Configuration = appContext.resources.configuration

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val executor: Executor = ContextCompat.getMainExecutor(context)

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

    var isStartAddressFocused by mutableStateOf(false)
    var isEndAddressFocused by mutableStateOf(false)

    private val _places = mutableStateOf<List<PlacePrediction>>(emptyList())
    val places: State<List<PlacePrediction>> = _places


    var routePoints by mutableStateOf<List<LatLng>>(emptyList())

    init {
        setDefaultHeights()
    }

    fun requestLocationPermission(activity: RoutePlannerActivity) {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            activity.locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {

        appViewModel.isLoading = true

        val cancellationTokenSource = CancellationTokenSource()

        val task: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        task.addOnSuccessListener(executor) { location ->
            if (location != null) {

                getAddressFromCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    callbackSuccess = { address ->
                        appViewModel.isLoading = false
                        startAddress = address
                        isSelectingStart = false
                        startLocation = UserLocation(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    },
                    callbackError = {
                        appViewModel.showMessage(
                            type = DialogType.ERROR,
                            title = appContext.getString(R.string.something_went_wrong),
                            message = appContext.getString(R.string.error_getting_address)
                        )
                    }
                )

            } else {
                appViewModel.isLoading = false
                appViewModel.showMessage(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_fetching_location)
                )
            }
        }.addOnFailureListener {
            appViewModel.isLoading = false
            appViewModel.showMessage(
                DialogType.ERROR,
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_fetching_location)
            )
        }
    }

    private fun setDefaultHeights() {
        isSheetExpanded = true
        mainColumnHeight = (localConfiguration.screenHeightDp * 0.4).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.65).dp
    }


    fun setPointOnMap() {
        isSheetExpanded = false
        mainColumnHeight = (localConfiguration.screenHeightDp * 0.65).dp
        sheetPeekHeight = (localConfiguration.screenHeightDp * 0.35).dp
    }


    fun loadAddressBasedOnCoordinates(latitude: Double, longitude: Double) {
        getAddressFromCoordinates(
            latitude = latitude,
            longitude = longitude,
            callbackSuccess = { address ->
                tempAddressOnMap = address
                tempLocationOnMap = UserLocation(latitude = latitude, longitude = longitude)
            },
            callbackError = {
                appViewModel.showMessage(
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
            startAddress = tempAddressOnMap
            startLocation = tempLocationOnMap
        } else {
            endAddress = tempAddressOnMap
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
                appViewModel.showMessage(
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
            country = appViewModel.userData?.countryCode ?: "CO",
            callbackSuccess = { predictions ->
                _places.value = predictions
            },
            callbackError = {
                appViewModel.showMessage(
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

                    placePrediction.description?.let { description ->
                        if (isSelectingStart) {
                            startAddress = description
                            startLocation = location
                        } else {
                            endAddress = description
                            endLocation = location
                        }
                    }

                },
                callbackError = {
                    appViewModel.showMessage(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.error_getting_address)
                    )
                }
            )
        }


    }

    fun validateStartTrip(onIntentReady: (Intent) -> Unit) {
        if (startAddress.isEmpty() || startLocation.latitude == null || startLocation.longitude == null
            || endAddress.isEmpty() || endLocation.latitude == null || endLocation.longitude == null
        ) {
            appViewModel.showMessage(
                DialogType.WARNING,
                appContext.getString(R.string.attention),
                appContext.getString(R.string.select_start_and_end_points)
            )
            return
        }

        if (appViewModel.userData?.city == "Pasto") {
            val intent = Intent(appContext, TaximeterByRegionActivity::class.java)
            intent.putExtra("start_address", startAddress)
            intent.putExtra("start_location", Gson().toJson(startLocation))
            intent.putExtra("end_address", endAddress)
            intent.putExtra("end_location", Gson().toJson(endLocation))
            onIntentReady(intent)
        } else {
            val intent = Intent(appContext, TaximeterActivity::class.java)
            intent.putExtra("start_address", startAddress)
            intent.putExtra("start_location", Gson().toJson(startLocation))
            intent.putExtra("end_address", endAddress)
            intent.putExtra("end_location", Gson().toJson(endLocation))
            onIntentReady(intent)
        }

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

