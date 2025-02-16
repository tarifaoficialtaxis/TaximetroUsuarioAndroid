package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mitarifamitaxi.taximetrousuario.R

@Composable
fun FloatingActionButtonRoutes(
    expanded: Boolean,
    onMainFabClick: () -> Unit,
    onAction1Click: () -> Unit,
    onAction2Click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            if (expanded) {

                FloatingActionButton(
                    onClick = onAction1Click,
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(46.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.waze_button),
                        contentDescription = null
                    )
                }
                FloatingActionButton(
                    onClick = onAction2Click,
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(46.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.maps_button),
                        contentDescription = "Action 2"
                    )
                }
            }

            FloatingActionButton(
                onClick = onMainFabClick,
                shape = CircleShape,
                containerColor = colorResource(id = R.color.main),
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.NearMe,
                    contentDescription = null
                )
            }
        }
    }
}