package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.models.DialogType

@Composable
fun CustomPopupDialog(
    dialogType: DialogType,
    title: String,
    message: String,
    primaryActionButton: String? = null,
    showCloseButton: Boolean = true,
    onDismiss: () -> Unit,
    onPrimaryActionClicked: () -> Unit = {}
) {

    val primaryColor: Color = when (dialogType) {
        DialogType.SUCCESS -> colorResource(id = R.color.main)
        DialogType.ERROR -> colorResource(id = R.color.red1)
        DialogType.WARNING -> colorResource(id = R.color.main)
        DialogType.INFO -> Color.Blue
    }

    val secondaryColor: Color = when (dialogType) {
        DialogType.SUCCESS -> colorResource(id = R.color.yellow2)
        DialogType.ERROR -> colorResource(id = R.color.red2)
        DialogType.WARNING -> colorResource(id = R.color.yellow2)
        DialogType.INFO -> Color.Blue
    }

    val imageIcon: ImageVector = when (dialogType) {
        DialogType.SUCCESS -> Icons.Default.PriorityHigh
        DialogType.ERROR -> Icons.Default.Close
        DialogType.WARNING -> Icons.Default.PriorityHigh
        DialogType.INFO -> Icons.Default.PriorityHigh
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(8.dp, secondaryColor, CircleShape)
                        .background(primaryColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = imageIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(55.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))


                primaryActionButton?.let {
                    CustomButton(
                        text = it.uppercase(),
                        onClick = onPrimaryActionClicked,
                        color = primaryColor,
                    )
                }


                if (showCloseButton) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        border = BorderStroke(0.dp, Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colorResource(id = R.color.gray7),
                            contentColor = colorResource(id = R.color.white)
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "content description"
                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}

