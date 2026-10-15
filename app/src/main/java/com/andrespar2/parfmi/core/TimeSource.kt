package com.andrespar2.parfmi.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Fuente de tiempo inyectable para poder probar la app con fechas fijas.
 */
fun interface TimeSource {
    fun nowMillis(): Long

    companion object {
        val System: TimeSource = TimeSource { java.lang.System.currentTimeMillis() }
    }
}

/** Formato de fecha y hora usado en toda la aplicacion. */
object TimestampFormatter {

    const val PATTERN = "dd/MM/yyyy HH:mm:ss"

    fun format(millis: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(PATTERN, Locale.getDefault())
        formatter.timeZone = zone
        return formatter.format(Date(millis))
    }

    /** Formato compacto para resumenes compartidos. */
    fun formatShort(millis: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.timeZone = zone
        return formatter.format(Date(millis))
    }
}
