# Verificación de 0.2.1

9 de noviembre de 2026 (UTC). Rama `feature/ui-refinement-0.2.1`, base `71f2fe5`.
JDK 21, Wrapper 8.11.1, SDK 35/Build Tools 35.0.0.

```bash
source scripts/android-env.sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

- BUILD SUCCESSFUL; APK `app/build/outputs/apk/debug/app-debug.apk`.
- Versión 0.2.1 (3), site.neobash.launcher, minSdk 26, targetSdk 35.
- 16 pruebas, 0 fallos/errores/omitidas: dominio (6), iconos (3), cajón (2),
  distribución normal/fuente 200 %/ventana baja (3), navegación y límites (2).
- Lint: `No issues found.`; firma APK v2 válida; `git diff --check` correcto.
- SHA-256 local: `df8ad1b9fe65123cf6dd2cb31706fc3abee07bdcf426c7e85b5b50dcb654f007`.
- Pruebas UI con Compose/Robolectric API 28 y gráficos nativos; no dispositivo.
- El usuario informa validación física de 0.2 en HyperOS. La nueva 0.2.1 requiere
  validación física adicional según [el procedimiento](ITERATION_0.2.1.md).

Los informes locales permanecen en `app/build/reports/`, fuera de Git. Los APK de
Actions tienen otra clave debug y pueden tener otro hash; consultar el artefacto
de la ejecución del PR correspondiente. No se versionan APK ni cachés.

---

# Verificación de 0.2.0

Fecha: 9 de noviembre de 2026 (UTC). Base: `origin/main` `3c44dc4`.
Rama: `feature/celestial-ui-0.2`. JDK 21, Gradle Wrapper 8.11.1, SDK 35,
Build Tools 35.0.0; entorno activado con `scripts/android-env.sh`.

```bash
source scripts/android-env.sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Resultado final: BUILD SUCCESSFUL (12 s). La ejecución offline inicial carecía de
algunas dependencias; la ejecución con red descargó las versiones ya fijadas.

- APK debug 0.2.0 (versionCode 2), applicationId `site.neobash.launcher`,
  minSdk 26, targetSdk 35; 10200855 bytes.
- 6 pruebas JVM, 0 fallos, 0 errores, 0 omitidas: búsqueda normalizada,
  límites solares, fases lunares, tránsitos nocturnos/eventos ausentes,
  simultaneidad e independencia de cuerpos y simetría/radio del semicírculo.
- Lint: `No issues found.` (0 errores, 0 advertencias).
- `apksigner verify --verbose`: firma v2 válida, un firmante debug.
- `aapt dump badging`: paquete y versión correctos; filtros HOME/DEFAULT/LAUNCHER
  conservados en el manifest fusionado. Sin permisos sensibles nuevos.
- `git diff --check`: sin errores.

APK: `app/build/outputs/apk/debug/app-debug.apk` (no versionado).
SHA-256: `2186254166e43fb40b0562188fcea3c92afd81fe0828382551b4ceea14047c75`.
Informes: `app/build/reports/tests/testDebugUnitTest/index.html` y
`app/build/reports/lint-results-debug.html`.

No hay emulador/imagen instalado ni `/dev/kvm`. No se ejecutaron pruebas
instrumentadas o físicas. Interacción, gestos, accesibilidad, rendimiento, HOME
y persistencia end-to-end siguen pendientes de dispositivo. La compatibilidad
con gestos de HyperOS no se afirma. Procedimiento de prueba y límites del modelo
en [Iteración 0.2](ITERATION_0.2.md).

---

# Verificación de 0.1.0

Fecha: 8 de noviembre de 2026 (America/Mexico_City).
Base: origin/main d7b67d6, que fusionó 82011ad. Rama: feature/initial-launcher.
Se ejecutó el instalador del repositorio y se reutilizaron JDK 21.0.12.1,
Gradle 8.11.1, SDK 35 revisión 2, Build Tools 35.0.0 y Platform Tools 37.0.1.
El Wrapper descargó su distribución fijada en su propia caché y verificó SHA-256.

