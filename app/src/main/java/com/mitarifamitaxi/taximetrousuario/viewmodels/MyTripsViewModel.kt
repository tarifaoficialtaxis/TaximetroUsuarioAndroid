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
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip


class MyTripsViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {
    private val appContext = context.applicationContext

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")


    private val _trips = mutableStateOf<List<Trip>>(emptyList())
    val trips: State<List<Trip>> = _trips

    init {
        getTripsByUserId()
    }

    private fun getTripsByUserId() {
        appViewModel.isLoading = true
        val db = FirebaseFirestore.getInstance()
        val tripsRef = db.collection("trips")
            .whereEqualTo("userId", appViewModel.userData?.id)
            .orderBy("endHour", Query.Direction.DESCENDING)

        tripsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                appViewModel.isLoading = false
                showErrorMessage("Error listening to trips", error.message ?: "Unknown error")
                return@addSnapshotListener
            }
            try {
                appViewModel.isLoading = false
                if (snapshot != null && !snapshot.isEmpty) {
                    val trips = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Trip::class.java)?.copy(id = doc.id)
                    }
                    _trips.value = trips
                } else {
                    _trips.value = emptyList()
                }
            } catch (e: Exception) {
                appViewModel.isLoading = false
                Log.e("MyTripsViewModel", "Unexpected error: ${e.message}")
            }

        }
    }

    private fun showErrorMessage(title: String, message: String) {
        showDialog = true
        dialogType = DialogType.ERROR
        dialogTitle = title
        dialogMessage = message
    }

}

class MyTripsViewModelFactory(private val context: Context, private val appViewModel: AppViewModel) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyTripsViewModel::class.java)) {
            return MyTripsViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}