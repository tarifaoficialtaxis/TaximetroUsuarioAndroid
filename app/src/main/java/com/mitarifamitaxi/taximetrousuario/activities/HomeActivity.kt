package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModelFactory
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModelFactory

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainView()
        }
    }

    @Composable
    private fun MainView(

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                viewModel.logout(
                    onLogoutComplete = {
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }) {
                Text("Logout")
            }
        }
    }
}