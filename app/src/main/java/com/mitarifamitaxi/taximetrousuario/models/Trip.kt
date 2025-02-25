package com.mitarifamitaxi.taximetrousuario.models

import android.graphics.Bitmap
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
    val units: Double? = null,
    val total: Double? = null,
    val distance: Double? = null,

    @get:PropertyName("isHolidayOrNightSurcharge")
    @field:PropertyName("isHolidayOrNightSurcharge")
    val holidayOrNightSurchargeEnabled: Boolean? = null,
    val holidayOrNightSurcharge: Double? = null,

    @get:PropertyName("isNightSurcharge")
    @field:PropertyName("isNightSurcharge")
    val nightSurchargeEnabled: Boolean? = null,
    val nightSurcharge: Double? = null,

    @get:PropertyName("isHolidaySurcharge")
    @field:PropertyName("isHolidaySurcharge")
    val holidaySurchargeEnabled: Boolean? = null,
    val holidaySurcharge: Double? = null,

    @get:PropertyName("isDoorToDoorSurcharge")
    @field:PropertyName("isDoorToDoorSurcharge")
    val doorToDoorSurchargeEnabled: Boolean? = null,
    val doorToDoorSurcharge: Double? = null,

    @get:PropertyName("isAirportSurcharge")
    @field:PropertyName("isAirportSurcharge")
    val airportSurchargeEnabled: Boolean? = null,
    val airportSurcharge: Double? = null,

    val routeImage: String? = null,

    val routeImageLocal: Bitmap? = null
)
