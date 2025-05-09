package com.mitarifamitaxi.taximetrousuario.helpers

import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
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
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a", Locale("es", "CO"))
    val instant = Instant.parse(dateString)
    val localDateTime =
        instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    return localDateTime.format(formatter)
        .replace("a. m.", "AM")
        .replace("p. m.", "PM")
}

private fun getColombianHolidays(year: Int): List<LocalDate> {
    // Fixed-date holidays
    val fixedHolidays = listOf(
        LocalDate.of(year, 1, 1),   // Año Nuevo
        LocalDate.of(year, 5, 1),   // Día del Trabajo
        LocalDate.of(year, 7, 20),  // Día de la Independencia
        LocalDate.of(year, 8, 7),   // Batalla de Boyacá
        LocalDate.of(year, 12, 8),  // Inmaculada Concepción
        LocalDate.of(year, 12, 25)  // Navidad
    )

    // Movable holidays (Ley Emiliani)
    val movableHolidays = listOf(
        getNextMonday(LocalDate.of(year, 1, 6)),   // Reyes Magos
        getNextMonday(LocalDate.of(year, 3, 19)),  // San José
        getNextMonday(LocalDate.of(year, 6, 29)),  // San Pedro y San Pablo
        getNextMonday(LocalDate.of(year, 8, 15)),  // Asunción de la Virgen
        getNextMonday(LocalDate.of(year, 10, 12)), // Día de la Raza
        getNextMonday(LocalDate.of(year, 11, 1)),  // Todos los Santos
        getNextMonday(LocalDate.of(year, 11, 11))  // Independencia de Cartagena
    )

    // Easter-based holidays
    val easterSunday = getEasterSunday(year)
    val easterRelatedHolidays = listOf(
        easterSunday.minusDays(3),                  // Jueves Santo
        easterSunday.minusDays(2),                  // Viernes Santo
        getNextMonday(easterSunday.plusDays(43)),   // Ascensión del Señor
        getNextMonday(easterSunday.plusDays(64)),   // Corpus Christi
        getNextMonday(easterSunday.plusDays(71))    // Sagrado Corazón
    )

    return fixedHolidays + movableHolidays + easterRelatedHolidays
}

// Check if a date is a Colombian holiday (for current and next year)
fun isColombianHoliday(): Boolean {

    val date = LocalDate.now()

    val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY

    if (isSunday) {
        return true
    }

    val currentYearHolidays = getColombianHolidays(date.year)
    val nextYearHolidays =
        getColombianHolidays(date.year + 1) // In case we're checking near end of year
    return date in currentYearHolidays || date in nextYearHolidays
}

// Move holidays to next Monday if not already on Monday
private fun getNextMonday(date: LocalDate): LocalDate {
    return if (date.dayOfWeek == DayOfWeek.MONDAY) date
    else date.plusDays((DayOfWeek.MONDAY.value - date.dayOfWeek.value + 7) % 7L)
}

// Compute Easter Sunday
private fun getEasterSunday(year: Int): LocalDate {
    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = ((h + l - 7 * m + 114) % 31) + 1
    return LocalDate.of(year, month, day)
}

fun isNightTime(
    nightHour: Int = 21,
    nightMinute: Int = 0,
    morningHour: Int = 5,
    morningMinute: Int = 30
): Boolean {
    val now = LocalTime.now()
    val start = LocalTime.of(nightHour, nightMinute)
    val end = LocalTime.of(morningHour, morningMinute)

    return (now >= start) || (now <= end)
}
