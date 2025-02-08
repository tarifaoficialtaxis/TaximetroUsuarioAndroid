package com.mitarifamitaxi.taximetrousuario.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomPopupDialog
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModelFactory
import com.mitarifamitaxi.taximetrousuario.viewmodels.ProfileViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.ProfileViewModelFactory

class ProfileActivity : BaseActivity() {

    override fun isDrawerEnabled(): Boolean = true

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(this, appViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun Content() {
        MainView(
            onClickBack = {
                finish()
            }
        )

    }

    @Composable
    private fun MainView(
        onClickBack: () -> Unit
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.white)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        colorResource(id = R.color.main),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(top = 20.dp)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.city_background),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        Button(
                            onClick = { onClickBack() },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RectangleShape,
                            modifier = Modifier
                                .width(40.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "content description",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(0.dp),
                                tint = colorResource(id = R.color.white),
                            )

                        }

                        Text(
                            text = stringResource(id = R.string.profile).uppercase(),
                            color = colorResource(id = R.color.white),
                            fontSize = 20.sp,
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.width(40.dp))

                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(colorResource(id = R.color.blue1), shape = CircleShape)
                            .border(2.dp, colorResource(id = R.color.white), CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "content description",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(66.dp),
                            tint = colorResource(id = R.color.white),

                            )
                    }

                    Text(
                        text = appViewModel.userData?.firstName + " " + appViewModel.userData?.lastName,
                        color = colorResource(id = R.color.white),
                        fontSize = 18.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .fillMaxWidth()
                    )

                    Text(
                        text = stringResource(
                            id = R.string.city_param,
                            appViewModel.userData?.city ?: ""
                        ),
                        color = colorResource(id = R.color.white),
                        fontSize = 14.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                        )
                        {

                            Text(
                                text = "500",
                                color = colorResource(id = R.color.white),
                                fontSize = 16.sp,
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = stringResource(
                                    id = R.string.trips_made
                                ),
                                color = colorResource(id = R.color.white),
                                fontSize = 14.sp,
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,

                                )

                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(colorResource(id = R.color.white))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                        ) {

                            Text(
                                text = "1200",
                                color = colorResource(id = R.color.white),
                                fontSize = 16.sp,
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = stringResource(
                                    id = R.string.km_made
                                ),
                                color = colorResource(id = R.color.white),
                                fontSize = 14.sp,
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )


                        }
                    }

                }

            }


        }
    }
}