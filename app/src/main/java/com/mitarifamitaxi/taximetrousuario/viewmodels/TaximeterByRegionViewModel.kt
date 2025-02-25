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
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.TaximeterByRegionActivity
import com.mitarifamitaxi.taximetrousuario.activities.TripSummaryActivity
import com.mitarifamitaxi.taximetrousuario.helpers.findRegionForCoordinates
import com.mitarifamitaxi.taximetrousuario.helpers.isColombianHoliday
import com.mitarifamitaxi.taximetrousuario.helpers.isNightTime
import com.mitarifamitaxi.taximetrousuario.models.CityArea
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.PropertiesType
import com.mitarifamitaxi.taximetrousuario.models.RegionalRates
import com.mitarifamitaxi.taximetrousuario.models.Trip
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.Locale
import java.util.concurrent.Executor

class TaximeterByRegionViewModel(context: Context, private val appViewModel: AppViewModel) :
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

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val executor: Executor = ContextCompat.getMainExecutor(context)

    // Taximeter values
    var total by mutableStateOf(0.0)
    var distanceMade by mutableStateOf(0.0)

    var timeElapsed by mutableStateOf(0)
    var formattedTime by mutableStateOf("0")

    var isDoorToDoorSurcharge by mutableStateOf(false)
    var isHolidaySurcharge by mutableStateOf(false)
    var isNightSurcharge by mutableStateOf(false)

    val ratesObj = mutableStateOf(RegionalRates())

    var isTaximeterStarted by mutableStateOf(false)

    var routeCoordinates by mutableStateOf<List<LatLng>>(emptyList())
    var currentPosition by mutableStateOf(startLocation)

    var previousLocation: Location? = null

    var fitCameraPosition by mutableStateOf(false)
    var isMapLoaded by mutableStateOf(false)
    var takeMapScreenshot by mutableStateOf(false)

    private var startTime by mutableStateOf("")
    private var endTime by mutableStateOf("")

    var cityAreas: CityArea? by mutableStateOf(null)

    fun getCityRates(userCity: String?) {

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
                        val cityRatesDoc = ratesQuerySnapshot.documents[0]
                        try {
                            ratesObj.value =
                                cityRatesDoc.toObject(RegionalRates::class.java) ?: RegionalRates()
                            getEstimatedPrice()
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

    fun requestBackgroundLocationPermission(activity: TaximeterByRegionActivity) {
        activity.backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {

        val cancellationTokenSource = CancellationTokenSource()

        val task: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        task.addOnSuccessListener(executor) { location ->
            if (location != null) {

                currentPosition = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

            } else {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_fetching_location)
                )
            }
        }.addOnFailureListener {
            showCustomDialog(
                DialogType.ERROR,
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_fetching_location)
            )
        }
    }

    private fun getEstimatedPrice() {

        Log.d("TaximeterByRegionVM", "Start coordinates: ${Gson().toJson(startLocation)}")
        val startArea = findRegionForCoordinates(
            startLocation.latitude ?: 0.0,
            startLocation.longitude ?: 0.0,
            cityAreas?.features ?: emptyList()
        )
        Log.d("TaximeterByRegionVM", "Start area: $startArea")

        Log.d("TaximeterByRegionVM", "End coordinates: ${Gson().toJson(endLocation)}")
        val endArea = findRegionForCoordinates(
            endLocation.latitude ?: 0.0,
            endLocation.longitude ?: 0.0,
            cityAreas?.features ?: emptyList()
        )
        Log.d("TaximeterByRegionVM", "End area: $endArea")

        var fareStart = ""
        if (startArea?.type == PropertiesType.subdistrict) {
            fareStart = startArea.fare.toString()
        }

        var fareEnd = ""
        if (endArea?.type == PropertiesType.subdistrict) {
            fareEnd = endArea.fare.toString()
        }

        if (startArea?.type == PropertiesType.innerArea && endArea?.type == PropertiesType.innerArea) {
            // De barrio no periférco a barrio no periférico
            total = ratesObj.value.basicRace?.toDouble() ?: 0.0
        } else if (startArea?.type == PropertiesType.innerArea && endArea == null) {
            // De barrio no periférco a barrio periférico
            total = ratesObj.value.specialRace?.toDouble() ?: 0.0
        } else if (startArea == null && endArea?.type == PropertiesType.innerArea) {
            // De barrio periférico a barrio no periférico
            total = ratesObj.value.specialRace?.toDouble() ?: 0.0
        } else if (startArea == null && endArea?.type == null) {
            // De barrio periférico a barrio periférico
            total = ratesObj.value.superRace?.toDouble() ?: 0.0
        } else if (startArea?.type == PropertiesType.innerArea && endArea?.type == PropertiesType.subdistrict) {
            // De barrio no periférco a corregimiento
            total = fareEnd.toDouble()
        } else if (startArea?.type == PropertiesType.subdistrict && endArea?.type == PropertiesType.innerArea) {
            // De corregimiento a barrio no periférico
            total = fareStart.toDouble()
        }

        validateSurcharges()

    }

    private fun validateSurcharges() {

        if (isNightTime(
                ratesObj.value.nightHourSurcharge ?: 21,
                ratesObj.value.nighMinuteSurcharge ?: 0,
                ratesObj.value.morningHourSurcharge ?: 5,
                ratesObj.value.morningMinuteSurcharge ?: 30
            )
        ) {
            isNightSurcharge = true
            total += ratesObj.value.nightSurcharge?.toDouble() ?: 0.0
        }

        if (isColombianHoliday()) {
            isHolidaySurcharge = true
            total += ratesObj.value.holidaySurcharge?.toDouble() ?: 0.0
        }
    }

    fun startTaximeter() {
        isTaximeterStarted = true
        startTime = Instant.now().toString()
        startTimer()
        startWatchLocation()
    }

    fun showFinishConfirmation() {
        showCustomDialog(
            DialogType.WARNING,
            appContext.getString(R.string.finish_your_trip),
            appContext.getString(R.string.you_are_about_to_finish),
            appContext.getString(R.string.finish_trip)
        )
    }

    fun stopTaximeter() {
        isTaximeterStarted = false
        stopWatchLocation()
        endTime = Instant.now().toString()
        fitCameraPosition = true
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

    private fun stopWatchLocation() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    private fun startWatchLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->

                    currentPosition = UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )

                    if (previousLocation == null || (previousLocation?.latitude != location.latitude && previousLocation?.longitude != location.longitude)) {
                        routeCoordinates = routeCoordinates + LatLng(
                            currentPosition.latitude ?: 0.0,
                            currentPosition.longitude ?: 0.0
                        )
                    }

                    previousLocation = location

                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun mapScreenshotReady(bitmap: Bitmap, onIntentReady: (Intent) -> Unit) {

        val newWidth = bitmap.width / 1.3
        val newHeight = bitmap.height / 1.3
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val compressedBytes = outputStream.toByteArray()
        val compressedBitmap =
            BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

        val tripObj = Trip(
            startAddress = startAddress,
            startCoords = startLocation,
            endAddress = endAddress,
            endCoords = endLocation,
            startHour = startTime,
            endHour = endTime,
            total = total,
            distance = distanceMade,
            doorToDoorSurchargeEnabled = isDoorToDoorSurcharge,
            doorToDoorSurcharge = if (isDoorToDoorSurcharge) (ratesObj.value.doorToDoorSurcharge?.toDouble()) else null,
            holidaySurchargeEnabled = isHolidaySurcharge,
            holidaySurcharge = if (isHolidaySurcharge) (ratesObj.value.holidaySurcharge?.toDouble()) else null,
            nightSurchargeEnabled = isNightSurcharge,
            nightSurcharge = if (isNightSurcharge) (ratesObj.value.nightSurcharge?.toDouble()) else null,
            routeImageLocal = compressedBitmap
        )

        val tripJson = Gson().toJson(tripObj)
        val intent = Intent(appContext, TripSummaryActivity::class.java)
        intent.putExtra("trip_data", tripJson)
        onIntentReady(intent)

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

class TaximeterByRegionViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaximeterByRegionViewModel::class.java)) {
            return TaximeterByRegionViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
