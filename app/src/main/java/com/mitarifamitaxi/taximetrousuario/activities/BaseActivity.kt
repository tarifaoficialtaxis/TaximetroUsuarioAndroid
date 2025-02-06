package com.mitarifamitaxi.taximetrousuario.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModelFactory


open class BaseActivity : ComponentActivity() {

    val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory(this)
    }

    // Snackbar host state
    private val snackbarHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MyTheme {
                BaseScreen()
            }
        }
    }

    @Composable
    fun BaseScreen() {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Content()

                // Loading overlay
                if (appViewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.main)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MyTheme(content: @Composable () -> Unit) {
        val customColorScheme = lightColorScheme(
            primary = colorResource(id = R.color.main),
            onPrimary = Color.White,
            secondary = colorResource(id = R.color.yellow1)
        )

        MaterialTheme(
            colorScheme = customColorScheme,
            content = content
        )
    }

    @Composable
    open fun Content() {
    }

    // Function to show a snackbar with action
    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel
        )
        if (result == SnackbarResult.ActionPerformed) {
            onAction?.invoke()
        }
    }
}
