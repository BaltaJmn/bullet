# Bobbin

Bullet Journal digital fiel al método de papel de Ryder Carroll: rapid logging, tres bullets, cinco
estados de tarea, migración a mano y entrada por entrada. Android + iOS, Compose Multiplatform sobre
Kotlin Multiplatform. Nombre de producto **Bobbin**, nombre de tienda **Bobbin: Bullet Journal**.
Identificador en las dos tiendas: `com.baltajmn.bullet`. Repositorio `BaltaJmn/bullet`, **público**.

Este fichero lo carga Claude Code solo en cualquier sesión abierta sobre este repositorio, desde
cualquier cuenta. Es el contexto permanente del proyecto: si algo hay que saber siempre, va aquí,
no en el chat. Un agente lanzado desde otro directorio no lo recibe: tiene que leerlo él.

Cuarta de la familia. Hermana de Purl (`../line`, `com.baltajmn.line`), Quilt (`../HabitTracker`,
`com.baltajmn.habit`) y MoodTraker (`../MoodTraker`, `com.baltajmn.mood`): misma arquitectura, misma
paleta, misma promesa. Casi todo el código de plataforma sale de **line**, la más reciente; se copia y
se adapta (`com.baltajmn.line` a `com.baltajmn.bullet`, Purl a Bobbin, tipos `Line*` a `Bobbin*`), no
se reescribe. Los repos hermanos se leen y **no se tocan nunca** desde aquí.

## Antes de escribir código

**No queda nada por decidir.** Todo está escrito en cuatro documentos, y el código los sigue:

| Documento | Qué manda |
|---|---|
| `SPEC.md` | El porqué de cada decisión de producto y el método traducido regla a regla. Arbitra cuando una issue y el código no coinciden |
| `docs/tecnico.md` | El contrato: identificadores, versiones, árbol de ficheros, formatos, algoritmos, puentes de plataforma, tests numerados (10) y qué secciones gobiernan cada issue (11) |
| `docs/pantallas.md` | La interfaz: cada pantalla con su estado vacío, los cuatro gestos, navegación, widgets e icono |
| `docs/textos.md` | Todos los textos en los cinco idiomas y el glosario fijo del método |

Se trabaja por issues de GitHub (`gh issue list -R BaltaJmn/bullet`), en el orden de `SPEC.md` 11.
Cada issue enlaza sus secciones. Lo marcado **[autor]** en una issue lo hace el humano, nunca el código.

Si el código necesita algo que los documentos no dicen, **se decide, se escribe en el documento que
toca en el mismo commit, y se sigue**. Un documento que se queda atrás del código deja de servir el
día que se abre la siguiente sesión.

## Dónde vive cada cosa

| Ruta | Qué es |
|---|---|
| `shared/src/commonMain/kotlin/com/baltajmn/bullet` | Toda la interfaz y toda la lógica. Es donde se trabaja por defecto |
| `.../model` | `Entry`, `Place`, `Journal`, `DayClock`, `Migration`, `RapidParse`: puro, sin disco ni plataforma |
| `.../data` | Almacén, estado de widgets, fusión, zip, exportar, bloqueo, recordatorio, compartir, copia |
| `.../ui` | Una pantalla por fichero, `BulletGlyph.kt`, `Pro.kt`, `theme/Theme.kt` |
| `shared/src/androidMain`, `shared/src/iosMain` | Solo los `actual` que el sistema obliga: almacén, bloqueo, recordatorio, compartir, selector de ficheros, widgets, compras |
| `androidApp` | `MainActivity` (`FragmentActivity`), manifiesto, recursos, icono. Nada de lógica |
| `iosApp/iosApp` | `iOSApp.swift`, `Info.plist`, `PrivacyInfo.xcprivacy`, `<lang>.lproj`. Grupo sincronizado |
| `iosApp/BobbinWidget` | Widgets de WidgetKit. Leen `widget.json` y nada más |
| `iosApp/Configuration/Config.xcconfig` | Versión, identificador y Team ID de iOS. No se editan en el `.pbxproj` |
| `docs/` | Contrato técnico, interfaz y textos |
| `store/` | Fichas, novedades, formularios, política, compras, CI, capturas y el checklist de lanzamiento |
| `tools/` | Scripts de ficha, capturas, diario de demostración e icono |
| `.github/workflows` | Tests en cada push, publicación por etiqueta, fichas |

