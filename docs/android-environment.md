# Entorno Android y persistencia en Codex Cloud

El repositorio contiene únicamente instrucciones y scripts de infraestructura;
todavía no se ha iniciado la implementación del launcher.

## Instalación desde un checkout nuevo

```bash
cd /workspace/neobash-launcher
bash scripts/setup-android.sh
source scripts/android-env.sh
```

Los scripts se localizan a partir de su propia ruta. El destino por defecto es
`/workspace/android-dev`, fuera del repositorio. Para elegir otro destino, exporte
`NEOBASH_ANDROID_DEV_ROOT` con una ruta absoluta, sin espacios y fuera del checkout,
antes de instalar y activar. No se cambia `HOME`, no se usa `sudo` y no se crean
worktrees: cada tarea de Codex ya está aislada.

Requisitos: Linux x86_64, Bash, Python 3, curl, unzip, dpkg-deb, coreutils y una
carpeta de destino escribible. Se reutiliza un JDK completo 17/21 cuando está
disponible. En caso contrario, se extrae OpenJDK 21.0.12.1 con hashes fijados;
esta alternativa requiere la imagen Debian 13 con configuración de OpenJDK 21
y almacén de certificados Java ya presentes. Ante otra imagen sin esos requisitos,
el script se detiene con un diagnóstico y se debe proporcionar un JDK compatible.
Los paquetes Debian fijados deben seguir disponibles o estar en la caché verificada.

Se instalan solo las herramientas de compilación:

| Componente | Versión |
| --- | --- |
| Gradle | 8.11.1 |
| Android Command-line Tools | 19.0 |
| SDK Platform | android-35 |
| Build Tools | 35.0.0 |
| Platform Tools | La revisión estable que entrega sdkmanager; 37.0.1 en la validación |

La revisión de Platform Tools y de la plataforma SDK puede avanzar en el catálogo
de Google; no se promete una reproducción bit a bit de esos paquetes. Sus archivos
se verifican mediante los metadatos de sdkmanager. Las descargas directas de Java,
Gradle y Command-line Tools tienen hashes fijados. No se desactiva TLS ni se
guardan credenciales. El instalador acepta las licencias requeridas por los
paquetes solicitados. No instala emulador, imágenes, Android Studio ni NDK.

La cadena ya comprobada usa AGP 8.9.2, Kotlin Android/compilador Compose 2.1.20,
Compose BOM 2025.04.01 y bytecode Java/Kotlin 17. Estas versiones describen la
prueba de infraestructura; no constituyen todavía dependencias de una aplicación
en este repositorio. Cuando exista un Gradle Wrapper, se deben respetar sus
versiones y revisar la compatibilidad antes de cambiar esta base.

## Activación y comprobaciones

Cada shell debe ejecutar `source scripts/android-env.sh`. Configura `JAVA_HOME`,
`ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_USER_HOME`, `GRADLE_USER_HOME`, `PATH`
y propiedades Java para utilizar el proxy HTTPS heredado y un directorio Java
de usuario escribible. Conserva las opciones Java ya declaradas por el usuario.

```bash
javac -version
gradle --version
sdkmanager --sdk_root="$ANDROID_HOME" --list_installed
test -f "$ANDROID_HOME/platforms/android-35/android.jar"
"$ANDROID_HOME/build-tools/35.0.0/aapt2" version
```

El ejemplo aislado de Compose de la sesión anterior permanece fuera del checkout,
en `/workspace/android-dev/compose-smoke`. Allí se validaron `assembleDebug` y
`lintDebug` con cero errores y una advertencia por falta de icono, el APK firmado
y bytecode Java 17. Ese ejemplo no es parte del repositorio ni debe suponerse
presente en una nueva tarea. No se validó ejecución en un dispositivo o emulador.
ADB puede requerir ejecución aprobada fuera del sandbox para crear `~/.android`.

El instalador del repositorio se ejecutó con éxito tanto sobre la instalación
existente como en un destino nuevo, `/workspace/android-dev-persistence-check`.
En ese destino descargó y verificó Java, Gradle y Command-line Tools e instaló
los tres paquetes SDK solicitados. La activación del nuevo destino permitió
ejecutar `assembleDebug` y `lintDebug` del ejemplo externo: `BUILD SUCCESSFUL`,
46 tareas, 4 ejecutadas y 42 al día. Se reutilizaron la caché de dependencias
Gradle y las salidas compatibles del ejemplo anterior. Esto prueba instalación
en otra carpeta de la misma máquina; no prueba restauración en otra tarea.
También se verificaron la sintaxis Bash y la activación repetida sin duplicar
las propiedades de proxy/Java introducidas por el script.

