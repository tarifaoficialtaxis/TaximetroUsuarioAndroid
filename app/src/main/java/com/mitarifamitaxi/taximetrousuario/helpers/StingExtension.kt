package com.mitarifamitaxi.taximetrousuario.helpers

fun String.isValidEmail(): Boolean {
    return this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.getShortAddress(): String {
    return this.split(",").getOrNull(0) ?: ""
}
