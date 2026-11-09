# Iteración 0.2.1 — Refinamiento móvil

Rama: `feature/ui-refinement-0.2.1`, basada en `feature/celestial-ui-0.2`
(`71f2fe5`). Se consultó `origin/main` (`3c44dc4`); no había cambios adicionales
que incorporar. Mientras 0.2 no esté fusionada, el PR hacia main incluye ambas
iteraciones. No se reinicializa el proyecto ni se cambian applicationId o HOME.

El usuario comunica que 0.2 funciona en dispositivo HyperOS: HOME, paneles,
selección predeterminada y descubrimiento de aplicaciones. Esto es evidencia
aportada por el usuario sobre **0.2**, no una prueba física ejecutada aquí de 0.2.1.

## Distribución y navegación

Se elimina la fila permanente Herramientas/Inicio/Apps. El mismo HorizontalPager
conserva tres páginas y sus límites, gestos Compose, estado restaurable y
transiciones programáticas de 240 ms. Sigue fuera de `safeDrawing ∪ systemGestures`;
no hay exclusiones de gestos ni cambios de SystemUI/Quickstep/HyperOS.

La cabecera de Home ofrece un engranaje de 48 dp para opciones y navegación.
El menú permite ir a Herramientas o Aplicaciones sin deslizar. Búsqueda abre el
cajón; desde Herramientas y Apps existe regreso explícito a Inicio. Se añaden
acciones semánticas para ir a otros paneles y título accesible del panel actual.
Pulsación larga y gesto vertical ascendente en la zona vacía siguen disponibles.
La selección de favoritos bloquea el pager y regresa a su origen (Home,
Herramientas o ajustes), también con Back. HOME recibido cierra los overlays.

No se añade Scaffold ni se duplican insets. La Column de Home conserva medición
natural con verticalScroll, pero habilita desplazamiento **solo si el contenido
medido desborda** (`ScrollState.maxValue > 0`). No se decide con una estimación de
altura de texto. Se reducen padding/espaciado, se elimina el segundo botón de
aplicaciones y se adaptan altura del Canvas y distribución de favoritos a las
restricciones útiles. Se conservan etiquetas de simulación y objetivos táctiles.

El reloj sigue siendo fino (64 sp en altura compacta, 76 sp en altura amplia),
acotando su ancho para evitar corte con fuente aumentada. El resto del texto
respeta la escala del usuario; favoritos pasan a dos columnas con fuente grande.
En ventanas pequeñas y fuente 200 % se permite scroll para conservar controles,
sin comprimir el texto hasta volverlo ilegible. Fuentes/apps con etiquetas largas
pueden producir overflow legítimo aunque una configuración normal quepa.

CelestialCycle solo recibe una restricción de altura opcional; no cambian
proveedor simulado, horarios, fase, orientación, cálculo de posición ni geometría
circular. Sol y Luna permanecen independientes en el mismo semicírculo.

## Cajón e iconos

Cada fila normal muestra el nombre y el icono original de su Activity, sin
paquete ni ComponentName visibles. Los identificadores se conservan para apertura,
selección, claves de lista y persistencia. Se mantiene orden alfabético y búsqueda
normalizada. Aplicaciones con el mismo nombre pueden verse iguales; la
identificación avanzada queda pendiente, sin volver a mostrar paquetes por defecto.

`AppIconsRepository`, propiedad del ViewModel con contexto Application, carga
solo iconos de filas compuestas mediante PackageManager en Dispatchers.IO.
Rasteriza Drawable (incluidos adaptativos y vectoriales) a 40 dp para la densidad
actual. No recolorea los iconos: se conserva el diseño original del paquete.
La caché LRU de 4 MiB incluye componente, última actualización del paquete,
tamaño, densidad y modo UI. Un Mutex evita duplicar cargas concurrentes. No se
reciclan bitmaps todavía usados por Compose ni se guardan en disco. Solo las
filas visibles/próximas de LazyColumn solicitan iconos; buscar o volver a ellas
reutiliza la caché mientras siga vigente.

