# Compatibilidad y pruebas pendientes

Soporte declarado: Android 8.0/API 26 o superior, teléfonos y tablets. El build
no demuestra compatibilidad física. No se ejecutó en un teléfono ni emulador.

| Entorno | Estado / estrategia |
| --- | --- |
| Android estándar | Pendiente: HOME, Back, reinicio, apps instaladas, cambios de paquete, rotación, fuente grande y tablet |
| Xiaomi HyperOS 3 | Pendiente; el fabricante puede restringir gestos con launchers de terceros. Probar navegación de tres botones como alternativa |
| Huawei con Android/EMUI/HarmonyOS compatible APK | Pendiente; comprobar instalación y selección HOME según región y política del fabricante |
| HarmonyOS NEXT sin compatibilidad Android | Esta app Android no ofrece soporte nativo |

Usar dispositivos sin root. Elegir HOME desde Ajustes, conservar acceso al
launcher original y probar botones antes de gestos. No modificar SystemUI,
Quickstep ni permisos privilegiados. No hay servicios de accesibilidad.

La primera versión consulta solo apps iniciables del usuario actual; perfiles
de trabajo, accesos directos y paquetes ocultos requieren futura integración
con LauncherApps y pruebas específicas. La lista se actualiza al reanudar,
no en tiempo real mientras permanece visible. Si una app desaparece, el fallo
de apertura se captura y el favorito permite nueva selección.
