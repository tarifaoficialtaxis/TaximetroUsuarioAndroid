package com.mitarifamitaxi.taximetrousuario.helpers

import com.mitarifamitaxi.taximetrousuario.models.PlacePrediction
import com.mitarifamitaxi.taximetrousuario.models.UserLocation
import com.mitarifamitaxi.taximetrousuario.resources.countries
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

const val googleapisUrl = "https://maps.googleapis.com/maps/api/"

fun getCityFromCoordinates(
    latitude: Double,
    longitude: Double,
    callbackSuccess: (city: String?, countryCodeWhatsapp: String?) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "${googleapisUrl}geocode/json?latlng=$latitude,$longitude&key=${Constants.GOOGLE_API_KEY}"

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

fun getAddressFromCoordinates(
    latitude: Double,
    longitude: Double,
    callbackSuccess: (address: String) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "${googleapisUrl}geocode/json?latlng=$latitude,$longitude&key=${Constants.GOOGLE_API_KEY}"

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
                    val address =
                        results.getJSONObject(0).getString("formatted_address")

                    callbackSuccess(address)

                } else {
                    callbackError(IOException("No results found"))
                }
            }
        }
    })
}


fun getPlacePredictions(
    input: String,
    callbackSuccess: (ArrayList<PlacePrediction>) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "${googleapisUrl}place/autocomplete/json?input=$input&key=${Constants.GOOGLE_API_KEY}"

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
                val predictions = jsonResponse.optJSONArray("predictions")

                if (predictions != null && predictions.length() > 0) {
                    val predictionsList = ArrayList<PlacePrediction>()
                    for (i in 0 until predictions.length()) {
                        val prediction = predictions.getJSONObject(i)
                        predictionsList.add(
                            PlacePrediction(
                                placeId = prediction.optString("place_id"),
                                description = prediction.optString("description")
                            )
                        )
                    }
                    callbackSuccess(predictionsList)
                } else {
                    callbackError(IOException("No results found"))
                }
            }
        }
    })

}

fun getPlaceDetails(
    placeId: String,
    callbackSuccess: (UserLocation) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "${googleapisUrl}place/details/json?place_id=$placeId&key=${Constants.GOOGLE_API_KEY}&language=es"

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

                val result = jsonResponse.optJSONObject("result")
                val geometry = result?.optJSONObject("geometry")
                val location = geometry?.optJSONObject("location")

                callbackSuccess(
                    UserLocation(
                        latitude = location?.optDouble("lat"),
                        longitude = location?.optDouble("lng")
                    )
                )

            }
        }
    })
}


fun getShortAddress(inputString: String?): String {
    if (inputString.isNullOrEmpty()) return ""
    val parts = inputString.split(",")
    val addressStreet = parts[0]
    val addressComplement = parts.getOrNull(1)
    return addressStreet + if (addressComplement != null) ", $addressComplement" else ""
}

fun getStreetAddress(inputString: String?): String {
    if (inputString.isNullOrEmpty()) return ""
    return inputString.split(",")[0]
}

fun getComplementAddress(inputString: String?): String {
    if (inputString.isNullOrEmpty()) return ""
    val commaIndex = inputString.indexOf(',')
    return inputString.substring(commaIndex + 1).trim()
}



