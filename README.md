# Android2Car

Android2Car es una aplicación nativa para **Android 14 (API 34)**, escrita en **Kotlin** y **Jetpack Compose**, que proyecta la pantalla del móvil en la pantalla del coche a través de **Android Auto**.

- **Una pantalla, un botón:** el botón **Screen-share** comprueba que Android Auto está conectado y empieza a proyectar.
- **Modo claro / oscuro** con un interruptor; la preferencia se guarda con DataStore.
- **Indicador de estado:** desconectado, comprobando conexión, conectado, esperando al coche, proyectando, o alerta (en amarillo `#FFE817`).
- **Seguridad en conducción:** si el coche se mueve, la proyección se detiene y la app se cierra, tanto en el coche como en el móvil.
- **Sigue funcionando con la pantalla apagada:** un `ForegroundService` de tipo `mediaProjection` con `PARTIAL_WAKE_LOCK` mantiene viva la captura aunque bloquees el móvil.

> [!IMPORTANT]
> Es una herramienta para desarrollo o uso personal. Google Play no acepta apps de Android Auto que muestren contenido arbitrario. Hay que instalarla manualmente y activar **Fuentes desconocidas** en los ajustes de desarrollador de Android Auto (ver [Probar con el DHU](#probar-con-el-desktop-head-unit-dhu)).

---

## Requisitos previos

| Requisito | Versión |
|---|---|
| Móvil | Android 10 (API 29) o superior. La app está pensada y probada para Android 14 (`targetSdk 34`). |
| Android Auto | La app de Android Auto instalada en el móvil y un coche o head unit compatible (o el DHU). |
| JDK | 17 o superior. Gradle 9.7 también funciona con JDK 21 y 25. |
| Android SDK | Plataforma **Android 37.2** (`compileSdk`; la piden las librerías AndroidX actuales) y Build-Tools 36. |
| Para el DHU | *Android Auto Desktop Head Unit Emulator* (`extras;google;auto`) y `adb` (platform-tools). |

Compilar y ejecutar las pruebas:

```powershell
.\gradlew.bat assembleDebug
```

```powershell
.\gradlew.bat testDebugUnitTest
```

En macOS/Linux usa `./gradlew`. El SDK se busca en `local.properties` (`sdk.dir=...`) o en `ANDROID_HOME`.

**Firma de release:** copia `keystore.properties.example` a `keystore.properties` (ignorado por git) y rellena la ruta y las contraseñas de tu keystore. Sin ese archivo, `assembleRelease` genera un APK sin firmar.

---

## Permisos

| Permiso | Motivo |
|---|---|
| `FOREGROUND_SERVICE` y `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Android 14 exige que la captura se haga dentro de un servicio en primer plano de tipo `mediaProjection`. |
| Consentimiento de `MediaProjection` | Diálogo del sistema que aparece al pulsar Screen-share. En Android 14 se pide siempre la pantalla completa (`createConfigForDefaultDisplay`), porque compartir una sola app no serviría para el espejo. |
| `POST_NOTIFICATIONS` | Muestra la notificación persistente, que incluye el botón **Detener** (permiso en tiempo de ejecución desde Android 13). |
| `WAKE_LOCK` | Permite el `PARTIAL_WAKE_LOCK` que mantiene la CPU activa con la pantalla apagada. |
| `androidx.car.app.ACCESS_SURFACE` y `androidx.car.app.NAVIGATION_TEMPLATES` | Dan acceso a la superficie de dibujo de pantalla completa del coche, que solo se concede a apps de la categoría de navegación. |
| `com.google.android.gms.permission.CAR_SPEED` | Permite leer la velocidad del vehículo con `CarHardwareManager`. Se pide desde el coche la primera vez que abres la app. |

---

## Arquitectura

MVVM con capas separadas y un contenedor de dependencias manual (`di/AppContainer`) que comparten la UI del móvil, el servicio y la sesión del coche, porque los tres corren en el mismo proceso.

```
com.android2car
├── ui/        Compose + MVVM: MainActivity, MainScreen, StatusCard, MainViewModel, theme/
├── domain/    Kotlin puro y testeable
│   ├── model/       CarConnectionStatus, ProjectionState, VehicleMotion, ThemeMode…
│   ├── repository/  Contratos: CarConnectionRepository, ProjectionController, ThemeRepository
│   ├── usecase/     CheckCarConnectionUseCase
│   └── safety/      DrivingSafetyPolicy, MotionSafetyMonitor, SafetyShutdownCoordinator
├── data/      CarConnection (LiveData → Flow) y DataStore de preferencias
├── service/   ScreenProjectionService (FGS mediaProjection), ScreenMirror (VirtualDisplay),
│              WakeLockHolder, notificación y ServiceProjectionController
└── car/       Android2CarAppService, ProjectionCarSession, ProjectionCarScreen,
               CarSurfaceCallback/Registry y CarSpeedMotionSource (CarHardwareManager)
```

**Flujo de proyección**

1. `MainViewModel` llama a `CheckCarConnectionUseCase`, que usa `CarConnection` de la Car App Library. Si el tipo de conexión no es `PROJECTION`, muestra una alerta y no inicia la captura.
2. La actividad pide el permiso de notificaciones y el consentimiento de `MediaProjection`, y entrega el resultado al `ProjectionController`.
3. `ScreenProjectionService` pasa a primer plano (en Android 14 es obligatorio hacerlo antes de canjear el token), obtiene la `MediaProjection`, adquiere el wake lock y espera la superficie del coche.
4. Al abrir Android2Car en el coche, `ProjectionCarSession` registra un `SurfaceCallback` en un `NavigationTemplate`. La superficie se publica en `CarSurfaceRegistry`, y `ScreenMirror` crea un `VirtualDisplay` con `AUTO_MIRROR` que dibuja directamente sobre ella.
5. Android 14 solo permite **un** `createVirtualDisplay()` por token. Por eso, cuando el coche recrea su superficie, el `VirtualDisplay` existente se redirige con `resize()` y `setSurface()` en lugar de crear uno nuevo.

**Seguridad en conducción**

- `CarSpeedMotionSource` convierte los datos de `CarInfo.addSpeedListener` en un `Flow<VehicleMotion>`.
- `DrivingSafetyPolicy` considera insegura cualquier velocidad distinta de 0, también la marcha atrás. Con `stopOnUnknownSpeed`, un coche que no informa de su velocidad también cuenta como inseguro.
- `MotionSafetyMonitor` avisa del movimiento al instante. La falta de datos solo la notifica si dura más de 3 s, porque el host suele enviar un primer valor "desconocido".
- `SafetyShutdownCoordinator` detiene el servicio y emite el evento de cierre. El coche muestra un `CarToast` y llama a `finishCarApp()`, y el móvil ejecuta `finishAndRemoveTask()`.
- `BuildConfig.STOP_ON_UNKNOWN_SPEED` vale `true` en *release* (a prueba de fallos) y `false` en *debug*, para poder trabajar con el DHU, que normalmente no envía velocidad.

> Android Auto (modo proyectado) no expone la marcha ni el freno de mano a apps de terceros, así que el estado "estacionado" se deduce de la velocidad.

---

## Guía de uso

1. Instala la app en el móvil y conéctalo al coche (por cable o de forma inalámbrica) con Android Auto.
2. Abre **Android2Car** en el móvil. El indicador debe mostrar *Conectado a Android Auto*.
3. Pulsa **Screen-share**. Si Android Auto no está conectado, aparece una alerta amarilla y no se inicia nada.
4. Acepta el permiso de notificaciones (solo la primera vez) y el diálogo de captura de pantalla.
5. En la pantalla del coche, abre **Android2Car** desde el lanzador de apps. La primera vez te pedirá permiso para leer la velocidad del vehículo; acéptalo.
6. La pantalla del móvil aparece en el coche. Puedes bloquear el móvil y la transmisión sigue activa.
7. Para parar, usa **Detener Screen-share** en el móvil, **Detener** en el coche o **Detener** en la notificación.
8. Si el coche empieza a moverse, la proyección se detiene y la app se cierra sola.

---

## Probar con el Desktop Head Unit (DHU)

1. **Instala el DHU:** en Android Studio ve a *SDK Manager → SDK Tools → Android Auto Desktop Head Unit Emulator*, o ejecuta `sdkmanager "extras;google;auto"`. El ejecutable queda en `<SDK>/extras/google/auto/`.
2. **Activa el modo desarrollador de Android Auto en el móvil:** abre los *Ajustes de Android Auto* y toca **Versión** 10 veces.
3. Desde el menú ⋮ → *Ajustes de desarrollador*, activa **Fuentes desconocidas** para que Android Auto muestre apps instaladas manualmente.
4. Desde el menú ⋮, elige **Iniciar servidor de la unidad principal**.
5. Conecta el móvil por USB con la depuración activada, instala la app y redirige el puerto:

   ```powershell
   .\gradlew.bat installDebug
   ```

   ```powershell
   adb forward tcp:5277 tcp:5277
   ```

6. Arranca el DHU:

   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
   ```

7. En el móvil, abre Android2Car y pulsa **Screen-share**. En el DHU, abre **Android2Car** desde el lanzador y verás la pantalla del móvil en tiempo real.
8. **Prueba de pantalla apagada:** con la proyección activa, pulsa el botón de encendido del móvil. El servicio y la notificación siguen activos (lo puedes comprobar con `adb shell dumpsys activity services com.android2car`).
9. **Prueba del cierre de seguridad:** el DHU no simula velocidad, así que la variante *debug* tolera la falta de datos. Para comprobar el cierre de punta a punta, instala la variante *release* (`.\gradlew.bat installRelease`, con `keystore.properties` configurado, o el APK de la release de GitHub). Si el DHU no informa de la velocidad, la app se cerrará a los 3 s con el aviso *"Proyección detenida"*. La lógica de movimiento también está cubierta por pruebas unitarias.

---

## Pruebas

`./gradlew testDebugUnitTest` ejecuta las pruebas JVM de la lógica crítica:

- `DrivingSafetyPolicyTest` y `MotionSafetyMonitorTest`: velocidad, marcha atrás, tolerancia, velocidad desconocida con periodo de gracia (usan tiempo virtual).
- `SpeedMappingTest`: conversión de `Speed`/`CarValue` de la Car App Library al modelo de dominio.
- `SafetyShutdownCoordinatorTest`: el cierre detiene la proyección y emite el evento.
- `CarConnectionTypeMapperTest` y `CheckCarConnectionUseCaseTest`: estados de conexión y *timeout*.
- `MainViewModelTest` y `ResolveStatusTest`: flujo del botón, consentimiento, alertas, cierre automático y persistencia del tema.

---

## Limitaciones conocidas

- **No se reenvían los toques:** la pantalla del coche solo muestra la imagen; el móvil se maneja desde el propio móvil.
- **Contenido protegido:** las apps con `FLAG_SECURE` o DRM (bancos, plataformas de vídeo) se ven en negro, por decisión del sistema.
- **Pantalla apagada:** el servicio, la captura y el wake lock siguen activos. Sin embargo, algunos fabricantes dejan de componer la pantalla física mientras está apagada, y en esos casos el coche puede mostrar el último fotograma o la pantalla de bloqueo.
- **Versiones posteriores a Android 14:** pueden aplicar restricciones extra a la captura (por ejemplo, ocultar notificaciones sensibles o detenerla al bloquear el móvil).
- **Relación de aspecto:** el espejo mantiene la proporción del móvil, así que en la pantalla horizontal del coche aparecen bandas laterales.

## Licencia

[MIT](LICENSE)
