package com.mitarifamitaxi.taximetrousuario.activities

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.NoTripsView
import com.mitarifamitaxi.taximetrousuario.components.ui.TripItem
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModelFactory
import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip

class HomeActivity : BaseActivity() {

    override fun isDrawerEnabled(): Boolean = true

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(this, appViewModel)
    }

    val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                viewModel.getCurrentLocation()
            } else {
                appViewModel.showMessage(
                    type = DialogType.ERROR,
                    getString(R.string.permission_required),
                    getString(R.string.location_permission_required)
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.requestLocationPermission(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.reloadUserData()
    }

    @Composable
    override fun Content() {
        MainView(
            onTaximeterClick = {
                startActivity(Intent(this, RoutePlannerActivity::class.java))
                //startActivity(Intent(this, TaximeterActivity::class.java))
            },
            onSosClick = {
                startActivity(Intent(this, SosActivity::class.java))
            },
            onPqrsClick = {
                startActivity(Intent(this, PqrsActivity::class.java))
            },
            onMyTripsClick = {
                startActivity(Intent(this, MyTripsActivity::class.java))
            },
            onTripClicked = { trip ->
                val tripJson = Gson().toJson(trip)
                val intent = Intent(this, TripSummaryActivity::class.java)
                intent.putExtra("is_details", true)
                intent.putExtra("trip_data", tripJson)
                startActivity(intent)
            }
        )

    }

    @Composable
    private fun MainView(
        onTaximeterClick: () -> Unit,
        onSosClick: () -> Unit,
        onPqrsClick: () -> Unit,
        onMyTripsClick: () -> Unit,
        onTripClicked: (Trip) -> Unit
    ) {
        val openDrawer = LocalOpenDrawer.current
        val trips by viewModel.trips

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.white)),
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        colorResource(id = R.color.black),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )

            ) {
                Image(
                    painter = painterResource(id = R.drawable.city_background3),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))

                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                ) {

                    OutlinedButton(
                        onClick = { openDrawer() },
                        modifier = Modifier
                            .size(45.dp)
                            .border(2.dp, colorResource(id = R.color.white), CircleShape),
                        shape = CircleShape,
                        border = BorderStroke(0.dp, Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colorResource(id = R.color.main),
                            contentColor = colorResource(id = R.color.white)
                        ),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "content description"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = stringResource(id = R.string.welcome_home),
                        color = colorResource(id = R.color.white),
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )

                    Text(
                        text = appViewModel.userData?.firstName ?: "",
                        color = colorResource(id = R.color.main),
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = appViewModel.userData?.city ?: "",
                        color = colorResource(id = R.color.white),
                        fontSize = 14.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.home_taxi),
                    contentDescription = null,
                    modifier = Modifier
                        .width(220.dp)
                        .height(100.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 15.dp)
                )
            }

            if (viewModel.isGettingLocation) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        text = stringResource(id = R.string.getting_location),
                        color = colorResource(id = R.color.black),
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.black)
                    )
                }
            } else {

                Column(
                    Modifier
                        .padding(horizontal = 29.dp)
                        .padding(top = 40.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    OutlinedButton(
                        onClick = onTaximeterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        border = null,
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.home_taximetro_button),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 11.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSosClick,
                            modifier = Modifier
                                .weight(1.0f)
                                .height(140.dp),
                            border = null,
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home_sos_button),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        OutlinedButton(
                            onClick = onPqrsClick,
                            modifier = Modifier
                                .weight(1.0f)
                                .height(140.dp),
                            border = null,
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home_pqrs_button),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

                    Column {
                        Row {
                            Text(
                                text = stringResource(id = R.string.my_trips),
                                color = colorResource(id = R.color.black),
                                fontSize = 16.sp,
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 15.dp)
                            )

                            Spacer(modifier = Modifier.weight(1.0f))

                            if (trips.isNotEmpty()) {
                                TextButton(onClick = onMyTripsClick) {
                                    Text(
                                        text = stringResource(id = R.string.see_all),
                                        color = colorResource(id = R.color.main),
                                        textDecoration = TextDecoration.Underline,
                                        fontSize = 14.sp,
                                        fontFamily = MontserratFamily,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }


                        if (trips.isEmpty()) {
                            NoTripsView()
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(11.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorResource(id = R.color.white))
                                    .padding(top = 10.dp)
                                    .padding(bottom = 40.dp)
                            ) {
                                trips.forEach { trip ->
                                    TripItem(
                                        trip, onTripClicked = {
                                            onTripClicked(trip)
                                        }

                                    )
                                }
                            }
                        }

                    }

                }


            }
        }
    }
}