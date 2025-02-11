package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButtonActionDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomImageButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.components.ui.TopHeaderView
import com.mitarifamitaxi.taximetrousuario.models.ItemImageButton
import com.mitarifamitaxi.taximetrousuario.viewmodels.SosViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.SosViewModelFactory

class SosActivity : BaseActivity() {

    private val viewModel: SosViewModel by viewModels {
        SosViewModelFactory(this, appViewModel)
    }

    override fun onResume() {
        super.onResume()
        appViewModel.reloadUserData()
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
                    if (viewModel.dialogTitle == getString(R.string.support_number_not_found) || viewModel.dialogTitle == getString(
                            R.string.family_number_not_found
                        )
                    ) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                }
            )
        }

        if (viewModel.showContactDialog) {
            CustomButtonActionDialog(
                title = stringResource(id = R.string.select_one_action),
                onDismiss = { viewModel.showContactDialog = false },
                onPrimaryActionClicked = {
                    viewModel.showContactDialog = false
                    viewModel.validateSendMessageAction(onIntentReady = {
                        startActivity(it)
                    })
                },
                onSecondaryActionClicked = {
                    viewModel.showContactDialog = false
                    viewModel.validateCallAction(onIntentReady = {
                        startActivity(it)
                    })
                }
            )
        }

    }


    @Composable
    fun sosItems(): List<ItemImageButton> {
        return listOf(
            ItemImageButton(
                id = "POLICE",
                image = painterResource(id = R.drawable.sos_police_button),
            ),
            ItemImageButton(
                id = "FIRE_FIGHTERS",
                image = painterResource(id = R.drawable.sos_firefighters_button),
            ),
            ItemImageButton(
                id = "AMBULANCE",
                image = painterResource(id = R.drawable.sos_ambulance_button),
            ),
            ItemImageButton(
                id = "FAMILY",
                image = painterResource(id = R.drawable.sos_family_button),
            ),
            ItemImageButton(
                id = "SUPPORT",
                image = painterResource(id = R.drawable.sos_support_button),
            ),
            ItemImageButton(
                id = "ANIMAL_CARE",
                image = painterResource(id = R.drawable.sos_animal_care_button),
            )
        )
    }


    @Composable
    private fun MainView(

    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.gray4)),
        ) {
            TopHeaderView(
                title = stringResource(id = R.string.sos),
                leadingIcon = Icons.Filled.ChevronLeft,
                onClickLeading = {
                    finish()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 29.dp)
                        .padding(top = 20.dp)
                        .padding(bottom = 40.dp)
                ) {
                    sosItems().forEach { item ->
                        CustomImageButton(
                            image = item.image,
                            height = item.height,
                            onClick = {
                                viewModel.showContactDialog = true
                                viewModel.itemSelected = item
                            }
                        )
                    }

                }
            }


        }
    }
}