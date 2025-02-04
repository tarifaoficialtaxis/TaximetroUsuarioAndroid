package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isSecure: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector,
) {
    val isPasswordVisible = remember { mutableStateOf(isSecure) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = placeholder,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = if (isSecure) KeyboardType.Password else keyboardType),
        singleLine = true,
        visualTransformation = if (isPasswordVisible.value) PasswordVisualTransformation() else VisualTransformation.None,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colorResource(id = R.color.main)
            )
        },
        trailingIcon = {
            if (isSecure) {
                Icon(
                    imageVector = if (isPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = colorResource(id = R.color.main),
                    modifier = Modifier.clickable {
                        isPasswordVisible.value = !isPasswordVisible.value
                    }
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorResource(id = R.color.transparent),
            unfocusedContainerColor = colorResource(id = R.color.transparent),

            focusedLabelColor = colorResource(id = R.color.gray1),
            focusedPlaceholderColor = colorResource(id = R.color.gray1),
            unfocusedPlaceholderColor = colorResource(id = R.color.gray1),

            focusedTextColor = colorResource(id = R.color.gray1),

            focusedIndicatorColor = colorResource(id = R.color.main),
            unfocusedIndicatorColor = colorResource(id = R.color.gray2)
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.fillMaxWidth()
    )
}