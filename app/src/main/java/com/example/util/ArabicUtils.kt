package com.example.util

import java.util.Locale

/**
 * Converts English digits (0-9) to Arabic-Indic digits (٠-٩).
 */
fun String.toArabicDigits(): String {
    val builder = StringBuilder(this.length)
    for (char in this) {
        when (char) {
            '0' -> builder.append('٠')
            '1' -> builder.append('١')
            '2' -> builder.append('٢')
            '3' -> builder.append('٣')
            '4' -> builder.append('٤')
            '5' -> builder.append('٥')
            '6' -> builder.append('٦')
            '7' -> builder.append('٧')
            '8' -> builder.append('٨')
            '9' -> builder.append('٩')
            else -> builder.append(char)
        }
    }
    return builder.toString()
}

fun Int.toArabicDigits(): String = this.toString().toArabicDigits()
fun Long.toArabicDigits(): String = this.toString().toArabicDigits()

/**
 * Formats minutes from midnight into 12-hour Arabic string, e.g. "٥:٣٠ ص" or "٦:٣٠ م"
 */
fun formatMinutesToTime(minutes: Int): String {
    val normalized = ((minutes % 1440) + 1440) % 1440
    val hour24 = normalized / 60
    val minute = normalized % 60
    val isPm = hour24 >= 12
    val hour12 = when (hour24) {
        0 -> 12
        in 1..12 -> hour24
        else -> hour24 - 12
    }
    val period = if (isPm) "م" else "ص"
    val timeStr = String.format(Locale.US, "%d:%02d", hour12, minute).toArabicDigits()
    return "$timeStr $period"
}

fun formatTimeRange(startMinutes: Int, endMinutes: Int): String {
    return "من ${formatMinutesToTime(startMinutes)} إلى ${formatMinutesToTime(endMinutes)}"
}

fun formatDurationHoursMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainingMins = minutes % 60
    return when {
        hours > 0 && remainingMins > 0 -> "${hours.toArabicDigits()} س و${remainingMins.toArabicDigits()} د"
        hours > 0 -> "${hours.toArabicDigits()} س"
        else -> "${remainingMins.toArabicDigits()} دقيقة"
    }
}
