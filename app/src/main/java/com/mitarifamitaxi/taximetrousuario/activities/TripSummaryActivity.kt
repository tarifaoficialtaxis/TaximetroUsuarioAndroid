package com.mitarifamitaxi.taximetrousuario.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.Trip
import com.mitarifamitaxi.taximetrousuario.viewmodels.TripSummaryViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.TripSummaryViewModelFactory

class TripSummaryActivity : BaseActivity() {

    private val viewModel: TripSummaryViewModel by viewModels {
        TripSummaryViewModelFactory(this, appViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tripJson = intent.getStringExtra("trip_data")
        tripJson?.let {
            viewModel.tripData = Gson().fromJson(it, Trip::class.java)
        }
    }

    @Composable
    override fun Content() {
        MainView(
            onDeleteAction = {
                viewModel.showCustomDialog(
                    DialogType.WARNING,
                    getString(R.string.delete_trip),
                    getString(R.string.delete_trip_message),
                    getString(R.string.delete)
                )
            }
        )

        if (viewModel.showDialog) {
            CustomPopupDialog(
                dialogType = viewModel.dialogType,
                title = viewModel.dialogTitle,
                message = viewModel.dialogMessage,
                showCloseButton = viewModel.dialogShowCloseButton,
                primaryActionButton = viewModel.dialogPrimaryAction,
                onDismiss = { viewModel.showDialog = false },
                onPrimaryActionClicked = {
                    viewModel.showDialog = false
                    if (viewModel.dialogPrimaryAction == getString(R.string.delete)) {
                        viewModel.tripData.id?.let { viewModel.deleteTrip(it) }
                    } else {
                        finish()
                    }
                }
            )
        }

    }

    @Composable
    private fun MainView(onDeleteAction: () -> Unit) {

        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.gray4))
                .fillMaxSize(),
        ) {
            TopHeaderView(
                title = stringResource(id = R.string.trip_summary),
                leadingIcon = Icons.Filled.ChevronLeft,
                onClickLeading = {
                    finish()
                },
                trailingIcon = Icons.Filled.Delete,
                onClickTrailing = onDeleteAction
            )

            AsyncImage(
                model = viewModel.tripData.routeImage,
                contentDescription = "Trip route map image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            )

        }

    }
}