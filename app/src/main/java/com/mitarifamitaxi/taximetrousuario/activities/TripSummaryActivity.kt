package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextFieldDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.components.ui.TripInfoRow
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.helpers.formatDigits
import com.mitarifamitaxi.taximetrousuario.helpers.formatNumberWithDots
import com.mitarifamitaxi.taximetrousuario.helpers.getShortAddress
import com.mitarifamitaxi.taximetrousuario.helpers.hourFormatDate
import com.mitarifamitaxi.taximetrousuario.helpers.tripSummaryFormatDate
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

        val isDetails = intent.getBooleanExtra("is_details", false)
        viewModel.isDetails = isDetails
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
            },
            onSosAction = {
                startActivity(Intent(this, SosActivity::class.java))
            },
            onShareAction = {
                viewModel.showShareDialog = true
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

        if (viewModel.showShareDialog) {
            CustomTextFieldDialog(
                title = getString(R.string.share_trip),
                message = getString(R.string.share_trip_message),
                textButton = getString(R.string.send),
                textFieldValue = viewModel.shareNumber,
                isTextFieldError = viewModel.isShareNumberError,
                onDismiss = { viewModel.showShareDialog = false },
                onButtonClicked = {
                    viewModel.sendWatsAppMessage(
                        onIntentReady = { intent ->
                            startActivity(intent)
                        }
                    )
                }
            )
        }

    }

    @Composable
    private fun MainView(
        onDeleteAction: () -> Unit,
        onSosAction: () -> Unit,
        onShareAction: () -> Unit
    ) {

        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.gray4))
                .fillMaxSize()
        ) {
            TopHeaderView(
                title = stringResource(id = R.string.trip_summary),
                leadingIcon = Icons.Filled.ChevronLeft,
                onClickLeading = {
                    finish()
                },
                trailingIcon = if (viewModel.isDetails) Icons.Filled.Delete else null,
                onClickTrailing = onDeleteAction
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (viewModel.isDetails) {
                    AsyncImage(
                        model = viewModel.tripData.routeImage,
                        contentDescription = "Trip route map image",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(250.dp)
                            .fillMaxWidth()
                    )
                } else {
                    viewModel.tripData.routeImageLocal?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(250.dp)
                                .fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 29.dp)
                        .padding(horizontal = 29.dp)
                ) {
                    Text(
                        text = tripSummaryFormatDate(viewModel.tripData.startHour ?: ""),
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.blue1),
                    )

                    Text(
                        text = "$ ${viewModel.tripData.total?.toInt()?.formatNumberWithDots()} COP",
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = colorResource(id = R.color.main),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {

                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .border(2.dp, colorResource(id = R.color.yellow2), CircleShape)
                                .background(colorResource(id = R.color.main), shape = CircleShape),
                        )

                        Text(
                            text = viewModel.tripData.startAddress ?: "",
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.black),
                            modifier = Modifier.weight(0.8f)
                        )

                        Spacer(modifier = Modifier.weight(0.2f))

                        Text(
                            text = hourFormatDate(viewModel.tripData.startHour ?: ""),
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.gray1),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(bottom = 10.dp),
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colorResource(id = R.color.main),
                            modifier = Modifier.size(15.dp)
                        )

                        Text(
                            text = viewModel.tripData.endAddress?.getShortAddress() ?: "",
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.black),
                            modifier = Modifier.weight(0.8f)
                        )

                        Spacer(modifier = Modifier.weight(0.2f))

                        Text(
                            text = hourFormatDate(viewModel.tripData.endHour ?: ""),
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.gray1),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(colorResource(id = R.color.gray2))
                    )

                    TripInfoRow(
                        title = stringResource(id = R.string.distance_made),
                        value = "${((viewModel.tripData.distance ?: 0.0) / 1000).formatDigits(1)} KM"
                    )

                    TripInfoRow(
                        title = stringResource(id = R.string.units),
                        value = viewModel.tripData.units.toString()
                    )

                    if (viewModel.tripData.airportSurchargeEnabled == false &&
                        viewModel.tripData.holidaySurchargeEnabled == false &&
                        viewModel.tripData.doorToDoorSurchargeEnabled == false
                    ) {
                        TripInfoRow(
                            title = stringResource(id = R.string.recharges),
                            value = stringResource(id = R.string.without_recharges)
                        )
                    }

                    if (viewModel.tripData.airportSurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.airport_surcharge),
                            value = "$ ${viewModel.tripData.airportSurcharge?.formatNumberWithDots()} COP"
                        )
                    }

                    if (viewModel.tripData.holidaySurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.holiday_surcharge),
                            value = "$ ${viewModel.tripData.holidaySurcharge?.formatNumberWithDots()} COP"
                        )
                    }

                    if (viewModel.tripData.doorToDoorSurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.airport_surcharge),
                            value = "$ ${viewModel.tripData.doorToDoorSurcharge?.formatNumberWithDots()} COP"
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 20.dp)
                    ) {
                        CustomButton(
                            text = stringResource(id = R.string.sos).uppercase(),
                            onClick = onSosAction,
                            color = colorResource(id = R.color.red1),
                            leadingIcon = Icons.Rounded.WarningAmber
                        )

                        CustomButton(
                            text = stringResource(id = R.string.share).uppercase(),
                            onClick = onShareAction,
                            leadingIcon = Icons.Rounded.Share
                        )

                        if (!viewModel.isDetails) {
                            CustomButton(
                                text = stringResource(id = R.string.finish).uppercase(),
                                onClick = onDeleteAction,
                                color = colorResource(id = R.color.gray1),
                                leadingIcon = Icons.Default.Close
                            )
                        }
                    }


                }
            }

        }

    }
}