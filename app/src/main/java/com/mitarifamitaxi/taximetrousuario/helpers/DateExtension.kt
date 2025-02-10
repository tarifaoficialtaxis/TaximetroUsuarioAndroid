package com.mitarifamitaxi.taximetrousuario.helpers

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


fun tripCardFormatDate(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("E d MMM • h:mm a", Locale("es", "CO"))
    val instant = Instant.parse(dateString)
    val localDateTime =
        instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

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

fun tripSummaryFormatDate(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("E d MMM • yyyy", Locale("es", "CO"))
    val instant = Instant.parse(dateString)
    val localDateTime =
        instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    return localDateTime.format(formatter)
        .replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale(
                    "es",
                    "ES"
                )
            ) else it.toString()
        }
}

fun hourFormatDate(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale("es", "CO"))
    val instant = Instant.parse(dateString)
    val localDateTime =
        instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    return localDateTime.format(formatter)
        .replace("a. m.", "AM")
        .replace("p. m.", "PM")
}

fun shareFormatDate(dateString: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = formatter.parse(dateString)
    return date?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) } ?: ""
}
