package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CompleteProfileViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var mobilePhone by mutableStateOf("")
    var email by mutableStateOf("")


    fun completeProfile() {
        // completeProfile logic
    }

}

class CompleteProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompleteProfileViewModel::class.java)) {
            return CompleteProfileViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}