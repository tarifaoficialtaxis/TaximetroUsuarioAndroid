package com.mitarifamitaxi.taximetrousuario.models

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ItemImageButton(
    val id: String,
    val image: Painter,
    val height: Dp = 140.dp,
)