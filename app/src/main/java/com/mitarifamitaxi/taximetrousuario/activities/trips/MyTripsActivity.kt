package com.mitarifamitaxi.taximetrousuario.activities.trips

import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.BaseActivity
import com.mitarifamitaxi.taximetrousuario.components.ui.NoTripsView
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.components.ui.TripItem
import com.mitarifamitaxi.taximetrousuario.models.Trip
import com.mitarifamitaxi.taximetrousuario.viewmodels.trips.MyTripsViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.trips.MyTripsViewModelFactory

class MyTripsActivity : BaseActivity() {

    override fun isDrawerEnabled(): Boolean = true

    private val viewModel: MyTripsViewModel by viewModels {
        MyTripsViewModelFactory(this, appViewModel)
    }

    @Composable
    override fun Content() {
        MainView(
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
    private fun MainView(onTripClicked: (Trip) -> Unit) {

        val trips by viewModel.trips

        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.gray4))
                .fillMaxSize(),
        ) {
            TopHeaderView(
                title = stringResource(id = R.string.my_trips),
                leadingIcon = Icons.Filled.ChevronLeft,
                onClickLeading = {
                    finish()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 29.dp)
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 29.dp)
            ) {
                if (trips.isEmpty()) {
                    NoTripsView()
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        trips.forEach { trip ->
                            TripItem(
                                trip,
                                onTripClicked = { onTripClicked(trip) }
                            )
                        }
                    }
                }
            }


        }
    }
}