package com.mitarifamitaxi.taximetrousuario.activities

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.viewmodels.PqrsViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.PqrsViewModelFactory

class PqrsActivity : BaseActivity() {

    private val viewModel: PqrsViewModel by viewModels {
        PqrsViewModelFactory(this, appViewModel)
    }

    @Composable
    override fun Content() {
        MainView()

    }

    @Composable
    private fun MainView(

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
                    .padding(29.dp),
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
        }


    }
}