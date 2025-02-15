package com.mitarifamitaxi.taximetrousuario.activities

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.RoutePlannerViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.RoutePlannerViewModelFactory

class RoutePlannerActivity : BaseActivity() {

    private val viewModel: RoutePlannerViewModel by viewModels {
        RoutePlannerViewModelFactory(this, appViewModel)
    }

    @Composable
    override fun Content() {
        MainView()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
    @Composable
    fun MainView() {
        val scaffoldState = rememberBottomSheetScaffoldState()

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    appViewModel.userData?.location?.latitude ?: 4.60971,
                    appViewModel.userData?.location?.longitude ?: -74.08175
                ), 15f
            )
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.begin_your_trip),
                        color = colorResource(id = R.color.main),
                        fontSize = 17.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    if (viewModel.isSheetExpanded) {

                        Text(
                            text = stringResource(id = R.string.select_start_and_end),
                            color = colorResource(id = R.color.gray1),
                            fontSize = 15.sp,
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        Text(
                            text = stringResource(id = R.string.we_will_show_best_route),
                            color = colorResource(id = R.color.gray1),
                            fontSize = 15.sp,
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 15.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = colorResource(id = R.color.main),
                                    shape = RoundedCornerShape(size = 15.dp)
                                )
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 5.dp),
                        ) {
                            CustomTextField(
                                value = viewModel.startAddress,
                                onValueChange = { viewModel.startAddress = it },
                                leadingIcon = Icons.Filled.MyLocation,
                                placeholder = stringResource(id = R.string.start_point),
                                focusedIndicatorColor = colorResource(id = R.color.transparent),
                                unfocusedIndicatorColor = colorResource(id = R.color.transparent),
                            )

                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .background(colorResource(id = R.color.gray2))
                                    .fillMaxWidth()
                            )

                            CustomTextField(
                                value = viewModel.endAddress,
                                onValueChange = { viewModel.endAddress = it },
                                leadingIcon = Icons.Filled.LocationOn,
                                placeholder = stringResource(id = R.string.end_point),
                                focusedIndicatorColor = colorResource(id = R.color.transparent),
                                unfocusedIndicatorColor = colorResource(id = R.color.transparent),
                            )
                        }

                        Button(
                            onClick = { viewModel.setPointOnMap() },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RectangleShape,
                            modifier =
                            Modifier
                                .padding(top = 10.dp)
                                .padding(horizontal = 15.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(
                                    5.dp,
                                    Alignment.CenterHorizontally
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {

                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = colorResource(id = R.color.main),
                                )

                                Text(
                                    text = stringResource(id = R.string.set_point_on_map),
                                    color = colorResource(id = R.color.main),
                                    fontSize = 15.sp,
                                    fontFamily = MontserratFamily,
                                    fontWeight = FontWeight.Bold,
                                )

                            }
                        }
                    } else {

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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = colorResource(id = R.color.main),
                                    shape = RoundedCornerShape(size = 15.dp)
                                )
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 5.dp),
                        ) {
                            CustomTextField(
                                value = viewModel.startAddress,
                                onValueChange = { viewModel.startAddress = it },
                                leadingIcon = Icons.Filled.MyLocation,
                                placeholder = stringResource(id = R.string.start_point),
                                focusedIndicatorColor = colorResource(id = R.color.transparent),
                                unfocusedIndicatorColor = colorResource(id = R.color.transparent),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp)
                        ) {
                            CustomButton(
                                text = stringResource(id = R.string.set_start).uppercase(),
                                onClick = { },
                                color = colorResource(id = R.color.main),
                                leadingIcon = Icons.Default.Check
                            )
                        }


                    }

                }

            },
            sheetContainerColor = colorResource(id = R.color.white),
            sheetPeekHeight = viewModel.sheetPeekHeight,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewModel.mainColumnHeight)
            ) {
                TopHeaderView(
                    title = stringResource(id = R.string.taximeter),
                    leadingIcon = Icons.Filled.ChevronLeft,
                    onClickLeading = {
                        finish()
                    }
                )

                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onMapLoaded = { }
                )
            }
        }
    }

}