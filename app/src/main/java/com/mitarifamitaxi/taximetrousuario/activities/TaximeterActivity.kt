package com.mitarifamitaxi.taximetrousuario.activities

import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomSizedMarker
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.helpers.formatNumberWithDots
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import com.mitarifamitaxi.taximetrousuario.viewmodels.TaximeterViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.TaximeterViewModelFactory

class TaximeterActivity : BaseActivity() {

    private val viewModel: TaximeterViewModel by viewModels {
        TaximeterViewModelFactory(this, appViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    @Composable
    override fun Content() {
        MainView()

        if (viewModel.showDialog) {
            CustomPopupDialog(
                dialogType = viewModel.dialogType,
                title = viewModel.dialogTitle,
                message = viewModel.dialogMessage,
                onDismiss = { viewModel.showDialog = false }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
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

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    appViewModel.userData?.location?.latitude ?: 4.60971,
                    appViewModel.userData?.location?.longitude ?: -74.08175
                ), 15f
            )
        }


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
                    ) {


                        if (viewModel.startAddress.isNotEmpty()) {
                            CustomSizedMarker(
                                position = LatLng(
                                    viewModel.startLocation.latitude ?: 0.0,
                                    viewModel.startLocation.longitude ?: 0.0
                                ),
                                drawableRes = R.drawable.flag_start,
                                width = 120,
                                height = 120
                            )

                        }

                    }
                }
            }
        }
    }

    @Composable
    fun SheetExpandedView() {


        Column(
            modifier = Modifier
                .fillMaxHeight()
        ) {

            Text(
                text = "$ ${viewModel.total.formatNumberWithDots()} COP",
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
                    .padding(bottom = 15.dp)
            )


            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            ) {
                CustomButton(
                    text = stringResource(id = R.string.start_trip).uppercase(),
                    onClick = {},
                    color = colorResource(id = R.color.main),
                    leadingIcon = Icons.Default.PlayArrow
                )
            }
        }
    }

    @Composable
    fun SheetFoldedView() {
        Text(
            text = stringResource(id = R.string.drag_to_set_start_point),
            color = colorResource(id = R.color.gray1),
            fontSize = 15.sp,
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        ) {
            CustomButton(
                text = stringResource(id = R.string.start_trip).uppercase(),
                onClick = {},
                color = colorResource(id = R.color.main),
                leadingIcon = Icons.Default.PlayArrow
            )
        }

    }

}