package com.mitarifamitaxi.taximetrousuario.components.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.Trip

@Composable
fun TripItem(trip: Trip) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Use elevation for shadow
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ) {
            Text("Destination: ${trip.endAddress}", fontWeight = FontWeight.Bold)
            Text("Start: ${trip.startHour}")
            Text("End: ${trip.endHour}")
        }
    }
}