# Desarrollo de Neobash

- Android nativo Kotlin/Compose. Mantener applicationId `site.neobash.launcher`.
- Un módulo app; separar core/model, core/data, core/designsystem y feature/*.
- Estado de pantalla mediante ViewModel y StateFlow; IO fuera del hilo principal.
- Persistir preferencias en DataStore. No guardar Context de Activity en repositorios.
- Mantener datos de clima y astronomía explícitamente simulados hasta sustituir proveedores.
- Sin backend, permisos de micrófono/ubicación, accesibilidad, root ni servicios persistentes.
- Inspeccionar cambios existentes antes de editarlos. Nunca versionar APK, SDK, cachés o secretos.
- Activar scripts/android-env.sh; ejecutar assembleDebug, testDebugUnitTest y lintDebug.
- No afirmar pruebas físicas o compatibilidad de gestos sin evidencia en dispositivo.
- No fusionar PR automáticamente.
