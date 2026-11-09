# Descargar el APK de GitHub Actions en Android

El workflow [Android Debug APK](https://github.com/LoyaliZA/neobash-launcher/actions/workflows/android-debug.yml)
compila con JDK 21, Android SDK 35, Build Tools 35.0.0 y el Gradle Wrapper
8.11.1 del repositorio. Ejecuta `assembleDebug`, `testDebugUnitTest` y `lintDebug`.
Solo si todas las comprobaciones pasan, guarda `app-debug.apk` mediante
`actions/upload-artifact`. El artefacto se llama `neobash-launcher-debug-N`, donde
N es el número de ejecución, y caduca a los 7 días.

## Desde el teléfono

1. Abre GitHub en Chrome u otro navegador e inicia sesión con una cuenta que
   tenga acceso al repositorio. Usa la web si la aplicación de GitHub no muestra
   los artefactos; activa **Sitio para computadoras** si hace falta.
2. Abre el enlace del workflow anterior, o entra al PR → **Checks** →
   **Android Debug APK / Build, test and lint** → **Details**.
3. Elige una ejecución verde de la rama/PR correcto. Comprueba su commit para
   saber qué versión vas a instalar.
4. En el resumen de esa ejecución, baja hasta **Artifacts** y toca
   `neobash-launcher-debug-N`. GitHub descargará un archivo ZIP, no un APK directo.
5. En Archivos/Files, abre Descargas y extrae el ZIP. Dentro encontrarás
   `app-debug.apk`.
6. Toca el APK. Si Android lo solicita, permite temporalmente a Archivos instalar
   aplicaciones desconocidas; después puedes retirar ese permiso.
7. Abre Neobash → engranaje → Herramientas → Elegir launcher predeterminado. Conserva la
   opción de volver al launcher original desde Ajustes → Apps predeterminadas → Inicio.

Si no hay artefacto, comprueba que la ejecución haya terminado correctamente,
que has iniciado sesión y que no haya caducado. Un fallo en compilación, pruebas
o lint impide subir el APK. Para obtener uno nuevo, actualiza el PR o vuelve a
ejecutar un run existente desde **Re-run all jobs** si tienes permisos.

## Disparadores y ejecución manual

Se ejecuta automáticamente al abrir, reabrir o actualizar un pull request hacia
`main`, incluidos los PR en borrador. No usa `pull_request_target` ni ejecuta
publicaciones. Si GitHub requiere aprobar un workflow de un colaborador externo,
un mantenedor debe hacerlo en la página del PR.

Incluye `workflow_dispatch`. GitHub exige que el archivo del workflow exista en
la rama predeterminada para habilitar este disparador. Mientras este cambio solo
esté en la rama de desarrollo, se puede obtener el APK mediante la ejecución del
PR, sin fusionarlo. Una vez que el workflow esté en `main`, abre Actions →
Android Debug APK → **Run workflow**, selecciona `feature/ui-refinement-0.2.1`
(o la rama que quieras probar) y pulsa **Run workflow**. Requiere permiso de escritura.

## Alcance de la distribución y firma

No se crean releases, enlaces de descarga anónimos, publicaciones en tiendas ni
fusiones automáticas. El APK solo se guarda como artefacto de Actions. Los
artefactos heredan los permisos del repositorio y GitHub exige iniciar sesión
para descargarlos: **en un repositorio público no son una distribución privada**,
ya que otros usuarios con acceso de lectura pueden descargarlos. Para restringir
la descarga a colaboradores, el repositorio tendría que ser privado; este
workflow no cambia su visibilidad.

Cada runner genera una clave debug temporal. Una compilación puede tener una
firma distinta a una instalada desde Codex, Android Studio u otro run. Si Android
rechaza la actualización por firma incompatible, desinstala la versión anterior
antes de instalar esta; se perderán los favoritos y preferencias locales. No se
incluyen claves privadas ni secretos de firma en el repositorio o artefacto.

La ejecución de Actions comprueba compilación, lógica JVM y pruebas Compose en Robolectric. No verifica HOME,
gestos, HyperOS ni funcionamiento en un teléfono: esas pruebas se realizan al
instalar y usar la aplicación.
