package com.mitarifamitaxi.taximetrousuario.models

import com.google.firebase.firestore.PropertyName

data class Trip(
    val id: String? = null,
    val userId: String? = null,
    val startAddress: String? = null,
    val endAddress: String? = null,
    val startCoords: UserLocation? = null,
    val endCoords: UserLocation? = null,
    val startHour: String? = null,
    val endHour: String? = null,
    val units: Int? = null,
    val total: Int? = null,
    val distance: Int? = null,

    @get:PropertyName("isHolidaySurcharge")
    @field:PropertyName("isHolidaySurcharge")
    val holidaySurchargeEnabled: Boolean? = null,
    val holidaySurcharge: Int? = null,

    @get:PropertyName("isDoorToDoorSurcharge")
    @field:PropertyName("isDoorToDoorSurcharge")
    val doorToDoorSurchargeEnabled: Boolean? = null,
    val doorToDoorSurcharge: Int? = null,

    @get:PropertyName("isAirportSurcharge")
    @field:PropertyName("isAirportSurcharge")
    val airportSurchargeEnabled: Boolean? = null,
    val airportSurcharge: Int? = null,

    val routeImage: String? = null
)