```bash
source scripts/android-env.sh
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew lintDebug --rerun-tasks
```

- assembleDebug: BUILD SUCCESSFUL; APK existente, 10620775 bytes.
- testDebugUnitTest: 3 pruebas, 0 fallos, 0 errores, 0 omitidas.
  Casos: búsqueda con acentos/mayúsculas/espacios y sin resultados;
  amanecer/atardecer y acotación/progreso solar; cinco fases lunares principales.
- lintDebug: 0 errores y 0 advertencias. Se forzó una ejecución final para
  descartar un informe incremental antiguo tras ajustar reglas de respaldo.
- apksigner verify --verbose: firma APK v2 válida, un firmante debug.
- aapt dump badging: site.neobash.launcher, versión 0.1.0, minSdk 26, targetSdk 35.
- git diff --check: sin errores de espacios.

APK local: `app/build/outputs/apk/debug/app-debug.apk` (fuera de Git).
SHA-256: `bb12c2742e94787c9dd772c6ff955ece5b3c1fba8087274c624c8975f26f3131`.

Informes locales generados (no versionados):
`app/build/reports/tests/testDebugUnitTest/index.html` y
`app/build/reports/lint-results-debug.html`.

No se ejecutaron pruebas instrumentadas, emulador ni dispositivo real. No hay
imagen de emulador instalada ni /dev/kvm en esta instancia. No se verificaron
HOME, gestos, apertura de aplicaciones, persistencia tras reiniciar el proceso,
teclado/stylus, tablets ni HyperOS en hardware. Las comprobaciones anteriores
acreditan compilación y lógica JVM, no experiencia de uso física.

Pendientes conocidos: clima y astronomía simulados; orientación lunar aparente,
iconos reales/personalizables, voz, búsqueda universal y perfiles de trabajo.
La vista Día/Noche se conserva durante recreación, pero no tras reiniciar el proceso.
Las aplicaciones se recargan al reanudar o recibir HOME, sin receptor continuo.

## Iteración 0.2.2 · 2026-11-09

Base `31e6895` de main; rama `perf/navigation-0.2.2`. Se conservaron los cambios
previos y se comprobaron todas las ramas remotas, incorporadas en main.

Validación con `source scripts/android-env.sh` y Gradle 8.11.1 instalado:

```bash
gradle --no-daemon --console=plain assembleDebug testDebugUnitTest lintDebug
```

El Wrapper intentó descargar esa misma versión y agotó su timeout de 10 s;
por eso se usó la instalación existente, sin cambiar versiones ni el workflow.

- `assembleDebug`: APK `0.2.2` / versionCode `4`, applicationId
  `site.neobash.launcher`, minSdk 26, target/compile 35.
- `testDebugUnitTest`: 24 pruebas, cero fallos/errores/omisiones.
- `lintDebug`: cero errores y cero advertencias en el informe final.
- Firma debug v2 verificada con apksigner; APK fuera de Git.
- `bash -n scripts/measure-drawer.sh` y `git diff --check`: correctos.
- `adb devices -l`: ningún dispositivo conectado. Sin prueba física o emulador.

Además de las coberturas anteriores, se comprueban reutilización de metadatos,
actualización/revisión y deshabilitación por paquete sin sustituir otras entradas,
invalidación de iconos, propagación de broadcast de desinstalación a HomeState,
persistencia/reordenación/cantidad/restauración de favoritos, búsqueda en Compose,
pulsación larga, destino único de ajustes y regreso del selector. El intent HOME
se entrega mediante ActivityController y se verifica el regreso desde Apps;
ActivityScenario sigue controlando los casos normales de navegación. El fallback
anterior a RoleManager se comprueba en API 28. El diálogo de rol API 29+ y las
restricciones del fabricante necesitan dispositivo real.

No hay resultados de rendimiento A/B. El protocolo y el script se documentan en
[ITERATION_0.2.2.md](ITERATION_0.2.2.md), con separación de carga, frames y reposo del
pager. No se atribuye una mejora cuantitativa al cambio sin esa comparación.
