package com.mitarifamitaxi.taximetrousuario.viewmodels


import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.models.LocalUser

class HomeViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    var userData: LocalUser? by mutableStateOf(null)

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)

        userData = Gson().fromJson(userJson, LocalUser::class.java)
    }


    fun logout(onLogoutComplete: () -> Unit) {
        val sharedPref = appContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_OBJECT")
            apply()
        }
        onLogoutComplete()
    }

}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}