package com.mitarifamitaxi.taximetrousuario.activities.onboarding

import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.BaseActivity
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.onboarding.TermsConditionsViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.onboarding.TermsConditionsViewModelFactory

class TermsConditionsActivity : BaseActivity() {

    private val viewModel: TermsConditionsViewModel by viewModels {
        TermsConditionsViewModelFactory(this)
    }

    @Composable
    override fun Content() {
        MainView(
            onAcceptClicked = {
                viewModel.saveAcceptedTerms()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        )
    }

    @Composable
    private fun MainView(
        onAcceptClicked: () -> Unit
    ) {
        Column {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .background(colorResource(id = R.color.white))
            ) {
                Column(modifier = Modifier.Companion.fillMaxSize()) {

                    Box(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(colorResource(id = R.color.main))
                    ) {

                        Box(
                            modifier = Modifier.Companion
                                .fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.city_background),
                                contentDescription = null,
                                modifier = Modifier.Companion
                                    .fillMaxSize()
                                    .align(Alignment.Companion.BottomCenter)
                                    .offset(y = 16.dp)
                            )

                            Image(
                                painter = painterResource(id = R.drawable.logo1),
                                contentDescription = null,
                                modifier = Modifier.Companion
                                    .height(134.dp)
                                    .align(Alignment.Companion.Center)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .offset(y = (-24).dp),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.white),
                        )
                    ) {
                        Column(
                            modifier = Modifier.Companion
                                .fillMaxSize()
                                .padding(top = 29.dp, bottom = 70.dp, start = 29.dp, end = 29.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.terms_title),
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Companion.Bold,
                                fontSize = 24.sp,
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(bottom = 25.dp),
                                textAlign = TextAlign.Companion.Center,
                            )

                            Column(
                                modifier = Modifier.Companion
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = stringResource(id = R.string.terms_body),
                                    fontFamily = MontserratFamily,
                                    fontWeight = FontWeight.Companion.Normal,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.BottomCenter)
                        .padding(horizontal = 29.dp)
                        .padding(bottom = 29.dp)
                ) {
                    CustomButton(
                        text = stringResource(id = R.string.accept_continue).uppercase(),
                        onClick = onAcceptClicked
                    )
                }
            }
        }
    }
}