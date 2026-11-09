# Iteración 0.2 — Celestial UI

Base: `origin/main` (`3c44dc4`). Rama: `feature/celestial-ui-0.2`.

## Interfaz y navegación

Home conserva reloj fino, clima explícitamente simulado, favoritos persistidos,
búsqueda de aplicaciones, apertura por ComponentName y recuperación de apps
no disponibles. Se reutiliza el Canvas original `FacetedN`, sin cambiar sus
triángulos: ahora está en la cabecera, a 24 dp. Su brillo entra en 220 ms y sale
en 320 ms, una vez por entrada en composición; no hay bucles decorativos.
Tarjetas más compactas y barra de búsqueda redondeada con menor altura visual.
Ambos temas siguen usando el esquema monocromático existente (negro OLED/blanco).

`HorizontalPager` coloca Herramientas (0), Home (1) y Apps (2). El estado del pager,
la búsqueda, la selección de favorito y el tema temporal astronómico sobreviven
recreaciones. HOME recibido por `singleTask` cierra overlays y selección y vuelve
al panel central. Back vuelve a Home; sheets y diálogos consumen primero Back.
Al elegir un favorito se bloquea el desplazamiento horizontal y se mantiene la
ruta de regreso a configuración. Las preferencias siguen en DataStore.

La zona vacía de 56 dp entre favoritos y búsqueda acepta pulsación prolongada
para abrir el bottom sheet y deslizamiento vertical ascendente para abrir Apps.
Se limita allí para no competir con el scroll del escritorio, listas, botones o
búsqueda. El resto del escritorio permite scroll en pantallas bajas o fuente grande.
Los márgenes de `safeDrawing ∪ systemGestures` quedan fuera de los detectores y del
pager: no se solicitan exclusiones ni se interceptan bordes del sistema.

Los botones inferiores Herramientas/Inicio/Apps ofrecen navegación sin gestos,
con estado seleccionado semántico. Herramientas ofrece Ajustes con engranaje,
favoritos, tema, información y selección de HOME. El sheet ofrece las primeras
cuatro opciones; personalizar favoritos abre la lista editable de ajustes y cambiar
tema alterna claro/OLED con persistencia. Logo, zona vacía y acciones tienen
semántica; la zona vacía expone una acción larga a accesibilidad.

## Contrato celeste y límites

`core/model/Celestial.kt` contiene el contrato del proveedor, efemérides por astro,
progreso de tránsito y geometría normalizada, sin dependencias Android/Compose.
`CelestialCycle` solo representa esos datos. Cada `BodyEphemeris` admite salida,
puesta, elevación y azimut opcionales; el snapshot admite fracción iluminada y
orientación del limbo lunar. La fase sinódica determina el lado iluminado en la
simulación, con terminador elíptico y rotación alrededor del disco.

El semicírculo **es estilizado, no una proyección astronómica exacta**. Cada astro
recorre izquierda → cima → derecha con el tiempo transcurrido entre su propia
salida y puesta. Ambas coordenadas usan exactamente el mismo radio, incluso en
ventanas anchas. La cima significa mitad del intervalo, no elevación observada,
y los extremos no codifican azimut. No se usan coordenadas reales ni ubicación.
Sol y Luna se dibujan simultáneamente si ambos tienen tránsito visible; cada uno
se oculta fuera de su intervalo, sin fijarlo artificialmente al horizonte.

Proveedor actual: simulado; Sol 06:30–18:45, Luna 12:00–23:30, fase 0.35,
orientación de demostración 0°. Su procedencia aparece siempre en pantalla y en
la descripción accesible. Día/Noche siguen disponibles en ajustes: ahora son
vistas de demostración a las 14:00/21:00, etiquetadas como tales; Auto usa el reloj.
Datos ausentes o salida=puesta no producen posición. Se admite cruce de medianoche
para intervalos menores de 24 horas. Antes de integrar astronomía real se deben
verificar fecha/zona/ubicación, eventos polares y múltiples días, orientación y
convenciones de coordenadas: `LocalTime` no basta para eventos de varios días.
`VERIFIED` está reservado; ningún proveedor actual declara datos verificados.

No se añaden servicios ni permisos. El reloj conserva su actualización por minuto
solo con suscriptores; el Canvas es estático entre cambios de estado. Las transiciones
del pager duran 240 ms al usar botones. No hay animaciones infinitas.

## Integración HOME y prueba manual pendiente

Manifest conserva MAIN + HOME + DEFAULT y MAIN + LAUNCHER separados, Activity
exportada `singleTask`, sin APIs privadas. Android 10+ usa `RoleManager.ROLE_HOME`
con contrato de Activity Result, comprueba disponibilidad y si ya posee el rol.
Android 8–9 o un sistema sin selector de rol usa `Settings.ACTION_HOME_SETTINGS`;
si no existe actividad compatible, informa la ruta manual. Cancelar no cambia
preferencias ni presupone concesión. No se modifica SystemUI, Quickstep ni HyperOS.

Prueba en dispositivo (hacer también en HyperOS antes de afirmar compatibilidad):

1. Instalar `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
2. Abrir desde el icono; Herramientas → Elegir launcher predeterminado. Cancelar
   y comprobar retorno; repetir y conceder el rol en la UI del sistema.
3. Pulsar HOME desde otra app; comprobar panel central y cierre de búsqueda,
   editor de favoritos, sheet y diálogo. Repetir con la Activity ya existente.
4. Usar Back y gestos laterales del sistema desde ambos bordes. Comprobar que los
   márgenes no cambian de panel. Probar barra de navegación y navegación gestual.
5. Recorrer horizontalmente los tres paneles. Deslizar arriba en la zona vacía;
   verificar que el scroll de listas y la búsqueda no activan esa ruta.
6. Mantener pulsada esa zona y probar las cuatro acciones; con TalkBack usar
   botones de panel, Ajustes y acción larga semántica. Verificar foco y teclado.
7. Asignar/quitar favoritos, abrir apps y reiniciar proceso para comprobar DataStore.
   Desinstalar/reinstalar una app favorita y verificar la referencia no disponible.
8. Probar claro/OLED, rotación, multiventana, pantalla pequeña/tablet, fuente 200 %,
   teclado visible e insets; comprobar ausencia de recortes y objetivos ≥48 dp.
9. En Auto revisar antes/durante/después de ambos tránsitos; Día debe mostrar ambos
   astros y Noche solo Luna. Confirmar etiquetas de simulación y ausencia de loops.
10. Restaurar el launcher anterior desde aplicaciones predeterminadas del sistema.

La compilación/JVM/Lint no acreditan interacción física, rendimiento o integración
con gestos de HyperOS. Esos puntos permanecen pendientes de dispositivo.
