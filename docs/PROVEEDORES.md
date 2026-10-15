# Proveedores de datos

ParFMI **no incluye datos de dispositivos**: obtiene los resultados de un proveedor externo autorizado que el propio usuario configura con su clave. Esto mantiene la app ligera, legal y transparente sobre el origen de los datos.

Actualmente está integrado **Sickw.com**. La arquitectura permite añadir otros proveedores sin tocar la interfaz.

---

## Proveedor integrado: Sickw.com

### Endpoint

Sickw expone un único endpoint documentado:

```
https://sickw.com/api.php?format=json&key=API_KEY&imei=IMEI_O_SN&service=ID
```

| Parámetro | Descripción |
|---|---|
| `format` | `json` para obtener una respuesta estructurada. |
| `key` | La API key del usuario (nunca se escribe en logs). |
| `imei` | El IMEI (15 dígitos) **o** el número de serie del dispositivo. |
| `service` | El ID numérico del servicio a consultar. |

Si se omite el identificador, el proveedor devuelve la lista de servicios disponibles (se usa para el botón **Actualizar catálogo**).

### Formato de respuesta

```json
{
  "result": { "Find My iPhone": "ON", "Model Name": "iPhone 13" },
  "imei": "356938035643809",
  "balance": "3.42 USD",
  "price": "0.40 USD",
  "service": "72",
  "id": "abc123",
  "status": "success"
}
```

- Si todo va bien, `result` es un **objeto** con pares etiqueta/valor.
- Si hay un aviso o un error, `result` puede ser un **texto** explicativo (por ejemplo, saldo insuficiente o servicio no válido); la app lo clasifica en una categoría de error legible.
- El proveedor puede devolver `status` / `id` en la respuesta.

### Cómo se interpreta la respuesta

1. `SickwJsonParser` convierte el cuerpo en un `SickwPayload` (campos, saldo, estado) o detecta el mensaje de error / catálogo.
2. `SickwFieldMapper` clasifica cada etiqueta (FMI, Activation Lock, modelo, operador, cobertura...) y normaliza su valor.
3. Los valores se traducen a **estados tri-valorados** (`ON` / `OFF` / `UNKNOWN`, etc.). Cualquier valor no reconocido queda en **Desconocido**, nunca en OFF.
4. Las etiquetas no reconocidas se conservan en **Información adicional del proveedor** sin interpretarlas.

### Configuración en la app

1. **Ajustes → Proveedor de datos → API key del proveedor**: pega tu clave y guarda.
   - Se cifra con **AES-GCM** usando una clave de Android Keystore. No se guarda en claro ni se sube al repositorio.
2. **Servicio a consultar**: elige uno del catálogo incluido (con IDs y precios reales publicados por Sickw) o introduce un ID numérico.
3. **Actualizar catálogo desde Sickw** (opcional): descarga la lista de servicios en vivo. Si falla, se mantiene el catálogo incluido.

### Catálogo incluido

`SickwServiceCatalog` contiene los IDs reales de los servicios relevantes para Apple, con su precio publicado como referencia (por ejemplo `72`, `3`, `40`, `61`, `66`, `101`, `152`, `6`, `54`, `81`, entre otros). El servicio por defecto es el `72`.

> Los precios y la disponibilidad los fija Sickw y pueden cambiar; la app solo los muestra como referencia.

---

## Añadir otro proveedor

La interfaz de la app depende de la abstracción `FmiProvider`:

```kotlin
interface FmiProvider {
    val id: String
    val displayName: String
    suspend fun lookup(request: LookupRequest): LookupOutcome
    suspend fun fetchServices(apiKey: String): ServiceCatalogResult
}
```

Pasos para integrar un proveedor nuevo:

1. Crea un paquete en `data/provider/<tuproveedor>/`.
2. Implementa `FmiProvider`:
   - `lookup`: construye la petición y devuelve `LookupOutcome.Success(FmiReport)` o `LookupOutcome.Failure(ProviderError)`.
   - `fetchServices`: devuelve el catálogo de servicios si el proveedor lo ofrece.
   - Usa `HttpEngine` (OkHttp) para las peticiones y **no registres** el identificador ni la clave.
3. Mapea la respuesta con un `FieldMapper` propio hacia `FmiReport`, aplicando la regla de oro: **si no hay dato, es `UNKNOWN`**.
4. Registra el proveedor en `di/AppContainer` y expón su catálogo en el `SettingsRepository`.

### Reglas que debe cumplir cualquier proveedor

- **HTTPS** obligatorio.
- **Nunca** inventar ni estimar valores: los campos ausentes quedan vacíos/desconocidos.
- **Nunca** escribir el IMEI/SN ni la API key en logs, analítica o informes de fallos.
- **Sin funciones de elusión**: el proveedor solo aporta datos de consulta.

---

## Nota legal

Consulta únicamente proveedores a los que tengas acceso autorizado y respeta sus términos de servicio y la legislación aplicable. ParFMI es una herramienta de verificación previa a la compra y de diagnóstico; no presta soporte para eludir bloqueos.
