package com.mitarifamitaxi.taximetrousuario.models

data class RegionalRates(
    val city: String? = null,
    val basicRace: Int? = null,
    val specialRace: Int? = null,
    val superRace: Int? = null,
    val doorToDoorSurcharge: Int? = null,
    val holidaySurcharge: Int? = null,
    val nightSurcharge: Int? = null,
    val nightHourSurcharge: Int? = null,
    val nighMinuteSurcharge: Int? = null,
    val morningHourSurcharge: Int? = null,
    val morningMinuteSurcharge: Int? = null,
    val companyImage: String? = null,
)