package com.andrespar2.parfmi

import android.app.Application
import com.andrespar2.parfmi.di.AppContainer

/**
 * Punto de entrada de la aplicacion: crea el contenedor de dependencias.
 *
 * Se mantiene minimo a proposito; toda la logica vive en las capas `data` y
 * `ui`, y aqui solo se compone.
 */
class ParFmiApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
