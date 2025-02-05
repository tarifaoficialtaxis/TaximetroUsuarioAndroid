package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoginViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    // Example username/password states
    var userName by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)



    fun login() {
        // Do your normal email/password login...
    }

}

/**
 * A simple factory to provide the LoginViewModel with a context.
 */
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
