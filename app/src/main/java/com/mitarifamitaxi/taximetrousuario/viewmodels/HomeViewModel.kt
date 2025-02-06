package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.LocalUser
import com.mitarifamitaxi.taximetrousuario.models.Trip

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.*

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.HomeActivity
import com.mitarifamitaxi.taximetrousuario.helpers.getCityFromCoordinates
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import java.util.concurrent.Executor

class HomeViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext
    var userData: LocalUser? by mutableStateOf(null)

    private lateinit var locationCallback: LocationCallback

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val executor: Executor = ContextCompat.getMainExecutor(context)

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")

    var isGettingLocation by mutableStateOf(false)

    private val _trips = mutableStateOf<List<Trip>>(emptyList())
    val trips: State<List<Trip>> = _trips

    init {
        loadUserData()
        getTripsByUserId()
    }


    fun requestLocationPermission(activity: HomeActivity) {
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

        isGettingLocation = true
        val cancellationTokenSource = CancellationTokenSource()

        val task: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        task.addOnSuccessListener(executor) { location ->
            if (location != null) {

                getCityFromCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    callbackSuccess = { city, countryCodeWhatsapp ->
                        isGettingLocation = false
                        updateUserData(
                            location = UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude
                            ),
                            city = city ?: "",
                            countryCodeWhatsapp = countryCodeWhatsapp ?: ""
                        )
                    },
                    callbackError = { error ->
                        isGettingLocation = false
                        showErrorMessage(
                            appContext.getString(R.string.something_went_wrong),
                            appContext.getString(R.string.error_fetching_city)
                        )
                    }
                )

            } else {
                isGettingLocation = false
                showErrorMessage(
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_fetching_location)
                )
            }
        }.addOnFailureListener {
            isGettingLocation = false
            showErrorMessage(
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.error_fetching_location)
            )
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateUserData(location: UserLocation, city: String, countryCodeWhatsapp: String) {
        userData = userData?.copy(
            location = location,
            city = city,
            countryCodeWhatsapp = countryCodeWhatsapp
        )

        userData?.let { saveUserState(it) }

    }

    private fun saveUserState(user: LocalUser) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_OBJECT", Gson().toJson(user))
            apply()
        }
    }

    private fun loadUserData() {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)

        userData = Gson().fromJson(userJson, LocalUser::class.java)
    }


    private fun getTripsByUserId() {
        val db = FirebaseFirestore.getInstance()
        val tripsRef = db.collection("trips")
            .whereEqualTo("userId", userData?.id)
            .orderBy("endHour", Query.Direction.DESCENDING)
            .limit(3)

        tripsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                showErrorMessage("Error listening to trips", error.message ?: "Unknown error")
                return@addSnapshotListener
            }

            try {

                if (snapshot != null && !snapshot.isEmpty) {
                    val trips = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Trip::class.java)?.copy(id = doc.id)
                    }
                    _trips.value = trips
                } else {
                    _trips.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Unexpected error: ${e.message}")
            }

        }
    }


    fun logout(onLogoutComplete: () -> Unit) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_OBJECT")
            apply()
        }
        onLogoutComplete()
    }

    fun showErrorMessage(title: String, message: String) {
        showDialog = true
        dialogType = DialogType.ERROR
        dialogTitle = title
        dialogMessage = message
    }

}

class HomeViewModelFactory(private val context: Context, private val appViewModel: AppViewModel) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}