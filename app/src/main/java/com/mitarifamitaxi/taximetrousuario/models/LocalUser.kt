package com.mitarifamitaxi.taximetrousuario.models

data class LocalUser(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val mobilePhone: String? = null,
    val email: String? = null,
    val countryCode: String? = null,
    val countryCodeWhatsapp: String? = null,
    val countryCurrency: String? = null,
    val city: String? = null,
    val location: UserLocation? = null,
    val familyNumber: String? = null,
    val supportNumber: String? = null
)

