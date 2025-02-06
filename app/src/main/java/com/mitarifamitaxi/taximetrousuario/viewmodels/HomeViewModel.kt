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

class HomeViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext
    var userData: LocalUser? by mutableStateOf(null)

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")


    private val _trips = mutableStateOf<List<Trip>>(emptyList())
    val trips: State<List<Trip>> = _trips

    init {
        loadUserData()

        getTripsByUserId()
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

            /*if (snapshot != null && !snapshot.isEmpty) {
                val trips = snapshot.documents
                val sortedTrips = trips.sortedByDescending { it.getDate("endHour")?.time ?: 0L }



            } else {
                // Handle empty trips list
            }*/
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

    private fun showErrorMessage(title: String, message: String) {
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