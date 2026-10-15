package com.andrespar2.parfmi.ui.settings

/** Avisos que la pantalla de ajustes muestra al usuario. */
sealed interface SettingsMessage {
    data object Saved : SettingsMessage
    data object KeyCleared : SettingsMessage
    data class CatalogUpdated(val count: Int) : SettingsMessage
    data object CatalogFailed : SettingsMessage
    data object CatalogReset : SettingsMessage
}
