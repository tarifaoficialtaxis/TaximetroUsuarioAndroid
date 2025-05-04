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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily

@Composable
fun CustomPasswordPopupDialog(
    title: String,
    message: String,
    buttonText: String,
    onDismiss: () -> Unit,
    onPasswordValid: (password: String) -> Unit
) {

    val primaryColor: Color = colorResource(id = R.color.main)
    val secondaryColor: Color = colorResource(id = R.color.yellow2)
    val imageIcon: ImageVector = Icons.Default.PriorityHigh

    var password by remember { mutableStateOf("") }
    var passwordIsValid by remember { mutableStateOf(true) }
    val requiredFieldMessage = stringResource(id = R.string.required_field)

    val validatePassword = {
        val isValid = password.isNotEmpty()
        passwordIsValid = isValid
        if (isValid) {
            onPasswordValid(password)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    //.align(Alignment.BottomCenter)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(20.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
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
                        textAlign = TextAlign.Center,
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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


                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = stringResource(id = R.string.password),
                        isSecure = true,
                        leadingIcon = Icons.Rounded.Lock,
                        isError = !passwordIsValid,
                        errorMessage = if (!passwordIsValid) requiredFieldMessage else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButton(
                        text = buttonText.uppercase(),
                        onClick = validatePassword,
                        color = primaryColor,
                    )


                    Spacer(modifier = Modifier.height(16.dp))

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
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}

