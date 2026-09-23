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

## Documentos

- `SPEC.md`: qué hace la app, el método regla a regla y por qué.
- `docs/tecnico.md`, `docs/pantallas.md` y `docs/textos.md`: el contrato que sigue el código.
- `store/`: fichas, política de privacidad, formularios y el checklist de lanzamiento.
- `CLAUDE.md`: las reglas de trabajo del repositorio.
