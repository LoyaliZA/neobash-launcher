# 0.3.0 — Fluid Motion

Base: `af3ff17`, main con PR #6 (0.2.2) integrado. Rama:
`feature/fluid-motion-0.3`. Se conservan API 26–35, applicationId, dependencias
existentes, proveedores simulados, selector HOME y claves de preferencias antiguas.

## Decisiones de movimiento

Se evaluó la implementación existente de HorizontalPager antes de ampliarla.
Se conserva el arrastre, detección de dirección, velocidad, umbrales y cancelación
oficiales de Compose; no se introduce un reconocedor horizontal propio ni barra
inferior. PagerDefaults limita el destino al panel vecino y resuelve el fling.
La fase final utiliza spring amortiguado (ratio 1, rigidez 400); una nueva petición
cancela la navegación programática anterior. El dedo sigue controlando la posición
sin esperar una animación. Los márgenes de gestos del sistema siguen excluidos.

Se mantiene una página vecina compuesta: desde HOME esto prepara ambos laterales,
incluidas solamente las filas visibles del cajón. Es un intercambio explícito de
trabajo inicial/memoria por continuidad, que requiere medir en hardware. No se
precarga todo el catálogo de iconos. LazyListState y scroll de Ajustes se elevan
por encima del pager/overlay. Las claves estables del pager y las filas permanecen.
Ajustes se presenta con AnimatedVisibility sobre los paneles existentes, oculta
su semántica y deshabilita el arrastre mientras está abierto. Material conserva
la presentación modal y el manejo de Atrás del menú contextual.

`core/designsystem/Motion.kt` centraliza resortes espaciales (ratio 1, rigidez 500),
entrada de superficies (fade 140 ms + desplazamiento 1/24 de altura), salida
(100 ms), expansión/cambio de tamaño y presión a escala .97. Los favoritos
conservan marcas y etiquetas actuales, con ripple y retorno del resorte al soltar
o cancelar; el callback de apertura nunca espera su terminación. El reajuste de
filas del cajón usa animateItem sin fade que oculte resultados. HOME acompaña los
cambios de distribución con animateContentSize. No hay animaciones infinitas.
No se usa AnimatedContent para sustituir paneles porque descartaría su composición.
No se añaden Animatable adicionales cuando animate*AsState resuelve la reversión.

La preferencia Reducir movimiento y el observador de ANIMATOR_DURATION_SCALE=0
eliminan las transiciones propias, presión y resortes de tamaño. Compose/Material
además aplican la escala de duración del sistema a sus animaciones oficiales.
No se controla SystemUI, Quickstep ni transiciones protegidas del fabricante.

El botón Buscar de HOME solicita foco/IME una vez, cuando Aplicaciones queda en
reposo. Deslizar al cajón no abre el teclado automáticamente. Los cambios de panel
y Ajustes limpian foco; volver a HOME cancela búsquedas pendientes. imePadding
continúa usando los insets oficiales, sin esperas artificiales para lanzar apps.

## Ciclo celeste y Smart Layout

El cálculo astronómico permanece en core/model/Celestial: proveedor simulado,
tránsitos independientes, fase, orientación y semicirclePoint normalizado.
El Canvas usa el mismo radio para ambos cuerpos y ambas coordenadas. Su altura
se interpola mediante un resorte interrumpible; radio, arco y posiciones siguen
la medida interpolada. Compacto/Mediano/Expandido aplican .72/1/1.45 a la altura
adaptable del panel. No se reinicia el proveedor ni cambia la procedencia de datos.
Ajustes permite seleccionar y persistir tamaño. Solo debug incluye un laboratorio
visible en Ajustes con el mismo componente y datos explícitamente simulados:
cambiar rápidamente entre tamaños comprueba inversión durante la transición.
HOME no incorpora controles de desarrollo.

