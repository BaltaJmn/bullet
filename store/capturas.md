# Capturas y gráficos de las fichas

Seis escenas en los cinco idiomas de la ficha (en-US, es-ES, pt-BR, de-DE, fr-FR). Se sacan con un
diario de demostración generado, nunca con uno real: un diario de verdad en una ficha pública es justo
lo que esta app promete no hacer. Todo lo que se ve existe en la app; la portada y el papel Pro de la
escena 06 son la vista previa de Ajustes, no un montaje aparte.

Herramientas (`docs/tecnico.md` 3):

- `tools/demo/generar.py`: escribe el diario de demostración.
- `tools/store/crudas.py`: recorre las escenas en el emulador y deja las capturas crudas.
- `tools/store/capturas.py`: el marco de MoodTraker y line, con las seis escenas de aquí.
- `tools/store/cabecera.py`: gráfico de cabecera de Play e icono de 512.

```bash
python3 tools/demo/generar.py --idioma es-ES
python3 tools/store/crudas.py es-ES
python3 tools/store/capturas.py tools/demo/salida/es-ES/crudas es-ES play
```

---

## 1. Tamaños

| Destino | Dispositivo | Captura cruda | Imagen final |
|---|---|---|---|
| `play` | Emulador Pixel 8, API 35 | 1080x2400 | 1200x2100 PNG |
| `iphone` | Simulador iPhone 17 Pro Max | 1320x2868 | 1320x2868 PNG (6,9") |
| `ipad` | Simulador iPad Pro 13" | 2064x2752 | 2064x2752 PNG (13") |

Play rechaza una captura cuyo lado largo pase del doble del corto, y por eso la de Play va en un marco
de 1200x2100. Al recortar la cruda se quitan 90 px arriba (barra de estado) y 38 abajo (barra de
gestos): las etiquetas de las pestañas acaban en la fila 2361. Las de Apple conservan el tamaño del
dispositivo; el marco es el mismo, escalado. `crudas.py` solo sabe de Android: las de iOS se sacan a
mano con las mismas escenas y el mismo diario (sección 5).

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
sirve igual en los cinco idiomas. Lo que no hace solo:

- **Los widgets se colocan una vez a mano**: los dos de Bobbin, solos en la segunda página del
  lanzador. La escena 05 va a esa página y la fotografía.
- **Pro para la escena 05**: el script escribe `pro` en las preferencias de la build de depuración con
  `run-as`, abre la app para que reescriba `widget.json` y lo quita al terminar. La escena 06 se saca
  antes, siempre sin Pro. Con la clave de RevenueCat a `null`, nada corrige esa caché.
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

Dos líneas por escena, con las palabras del glosario de `docs/textos.md`. Es lo único que se lee en la
tira de la ficha.

| Escena | en-US | es-ES | pt-BR | de-DE | fr-FR |
|---|---|---|---|---|---|
| 01 | Rapid logging: / one line, one bullet | Rapid logging: / una línea, un bullet | Rapid logging: / uma linha, um bullet | Rapid logging: / eine Zeile, ein Bullet | Rapid logging : / une ligne, un bullet |
| 02 | The month on one page: / calendar and tasks | El mes en una página: / calendario y tareas | O mês em uma página: / calendário e tarefas | Der Monat auf einer Seite: / Kalender und Aufgaben | Le mois sur une page : / calendrier et tâches |
| 03 | You migrate by hand, / one task at a time | Migras a mano, / tarea por tarea | Você migra à mão, / tarefa por tarefa | Du migrierst von Hand, / Aufgabe für Aufgabe | Tu migres à la main, / tâche par tâche |
| 04 | An Index, lists / and trackers | Índice, listas / y seguimientos | Índice, listas / e trackers | Index, Listen / und Tracker | Index, listes / et suivis |
| 05 | On your home screen, / never your words | En tu pantalla de inicio, / sin tus palabras | Na sua tela inicial, / sem as suas palavras | Auf dem Startbildschirm, / ohne deine Worte | Sur l'écran d'accueil, / jamais tes mots |
| 06 | Your notebook: / cover and paper | Tu cuaderno: / portada y papel | Seu caderno: / capa e papel | Dein Notizbuch: / Umschlag und Papier | Ton carnet : / couverture et papier |

Fondos, uno por escena, los seis primeros pasteles de la paleta aclarados como en MoodTraker:
`#F9ECEF`, `#FBF0E6`, `#F8F4E2`, `#E9F1E5`, `#E4F0EC`, `#E6EDF7`.

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

`tools/store/cabecera.py` deja `store/play/icon-512.png` y `store/play/feature-1024x500.png` (en-US,
el idioma por defecto de la ficha), que es donde los busca `~/keys/play.sh ficha`, y
`store/feature/<idioma>.png` para los otros cuatro (1024x500): "Bobbin" en Literata, la línea de
abajo y una página de Hoy con cuatro entradas y sus bullets.

| Idioma | Línea |
|---|---|
| en-US | Rapid logging. Migration by hand. |
| es-ES | Rapid logging. Migración a mano. |
| pt-BR | Rapid logging. Migração à mão. |
| de-DE | Rapid logging. Migration von Hand. |
| fr-FR | Rapid logging. Migration à la main. |
