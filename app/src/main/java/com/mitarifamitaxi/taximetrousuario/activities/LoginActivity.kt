package com.mitarifamitaxi.taximetrousuario.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomCheckBox
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainView()
        }
    }

    @Composable
    private fun MainView() {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(colorResource(id = R.color.main))
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.city_background),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 40.dp)
                            )

                            Image(
                                painter = painterResource(id = R.drawable.logo2),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(134.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-24).dp),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.white),
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 29.dp, bottom = 70.dp, start = 29.dp, end = 29.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.welcome),
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = colorResource(id = R.color.main),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 25.dp),
                                textAlign = TextAlign.Center,
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(17.dp),
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                            ) {
                                CustomTextField(
                                    value = viewModel.userName,
                                    onValueChange = { viewModel.userName = it },
                                    placeholder = stringResource(id = R.string.user_name),
                                    leadingIcon = Icons.Rounded.Person,
                                    keyboardType = KeyboardType.Email
                                )

                                CustomTextField(
                                    value = viewModel.password,
                                    onValueChange = { viewModel.password = it },
                                    placeholder = stringResource(id = R.string.password),
                                    isSecure = true,
                                    leadingIcon = Icons.Rounded.Lock,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,

                                ) {

                                CustomCheckBox(
                                    text = stringResource(id = R.string.remember_me),
                                    onValueChange = { viewModel.rememberMe = it }
                                )

                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(id = R.color.transparent),
                                    ),
                                    modifier = Modifier
                                        .width(180.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.recover_password),
                                        fontFamily = MontserratFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.main),
                                        textAlign = TextAlign.End,
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(vertical = 29.dp)
                            ) {

                                CustomButton(
                                    text = stringResource(id = R.string.login).uppercase(),
                                    onClick = { viewModel.login() }
                                )
                            }


                        }
                    }
                }


            }
        }
    }
}