## Diagnóstico histórico del servicio de configuración

Se inspeccionaron los esquemas publicados de las herramientas oficiales
`cloud_environment_onboarding_read_environment_config_draft` y
`cloud_environment_onboarding_update_environment_config_draft`.
La actualización recibe este envoltorio:

```json
{
  "config": {
    "install_script": "texto completo del script de instalación",
    "start_skill": "texto completo de las instrucciones de activación"
  }
}
```

Los demás campos opcionales admitidos dentro de `config` son `network`,
`environment_variables`, `secrets` y `repositories`. No son necesarios para
guardar los dos campos de scripts y se omitieron para preservar su configuración.
La herramienta selecciona el entorno mediante metadatos de la conversación;
no admite añadir `environment_id`, `draft_id` ni identificadores inventados.
Un guardado confirmado debe devolver `status: "saved"` y `draft_id`.

Se revisó `/workspace/android-dev/codex-environment-config.json`: contiene solo
`install_script` y `start_skill`, ambos strings no vacíos. El script pasa
`bash -n`. Ese JSON contiene los campos internos, no el envoltorio `config`:
se envió oficialmente como `{"config": contenido_del_archivo}`.
El contenido utiliza rutas absolutas bajo `/workspace/android-dev` y genera un
ejemplo externo; esos archivos no equivalen a archivos versionados del proyecto.
No se detectaron campos ajenos al esquema. El archivo revisado se conserva
sin cambios como evidencia de la propuesta anterior.

En esta investigación, dos lecturas con `{}` devolvieron `INVALID_ARGUMENT` y
`The current environment draft could not be verified. Retry the read before
editing the configuration.` Un único nuevo intento de actualización con los dos
campos válidos devolvió `INVALID_ARGUMENT` y `Setup could not be completed;
saving the draft could not be confirmed.` No se hicieron más intentos de escritura.

El estado de ejecución sí identifica una configuración publicada de origen y
una política de red vigente. Eso no demuestra que el borrador editable pueda
resolverse. Como falla incluso la lectura sin contenido de scripts, el rechazo
no demuestra un error de sintaxis del JSON ni de nombres de campos. La evidencia
apunta al servicio o a la resolución del borrador vinculado a esta conversación.
La causa interna exacta no está expuesta y no puede confirmarse desde estas
herramientas. No se modificaron red, secretos ni repositorios para sortearlo.

## Estado tras la publicación del entorno

El usuario confirmó la publicación del entorno y la carga de un nuevo borrador
basado en esa versión. En la instancia posterior se encontraron los scripts y
esta documentación conservados. El diagnóstico anterior describe el fallo de
guardado durante la preparación; no describe el estado del nuevo borrador.
No se volvió a aplicar la configuración anterior ni se publicó otra versión.

- **Archivos locales del checkout:** `scripts/setup-android.sh`,
  `scripts/android-env.sh` y esta documentación. No contienen binarios, cachés,
  logs, archivos temporales ni copias del SDK. Estos archivos y el enlace del
  README forman el cambio de infraestructura preparado para revisión en Git.
- **Instancia actual:** conserva el JDK, Gradle, SDK, cachés y ejemplo ya validados
  fuera del repositorio. Su existencia no prueba persistencia en una nueva tarea.
- **Configuración de Codex Cloud:** la publicación fue confirmada por el usuario.
  No se inspeccionaron ni modificaron los campos del nuevo borrador. El JSON
  externo sigue siendo un respaldo local de la propuesta anterior.
- **Restauración:** se comprobó la presencia de los archivos en la instancia
  posterior y la activación de Java, Gradle y SDK. Esto no equivale a repetir
  una instalación desde cero ni una compilación de Compose en otra tarea.

Como referencia para una futura edición explícita, el campo `install_script` puede
contener `cd /workspace/neobash-launcher && bash scripts/setup-android.sh`.
`start_skill` debe indicar usar el checkout existente, no crear worktrees,
ejecutar `source /workspace/neobash-launcher/scripts/android-env.sh` en cada shell
y realizar las comprobaciones anteriores. La configuración debe revisarse,
guardarse y publicarse mediante los controles oficiales del producto. Guardar
un borrador no ejecuta scripts ni publica una instantánea.

En una nueva tarea hay que verificar que estos archivos llegaron mediante el
checkout o la instantánea, ejecutar el instalador, abrir otra shell y activar
el entorno, comprobar las versiones y el SDK, y volver a compilar una aplicación
real o un ejemplo aislado de Compose. No debe afirmarse persistencia validada
hasta completar esa comprobación en una tarea distinta. Si se depende de Git
para transportar los scripts, antes deben incorporarse al repositorio remoto.