Mientras carga o si falta el recurso gráfico se usa la inicial como fallback.
Si la Activity ya no existe/no está habilitada/no es accesible al cargar, la fila
muestra «No disponible» y no permite abrirla. El descubrimiento al reanudar
excluye componentes deshabilitados/eliminados; una carrera posterior a la carga
se resuelve con el manejo existente de apertura fallida y refresco. No se instala
un observador permanente: los cambios de paquetes con el launcher visible pueden
reflejarse al reanudar o tras un intento fallido. Favoritos no disponibles conservan
su referencia y permiten reasignarse. Sin QUERY_ALL_PACKAGES ni permisos nuevos.

## Herramientas y alcance

`feature/tools/ToolsScreen` separa el panel para su evolución en 0.3. Conserva
engranaje de ajustes, tema, favoritos, información y elección de HOME. Favoritos
abre directamente un selector de slots, evitando pasar por toda la configuración.
No hay tarjetas vacías de Notes, Tareas o Agenda, ni implementaciones de esas
funciones. Se mantienen ambos temas Monochrome Fluid, sin servicios, red de clima
ni animaciones decorativas infinitas.

## Verificación automatizada y física pendiente

`source scripts/android-env.sh` y
`./gradlew assembleDebug testDebugUnitTest lintDebug` completados correctamente.
16 pruebas sin fallos; Lint sin incidencias; APK 0.2.1 (3) con firma debug v2 válida.

Además de seis pruebas de dominio existentes:

- Robolectric comprueba carga del drawable real de una Activity, tamaño de raster,
  reutilización de caché, nueva revisión/tamaño e identificadores ausentes/inválidos.
- Compose comprueba la fila con nombre, uso del icono cargado, paquete oculto,
  conservación del identificador al seleccionar y estado deshabilitado no disponible.
- Compose con gráficos nativos comprueba Home a 393×760 dp útiles sin scroll,
  búsqueda alcanzable con fuente 200 % y ventana de 480 dp de altura.
- Compose sobre MainActivity comprueba desplazamiento horizontal en ambas
  direcciones, detención en los extremos y navegación por opciones sin deslizar.

Robolectric usa API 28 en JVM; no sustituye emulador, GPU ni dispositivo. La Activity
host de pruebas Compose se incluye solo en debug por ui-test-manifest. No se
modifica el manifest de producción ni se añaden permisos a la aplicación.

Antes de dar por validada físicamente 0.2.1:

1. Instalar el APK y comprobar Home en el HyperOS ya usado, claro/OLED, fuente
   normal, 150 % y 200 %, orientación y multiventana. Verificar barras/insets,
   ausencia de scroll innecesario y búsqueda alcanzable cuando sí desborde.
2. Deslizar por las tres páginas y sus extremos; probar Back del sistema desde
   bordes, HOME desde otra app y la ruta vertical en la zona vacía. Probar teclado.
3. Con TalkBack y navegación por botones, abrir el engranaje, recorrer paneles,
   ajustar tema y editar/quitar favoritos; confirmar regreso al origen y foco.
4. Revisar iconos originales de paquetes reales adaptativos/vectoriales/bitmap,
   desplazamiento y búsqueda repetidos. Actualizar, deshabilitar y desinstalar una
   app; reanudar y comprobar lista/favoritos y manejo de carreras al abrir.
5. Revisar fluidez y memoria con muchas aplicaciones y comprobar ambas posiciones
   celestes en las vistas simuladas, sin cambio de lógica ni efectos permanentes.

GitHub Actions conserva el workflow de PR hacia main (incluye borradores): compila,
ejecuta las pruebas y Lint y solo después sube `neobash-launcher-debug-N` durante
7 días. Consultar [descarga del APK](ANDROID_APK_DOWNLOAD.md). No se fusiona el PR.
