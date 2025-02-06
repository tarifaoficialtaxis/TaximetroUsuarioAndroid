package com.mitarifamitaxi.taximetrousuario.helpers

import java.text.NumberFormat
import java.util.Locale

fun Int.formatNumberWithDots(): String {

    val formatter = NumberFormat.getInstance(Locale("es", "CO")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
    return formatter.format(this)
}