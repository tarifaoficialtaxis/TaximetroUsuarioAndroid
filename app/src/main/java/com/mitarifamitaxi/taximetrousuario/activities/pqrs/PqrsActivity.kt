package com.mitarifamitaxi.taximetrousuario.activities.pqrs

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.BaseActivity
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomCheckBox
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomMultilineTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.pqrs.PqrsViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.pqrs.PqrsViewModelFactory

class PqrsActivity : BaseActivity() {

    private val viewModel: PqrsViewModel by viewModels {
        PqrsViewModelFactory(this, appViewModel)
    }

    @Composable
    override fun Content() {
        MainView(
            onClickSendPqr = {
                viewModel.validateSendPqr {
                    startActivity(it)
                }
            }
        )
    }

    @Composable
    private fun MainView(
        onClickSendPqr: () -> Unit,
    ) {

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(colorResource(id = R.color.white)),
        ) {
            TopHeaderView(
                title = stringResource(id = R.string.pqrs),
                leadingIcon = Icons.Filled.ChevronLeft,
                onClickLeading = {
                    finish()
                }
            )

            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(horizontal = 29.dp)
                    .padding(vertical = 29.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {

                    CustomTextField(
                        value = viewModel.plate,
                        onValueChange = { viewModel.plate = it },
                        placeholder = stringResource(id = R.string.plate),
                        leadingIcon = Icons.Rounded.DirectionsCar,
                    )
                }

                Column(
                    modifier = Modifier.Companion.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.reason_complaint),
                        color = colorResource(id = R.color.gray1),
                        fontSize = 16.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Companion.Medium,
                        textAlign = TextAlign.Companion.Start,
                        modifier = Modifier.Companion
                            .padding(bottom = 10.dp)
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.high_fare),
                        checked = viewModel.isHighFare,
                        onValueChange = { viewModel.isHighFare = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.user_mistreated),
                        checked = viewModel.isUserMistreated,
                        onValueChange = { viewModel.isUserMistreated = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.service_abandonment),
                        checked = viewModel.isServiceAbandonment,
                        onValueChange = { viewModel.isServiceAbandonment = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.unauthorized_charges),
                        checked = viewModel.isUnauthorizedCharges,
                        onValueChange = { viewModel.isUnauthorizedCharges = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.no_fare_notice),
                        checked = viewModel.isNoFareNotice,
                        onValueChange = { viewModel.isNoFareNotice = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.dangerous_driving),
                        checked = viewModel.isDangerousDriving,
                        onValueChange = { viewModel.isDangerousDriving = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.other),
                        checked = viewModel.isOther,
                        onValueChange = { viewModel.isOther = it }
                    )


                    if (viewModel.isOther) {
                        Spacer(modifier = Modifier.Companion.height(10.dp))
                        CustomMultilineTextField(
                            value = viewModel.otherValue,
                            onValueChange = { viewModel.otherValue = it },
                            placeholder = stringResource(id = R.string.other_reason),
                        )
                    }

                }

                Spacer(modifier = Modifier.Companion.weight(1f))

                CustomButton(
                    text = stringResource(id = R.string.create_pqr).uppercase(),
                    onClick = onClickSendPqr,
                )

            }


        }


    }
}