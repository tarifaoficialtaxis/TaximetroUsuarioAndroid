package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.DrawerContent
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch


val LocalOpenDrawer = compositionLocalOf<() -> Unit> {
    error("LocalOpenDrawer not provided")
}

open class BaseActivity : ComponentActivity() {

    val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory(this)
    }


    open fun isDrawerEnabled(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                BaseScreen(
                    onMenuSectionClicked = { sectionId ->
                        when (sectionId) {
                            "PROFILE" -> {
                                startActivity(Intent(this, ProfileActivity::class.java))
                            }

                            "HOME" -> {
                                startActivity(Intent(this, HomeActivity::class.java))
                            }

                            "TAXIMETER" -> {
                                startActivity(Intent(this, TaximeterActivity::class.java))
                            }

                            "SOS" -> {
                                startActivity(Intent(this, SosActivity::class.java))
                            }

                            "PQRS" -> {
                                startActivity(Intent(this, PqrsActivity::class.java))
                            }

                            "MY_TRIPS" -> {
                                startActivity(Intent(this, MyTripsActivity::class.java))
                            }
                        }
                    }
                )
            }
        }
    }


    @Composable
    fun BaseScreen(onMenuSectionClicked: (String) -> Unit) {
        if (isDrawerEnabled()) {
            // Remember the drawer state and provide a lambda to open the drawer.
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val openDrawer: () -> Unit = {
                scope.launch { drawerState.open() }
                Unit
            }

            // Provide the openDrawer lambda via a CompositionLocal.
            CompositionLocalProvider(LocalOpenDrawer provides openDrawer) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        appViewModel.userData?.let {
                            DrawerContent(
                                userData = it,
                                onProfileClicked = { onMenuSectionClicked("PROFILE") },
                                onSectionClicked = { onMenuSectionClicked(it.id) }
                            )
                        }
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Content()

                        // Loading overlay (if needed)
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
        } else {
            // When the drawer is disabled, provide a no-op openDrawer lambda.
            CompositionLocalProvider(LocalOpenDrawer provides {}) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Content()

                    // Loading overlay (if needed)
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
    }

    /**
     * MyTheme sets up a custom MaterialTheme.
     */
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

    /**
     * Content is the main screen content that child activities override.
     */
    @Composable
    open fun Content() {
        // Default empty content; override in child activities.
    }
}
