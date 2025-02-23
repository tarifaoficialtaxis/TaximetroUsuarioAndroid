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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.HomeActivity
import com.mitarifamitaxi.taximetrousuario.helpers.getCityFromCoordinates
import com.mitarifamitaxi.taximetrousuario.models.CityArea
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import java.util.concurrent.Executor

class HomeViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

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
        getTripsByUserId()
    }

    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
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
                        validateCity(city ?: "")
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
                        Log.e("HomeViewModel", "Error getting city: $error")
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
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateUserData(location: UserLocation, city: String, countryCodeWhatsapp: String) {
        appViewModel.userData = appViewModel.userData?.copy(
            location = location,
            city = city,
            countryCodeWhatsapp = countryCodeWhatsapp
        )

        appViewModel.userData?.let { saveUserState(it) }

    }

    private fun validateCity(city: String) {
        if (city == "Pasto") {
            getCityAreas(city)
        }
    }

    private fun saveUserState(user: LocalUser) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_OBJECT", Gson().toJson(user))
            apply()
        }
    }

    private fun getTripsByUserId() {
        val db = FirebaseFirestore.getInstance()
        val tripsRef = db.collection("trips")
            .whereEqualTo("userId", appViewModel.userData?.id)
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


    fun showErrorMessage(title: String, message: String) {
        showDialog = true
        dialogType = DialogType.ERROR
        dialogTitle = title
        dialogMessage = message
    }

    private fun getCityAreas(city: String) {

        val database = FirebaseDatabase.getInstance()
        val citiesRef = database.getReference("cities")

        val query = citiesRef.orderByChild("city").equalTo(city)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    try {
                        val cityArea = snapshot.children.firstOrNull()?.getValue(CityArea::class.java)
                        Log.d("Firebase", "Found city: $cityArea")
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error parsing city data: ${e.message}")
                    }

                } else {
                    Log.d("Firebase", "No city found with the given name")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Query cancelled or failed: ${error.message}")
            }
        })
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