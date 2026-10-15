package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.data.model.ProviderService

/**
 * Catalogo de servicios de Sickw incluido en la app.
 *
 * Los IDs y los nombres son los publicados por Sickw.com en su lista de
 * servicios (seccion "Apple Check Services" y servicios de estado). Se
 * incluyen solo los servicios relevantes para consultar el estado de un
 * dispositivo Apple. El catalogo se puede actualizar desde la propia API en
 * Ajustes; si eso falla, este catalogo se mantiene como respaldo.
 *
 * El precio es el publicado por el proveedor y puede cambiar: la app lo
 * muestra solo como referencia.
 */
object SickwServiceCatalog {

    const val PROVIDER_ID = "sickw"
    const val PROVIDER_NAME = "Sickw.com"
    const val BASE_URL = "https://sickw.com/api.php"

    /** Servicio preseleccionado: el mas completo para FMI y bloqueos. */
    const val DEFAULT_SERVICE_ID = "72"

    val services: List<ProviderService> = listOf(
        ProviderService(
            id = "72",
            name = "Apple: operador + MDM, iCloud y GSX",
            price = "0.40 USD",
            defaultService = true,
        ),
        ProviderService("3", "iCloud / FMI ON-OFF", "0.02 USD"),
        ProviderService("40", "Apple: MDM y estado iCloud", "0.24 USD"),
        ProviderService("61", "iPhone: operador, FMI y lista de bloqueo", "0.10 USD"),
        ProviderService("78", "iPhone: operador y FMI", "0.08 USD"),
        ProviderService("66", "iPhone y Mac: estado iCloud CLEAN/LOST", "0.22 USD"),
        ProviderService("101", "Apple: estado de activacion", "0.03 USD"),
        ProviderService("88", "Apple: estado de activacion (PRO)", "0.04 USD"),
        ProviderService("152", "Apple: garantia por numero de serie", "0.03 USD"),
        ProviderService("26", "Apple: informacion por numero de serie", "0.01 USD"),
        ProviderService("92", "iPhone: modelo, color y capacidad", "0.022 USD"),
        ProviderService("203", "Marca y modelo", "0.004 USD"),
        ProviderService("63", "Apple GSX: detalles completos", "1.80 USD"),
        ProviderService("146", "Apple GSX: detalles (promo)", "0.80 USD"),
        ProviderService("105", "Apple: vendido por y pais", "1.60 USD"),
        ProviderService("60", "Apple: pista del ID de iCloud", "0.40 USD"),
        ProviderService("219", "Apple: numero de pieza (MPN)", "0.15 USD"),
        ProviderService("12", "Convertir IMEI a numero de serie", "0.025 USD"),
        ProviderService("103", "iPhone: operador", "0.06 USD"),
        ProviderService("6", "Lista de bloqueo mundial (PRO)", "0.12 USD"),
        ProviderService("54", "Lista de bloqueo mundial", "0.04 USD"),
        ProviderService("81", "Apple: estado MDM", "0.30 USD"),
        ProviderService("29", "Apple: historial de reemplazos", "0.70 USD"),
        ProviderService("68", "Apple GSX: casos y reparaciones", "1.10 USD"),
        ProviderService("77", "Apple GSX: elegibilidad de reparacion", "0.14 USD"),
        ProviderService("85", "Apple: dispositivos de demostracion", "0.20 USD"),
        ProviderService("110", "MacBook e iMac: estado iCloud ON/OFF", "0.20 USD"),
        ProviderService("147", "iPhone: operador, FMI y lista de bloqueo (S2)", "0.25 USD"),
        ProviderService("151", "iPhone: operador y deteccion CHIMAERA", "0.12 USD"),
    )

    fun findById(serviceId: String): ProviderService? =
        services.firstOrNull { it.id == serviceId }
}
