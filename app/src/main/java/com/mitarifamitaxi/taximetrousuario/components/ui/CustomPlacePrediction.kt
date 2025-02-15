package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily


@Composable
fun CustomPlacePrediction(
    address: String,
    region: String,
    onPlaceClicked: () -> Unit
) {

    Button(
        onClick = onPlaceClicked,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RectangleShape
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = address,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.gray1),
                )

                Text(
                    text = region,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.gray1),
                )
            }




            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(colorResource(id = R.color.gray2))
            )

        }
    }


}