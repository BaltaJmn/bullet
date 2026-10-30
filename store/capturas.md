# Capturas y gráficos de las fichas

Seis escenas en los trece idiomas de la ficha (en-US, es-ES, pt-BR, de-DE, fr-FR, it-IT, nl-NL,
pl-PL, ru-RU, tr-TR, id, ja-JP, ko-KR). Se sacan con un diario de demostración generado, nunca con uno real: un diario de verdad en una ficha pública es justo
lo que esta app promete no hacer. Todo lo que se ve existe en la app; la portada y el papel Pro de la
escena 06 son la vista previa de Ajustes, no un montaje aparte.

Herramientas (`docs/tecnico.md` 3):

- `tools/demo/generar.py`: escribe el diario de demostración.
- `tools/store/crudas.py`: recorre las escenas en el emulador y deja las capturas crudas.
- `../gk/contenido/plantilla/tienda/`: la plantilla de capturas de la familia (su README). Monta las de
  App Store y Play con el titular, el fondo y el recorte de cada escena; el frame `bobbin` de `apps.js`
  y los textos de `textos.js`.
- `tools/store/cabecera.py`: gráfico de cabecera de Play e icono de 512.

```bash
python3 tools/demo/generar.py --idioma es-ES
python3 tools/store/crudas.py es-ES      # y copiarlas a store/screenshots/raw/android/es-ES/
../gk/contenido/plantilla/tienda/todas.sh bobbin
```

Las crudas viven en `store/screenshots/raw/`: `android/<idioma>/` del emulador para Play e
`iphone/<idioma>/` del simulador "Capturas-Bobbin" (iPhone 17 Pro Max, iOS 26.5) para la App Store,
porque Apple no admite un teclado ni una pantalla de inicio de Android. `todas.sh` deja las finales en
`store/screenshots/play/` e `iphone/`.

---

## 1. Tamaños

