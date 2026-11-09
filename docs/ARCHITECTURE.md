# Arquitectura

Un módulo Gradle `app` evita complejidad temprana. `MainActivity` es el punto de
entrada HOME y LAUNCHER (singleTask); un nuevo intent HOME regresa a inicio.
La navegación local Compose conserva pantalla/búsqueda durante recreación y
Back vuelve a inicio. No hay servicios en segundo plano ni acceso a red.

`feature/home/HomeViewModel` combina repositorios, reloj y errores en StateFlow;
Compose recoge con ciclo de vida. El reloj solo corre con suscriptores y se
actualiza con la hora local del dispositivo. `core/data/AppsRepository` consulta
MAIN/LAUNCHER en Dispatchers.IO, guarda identificadores ComponentName y abre la
actividad concreta. Se excluye Neobash y se refresca al reanudar; una apertura
fallida informa y recarga. Visibilidad declarada mediante queries de intent,
sin QUERY_ALL_PACKAGES. Primera versión limitada al perfil de usuario actual.

`PreferencesRepository` guarda cuatro slots y tema en Preferences DataStore.
Los favoritos desaparecidos mantienen su referencia para poder recuperarse
tras reinstalar; se muestran como no disponibles y permiten reasignación.
No se persisten iconos ni se cargan imágenes de paquetes: se usan marcas lineales
provisionales de slot. La lista muestra nombre y paquete para desambiguar.

`core/model/CelestialProvider` permite sustituir el proveedor simulado por cálculos
locales. Horarios fijos 06:30–18:45, fase 0.35, clima fijo 24°C/UV 3; no son
observaciones reales. Canvas representa progreso solar y disco lunar mediante
limbo y terminador elíptico. Día/noche se decide por hora local o vista manual.
La orientación lunar aparente por ubicación/hemisferio queda pendiente; creciente
se ilumina a la derecha. La preferencia de vista astronómica es temporal.

`core/designsystem` define ambos esquemas. `feature/apps`, `feature/settings` y
`feature/celestial` aíslan UI. Inyección manual en ViewModel; sin framework DI.

## Futuro sistema de notas (sin implementar)

Crear un feature o módulo independiente con repositorio local y modelos de
Documento, BloqueTexto, Trazo y ClipAudio; Room para metadatos/documentos e
archivos privados para audio y vectores. Trazo preservará puntos con tiempo,
presión, inclinación y herramienta; el capturador de stylus distinguirá palma
y lápiz según eventos y capacidades del hardware. Un documento híbrido podrá
combinar bloques sin acoplarse a favoritos o PackageManager. Exportación mediante
URI/Storage Access Framework; grabación solo tras acción explícita y permiso
contextual. Ninguno de estos permisos o editores se introduce ahora.