## Contratos que no se rompen

- **El diario no sale del teléfono.** Ni analítica, ni informes de fallos, ni servidor, ni red salvo
  RevenueCat. Cualquier cambio a eso toca en el mismo commit `store/privacy/index.html`,
  `store/formularios.md` y `PrivacyInfo.xcprivacy`.
- **La migración es una decisión por tarea.** No existe `migrateAll` ni ninguna función de
  `model/Migration.kt` que acepte una lista. Migrar, programar y descartar nunca borran ni reescriben
  el original: solo cambian su estado y, si toca, crean una copia enlazada por `from`. Ni Hoy copia
  las tareas de ayer, ni el mes nuevo las del anterior, ni el Future Log mueve nada solo.
- **Cero configuración antes de escribir.** La app abre en Hoy con el campo enfocado y el teclado
  arriba. Todo ajuste tiene valor por defecto; ninguna pantalla manda a Ajustes para empezar.
- `journal.json` vive en `filesDir` (Android) y `Application Support` (iOS), **nunca** en el App
  Group. Se escribe tras cada cambio, de forma atómica (`.tmp` y rename), con `.bak`, un solo
  escritor, y un fichero ilegible se pone en cuarentena, no se sobrescribe (`docs/tecnico.md` 6.14).
- `widget.json` es el único contrato con los widgets y vive en el App Group: números, fechas y
  booleanos, **nunca texto del diario**. Un campo nuevo se añade en `WidgetState` y en
  `BobbinStore.swift` en el mismo commit. Los widgets no escriben nunca.
- El día lógico empieza a las **04:00** por defecto (ajuste). Las fechas son ISO **locales**.
- El texto de una entrada es de una línea, con tope de **500** puntos de código que se aplica al
  capturar y al editar, nunca al leer ni al importar, y el corte nunca parte una pareja suplente.
- **Importar nunca borra**: fusiona por `id`, gana el `updatedAt` más reciente, un estado cerrado no
  vuelve a `OPEN`, y se enseña un resumen antes de tocar nada (`docs/tecnico.md` 6.9).
- Lo que nunca se cobra: el método entero, exportar e importar, compartir, el recordatorio y el
  bloqueo. Cambiar qué es Pro toca en el mismo commit `SPEC.md` 7, los textos `pro*`, las dos fichas
  y `store/revenuecat.md`. El estado de compra nunca bloquea contenido: se guarda el último derecho
  conocido.
- `Strings.kt` obliga a los cinco idiomas (en, es, pt, de, fr) por firma de función, y se copia de
  `docs/textos.md`. Los símbolos del método no se traducen. Los textos **no** salen de Compose
  Resources: parte se pinta fuera de un `@Composable` (receptor, Glance, `Canvas`, notificación).
- **El `versionCode` no se reutiliza nunca**, ni entre canales de Play.
- La descripción larga de Play **conserva los saltos de línea tal cual**: cada párrafo de
  `store/listings/` va en una sola línea.

## Superficies del sistema

Todo lo que se ve fuera de la app lee y escribe por `BobbinRepository`, nunca por su cuenta.

| Superficie | Dónde | Nota |
|---|---|---|
| Widget de hoy | `widget/TodayWidget.kt` (Glance), `BobbinWidget.swift` | Gratis. Abiertas, hechas y eventos de hoy, sin texto |
| Widget del mes | `widget/MonthWidget.kt`, `BobbinMonthWidget.swift` | Pro. Se pinta bloqueado sin Pro |
| Widget de pantalla de bloqueo | `BobbinWidget.swift` | Pro. Solo iOS |
| Recordatorio de reflexión | `Reminders.android.kt` + receptores, `Reminders.ios.kt` | Apagado por defecto. Texto fijo, nunca cita el diario. Android se reprograma al arrancar, al reiniciar y al cambiar de hora |
| Enlaces | `bobbin://today?focus`, `bobbin://review`, `bobbin://pro` | El esquema es `bobbin://` |

