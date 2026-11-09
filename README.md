# Neobash Launcher · 0.1.0

Launcher Android nativo, offline y minimalista con Jetpack Compose. Android 8.0+
(API 26); objetivo y compilación API 35. Application ID: `site.neobash.launcher`.

Incluye HOME, reloj/fecha local en español, clima y astronomía **simulados**,
trayectoria solar, Luna por fase simulada, cajón y búsqueda sin distinción de
acentos, favoritos persistentes, configuración y tema claro opcional. Las cuatro
etiquetas iniciales GELIA, Notas, Tareas y Agenda son espacios configurables:
no se inventan aplicaciones instaladas. Tocar un espacio vacío permite elegir;
Configuración permite reasignarlo o quitarlo.

## Compilar en Codex/Linux

```bash
bash scripts/setup-android.sh
source scripts/android-env.sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Se reutiliza el entorno existente. Ver [entorno Android](docs/android-environment.md).
El Wrapper fija Gradle 8.11.1 con SHA-256. AGP 8.9.2; Kotlin/Compose Compiler
2.1.20; Compose BOM 2025.04.01; JDK 21, bytecode 17; Build Tools 35.0.0.
Lifecycle 2.8.7, Activity 1.10.1 y DataStore 1.1.4.

APK: `app/build/outputs/apk/debug/app-debug.apk`. No se versiona.

## Android Studio o Cursor en Windows

1. Clonar el repositorio y cambiar a `feature/initial-launcher`.
2. Instalar Android Studio y SDK Platform 35, Build Tools 35.0.0 y Platform Tools
   con SDK Manager. Usar JDK 21 (17 también es compatible con esta cadena).
3. Abrir la raíz en Android Studio y sincronizar Gradle. En Cursor, configurar
   `JAVA_HOME`, `ANDROID_HOME` y añadir Platform Tools al PATH. Si hace falta,
   crear `local.properties` con `sdk.dir=C:/Users/TU_USUARIO/AppData/Local/Android/Sdk`.
4. En PowerShell: `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug`.
5. Con depuración USB y el PC autorizado: `adb devices`, después
   `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

## Instalar directamente en Android

Copiar el APK al dispositivo, abrirlo desde Archivos y permitir a esa aplicación
instalar apps desconocidas cuando Android lo solicite. Abrir Neobash y entrar en
Configuración → Elegir launcher predeterminado; también se puede usar Ajustes →
Aplicaciones → Apps predeterminadas → Inicio. Para revertir, elegir el launcher
original allí. Desinstalar borra preferencias; actualizar requiere la misma firma.
Es una compilación debug para pruebas, no una distribución de producción.

## Probar en el dispositivo

- Elegir HOME, pulsar Inicio desde otra app y desde búsqueda/configuración.
- Abrir apps; buscar con acentos, teclado y nombres repetidos.
- Configurar favoritos, reiniciar el proceso y comprobar persistencia.
- Desinstalar/deshabilitar un favorito; regresar y comprobar que se puede reasignar.
- Probar Día/Noche/Auto en Configuración, tema claro y oscuro.
- Rotar, usar multiventana/tablet y aumentar fuente; comprobar teclado y objetivos táctiles.
- Verificar botones/gestos en el fabricante concreto. No hay compatibilidad HyperOS garantizada.

No se ha verificado ejecución en dispositivo real ni emulador. Los resultados
concretos de compilación, pruebas y lint se registran en docs/VALIDATION.md.

## Documentación

- [Arquitectura](docs/ARCHITECTURE.md)
- [Diseño](docs/DESIGN_SYSTEM.md)
- [Compatibilidad](docs/DEVICE_COMPATIBILITY.md)
- [Roadmap](docs/ROADMAP.md)
