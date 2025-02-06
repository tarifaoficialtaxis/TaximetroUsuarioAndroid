
package com.mitarifamitaxi.taximetrousuario.helpers

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


fun tripCardFormatDate(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("E d MMM • h:mm a", Locale("es", "CO"))
    val instant = Instant.parse(dateString)
    val localDateTime =
        instant.atZone(ZoneId.systemDefault()).toLocalDateTime() // Convert to local time

    return localDateTime.format(formatter)
        .replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale(
                    "es",
                    "ES"
                )
            ) else it.toString()
        }
        .replace("a. m.", "AM")
        .replace("p. m.", "PM")
}

