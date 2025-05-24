package com.mitarifamitaxi.taximetrousuario.viewmodels.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModel

class HomeViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    private val _trips = mutableStateOf<List<Trip>>(emptyList())
    val trips: State<List<Trip>> = _trips

    init {
        getTripsByUserId()
    }

    /*private fun validateCity(city: String) {
        if (city == "Pasto") {
            getCityAreas(city)
        }
    }*/

    private fun getTripsByUserId() {
        val db = FirebaseFirestore.getInstance()
        val tripsRef = db.collection("trips")
            .whereEqualTo("userId", appViewModel.userData?.id)
            .orderBy("endHour", Query.Direction.DESCENDING)
            .limit(3)

        tripsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                appViewModel.showMessage(
                    type = DialogType.ERROR,
                    title = appContext.getString(R.string.something_went_wrong),
                    message = error.message ?: appContext.getString(R.string.error_fetching_trips)
                )
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

    /*private fun getCityAreas(city: String) {

        val database = FirebaseDatabase.getInstance()
        val citiesRef = database.getReference("cities")

        val query = citiesRef.orderByChild("city").equalTo(city)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    try {
                        val cityArea =
                            snapshot.children.firstOrNull()?.getValue(CityArea::class.java)
                        saveCityArea(cityArea ?: return)
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error parsing city data: ${e.message}")
                        appViewModel.showMessage(
                            type = DialogType.ERROR,
                            title = appContext.getString(R.string.something_went_wrong),
                            message = appContext.getString(R.string.error_fetching_regions)
                        )
                    }

                } else {
                    Log.d("HomeViewModel", "No city found with the given name")
                    appViewModel.showMessage(
                        type = DialogType.ERROR,
                        title = appContext.getString(R.string.something_went_wrong),
                        message = appContext.getString(R.string.error_fetching_regions)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Query cancelled or failed: ${error.message}")
                appViewModel.showMessage(
                    type = DialogType.ERROR,
                    title = appContext.getString(R.string.something_went_wrong),
                    message = appContext.getString(R.string.error_fetching_regions)
                )
            }
        })
    }

    private fun saveCityArea(area: CityArea) {
        val sharedPref = appContext.getSharedPreferences("CityAreaData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("CITY_AREA_OBJECT", Gson().toJson(area))
            apply()
        }
    }*/

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