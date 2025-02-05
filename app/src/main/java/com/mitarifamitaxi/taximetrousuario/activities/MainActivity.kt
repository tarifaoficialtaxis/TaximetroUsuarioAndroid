package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        installSplashScreen()

        val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)

        if (userJson != null) {

            startActivity(
                Intent(this, HomeActivity::class.java)
            )
            finish()
            //val user = Gson().fromJson(userJson, User::class.java)

        } else {
            if (hasUserAcceptedTerms()) {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
                finish()
            } else {
                startActivity(
                    Intent(this, TermsConditionsActivity::class.java)
                )
            }
        }


    }


    private fun hasUserAcceptedTerms(): Boolean {
        val sharedPref = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("accepted_terms", false)
    }
}