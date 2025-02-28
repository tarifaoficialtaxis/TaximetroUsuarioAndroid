package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.delay
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomCheckBox
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomSizedMarker
import com.mitarifamitaxi.taximetrousuario.components.ui.FloatingActionButtonRoutes
import com.mitarifamitaxi.taximetrousuario.components.ui.TaximeterInfoRow
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.helpers.calculateBearing
import com.mitarifamitaxi.taximetrousuario.helpers.formatDigits
import com.mitarifamitaxi.taximetrousuario.helpers.formatNumberWithDots
import com.mitarifamitaxi.taximetrousuario.helpers.getShortAddress
import com.mitarifamitaxi.taximetrousuario.models.CityArea
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import com.mitarifamitaxi.taximetrousuario.viewmodels.TaximeterByRegionViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.TaximeterByRegionViewModelFactory

class TaximeterByRegionActivity : BaseActivity() {

    private val viewModel: TaximeterByRegionViewModel by viewModels {
        TaximeterByRegionViewModelFactory(this, appViewModel)
    }

    val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.getCurrentLocation()
        } else {
            viewModel.showCustomDialog(
                DialogType.ERROR,
                getString(R.string.permission_required),
                getString(R.string.background_location_permission_required)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.requestBackgroundLocationPermission(this)

        val startAddress = intent.getStringExtra("start_address")
        startAddress?.let {
            viewModel.startAddress = it
        }
        val startLocation = intent.getStringExtra("start_location")
        startLocation?.let {
            viewModel.startLocation = Gson().fromJson(it, UserLocation::class.java)
        }

        val endAddress = intent.getStringExtra("end_address")
        endAddress?.let {
            viewModel.endAddress = it
        }
        val endLocation = intent.getStringExtra("end_location")
        endLocation?.let {
            viewModel.endLocation = Gson().fromJson(it, UserLocation::class.java)
        }

        val sharedPref = this.getSharedPreferences("CityAreaData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("CITY_AREA_OBJECT", null)
        viewModel.cityAreas = Gson().fromJson(userJson, CityArea::class.java)

        viewModel.getCityRates(appViewModel.userData?.city ?: "")
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Composable
    override fun Content() {
        MainView()

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
                    if (viewModel.dialogType == DialogType.WARNING && viewModel.dialogPrimaryAction == getString(
                            R.string.finish_trip
                        )
                    ) {
                        viewModel.stopTaximeter()
                    }

                }
            )
        }
    }

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
        MapsComposeExperimentalApi::class
    )
    @Composable
    fun MainView() {

        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = true
        )

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val peekHeight = screenHeight * 0.23f
        val fullHeight = screenHeight * 0.6f

        val mapPeekHeight = screenHeight * 0.36f
        val mapFullHeight = screenHeight * 0.78f

        val sheetTopOffset =
            if (sheetState.currentValue == SheetValue.Expanded) mapPeekHeight else mapFullHeight
        val sheetTopOffsetAdjust =
            if (viewModel.isFabExpanded) 188.dp else 80.dp

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    appViewModel.userData?.location?.latitude ?: 4.60971,
                    appViewModel.userData?.location?.longitude ?: -74.08175
                ), 15f
            )
        }

        LaunchedEffect(viewModel.currentPosition, viewModel.routeCoordinates) {
            val targetLatLng = LatLng(
                viewModel.currentPosition.latitude ?: 0.0,
                viewModel.currentPosition.longitude ?: 0.0
            )

            if (viewModel.routeCoordinates.size > 1) {
                val previousPosition =
                    viewModel.routeCoordinates[viewModel.routeCoordinates.size - 2]
                val newRotation = calculateBearing(
                    previousPosition, LatLng(
                        viewModel.currentPosition.latitude ?: 0.0,
                        viewModel.currentPosition.longitude ?: 0.0
                    )
                )

                val camPos = CameraPosition.builder(cameraPositionState.position)
                    .target(targetLatLng)
                    .zoom(15f)
                    .bearing(newRotation)
                    .build()

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(camPos)
                )
            }


        }

        LaunchedEffect(viewModel.fitCameraPosition) {
            if (viewModel.fitCameraPosition && viewModel.routeCoordinates.size > 1) {
                val boundsBuilder = LatLngBounds.builder()
                viewModel.routeCoordinates.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                val padding = 120
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
                )
                delay(1000L)
                viewModel.takeMapScreenshot = true
            } else if (viewModel.fitCameraPosition) {
                viewModel.takeMapScreenshot = true
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            BottomSheetScaffold(
                scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState),
                sheetContent = {
                    Column(
                        modifier = Modifier
                            .height(if (sheetState.currentValue == SheetValue.Expanded) fullHeight else peekHeight)
                            .padding(horizontal = 20.dp),
                    ) {
                        if (sheetState.currentValue == SheetValue.Expanded) {
                            SheetExpandedView()
                        } else {
                            SheetFoldedView()
                        }

                    }

                },
                sheetContainerColor = colorResource(id = R.color.white),
                sheetPeekHeight = peekHeight,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (sheetState.currentValue == SheetValue.Expanded) mapPeekHeight else mapFullHeight)
                ) {
                    TopHeaderView(
                        title = stringResource(id = R.string.taximeter),
                        leadingIcon = Icons.Filled.ChevronLeft,
                        onClickLeading = {
                            finish()
                        }
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false
                            ),
                            modifier = Modifier.fillMaxSize(),
                            onMapLoaded = { viewModel.isMapLoaded = true }
                        ) {

                            if (viewModel.routeCoordinates.isNotEmpty()) {
                                Polyline(
                                    points = viewModel.routeCoordinates,
                                    color = colorResource(id = R.color.main),
                                    width = 10f
                                )
                            }


                            if (viewModel.startAddress.isNotEmpty()) {
                                CustomSizedMarker(
                                    position = LatLng(
                                        viewModel.startLocation.latitude ?: 0.0,
                                        viewModel.startLocation.longitude ?: 0.0
                                    ),
                                    drawableRes = R.drawable.flag_start,
                                    width = 60,
                                    height = 60
                                )

                            }

                            CustomSizedMarker(
                                position = LatLng(
                                    viewModel.currentPosition.latitude ?: 0.0,
                                    viewModel.currentPosition.longitude ?: 0.0
                                ),
                                drawableRes = R.drawable.taxi_marker,
                                width = 27,
                                height = 57
                            )

                            if (viewModel.isMapLoaded && viewModel.takeMapScreenshot) {
                                MapEffect { map ->
                                    map.snapshot { snapshot ->
                                        if (snapshot != null) {
                                            viewModel.mapScreenshotReady(snapshot) { intent ->
                                                startActivity(intent)
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        if (viewModel.ratesObj.value.companyImage != null) {
                            AsyncImage(
                                model = viewModel.ratesObj.value.companyImage,
                                contentDescription = "Company Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            FloatingActionButtonRoutes(
                expanded = viewModel.isFabExpanded,
                onMainFabClick = { viewModel.isFabExpanded = !viewModel.isFabExpanded },
                onAction1Click = {
                    viewModel.openWazeApp(
                        viewModel.endLocation.latitude ?: 0.0,
                        viewModel.endLocation.longitude ?: 0.0,
                        onIntentReady = { startActivity(it) }
                    )
                },
                onAction2Click = {
                    viewModel.openGoogleMapsApp(
                        viewModel.startLocation.latitude ?: 0.0,
                        viewModel.startLocation.longitude ?: 0.0,
                        viewModel.endLocation.latitude ?: 0.0,
                        viewModel.endLocation.longitude ?: 0.0,
                        onIntentReady = { startActivity(it) }
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = sheetTopOffset - sheetTopOffsetAdjust)
                    .padding(end = 16.dp)
            )

        }
    }

    @Composable
    fun SheetExpandedView() {


        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "$ ${viewModel.total.toInt().formatNumberWithDots()} COP",
                color = colorResource(id = R.color.main),
                fontSize = 36.sp,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.price_to_pay),
                color = colorResource(id = R.color.gray1),
                fontSize = 15.sp,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            TaximeterInfoRow(
                title = stringResource(id = R.string.distance_made),
                value = "${(viewModel.distanceMade / 1000).formatDigits(1)} KM",
            )

            TaximeterInfoRow(
                title = stringResource(id = R.string.time_trip),
                value = viewModel.formattedTime
            )

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colorResource(id = R.color.main),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = getShortAddress(viewModel.endAddress),
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.gray1),
                    )

                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(colorResource(id = R.color.gray2))
                )

            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {

                CustomCheckBox(
                    text = stringResource(id = R.string.door_to_door_surcharge).replace(":", ""),
                    checked = viewModel.isDoorToDoorSurcharge,
                    onValueChange = {
                        viewModel.isDoorToDoorSurcharge = it
                        if (it) {
                            viewModel.total += viewModel.ratesObj.value.doorToDoorSurcharge?.toDouble()
                                ?: 0.0
                        } else {
                            viewModel.total -= viewModel.ratesObj.value.doorToDoorSurcharge?.toDouble()
                                ?: 0.0
                        }
                    }
                )

                CustomCheckBox(
                    text = stringResource(id = R.string.holiday_surcharge_only).replace(":", ""),
                    checked = viewModel.isHolidaySurcharge,
                    isEnabled = false,
                    onValueChange = {}
                )

                CustomCheckBox(
                    text = stringResource(id = R.string.night_surcharge_only).replace(":", ""),
                    checked = viewModel.isNightSurcharge,
                    isEnabled = false,
                    onValueChange = {}
                )

            }
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    CustomButton(
                        text = stringResource(id = R.string.sos).uppercase(),
                        onClick = {
                            startActivity(
                                Intent(
                                    this@TaximeterByRegionActivity,
                                    SosActivity::class.java
                                )
                            )
                        },
                        color = colorResource(id = R.color.red1),
                        leadingIcon = Icons.Rounded.WarningAmber
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    CustomButton(
                        text = stringResource(id = R.string.pqrs).uppercase(),
                        onClick = {
                            startActivity(
                                Intent(
                                    this@TaximeterByRegionActivity,
                                    PqrsActivity::class.java
                                )
                            )
                        },
                        color = colorResource(id = R.color.blue2),
                        leadingIcon = Icons.AutoMirrored.Outlined.Chat
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                CustomButton(
                    text = stringResource(id = if (viewModel.isTaximeterStarted) R.string.finish_trip else R.string.start_trip).uppercase(),
                    onClick = { if (viewModel.isTaximeterStarted) viewModel.showFinishConfirmation() else viewModel.startTaximeter() },
                    color = colorResource(id = if (viewModel.isTaximeterStarted) R.color.gray1 else R.color.main),
                    leadingIcon = if (viewModel.isTaximeterStarted) Icons.Default.Close else Icons.Default.PlayArrow
                )
            }
        }
    }

    @Composable
    fun SheetFoldedView() {

        Row(
            horizontalArrangement = Arrangement.spacedBy(
                10.dp,
                Alignment.CenterHorizontally
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {

                Text(
                    text = "$ ${viewModel.total.toInt().formatNumberWithDots()} COP",
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.main),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center

                )

                Text(
                    text = stringResource(id = R.string.price_to_pay),
                    color = colorResource(id = R.color.gray1),
                    fontSize = 12.sp,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(45.dp)
                    .background(colorResource(id = R.color.gray2))
            )


            Column(
                modifier = Modifier
                    .weight(1f)
            ) {

                Text(
                    text = "${(viewModel.distanceMade / 1000).formatDigits(1)} KM",
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.main),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.distance_made),
                    color = colorResource(id = R.color.gray1),
                    fontSize = 12.sp,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

            }


        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        ) {
            CustomButton(
                text = stringResource(id = if (viewModel.isTaximeterStarted) R.string.finish_trip else R.string.start_trip).uppercase(),
                onClick = { if (viewModel.isTaximeterStarted) viewModel.showFinishConfirmation() else viewModel.startTaximeter() },
                color = colorResource(id = if (viewModel.isTaximeterStarted) R.color.gray1 else R.color.main),
                leadingIcon = if (viewModel.isTaximeterStarted) Icons.Default.Close else Icons.Default.PlayArrow
            )
        }

    }

}