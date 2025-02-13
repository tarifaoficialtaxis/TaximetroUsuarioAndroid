package com.mitarifamitaxi.taximetrousuario.activities

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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomCheckBox
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomMultilineTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.PqrsViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.PqrsViewModelFactory

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

        if (viewModel.showDialog) {
            CustomPopupDialog(
                dialogType = viewModel.dialogType,
                title = viewModel.dialogTitle,
                message = viewModel.dialogMessage,
                showCloseButton = viewModel.dialogShowCloseButton,
                primaryActionButton = viewModel.dialogPrimaryAction,
                onDismiss = { viewModel.showDialog = false }
            )
        }

    }

    @Composable
    private fun MainView(
        onClickSendPqr: () -> Unit,
    ) {

        Column(
            modifier = Modifier
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
                modifier = Modifier
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

                    CustomTextField(
                        value = viewModel.idNumber,
                        onValueChange = { viewModel.idNumber = it },
                        placeholder = stringResource(id = R.string.cedula),
                        leadingIcon = Icons.Rounded.Person,
                        keyboardType = KeyboardType.Number
                    )
                }

                Column(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.reason_complaint),
                        color = colorResource(id = R.color.gray1),
                        fontSize = 16.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.high_fare),
                        onValueChange = { viewModel.isHighFare = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.user_mistreated),
                        onValueChange = { viewModel.isUserMistreated = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.service_abandonment),
                        onValueChange = { viewModel.isServiceAbandonment = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.unauthorized_charges),
                        onValueChange = { viewModel.isUnauthorizedCharges = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.no_fare_notice),
                        onValueChange = { viewModel.isNoFareNotice = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.dangerous_driving),
                        onValueChange = { viewModel.isDangerousDriving = it }
                    )

                    CustomCheckBox(
                        text = stringResource(id = R.string.other),
                        onValueChange = { viewModel.isOther = it }
                    )


                    if (viewModel.isOther) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CustomMultilineTextField(
                            value = viewModel.otherValue,
                            onValueChange = { viewModel.otherValue = it },
                            placeholder = stringResource(id = R.string.other_reason),
                        )
                    }

                }

                Spacer(modifier = Modifier.weight(1f))

                CustomButton(
                    text = stringResource(id = R.string.create_pqr).uppercase(),
                    onClick = onClickSendPqr,
                )

            }


        }


    }
}