# ParFMI

**Consulta el estado de Find My iPhone (FMI) y de otros indicadores de seguridad de dispositivos Apple** a partir de su IMEI o número de serie.

ParFMI es una aplicación Android nativa (Kotlin + Jetpack Compose) de **solo consulta**: verifica si un dispositivo tiene FMI / Activation Lock activos, su estado de activación, cobertura, operador y otros datos útiles antes de comprar o de diagnosticar un equipo. **No** desbloquea, no elimina y no desactiva ninguna protección.

> Desarrollado por **andrespar2** — <https://github.com/andrespar2>

---

## Índice

- [Características](#características)
- [Diseño](#diseño)
- [Capturas](#capturas)
- [Descarga e instalación](#descarga-e-instalación)
- [Uso](#uso)
- [Configuración del proveedor](#configuración-del-proveedor)
- [Privacidad y seguridad](#privacidad-y-seguridad)
- [Datos que muestra](#datos-que-muestra)
- [Arquitectura](#arquitectura)
- [Compilar desde el código fuente](#compilar-desde-el-código-fuente)
- [Pruebas](#pruebas)
- [Alcance y limitaciones](#alcance-y-limitaciones)
- [Créditos y licencia](#créditos-y-licencia)

---

## Características

- **Consulta por IMEI o número de serie (SN)** en una sola pantalla.
- **Validación de la entrada**: los IMEI se comprueban con el dígito de control (Luhn) y con la longitud; los números de serie con un formato permisivo pero razonable. La app detecta en vivo si lo introducido parece un IMEI o un SN.
- **Estado de FMI y Activation Lock** mostrados por separado, junto al estado de activación, iCloud, lista de bloqueo, MDM y bloqueo de operador (SIM Lock).
- **Datos de dispositivo**: fabricante, modelo, nombre e identificador del modelo, familia de producto, color, capacidad y número de pieza (MPN).
- **Cobertura y garantía**, fechas de compra/activación y datos de red/operador cuando el proveedor los aporta.
- **Campos adicionales** devueltos por el proveedor, mostrados de forma cruda y ordenada.
- **Tri-estado estricto**: cada indicador puede ser ON / OFF / **Desconocido**. Un dato ausente **nunca** se presenta como OFF ni como "desbloqueado".
- **Copiar y compartir** un resumen en texto del resultado.
- **Respuesta original del proveedor** disponible bajo demanda (colapsada por defecto).
- **Tema Black Modern AMOLED Red** con negro puro, alto contraste y colores de estado semánticos.
- **Sin publicidad, sin analítica y sin rastreo.**

## Diseño

- Fondo negro puro `#000000` y superficies `#101010` / `#161616` (pensado para pantallas AMOLED).
- Acento rojo `#FF1744`, texto principal blanco y secundario gris.
- Bordes finos, tarjetas modernas y animaciones discretas.
- Colores de estado semánticos: verde = despejado, rojo = bloqueado, ámbar = desconocido.
- Orientación vertical forzada para una lectura cómoda.

## Capturas

_No se incluyen capturas en el repositorio para no inventar contenido. Ejecuta la app para ver la interfaz real._

## Descarga e instalación

1. Descarga el APK desde la página de Releases:

   - Página de descargas: <https://github.com/andrespar2/ParFMI/releases>
   - APK de la versión 1.0.0: <https://github.com/andrespar2/ParFMI/releases/download/v1.0.0/ParFMI-v1.0.0.apk>

2. En el teléfono, permite la instalación desde orígenes desconocidos para el navegador o el explorador de archivos.
3. Abre el APK y confirma la instalación.

**Requisitos:** Android 7.0 (API 24) o superior.

> **Aviso sobre la firma.** El APK publicado está firmado con una **clave de prueba local** generada por el autor para poder instalarse, no con una clave de producción de Google Play. Es perfectamente instalable, pero si más adelante se publica en Google Play habrá que firmarlo con la clave definitiva (y el `applicationId` de las actualizaciones debe coincidir).

## Uso

1. Abre ParFMI y acepta que es una herramienta de consulta.
2. Escribe un **IMEI** (15 dígitos) o un **número de serie** de Apple.
   - La app indica bajo el campo si lo detecta como IMEI o como número de serie.
3. Pulsa **Consultar estado**.
4. Revisa el informe por secciones (Dispositivo, Find My iPhone, Activación y bloqueos, Cobertura, Red y datos de la consulta).
5. Puedes **copiar** o **compartir** el resumen, y desplegar la **respuesta original** del proveedor.

Si aún no has configurado una API de proveedor, la app te lo indicará y **no mostrará ningún dato inventado**.

## Configuración del proveedor

ParFMI obtiene los datos de **un proveedor autorizado que el usuario configura con su propia clave**. Actualmente se integra [Sickw.com](https://sickw.com):

1. Crea una cuenta y obtén tu **API key** en Sickw.com.
2. En ParFMI, abre **Ajustes**.
3. Pega la API key y pulsa **Guardar**. Se almacena **cifrada** con Android Keystore (AES-GCM).
4. Elige el **servicio** a consultar (por ejemplo *"Apple: operador + MDM, iCloud y GSX"*) o introduce su ID numérico.
5. Opcionalmente, pulsa **Actualizar catálogo desde Sickw** para traer la lista de servicios en vivo.

En [`docs/PROVEEDORES.md`](docs/PROVEEDORES.md) se explica en detalle la integración, el formato de la API y cómo añadir otro proveedor.

## Privacidad y seguridad

- **La API key nunca se guarda en claro**: se cifra con una clave custodiada por Android Keystore.
- **No se almacena historial de consultas** ni los IMEI/SN en el dispositivo.
- **Los identificadores no se registran en logs** ni se envían a servicios de analítica o de informes de fallos. Únicamente viajan al proveedor que el usuario configura, por **HTTPS**.
- **Sin permisos innecesarios**: solo `INTERNET` y `ACCESS_NETWORK_STATE`.
- **`allowBackup=false`**: los datos de la app no se incluyen en las copias de seguridad.
- **Sin funciones de elusión**: la app es estrictamente de lectura.

## Datos que muestra

| Sección | Campos |
|---|---|
| Dispositivo | IMEI, número de serie, fabricante, modelo, nombre del modelo, identificador del modelo, familia de producto, color, capacidad, MPN |
| Find My iPhone | Estado FMI (ON/OFF/Desconocido), Activation Lock (Bloqueado/Desbloqueado/Desconocido) |
| Activación y bloqueos | Estado de activación, estado de iCloud, lista de bloqueo, MDM, bloqueo de operador |
| Cobertura y garantía | Cobertura, fecha de compra/activación, soporte técnico, AppleCare |
| Red y operador | Operador, país y región |
| Datos de la consulta | Tipo e identificador consultado, fecha y hora, proveedor, servicio, saldo |
| Adicional | Cualquier campo extra devuelto por el proveedor |

Los campos que el proveedor no devuelva se muestran como **"No disponible"** o **"Desconocido"**, nunca con un valor inventado.

## Arquitectura

- **Kotlin + Jetpack Compose + Material 3** siguiendo MVVM.
- Capa `core`: validación (`ImeiValidator`, `SerialNumberValidator`), utilidades (`Redactor`, `TextNormalizer`, `TimestampFormatter`).
- Capa `data`:
  - `model`: informe (`FmiReport`) y estados tri-valorados.
  - `net`: motor HTTP sobre OkHttp, mapeo de errores de red, comprobación de conectividad.
  - `settings`: preferencias y **almacenamiento cifrado** de la API key.
  - `provider/sickw`: integración real con la API de Sickw (URL, parser JSON, mapeo de campos).
  - `LookupFmiUseCase`: validación previa + delegación en el proveedor.
- Capa `ui`: tema, componentes reutilizables, pantallas (Consulta, Resultado, Ajustes, Acerca de), ViewModels y navegación.
- **Inyección de dependencias manual** (`AppContainer`) para mantener el proyecto ligero y explícito.

## Compilar desde el código fuente

Requisitos: **JDK 17** y **Android SDK** (platform 35, build-tools 35.0.0).

```powershell
# Compilar el APK de depuración
.\tools\build.ps1

# Compilar el APK de release (usa keystore.properties si existe)
.\tools\build.ps1 -Task assembleRelease

# Ejecutar las pruebas unitarias
.\tools\build.ps1 -Task testDebugUnitTest
```

O directamente con el wrapper de Gradle:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Para firmar el release, crea en la raíz un `keystore.properties` (ignorado por Git):

```properties
storeFile=ruta/a/tu-keystore.jks
storePassword=********
keyAlias=********
keyPassword=********
```

Si no existe, el release se firma con la clave de depuración (instalable, no apta para producción).

## Pruebas

Las pruebas unitarias se ejecutan en la JVM con JUnit4 y `MockWebServer`:

```bash
./gradlew testDebugUnitTest
```

Cubren validación de IMEI/SN, enmascarado de datos sensibles, parseo y mapeo de la respuesta del proveedor, integración con el proveedor contra un servidor simulado, almacenamiento cifrado de ajustes, el catálogo de servicios y los ViewModels de consulta y ajustes.

## Alcance y limitaciones

- ParFMI es **exclusivamente de consulta**. No omite, desbloquea ni desactiva FMI ni Activation Lock.
- Los resultados **dependen por completo del proveedor configurado**. Si un dato no está disponible, la app lo indica y no lo inventa.
- Una consulta real requiere una **API key válida y con saldo** del proveedor.
- Los campos genéricos devueltos por el proveedor se muestran tal cual en "Información adicional"; no se interpretan más allá de los indicadores conocidos.
- ParFMI **no está afiliada a Apple Inc.** Apple, iPhone y Find My son marcas de Apple Inc. y se mencionan solo con fines descriptivos.

## Créditos y licencia

- **Desarrollo, diseño y documentación:** andrespar2.
- **Datos:** proporcionados por el proveedor de consultas que configura el usuario.
- **Licencia:** [MIT](LICENSE).

Repositorio: <https://github.com/andrespar2/ParFMI>
