package com.mitarifamitaxi.taximetrousuario.helpers

import com.mitarifamitaxi.taximetrousuario.resources.countries
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

fun getCityFromCoordinates(
    latitude: Double,
    longitude: Double,
    callbackSuccess: (city: String?, countryCodeWhatsapp: String?) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=${Constants.GOOGLE_API_KEY}"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callbackError(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    callbackError(IOException("Unexpected response $response"))
                    return
                }

                val jsonResponse = JSONObject(it.body?.string() ?: "")
                val results = jsonResponse.optJSONArray("results")

                if (results != null && results.length() > 0) {
                    val addressComponents =
                        results.getJSONObject(0).optJSONArray("address_components")

                    var city: String? = null
                    var country: String? = null

                    addressComponents?.let {
                        for (i in 0 until it.length()) {
                            val component = it.getJSONObject(i)
                            val types = component.optJSONArray("types")

                            if (types != null) {
                                if (types.toString().contains("locality")) {
                                    city = component.optString("long_name")
                                } else if (city == null && types.toString()
                                        .contains("administrative_area_level_1")
                                ) {
                                    city = component.optString("long_name")
                                } else if (types.toString().contains("country")) {
                                    country = component.optString("long_name")
                                }
                            }
                        }
                    }

                    val countryCodeWhatsapp =
                        countries.find { it.name == country }?.dial?.replace("+", "")

                    callbackSuccess(city, countryCodeWhatsapp)
                } else {
                    callbackError(IOException("No results found"))
                }
            }
        }
    })
}