Reglas que cuestan una tarde si se olvidan:

- La vista que tapa la multitarea en iOS se pone desde `iOSApp.swift`, no desde Compose, que no
  llega a repintar antes de la foto del sistema. En Android, `setRecentsScreenshotEnabled(false)`.
- `purchases-kmp` 3.2.1 necesita el `-L` al toolchain de Swift en los binarios de test de iOS
  (`shared/build.gradle.kts`, copiado de line).
- `CADisableMinimumFrameDurationOnPhone` tiene que estar en el `Info.plist` y valer `true`, o la app
  se cierra sola al arrancar.
- `plutil -extract` sin `-o -` reescribe el fichero de entrada.
- Todo lo que convive con el teclado scrollea con `imePadding()` y `verticalScroll`.
- Glance no tiene borde ni lienzo: la rejilla del widget del mes es un `Bitmap`. La previsualización
  del selector de widgets de Android es un XML mantenido a mano.
- La capa `<monochrome>` del icono adaptativo es una máscara de un solo color.
- Los textos de `AppIntents` (v1.1) tienen que ser literales; las traducciones, en
  `<lang>.lproj/Localizable.strings`.

## Seguridad, sin excepciones

El repositorio es público: todo lo que entra en su historia se queda.

- El `.jks` de firma, `keystore.properties`, `local.properties`, las claves `.p8` y las cuentas de
  servicio nunca se suben. Están en `.gitignore`.
- Los secretos viven solo en GitHub repository secrets, listados uno a uno en cada workflow, nunca
  con `secrets: inherit`.
- La clave secreta de RevenueCat (`sk_...`) nunca entra en el repositorio. Solo las públicas
  (`goog_`, `appl_`), que ya viajan dentro del binario.
- La cuenta de servicio que publica en Play es distinta de la de RevenueCat, que es de solo lectura a
  propósito. No se juntan ni se usan una para el trabajo de la otra.
- Nada de force push, nada de reescribir historia.

## Cómo se marcan los cambios

El registro es `git log`, no un fichero paralelo que se desincroniza al segundo día.

- Un commit por issue, con título `<ámbito>: <qué> (#N)`.
- El cuerpo explica **por qué**, no qué. El diff ya dice qué.
- `Closes #N` si la issue queda completa; `Refs #N` si queda algo del autor, que se le deja escrito
  en un comentario de la issue.
- Cada commit hecho con Claude termina con la línea `Co-Authored-By` del modelo que lo hizo.

## Estilo

- Documentación y commits en español, con tildes, salvo el material de tienda en otros idiomas.
- Sin em dash, en dash, comillas tipográficas, flechas ni emoji en nada que escriba Claude: texto,
  comentarios y commits. En los tests, un emoji se escribe con su escape (`\uD83D\uDE42`).
- Comentarios: solo los que explican una decisión que el código no puede explicar solo.
- Código, comentarios y nombres en inglés, como en las hermanas.
- No se tocan versiones de dependencias salvo que una issue lo pida: subir de versión es una tarea
  para toda la familia a la vez.

## Comandos

```bash
./gradlew :shared:testAndroidHostTest          # tests comunes sobre JVM, el rápido
./gradlew :shared:iosSimulatorArm64Test        # tests comunes sobre Kotlin/Native
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:bundleRelease            # necesita keystore.properties
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

Iterar con el objetivo mínimo del módulo tocado: lógica común, `testAndroidHostTest`; Android o
Compose, `assembleDebug`; `iosApp` o `iosMain`, el `xcodebuild`. El build de todo, una vez al final.

Publicar: `git tag v1.0.0 && git push origin v1.0.0` dispara Play (`alpha`) y TestFlight. El de iOS
se salta solo mientras no existan los secretos de Apple (`store/ci.md`). Las etiquetas las pone el
autor, nunca un agente.

Ficha de tienda:

```bash
python3 tools/play-listing/subir.py                        # comprueba los topes, no toca Play
gh workflow run listings.yml --ref main -f accion=estado   # lee en qué canal está cada versión
gh workflow run listings.yml --ref main -f accion=subir    # escribe la ficha en Play
```
