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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.TaximeterActivity
import com.mitarifamitaxi.taximetrousuario.activities.TripSummaryActivity
import com.mitarifamitaxi.taximetrousuario.helpers.isColombianHoliday
import com.mitarifamitaxi.taximetrousuario.helpers.isNightTime
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Rates
import com.mitarifamitaxi.taximetrousuario.models.Trip
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import com.mitarifamitaxi.taximetrousuario.services.LocationUpdatesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.Locale
import java.util.concurrent.Executor

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

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val executor: Executor = ContextCompat.getMainExecutor(context)

    // Taximeter values
    var total by mutableStateOf(0.0)
    var distanceMade by mutableStateOf(0.0)

    private val _units = mutableStateOf(0.0)

    var units: Double
        get() = _units.value
        set(value) {
            _units.value = value
            onUnitsChanged(value)
        }

    var timeElapsed by mutableStateOf(0)
    var dragTimeElapsed by mutableStateOf(0)
    var formattedTime by mutableStateOf("0")

    var isAirportSurcharge by mutableStateOf(false)
    var isHolidaySurcharge by mutableStateOf(false)
    var isDoorToDoorSurcharge by mutableStateOf(false)

    val ratesObj = mutableStateOf(Rates())

    var isTaximeterStarted by mutableStateOf(false)
    var isMooving by mutableStateOf(false)

    var routeCoordinates by mutableStateOf<List<LatLng>>(emptyList())
    var currentPosition by mutableStateOf(startLocation)

    var previousLocation: Location? = null

    var fitCameraPosition by mutableStateOf(false)
    var isMapLoaded by mutableStateOf(false)
    var takeMapScreenshot by mutableStateOf(false)

    private var startTime by mutableStateOf("")
    private var endTime by mutableStateOf("")

    init {
        getCityRates(appViewModel.userData?.city)
    }

    fun requestBackgroundLocationPermission(activity: TaximeterActivity) {
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
                FirebaseCrashlytics.getInstance().recordException(Exception("TaximeterViewModel location null"))
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_fetching_location)
                )
            }
        }.addOnFailureListener {
            FirebaseCrashlytics.getInstance().recordException(it)
            showCustomDialog(
                DialogType.ERROR,
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_fetching_location)
            )
        }
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
                        val cityRatesDoc = ratesQuerySnapshot.documents[0]
                        try {
                            ratesObj.value =
                                cityRatesDoc.toObject(Rates::class.java) ?: Rates()
                            validateSurcharges()
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                            showCustomDialog(
                                DialogType.ERROR,
                                appContext.getString(R.string.something_went_wrong),
                                appContext.getString(R.string.general_error)
                            )
                        }
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(Exception("TaximeterViewModel ratesQuerySnapshot empty"))
                        showCustomDialog(
                            DialogType.ERROR,
                            appContext.getString(R.string.something_went_wrong),
                            appContext.getString(R.string.general_error)
                        )
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showCustomDialog(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.general_error)
                    )
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(Exception("TaximeterViewModel userCity null"))
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_no_city_set)
                )
            }
        }

    }

    private fun validateSurcharges() {

        if (isNightTime(
                ratesObj.value.nightHourSurcharge ?: 21,
                ratesObj.value.nighMinuteSurcharge ?: 0,
                ratesObj.value.morningHourSurcharge ?: 5,
                ratesObj.value.morningMinuteSurcharge ?: 30
            )
        ) {
            isHolidaySurcharge = true
            units += ratesObj.value.holidayRateUnits ?: 0.0
            return
        }

        if (isColombianHoliday()) {
            isHolidaySurcharge = true
            units += ratesObj.value.holidayRateUnits ?: 0.0
        }
    }

    private fun onUnitsChanged(newValue: Double) {
        total = newValue * (ratesObj.value.unitPrice ?: 0.0)
    }

    fun startTaximeter() {
        isTaximeterStarted = true
        units = ratesObj.value.startRateUnits ?: 0.0
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

                //Log.d("TaximeterViewModel", "isMooving in timer: $isMooving")
                if (!isMooving && isTaximeterStarted) {

                    dragTimeElapsed++
                    val sumDrag = (dragTimeElapsed * (ratesObj.value.unitsPerHour ?: 0.0)) / 3600
                    if (sumDrag >= 1) {
                        units += 1
                        dragTimeElapsed = 0
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

                    val speedMetersPerSecond = location.speed
                    val speedKmPerHour = speedMetersPerSecond * 3.6
                    //Log.d("TaximeterViewModel", "Speed: $speedKmPerHour")
                    if (speedKmPerHour > (ratesObj.value.dragSpeed ?: 0.0)) {
                        isMooving = true

                        val distanceCovered: Float = previousLocation?.distanceTo(location) ?: 0f
                        distanceMade += distanceCovered.toDouble()

                        val additionalUnits = distanceCovered / (ratesObj.value.meters ?: 0)
                        units += additionalUnits

                        Log.d("TaximeterViewModel", "onLocationResult New units: $units")


                    } else {
                        isMooving = false
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

    /*fun onLocationUpdate(location: Location) {
        currentPosition = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )

        if (previousLocation == null ||
            (previousLocation?.latitude != location.latitude || previousLocation?.longitude != location.longitude)
        ) {
            routeCoordinates = routeCoordinates + LatLng(location.latitude, location.longitude)
        }

        val speedMetersPerSecond = location.speed
        val speedKmPerHour = speedMetersPerSecond * 3.6
        if (speedKmPerHour > (ratesObj.value.dragSpeed ?: 0.0)) {
            isMooving = true
            val distanceCovered: Float = previousLocation?.distanceTo(location) ?: 0f
            distanceMade += distanceCovered.toDouble()
            val additionalUnits = distanceCovered / (ratesObj.value.meters ?: 0)
            units += additionalUnits
            Log.d("TaximeterViewModel", "onLocationUpdate New units: $units")
        } else {
            isMooving = false
        }
        previousLocation = location
    }*/

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

        val finalUnits = getFinalUnits()

        val tripObj = Trip(
            startAddress = startAddress,
            startCoords = startLocation,
            endAddress = endAddress,
            endCoords = endLocation,
            startHour = startTime,
            endHour = endTime,
            units = finalUnits,
            total = finalUnits * (ratesObj.value.unitPrice ?: 0.0),
            distance = distanceMade,
            airportSurchargeEnabled = isAirportSurcharge,
            airportSurcharge = if (isAirportSurcharge) (ratesObj.value.airportRateUnits
                ?: 0.0) * (ratesObj.value.unitPrice ?: 0.0) else null,
            holidayOrNightSurchargeEnabled = isHolidaySurcharge,
            holidayOrNightSurcharge = if (isHolidaySurcharge) (ratesObj.value.holidayRateUnits
                ?: 0.0) * (ratesObj.value.unitPrice ?: 0.0) else null,
            doorToDoorSurchargeEnabled = isDoorToDoorSurcharge,
            doorToDoorSurcharge = if (isDoorToDoorSurcharge) (ratesObj.value.doorToDoorRateUnits
                ?: 0.0) * (ratesObj.value.unitPrice ?: 0.0) else null,
            routeImageLocal = compressedBitmap
        )

        val tripJson = Gson().toJson(tripObj)
        val intent = Intent(appContext, TripSummaryActivity::class.java)
        intent.putExtra("trip_data", tripJson)
        onIntentReady(intent)

    }

    private fun getFinalUnits(): Double {
        val minimumRateUnits = ratesObj.value.minimumRateUnits ?: 0.0
        return if (units < minimumRateUnits) {
            minimumRateUnits
        } else {
            units
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