SmartBlock declara id estable, tipo, visibilidad, tamaños admitidos y posición
lógica. BlockPreferences aporta tamaño, posición y opciones extensibles; el codec
JSON v1 guarda preferencias en la clave aditiva smart_blocks_v1 de DataStore.
smartBlocks() adapta la visibilidad anterior y genera valores predeterminados para
instalaciones 0.2.2. Los identificadores son clock/celestial/favorites y deben
permanecer estables. Las opciones reservan alias por espacio; no hay editor de
alias todavía. Las opciones desconocidas se conservan al editar un bloque.
JSON inválido/versión desconocida cae a valores predeterminados sin borrar las
claves antiguas. Restaurar distribución también limpia preferencias de bloques.
Las posiciones son un contrato preparado, no un editor ni ordenación visual de
bloques en esta versión. El editor y arrastrar/soltar corresponden a 0.4.

## Rendimiento: evidencia y reproducción

Inspección: catálogo y rasterización ya estaban en Dispatchers.IO, con catálogo
retenido y LRU de 4 MiB por revisión/densidad/tamaño/tema. El mutex de iconos evita
raster duplicado. La primera composición del cajón todavía necesitaba solicitar
sus iconos visibles. Ahora puede hacerlo como vecino de HOME; no se afirma una
reducción de latencia sin comparar capturas. Repositorios y bitmaps conservan su
ciclo de vida; no hay consulta de catálogo por gesto o por retorno desde otra app.
La medición de panel descarta objetivos cancelados para no arrastrar marcas
obsoletas al siguiente gesto. Se conservan trazas catalog/icon.miss/panel.settled.

ADB no tenía dispositivos conectados durante esta implementación. No hay FPS,
p50/p95, mejora porcentual ni consumo físico medidos. Robolectric no es una
medición de rendimiento Android/GPU.

Protocolo A/B: usar el mismo dispositivo, apps, preferencias, temperatura,
orientación, densidad, refresco y escalas. Alternar APK 0.2.2/0.3.0 con la misma
firma y ejecutar `bash scripts/measure-drawer.sh /tmp/captura 20` en cada uno.
El script guarda metadatos, Logcat y gfxinfo; separar primer recorrido de calientes.
Ver detalles y límites del reloj interno en [0.2.2](ITERATION_0.2.2.md).

Hay un módulo opcional Macrobenchmark (AndroidX 1.3.4, UIAutomator 2.3.0,
AndroidX Test 1.2.1). No altera dependencias del launcher ni la compilación debug
normal. La variante benchmark usa release, es no depurable, profileable solo en
esa variante y tiene firma debug para pruebas. En un teléfono físico API 29+
(API 28 permite instrumentación pero no todos los datos de FrameTiming):

```bash
source scripts/android-env.sh
# Compilar los APK de objetivo y prueba:
gradle -PenableMacrobenchmark :app:assembleBenchmark :macrobenchmark:assembleBenchmark
# Teléfono desbloqueado, conectado y autorizado:
gradle -PenableMacrobenchmark :macrobenchmark:connectedBenchmarkAndroidTest
```

DrawerBenchmark separa procesos fríos de recorridos calientes, con diez iteraciones,
CompilationMode.None y FrameTimingMetric. El tramo medido incluye ida y regreso;
no se debe describir como tiempo exclusivo de apertura. Esperar reposo de UI no
prueba que todos los iconos hayan terminado. Conservar JSON/trazas de resultados
bajo macrobenchmark/build/outputs, registrar fingerprint y refresco y examinar
Perfetto para IO, composición y frames consecutivos. No cambia el launcher
predeterminado ni escalas globales. Las cifras de debug y benchmark no son
comparables entre sí. Ejecución física pendiente.

## Verificación física pendiente

HyperOS 3 y Android estándar: cancelar/invertir arrastres, alternar rápidamente
paneles, desplazar verticalmente cajón, volver desde apps, búsqueda/IME/Atrás,
menú largo, rol HOME API 29+, instalación/desinstalación de favoritos, reinicio,
TalkBack/teclado, escalas 0/.5/1, opción Reducir movimiento, claro/oscuro,
refresco variable, tablets, multiventana, rotación y fuentes 200 %.
No se atribuye soporte verificado a un fabricante sin estas pruebas.

El workflow usa scripts/ci-android-checks.sh para recuperar hasta tres veces
fallos transitorios de descarga en el mismo runner. Un error de código, pruebas
o lint no habilita ese reintento; el artefacto solo se sube después de éxito.
