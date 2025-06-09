package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily

@Composable
fun WaitTimeBox(
    time: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.white),
                shape = RoundedCornerShape(8.dp),

                )
            .padding(horizontal = 10.dp)
            .padding(top = 5.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-7).dp),
        ) {
            Text(
                text = time,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorResource(id = R.color.black),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-17).dp),
            ) {
                Text(
                    text = stringResource(R.string.time),
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.black),
                )

                Text(
                    text = stringResource(R.string.of_wait),
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.black),
                )
            }


        }
    }
}