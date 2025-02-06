package com.mitarifamitaxi.taximetrousuario.models

data class Trip(
    val id: String? = null,
    val userId: String? = null,
    val startAddress: String? = null,
    val endAddress: String? = null,
    val startCoords: Location? = null,
    val endCoords: Location? = null,
    val startHour: String? = null,
    val endHour: String? = null,
    val units: Long? = null,
    val total: Long? = null,
    val distance: Long? = null,
    val imageRoute: String? = null,
    val isHolidaySurcharge: Boolean? = null,
    val isDoorToDoorSurcharge: Boolean? = null,
    val isAirportSurcharge: Boolean? = null
)