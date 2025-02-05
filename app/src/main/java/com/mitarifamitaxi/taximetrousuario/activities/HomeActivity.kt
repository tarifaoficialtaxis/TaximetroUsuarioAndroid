package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberAsyncImagePainter
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.HomeViewModelFactory
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModelFactory

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainView()
        }
    }

    @Composable
    private fun MainView(

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.white)),
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        colorResource(id = R.color.black),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )

            ) {
                Image(
                    painter = painterResource(id = R.drawable.city_background3),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))

                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                ) {

                    Image(
                        painter = rememberAsyncImagePainter("https://nypost.com/wp-content/uploads/sites/2/2023/01/Bianca-Censori-09.jpg"),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .border(2.dp, colorResource(id = R.color.white), CircleShape)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = stringResource(id = R.string.welcome_home),
                        color = colorResource(id = R.color.white),
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )

                    Text(
                        text = "Hugo Ospina",
                        color = colorResource(id = R.color.main),
                        fontSize = 20.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Barranquilla",
                        color = colorResource(id = R.color.white),
                        fontSize = 14.sp,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.home_taxi),
                    contentDescription = null,
                    modifier = Modifier
                        .width(220.dp)
                        .height(100.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 15.dp)
                )
            }


            Spacer(modifier = Modifier.weight(1.0f))

            Button(onClick = {
                viewModel.logout(
                    onLogoutComplete = {
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }) {
                Text("Logout")
            }
        }
    }
}