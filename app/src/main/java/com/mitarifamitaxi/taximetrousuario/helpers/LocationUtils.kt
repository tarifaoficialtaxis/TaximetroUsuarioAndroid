package com.mitarifamitaxi.taximetrousuario.helpers

import com.google.android.gms.maps.model.LatLng
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
    latitude: Double,
    longitude: Double,
    radius: Int = 50000,
    callbackSuccess: (ArrayList<PlacePrediction>) -> Unit,
    callbackError: (Exception) -> Unit
) {
    val url =
        "${googleapisUrl}place/autocomplete/json?input=$input&location=$latitude,$longitude&radius=$radius&language=es&key=${Constants.GOOGLE_API_KEY}"

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

fun fetchRoute(
    originLatitude: Double,
    originLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double,
    callbackSuccess: (List<LatLng>) -> Unit,
    callbackError: (Exception) -> Unit
) {

    val origin = "${originLatitude},${originLongitude}"
    val destination = "${destinationLatitude},${destinationLongitude}"


    val url =
        "${googleapisUrl}directions/json?origin=$origin&destination=$destination&key=${Constants.GOOGLE_API_KEY}"

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

                val routes = jsonResponse.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    val overviewPolyline = routes.getJSONObject(0).getJSONObject("overview_polyline")
                    val points = overviewPolyline.getString("points")
                    val decodedPoints = decodePolyline(points)
                    callbackSuccess(decodedPoints)
                } else {
                    callbackError(IOException("No routes found"))
                }
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

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return poly
}




