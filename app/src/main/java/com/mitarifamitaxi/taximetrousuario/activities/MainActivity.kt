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

    private fun hasUserAcceptedTerms(): Boolean {
        val sharedPref = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("accepted_terms", false)
    }
}