| Destino | Dispositivo | Captura cruda | Imagen final |
|---|---|---|---|
| `play` | Emulador `Pixel_8_capturas`, API 35 | 1080x2400 | 1080x2160 PNG |
| `iphone` | Simulador "Capturas-Bobbin", iPhone 17 Pro Max | 1320x2868 | 1320x2868 PNG (6,9") |
| `ipad` | Simulador iPad Pro 13-inch (M5) | 2064x2752 | La misma, sin plantilla (13") |

La app es universal y App Store Connect no envía sin capturas de iPad de 13". Van solo en en-US, que
la tienda enseña en los idiomas que no tienen las suyas, y crudas: la plantilla recorta para el ancho
del iPhone. Son cinco, las escenas de iPhone en su orden menos los widgets, que no tienen cruda de
iPad. Las crudas salen como las de iPhone (sección 5) en `tools/demo/salida/en-US/crudas-ipad/`; al
pasarlas a `store/screenshots/ipad/en-US/` se tapan con el color del papel las filas 0 a 70, la
barra de estado, que el simulador pinta con la fecha en su propio idioma. Se suben con
`APP_IPAD_PRO_3GEN_129`: `appstore.py capturas` solo sabe de iPhone.

Play rechaza una captura cuyo lado largo pase del doble del corto: 1080x2160 es el máximo. La plantilla
quita la barra de estado (90 px en Android, 150 en iPhone) y recorta cada escena por su propio fin, con
ajustes por idioma donde una traducción alarga la pantalla o Ajustes queda en otro punto del scroll.

## 2. Las seis escenas

| Fichero | Pantalla | Qué tiene que verse |
|---|---|---|
| `01_hoy` | Hoy, al abrir | La tarjeta del mes sin cerrar; evento, tarea hecha, tarea con prioridad y la tarea que llegó pasada de ayer; una línea a medio escribir en la barra de escribir y el teclado arriba |
| `02_mes` | Mes, desde arriba | El título, su explicación, la tarjeta del mes sin cerrar y los primeros días con sus eventos |
| `03_revisar` | Revisión del mes anterior, abierta desde la tarjeta de Hoy | Pasado el releer con "Decidir las 4 tareas"; la primera tarea abierta, "Ya la has pasado 2 veces", con las cinco salidas y lo que deja cada una |
| `04_indice` | Índice | Tres meses, dos listas y un seguimiento, en el orden en que se empezaron |
| `05_widgets` | Segunda página del lanzador | El widget de hoy y el del mes, con Pro para que el del mes no salga bloqueado |
| `06_cuaderno` | Ajustes, sección Cuaderno | La vista previa con una portada (lila) y un papel (cuadrícula) Pro mirados sin comprar, y la línea de qué es gratis |

`crudas.py` busca cada botón por su texto en el árbol de accesibilidad, no por coordenadas, así que
sirve igual en todos los idiomas. Lo que no hace solo:

- **Ruso, japonés y coreano no teclean** en la escena 01: `adb shell input text` solo escribe ASCII.
  Hoy sale con el campo enfocado, el teclado arriba y la pista vacía. Los demás teclean una tarea
  escrita sin letras propias del idioma (`escribir` en `IDIOMAS`).

- **Los widgets se colocan una vez a mano**: los dos de Bobbin, solos en la segunda página del
  lanzador. La escena 05 va a esa página y la fotografía.
- **Pro para la escena 05**: con la clave de RevenueCat en la build, `Billing.refresh()` devuelve `pro`
  a `false` en segundos y el widget del mes sale bloqueado. Se pone `isPro: true` en `widget.json` con
  la app parada y se fuerza el redibujado del widget (en Android, cambiando la densidad y volviendo).
  La escena 06 se saca siempre sin Pro.
- **La guía y la pista de Hoy**: el script escribe `guideSeen` y `hintSeen` en las preferencias, como
  `pro`, porque el diario de demostración no es un primer arranque.
- **en-US se saca con la app en en-GB**: el Gboard del emulador no levanta el teclado con el subtipo
  en-US, y la app solo lee el idioma, así que los textos son los mismos. Si el emulador cree tener un
  teclado físico, Gboard solo enseña su barrita flotante:
  `adb shell settings put secure show_ime_with_hard_keyboard 1` en ese emulador de pruebas.
- **Con varios emuladores abiertos**, `ANDROID_SERIAL=emulator-5556 python3 tools/store/crudas.py ...`.
  La escena 05 sale del lanzador en el que se colocaron los widgets: en otro emulador se conserva la
  cruda anterior.
- En el emulador el teclado a veces no sube en un arranque en frío; el script vuelve a arrancar la app
  hasta que sube. Tocar el campo no sirve: deja el asa del texto a la vista, y el asa se queda encima
  de la escena siguiente.

## 3. Titulares

Viven en la plantilla: es y en en el frame `bobbin` de `apps.js`, los demás idiomas en `textos.js`
(con sonnet, nunca haiku), con las palabras del glosario de `docs/textos.md` y de `Strings.kt`. Seis
palabras como mucho: es lo único que se lee en la tira de la ficha. Fondos a color entero: el verde de
la app y los colores de las portadas.

## 4. El diario de demostración

```bash
python3 tools/demo/generar.py --idioma es-ES [--hoy AAAA-MM-DD]
```

`--hoy` es por defecto el día en que se ejecuta: el diario se construye alrededor de la fecha del
dispositivo, así que no hay que tocar el reloj del emulador. Deja
`tools/demo/salida/<idioma>/journal.json`; `tools/demo/salida/` va en `.gitignore`. Todo es fijo
alrededor de `--hoy`: dos ejecuciones el mismo día dan el mismo diario. Qué lleva:

- **Hoy**: cinco entradas (evento, tarea hecha, tarea con prioridad, la tarea que llegó migrada de ayer
  y una nota). Una más y, con la línea escrita y el teclado arriba, la barra de arriba se sale.
- **Este mes**: los días anteriores a ayer, escritos la mayoría y todos cerrados, para que el widget del
  mes tenga puntos y Hoy no enseñe la línea de días anteriores; cuatro eventos en los días 3, 6, 9 y 12;
  tres tareas del mes.
- **El mes anterior sin cerrar**: cuatro tareas abiertas, la primera la que viene migrada dos veces
  desde hace dos meses.
- **Future Log**: tres entradas en los tres meses siguientes, con `futureSeen` en el mes actual para que
  su línea no tape la del mes sin cerrar.
- **Índice**: dos listas empezadas el mes anterior y un seguimiento de tres filas en este.
- `reminderOffered: true`: la oferta del recordatorio no sale en ninguna escena.

## 5. Meter el diario en la app a mano

`crudas.py` lo hace solo en Android. Con la build de depuración instalada y la app **cerrada**: si
está abierta, al irse al fondo escribe su propio diario encima.

Android:

```bash
adb shell am force-stop com.baltajmn.bullet
adb push tools/demo/salida/es-ES/journal.json /data/local/tmp/journal.json
adb shell run-as com.baltajmn.bullet sh -c 'mkdir -p files && cp /data/local/tmp/journal.json files/ && rm -f files/journal.bak.json'
adb shell cmd locale set-app-locales com.baltajmn.bullet --locales es-ES
```

iOS (cada simulador tiene su contenedor: se repite en el iPhone y en el iPad). La app se compila
firmada en local, **sin** `CODE_SIGNING_ALLOWED=NO`: sin firma el simulador no recibe el App Group,
`widget.json` no se escribe y los widgets de la escena 05 salen vacíos.

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' -derivedDataPath build/ios build
xcrun simctl install booted build/ios/Build/Products/Debug-iphonesimulator/Bobbin.app
xcrun simctl terminate booted com.baltajmn.bullet
D="$(xcrun simctl get_app_container booted com.baltajmn.bullet data)/Library/Application Support"
cp tools/demo/salida/es-ES/journal.json "$D/" && rm -f "$D/journal.bak.json"
xcrun simctl launch booted com.baltajmn.bullet -AppleLanguages "(es)" -AppleLocale es_ES
```

Barra de estado limpia: en Android la pone `crudas.py` con el modo demo de SystemUI; en iOS,

```bash
xcrun simctl status_bar booted override --time 9:41 --batteryState charged --batteryLevel 100 --cellularBars 4 --wifiBars 3
```

Modo claro en todas.

## 6. Gráfico de cabecera de Play e icono de la ficha

`tools/store/cabecera.py` deja `store/play/icon-512.png` y `store/play/feature/<idioma>.png` (1024x500)
para cada idioma de la ficha, que es donde los busca `tienda/play.py` de `BaltaJmn/ci`: "Bobbin" en
Literata, la línea de abajo y una página de Hoy con cuatro entradas y sus bullets. Japonés y coreano
escriben con la serif y la sans del sistema (Hiragino, AppleMyungjo y Apple SD Gothic Neo), porque
Literata no tiene kana ni hangul.

| Idioma | Línea |
|---|---|
| en-US | Rapid logging. Migration by hand. |
| es-ES | Rapid logging. Migración a mano. |
| pt-BR | Rapid logging. Migração à mão. |
| de-DE | Rapid logging. Migration von Hand. |
| fr-FR | Rapid logging. Migration à la main. |
