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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.mitarifamitaxi.taximetrousuario.viewmodels.ForgotPasswordViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.TripSummaryViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.TripSummaryViewModelFactory
import kotlinx.coroutines.launch

class TripSummaryActivity : BaseActivity() {

    private val viewModel: TripSummaryViewModel by viewModels {
        TripSummaryViewModelFactory(this, appViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModelEvents()

        val isDetails = intent.getBooleanExtra("is_details", false)
        viewModel.isDetails = isDetails
        val tripJson = intent.getStringExtra("trip_data")
        tripJson?.let {
            viewModel.tripData = Gson().fromJson(it, Trip::class.java)
        }
    }


    private fun observeViewModelEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvents.collect { event ->
                    when (event) {
                        is TripSummaryViewModel.NavigationEvent.GoBack -> {
                            finish()
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Content() {
        MainView(
            onDeleteAction = {
                viewModel.onDeleteAction()
            },
            onSosAction = {
                startActivity(Intent(this, SosActivity::class.java))
            },
            onShareAction = {
                viewModel.showShareDialog = true
            },
            onFinishAction = {
                viewModel.saveTripData() { intent ->
                    startActivity(intent)
                }
            }
        )

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
        onShareAction: () -> Unit,
        onFinishAction: () -> Unit
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
                    if (viewModel.isDetails) {
                        finish()
                    } else {
                        onFinishAction()
                    }
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
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    viewModel.tripData.routeImageLocal?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
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

                    Row {
                        Column {
                            Text(
                                text = tripSummaryFormatDate(viewModel.tripData.startHour ?: ""),
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
                                color = colorResource(id = R.color.blue1),
                            )

                            Text(
                                text = "$ ${
                                    viewModel.tripData.total?.toInt()?.formatNumberWithDots()
                                } COP",
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = colorResource(id = R.color.main),
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (viewModel.tripData.companyImage != null) {
                            AsyncImage(
                                model = viewModel.tripData.companyImage,
                                contentDescription = "Company logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                            )
                        }
                    }



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

                    if (viewModel.tripData.baseUnits != null) {
                        TripInfoRow(
                            title = stringResource(id = R.string.units_base),
                            value = viewModel.tripData.baseUnits?.toInt().toString()
                        )
                    }

                    TripInfoRow(
                        title = stringResource(id = R.string.fare_base),
                        value = "$${
                            viewModel.tripData.baseRate?.toInt()?.formatNumberWithDots()
                        } COP"
                    )

                    if (viewModel.tripData.rechargeUnits != null) {
                        TripInfoRow(
                            title = stringResource(id = R.string.units_recharge),
                            value = viewModel.tripData.rechargeUnits?.toInt().toString()
                        )
                    }

                    if (viewModel.tripData.airportSurchargeEnabled == false &&
                        viewModel.tripData.nightSurchargeEnabled == false &&
                        viewModel.tripData.holidayOrNightSurchargeEnabled == false &&
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
                            value = "+$${
                                viewModel.tripData.airportSurcharge?.toInt()?.formatNumberWithDots()
                            } COP"
                        )
                    }

                    if (viewModel.tripData.doorToDoorSurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.door_to_door_surcharge),
                            value = "+$${
                                viewModel.tripData.doorToDoorSurcharge?.toInt()
                                    ?.formatNumberWithDots()
                            } COP"
                        )
                    }

                    if (viewModel.tripData.nightSurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.night_surcharge_only),
                            value = "+$${
                                viewModel.tripData.nightSurcharge?.toInt()
                                    ?.formatNumberWithDots()
                            } COP"
                        )
                    }

                    if (viewModel.tripData.holidaySurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.holiday_surcharge_only),
                            value = "+$${
                                viewModel.tripData.holidaySurcharge?.toInt()?.formatNumberWithDots()
                            } COP"
                        )
                    }

                    if (viewModel.tripData.holidayOrNightSurchargeEnabled == true) {
                        TripInfoRow(
                            title = stringResource(id = R.string.holiday_surcharge),
                            value = "+$${
                                viewModel.tripData.holidayOrNightSurcharge?.toInt()
                                    ?.formatNumberWithDots()
                            } COP"
                        )
                    }

                    if (viewModel.tripData.units != null) {
                        TripInfoRow(
                            title = stringResource(id = R.string.total_units),
                            value = viewModel.tripData.units?.toInt().toString()
                        )
                    }

                    if (viewModel.tripData.total != null) {
                        TripInfoRow(
                            title = stringResource(id = R.string.total),
                            value = "$${
                                viewModel.tripData.total?.toInt()
                                    ?.formatNumberWithDots()
                            } COP"
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
                                onClick = onFinishAction,
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