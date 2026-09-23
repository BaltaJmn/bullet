# Contrato técnico de Bobbin

Todo lo que el código tiene que respetar, fichero a fichero. El porqué de cada decisión está en
`SPEC.md`; la interfaz, en `docs/pantallas.md`; los textos, en `docs/textos.md`. Si el código necesita
algo que aquí no está, se decide, se escribe aquí en el mismo cambio y se sigue. Si un detalle de
implementación de `SPEC.md` no coincide con este documento, manda este y el SPEC se corrige en el
mismo cambio.

Las rutas de las hermanas son relativas a `/Users/baltajmn/AndroidStudioProjects/`: `line/` (Purl, la
más reciente y origen de casi todo), `MoodTraker/` y `HabitTracker/` (Quilt). Se leen, nunca se tocan.

---

## 1. Identificadores

Irreversibles en cuanto se publica la primera build (SPEC §8).

| Qué | Valor |
|---|---|
| Nombre visible | `Bobbin` (los cinco idiomas) |
| Nombre de tienda | `Bobbin: Bullet Journal` |
| `applicationId` y `namespace` de `androidApp` | `com.baltajmn.bullet` |
| `namespace` de `shared` | `com.baltajmn.bullet.shared` |
| Paquete Kotlin | `com.baltajmn.bullet` |
| `rootProject.name` | `Bobbin` |
| Bundle id de la app iOS | `com.baltajmn.bullet` (`APP_BUNDLE_ID` en `Config.xcconfig`) |
| Bundle id del widget iOS | `com.baltajmn.bullet.widget` (`$(APP_BUNDLE_ID).widget`) |
| Target y producto del widget | `BobbinWidget`, `BobbinWidgetExtension` |
| App Group | `group.com.baltajmn.bullet`. Solo contiene `widget.json` (4.2) |
| Kinds de WidgetKit | `BobbinTodayWidget`, `BobbinMonthWidget`, `BobbinLockWidget` |
| Esquema de URL | `bobbin` (`bobbin://today?focus`, `bobbin://review`, `bobbin://pro`) |
| Canal de notificación Android | `bobbin-reflection` |
| Identificador de la notificación iOS | `bobbin-reflection` |
| Producto RevenueCat | `bullet_pro`, no consumible, solo en el panel |
| Entitlement RevenueCat | `pro` (offering `default`) |
| Política de privacidad | `https://bullet.baltajmn.dev/` |
| Nombre del zip de copia | `bobbin-AAAA-MM-DD.zip` |
| Fichero de sincronización (v1.2) | `bobbin-sync.json` |
| Nombre del libro (v1.1) | `bobbin-book-AAAA-MM-DD.pdf` |
| `versionCode` / `versionName` inicial | `1` / `1.0.0`; iOS `MARKETING_VERSION = 1.0.0`, `CURRENT_PROJECT_VERSION = 1` |

El `versionCode` **no se reutiliza nunca**, ni entre pistas de Play: una versión subida a `internal`
no puede volver a subirse a `alpha` (Play responde "Version code N has already been used"). Vive solo
en `androidApp/build.gradle.kts` y lo sube quien etiqueta, antes de etiquetar (9).

El nombre del producto solo vive en los textos, la ficha, el icono, el esquema `bobbin://` y los
nombres de fichero de copia. Si el autor pasa a la reserva (Skein, SPEC §8), se cambian esos y nada
más: ningún identificador de esta tabla que empiece por `com.baltajmn.bullet` depende del nombre.

---

## 2. Versiones y dependencias

Las de la familia, sin tocar. Subir de versión es una tarea aparte para las cuatro apps a la vez.

`gradle/libs.versions.toml` es el de `line/gradle/libs.versions.toml` tal cual, con estas versiones:

| Clave | Versión |
|---|---|
| `kotlin` | `2.4.10` |
| `agp` | `9.0.1` |
| `composeMultiplatform` | `1.11.1` |
| `material3` | `1.11.0-alpha07` |
| `glance` | `1.1.1` |
| `kotlinx-datetime` | `0.8.0` |
| `kotlinx-serialization` | `1.11.0` |
| `purchases-kmp` | `3.2.1` |
| `androidx-activity` | `1.13.0` |
| `androidx-appcompat` | `1.7.1` |
| `androidx-biometric` | `1.1.0` |
| `androidx-core` | `1.17.0` |
| `androidx-espresso` | `3.7.0` |
| `androidx-fragment` | `1.9.0` |
| `androidx-lifecycle` | `2.11.0-beta01` |
| `androidx-testExt` | `1.3.0` |
| `junit` | `4.13.2` |
| `android-compileSdk` / `android-targetSdk` / `android-minSdk` | `36` / `36` / `24` |

Gradle `9.1.0` (`gradle/wrapper/gradle-wrapper.properties`), JVM target 11.

`androidx.biometric` 1.1.0 es la única versión estable (enero de 2021); `biometric-compose` y
`registerForAuthenticationResult` están en alpha y no se usan. `fragment-ktx` se declara explícito
porque `MainActivity` es `FragmentActivity` y la versión que arrastra biometric 1.1.0 es de 2020.

**Una sola dependencia nueva respecto a line**, y solo cuando llegue #56: Play In-App Review.

```toml
[versions]
play-review = "2.0.2"

[libraries]
play-review = { module = "com.google.android.play:review", version.ref = "play-review" }
```

Va solo en `androidMain` de `shared`. iOS usa `SKStoreReviewController`, que es del sistema.

`shared/build.gradle.kts`: copia de `line/shared/build.gradle.kts`, comentario incluido. Cambios:
`namespace = "com.baltajmn.bullet.shared"` y, en #56, `implementation(libs.play.review)` en
`androidMain`. Lo que no se toca:

- **El apaño del enlazador de purchases-kmp 3.2.1.** El artefacto trae en su manifiesto de cinterop un
  `linkerOpts` con la ruta absoluta al Xcode de la máquina de RevenueCat; el enlace de los binarios de
  test de iOS muere por símbolos de Swift sin definir (`swiftCompatibility56` y parecidos). Se añade
  `-L$(xcode-select -p)/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/<sdk>` **solo en Mac y solo
  a los binarios de test** (`TestExecutable`). El framework de la app es estático y lo enlaza Xcode,
  que ya busca su propio toolchain. Preguntar a `xcode-select` en Linux tumbaría la configuración del
  proyecto entero, de ahí el `isMacHost`.
- `compose.components.resources` en `commonMain`, para la fuente Literata.
- `glance-appwidget`, `biometric`, `fragment` y `core-ktx` en `androidMain`.

`androidApp/build.gradle.kts`: copia del de line. Cambios: `applicationId`, `namespace`,
`versionCode = 1`, `versionName = "1.0.0"`. La firma lee `storeFile`, `storePassword`, `keyAlias` y
`keyPassword` de `keystore.properties` (ignorado por git) y, sin ese fichero, cae a la firma de debug:
Play rechaza una subida firmada con debug, así que no llega a la tienda por accidente.

`build.gradle.kts`, `settings.gradle.kts` (con `rootProject.name = "Bobbin"`), `gradle.properties`,
`gradlew`, `gradlew.bat` y `gradle/`: copia de line.

---

## 3. Árbol de ficheros

`C` copia casi literal de la hermana indicada, `A` adaptación, `N` nuevo. Adaptar es cambiar
`com.baltajmn.line` por `com.baltajmn.bullet`, Purl por Bobbin y los tipos `Line*` por `Bobbin*`, y lo
que diga la columna. Lo que ya existe en line se copia y se adapta, no se reescribe.

No se copian de line `model/Text.kt`, `model/Insights.kt`, `data/Merge.kt`, `data/Photos.kt`,
`data/ReminderPlan.kt`, `ui/YearGrid.kt`, `ui/DaySheet.kt`, `ui/Photo.kt` ni el importador de
MoodTraker: son de otro producto. De `model/Text.kt` se toman solo cuatro funciones, que viven donde
dice la tabla (`codePointCount`, `clampCodePoints` y `limitEdit` en `model/Entry.kt`; `fold` en
`data/Search.kt`).

### `shared/src/commonMain/kotlin/com/baltajmn/bullet`

