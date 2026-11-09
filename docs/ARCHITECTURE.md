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
Los favoritos conservan marcas lineales de slot. El cajón muestra nombre e icono
original sin paquetes visibles. `AppIconsRepository` carga en IO y mantiene una
caché LRU de 4 MiB por componente/revisión/tamaño/configuración; sin caché en disco.

`core/model/Celestial.kt` separa proveedor, efemérides independientes de Sol/Luna,
progreso por tránsito y geometría de semicírculo. `CelestialCycle` representa ambos
sobre un único arco circular estilizado. La procedencia sigue explícitamente
simulada. Ver contratos, convenciones y limitaciones en [Iteración 0.2](ITERATION_0.2.md).
La navegación principal es un HorizontalPager Herramientas–Home–Apps, sin barra
inferior. Opciones del escritorio y acciones semánticas ofrecen rutas accesibles. Ajustes y selección de favorito conservan sus rutas.
El menú contextual es un ModalBottomSheet desde el engranaje o una zona vacía.
Home habilita scroll solo ante overflow medido; `feature/tools` prepara el panel
para evolucionar a productividad. Ver [Iteración 0.2.1](ITERATION_0.2.1.md).

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

## Consolidación 0.2.2

El catálogo vive en AppsRepository, retenido por HomeViewModel; el descubrimiento
inicial y los cambios incrementales de paquetes son sus únicos productores.
El ciclo de vida de Activity/HOME no provoca consultas completas. Un receiver
liberado con el ViewModel agrupa eventos de paquetes y serializa invalidaciones.
PreferencesRepository conserva las claves anteriores y añade distribución y
reducción de movimiento, con movimientos/restauración atómicos en DataStore.
SettingsScreen es el destino único para configuración y personalización básica.
Véase ITERATION_0.2.2.md para alcance, mediciones y limitaciones.

## Fluid Motion 0.3

El pager oficial conserva vecinos; los estados de scroll viven encima de los
paneles y Ajustes se representa como superficie animada sobre ellos. El sistema
de movimiento está en core/designsystem/Motion.kt. SmartBlock/BlockPreferences
separan identidad, tipo, visibilidad, tamaño y posición de la geometría Compose;
DataStore añade un payload versionado sin sustituir las claves anteriores.
El módulo macrobenchmark es opt-in y ajeno al runtime del launcher.
Ver [contratos y decisiones](FLUID_MOTION.md).
