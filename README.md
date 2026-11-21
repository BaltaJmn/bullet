# Bobbin

Un Bullet Journal para Android e iOS, fiel al método de papel: apuntas en rapid logging con tareas,
eventos y notas, y a final de mes decides una a una qué tareas merecen seguir. Sin cuenta, sin
servidor y sin anuncios: el diario no sale del teléfono.

Compose Multiplatform sobre Kotlin Multiplatform, hermana de Purl, Quilt y MoodTraker: una sola
interfaz para las dos plataformas, con `expect`/`actual` solo donde el sistema obliga.

## Compilar

```bash
./gradlew :shared:testAndroidHostTest     # tests comunes sobre JVM
./gradlew :shared:iosSimulatorArm64Test   # tests comunes sobre Kotlin/Native
./gradlew :androidApp:assembleDebug
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

## Sin probar

Lo que los tests no pueden ver porque depende de un aparato, una cuenta o un servicio real: el emulador y
el simulador no traen biometría real ni Play Billing, y los widgets y el recordatorio se portan distinto
en un teléfono de verdad. Los cuatro primeros puntos son los de la issue #61 (verificación en un aparato
físico); el resto es lo nuevo o lo que aún no se ha visto. Cada punto sigue abierto hasta que alguien lo
pruebe y lo tache aquí.

- **El bloqueo biométrico** en un Android y un iPhone reales: que bloquee al volver de segundo plano y que
  la huella o Face ID funcionen igual que en el emulador.
- **El recordatorio de reflexión** en los dos aparatos: que la notificación suene y que sobreviva a
  reiniciar el teléfono.
- **El widget de hoy** en la pantalla de inicio real de un Android y de un iPhone: que se actualice al
  escribir en Hoy. Lo mismo para el widget del mes y el de pantalla de bloqueo (Pro), y para el widget
  "Escribir" de Android y el control de iOS 18 (gratis).
- **Compartir** un día y compartir texto a Bobbin (Android) contra al menos dos apps de destino distintas.
- **La compra de Pro** (`bullet_pro`): en iOS la compra se grabó en sandbox el 30-10-2026 y falta
  "Restaurar compra" (`store/lanzamiento.md`); en Play falta con un probador de licencias. Y, desde la
  1.0.1, que la hoja enseñe el precio, que "Reintentar" salga si la tienda no contesta y que los enlaces
  de condiciones y privacidad abran. `iosApp/Products.storekit` tiene el producto para probarlo en el
  simulador (Xcode, esquema `iosApp`, Opciones, StoreKit Configuration; si no sale en la lista, añade
  antes el fichero al proyecto); no hay prueba de interfaz que recorra el muro.
- **La sincronización de Pro** por iCloud Drive o por la carpeta elegida, con dos aparatos reales y un
  cambio en cada lado.
- **La petición de valoración**: en iOS la hoja del sistema sale siempre en una build de desarrollo y
  nunca en TestFlight; en Android solo se ve en una versión instalada desde Play (vale la pista interna).
  Que salga en producción con las condiciones de la app no se ha visto.

## Documentos

- `SPEC.md`: qué hace la app, el método regla a regla y por qué.
- `docs/tecnico.md`, `docs/pantallas.md` y `docs/textos.md`: el contrato que sigue el código.
- `store/`: fichas, política de privacidad, formularios y el checklist de lanzamiento.
- `CLAUDE.md`: las reglas de trabajo del repositorio.
