# Changelog

Todos los cambios relevantes de ParFMI se documentan en este archivo.
El formato sigue, de forma informal, [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).

## [1.0.0] - 2026-10-15

Primera versión publicada.

### Añadido

- Consulta del estado FMI y Activation Lock por IMEI o número de serie.
- Validación de IMEI (longitud + dígito de control Luhn) y de números de serie, con detección en vivo del tipo de identificador.
- Informe por secciones: dispositivo, Find My iPhone, activación y bloqueos, cobertura y garantía, red y operador, y datos de la consulta.
- Estados tri-valorados estrictos: un dato ausente se muestra como "Desconocido" o "No disponible", nunca como OFF ni desbloqueado.
- Espacio para campos adicionales devueltos por el proveedor.
- Copiar el resultado y compartir un resumen en texto.
- Visualización opcional de la respuesta original del proveedor.
- Integración con Sickw.com mediante su API documentada, con catálogo de servicios incluido y actualizable.
- Almacenamiento cifrado de la API key con Android Keystore (AES-GCM).
- Pantalla de Ajustes (proveedor, API key, servicio, catálogo) y pantalla Acerca de con créditos.
- Tema "Black Modern AMOLED Red": negro puro, superficies oscuras, acento rojo y colores de estado semánticos.
- Icono adaptativo original en estilo AMOLED rojo.
- Pruebas unitarias de la lógica crítica (validación, parseo, mapeo, red, ajustes y ViewModels).

### Seguridad y privacidad

- Sin historial de consultas almacenado.
- Sin registro de IMEI/SN en logs ni envío a terceros distintos del proveedor configurado.
- `allowBackup=false` y permisos mínimos (`INTERNET`, `ACCESS_NETWORK_STATE`).

### Alcance

- Aplicación de solo consulta: no omite ni desactiva ninguna protección.

[1.0.0]: https://github.com/andrespar2/ParFMI/releases/tag/v1.0.0
