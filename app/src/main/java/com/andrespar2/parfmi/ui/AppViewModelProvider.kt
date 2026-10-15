package com.andrespar2.parfmi.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrespar2.parfmi.ParFmiApplication
import com.andrespar2.parfmi.ui.lookup.LookupViewModel
import com.andrespar2.parfmi.ui.settings.SettingsViewModel

/**
 * Fabrica de ViewModels conectada al [ParFmiApplication].
 *
 * Permite construir los ViewModels con sus dependencias reales sin librerias
 * de inyeccion.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            LookupViewModel(
                lookupFmi = parfmiApplication().container.lookupFmiUseCase,
                settingsRepository = parfmiApplication().container.settingsRepository,
            )
        }
        initializer {
            SettingsViewModel(
                settingsRepository = parfmiApplication().container.settingsRepository,
                lookupFmi = parfmiApplication().container.lookupFmiUseCase,
            )
        }
    }
}

private fun CreationExtras.parfmiApplication(): ParFmiApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ParFmiApplication
