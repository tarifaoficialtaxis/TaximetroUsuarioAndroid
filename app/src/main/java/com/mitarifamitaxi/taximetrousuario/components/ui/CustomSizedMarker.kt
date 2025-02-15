package com.mitarifamitaxi.taximetrousuario.components.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.LatLng

@Composable
fun CustomSizedMarker(position: LatLng, drawableRes: Int, width: Int, height: Int) {
    val context = LocalContext.current
    val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
    val markerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

    Marker(
        state = MarkerState(position = position),
        icon = markerIcon
    )
}

