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
import com.mitarifamitaxi.taximetrousuario.activities.TaximeterActivity
import com.mitarifamitaxi.taximetrousuario.activities.TripSummaryActivity
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Rates
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
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.DayOfWeek
import java.time.LocalDate

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

        isHolidaySurcharge = checkHolidayDate()

        Log.d("TaximeterViewModel", checkHolidayDate().toString())
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
            holidaySurchargeEnabled = isHolidaySurcharge,
            holidaySurcharge = if (isHolidaySurcharge) (ratesObj.value.holidayRateUnits
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

    private fun getFinalUnits(): Int {
        val minimumRateUnits = ratesObj.value.minimumRateUnits ?: 0.0
        return if (units < minimumRateUnits) {
            minimumRateUnits.toInt()
        } else {
            units.toInt()
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

    private fun getColombianHolidays(year: Int): List<LocalDate> {
        // Fixed-date holidays
        val fixedHolidays = listOf(
            LocalDate.of(year, 1, 1),   // Año Nuevo
            LocalDate.of(year, 5, 1),   // Día del Trabajo
            LocalDate.of(year, 7, 20),  // Día de la Independencia
            LocalDate.of(year, 8, 7),   // Batalla de Boyacá
            LocalDate.of(year, 12, 8),  // Inmaculada Concepción
            LocalDate.of(year, 12, 25)  // Navidad
        )

        // Movable holidays (Ley Emiliani)
        val movableHolidays = listOf(
            getNextMonday(LocalDate.of(year, 1, 6)),   // Reyes Magos
            getNextMonday(LocalDate.of(year, 3, 19)),  // San José
            getNextMonday(LocalDate.of(year, 6, 29)),  // San Pedro y San Pablo
            getNextMonday(LocalDate.of(year, 8, 15)),  // Asunción de la Virgen
            getNextMonday(LocalDate.of(year, 10, 12)), // Día de la Raza
            getNextMonday(LocalDate.of(year, 11, 1)),  // Todos los Santos
            getNextMonday(LocalDate.of(year, 11, 11))  // Independencia de Cartagena
        )

        // Easter-based holidays
        val easterSunday = getEasterSunday(year)
        val easterRelatedHolidays = listOf(
            easterSunday.minusDays(3),                  // Jueves Santo
            easterSunday.minusDays(2),                  // Viernes Santo
            getNextMonday(easterSunday.plusDays(43)),   // Ascensión del Señor
            getNextMonday(easterSunday.plusDays(64)),   // Corpus Christi
            getNextMonday(easterSunday.plusDays(71))    // Sagrado Corazón
        )

        return fixedHolidays + movableHolidays + easterRelatedHolidays
    }

    // Check if a date is a Colombian holiday (for current and next year)
    private fun isColombianHoliday(date: LocalDate): Boolean {

        /*
        * if date is sunday return true
        * */
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY

        if (isSunday) {
            return true
        }

        val currentYearHolidays = getColombianHolidays(date.year)
        val nextYearHolidays =
            getColombianHolidays(date.year + 1) // In case we're checking near end of year
        return date in currentYearHolidays || date in nextYearHolidays
    }

    // Move holidays to next Monday if not already on Monday
    private fun getNextMonday(date: LocalDate): LocalDate {
        return if (date.dayOfWeek == DayOfWeek.MONDAY) date
        else date.plusDays((DayOfWeek.MONDAY.value - date.dayOfWeek.value + 7) % 7L)
    }

    // Compute Easter Sunday
    private fun getEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    private fun checkHolidayDate(): Boolean {

        val now = Date()

        /*val now: Date = Date.from(
            LocalDate.of(2025, 5, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )*/

        val todayDateTime = LocalDateTime.now()
        val today: LocalDate = now.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // 8 PM today
        val ninePM = todayDateTime.withHour(20).withMinute(0).withSecond(0).withNano(0)

        // 5 AM tomorrow
        val tomorrow = todayDateTime.plusDays(1).withHour(5).withMinute(0).withSecond(0).withNano(0)


        // Convierte LocalDateTime a Date
        val ninePMDate = Date.from(ninePM.atZone(ZoneId.systemDefault()).toInstant())
        val fiveAMDate = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant())

        val isLateHours = now.after(ninePMDate) && now.before(fiveAMDate)

        if (isLateHours) {
            return true
        } else {
            return isColombianHoliday(today)
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
