package com.andrespar2.parfmi.ui.lookup

import com.andrespar2.parfmi.data.model.LookupOutcome

/**
 * Estado del flujo de consulta.
 *
 * [Done] encapsula tanto el exito como el fallo para que la pantalla de
 * resultados pueda mostrar la causa real sin inventar datos.
 */
sealed interface LookupStatus {

    /** Sin consulta en curso: se muestra el formulario. */
    data object Idle : LookupStatus

    /** Consulta en curso contra el proveedor. */
    data class Loading(val serviceName: String) : LookupStatus

    /** Consulta terminada, con exito o con error. */
    data class Done(val outcome: LookupOutcome) : LookupStatus
}