| Fichero | Qué hace | Origen |
|---|---|---|
| `App.kt` | `enum class Screen` con los diez destinos, barra de cuatro, pila con `BackHandler`, puerta de bloqueo, `LifecycleEventEffect` de `ON_RESUME` y `ON_STOP`, `Route` | A `line/.../App.kt` |
| `model/Entry.kt` | `Entry`, `Bullet`, `TaskStatus`, `Signifier`, `oneLine`, `codePointCount`, `clampCodePoints`, `limitEdit`, `TEXT_LIMIT` | N; las tres funciones de texto, C `line/.../model/Text.kt` |
| `model/Place.kt` | `Place` sellado y su serializador | N |
| `model/Journal.kt` | `Journal`, `BulletCollection`, `CollectionKind`, `TrackerRow`, `Settings`, `JournalJson`, `newId`, operaciones puras de entrada | N |
| `model/DayClock.kt` | `logicalDate`, `nextDayStart`, `monthOf`, `monthDays`, `weekStarts`, `firstDayOfWeek` | A `line/.../model/DayClock.kt` |
| `model/Migration.kt` | `migrate`, `schedule`, `discard`, `migrationCount`, `openTasksBefore`, `openTasksOfDay`, `openTasksOfMonth`, `unclosedMonth`, `futureWaiting` | N |
| `model/RapidParse.kt` | `rapidParse` | N |
| `model/Collections.kt` | colecciones, índice, hilo, seguimientos, `FREE_TRACKER_LIMIT` | N |
| `data/Storage.kt` | `interface JournalFiles`, `expect object Storage`, `load`, `SCHEMA_STEPS` | A `line/.../data/Storage.kt` |
| `data/Prefs.kt` | `expect object Prefs`: lo que vive fuera del diario (Pro, valoración pedida) | N |
| `data/BobbinRepository.kt` | `object BobbinRepository`: estado, escritor único, deshacer, Pro | A `line/.../data/LineRepository.kt` |
| `data/Search.kt` | `fold`, `tags`, `search` | A `line/.../data/Search.kt`; `fold` C `line/.../model/Text.kt` |
| `data/WidgetState.kt` | `WidgetState`, `WidgetJson`, `widgetState`, `widgetView` | A `line/.../data/WidgetState.kt` |
| `data/Widgets.kt` | `expect fun writeWidgetState`, `expect fun refreshWidgets`, `syncWidgets` | C `line/.../data/Widgets.kt` |
| `data/Merge.kt` | `merge`, `MergeResult` | N |
| `data/Zip.kt` | `ZipWriter`, `ZipReader`, `crc32`, `ZipDamaged` | C `line/.../data/Zip.kt` |
| `data/Export.kt` | `exportName`, `monthMarkdown`, `collectionMarkdown`, `exportZip`, `readBackup`, `ImportProblem` | A `line/.../data/Export.kt` |
| `data/FilePicker.kt` | `expect object FilePicker`, `PickResult` | C `line/.../data/FilePicker.kt` |
| `data/Backup.kt` | `expect object Backup`: qué queda fuera de la copia del sistema | N |
| `data/Lock.kt` | `expect object Lock` | C `line/.../data/Lock.kt` |
| `data/Reminders.kt` | `expect object Reminders`, `nextReminder`, `NotifyPermission` | A `line/.../data/Reminder.kt` |
| `data/Sharing.kt` | `expect fun ImageBitmap.encodeToPng()`, `expect object Sharing`, `shareText`, `paginate` | A `line/.../share/Sharing.kt` |
| `data/StoreReview.kt` | `expect object StoreReview`, `shouldAskReview` | N |
| `data/Route.kt` | `object Route`: a qué pantalla pide ir un widget, un enlace o la notificación | A `line/.../data/Route.kt` |
| `data/AppInfo.kt` | `PRIVACY_URL`, `SIBLINGS`, `expect object AppInfo` | A `line/.../data/AppInfo.kt` |
| `data/SiblingImport.kt` | v1.2: leer las copias de Purl, MoodTraker y Quilt (4.5, 12.10) | N |
| `billing/Billing.kt` | `expect val revenueCatApiKey`, `object Billing` | A `line/.../billing/Billing.kt`: el derecho va a `Prefs`, no al diario |
| `i18n/Strings.kt` | `expect fun systemLanguage()`, `expect fun systemFirstDayOfWeek()`, `object S` con `t(en, es, pt, de, fr)`, plurales y nombres de mes y día | A `line/.../i18n/Strings.kt` |
| `ui/theme/Theme.kt` | `BobbinTheme`, colores, `enum class Cover`, `enum class Paper`, `isWideScreen`, `MAX_CONTENT_WIDTH` | A `line/.../ui/theme/Theme.kt` |
| `ui/theme/Grid.kt` | `gridUnit` | N (#17) |
| `ui/theme/Paper.kt` | modificador que pinta el papel | N (#17) |
| `ui/theme/Type.kt` | `Ink`, `PageTitle`, `Body`, `Secondary`, `Eyebrow` | N (#17) |
| `ui/BulletGlyph.kt` | los glifos del método en `Canvas` | N |
| `ui/Icons.kt` | los pocos iconos de la cabecera | A `line/.../ui/Icons.kt` |
| `ui/EntryList.kt` | la lista de entradas y el campo de captura, compartidos por Hoy, Mes y Colección | N; el campo, A `line/.../ui/LineField.kt` |
| `ui/EntrySheet.kt` | la hoja de la pulsación larga | N |
| `ui/TodayScreen.kt` | Hoy | N, con el patrón de foco de `line/.../ui/TodayScreen.kt` |
| `ui/MonthScreen.kt` | Mes | N |
| `ui/FutureScreen.kt` | Futuro | N |
| `ui/IndexScreen.kt` | Índice | N |
| `ui/CollectionScreen.kt` | una colección, o un seguimiento | N |
| `ui/ReviewScreen.kt` | reflexión y revisión tarea a tarea, y la del Future Log | N |
| `ui/SearchScreen.kt` | búsqueda | N |
| `ui/KeyScreen.kt` | la clave de símbolos | N |
| `ui/SettingsScreen.kt` | Ajustes | A `line/.../ui/SettingsScreen.kt` |
| `ui/LockScreen.kt` | overlay de bloqueo | C `line/.../ui/LockScreen.kt` |
| `ui/Pro.kt` | `ProDialog` | A `line/.../ui/Pro.kt` |
| `ui/ShareScreen.kt` | vista previa y botones de compartir | A `line/.../ui/ShareScreen.kt` |
| `ui/SharePage.kt` | `renderSharePages`: la página punteada como imagen | A `line/.../share/ShareCard.kt` |

`shared/src/commonMain/composeResources/font/literata_regular.ttf` y su licencia en
`composeResources/files/OFL.txt`: C line (Literata Regular, SIL Open Font License 1.1). La licencia va
en `files/` y no en `font/`, donde el generador la tomaría por una fuente más. Se borra
`composeResources/drawable/compose-multiplatform.xml` de la plantilla.

### `shared/src/androidMain/kotlin/com/baltajmn/bullet`

| Fichero | Origen |
|---|---|
| `data/AndroidContext.kt` | C line |
| `data/AppInfo.android.kt` | C line |
| `data/FilePicker.android.kt` | C line: `OpenDocument` para importar, `CreateDocument("application/zip")` para exportar |
| `data/Storage.android.kt` | A line: los nombres de 6.14 en `filesDir`, `keepCopy`, `readCopy` y `wipe` |
| `data/Prefs.android.kt` | N: `SharedPreferences` `bobbin` |
| `data/Backup.android.kt` | N: no hace nada; lo dicen las reglas de 8.2 |
| `data/Lock.android.kt` | C line |
| `data/Reminders.android.kt` y `data/ReminderReceiver.kt` (con `BootReceiver`) | A line: texto fijo, sin comprobar si el día está escrito |
| `data/Widgets.android.kt` | A line: `widget.json` en `filesDir`, `updateAll` de los dos widgets y la alarma del cambio de día (6.13) |
| `data/StoreReview.android.kt` | N: `ReviewManagerFactory` (#56) |
| `data/Sharing.android.kt` | A line `share/Sharing.android.kt`: varias imágenes con `ACTION_SEND_MULTIPLE` y texto con `ACTION_SEND` |
| `widget/TodayWidget.kt` | A `line/.../widget/TodayWidget.kt` |
| `widget/MonthWidget.kt` | A `line/.../widget/YearWidget.kt`: la rejilla del mes es un `Bitmap` |
| `i18n/Strings.android.kt` | A line, más `systemFirstDayOfWeek` |
| `billing/Billing.android.kt` | C line, clave `goog_...` o `null` |
| `res/drawable/ic_notification.xml` | N, lo genera `tools/generate_icons.py` (#18) |

### `shared/src/iosMain/kotlin/com/baltajmn/bullet`

| Fichero | Origen |
|---|---|
| `MainViewController.kt` | C line |
| `data/BobbinBridge.kt` | A `line/.../data/LineBridge.kt` (sección 7) |
| `data/AppInfo.ios.kt`, `data/FilePicker.ios.kt` | C line |
| `data/Storage.ios.kt` | A line: Application Support, **no** el App Group |
| `data/Prefs.ios.kt` | N: `NSUserDefaults.standardUserDefaults` |
| `data/Backup.ios.kt` | N: `NSURLIsExcludedFromBackupKey` |
| `data/Lock.ios.kt` | C line |
| `data/Reminders.ios.kt` | N: un `UNCalendarNotificationTrigger` que se repite (6.12) |
| `data/Widgets.ios.kt` | A line: `widget.json` en el App Group y `WidgetCenter` vía `BobbinBridge` |
| `data/StoreReview.ios.kt` | N: `SKStoreReviewController.requestReviewInScene` |
| `data/Sharing.ios.kt` | A line `share/Sharing.ios.kt`: `UIActivityViewController` con varias imágenes o texto |
| `i18n/Strings.ios.kt` | A line, más `systemFirstDayOfWeek` |
| `billing/Billing.ios.kt` | C line, clave `appl_...` o `null` |

### Tests

| Fichero | Qué cubre (sección 10) |
|---|---|
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/ModelTest.kt` | tests 1, 2, 3, 4, 5, 27, 30, 34 |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/MigrationTest.kt` | tests 6, 7, 8, 28, 33 |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/DataTest.kt` | tests 10 a 20, 29, 31, 35 |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/StorageTest.kt` | tests 21, 22, 32 |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/WidgetSample.kt` | `WIDGET_SAMPLE`, el fichero de ejemplo de `widget.json` (test 12 y 36) |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/i18n/StringsTest.kt` | test 24 (C `line/.../i18n/StringsTest.kt`) |
| `shared/src/commonTest/kotlin/com/baltajmn/bullet/ThemeTest.kt` | test 25 |
| `shared/src/androidHostTest/kotlin/com/baltajmn/bullet/NoBulkTest.kt` | test 9 |
| `shared/src/androidHostTest/kotlin/com/baltajmn/bullet/StorageFileTest.kt` | test 23 (A `line/.../StorageTest.kt`) |
| `shared/src/androidHostTest/kotlin/com/baltajmn/bullet/PerfTest.kt` | test 26 |

`ModelTest`, `MigrationTest`, `DataTest` y `StorageTest` están en `commonTest`: corren en JVM y en el
Simulador de iOS (9). Solo va a `androidHostTest` lo que necesita disco real o reflexión de la JVM.

### Android e iOS

| Ruta | Qué es | Origen |
|---|---|---|
| `androidApp/src/main/AndroidManifest.xml` | 8.1 | A line |
| `androidApp/src/main/kotlin/com/baltajmn/bullet/MainActivity.kt` | `FragmentActivity`; lanzadores de ficheros y de permiso; `Lock.host` y `StoreReview.host`; el `data` del intent a `Route` | A line |
| `androidApp/src/main/res/xml/` | `data_extraction_rules.xml`, `backup_rules.xml`, `locales_config.xml`, `file_paths.xml`, `today_widget_info.xml`, `month_widget_info.xml` (8.2) | A line |
| `androidApp/src/main/res/layout/today_widget_preview.xml`, `month_widget_preview.xml` | previsualización del selector de widgets, mantenida a mano | A line |
| `androidApp/src/main/res/values*/strings.xml` | `app_name` = `Bobbin` y las descripciones de los widgets, en los cinco idiomas | A line |
| `androidApp/src/main/res/values*/themes.xml`, `colors.xml` | `Theme.Bobbin` y los colores de la previsualización (8.2) | A line |
| `androidApp/src/main/res/mipmap-*`, `drawable/` | icono, generado (#18) | N |
| `iosApp/Configuration/Config.xcconfig` | 8.3 | A line |
| `iosApp/iosApp/iOSApp.swift` | la vista que tapa la multitarea, `onOpenURL`, `BobbinBridge` | A line |
| `iosApp/iosApp/ContentView.swift` | el `UIViewControllerRepresentable` de Compose | C line |
| `iosApp/iosApp/Info.plist`, `iosApp.entitlements`, `PrivacyInfo.xcprivacy` | 8.3 | A line |
| `iosApp/iosApp/<lang>.lproj/InfoPlist.strings` | `docs/textos.md` | A line |
| `iosApp/BobbinWidget/BobbinWidget.swift` | widget de hoy y widget de pantalla de bloqueo, con el mismo `TimelineProvider` | A `line/iosApp/LineWidget/LineWidget.swift` |
| `iosApp/BobbinWidget/BobbinMonthWidget.swift` | widget del mes | A `line/iosApp/LineWidget/LineYearWidget.swift` |
| `iosApp/BobbinWidget/BobbinStore.swift` | decodifica `widget.json` y aplica `widgetView`; nada más | A `line/iosApp/LineWidget/LineStore.swift` |
| `iosApp/BobbinWidget/Assets.xcassets` | `WidgetBackground`, el crema y el oscuro | C line |
| `iosApp/BobbinWidget/<lang>.lproj/Localizable.strings` | nombre y descripción de cada widget en el selector | A line |
| `iosApp/BobbinWidget/Info.plist`, `BobbinWidget.entitlements` | 8.3 | A line |
| `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme` | esquema compartido: sin él el CI no archiva | C line |
| `tools/play-listing/subir.py` | `PACKAGE_NAME = "com.baltajmn.bullet"` | C line |
| `tools/store/capturas.py`, `tools/store/cabecera.py` | capturas y cabecera de Play (#58) | A line |
| `tools/demo/generar.py` | el diario de demostración de las capturas, datos inventados | A line |
| `tools/perf/generar.py` | `journal.json` de 5.000 entradas en 36 meses (#55) | N |
| `tools/generate_icons.py`, `tools/icon-master.svg` | icono (#18) | A line; el SVG, N |
| `tools/check-bobbinstore.swift` | comprueba `BobbinStore.swift` contra `WIDGET_SAMPLE` (test 36) | A `line/tools/check-linestore.swift` |

`iosApp/iosApp` es un grupo sincronizado de Xcode: un fichero nuevo entra en el target sin tocar el
`.pbxproj`. La versión y el identificador viven en `Config.xcconfig`, nunca en el `.pbxproj`.

---

## 4. Formatos de datos

### 4.1 `journal.json` y `schemaVersion`

Vive en `filesDir` (Android) y en `Application Support/` (iOS). **Nunca en el App Group**: allí solo va
`widget.json` (4.2), que no lleva texto. Es la frontera entre el diario y lo que un widget puede leer.
Se escribe tras cada cambio, de forma atómica (`.tmp`, `fsync` y rename), con `.bak` de la versión
anterior, un solo escritor y fuera del hilo principal (6.14).

```kotlin
const val SCHEMA_VERSION = 1              // = 1 + SCHEMA_STEPS.size (6.14)
const val TEXT_LIMIT = 500

@Serializable enum class Bullet { @SerialName("task") TASK, @SerialName("event") EVENT, @SerialName("note") NOTE }

@Serializable enum class TaskStatus {
    @SerialName("open") OPEN, @SerialName("done") DONE, @SerialName("migrated") MIGRATED,
    @SerialName("scheduled") SCHEDULED, @SerialName("irrelevant") IRRELEVANT,
}

@Serializable enum class Signifier {
    @SerialName("priority") PRIORITY, @SerialName("inspiration") INSPIRATION, @SerialName("explore") EXPLORE,
}

@Serializable(with = PlaceSerializer::class)
sealed interface Place {
    data class Daily(val date: LocalDate) : Place
    data class Monthly(val month: YearMonth, val day: Int? = null) : Place   // day: línea del calendario
    data class Future(val month: YearMonth, val day: Int? = null) : Place
    data class InCollection(val id: String) : Place
}

@Serializable
data class Entry(
    val id: String,                              // "e-" + 8 hex
    val bullet: Bullet = Bullet.TASK,
    val text: String,
    val status: TaskStatus = TaskStatus.OPEN,    // solo cambia si bullet == TASK
    val signifiers: Set<Signifier> = emptySet(),
    val place: Place,
    val order: Int = 0,
    val createdAt: Long,                         // epoch ms
    val updatedAt: Long,                         // epoch ms, decide la fusión (6.9)
    val from: String? = null,                    // id del original de una migración (6.4)
    val gone: Boolean = false,                   // esqueleto de un original borrado (6.4)
)

@Serializable enum class CollectionKind { @SerialName("notes") NOTES, @SerialName("tracker") TRACKER }

@Serializable
data class TrackerRow(val id: String, val title: String, val days: Set<Int> = emptySet())

@Serializable
data class BulletCollection(
    val id: String,                              // "c-" + 8 hex
    val title: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val archived: Boolean = false,
    val kind: CollectionKind = CollectionKind.NOTES,
    val threadFrom: String? = null,              // colección anterior del hilo (6.7, 6.18, 12.1)
    val month: YearMonth? = null,                // solo TRACKER: el mes de esta página (6.18)
    val rows: List<TrackerRow> = emptyList(),    // solo TRACKER
    val icon: String? = null,                    // v1.2, 12.9
    val theme: String? = null,                   // v1.2, 12.9
)

@Serializable
data class Settings(
    val dayStartHour: Int = DAY_START_DEFAULT,   // 0..6; fuera de rango se lee como 4
    val firstDayOfWeek: Int? = null,             // ISO 1..7; null = el del sistema
    val reminderOn: Boolean = false,
    val reminderHour: Int = REMINDER_DEFAULT_HOUR,
    val reminderMinute: Int = REMINDER_DEFAULT_MINUTE,
    val reminderOffered: Boolean = false,
    val lockOn: Boolean = false,
    val cover: String = "sage",
    val paper: String = "dotted",
    val futureSeen: YearMonth? = null,           // mes cuya revisión del Future Log se terminó (6.5)
    val questionsUsed: List<Int> = emptyList(),  // v1.1, 12.3
    val notebooks: List<LocalDate> = emptyList(),// v1.1, 12.2: el día en que empezó cada cuaderno nuevo
)

@Serializable
data class IndexMark(val icon: String? = null, val theme: String? = null)   // v1.2, 12.9

@Serializable
data class Journal(
    @EncodeDefault val schemaVersion: Int = SCHEMA_VERSION,
    @EncodeDefault val entries: List<Entry> = emptyList(),
    @EncodeDefault val collections: List<BulletCollection> = emptyList(),
    val settings: Settings = Settings(),
    val months: Map<String, IndexMark> = emptyMap(),                        // v1.2, 12.9; clave "yyyy-MM"
)

val JournalJson = Json { ignoreUnknownKeys = true; encodeDefaults = false; explicitNulls = false }
```

- `BulletCollection` y no `Collection`: una clase `Collection` en el paquete taparía a
  `kotlin.collections.Collection` en todo `model/`. En los documentos se sigue diciendo colección.
- `schemaVersion`, `entries` y `collections` se escriben siempre (`@EncodeDefault`, que exige
  `@OptIn(ExperimentalSerializationApi::class)`); lo demás solo si no vale su defecto. `from: null`,
  `gone: false`, `signifiers: []` y los ajustes por defecto no aparecen en el fichero.
- `Place` se escribe como objeto con una sola clave de lugar y, en `monthly` y `future`, `day`
  opcional. `day` presente es la línea del calendario; ausente, la lista de tareas del mes:

  ```json
  { "daily": "2026-09-22" }
  { "monthly": "2026-10" }            { "monthly": "2026-10", "day": 3 }
  { "future": "2027-02" }             { "future": "2027-02", "day": 14 }
  { "collection": "c-1d2e7a40" }
  ```

  `PlaceSerializer` lee un `JsonObject` y lanza `SerializationException` si no hay exactamente una de
  `daily`, `monthly`, `future` o `collection`, si hay `day` fuera de `monthly` o `future`, o si `day` no
  cabe en el mes (`1..monthDays(month)`). Las fechas son ISO **locales** (`LocalDate.toString()`,
  `YearMonth.toString()`), nunca UTC: cambiar de huso no reescribe nada. `createdAt` y `updatedAt` son
  instantes porque ordenan y deciden la fusión.
- `firstDayOfWeek` es un `Int` ISO (1 lunes, 7 domingo) y `null` significa "el del sistema".
- Una lista de entradas con `id`, no un mapa por fecha: una entrada vive en un día, en un mes, en el
  Future Log o en una colección, y la fusión va por `id`. El orden de la lista es el de creación; la
  posición en pantalla la da `order` dentro de su `place` (6.3).
- `status` de un `EVENT` o una `NOTE` es siempre `OPEN`. Si un fichero trae otro, se normaliza a
  `OPEN` al leer (`Entry.normalized()`), no se rechaza.
- `text` es de una línea y sin tope al leer: el tope de 500 se aplica al capturar y al editar (6.2),
  nunca al leer ni al importar, porque si viviera en la carga importar una copia truncaría el diario en
  silencio.
- Los ajustes van en su propio objeto porque la importación los ignora salvo con el diario vacío
  (6.9). La portada y el papel también viven aquí: un solo fichero es la fuente de verdad (SPEC §4).
- **Lo que no está en `journal.json`**: el derecho Pro, "valoración ya pedida" y "permiso de avisos ya
  pedido". Viven en `Prefs` (sección 7): sobreviven a borrar los datos y a importar, y una copia no
  regala Pro.

Ejemplo completo con los nombres exactos:

```json
{
  "schemaVersion": 1,
  "entries": [
    { "id": "e-3f9a1c2e", "text": "Llamar al fontanero", "status": "migrated",
      "signifiers": ["priority"], "place": { "daily": "2026-09-22" },
      "createdAt": 1790064000000, "updatedAt": 1790150400000 },
    { "id": "e-8b21d0f4", "text": "Llamar al fontanero", "signifiers": ["priority"],
      "place": { "monthly": "2026-10" }, "order": 3,
      "createdAt": 1790150400000, "updatedAt": 1790150400000, "from": "e-3f9a1c2e" },
    { "id": "e-51c7aa09", "bullet": "event", "text": "Cumpleaños de Ana",
      "place": { "future": "2027-02", "day": 14 },
      "createdAt": 1790150400000, "updatedAt": 1790150400000 }
  ],
  "collections": [
    { "id": "c-1d2e7a40", "title": "Lecturas 2026", "createdAt": 1790064000000 }
  ],
  "settings": { "reminderOn": true }
}
```

`schemaVersion` sube solo cuando un campo cambia de significado o de forma; un campo nuevo con
defecto no lo sube (`ignoreUnknownKeys` y los defectos lo cubren). Por eso `threadFrom`, `icon`,
`theme`, `months`, `questionsUsed` y `notebooks` existen desde el esquema 1. Cada subida trae su paso de
conversión (6.14).

### 4.2 `widget.json`

En el App Group (iOS) y en `filesDir` (Android), fuera de donde vive `journal.json`. **Solo números,
fechas, booleanos y un identificador fijo (`cover`), nunca texto del diario**: el diario no está
en el contenedor del widget, así que un widget no puede leerlo aunque quiera. Se reescribe de forma
atómica tras cada guardado bueno del diario (6.13). Los widgets **no escriben nunca**.

```kotlin
@Serializable
data class WidgetState(
    val date: String,            // fecha lógica de hoy, ISO local
    val open: Int,               // tareas OPEN de hoy (6.5, ofDay)
    val done: Int,               // tareas DONE de hoy
    val events: Int,             // eventos de hoy
    val month: String,           // "yyyy-MM" de date
    val monthMask: String,       // un carácter por día del mes: '1' si ese día tiene entradas
    val reviewPending: Boolean,  // alguna de las tres líneas de revisión está puesta (6.6)
    val isPro: Boolean,
    val cover: String,           // id de portada activa (6.17)
    val dayStartHour: Int,       // para que el widget sepa cuándo cambia el día (6.13)
)

val WidgetJson = Json { encodeDefaults = true; explicitNulls = false }
```

```swift
struct BobbinState: Decodable {
    let date: String; let open: Int; let done: Int; let events: Int
    let month: String; let monthMask: String; let reviewPending: Bool; let isPro: Bool
    let cover: String; let dayStartHour: Int
}
```

`encodeDefaults = true` porque Swift decodifica todos los campos. Ejemplo (el de `WIDGET_SAMPLE`):

```json
{"date":"2026-09-23","open":3,"done":2,"events":1,"month":"2026-09",
 "monthMask":"110110011101111011101000000000","reviewPending":true,"isPro":false,
 "cover":"sage","dayStartHour":4}
```

**El App Group es un contrato frágil**, y es la trampa que Quilt y MoodTraker ya pagaron: allí el
widget de Swift reescribe el JSON entero desde su `struct` y borra en el siguiente toque cualquier
campo que no declara. Aquí el diario ni está en el App Group ni los widgets escriben, pero la regla
sigue: **un campo nuevo se añade en `WidgetState` y en `BobbinStore.swift` en el mismo commit**, y
`WIDGET_SAMPLE` cambia con ellos (tests 12 y 36).

### 4.3 La copia: `bobbin-AAAA-MM-DD.zip`

Zip STORED (método 0), bit 11 de propósito general puesto (nombres UTF-8), sin cifrado, sin zip64, sin
descriptor de datos: tamaño y CRC van en la cabecera local de cada entrada, así que el lector lo
recorre de principio a fin sin leer el directorio central. El directorio central y el registro de fin
(EOCD) se escriben completos, para que cualquier descompresor lo abra. Hora DOS de cada entrada: la del
momento de exportar. Orden fijo:

1. `journal.json`: el `Journal` entero, ajustes y esqueletos incluidos, codificado con `JournalJson`.
   Es lo que reimporta la app.
2. `months/AAAA-MM.md`, uno por mes con alguna entrada en `Daily`, `Monthly` o `Future` de ese mes, en
   orden ascendente (4.4).
3. `collections/<nombre>.md`, uno por colección, en orden de creación (4.4).

Los nombres de dentro son fijos y en inglés en los cinco idiomas. `AAAA-MM-DD` es la fecha lógica de
hoy. Sin fotos: el método es texto (SPEC §3).

### 4.4 Exportación Markdown

Un fichero por mes y uno por colección, con los símbolos del método en ASCII para que se lean igual en
cualquier editor dentro de 50 años. Sin traducir: los encabezados fijos van en inglés, como los nombres
de fichero.

| Qué | Símbolo |
|---|---|
| Tarea abierta | `.` |
| Tarea hecha | `x` |
| Tarea migrada | `>` |
| Tarea programada | `<` |
| Tarea irrelevante | `.` y la línea entre `~~` |
| Evento | `o` |
| Nota | `-` |
| Prioridad, inspiración, explorar | `*`, `!`, `?`, delante del bullet |

Cada entrada es un elemento de lista: `- ` y después los signifiers (en el orden prioridad,
inspiración, explorar), el símbolo del bullet o del estado y el texto, separados por un espacio. Ir
dentro de un elemento de lista hace que `-`, `*` y `.` se vean tal cual también renderizados. Los
signifiers usan los mismos caracteres que los prefijos de captura (6.2), así que una línea copiada del
Markdown a Hoy vuelve a ser la misma entrada.

```
# 2026-09

## Calendar

### 14
- o Cumpleaños de Ana
- . Pagar el alquiler

## Tasks
- * . Renovar el pasaporte
- x Llamar al banco

## 2026-09-22
- * > Llamar al fontanero
- - La reunión pasa al jueves
- ~~. Comprar tinta~~
- ! o Concierto en el parque

## Future log
- < Revisar el seguro
```

- `## Calendar`: los `Monthly(mes, day)`, un `### día` por cada día con entradas.
- `## Tasks`: los `Monthly(mes, null)`.
- `## AAAA-MM-DD`: un bloque por cada `Daily` del mes con entradas, en orden ascendente.
- `## Future log`: los `Future(mes, *)`, con `(día)` delante del símbolo si tienen día.
- Dentro de cada bloque, por `order`. Una sección vacía no se escribe. Los esqueletos (`gone`) no se
  escriben nunca.

Colección de notas: `# <título>` y sus entradas. Seguimiento: `# <título> AAAA-MM` y una tabla con una
fila por `TrackerRow`, una columna por día y `x` en los marcados.

Nombre del fichero de una colección: el título con `/ \ : * ? " < > |` y los caracteres de control
cambiados por `-`, espacios de los extremos quitados, recortado a 60 puntos de código con
`clampCodePoints`; vacío, el `id`. Un seguimiento añade ` AAAA-MM`. Si dos nombres coinciden, el segundo
lleva ` 2`, el tercero ` 3`. Extensión `.md`.

### 4.5 Importar de las apps hermanas

v1.2 (#72, 12.10). Se reconoce el fichero por su forma, sin clases propias: se lee como `JsonObject`
con `JournalJson`, y solo se toma lo que dice la tabla. Lo que no encaja se salta (y se cuenta), no
tumba el resto.

| App | Cómo se reconoce | Qué se toma | En qué se convierte |
|---|---|---|---|
| Purl | zip con `entries.json` y sin `journal.json`, o JSON con `version` entero y `entries` objeto | cada `entries[fecha]` con `text` no vacío; `tags` si existen | `EVENT` con `oneLine(text)` y, si hay etiquetas, ` #etiqueta` por cada una al final; fecha = la clave |
| MoodTraker | JSON con `app == "mood"` y `store.entries` lista | cada entrada con `note` no vacía; `tags` | `EVENT` con `oneLine(note)` y las etiquetas igual; fecha = `date` |
| Quilt | JSON con `habits` lista | por cada hábito, cada fecha de `log` con valor mayor o igual que `target` (1 si falta) | `TASK` en `DONE` con `name` (y el `emoji` delante si lo hay); fecha = la clave |

Las fotos de Purl y MoodTraker, el ánimo, los días omitidos de Quilt y cualquier otro campo se
ignoran. Ningún texto se recorta. La fecha tiene que ser ISO válida; si no, esa línea se salta.

### 4.6 Validación de una importación

Se rechaza, con el diario intacto y la clave de texto indicada, si:

| Caso | Clave |
|---|---|
| No empieza por `50 4B 03 04` (zip) ni por `{` tras espacios (JSON) | `importNotBackup` |
| Zip truncado, CRC distinto, método distinto de 0 o entrada cifrada | `importDamaged` |
| Zip sin `journal.json` | `importNotBackup` |
| JSON sin `schemaVersion` entero o sin lista `entries` | `importNotBackup` |
| `schemaVersion` mayor que `SCHEMA_VERSION` | `importTooNew` |
| Una entrada o colección que no decodifica (`id` vacío, `bullet` o `status` desconocido, `place` inválido) | `importDamaged` |
| Dos entradas o dos colecciones con el mismo `id` | `importDamaged` |
| Un `InCollection` que apunta a una colección que no está en la copia, en una entrada que no es esqueleto | `importDamaged` |
| Copia sin entradas ni colecciones | `importEmpty` |
| Una copia de Purl, MoodTraker o Quilt por el botón de importar normal (4.5) | `importIsSibling` en v1.0 y v1.1; en v1.2 se desvía a su importación |

Un `schemaVersion` menor se convierte en memoria con los pasos de 6.14 antes de decodificar, sin
tocar el disco. Un `from` que apunta a un `id` que no está se acepta: el original pudo borrarse. Un
texto de más de 500 se acepta tal cual.

---

## 5. Constantes

| Nombre | Valor | Fichero |
|---|---|---|
| `DAY_START_DEFAULT` | `4` | `model/DayClock.kt` |
| `DAY_START_RANGE` | `0..6` | `model/DayClock.kt` |
| `TEXT_LIMIT` | `500` puntos de código | `model/Entry.kt` |
| `COUNTER_FROM` | `450` | `model/Entry.kt` |
| `COLLECTION_TITLE_MAX` | `60` | `model/Collections.kt` |
| `SCHEMA_VERSION` | `1` | `model/Journal.kt` |
| `MIGRATION_SHOWN_FROM` | `2` | `model/Migration.kt` |
| `FUTURE_MONTHS` / `FUTURE_MONTHS_MAX` | `6` / `24` | `model/Migration.kt` |
| `FREE_TRACKER_LIMIT` | `1` | `model/Collections.kt` |
| `UNDO_MS` | `5000` | `data/BobbinRepository.kt` |
| `RELOCK_AFTER` | `60.seconds` | `App.kt` |
| `REMINDER_DEFAULT_HOUR` / `_MINUTE` | `21` / `0` | `model/Journal.kt` |
| `REMINDER_CHANNEL` | `"bobbin-reflection"` | `data/Reminders.android.kt` |
| `REMINDER_ID` | `"bobbin-reflection"` | `data/Reminders.ios.kt` |
| `APP_GROUP` | `"group.com.baltajmn.bullet"` | `data/Widgets.ios.kt`, `BobbinStore.swift` |
| `URL_SCHEME` | `"bobbin"` | `data/Route.kt` |
| `PRIVACY_URL` | `"https://bullet.baltajmn.dev/"` | `data/AppInfo.kt` |
| `EXPORT_PREFIX` | `"bobbin"` | `data/Export.kt` |
| `JOURNAL_NAME` | `"journal.json"` | `data/Export.kt`, `data/Storage.*.kt` |
| `SHARE_W` / `SHARE_H` | `1080` / `1350` px | `ui/SharePage.kt` |
| `GRID_UNIT` | `24.dp`, por `max(1, fontScale)` | `ui/theme/Grid.kt` |
| `WIDE_SCREEN_FROM` | `600.dp` | `ui/theme/Theme.kt` |
| `MAX_CONTENT_WIDTH` | `640.dp` (SPEC §5; si `docs/pantallas.md` 1 fija otro, manda ese y se corrige aquí) | `ui/theme/Theme.kt` |
| `PARSE_BUDGET_MS` / `SEARCH_BUDGET_MS` | `1000` / `200` (test 26) | `PerfTest.kt` |
| `QUESTION_COUNT` | `60` (v1.1) | `i18n/Strings.kt` |
| `INDEX_ICONS` | los 12 de 12.9 (v1.2) | `model/Collections.kt` |
| `updatePeriodMillis` de los dos widgets | `0`: sin sondeo (6.13) | `res/xml/*_widget_info.xml` |

Portadas, en este orden (el de la paleta de la familia), con `sage` gratis y por defecto:

| id | hex | id | hex |
|---|---|---|---|
| `rose` | `F0AFBE` | `mint` | `9CD3C7` |
| `peach` | `F5C39B` | `sky` | `A2C3E9` |
| `butter` | `EDDC98` | `periwinkle` | `B4B8EC` |
| `sage` | `B6D6AB` | `lilac` | `D9AFE6` |

Papeles: `dotted` (gratis y por defecto), `lined`, `grid`, `blank`. Un id de portada o de papel
desconocido se lee como `sage` o `dotted`.

---

## 6. Algoritmos

Todas las funciones de `model/` y de los ficheros puros de `data/` reciben la fecha, el instante y el
generador de ids como parámetro. Nada de `Clock.System` dentro: se prueban con fechas fijas, como en
la familia. `BobbinRepository` es quien lee el reloj y llama.

### 6.1 Fecha lógica y `DayClock`

```kotlin
fun logicalDate(local: LocalDateTime, dayStartHour: Int): LocalDate =
    if (local.hour < dayStartHour) local.date.minus(1, DateTimeUnit.DAY) else local.date

fun logicalDate(now: Instant, tz: TimeZone, dayStartHour: Int): LocalDate =
    logicalDate(now.toLocalDateTime(tz), dayStartHour)

// El primer instante en que logicalDate deja de ser `date`: el día siguiente a dayStartHour:00.
fun nextDayStart(date: LocalDate, dayStartHour: Int, tz: TimeZone): Instant

fun monthOf(d: LocalDate): YearMonth
fun monthDays(m: YearMonth): Int                        // 28, 29, 30 o 31
fun firstDayOfWeek(s: Settings, system: DayOfWeek): DayOfWeek   // s.firstDayOfWeek ?: system
fun weekStarts(m: YearMonth, first: DayOfWeek): Set<Int>        // días del mes que empiezan semana
```

- El día lógico empieza a las **04:00 locales** por defecto (`dayStartHour`, ajuste de 0 a 6): quien
  apunta a la una de la madrugada sigue en el día que está viviendo. A las 02:30 se está en el día
  anterior.
- Se compara la hora local, no se restan horas a un instante: el cambio de hora no mueve el corte.
- **Cambiar `dayStartHour` no mueve ninguna entrada**: las entradas llevan su `place` con fecha
  explícita. Solo cambia qué día es hoy desde ese momento.
- "Hoy" en toda la app es `logicalDate(Clock.System.now(), TimeZone.currentSystemDefault(),
  settings.dayStartHour)`, en `BobbinRepository.today()`. `App.kt` lo recalcula en cada `ON_RESUME`
  (`LifecycleEventEffect`), así que volver a primer plano después de las 04:00 enseña el día nuevo sin
  reiniciar.
- `systemFirstDayOfWeek()` es de plataforma (sección 7). `weekStarts` sirve al Mes para poner el
  separador de semana (`docs/pantallas.md`).

### 6.2 Captura rápida y prefijos

```kotlin
data class Parsed(val bullet: Bullet, val signifiers: Set<Signifier>, val text: String)

fun rapidParse(input: String, picked: Bullet? = null): Parsed?
```

1. `s = oneLine(input).trimStart()`. `oneLine` cambia `\r\n`, `\n` y `\r` por un espacio.
2. Mientras `s` empiece por un prefijo aún no usado, se consume: `"- "` da `NOTE`, `"o "` da `EVENT`
   (un solo prefijo de bullet), `"* "` da `PRIORITY`, `"! "` da `INSPIRATION`, `"? "` da `EXPLORE`
   (cada signifier una vez), en cualquier orden. `"* - texto"` y `"- * texto"` son la misma nota con
   prioridad.
3. La consumición para en el primer carácter que no es un prefijo sin usar: un guion en mitad del
   texto no cambia nada, `"-5 grados"` es una tarea, y `"- - x"` es una nota con texto `"- x"`.
4. `text = s.trim()`. Si queda vacío, `null`: no se crea entrada.
5. Sin prefijo de bullet, `picked` (el selector opcional de punto, círculo y guion junto al campo) o
   `TASK`. Un prefijo escrito gana al selector.

Solo `o` minúscula es evento. En portugués "o livro" empieza igual: se acepta, porque el prefijo lo
fija el método (SPEC §2.2) y la entrada se corrige con la hoja o editándola.

Crear: `Entry(id = newId("e", taken), bullet, text = clampCodePoints(text, TEXT_LIMIT), status = OPEN,
signifiers, place, order = siguiente de ese place, createdAt = now, updatedAt = now)`. El lugar es
`Daily(hoy o el día visible)` en Hoy, `Monthly(mes, día)` en la línea de un día del Mes,
`Monthly(mes, null)` en las tareas del mes, `Future(mes, día?)` en Futuro e `InCollection(id)` en una
colección.

**Tope de 500 y parejas suplentes.** Se cuenta en puntos de código, no en unidades UTF-16, con las
funciones de `line/.../model/Text.kt`, copiadas a `model/Entry.kt`:

```kotlin
fun String.codePointCount(): Int                 // una pareja suplente cuenta uno
fun String.clampCodePoints(n: Int): String       // nunca parte una pareja ni deja un VS16, ZWJ o tono de piel colgando
data class Edit(val text: String, val cursor: Int)
fun limitEdit(old: String, new: String, cursor: Int, limit: Int = TEXT_LIMIT): Edit
```

Media pareja no es texto válido y el lado Swift la rechaza al decodificar: es la lección de
`habitIcon()` en Quilt (`e8ef697`). `limitEdit` recorta solo lo que se acaba de insertar, nunca lo que
ya había (el algoritmo y su porqué, en `line/docs/tecnico.md` 6.4). Un texto de más de 500 que llegó
por una importación se puede acortar pero no alargar. El campo enseña el contador desde
`COUNTER_FROM`.

El campo de captura vive con el teclado abierto: todo lo que convive con el teclado usa
**`imePadding()` junto con `verticalScroll`** (o la lista perezosa que scrollea). `imePadding()` solo
encoge la pantalla y corta lo de abajo; es la trampa que más probabilidades tiene de repetirse aquí,
porque Hoy abre con el teclado arriba.

### 6.3 Estados y signifiers

Solo las tareas tienen estado.

| Acción | Efecto | Permitido desde |
|---|---|---|
| `toggleDone(id)` | `OPEN` a `DONE` y `DONE` a `OPEN` | tarea `OPEN` o `DONE`; en otro caso no hace nada |
| `reopen(id)` | a `OPEN` | tarea `DONE` o `IRRELEVANT` |
| `migrate`, `schedule`, `discard` | 6.4 | tarea `OPEN` |
| `toggleSignifier(id, s)` | pone o quita `s` | cualquier entrada |
| `editText(id, text)` | `oneLine`, `limitEdit` con el texto anterior; vacío tras `trim` no guarda | cualquier entrada no esqueleto |
| `reorder(place, ids)` | reescribe `order` de 0 a n-1 en ese lugar | cualquier lugar |

Toda acción que cambia una entrada pone `updatedAt = now`. `reorder` solo toca las que cambian de
`order`. Una tarea `MIGRATED` o `SCHEDULED` no se reabre: su copia ya existe.

La hoja de la pulsación larga ofrece a una tarea `OPEN` migrar, programar, descartar, los tres
signifiers, editar y borrar; a un evento o una nota, solo los signifiers, editar y borrar. Nunca
ofrece estados de tarea a un evento o una nota.

Orden en pantalla dentro de un lugar: `order`, después `createdAt`, después `id`.

Glifos (`ui/BulletGlyph.kt`, dibujados en `Canvas` sobre una caja de 24 dp, nunca con emoji ni fuente
de iconos; geometría en `docs/pantallas.md`):

| Estado | Glifo | Texto |
|---|---|---|
| Tarea `OPEN` | punto | normal |
| Tarea `DONE` | aspa sobre el punto | `onSurfaceVariant` |
| Tarea `MIGRATED` | `>` | `onSurfaceVariant`, con enlace a la copia |
| Tarea `SCHEDULED` | `<` | `onSurfaceVariant`, con enlace a la copia |
| Tarea `IRRELEVANT` | punto | tachado entero |
| Evento | círculo | normal |
| Nota | guion | normal |
| Signifiers | asterisco, exclamación, ojo, en la columna de margen | |

La copia de una migrada o programada es `copyOf(j, id)`: la entrada no esqueleto cuyo `from` es `id`.

Lectura para TalkBack y VoiceOver: `S.entryDescription(bullet, status, signifiers, text)`, "Tarea
hecha, prioridad: comprar pan", en los cinco idiomas (#53). Completar, migrar y programar son además
acciones personalizadas del lector (`CustomAccessibilityAction`), sin depender de la pulsación larga.

### 6.4 Migración, programación y cadena

`model/Migration.kt`. **Cada función mueve una sola entrada.** No existe `migrateAll`, ni ninguna
función pública de este fichero que reciba una lista, un conjunto o un array de entradas o de ids
(test 9). Ninguna toca el texto del original: solo su estado, su `updatedAt` y, si toca, crean una copia
enlazada por `from`. Nada se mueve solo: ni ayer a hoy, ni un mes al siguiente, ni el Future Log al
mes que llega.

```kotlin
fun Journal.migrate(id: String, to: Place, now: Long, newId: String): Journal?
fun Journal.schedule(id: String, month: YearMonth, day: Int?, now: Long, newId: String): Journal?
fun Journal.discard(id: String, now: Long): Journal?
fun Journal.migrationCount(id: String): Int
```

Devuelven `null` si la acción se rechaza, y entonces no cambia nada.

- **`migrate`** con una tarea `OPEN`: el original pasa a `MIGRATED`; se añade una copia con `newId`,
  el mismo `bullet`, `text` y `signifiers`, `status = OPEN`, `place = to`, `order` al final de `to`,
  `createdAt = updatedAt = now` y `from = id`. Se rechaza si `to` es el mismo lugar, un día anterior a
  hoy, un mes anterior al actual o una colección archivada.
- **`migrate` con un evento o una nota** (solo lo usa la revisión del Future Log, 6.5): se **mueve**
  la misma entrada, sin copia ni cadena: cambia `place`, `order` y `updatedAt`. Un evento no tiene
  estado "migrado" que dejar atrás.
- **`schedule`**: como `migrate` hacia `Future(month, day)`, con el original en `SCHEDULED`. Se rechaza
  si `month` no es posterior al mes actual o está a más de `FUTURE_MONTHS_MAX`, o si `day` no está en
  `1..monthDays(month)` (el 31 en un mes de 30 no crea nada).
- **`discard`**: el original pasa a `IRRELEVANT`. Sin copia.
- **`migrationCount(id)`**: recorre `from` desde la entrada hacia atrás y cuenta los saltos. Si un
  `from` apunta a un `id` que no está, cuenta ese salto y para. Se protege de ciclos (un fichero editado
  a mano) parando al repetir un `id`. Tres migraciones seguidas de la misma tarea dan 3 en la copia
  abierta. La revisión enseña "migrada N veces" desde `MIGRATION_SHOWN_FROM` (2).

**Borrar el original de una migración no rompe la cadena.** Borrar una entrada a la que apunta el
`from` de otra no la quita de la lista: la deja como **esqueleto**, con `gone = true`, `text = ""`,
`signifiers` vacíos y el resto igual. Un esqueleto no se pinta, no se busca, no cuenta en ningún número,
no sale en el Markdown ni en compartir, y sí cuenta como eslabón en `migrationCount`. Las palabras
borradas no quedan en el fichero. Cuando un cambio deja un esqueleto sin nadie que apunte a él (se
borró también la copia), se elimina en ese mismo cambio. Borrar una entrada a la que no apunta nadie
la quita de la lista. Así la copia conserva su recuento y el enlace de la migrada sigue llevando a
ella.

Borrar no pide confirmación (#23): `BobbinRepository.delete(id)` guarda en memoria el `Journal` de
antes y enseña Deshacer durante `UNDO_MS`. Deshacer restaura ese `Journal` si no ha habido otro cambio
entre medias, o reinserta la entrada con su `order` y su estado si lo ha habido. Pasado el plazo, el
borrado queda firme. El guardado a disco no espera al plazo.

Consultas que alimentan la revisión (6.6). "Días del mes" son los lugares `Daily(d)` y
`Monthly(mes, día)`, cuya fecha es `d` o `LocalDate(mes, día)`. Ninguna cuenta esqueletos.

```kotlin
fun Journal.ofDay(d: LocalDate): List<Entry>                 // Daily(d) y después Monthly(monthOf(d), d.day)
fun Journal.openTasksOfDay(d: LocalDate): List<Entry>        // tareas OPEN de ofDay(d)
fun Journal.openTasksBefore(d: LocalDate): List<Entry>       // tareas OPEN en días de monthOf(d) anteriores a d
fun Journal.openTasksOfMonth(m: YearMonth): List<Entry>      // tareas OPEN en Daily de m y en Monthly(m, *)
fun Journal.unclosedMonth(today: LocalDate): YearMonth?      // el mes pasado más antiguo con openTasksOfMonth no vacío
fun Journal.futureWaiting(today: LocalDate): List<Entry>     // 6.5
```

Las tareas de una colección no entran en ninguna: una colección no se cierra con el mes.

### 6.5 Monthly Log y Future Log

**Lo que es de un día** es `ofDay(d)`: el Daily Log de `d` y la línea del calendario de ese día en su
Monthly Log. Hoy los lista a los dos (primero el Daily Log; la línea del calendario, debajo, con su
etiqueta, `docs/pantallas.md`), y lo mismo usan el widget (6.13) y la revisión de un día (6.6).

**Mes** (`ui/MonthScreen.kt`) para el mes `m`:

- Una línea por día de `1` a `monthDays(m)`, con su número y la inicial del día de la semana
  (`S.weekdayInitial`), y debajo de cada una los `Monthly(m, día)` por `order`. Un separador antes de
  cada día de `weekStarts(m, firstDayOfWeek(...))`.
- Después, las tareas del mes sin día: `Monthly(m, null)`. Una entrada con día nunca aparece aquí.
- Tocar la línea de un día abre el mismo campo de Hoy escribiendo en `Monthly(m, día)`.
- El mes nace vacío: no se copia ni se sugiere nada del anterior.

**Futuro** (`ui/FutureScreen.kt`): `futureMonths(today, n)` devuelve `n` meses empezando por el
siguiente al actual, `FUTURE_MONTHS` (6) de entrada y hasta `FUTURE_MONTHS_MAX` (24) con "ver más". Cada
bloque lista sus `Future(mes, día)` por día ascendente y después los `Future(mes, null)` por `order`.
El día se escribe como número en un campo, nunca con selector de calendario, y un día fuera de rango se
rechaza sin crear nada. El Future Log no notifica ni mueve nada.

**La línea del Future Log** ("N entradas del Future Log esperan", #26):

```kotlin
// Entradas del Future Log de este mes o de uno anterior que siguen ahí: tareas OPEN, y eventos y
// notas siempre. Por mes y después por día y order.
fun Journal.futureWaiting(today: LocalDate): List<Entry>
```

Mes enseña la línea cuando `futureWaiting` no está vacía y `settings.futureSeen != monthOf(today)`.
Tocarla abre una revisión entrada por entrada con tres acciones:

- **Pasar al calendario del mes**: `migrate` hacia `Monthly(mesActual, día)` si la entrada es de este
  mes y tiene día; si no, hacia `Monthly(mesActual, null)`. Una tarea deja el original `MIGRATED` y la
  copia enlazada; un evento o una nota se mueve (6.4).
- **Dejarla**: no cambia nada y pasa a la siguiente. El contador baja dentro de esta sesión.
- **Descartarla**: una tarea, `discard`; un evento o una nota, se borra con el Deshacer de 6.4.

Llegar al final pone `settings.futureSeen = monthOf(today)` y la línea no vuelve este mes. Salir a
medias no lo pone: la siguiente vez vuelven las que quedaron. Cambiar de mes sin abrir el Mes no toca
ninguna entrada del Future Log: la línea es pasiva.

### 6.6 Revisión y reflexión

`ui/ReviewScreen.kt` recibe un alcance:

| Alcance | Tareas | Periodo que se relee | Desde |
|---|---|---|---|
| `Month(m)` | `openTasksOfMonth(m)` | todo lo de `m`: días, calendario, tareas del mes | la línea "Febrero sin cerrar: N abiertas" de Hoy y Mes, con `m = unclosedMonth(today)` |
| `Earlier(today)` | `openTasksBefore(today)` | los días de este mes anteriores a hoy | la línea de Hoy con `openTasksBefore(today)` no vacía |
| `Day(today)` | `openTasksBefore(today) + openTasksOfDay(today)` | los días de este mes hasta hoy | la notificación y `bobbin://review` |

Las tres líneas son líneas dentro de la pantalla, nunca modales ni notificaciones, y `reviewPending`
de `widget.json` es que al menos una está puesta. Qué texto lleva cada una, en `docs/textos.md`.

**Paso 1, reflexión** (#28). El periodo entero en modo lectura, sin ningún número, porcentaje ni
gráfica. Un campo de nota opcional; "Guardar" crea una `NOTE` con ese texto (6.2, con el tope) en
`Monthly(m, null)` si el alcance es un mes, o en `Monthly(monthOf(today), today.day)` si es un día.
"Saltar" pasa al paso 2 sin crear nada. En v1.1, una pregunta opcional junto al campo (12.3).

**Paso 2, una tarea cada vez** (#27). La cola se recalcula de la consulta del alcance cada vez que se
abre, en el orden de su lugar (fecha, y después `order`). Cinco acciones, cada una sobre una sola
tarea:

1. Marcar hecha (`toggleDone`).
2. Migrar al mes nuevo o a un día: `migrate` hacia `Monthly(mesActual, null)`, `Daily(hoy)`,
   `Daily(mañana)` o `Monthly(mesActual, día)` con el día escrito como número.
3. Programar: `schedule` a un mes de la lista de `futureMonths`.
4. Mover a una colección: `migrate` hacia `InCollection(id)` de una colección no archivada.
5. Descartar: `discard`.

Todas cambian el estado de la tarea, así que dejar la revisión a medias y volver solo enseña las que
quedan: no hace falta guardar un progreso aparte. Desde la segunda migración, "migrada N veces" con
`migrationCount`. No hay ningún botón ni ruta de código que decida más de una tarea.

Una **revisión mensual completa** es una de alcance `Month(m)` que termina con
`openTasksOfMonth(m)` vacía. La primera dispara la valoración (#56): si `shouldAskReview(prefs)`,
se pone `Prefs.reviewAsked = true` y después `StoreReview.request()`. Una sola vez en la vida de la
instalación, nunca al arrancar ni tras un error.

### 6.7 Índice, colecciones y threading

`model/Collections.kt`.

```kotlin
fun Journal.createCollection(title: String, now: Long, newId: String, kind: CollectionKind = NOTES): Journal?
fun Journal.renameCollection(id: String, title: String, now: Long): Journal?
fun Journal.archiveCollection(id: String, archived: Boolean, now: Long): Journal
fun Journal.deleteCollection(id: String): Journal
fun Journal.indexItems(): List<IndexItem>
fun filterIndex(items: List<IndexItem>, query: String): List<IndexItem>
```

- Crear pide solo el título: `oneLine`, `trim`, `clampCodePoints(COLLECTION_TITLE_MAX)`; vacío se
  rechaza. Sin plantillas ni campos adicionales. `NOTES` por defecto.
- Renombrar cambia `title` y `updatedAt`, **nunca** `createdAt`: el orden del Índice no se mueve.
- Archivar pone `archived` y la lleva al bloque plegado del Índice sin tocar ninguna entrada. Una
  colección archivada no es destino de migración.
- Borrar quita la colección y sus entradas, con la regla del esqueleto de 6.4 para las que son origen
  de una migración, y con el mismo Deshacer de `UNDO_MS`. Si otra colección tenía `threadFrom` a la
  borrada, pasa a apuntar al `threadFrom` de esta: el hilo se engancha por encima del hueco.
- Una colección es destino válido de `migrate` (`InCollection(id)`) igual que un mes.

`IndexItem` es un mes o una colección:

- **Meses con contenido**: los `m` con alguna entrada no esqueleto en `Daily` de `m` o en
  `Monthly(m, *)`. Los Daily Log no se indexan por separado y el Future Log no hace mes. Su momento de
  creación es el menor `createdAt` de esas entradas; su título, `S.monthTitle(m)` ("Septiembre 2026").
- **Colecciones**, con su `createdAt`. Un seguimiento sale una sola vez, por su página más reciente
  (6.18).
- Todo por momento de creación ascendente, nunca alfabético. Las archivadas, al final, bajo su propio
  encabezado plegado. Sin números de página.
- `filterIndex`: `fold(title).contains(fold(query.trim()))` (6.8), sin distinguir mayúsculas ni
  acentos.

Threading (v1.1, 12.1) añade la acción "Continuar en una colección nueva" y la agrupación por hilo.
El campo `threadFrom` existe desde el esquema 1.

### 6.8 Búsqueda

`data/Search.kt`, en memoria sobre el `Journal` ya cargado, sin red, sin índice y sin base de datos
(SPEC §10: FTS5 no viene en la SQLite del sistema de la mayoría de Android, y un segundo almacén
rompería "un solo fichero es la fuente de verdad").

```kotlin
fun fold(s: String): String                              // minúsculas y sin diacríticos
fun tags(text: String): Set<String>                      // palabras que empiezan por '#', plegadas, sin '#'

enum class SearchFilter { OPEN, PRIORITY, INSPIRATION, EXPLORE }

data class SearchGroup(val place: GroupKey, val entries: List<Entry>)
sealed interface GroupKey {
    data class Day(val date: LocalDate) : GroupKey
    data class Month(val m: YearMonth) : GroupKey
    data class FutureMonth(val m: YearMonth) : GroupKey
    data class InCollection(val id: String) : GroupKey
}

fun search(j: Journal, query: String, filters: Set<SearchFilter>): List<SearchGroup>
```

- `q = fold(query.trim())`. Si `q` empieza por `#`, busca la etiqueta: entradas con `q.drop(1)` en
  `tags(text)`. Si no, `fold(text).contains(q)`.
- Una etiqueta es `#` seguido de letras, dígitos, `_` o `-` (Unicode), hasta el primer carácter que no
  lo sea. Sin gestor de etiquetas ni pantalla aparte.
- Filtros de un toque, combinados con Y: `OPEN` es tarea con estado `OPEN` (excluye hechas, migradas,
  programadas y descartadas); los otros tres, el signifier.
- Consulta vacía con filtros: todo lo que cumple los filtros. Consulta vacía sin filtros: nada, y la
  pantalla invita a escribir.
- Grupos por lugar de origen: un día (`Daily`), un mes (`Monthly`), un mes del Future Log (`Future`) o
  una colección (con su título). Los de fecha, del más reciente al más antiguo; las colecciones,
  después, por `createdAt` descendente. Dentro, por `order`. Sin esqueletos.

Tabla de plegado, tras `lowercase()` (la de `line/.../model/Text.kt`):

| Letras | Queda |
|---|---|
| á à â ã ä å | a |
| é è ê ë | e |
| í ì î ï | i |
| ó ò ô õ ö | o |
| ú ù û ü | u |
| ý ÿ | y |
| ç | c |
| ñ | n |
| ß | ss |
| œ | oe |
| æ | ae |
| marcas combinantes U+0300 a U+036F (acentos descompuestos, NFD) | se quitan |

### 6.9 Fusión

`data/Merge.kt`, pura. Sirve a importar (#45) y, en v1.2, a la opción "fusionar" de la sincronización
(12.7). **Importar nunca borra.**

```kotlin
data class MergeResult(
    val journal: Journal,
    val added: Int,      // entradas y colecciones que solo tenía la copia
    val updated: Int,    // de los dos lados, que cambian
    val same: Int,       // de los dos lados, que no cambian
)

fun merge(device: Journal, incoming: Journal): MergeResult
```

1. Unión por `id`, por separado para entradas y para colecciones.
2. Un `id` solo en `incoming`: se añade tal cual. `added++`.
3. Un `id` en los dos: gana el `updatedAt` más reciente; empate, `device`. Si el ganador es una tarea
   `OPEN` y el otro lado tiene un estado cerrado (`DONE`, `MIGRATED`, `SCHEDULED`, `IRRELEVANT`), el
   resultado es el ganador con el estado cerrado: **un estado cerrado nunca vuelve a `OPEN`**. Si el
   resultado es igual a `device`, `same++`; si no, `updated++`.
4. Lo que solo tiene `device` no se toca.
5. `settings` y `months` de `incoming` solo se aplican si `device` no tiene entradas ni colecciones
   (una reinstalación que se restaura); si no, se quedan los del dispositivo. Una copia no trae nunca
   el derecho Pro: no está en el diario.
6. `schemaVersion` del resultado es `SCHEMA_VERSION`.

Importar, paso a paso (`BobbinRepository.import`):

1. `FilePicker.importFile` entrega un flujo de bytes.
2. Si empieza por `PK`, `ZipReader` lo recorre y guarda en memoria solo `journal.json`; si empieza
   por `{`, se lee como `journal.json` suelto.
3. Validación (4.6), con los pasos de esquema en memoria. Si falla, se enseña el error y no se toca
   nada.
4. `merge` en seco y **resumen antes de tocar nada**: N nuevas, M actualizadas. Nunca fusión
   silenciosa.
5. Confirmar: `flush()`, `Storage.keepCopy("pre-import", actual)`, aplicar el resultado y `flush()`.
   Cancelar: nada cambia, porque nada se había aplicado.

### 6.10 Exportar

Exportar es gratis siempre, y no es el libro en PDF (12.6), que es un producto.

1. `FilePicker.exportZip(suggestedName = exportName(hoy))` devuelve un destino (Android: el
   `OutputStream` del documento creado con SAF; iOS: un fichero temporal en `tmp/` que luego se
   entrega a `UIDocumentPickerViewController(forExporting:)`).
2. `flush()` y una instantánea del `Journal`.
3. `exportZip(journal, sink)` escribe con `ZipWriter`, entrada a entrada, en el orden de 4.3:
   `journal.json` (ajustes incluidos, así que la exportación lleva los ajustes actuales), los meses y
   las colecciones (4.4). Nunca se monta el archivo entero en memoria.
4. Cancelar el selector no es fallar: no se enseña nada. `exportFailed` es solo para el error de
   verdad.

**Compartir** (#43) saca una página, no el diario, y también es gratis siempre. El usuario elige un
día (`ofDay`), un mes (su calendario y sus tareas) o una colección. La app nunca sugiere qué compartir.

- **Texto**: `shareText(title, entries)` con el título en la primera línea y una línea por entrada con
  los símbolos ASCII de 4.4, sin el `- ` de lista. Una irrelevante va entre `~~`.
- **Imagen**: `renderSharePages` (`ui/SharePage.kt`) pinta páginas de `SHARE_W` x `SHARE_H` con el
  papel elegido, el título en `PageTitle`, cada entrada con `BulletGlyph` y su texto en `Ink`, y
  "Bobbin" pequeño en la esquina inferior derecha de cada página. El reparto es puro:

  ```kotlin
  // Índices de entrada de cada página. Una entrada nunca se parte entre dos: si no cabe entera en lo
  // que queda, empieza la siguiente. Una que no cabe ni en una página vacía va sola y se corta abajo.
  fun paginate(heights: List<Int>, pageHeight: Int): List<IntRange>
  ```

  Las alturas las mide el `TextMeasurer` del renderizador con el ancho real de la columna.
- `Sharing.sharePngs(pngs)` o `Sharing.shareText(text)` abren la hoja del sistema (sección 7).

### 6.11 Zip

`data/Zip.kt`, copia de `line/.../data/Zip.kt`, unas 150 líneas con sus tests. Okio no sirve: solo lee,
y solo en JVM.

```kotlin
fun crc32(bytes: ByteArray, start: Int = 0, end: Int = bytes.size, crc: Int = 0): Int  // tabla de 256

class ZipWriter(private val sink: (ByteArray) -> Unit, at: LocalDateTime) {
    fun add(name: String, bytes: ByteArray)
    fun finish()          // directorio central y EOCD
}

class ZipReader(private val source: (Int) -> ByteArray?) {   // hasta n bytes; null al final
    // Recorre las cabeceras locales en orden. Lanza ZipDamaged si falta una firma, se acaba el
    // fichero antes del directorio central, el CRC no coincide, method != 0 o el bit 0 está puesto.
    fun forEach(block: (name: String, bytes: ByteArray) -> Unit)
}
```

Firmas: cabecera local `0x04034b50`, directorio central `0x02014b50`, EOCD `0x06054b50`. Enteros en
little endian. iOS (Archivos), los gestores de Android y cualquier escritorio lo abren con doble toque:
el usuario comprueba por sí mismo que sus datos son suyos.

### 6.12 Recordatorio

Una notificación al día, **apagada por defecto**, a las 21:00 si no se cambia. Texto fijo
(`S.reminderTitle`, `S.reminderBody`), igual cada día, con bloqueo o sin él: **no cita ni una palabra
del diario ni una fecha**. Tocarla abre `bobbin://review`, la revisión del día (6.6).

```kotlin
// El siguiente instante hora:minuto estrictamente posterior a now, en hora local.
fun nextReminder(now: LocalDateTime, hour: Int, minute: Int): LocalDateTime

enum class NotifyPermission { GRANTED, CAN_ASK, DENIED }

expect object Reminders {
    fun sync()                                          // reprograma desde los ajustes; cancela si está apagado o sin permiso
    fun permission(): NotifyPermission
    fun requestPermission(onResult: (Boolean) -> Unit)  // solo desde la oferta o el interruptor
    fun openSystemSettings()
}
```

`nextReminder` suma días en `LocalDateTime`, así que tras un cambio de hora el aviso sigue a las 21:00.

**Cuándo se pide el permiso.** Nunca al arrancar. Tras guardar el primer bullet, Hoy enseña una sola
vez la oferta ("¿Te aviso para repasar el día a las 21:00?") y pone `reminderOffered = true`, se
conteste o no. Si dice que sí, `requestPermission`; con el permiso, `reminderOn = true` y `sync()`. Sin
él, el recordatorio queda apagado. El permiso se pide desde la `Activity` en Android
(`registerForActivityResult(RequestPermission)` registrado en `MainActivity`), porque no se puede desde
código común ni desde la composición.

**Interruptor de Ajustes** (#37, #38). Se pinta encendido solo si `reminderOn` y `permission() ==
GRANTED`. Si Ajustes se muestra con `reminderOn` y el permiso ya no está, pone `reminderOn = false` y
enseña el texto que explica cómo activarlo (`S.reminderDenied`). Tocar el interruptor apagado: con
`CAN_ASK`, `requestPermission`; con `DENIED`, `openSystemSettings()`. Al volver de los ajustes del
sistema (`ON_RESUME`) el interruptor refleja el permiso sin reiniciar.

- **Android**: una sola alarma **inexacta**, `setAndAllowWhileIdle(RTC_WAKEUP, nextReminder(...))`, sin
  `SCHEDULE_EXACT_ALARM`: un aviso de reflexión puede llegar unos minutos tarde, y así no hay permiso
  especial ni fricción en la revisión de Play. `ReminderReceiver` publica la notificación en el canal
  `bobbin-reflection` con `ic_notification`, un `PendingIntent` a `MainActivity` con `data =
  bobbin://review` y `autoCancel`, y vuelve a llamar a `sync()` para el día siguiente. `BootReceiver`
  escucha `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET` y `TIMEZONE_CHANGED` y llama a `sync()`.
  La app llama a `sync()` al arrancar, en cada `ON_RESUME` y al cambiar el ajuste. Es el fallo de la
  app oficial, cuyo recordatorio desaparece. `permission()`: por debajo de API 33,
  `areNotificationsEnabled()` da `GRANTED` o `DENIED`; desde API 33, concedido es `GRANTED`, y si no,
  `CAN_ASK` mientras no se haya preguntado (`Prefs.notifyAsked`) o `shouldShowRequestPermissionRationale`
  sea cierto, y `DENIED` después. Apagar cancela la alarma pendiente y no deja nada.
- **iOS**: `UNCalendarNotificationTrigger(dateMatching = hora y minuto, repeats = true)` con el
  identificador `bobbin-reflection`. `sync()` quita la petición pendiente con ese identificador y, si
  toca, la vuelve a añadir; se llama al arrancar, al volver a primer plano y al cambiar el ajuste, así
  que un cambio de hora se recoge siempre. `permission()`: `authorized` es `GRANTED`, `notDetermined`
  es `CAN_ASK`, `denied` es `DENIED`. Un delegado de `UNUserNotificationCenter`, puesto al arrancar,
  lleva el toque a `Route` (`review`).

### 6.13 Estado de los widgets

```kotlin
fun widgetState(j: Journal, isPro: Boolean, today: LocalDate): WidgetState {
    val day = j.ofDay(today)
    val m = monthOf(today)
    val mask = CharArray(monthDays(m)) { i -> if (j.ofDay(LocalDate(m.year, m.month, i + 1)).isNotEmpty()) '1' else '0' }
    return WidgetState(
        date = today.toString(),
        open = day.count { it.bullet == TASK && it.status == OPEN },
        done = day.count { it.bullet == TASK && it.status == DONE },
        events = day.count { it.bullet == EVENT },
        month = m.toString(), monthMask = String(mask),
        reviewPending = j.openTasksBefore(today).isNotEmpty() || j.unclosedMonth(today) != null ||
            (j.futureWaiting(today).isNotEmpty() && j.settings.futureSeen != m),
        isPro = isPro, cover = activeCover(j.settings, isPro).id, dayStartHour = j.settings.dayStartHour,
    )
}

// Lo que pinta el widget ahora, sin la app. La misma regla en Swift (BobbinStore.swift).
fun widgetView(st: WidgetState, today: LocalDate): WidgetState {
    if (st.date == today.toString()) return st
    val m = monthOf(today)
    val sameMonth = st.month == m.toString()
    return st.copy(
        date = today.toString(), open = 0, done = 0, events = 0,
        reviewPending = st.reviewPending || st.open > 0,
        month = m.toString(), monthMask = if (sameMonth) st.monthMask else "0".repeat(monthDays(m)),
    )
}
```

`cover` es la portada activa (6.17), así que sin Pro es `sage`. El mapa de `monthMask` se calcula aquí
una sola vez: los widgets de las dos plataformas lo leen, no lo recalculan.

`syncWidgets(j, isPro, today)` escribe `widget.json` y llama a `refreshWidgets()`. Se llama tras cada
guardado bueno, al cambiar portada, Pro o `dayStartHour`, al arrancar y en cada `ON_RESUME`. Un fallo
al escribirlo no puede tumbar un guardado: se traga el error y el widget se corrige en el siguiente.

**Sin sondeo.** El widget se repinta cuando cambia `widget.json`, y una vez más cuando cambia el día:

- **Android**: `updatePeriodMillis = 0`. `refreshWidgets()` hace `updateAll` de `TodayWidget` y
  `MonthWidget` y programa una alarma inexacta que no despierta el teléfono,
  `AlarmManager.set(RTC, nextDayStart(...))`, hacia `WidgetDayReceiver`, que vuelve a llamar a
  `refreshWidgets()`. `provideGlance` lee `widget.json` y aplica `widgetView` con la fecha lógica de
  ahora. `BootReceiver` también llama a `refreshWidgets()`. Sin `widget.json`, estado vacío (ceros),
  nunca un fallo.
- **iOS**: `getTimeline` devuelve dos entradas, ahora y `nextDayStart` (con `widgetView` aplicado a
  cada una y la fecha lógica calculada con `dayStartHour`), y política `.after(nextDayStart)`: una
  recarga al día y ninguna más. La app pide `reloadAllTimelines` a través de `BobbinBridge`. Sin
  `widget.json` o ilegible, estado vacío.

Qué pinta cada widget, en `docs/pantallas.md`:

| Widget | Plan | Datos | Toque |
|---|---|---|---|
| Hoy (Android `TodayWidget`, iOS `BobbinTodayWidget` `systemSmall` y `systemMedium`) | gratis | `open`, `done`, `events` con sus glifos | `bobbin://today?focus` |
| Mes (Android `MonthWidget`, iOS `BobbinMonthWidget`) | Pro | `monthMask` como rejilla de puntos y `open` | `bobbin://today`; sin `isPro`, estado bloqueado con "Bobbin Pro" y `bobbin://pro` |
| Pantalla de bloqueo (iOS `BobbinLockWidget`, `accessoryCircular` y `accessoryRectangular`) | Pro | punto y `open`; el rectangular, además `events` | `bobbin://today`; sin `isPro`, bloqueado y `bobbin://pro` |

El widget de bloqueo usa el mismo `TimelineProvider` que el de hoy. WidgetKit no permite quitar un
widget del catálogo según una compra, así que sin Pro se ofrece y se pinta bloqueado, como el del mes
(SPEC §6). En Android, la rejilla del mes es un `Bitmap`: Glance no tiene borde ni lienzo.

**Borrar todos los datos** (#33) reescribe `widget.json` con el estado de un diario vacío: los
widgets pasan a ceros en vez de enseñar lo borrado.

### 6.14 Almacén: cargar, reparar, escribir y migrar el esquema

```kotlin
interface JournalFiles {
    fun read(): String?                     // journal.json
    fun readPrevious(): String?             // journal.bak.json
    fun write(text: String)                 // atómico, rota la .bak
    fun restoreMain(text: String)           // reescribe journal.json sin rotar la .bak
    fun quarantine()                        // mueve las dos a corrupt/journal-<yyyyMMdd-HHmmss>.json y .bak.json
    fun keepCopy(name: String, text: String)    // journal.<name>.json: "pre-migration", "pre-import", "pre-sync"
    fun readCopy(name: String): String?
    fun wipe()                              // borra journal, .bak, .tmp, las copias y corrupt/
}

expect object Storage : JournalFiles
```

`commonTest` prueba toda la lógica con un `MemoryFiles : JournalFiles` en memoria (test 21), y
`androidHostTest` prueba el disco de verdad (test 23).

Ficheros, todos en `filesDir` (Android) o `Application Support/` (iOS):

| Fichero | Qué es |
|---|---|
| `journal.json` | el diario |
| `journal.bak.json` | la versión anterior a la última escritura |
| `journal.tmp.json` | la escritura en curso; nunca se lee |
| `journal.pre-migration.json` | el original antes de convertir el esquema |
| `journal.pre-import.json` | el diario antes de la última importación confirmada |
| `journal.pre-sync.json` | v1.2: el diario antes de la última sincronización que lo sobrescribió |
| `corrupt/` | la cuarentena |

**Escritura.** Android: escribir `.tmp` y `fsync`, renombrar el actual a `.bak`, renombrar el `.tmp` a
`journal.json` (como line). iOS: copiar el actual a `.bak`, `writeToFile(atomically = true)`, y fijar
`NSFileProtectionCompleteUntilFirstUserAuthentication` en los ficheros: con `Complete` el fichero deja
de poder abrirse unos segundos después de bloquear el teléfono y el último guardado fallaría sin aviso.
Un corte entre el `.tmp` y el rename deja el último estado válido en `journal.json` o en la `.bak`.

**Carga** (`load(files, now): LoadResult`, una vez por sesión, al arrancar):

1. `read()` existe: se lee `schemaVersion` del `JsonObject`.
   - Igual a `SCHEMA_VERSION` y decodifica: se usa.
   - Mayor: `LoadResult.TooNew`. **No se toca el fichero.** La app enseña un aviso a pantalla completa
     (`S.updateNeeded`) y no escribe nada hasta que se actualice.
   - Menor: `keepCopy("pre-migration", texto)`, se aplican los pasos (abajo), se decodifica y se
     `write` el resultado. Si un paso lanza, se restaura `journal.json` desde la copia si se llegó a
     tocar, y `LoadResult.MigrationFailed`: el diario queda intacto en su formato viejo, la app avisa
     (`S.migrationFailed`) y no escribe nada. Nunca se pierde un fichero por convertirlo.
   - No decodifica: paso 2.
2. `readPrevious()` decodifica (con la misma regla de esquema): se usa y `restoreMain()` repara el
   principal sin rotar la `.bak`, para que la copia buena sobreviva.
3. No hay ninguno de los dos: diario nuevo, vacío.
4. Existen y ninguno decodifica: `quarantine()`, diario vacío, y Hoy enseña `noticeCorrupt` hasta que
   se descarte. **Nunca se sobrescribe un fichero que no se ha podido leer.**

**Migración del esquema** (#15), en `data/Storage.kt`:

```kotlin
// SCHEMA_STEPS[i] convierte un journal de la versión i + 1 a la i + 2. Cada paso es independiente,
// recibe y devuelve JsonObject, pone el schemaVersion nuevo y rellena con el defecto de esa versión
// los campos que falten, nunca con null.
val SCHEMA_STEPS: List<(JsonObject) -> JsonObject> = emptyList()

fun migrateSchema(json: JsonObject, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS): JsonObject
```

`SCHEMA_VERSION` es `1 + SCHEMA_STEPS.size`. Se convierte paso a paso (1 a 2, 2 a 3), nunca con saltos
directos, para probar cada paso por separado y añadir uno sin tocar los anteriores. En v1.0 la lista
está vacía; el mecanismo se prueba con pasos de mentira (test 21), y cada paso real llega con su test.

**Escritura en `BobbinRepository`:**

```kotlin
private val writeLock = Mutex()
private var written: Journal? = null

fun edit(change: (Journal) -> Journal?) {
    val next = change(journal) ?: return
    journal = next                                    // el estado de Compose cambia al momento
    scope.launch { persist() }
}

suspend fun flush() = persist()

private suspend fun persist() = writeLock.withLock {
    if (readOnly) return@withLock                     // TooNew o MigrationFailed
    val snapshot = journal
    if (snapshot === written) return@withLock
    val ok = withContext(Dispatchers.IO) { runCatching { Storage.write(encode(snapshot)) }.isSuccess }
    if (ok) { written = snapshot; saveFailed = false; afterSave(snapshot) } else saveFailed = true
}
```

Se guarda **tras cada cambio**, nunca solo al cerrar: cada acción es discreta (Intro, un toque en el
glifo, una decisión de la revisión). Editar un texto guarda al confirmar, no en cada pulsación. Un solo
`Mutex` y un solo escritor: dos escrituras cruzadas rotarían la `.bak` dos veces y la copia buena se
perdería; así, siempre se escribe la última instantánea. `saveFailed` pinta `noticeSaveFailed` en
Hoy; el siguiente cambio reintenta. `afterSave` escribe `widget.json` (6.13) y llama a
`Reminders.sync()` si cambió un ajuste del recordatorio. `App.kt` llama a `flush()` en `ON_STOP`.

**Borrar todos los datos** (#33): `flush()`, `Storage.wipe()`, `journal = Journal()`, `write` del
diario vacío, `syncWidgets`, `Reminders.sync()` (queda apagado) y vuelta a Hoy sin reiniciar. No toca
`Prefs`: el derecho Pro y "valoración ya pedida" sobreviven.

### 6.15 Bloqueo

`data/Lock.kt` y `ui/LockScreen.kt`, copiados de line. Gratis siempre. Sin PIN propio: el de la app
sería un secreto más que perder.

```kotlin
expect object Lock {
    fun isAvailable(): Boolean                  // false sin bloqueo de pantalla: el interruptor no se enciende
    fun authenticate(onResult: (Boolean) -> Unit)
    fun setHidesPreview(on: Boolean)            // Android 13+; en iOS no hace nada, lo pinta Swift
}
```

- Android, sobre `BiometricPrompt` de `androidx.biometric` 1.1.0, alojado en la `FragmentActivity`
  que `MainActivity` registra al crearse (`Lock.host = WeakReference(this)`):
  - API 30+: `setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)`, sin botón negativo.
    `isAvailable() = canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS`.
  - API 24 a 29: `setAllowedAuthenticators(BIOMETRIC_WEAK)` y `setDeviceCredentialAllowed(true)`.
    `isAvailable() = KeyguardManager.isDeviceSecure`.
  - `setHidesPreview(on)`: en API 33+, `activity.setRecentsScreenshotEnabled(!on)`. Oculta la
    miniatura de multitarea sin prohibir las capturas que el propio usuario quiere hacer, que es lo que
    haría `FLAG_SECURE` (descartado, SPEC §3).
- iOS: `LAContext().canEvaluatePolicy(.deviceOwnerAuthentication)` y `evaluatePolicy` con
  `localizedReason = S.lockPromptSubtitle`; el resultado vuelve al hilo principal.
- **La vista que tapa la multitarea en iOS se pone desde `iOSApp.swift`**, no desde Compose: el sistema
  hace la foto antes de que Compose llegue a repintar, y un `Composable` deja pasar la pantalla real una
  vez (sección 7).
- En `App.kt`: `locked = settings.lockOn` al arrancar (un arranque en frío siempre pide). En `ON_STOP`,
  `backgroundAt = TimeSource.Monotonic.markNow()`. En `ON_RESUME`, si `lockOn` y
  `backgroundAt.elapsedNow() >= RELOCK_AFTER` (60 s), `locked = true`. Con `locked`, `LockScreen` tapa
  todo, se pinta antes que cualquier otra pantalla y llama a `authenticate` al aparecer.
- Encender el bloqueo: `authenticate`, y solo si responde `true` se guarda `lockOn = true`. Apagar: sin
  autenticar. Apagado por defecto.
- Cifrar el fichero por encima es teatro: la clave tendría que estar donde el propio proceso la lea sin
  el usuario.

### 6.16 Compras

`billing/Billing.kt`, el de line: RevenueCat KMP, un solo producto no consumible (`bullet_pro`) que da
el entitlement `pro`, sin niveles ni suscripción. El id del producto y el precio viven en el panel de
RevenueCat y en las tiendas, **nunca en el código**: subir a 9,99 EUR en v1.1 es tocar el panel.

- `Billing.configure()` al arrancar; `Billing.refresh()` en cada `ON_RESUME`. Sin clave
  (`revenueCatApiKey == null`), la app funciona como gratis sin fallar.
- `BobbinRepository.updatePro(active)` escribe `Prefs.pro` y llama a `syncWidgets`. **El estado de
  compra nunca bloquea contenido**: se guarda el último derecho conocido tras cada consulta; sin red o
  con el servicio caído, se conserva. Ninguna pantalla espera a RevenueCat para pintarse.
- Claves públicas como literal en `Billing.android.kt` (`goog_...`) y `Billing.ios.kt` (`appl_...`),
  `null` hasta que exista el proyecto (#7). La secreta (`sk_...`) no entra nunca en el repositorio.
- `ProDialog` (`ui/Pro.kt`, de line): qué incluye, precio leído de la tienda
  (`Billing.proPackage()`), "compra única, sin suscripción" visible sin scroll, comprar, restaurar y
  cerrar. "Restaurar compras" siempre visible, también con Pro activo (requisito de Apple), aquí y en
  Ajustes. Cerrar sin comprar deja todo como estaba.

**Dónde se abre el paywall**: solo al chocar con un límite real o a propósito.

| Choque | Comprobación |
|---|---|
| Elegir una portada o un papel Pro | `canUse(cover, isPro)`, 6.17 |
| Crear el segundo seguimiento | `canCreateTracker(j, isPro)`, 6.18 |
| Tocar un widget Pro colocado sin Pro | `bobbin://pro` |
| Generar el libro en PDF (v1.1) | `isPro` |
| Captura desde fuera (v1.1) | `isPro` |
| Activar la sincronización o abrir el resumen del año (v1.2) | `isPro` |
| La fila "Bobbin Pro" de Ajustes | a propósito |

**Nunca al arrancar ni tras un número de usos**: `App.kt` arranca siempre en `TODAY` y solo abre
`PRO` por `Route` o por un choque de la tabla. Una comprobación que falla abre el diálogo y no cambia
nada del diario.

`SIBLINGS` (`data/AppInfo.kt`): Quilt, Purl y MoodTraker, cada una con la URL de su tienda **solo si
está en producción** en ella el día que se publica Bobbin; si no, `null`. Ajustes enseña "Más apps"
con las filas que tienen URL en la plataforma actual y oculta la sección si no queda ninguna. Sin
banners, sin notificaciones, sin cruzar datos.

### 6.17 Portadas y papeles

`enum class Cover` y `enum class Paper` en `ui/theme/Theme.kt`, con los ids y hex de la sección 5
(`Cover.of(id)`, `Paper.of(id)`). Gratis: `sage` y `dotted`.

```kotlin
fun canUse(cover: Cover, isPro: Boolean) = isPro || cover == Cover.Sage
fun canUse(paper: Paper, isPro: Boolean) = isPro || paper == Paper.Dotted
fun activeCover(s: Settings, isPro: Boolean): Cover = Cover.of(s.cover).takeIf { canUse(it, isPro) } ?: Cover.Sage
fun activePaper(s: Settings, isPro: Boolean): Paper = Paper.of(s.paper).takeIf { canUse(it, isPro) } ?: Paper.Dotted
```

- La portada tiñe la cabecera y la barra inferior, los widgets (`cover` de `widget.json`) y, en v1.1,
  la cubierta del libro. El papel es fondo: nunca cambia el tamaño ni la posición de una línea.
- Todas se previsualizan sin comprar sobre una pantalla de ejemplo. Guardar una Pro sin `isPro` abre
  el `ProDialog` y no cambia `settings`.
- **Si se pierde Pro** (un reembolso), la portada y el papel activos vuelven a los gratis porque la app
  pinta `activeCover` y `activePaper`, sin reescribir `settings` ni tocar ninguna entrada. Si Pro
  vuelve, vuelve lo que se había elegido.
- Portada y papel viven en `settings` dentro de `journal.json` (SPEC §4); donde #49 dice "no dentro de
  `journal.json`" quiere decir fuera de las entradas.

### 6.18 Seguimientos

Un seguimiento es una `BulletCollection` con `kind = TRACKER`: una rejilla de filas que define el
usuario (`TrackerRow`) por días del mes, con una marca de un toque por celda (`days`). Sin
recordatorios ni estadísticas: para eso está Quilt, y la ficha remite a ella.

- **Una página por mes.** `month` es el mes de la página. La página del mes siguiente es otra
  `BulletCollection` con el mismo título, las mismas filas (mismos `id` y `title`) y `days` vacíos, y
  `threadFrom` apuntando a la anterior: **enlazada por id, nunca copiada con las marcas del mes
  pasado**.
- La página nueva no se crea sola al cambiar de mes. `trackerPage(j, tail, month)` devuelve la página
  de ese mes si existe o una en blanco sin guardar; se guarda con la primera marca o el primer cambio
  de filas. Así no nacen páginas vacías.
- Un seguimiento es su hilo; su **página más reciente** (`tail`: la que no es `threadFrom` de ninguna
  otra `TRACKER`) es la que sale en el Índice, y desde ella se navega a los meses anteriores.
- **`FREE_TRACKER_LIMIT = 1`**: `trackerCount(j)` cuenta los hilos que existen ahora, es decir las
  páginas más recientes, archivadas incluidas. Archivar uno no libera hueco; borrarlo sí.
  `canCreateTracker(j, isPro) = isPro || trackerCount(j) < FREE_TRACKER_LIMIT`. Sin Pro, crear el
  segundo abre el `ProDialog` y no crea nada. Con Pro, sin tope.
- **Si se pierde Pro**, los seguimientos que existen siguen legibles y editables, las páginas nuevas
  de los que ya existen se siguen creando, y solo no se puede crear otro.
- Borrar un seguimiento borra las páginas de su hilo; no toca ninguna otra colección. Borrar una
  página suelta engancha la siguiente a la anterior (6.7).

### 6.19 Rendimiento con miles de entradas

Un bullet ocupa unos 250 bytes con la sobrecarga del JSON: diez al día son unas 3.650 entradas y del
orden de 1 MB al año; cinco años, unas 18.000 entradas y de 4 a 5 MB. Es una estimación.

- **El diario se carga una vez por sesión** (6.14) y las pantallas leen de memoria. Ninguna pantalla
  vuelve a leer el fichero.
- Las consultas por lugar (`ofDay`, las de 6.4, `indexItems`) van sobre un índice `byPlace:
  Map<Place, List<Entry>>` que se calcula perezosamente una vez por cada `Journal` (una instancia
  inmutable por cambio) y se guarda junto a él en `BobbinRepository`.
- Listas en `LazyColumn` con `key = id`. La búsqueda corre en `Dispatchers.Default`.
- Reescribir el fichero entero en cada cambio es lo que puede dejar de valer, no el tamaño: por eso
  el escritor único fuera del hilo principal (6.14).
- **Umbrales de #55**, con `tools/perf/generar.py` (5.000 entradas repartidas en 36 meses): abrir Hoy
  en frío por debajo de 300 ms en un dispositivo de gama media; Mes e Índice a 45 fps de media o más;
  una búsqueda con filtro de bullet y de signifier por debajo de 200 ms. En la JVM de los tests,
  decodificar esas 5.000 por debajo de `PARSE_BUDGET_MS` y buscar por debajo de `SEARCH_BUDGET_MS`
  (test 26).
- **Plan B**, solo si la medida lo pide: si el percentil 95 de serializar y escribir con 5.000 entradas
  se acerca a 50 a 100 ms en el dispositivo más antiguo soportado, se parte el fichero por años y se
  mantiene en memoria el año activo. Las cadenas de migración que cruzan de año lo complican; por eso
  no es el plan A. El widget no cambia: nunca leyó el diario.

Medidas (#55 las rellena):

| Medida | Umbral | Resultado | Dispositivo |
|---|---|---|---|
| Hoy en frío, 5.000 entradas | < 300 ms | pendiente | |
| Mes, desplazamiento | >= 45 fps | pendiente | |
| Índice, desplazamiento | >= 45 fps | pendiente | |
| Búsqueda con filtros | < 200 ms | pendiente | |
| Serializar y escribir, p95 | < 50 ms | pendiente | |

---

## 7. Puentes con cada plataforma

Cada `expect` y su contrato. Todo lo que se ve fuera de la app lee y escribe por `BobbinRepository`,
nunca por su cuenta.

| `expect` | Fichero común | Contrato | Android | iOS |
|---|---|---|---|---|
| `object Storage : JournalFiles` | `data/Storage.kt` | 6.14 | `filesDir`, `.tmp` con `fsync` y rename | Application Support, `atomically`, `CompleteUntilFirstUserAuthentication` |
| `object Prefs` | `data/Prefs.kt` | `bool(key)`, `setBool(key, value)` con las claves `pro`, `reviewAsked`, `notifyAsked`. Fuera del diario: sobrevive a borrar los datos y a importar | `SharedPreferences("bobbin")` | `NSUserDefaults.standardUserDefaults` |
| `object Lock` | `data/Lock.kt` | 6.15 | `BiometricPrompt` | `LAContext` |
| `object Reminders` | `data/Reminders.kt` | 6.12 | `AlarmManager`, receptores | `UNUserNotificationCenter` |
| `fun ImageBitmap.encodeToPng()`, `object Sharing` | `data/Sharing.kt` | `sharePngs(pngs: List<ByteArray>)` y `shareText(text)` abren la hoja del sistema con lo que ya se ha pintado; nada se guarda en la galería por su cuenta | `FileProvider` en `cache/share/`, `ACTION_SEND_MULTIPLE` o `ACTION_SEND` | `UIActivityViewController` |
| `object Backup` | `data/Backup.kt` | `exclude(path)`: deja una ruta del almacén privado fuera de la copia del sistema. `Storage` lo llama para `corrupt/` | no hace nada: lo dicen las reglas de 8.2 | `NSURLIsExcludedFromBackupKey = true` |
| `val revenueCatApiKey` | `billing/Billing.kt` | 6.16 | `goog_...` o `null` | `appl_...` o `null` |
| `object FilePicker` | `data/FilePicker.kt` | `available`, `exportZip(suggestedName, write, onDone)`, `importFile(read, onDone)` con `PickResult` (`Done`, `Cancelled`, `Failed`). Cancelar no es fallar. Exportar pasa por el selector y no por la hoja de compartir: una copia tiene que quedar donde el usuario la encuentre | `CreateDocument("application/zip")`, `OpenDocument` | `UIDocumentPickerViewController` |
| `fun writeWidgetState(json)`, `fun refreshWidgets()` | `data/Widgets.kt` | 6.13 | `filesDir/widget.json`, `updateAll`, alarma del cambio de día | `widget.json` en el App Group, `BobbinBridge.reloadWidgets` |
| `object StoreReview` | `data/StoreReview.kt` | `request()`: pide la valoración al sistema, que decide si la enseña. Solo la llama 6.6 | `ReviewManagerFactory.create(context)`, `requestReviewFlow` y `launchReviewFlow` sobre `StoreReview.host` | `SKStoreReviewController.requestReviewInScene` con la escena activa |
| `object AppInfo`, `val Sibling.storeUrl`, `val onIos` | `data/AppInfo.kt` | versión visible, abrir una URL, la tienda de la plataforma | C line | C line |
| `fun systemLanguage()`, `fun systemFirstDayOfWeek()` | `i18n/Strings.kt` | idioma de dos letras del sistema; primer día de la semana del locale | `Locale.getDefault()`, `Calendar.getInstance().firstDayOfWeek` | `NSLocale.preferredLanguages`, `NSCalendar.currentCalendar.firstWeekday` |

`Strings.kt` obliga a los cinco idiomas por firma de función (`t(en, es, pt, de, fr)`) y se copia de
`docs/textos.md`. Los textos no salen de Compose Resources: parte se pinta fuera de un `@Composable`
(receptor, Glance, `Canvas`, notificación). Plurales y nombres de mes y de día de la semana, por tabla
propia, sin depender del locale de la plataforma.

### Enlaces y `Route`

`object Route { var pending by mutableStateOf<Link?>(null) }`, con
`data class Link(val screen: String, val focus: Boolean)`. `parseLink(url)` acepta solo `bobbin://today`
(con `?focus` opcional), `bobbin://review` y `bobbin://pro`; cualquier otra cosa se ignora en vez de
adivinarse. `App.kt` lo recoge cuando la app ya está en marcha o en cuanto arranca. `focus` pone el
foco en el campo de Hoy y abre el teclado.

### Kotlin que llama Swift (`iosMain/.../data/BobbinBridge.kt`)

```kotlin
object BobbinBridge {
    fun isLockOn(): Boolean                      // para la vista que tapa la multitarea
    fun open(url: String)                        // onOpenURL: parseLink y Route.pending
    var reloadWidgets: (() -> Unit)? = null      // lo asigna iOSApp.swift: WidgetCenter.shared.reloadAllTimelines()
    fun capture(text: String): CaptureResult     // v1.1, 12.5: Saved, NeedsPro, Empty
}
```

### Swift en `iOSApp.swift`

- `ZStack { ContentView(); if scenePhase != .active && BobbinBridge.shared.isLockOn() { Color(papel) } }`,
  con el papel `#FBF8F3` o `#17150F` según el esquema del sistema.
- `.onOpenURL { BobbinBridge.shared.open(url: $0.absoluteString) }`.
- En `init`: `BobbinBridge.shared.reloadWidgets = { WidgetCenter.shared.reloadAllTimelines() }`.

### Android

- `MainActivity` (`FragmentActivity`): `enableEdgeToEdge()` antes de `super.onCreate`,
  `AndroidContext.init`, `Lock.host` y `StoreReview.host`, lanzadores de `OpenDocument`,
  `CreateDocument` y `RequestPermission`, y el `data` del intent (`bobbin://...`) a `Route`. Maneja
  `onNewIntent` igual. Los widgets y la notificación abren `MainActivity` con un intent explícito que
  lleva el enlace en `data`: no hace falta un `intent-filter` del esquema, y ninguna otra app puede
  abrir un enlace `bobbin://`.
- `Widgets.android.kt`: `writeWidgetState` a `filesDir/widget.json` (atómico, `.tmp` y rename);
  `refreshWidgets` con `TodayWidget().updateAll(context)` y `MonthWidget().updateAll(context)` en una
  corrutina, y la alarma de `WidgetDayReceiver` (6.13).

---

## 8. Configuración de plataforma

### 8.1 `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:localeConfig="@xml/locales_config"
        android:supportsRtl="true"
        android:theme="@style/Theme.Bobbin">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver android:name="com.baltajmn.bullet.data.ReminderReceiver" android:exported="false" />
        <receiver android:name="com.baltajmn.bullet.data.WidgetDayReceiver" android:exported="false" />

        <!-- Broadcasts protegidos del sistema: exported es obligatorio para recibirlos. -->
        <receiver android:name="com.baltajmn.bullet.data.BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.baltajmn.bullet.widget.TodayWidgetReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/today_widget_info" />
        </receiver>

        <receiver android:name="com.baltajmn.bullet.widget.MonthWidgetReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/month_widget_info" />
        </receiver>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
```

- `INTERNET` es solo para RevenueCat: la única conexión de la app (SPEC §4). Nada de analítica,
  informes de fallos ni red propia.
- `POST_NOTIFICATIONS` se pide en el momento de 6.12, nunca al arrancar.
- **Sin `SCHEDULE_EXACT_ALARM` ni `USE_EXACT_ALARM`**: alarmas inexactas (6.12, 6.13).
- `@style/Theme.Bobbin` en `res/values/themes.xml` hereda de `android:Theme.Material.Light.NoActionBar`
  con `android:windowBackground` `#FBF8F3`, y en `values-night` de `android:Theme.Material.NoActionBar`
  con `#17150F`: el arranque no destella en blanco.
- v1.1 añade el `TileService` de 12.5 y su actividad de captura.

### 8.2 `res/xml`

`data_extraction_rules.xml` (Android 12+):

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup disableIfNoEncryptionCapabilities="true">
        <include domain="file" path="journal.json" />
        <include domain="file" path="journal.bak.json" />
    </cloud-backup>
    <device-transfer>
        <include domain="file" path="journal.json" />
        <include domain="file" path="journal.bak.json" />
    </device-transfer>
</data-extraction-rules>
```

`backup_rules.xml` (Android 11 y anteriores):

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="file" path="journal.json" requireFlags="clientSideEncryption" />
    <include domain="file" path="journal.bak.json" requireFlags="clientSideEncryption" />
</full-backup-content>
```

Con un `include`, solo se copia lo incluido: `widget.json`, `corrupt/`, las copias `journal.pre-*`
y las preferencias (Pro, valoración) se quedan fuera. A la nube **solo si va cifrada de extremo a
extremo** (`disableIfNoEncryptionCapabilities`, que en Android 9+ exige bloqueo de pantalla): quien no
lo tiene se queda sin copia en la nube (SPEC §12). Tras restaurar, `Billing.refresh()` devuelve el
Pro en el primer arranque. El diario es texto y cabe de sobra en los 25 MB de Auto Backup.

`locales_config.xml`: el de la familia (`en`, `es`, `pt`, `de`, `fr`).

`file_paths.xml`:

```xml
<paths><cache-path name="share" path="share/" /></paths>
```

`today_widget_info.xml`:

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp" android:minHeight="110dp"
    android:targetCellWidth="2" android:targetCellHeight="2"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="0"
    android:previewLayout="@layout/today_widget_preview"
    android:initialLayout="@layout/today_widget_preview"
    android:description="@string/widget_today_description"
    android:widgetCategory="home_screen" />
```

`month_widget_info.xml`: igual con `minWidth="180dp"`, `minHeight="110dp"`, `targetCellWidth="3"`,
`targetCellHeight="2"`, `month_widget_preview` y `widget_month_description`. Sin `configure`.

**La previsualización del selector de widgets no es un render real**: infla `RemoteViews`, que solo
admite vistas como `ImageView`, `TextView` y los layouts básicos. `layout/today_widget_preview.xml` y
`layout/month_widget_preview.xml` se mantienen a mano y se actualizan cada vez que cambia el diseño del
widget. `values/colors.xml` y `values-night/colors.xml` llevan `widget_background`,
`widget_on_background`, `widget_muted` y `widget_outline` con los mismos hex que el tema, porque
`RemoteViews` no lee el tema de Compose. La del mes enseña `Bobbin Pro` centrado, que es lo que ve quien
no lo tiene.

**Icono** (#18, `tools/generate_icons.py`): adaptativo con `ic_launcher_foreground`,
`ic_launcher_background` y **una capa `<monochrome>` que es una máscara de un solo color**, no una copia
en color: un PNG en color ahí no rompe el build pero sale mal en los lanzadores con iconos temáticos
(Android 13+). PNG heredados en cinco densidades e `ic_notification` en blanco sobre transparente.

### 8.3 iOS

`Config.xcconfig`:

```
TEAM_ID=
PRODUCT_NAME=Bobbin
APP_BUNDLE_ID=com.baltajmn.bullet
PRODUCT_BUNDLE_IDENTIFIER=$(APP_BUNDLE_ID)
CURRENT_PROJECT_VERSION=1
MARKETING_VERSION=1.0.0
```

`CURRENT_PROJECT_VERSION` lo pisa `release-ios.yml` con `github.run_number` al archivar; el de aquí
solo vale para un archivado a mano. Proyecto: `IPHONEOS_DEPLOYMENT_TARGET = 17.0` en los dos targets,
`TARGETED_DEVICE_FAMILY = 1,2` (universal: App Store pide capturas de iPad, #58), orientaciones como la
familia.

`iosApp/iosApp/Info.plist`, claves además de las del proyecto:

| Clave | Valor |
|---|---|
| `CADisableMinimumFrameDurationOnPhone` | `true` |
| `CFBundleDisplayName` | `Bobbin` |
| `CFBundleLocalizations` | `en`, `es`, `pt`, `de`, `fr` |
| `NSFaceIDUsageDescription` | texto en inglés; traducciones en `InfoPlist.strings` |
| `ITSAppUsesNonExemptEncryption` | `false`: la única criptografía es el HTTPS del sistema que usa RevenueCat |
| `CFBundleURLTypes` | un tipo con `CFBundleURLSchemes = [bobbin]` |

**`CADisableMinimumFrameDurationOnPhone` tiene que estar y valer `true` desde el primer commit**:
Compose Multiplatform lo comprueba al arrancar (`PlistSanityCheck`) y aborta el proceso; la app se
cierra sola y en el Simulador parece que ni se ha lanzado.

**`plutil -extract CLAVE formato fichero` sin `-o -` reescribe el fichero de entrada**: es la forma más
rápida de vaciar un `Info.plist` creyendo que solo se leía. Para leer, siempre
`plutil -extract CLAVE raw -o - fichero`.

`iosApp/BobbinWidget/Info.plist`: `NSExtension/NSExtensionPointIdentifier =
com.apple.widgetkit-extension` y el mismo `CFBundleLocalizations`.

`iosApp.entitlements` y `BobbinWidget.entitlements`: solo `com.apple.security.application-groups =
[group.com.baltajmn.bullet]`. En v1.2, la app añade el contenedor de iCloud de 12.7.

`iosApp/iosApp/PrivacyInfo.xcprivacy`: el de `line/iosApp/iosApp/PrivacyInfo.xcprivacy`
(`NSPrivacyTracking = false`, sin dominios de seguimiento, `PurchaseHistory` y `UserID` de RevenueCat
no vinculados, sin seguimiento y con propósito `AppFunctionality`; `FileTimestamp 0A2A.1` y
`SystemBootTime 35F9.1`), más una entrada:

```xml
<dict>
    <key>NSPrivacyAccessedAPIType</key>
    <string>NSPrivacyAccessedAPICategoryUserDefaults</string>
    <key>NSPrivacyAccessedAPITypeReasons</key>
    <array><string>CA92.1</string></array>
</dict>
```

- `FileTimestamp 0A2A.1` y `SystemBootTime 35F9.1`: Compose Multiplatform puede dejar `fstat`, `stat` y
  `mach_absolute_time` en el binario ([Privacy manifest for iOS
  apps](https://kotlinlang.org/docs/multiplatform/multiplatform-privacy-manifest.html)). El framework
  `Shared` es estático, así que el manifiesto de la app es el que cuenta. `35F9.1` cubre además el
  reloj monótono del rebloqueo.
- `UserDefaults CA92.1`: `Prefs` guarda en ellos lo que solo lee la propia app (Pro, valoración pedida).
- Tipos de datos: los de RevenueCat, que coinciden con App Privacy y Data Safety (`store/formularios.md`,
  #59). RevenueCat trae además su propio manifiesto. El texto del diario no sale del dispositivo.
- Cualquier cambio a lo que sale del teléfono toca en el mismo commit este fichero,
  `store/privacy/index.html` y `store/formularios.md`.
- La comprobación final es el informe de privacidad de Xcode sobre el primer archivo (Product, Archive,
  Generate Privacy Report). Lo que añada, se añade aquí y ahí.

Copia del sistema en iOS (#46): la copia de iCloud del dispositivo incluye Application Support sin
configuración. `corrupt/` se excluye con `Backup.exclude`.

---

## 9. CI

Se copian los cuatro workflows de `line/.github/workflows/`:

| Workflow | Cambios |
|---|---|
| `tests.yml` | ninguno: `:shared:testAndroidHostTest` en `ubuntu-latest` y `:shared:iosSimulatorArm64Test` en `macos-26`, en cada push a `main` y en cada PR |
| `release.yml` | `package-name: com.baltajmn.bullet`; `whatsnew-dir: store/whatsnew`; sin `signer-cn` (la clave de subida se genera con `CN=Baltasar`, el valor por defecto del workflow compartido) |
| `release-ios.yml` | archivo `Bobbin.xcarchive`; esquema `iosApp`; se salta solo con `::notice::` mientras no exista `APPSTORE_KEY_ID`, sin ponerse en rojo |
| `listings.yml` | ninguno: manual (`accion`: `estado` por defecto, o `subir`) o en push que toque `store/listings/**`, y ahí solo valida topes |

- **Los tests de iOS corren en `macos-26`**: `ui-uikit` de Compose referencia una clase de UIKit que solo
  existe desde el SDK de iOS 26, y una imagen más vieja falla el enlace.
- `release.yml` llama a `BaltaJmn/ci/.github/workflows/android-play-release.yml@main` con cada secreto
  por su nombre, **nunca con `secrets: inherit`**: el repositorio guarda también los de Apple, que no
  pintan nada ahí. Android: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`,
  `PLAY_SERVICE_ACCOUNT_JSON`. Apple: `APPSTORE_KEY_ID`, `APPSTORE_ISSUER_ID`, `APPSTORE_PRIVATE_KEY`,
  `APPLE_TEAM_ID`. Se listan en `store/ci.md`.
- La cuenta de servicio que publica en Play **no es** la de RevenueCat, que es de solo lectura a
  propósito. No se juntan.
- `release.yml` y `release-ios.yml` se disparan por etiqueta `v*`, nunca por push a `main`: cada
  publicación gasta un `versionCode`. **El `versionCode` no se reutiliza nunca, ni entre pistas**; lo
  sube quien etiqueta, en `androidApp/build.gradle.kts`, antes de etiquetar; el workflow no lo toca. Las
  etiquetas las pone el autor, nunca un agente.
- Publicar: `git tag v1.0.0 && git push origin v1.0.0`, a la pista `alpha` de Play y a TestFlight.
- **La descripción larga de Play conserva los saltos de línea tal cual**: cada párrafo de
  `store/listings/<idioma>/full.txt` va en una sola línea larga. Partirlo a 100 columnas para leerlo
  cómodo lo parte a la vista en el móvil.
- `tools/check-bobbinstore.swift` (test 36) corre en el job de `macos-26` de `tests.yml` tras los
  tests de Kotlin: `swiftc tools/check-bobbinstore.swift iosApp/BobbinWidget/BobbinStore.swift -o
  /tmp/check && /tmp/check`.

---

## 10. Tests

Fechas y relojes siempre fijos y pasados como parámetro. Un emoji se escribe con su escape
(`🙂`), nunca literal.

### Automáticos

1. **Serialización** (#11, #12): ida y vuelta de un `Journal` con comillas, un emoji, acentos, los
   tres bullets, los cinco estados, los tres signifiers, `from`, y cada forma de `place` (`daily`,
   `monthly` con y sin `day`, `future` con y sin `day`, `collection`); conserva todos los campos. Los
   defectos no aparecen en el JSON; `schemaVersion`, `entries` y `collections` sí. Un `Journal` vacío se
   escribe y se relee idéntico. `Monthly` con y sin `day` se distinguen al releer. Un `place` con dos
   claves o con `day` 31 en un mes de 30 no decodifica.
2. **Invariantes de la entrada** (#11, #23): un `EVENT` o una `NOTE` con `status` distinto de `open` se
   lee como `OPEN`, y `toggleDone` no les cambia nada; `oneLine` cambia `\n` y `\r\n` por un espacio;
   `clampCodePoints(500)` con un emoji justo en el borde no parte la pareja; `limitEdit` al tope inserta
   solo lo que cabe y conserva el final; un texto de 600 importado se puede acortar y no alargar.
3. **Día lógico y semana** (#12, #19, #32): 2026-09-23 02:30 es el 22; 03:59 es el 22; 04:00 es el 23;
   con `dayStartHour = 0`, 00:00 es el 23; el cambio de hora de 2027-03-28 en Europe/Madrid no mueve el
   corte; `nextDayStart` del 2026-09-23 con 4 es el 2026-09-24 04:00 local. Cambiar `dayStartHour` no
   cambia ninguna entrada del `Journal`. `firstDayOfWeek` con `null` da el del sistema; con 7, domingo.
4. **Mes y Future Log** (#24, #25): `monthDays` de 2026-02, 2028-02, 2026-04 y 2026-01 da 28, 29, 30 y
   31; `weekStarts` de 2026-09 con lunes da 7, 14, 21 y 28, y con domingo, 6, 13, 20 y 27; una
   `Monthly(m, 3)` no está entre las tareas sin día; `futureMonths` en 2026-03 empieza en 2026-04 y da 6
   o 24.
5. **Índice y colecciones** (#29, #30): el orden de creación no cambia al renombrar; un mes sin entradas
   no aparece; un mes con solo entradas del Future Log no aparece; el filtro encuentra "Lecturas" con
   "LECTURAS" y "Canción" con "cancion"; las archivadas van al final; un seguimiento de tres páginas sale
   una vez; borrar una colección del medio de un hilo engancha la siguiente a la anterior; un título
   vacío no crea nada.
6. **Migrar, programar y descartar** (#13, #22, #25): migrar deja el original `MIGRATED` con su texto
   intacto y crea una copia `OPEN` en el destino con `from`; programar a diciembre desde marzo crea
   `Future(diciembre)` con el original `SCHEDULED`; programar al 31 de un mes de 30 devuelve `null` y no
   cambia nada; descartar no crea copia; migrar una tarea no abierta o al mismo lugar devuelve `null`;
   migrar un evento del Future Log lo mueve sin copia.
7. **Cadena y borrado** (#13, #23, #30): tres migraciones seguidas dan `migrationCount` 3; un `from` a
   un id que no está cuenta un salto; un ciclo no cuelga; borrar el original deja un esqueleto sin texto
   y la copia conserva su recuento; borrar después la copia elimina el esqueleto; deshacer dentro del
   plazo restaura posición y estado; crear una colección, migrar a ella una tarea de hoy y la cadena de
   `from` sigue completa.
8. **Revisión y reflexión** (#26, #27, #28): `openTasksBefore`, `openTasksOfDay`, `openTasksOfMonth` y
   `unclosedMonth` sobre un diario fijo; cinco tareas con las cinco acciones quedan cada una en su
   estado; recalcular la cola tras decidir dos deja solo las otras tres; guardar una reflexión crea una
   `NOTE` en `Monthly(m, null)`; saltar no crea nada; con tres entradas del Future Log del mes,
   `futureWaiting` cuenta 3 y baja de una en una al decidir; cambiar de mes sin abrir el Mes no toca
   ninguna.
9. **Sin acciones masivas** (`androidHostTest`, #13, #27, #64): por reflexión de la JVM sobre
   `MigrationKt` y `CollectionsKt`, ningún método público tiene un parámetro `Collection`, `Iterable`,
   `Sequence` ni array, y ninguno se llama `*All`. El receptor `Journal` no es una colección.
10. **Captura rápida** (#20, #21): cada prefijo solo; `"* - texto"` y `"- * texto"` dan la misma nota
    con prioridad; `"* ! ? o texto"` da un evento con los tres signifiers; `"-5 grados"` y `"hola - x"`
    son tareas; `"- - x"` es una nota con texto `"- x"`; `"- "` y `"* "` solos devuelven `null`; el
    selector da el bullet sin prefijo y el prefijo gana al selector; un salto de línea pegado se
    convierte en espacio.
11. **Búsqueda** (#35): `"cafe"` encuentra `"Café"`, `"cafe"` y un `"Café"` descompuesto (NFD);
    `"strasse"` encuentra `"Straße"`; el filtro `OPEN` excluye hechas, migradas, programadas y
    descartadas; `"#viaje"` encuentra `"Billetes #viaje"` y no `"viajero"`; los grupos salen por lugar,
    del más reciente al más antiguo; los esqueletos no salen.
12. **Estado de los widgets** (#40, #41, #42, #51, #52): `widgetState` de un diario fijo es exactamente
    `WIDGET_SAMPLE`; el JSON no contiene ningún texto de entrada ni título de colección; `open`, `done` y
    `events` cuentan `Daily` y la línea del calendario de hoy; `widgetView` con la fecha de ayer da ceros
    y `reviewPending`; al cambiar de mes, `monthMask` vacío del largo del mes nuevo; `isPro = false` y
    `cover = sage` sin Pro aunque el ajuste diga `rose`.
13. **Recordatorio** (#36, #38): `nextReminder` a las 20:59 es hoy a las 21:00; a las 21:00 en punto,
    mañana; a través del cambio de hora de Europe/Madrid sigue a las 21:00 locales. Los textos del
    recordatorio no dependen del `Journal` (se llaman sin él).
14. **Zip** (#44): ida y vuelta con `journal.json` y dos Markdown con acentos en el nombre;
    `crc32("123456789")` es `0xCBF43926`; un zip cortado por la mitad lanza `ZipDamaged`. En
    `androidHostTest`, si el sistema tiene `unzip`, `unzip -t` lo da por bueno.
15. **Exportar Markdown** (#44, #32): el Markdown de un mes lista los cinco estados con su símbolo, los
    signifiers delante y la irrelevante entre `~~`; secciones vacías no salen; los esqueletos no salen;
    los nombres de colección se sanean y los repetidos llevan ` 2`; el `journal.json` del zip se relee
    igual y lleva los ajustes actuales.
16. **Validación de importación** (#45): cada fila de 4.6 con su clave; un `schemaVersion` menor se
    convierte en memoria; una copia de Purl, de MoodTraker y de Quilt da `importIsSibling`.
17. **Compartir** (#43): una colección de 40 entradas con alturas distintas se reparte en varias páginas
    y ninguna entrada queda partida ni se pierde; el texto plano usa los símbolos ASCII y no incluye
    esqueletos.
18. **Portadas, papeles y paywall** (#48, #49): `activeCover` con `rose` y sin Pro es `sage`, y con Pro
    vuelve a `rose`, sin cambiar `settings` ni ninguna entrada; un id desconocido es `sage` o `dotted`;
    `canUse` falla para cualquier Pro sin `isPro`; el destino inicial de `App` es `TODAY` y no hay
    ninguna ruta que abra `PRO` sin un `Link` o un choque.
19. **Seguimientos** (#50): la página de un mes nuevo tiene las mismas filas, `days` vacíos y
    `threadFrom` a la anterior, y no se guarda hasta la primera marca; con uno existente y sin Pro,
    `canCreateTracker` es falso; archivar no libera hueco y borrar sí; con Pro, sin tope.
20. **Valoración** (#56): `shouldAskReview` es cierto tras la primera revisión mensual completa y falso
    después de marcar `reviewAsked`; una segunda revisión mensual no vuelve a pedir; una revisión de día
    no pide.
21. **Almacén: carga, reparación y esquema** (#14, #15, #33), con `MemoryFiles`: principal ilegible y
    `.bak` buena, carga la `.bak` y repara el principal sin rotarla; los dos ilegibles, cuarentena,
    diario vacío y ningún fichero sobrescrito; ninguno, diario nuevo; cien cambios seguidos por
    `BobbinRepository` no pierden ninguna entrada y el último guardado es la última instantánea; un paso
    de esquema de mentira convierte un JSON de la versión 1 en el esperado de la 2 y guarda
    `pre-migration`; un paso que lanza deja `journal.json` como estaba y da `MigrationFailed`; un
    `schemaVersion` mayor da `TooNew` sin escribir; un campo ausente de la versión vieja recibe su
    defecto; `wipe` deja el diario vacío sin `.bak`, copias ni cuarentena.
22. **Fusión** (#45): importar el mismo diario dos veces no duplica nada (`added` 0 la segunda);
    una tarea `DONE` no vuelve a `OPEN` con una copia vieja que la tiene abierta y más reciente; gana el
    `updatedAt` más reciente; los contadores `added`, `updated` y `same` cuadran; los ajustes de la copia
    solo se aplican con el diario vacío; nada del dispositivo desaparece.
23. **Almacén en disco** (`androidHostTest`, #14, #33): con `rootOverride` a una carpeta temporal, un
    `journal.tmp.json` a medias y un `journal.json` ilegible cargan la `.bak`; un corte simulado entre
    escribir el `.tmp` y el rename conserva el último estado válido; la cuarentena mueve los dos a
    `corrupt/`; `wipe` no deja nada en la carpeta.
24. **Textos** (#16): cada función de `S` devuelve texto no vacío en los cinco idiomas; los plurales de
    0, 1 y 2 tareas y entradas son correctos en cada idioma; los doce meses y los siete días existen en
    cada idioma.
25. **Tema** (#17, #34): contraste de cada par de texto y fondo de `docs/pantallas.md` 1, en claro y en
    oscuro, de 4,5:1 o más; `isWideScreen` con 599, 600 y 601 dp da falso, cierto y cierto.
26. **Rendimiento** (`androidHostTest`, #55): el diario de 5.000 entradas en 36 meses (generado en el
    test con semilla fija, igual que `tools/perf/generar.py`) se decodifica por debajo de
    `PARSE_BUDGET_MS` y una búsqueda con filtro de bullet y de signifier tarda menos de
    `SEARCH_BUDGET_MS`.
27. **Threading** (v1.1, #63): continuar crea una colección con `threadFrom`; "viene de" y "sigue en"
    se resuelven en los dos sentidos; archivar la colección origen mantiene el enlace navegable; el
    Índice agrupa el hilo; continuar no crea ninguna entrada.
28. **Nuevo cuaderno** (v1.1, #64): con diez colecciones y veinte tareas abiertas, cada paso del flujo
    ofrece acciones sobre un solo elemento; al terminar, `notebooks` gana la fecha y el Índice pliega lo
    anterior sin borrar nada.
29. **Preguntas** (v1.1, #65): encadenar revisiones no repite pregunta mientras queden otras sin usar;
    al agotar el banco empieza otra vuelta sin repetir la última.
30. **Fechas en lenguaje natural** (v1.1, #66): tabla de frases por idioma con su fecha esperada desde un
    hoy fijo ("mañana", "el viernes", "14 de marzo", "tomorrow", "on Friday", "amanhã", "morgen",
    "demain"...); texto sin fecha da `null`.
31. **Captura desde fuera** (v1.1, #67): `BobbinBridge.capture` sin Pro da `NeedsPro` y no crea nada; con
    Pro, `"o Dentista"` crea un evento en `Daily(hoy lógico)`; vacío da `Empty`.
32. **Sincronización** (v1.2, #69): solo cambió un lado, gana ese lado y se guarda `pre-sync` antes de
    sobrescribir; cambiaron los dos, devuelve `Conflict` sin escribir nada; fusionar usa `merge` y no
    duplica.
33. **Resumen del año** (v1.2, #70): reúne inspiraciones, prioridades hechas y las más migradas por
    `migrationCount`; un año sin signifiers da el estado vacío, no un cero.
34. **Índice por tema** (v1.2, #71): una colección sin tema cae en el grupo "sin tema"; dentro de cada
    grupo el orden sigue siendo el de creación; sin iconos ni temas, `indexItems` da lo mismo que antes.
35. **Importar de las hermanas** (v1.2, #72): un fichero de Purl, uno de MoodTraker y uno de Quilt se
    convierten en candidatos con la fecha y el texto sin pérdidas; una línea con fecha inválida se salta
    y el resto sigue; un fichero que no es de ninguna da `importNotSibling`.
36. **Espejo Swift de `widget.json`** (#40, #42): `tools/check-bobbinstore.swift` saca `WIDGET_SAMPLE`
    de `WidgetSample.kt`, lo decodifica con `BobbinStore.swift` y comprueba cada campo, y comprueba el
    `widgetView` y el día lógico de Swift con las mismas fechas que el test 12. La extensión no tiene
    target de tests; por eso es un script (9).

### A mano, en emulador y Simulador

37. **Arranque** (#21, #31, #48): en frío, Hoy con el campo enfocado y el teclado arriba sin ningún
    toque; diez bullets seguidos sin soltar el foco; en una instalación limpia se escribe el primer
    bullet sin pasar por otra pantalla; las tareas abiertas de ayer no están en la lista de hoy; nunca
    sale el paywall.
38. **Cambio de día** (#19): con la app en segundo plano al cruzar las 04:00, volver enseña el día nuevo
    sin reiniciar.
39. **Estados persistentes** (#22): cada estado y signifier se relee igual tras cerrar y reabrir.
40. **Recordatorio** (#36, #37, #38): sigue programado tras reiniciar y tras cambiar de zona horaria;
    tocarlo abre la revisión del día; con el permiso denegado, el interruptor sale apagado con su texto;
    concederlo en los ajustes del sistema y volver lo refleja sin reiniciar.
41. **Bloqueo** (#39): volver a los 59 segundos no pide; a los 61, sí; la miniatura de multitarea no
    enseña el diario en ninguna plataforma.
42. **Widgets** (#41, #42, #51, #52): completar una tarea actualiza el widget; el de hoy abre Hoy con el
    teclado; sin `widget.json`, estado vacío sin cerrarse; con `isPro` falso, el del mes y el de bloqueo
    salen bloqueados y abren el paywall; el de bloqueo solo enseña números y glifos.
43. **Copia del sistema** (#46): `adb shell bmgr backupnow com.baltajmn.bullet`, desinstalar, reinstalar
    y restaurar recupera el diario completo.
44. **Compras** (#47): tras comprar en sandbox, sin red, la app sigue siendo Pro; restaurar en una
    instalación limpia devuelve Pro.
45. **Accesibilidad** (#53): recorrido completo de Hoy y de Revisar con TalkBack y con VoiceOver; con la
    fuente al 200 % no se corta ningún texto; toda diana mide 48 dp o más.
46. **Rendimiento en dispositivo** (#55): los umbrales de 6.19 con el fichero de 5.000 entradas
    importado; el resultado va a la tabla de 6.19.
47. **Tableta** (#34): iPad y emulador de 10 pulgadas, Hoy, Mes, Futuro e Índice sin texto cortado ni
    botones fuera.
48. **Libro** (v1.1, #68): 12 meses con 2.000 entradas generan el PDF sin agotar memoria en el
    dispositivo más pequeño de la lista de pruebas.
49. **Sincronización** (v1.2, #69): editar sin conexión en dos dispositivos y reconectar pregunta en vez
    de duplicar.

---

## 11. Qué gobierna cada issue

Las secciones de este documento que manda cada issue, más los tests (sección 10) y los otros
documentos. Si una issue y los documentos no coinciden, mandan los documentos; las diferencias ya
resueltas están en la columna de notas.

| Issue | Secciones | Otros | Notas |
|---|---|---|---|
| #1 Nombre e identificadores | 1 | SPEC §8 | |
| #2 Precio | 6.16 | SPEC §7, `store/revenuecat.md` | |
| #3 SPEC | 11 | `SPEC.md` | |
| #4 Este documento | 1 a 12 | `SPEC.md` | |
| #5 Pantallas | 11 | `docs/pantallas.md`, SPEC §5 | Si fija otro `MAX_CONTENT_WIDTH`, se corrige la sección 5 |
| #6 Textos | 11 | `docs/textos.md` | Incluye las claves de 4.6, 6.6, 6.12 y 6.14 |
| #7 Altas y cuentas | 9 | `store/ci.md`, `store/revenuecat.md` | |
| #8 Andamiaje | 2, 3, 10 | `testAndroidHostTest` e `iosSimulatorArm64Test` en vacío | `Text.kt` no se copia como fichero (3) |
| #9 CI | 9, 10 | `store/ci.md` | La etiqueta de prueba la pone el autor |
| #10 Prueba cerrada | 9 | `store/lanzamiento.md` | |
| #11 `Entry` | 4.1, 10 | tests 1, 2 | `Bullet` es un `enum`: un conjunto cerrado |
| #12 `Place`, `Journal`, `DayClock` | 4.1, 6.1, 10 | tests 1, 3 | |
| #13 Migración en el modelo | 6.4, 10 | tests 6, 7, 9 | |
| #14 Almacén atómico | 4.1, 6.14, 10 | tests 21, 23 | |
| #15 Migración del esquema | 4.1, 6.14, 10 | test 21 | |
| #16 `Strings.kt` | 10, 11 | test 24, `docs/textos.md` | |
| #17 Tema y glifos | 6.3, 10 | test 25, `docs/pantallas.md` | |
| #18 Icono | 8.1, 8.2, 8.3 | `docs/pantallas.md` | |
| #19 Navegación | 6.1, 10 | tests 3, 38 | |
| #20 Captura rápida | 6.2, 10 | test 10 | |
| #21 Hoy | 6.1, 6.2, 10 | tests 10, 37, `docs/pantallas.md` | Hoy lista `ofDay` (6.5) |
| #22 Estados y signifiers | 6.3, 6.4, 10 | tests 6, 39 | |
| #23 Editar, reordenar, borrar | 4.1, 6.4, 10 | tests 2, 7 | |
| #24 Mes | 6.5, 10 | test 4 | |
| #25 Future Log | 6.5, 10 | tests 4, 6 | |
| #26 Aviso del Future Log | 6.4, 6.5, 10 | test 8 | Un evento o una nota se mueve; descartarlos es borrar |
| #27 Revisión | 6.4, 6.6, 10 | tests 8, 9 | |
| #28 Reflexión | 6.6, 10 | test 8 | |
| #29 Índice | 6.7, 10 | test 5 | |
| #30 Colecciones | 4.1, 6.7, 10 | tests 5, 7 | |
| #31 Clave y primer arranque | 6.3, 10 | test 37 | |
| #32 Ajustes | 4.1, 6.10, 10 | tests 3, 15 | |
| #33 Borrar todos los datos | 6.13, 6.14, 10 | tests 21, 23 | |
| #34 Tableta | 10, 11 | tests 25, 47 | |
| #35 Búsqueda | 6.8, 10 | test 11 | |
| #36 Recordatorio Android | 6.12, 10 | tests 13, 40 | El permiso se pide al aceptar la oferta que sale tras el primer bullet (SPEC §6) |
| #37 Permiso denegado Android | 6.12, 8.1 | test 40 | |
| #38 Recordatorio iOS | 6.12, 7, 10 | tests 13, 40 | |
| #39 Bloqueo | 6.15, 7, 10 | test 41 | |
| #40 `widget.json` | 4.2, 6.13, 6.14, 10 | tests 12, 36 | |
| #41 Widget de hoy Android | 6.13, 8.2, 10 | tests 12, 42 | |
| #42 Widget de hoy iOS | 6.13, 8.3, 10 | tests 12, 36, 42 | |
| #43 Compartir | 6.10, 10 | test 17 | |
| #44 Zip y exportar | 4.3, 4.4, 6.10, 6.11, 10 | tests 14, 15 | |
| #45 Importar fusionando | 4.1, 6.9, 10 | tests 16, 22 | `SCHEDULED` también es un estado cerrado |
| #46 Copia del sistema | 8, 10 | test 43 | |
| #47 Compras | 2, 6.16, 10 | test 44 | El derecho vive en `Prefs` |
| #48 Paywall | 6.16, 10 | tests 18, 37 | |
| #49 Portadas y papeles | 6.17, 10 | test 18 | Viven en `settings` (6.17) |
| #50 Seguimientos | 6.7, 6.18, 10 | test 19 | |
| #51 Widget del mes | 6.13, 6.16, 10 | tests 12, 42 | |
| #52 Widget de bloqueo iOS | 6.13, 6.16, 10 | tests 12, 42 | Sin Pro se ofrece bloqueado: WidgetKit no permite quitarlo del catálogo |
| #53 Accesibilidad | 7, 10 | test 45 | |
| #54 Tests | 6.1, 6.2, 6.4, 6.9, 6.13, 6.14, 9, 10 | tests 1 a 36 | |
| #55 Rendimiento | 6.14, 6.19, 10 | tests 26, 46 | |
| #56 Valoración | 7, 10 | test 20 | Añade `play-review` (2) |
| #57 Ficha | 9, 10 | `store/listings/`, `store/app-store/` | |
| #58 Capturas | 9 | `store/capturas.md`, `docs/pantallas.md` | |
| #59 Privacidad y formularios | 8.1, 8.3 | `store/formularios.md`, `store/privacy/` | |
| #60 Beta | 9 | `store/lanzamiento.md` | |
| #61 Dispositivo físico | 11 | tests 37 a 45 | |
| #62 Lanzamiento | 9 | `store/lanzamiento.md` | |
| #63 Threading (v1.1) | 6.7, 10, 12.1 | test 27 | |
| #64 Nuevo cuaderno (v1.1) | 6.4, 6.6, 10, 12.2 | tests 9, 28 | |
| #65 Reflexión guiada (v1.1) | 6.6, 10, 12.3 | test 29 | |
| #66 Fechas en lenguaje natural (v1.1) | 6.2, 6.4, 10, 12.4 | test 30 | |
| #67 Captura desde fuera (v1.1) | 6.2, 7, 10, 12.5 | test 31 | |
| #68 Libro en PDF (v1.1) | 6.10, 6.19, 10, 12.6 | test 48 | |
| #69 Sincronización (v1.2) | 6.9, 10, 12.7 | tests 32, 49 | |
| #70 Resumen del año (v1.2) | 6.4, 6.6, 10, 12.8 | test 33 | |
| #71 Variaciones del índice (v1.2) | 6.7, 10, 12.9 | test 34 | |
| #72 Importar de las hermanas (v1.2) | 4.5, 4.6, 10, 12.10 | test 35 | Un hábito hecho de Quilt es una tarea hecha (12.10) |

---

## 12. v1.1 y v1.2

### 12.1 Threading

`BulletCollection.threadFrom` existe desde el esquema 1. v1.1 añade:

- `continueCollection(j, id, title, now, newId)`: una colección nueva, del mismo `kind`, con
  `threadFrom = id` y el título que escriba el usuario (se le propone el de la actual). No crea
  entradas ni páginas: solo el enlace. Solo se ofrece si la colección no tiene ya continuación.
- `threadPrev(c) = threadFrom`; `threadNext(c)` = la colección cuyo `threadFrom` es `c.id`. La cabecera
  enseña "viene de" y "sigue en", navegables. Archivar cualquiera de las dos no rompe el enlace.
- El Índice agrupa un hilo en la posición de su primera colección, con las siguientes debajo en orden.
- Gratis: no pasa nunca por `ProDialog`.

### 12.2 Nuevo cuaderno anual

Flujo propio, lanzable desde Ajustes cuando el usuario quiera, no solo en enero. Gratis. Dos pasos, un
elemento cada vez y sin ninguna acción masiva (test 9 cubre las funciones nuevas):

1. **Cada colección** no archivada (incluidos los seguimientos): mantener, continuar con threading
   (12.1) o archivar.
2. **Cada tarea abierta** de cualquier lugar salvo el Future Log de meses posteriores, con las cinco
   acciones de la revisión (6.6).

Al terminar, `settings.notebooks += hoy`. El Índice pliega bajo "Cuaderno hasta <fecha>" todo lo creado
antes de la última fecha de `notebooks`: sigue legible, no se borra nada. Salir a medias conserva lo
decidido, porque cada decisión ya cambió su elemento.

### 12.3 Reflexión guiada

`S.question(i)` con `i` en `0 until QUESTION_COUNT`, en los cinco idiomas. En el paso de reflexión
(6.6), una pregunta junto al campo de nota, opcional y que se salta igual que el paso. Elección:

```kotlin
fun nextQuestion(used: List<Int>, random: Random): Int   // una no usada; si no queda ninguna, cualquiera menos la última
```

`settings.questionsUsed` guarda las usadas en esta vuelta y se vacía (dejando la última) al agotarse.
La pregunta nunca sale en una notificación ni fuera de la reflexión.

### 12.4 Fechas en lenguaje natural

`model/DateHint.kt`, en local y sin servidor:

```kotlin
data class DateHint(val date: LocalDate, val range: IntRange)   // range: dónde está la expresión en el texto
fun dateHint(text: String, lang: String, today: LocalDate, firstDayOfWeek: DayOfWeek): DateHint?
```

Reconoce, en `en`, `es`, `pt`, `de` y `fr`: hoy y mañana ("mañana", "tomorrow", "amanhã", "morgen",
"demain"), pasado mañana, un día de la semana (el siguiente de ese nombre, nunca hoy), "el 14", "14 de
marzo" y "14/3" (día y mes; si ya pasó este año, el que viene). Las tablas de palabras van en
`i18n/Strings.kt`. Al detectar una fecha, bajo el campo aparece una pastilla "Programar para 14 mar";
**nunca se aplica sola**. Tocarla crea la entrada en `Monthly(mes, día)` si la fecha es de este mes, o
en `Future(mes, día)` si es de uno posterior. El texto se guarda tal cual lo escribió el usuario.
Gratis.

### 12.5 Captura desde fuera

Pro. Las dos entradas pasan el texto por `rapidParse` (6.2) y crean la entrada en `Daily(hoy lógico)`
con el `dayStartHour` vigente, igual que Hoy.

- **Android**: `CaptureTileService` (`TileService`, etiqueta `S.tileLabel`) abre `CaptureActivity`, una
  actividad con tema de diálogo que enseña solo el campo sobre la pantalla actual. Sin Pro, abre
  `MainActivity` con `bobbin://pro`. Se declaran en el manifiesto con
  `android.permission.BIND_QUICK_SETTINGS_TILE`.
- **iOS**: `iosApp/iosApp/Shortcuts.swift` con `struct CaptureIntent: AppIntent,
  ForegroundContinuableIntent`, `@Parameter var text: String`, título "Note in Bobbin" y descripción
  **literales** en inglés, y `AppShortcutsProvider` con frases que llevan `\(.applicationName)`. Las
  traducciones van en `<lang>.lproj/Localizable.strings`: un valor de la tabla de idiomas rompe el build
  con `No AppIntents metadata have been exported`. `perform()` llama a `BobbinBridge.shared.capture`:
  `Saved` responde `S.captureSaved`; `NeedsPro` pide continuar en primer plano y deja `Route` en `pro`;
  `Empty` responde que no había texto.

### 12.6 Libro en PDF

Pro. `book/Book.kt` (común) calcula las páginas; `expect object BookRenderer` las dibuja: Android con
`PdfDocument` y un `Typeface` de la misma Literata; iOS con `UIGraphicsPDFRenderer` y la fuente
registrada con `CTFontManagerRegisterFontsForURL`. A5, 420 x 595 puntos, con el papel elegido
(`activePaper`) y los glifos de `BulletGlyph` con su misma geometría. Páginas: portada con la portada
elegida, la clave, el índice (6.7), cada mes (calendario, tareas del mes y cada Daily Log) y cada
colección, en orden de creación; en v1.2, el resumen del año como apéndice (12.8). Se dibuja página a
página en un fichero temporal, sin montar el libro en memoria, y se entrega al selector del sistema.
Nombre `bobbin-book-AAAA-MM-DD.pdf`. Cancelable entre páginas. Se genera desde Ajustes y desde el
Índice. El precio sube a 9,99 EUR el día que sale (**[autor]**, en el panel); quien compró antes lo
tiene sin pagar de nuevo, porque la compra es de por vida.

### 12.7 Sincronización

Pro. Un único `bobbin-sync.json` (el `Journal` codificado con `JournalJson`) en la nube del propio
usuario, sin cuenta en la app y sin servidor nuestro. iOS: el contenedor de iCloud Drive
`iCloud.com.baltajmn.bullet` (entitlement de la app), vigilado con `NSMetadataQuery`. Android: un
fichero elegido o creado con el selector del sistema (`OpenDocument` o `CreateDocument`, que incluye
Drive), con `takePersistableUriPermission`.

Cada dispositivo guarda `sync/base.json`, la última versión sincronizada. Al arrancar, en `ON_RESUME`
y tras cada guardado:

| Local frente a `base` | Remoto frente a `base` | Qué pasa |
|---|---|---|
| igual | igual | nada |
| cambió | igual | se escribe el local en remoto |
| igual | cambió | `keepCopy("pre-sync", local)` y se aplica el remoto |
| cambió | cambió | `Conflict`: se pregunta, este dispositivo, el otro, o fusionar con `merge` (6.9) y su resumen |

**Nunca hay fusión silenciosa.** Tras cualquier resolución, `base` pasa a ser lo escrito. Activarlo
toca en el mismo commit `store/privacy/index.html`, `store/formularios.md` y `PrivacyInfo.xcprivacy`:
el diario pasa a salir hacia la nube del usuario.

### 12.8 Resumen del año

Pro. Solo texto: sin gráficas, porcentajes ni comparativas.

```kotlin
data class YearSummary(val inspirations: List<Entry>, val prioritiesDone: List<Entry>, val mostMigrated: List<Pair<Entry, Int>>)
fun Journal.yearSummary(year: Int): YearSummary
```

- `inspirations`: entradas no esqueleto con `INSPIRATION` creadas en `year`.
- `prioritiesDone`: tareas `DONE` con `PRIORITY` creadas en `year`.
- `mostMigrated`: las diez tareas con mayor `migrationCount` (6.4) desde 2, contadas en el último
  eslabón de cada cadena creado en `year`. Sin contador aparte.
- Un año sin nada de eso da un estado vacío que invita a escribir el año que viene, nunca un cero.
  Exportable como apéndice del libro (12.6).

### 12.9 Variaciones del índice

Gratis: son del método original. Todo opcional; un Índice sin iconos ni temas se ve igual que antes.

- **Icono**: `BulletCollection.icon` y `Journal.months["yyyy-MM"].icon`, uno de `INDEX_ICONS`, 12 glifos
  dibujados en `Canvas` como los del método: `star`, `heart`, `book`, `home`, `work`, `travel`, `money`,
  `health`, `food`, `music`, `idea`, `people`.
- **Tema**: `theme`, una etiqueta libre de una línea (hasta 24 puntos de código). El Índice ofrece un
  filtro por tema y `groupByTheme(items)` agrupa con los sin tema al final, manteniendo el orden de
  creación dentro de cada grupo.
- **Subrayado de lo migrado**: un mes pasado con `openTasksOfMonth` vacío y al menos una tarea
  `MIGRATED` se pinta subrayado, como en el papel.

### 12.10 Importar de las apps hermanas

Gratis. La pantalla de importar de 6.9 reconoce los formatos de 4.5 y, en vez de fusionar, abre una
revisión **candidato por candidato**: cada uno se importa o se salta, y se elige a qué día o colección
va (por defecto, `Daily(fecha original)`). Nada se escribe en `journal.json` hasta que el usuario
decide cada uno, y cada importado es una entrada nueva con id nuevo. Nunca un volcado automático: es el
mismo traspaso masivo que la migración evita.

- Notas de Purl y MoodTraker: `EVENT` con su fecha original, nunca tareas.
- Hábitos de Quilt: `TASK` en `DONE`. #72 pide "evento con el signifier de tarea completada", y ese
  signifier no existe en el método: la marca de algo hecho es el aspa sobre una tarea (SPEC §2.3), y
  aquí manda el documento.
- Un fichero que no es de ninguna de las tres da `importNotSibling` y no se adivina el formato. Sin red.
