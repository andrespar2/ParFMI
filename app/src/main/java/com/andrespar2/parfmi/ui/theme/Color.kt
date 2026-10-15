package com.andrespar2.parfmi.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta "Black Modern AMOLED Red" de ParFMI.
 *
 * Los fondos son negro puro (#000000) para aprovechar las pantallas AMOLED y
 * reducir el consumo; el acento es un rojo de alto contraste y el texto
 * principal es blanco. Los colores de estado son semanticos y nunca se usan
 * para decorar: verde = despejado, rojo = bloqueado, ambar = desconocido.
 */
object ParfmiColors {

    // Fondos
    val AmoledBlack = Color(0xFF000000)
    val Surface = Color(0xFF101010)
    val SurfaceAlt = Color(0xFF161616)
    val SurfaceElevated = Color(0xFF1C1C1C)

    // Acento
    val Red = Color(0xFFFF1744)
    val RedDark = Color(0xFFB80E2E)

    // Texto
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF9E9E9E)
    val TextTertiary = Color(0xFF6E6E6E)

    // Bordes finos
    val Border = Color(0xFF262626)
    val BorderStrong = Color(0xFF3A3A3A)

    // Estado semantico
    val StatusPositive = Color(0xFF00E676)
    val StatusNegative = Color(0xFFFF1744)
    val StatusUnknown = Color(0xFFFFB300)
    val Info = Color(0xFF40C4FF)
}
