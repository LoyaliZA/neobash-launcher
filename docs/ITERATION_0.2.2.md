# Iteración 0.2.2 — Rendimiento y navegación

Base: `origin/main` en `31e6895` (PR #5, 0.2.1). El checkout inicial estaba en
`689b435`; el fetch inicial usó el refspec limitado a main del entorno.
Se completó el inventario con ls-remote y fetch explícito de todas las ramas:
`chore/android-cloud-environment`, `feature/initial-launcher`,
`feature/celestial-ui-0.2` y `feature/ui-refinement-0.2.1` están incorporadas en
main y ninguna contiene una versión posterior. Rama: `perf/navigation-0.2.2`.
Se conserva el historial de 0.1, 0.2 y 0.2.1.

## Diagnóstico y cambios

La revisión estática encontró descubrimiento completo desde `onResume`, HOME
y el retorno del selector de rol. Cada ejecución volvía a resolver actividades,
etiquetas y revisiones. Los iconos ya se rasterizaban en IO con LRU de 4 MiB;
no se encontró carga de iconos en el hilo principal. No hay evidencia física
que permita atribuir todo el retraso a una única causa.

Ahora el ViewModel inicia una carga. `AppsRepository` conserva los metadatos y
reutiliza la misma lista cuando no hay invalidaciones; navegar, volver de una
app y recibir HOME no consultan el catálogo. Las operaciones de catálogo y raster
siguen en `Dispatchers.IO`. Un Mutex serializa los cambios de catálogo y otro
las cargas/invalidaciones de iconos; un acierto LRU no espera ese segundo Mutex.
La carga del drawable usa el ActivityInfo ya obtenido, evitando otra resolución
interna de `getActivityIcon`. La LRU conserva su límite y claves de revisión,
densidad, tamaño y modo UI. No se reciclan bitmaps que Compose pueda mostrar.

Un receiver de aplicación, registrado mientras vive el ViewModel y liberado en
`onCleared`, observa PACKAGE_ADDED/REMOVED/CHANGED/REPLACED. Agrupa ráfagas durante
100 ms, invalida iconos del paquete y consulta únicamente sus actividades. Se
conservan los objetos de los demás paquetes y sus metadatos; la lista combinada
se ordena. No hay sondeo, servicio ni observador asociado a cada fila. Durante
proceso muerto no hay caché: el siguiente inicio vuelve a descubrir todo.

PackageManager es la fuente única para el usuario actual; no existían consultas
a LauncherApps ni se añaden consultas duplicadas. Perfiles de trabajo, perfiles
privados, suspensión/almacenamiento externo y cambios de idioma en caliente
requieren validación/ampliación específica; no se afirma soporte multiusuario.
No se añade QUERY_ALL_PACKAGES ni permisos de usuario.

LazyColumn conserva claves por componente y solo solicita iconos de las filas
compuestas. La consulta vacía devuelve la lista original. Cada metadato calcula
su etiqueta de búsqueda normalizada una vez, en vez de normalizar cada nombre
a cada pulsación; la consulta se normaliza una vez. El filtro se recuerda por
lista/consulta. No se fuerza la composición anticipada de todo el cajón ni se
añaden animaciones por fila.

## Configuración y distribución

HOME pierde el engranaje permanente. Pulsar prolongadamente la zona vacía abre
exactamente tres rutas: Personalizar inicio, Ajustes e Información. Las dos
primeras conducen al mismo destino de configuración. La zona mantiene su acción
semántica de pulsación larga y su gesto ascendente para abrir aplicaciones.
El pager ofrece la acción accesible «Abrir ajustes» y acciones para cambiar de
panel. Herramientas conserva un botón de texto «Ajustes», utilizable con teclado,
y el regreso a Inicio; elimina controles duplicados de tema, favoritos y HOME.

DataStore conserva los nombres anteriores `favorite_0..3` y `light`; no hay
migración destructiva. Añade visibilidad de reloj/clima simulado, ciclo celeste y
favoritos, cantidad visible 1–4 y reducción de movimiento. El orden de favoritos
se cambia con botones Subir/Bajar y se persiste atómicamente. Reducir la cantidad
no borra los favoritos ocultos. Restaurar distribución vuelve a cuatro espacios
vacíos y todos los componentes visibles; conserva tema y reducción de movimiento.
No hay editor visual, widgets, Notes, proveedor meteorológico ni nuevas funciones
de productividad. Día/Noche/Auto conserva el comportamiento temporal anterior.

`NeobashMotion` define una transición finita de panel de 240 ms. La navegación
programática pasa a instantánea al activar reducción de movimiento o desactivar
animaciones del sistema; Compose también recibe la escala de duración del sistema.
Los controles Material/clickable mantienen su respuesta ripple acotada. Se elimina
el brillo decorativo de entrada del logotipo. No hay animaciones infinitas.

## Medición reproducible

No se publican cifras de mejora: no hay mediciones comparables de dispositivo.
En debug, el tag Logcat `NeobashPerf` registra:

- `catalog`: alcance (`all` o paquete), número final y tiempo de consulta,
  metadatos y ordenación dentro de IO. Excluye la espera para entrar en IO/Mutex.
- `iconMiss`: componente, tamaño y tiempo de consulta/raster en un fallo LRU.
  Excluye espera de Mutex; los aciertos no generan este evento.
- `panel`: destino y `requestToSettledFrameNs`, desde la petición programática
  o cambio de objetivo del arrastre hasta el primer callback de frame posterior
  al reposo del pager. Incluye la animación; no equivale a latencia táctil, primer
  píxel ni finalización de todos los iconos. Un deslizamiento cancelado puede no
  emitir un resultado para el objetivo abandonado.

Perfetto puede localizar las secciones `Neobash.catalog`, `Neobash.icon.miss` y
el marcador `Neobash.panel.settled`. No se mantienen secciones Trace abiertas
entre suspensiones. Los eventos de Logcat se limitan a compilaciones debug.

Con ADB y un teléfono desbloqueado, ejecutar el mismo script sin modificar en
cada APK (no selecciona ni cambia el launcher predeterminado):

```bash
adb install -r /ruta/al/APK-0.2.1.apk
bash scripts/measure-drawer.sh /tmp/neobash-baseline 20
adb install -r /ruta/al/APK-0.2.2.apk
bash scripts/measure-drawer.sh /tmp/neobash-candidate 20
```

El script necesita Bash y un único dispositivo ADB seleccionado (o ANDROID_SERIAL).
Guarda fingerprint, versión, resolución, escalas, logs y `gfxinfo framestats`
por recorrido. Reinicia el proceso una vez; el primer recorrido tiene carga de
proceso frío, los demás usan proceso caliente. Espera dos segundos en HOME antes
de cada arrastre horizontal de 250 ms; por ello el tiempo de descubrimiento se
consulta por separado en logs y no se presenta como latencia de apertura fría.
El script se preparó y revisó sintácticamente, pero no se ejecutó con dispositivo.

Mantener mismo teléfono, apps instaladas, firma, preferencias, refresco de pantalla,
escalas, nivel térmico, orientación y batería. Registrar tipo de navegación y
versión HyperOS. Separar el primer recorrido de los calientes; repetir alternando
el orden de APK para reducir sesgo térmico. Comparar distribución p50/p95 de
`FrameCompleted - IntendedVsync` para frames válidos (Flags=0), frames lentos según
el presupuesto del refresco y memoria; conservar datos crudos y tamaños de muestra.
No comparar métricas internas nuevas con la ausencia de esos eventos en 0.2.1:
`gfxinfo`/Perfetto son el instrumento común. Para comparar tiempos internos hay
que instrumentar ambos commits de forma idéntica en ramas de medición separadas.

## Validación y límites

Los resultados automatizados finales se registran en [VALIDATION](VALIDATION.md).
Robolectric/Compose API 28 cubren búsqueda, navegación, selector, recepción de
HOME, fallback de selección predeterminada, iconos y preferencias. No sustituyen
Binder, GPU, el selector de RoleManager API 29+, accesibilidad real ni Quickstep.

Pendiente en el teléfono físico: repetir medidas A/B, apertura desde gesto
horizontal/vertical y búsqueda, Back/HOME desde apps y ajustes, rol HOME API 29+,
TalkBack/teclado, fuente 200 %, reducción de movimiento y escalas del sistema.
Con Neobash visible y en segundo plano, instalar, actualizar, deshabilitar,
habilitar y desinstalar apps; comprobar lista, iconos, favoritos y carreras al
abrir una app eliminada. Validar reinicio de proceso y actualización sobre 0.2.1.

El workflow existente sigue compilando, probando y ejecutando lint en PR hacia
main (también borrador), y sube el APK descargable tras pasar los checks. Se
conservan [las instrucciones de descarga](ANDROID_APK_DOWNLOAD.md); no se versiona
el APK ni se fusiona el PR automáticamente.
