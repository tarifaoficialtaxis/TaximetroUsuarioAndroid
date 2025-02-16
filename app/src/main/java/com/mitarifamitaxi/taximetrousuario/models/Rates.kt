package com.mitarifamitaxi.taximetrousuario.models

data class Rates(
    val city: String? = null,
    val airportRateUnits: Double? = null,
    val doorToDoorRateUnits: Double? = null,
    val holidayRateUnits: Double? = null,
    val dragSpeed: Double? = null,
    val meters: Int? = null,
    val minimumRateUnits: Double? = null,
    val startRateUnits: Double? = null,
    val unitPrice: Double? = null,
    val unitsPerHour: Double? = null
)