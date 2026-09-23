# Bobbin: Bullet Journal, spec de producto

App Compose Multiplatform (Android + iOS, teléfono y tableta) que traduce a digital el método Bullet
Journal de Ryder Carroll sin traicionarlo: rapid logging con tres bullets, cinco estados de tarea,
tres signifiers, Daily Log, Monthly Log, Future Log, Índice, colecciones y, sobre todo, la migración
hecha a mano, tarea a tarea.

Cuarta de la familia. Hermana de **Purl** (`../line`, `com.baltajmn.line`), **Quilt**
(`../HabitTracker`, `com.baltajmn.habit`) y **MoodTraker** (`../MoodTraker`, `com.baltajmn.mood`):
misma arquitectura, misma paleta, misma promesa (sin cuenta, sin suscripción, tus datos son tuyos).
Nombre de producto **Bobbin**; nombre de tienda **Bobbin: Bullet Journal**. Identificador en las dos
tiendas: `com.baltajmn.bullet`. El repositorio y el código siguen llamándose `bullet`, igual que Purl
vive en `com.baltajmn.line`.

## Documentos

Este SPEC es el porqué del producto: qué hace la app, qué no hace y por qué. Lo que se programa está
escrito aparte y aquí solo se resume lo imprescindible para entender cada decisión.

| Dónde | Qué contiene |
|---|---|
| `docs/tecnico.md` | El contrato de implementación: identificadores, versiones, árbol de código, formatos de `journal.json` y `widget.json`, algoritmos, puentes de plataforma, CI, tests numerados y qué secciones gobiernan cada issue |
| `docs/pantallas.md` | La interfaz pantalla a pantalla, con sus estados vacíos y los cuatro gestos de la app |
| `docs/textos.md` | Todos los textos en los cinco idiomas y el glosario fijo del método |
| `store/` | La tienda: fichas en cinco idiomas, novedades, capturas, formularios, política de privacidad, compras, CI y el checklist de lanzamiento |
| `CLAUDE.md` | Las reglas de trabajo que cargan solas las sesiones de Claude Code |
| Issues del repo | `gh issue list -R BaltaJmn/bullet`: #1 a #72, una por pieza, en v1.0, v1.1 y v1.2 (§3, §11) |

Todo lo que dicen estos documentos está decidido. **Cuando una issue y el código no coinciden, manda
este SPEC**; si un detalle de implementación de aquí no coincide con `docs/tecnico.md`, manda
`docs/tecnico.md` y el SPEC se corrige en el mismo cambio.

---

## 1. Benchmark: qué copiar y dónde atacar

| App | Modelo | Lo que hace bien | Lo que le duele |
|---|---|---|---|
| **Bullet Journal Companion** (oficial, Ryder Carroll) | De pago | La marca del método y de su autor | Reseñas de 2026 la llaman *abandonware*: sin actualizar en años, se bloquea al elegir fechas y el recordatorio de reflexión desaparece si se abre el teléfono por otro motivo. No guarda texto ni fotos de página: es un folleto sobre el método, no el método. Está en Productividad y pierde puestos por ello (§8) |
| **May: Beautiful Bullet Journal** (Play) | Freemium | Estética de cuaderno | Cierres y expulsiones frecuentes de la app |
| **BujoFlow** | 1,99 $/mes, 14,99 $/año o 29,99 $ de compra única | Ofrece compra única | La compra única más cara del nicho |
| **Bullet: Journal, Daily Planner** | De 3,99 $ a 99,99 $ según plan | Ocupa la keyword | Precios que no se entienden |
| **Zinnia, DailyBean, Buju, Digital Planner** | Freemium | Dominan la búsqueda de "bullet journal" (§8) | Son planners y diarios con estética bujo, no el método: sin migración de verdad |
| **Notion** | Suscripción | Flexible, sirve de bujo con plantillas | Bloquea el workspace al no renovar, "la peor experiencia de facturación", lenta en bases grandes y con onboarding eterno |
| **TickTick** | Gratis limitado; 3,99 $/mes o 35,99 $/año | Captura rápida que reconoce fechas en el texto | Plan gratis "casi sin sentido"; fallos de sincronización sin indicación de qué pasó |
| **Todoist** | Pro 5 $/mes | Gestor de tareas maduro | Tareas sin eventos ni notas: no es un log. Caro frente a TickTick |
| **Things 3** | 9,99 $ por plataforma, compra única | "No es una suscripción" es su argumento más citado; premio Apple Design | Solo Apple, y es un gestor de tareas, no un diario |
| **Obsidian** | Gratis, sincronización de pago | Ficheros locales | Editar offline en dos dispositivos deja duplicados y *Conflicted Copy*; hay quien reconstruyó la bóveda entera |
| **Workflowy** | Freemium | Outliner con búsqueda, enlaces y etiquetas; una comparativa lo llama "el único que merece llamarse app de Bullet Journal" | Sin glifos ni estados del método |
| **Structured** | Freemium, Pro de por vida | Línea de tiempo del día y widgets como panel (4,8 con 155.000 valoraciones) | Planificador de horas: lo contrario de un Daily Log sin franjas |
| **Daylio** | Gratis con anuncios; 35,99 $/año o 59,99 $ de por vida | Registro de un toque, "sostenible para gente con TDAH o depresión" | Anuncios en el plan gratis; limitaciones de exportación |
| **Day One** | Suscripción, Silver 49,99 $/año | El diario de referencia | Precio, y la propiedad de la empresa genera dudas de confianza a largo plazo |
| **Journey** | 29,99 $/año | Multiplataforma | Sincroniza por Google Drive o Dropbox sin cifrado de extremo a extremo real |
| **Diarium** | Gratis o 7,99 $ de por vida | Una de las pocas compras únicas baratas | Muy básico |
| **Diaro** | Freemium | Diario sencillo | Sin texto enriquecido: el contenido no sale con su estructura |
| **Grid Diary** | Gratis a 2,99 $/mes | Preguntas reflexivas opcionales que desbloquean la escritura | Suscripción |
| **Reflectly** | Hasta 59,99 $/año | Interfaz cuidada | La más cara del grupo de diarios, sentida como restrictiva |

### El cuaderno de papel

El Bullet Journal no es una plantilla de productividad: es un mecanismo de intencionalidad. La
fricción del papel (reescribir a mano para migrar, no tener autocompletado ni avisos) no es un defecto
que la app deba arreglar. **Es el mecanismo activo**: obliga a decidir, entrada por entrada, qué
merece seguir ocupando espacio. Carroll lo dice así: si una tarea no vale el esfuerzo de copiarla otra
vez, probablemente no era tan importante. El método nació además como herramienta de accesibilidad
para el TDAH, con materiales baratos y de baja fricción; la estética de redes y las apps sobrecargadas
socavan ese propósito.

- Lo que el papel hace mejor: el objeto, la mano, y la pausa obligada de reescribir. La pausa sí se
  conserva (§2.10); el objeto y la mano no se fingen.
- Lo que le duele y una app arregla: no hay búsqueda, no hay copia (si se moja, se acabó), hay que
  reescribir el texto entero al migrar, las colecciones se parten entre páginas (el threading existe
  solo por eso) y el índice se lleva a mano.

**La regla que sale de aquí gobierna toda la app: se quita la fricción de escribir, nunca la de
decidir.** Copiar el texto al migrar es legítimo; migrar sin que el usuario decida, no.

### Qué se copia

- **Captura de un gesto** (Daylio, y el propio papel): escribir en la primera pantalla, sin menús
  previos (#21).
- **Compra única de verdad** (Things 3): sin letra pequeña (§7).
- **El widget como panel** (Structured): abiertas, hechas y eventos de hoy (#41, #42).
- **Búsqueda y etiquetas ligeras** (Workflowy): la búsqueda recorre todo el diario, y una palabra con
  `#` es una etiqueta sin gestor aparte (#35).
- **Preguntas opcionales para el campo en blanco** (Grid Diary), en v1.1 y nunca obligatorias (#65).
- **Fechas en lenguaje natural** (TickTick), en local y sin aplicarse solas (#66).
- **Privacidad primero** (Hello Diary): nada sale del teléfono salvo la compra (§4).

### Fallos del mercado y qué issue ataca cada uno

1. **Pérdida de datos y cuentas secuestradas** (Notion, Companion, May). Almacén atómico que guarda
   tras cada cambio, con `.bak` y cuarentena (#14); migración de esquema que nunca pierde un fichero
   (#15); el modelo nunca borra al migrar (#13); borrar el diario no borra la compra (#33); copia
   automática del sistema (#46); el derecho Pro se guarda en local y nunca bloquea contenido (#47);
   tests de almacén y fusión (#54).
2. **Sincronización que duplica o pierde** (Obsidian, TickTick). Importar fusionando por id con
   resumen previo y copia antes de tocar nada (#45); sincronización de un solo fichero, última
   escritura gana solo si cambió un lado, `.bak` previo y elección explícita en conflicto, nunca
   fusión silenciosa (#69).
3. **Suscripción obligatoria y facturación agresiva** (TickTick, Notion, Reflectly). Compra única y
   reparto gratis/Pro generoso (#2); paywall solo al chocar con un límite (#48); "compra única, sin
   cuenta, sin anuncios" en la primera frase de la ficha (#57).
4. **Datos atrapados** (Daylio, Diaro). Exportar a JSON y a Markdown legible con los símbolos del
   método, gratis desde el primer día (#44); importar (#45); libro en PDF (#68).
5. **Apps que traicionan el método** (Companion, apps sobrecargadas). Modelo de migración sin traspaso
   masivo (#13); rapid logging real (#20, #21); cinco estados con glifos fieles (#22); Monthly Log en
   lista, no rejilla (#24); Future Log sin push ni movimiento automático (#25, #26); revisión tarea a
   tarea (#27); reflexión sin métricas (#28); Índice (#29); colecciones libres (#30); threading (#63);
   migración anual (#64).
6. **Cuenta obligatoria o privacidad de terceros** (Journey, Day One). Cero cuenta y cero servidor (#1,
   #3); bloqueo biométrico gratis (#39); `widget.json` sin texto del diario (#40); formularios que
   declaran que nada sale salvo RevenueCat (#59); la sincronización va solo a la nube del propio
   usuario (#69).
7. **Lentitud y onboarding eterno** (Notion). Campo enfocado al arrancar (#21); sin tutorial, clave de
   símbolos opcional (#31); nada obligatorio en Ajustes (#32); rendimiento medido con miles de
   entradas (#55).
8. **Publicidad en el plan gratis** (Daylio). Sin anuncios en ningún plan (#2), declarado en los
   formularios (#59).

Y los dos fallos concretos de la app oficial: el **recordatorio que desaparece**, atacado por #36, #37
y #38 (se reprograma al arrancar, al reiniciar y al cambiar de hora, y dice qué pasa si falta el
permiso); y el **bloqueo al elegir fechas**, atacado por #22 y #25 (programar elige el mes de una
lista, nunca de un selector de calendario).

El hueco de ASO (Zinnia gana por valoraciones, Buju por llevar la keyword, la oficial pierde por
categoría) lo atacan #1 y #57 ("Bullet Journal" en el nombre de tienda y categoría Estilo de vida),
#56 (pedir valoración una sola vez, tras cerrar el primer mes) y #58 (capturas en cinco idiomas).

### Nuestro ataque, en una línea cada uno

1. **El método entero, gratis.** Lo que la app oficial describe, Bobbin lo hace.
2. **La migración es tuya.** Nada se mueve solo: ni ayer a hoy, ni un mes al siguiente, ni el Future
   Log al mes que llega.
3. **Compra única, sin cuenta, sin anuncios.** En una categoría donde un año de suscripción cuesta más
   que toda la app.
4. **Android e iOS con el mismo cuidado.** La oficial y buena parte del nicho no están en las dos.
5. **Escribir cuesta cero toques extra**: la app abre en Hoy con el teclado arriba.
6. **Tus datos salen cuando quieras**, en JSON y en Markdown con los símbolos del método, gratis.

---

## 2. El método, regla a regla: rapid logging, tres bullets, cinco estados, tres signifiers, Daily Log, Monthly Log, Future Log, Índice, colecciones, migración, threading y reflexión

Cada regla con lo que hace en papel, cómo la traduce Bobbin, lo que no se hace aunque parezca una
mejora, y las issues que la implementan. Fuente primaria: bulletjournal.com (ver Fuentes). Los
símbolos del método no se traducen en ningún idioma (`docs/textos.md`).

### 2.1 Rapid logging

- **En papel:** notación breve, fragmentada, escrita en el momento en que llega el pensamiento, la
  tarea o el evento. Es el idioma en el que se escribe todo lo demás, y cualquier página se escanea
  en segundos porque el símbolo dice qué es cada línea antes de leerla.
- **En Bobbin:** un campo de una línea, sin editor de texto rico, que deduce el tipo del propio texto
  por su prefijo (`RapidParse`, #20). Intro guarda y deja el foco puesto para la siguiente línea, sin
  soltar el teclado (#21).
- **No se hace:** campos obligatorios de título, descripción, categoría u hora; párrafos largos. Los
  dos reintroducen la fricción de formulario que el rapid logging existe para evitar.
- **Issues:** #20, #21.

### 2.2 Tres bullets

- **En papel:** tarea, un punto; evento, un círculo; nota, un guion. Todo vive junto en la misma
  lista, distinguido solo por el símbolo.
- **En Bobbin:** un único modelo `Entry` con `bullet` entre `TASK`, `EVENT` y `NOTE` (#11). Sin
  prefijo, es tarea; `- ` al principio da nota y `o ` da evento, y el prefijo se quita del texto
  guardado (#20). Para quien prefiere tocar, un selector opcional de punto, círculo y guion junto al
  campo (#21).
- **No se hace:** tres pantallas o tres modelos (tareas, calendario, notas) pegados con cinta. El log
  unificado es la esencia.
- **Issues:** #11, #20, #21.

### 2.3 Cinco estados

- **En papel:** solo las tareas cambian de estado. Abierta (punto), hecha (X sobre el punto), migrada
  (`>`, se reescribió en otro sitio), programada (`<`, se aplazó al Future Log) e irrelevante (la
  entrada tachada: se descarta, no se migra).
- **En Bobbin:** `TaskStatus` con `OPEN`, `DONE`, `MIGRATED`, `SCHEDULED` e `IRRELEVANT`, válido solo
  para tareas (#11). Tocar el glifo alterna abierta y hecha; la pulsación larga abre la hoja con el
  resto (#22). Migradas y programadas se ven atenuadas, con su glifo y un enlace a donde fue la copia.
- **No se hace:** iconos reinventados; casillas de verificación; un "archivado" que no sea ninguno de
  los cinco.
- **Issues:** #11, #22.

### 2.4 Tres signifiers

- **En papel:** marcas opcionales delante del bullet, solo cuando hacen falta: prioridad (`*`),
  inspiración (`!`) y explorar (un ojo). Carroll insiste en introducirlos solo cuando se echan en
  falta.
- **En Bobbin:** un conjunto opcional de `PRIORITY`, `INSPIRATION` y `EXPLORE` sobre la entrada (#11).
  Se escriben con los prefijos `* `, `! ` y `? ` en cualquier orden con el del bullet (`* - texto` es
  una nota con prioridad, #20), o se ponen después desde la hoja de la pulsación larga (#22). El ojo
  de explorar se dibuja; se teclea como `?` porque no hay tecla de ojo.
- **No se hace:** etiquetas libres e ilimitadas con gestor propio, que devuelven la complejidad que el
  método evita (la búsqueda ya trata `#palabra` como etiqueta, #35); signifiers ofrecidos durante la
  creación como paso obligatorio.
- **Issues:** #11, #20, #22.

### 2.5 Daily Log

- **En papel:** la página del día, tan larga como haga falta, que se va llenando según ocurren las
  cosas, sin planificación previa ni franjas horarias.
- **En Bobbin:** la pantalla **Hoy**, que es donde arranca la app siempre (#19): una lista abierta que
  mezcla los tres bullets en orden de creación, con el campo enfocado y el teclado arriba desde el
  primer fotograma (#21). El día nace vacío. Deslizar en horizontal o tocar la fecha cambia de día, y
  los días pasados se editan igual que hoy.
- **No se hace:** rejilla de horas; copiar a hoy las tareas abiertas de ayer. En su lugar, una línea
  discreta, "Ayer quedaron N abiertas", que lleva a Revisar.
- **Issues:** #19, #21.

### 2.6 Monthly Log

- **En papel:** doble página. A la izquierda, el calendario: una línea por día con lo que tiene fecha
  fija. A la derecha, las tareas del mes sin día.
- **En Bobbin:** la pantalla **Mes** (#24): una línea por día con su número y la inicial del día de la
  semana, y debajo la lista de tareas del mes sin día. Tocar la línea de un día escribe en ese día con
  el mismo campo que Hoy. El primer día de la semana sigue al sistema. El mes nace vacío.
- **No se hace:** rejilla de calendario tipo Google Calendar; copiar o sugerir nada del mes anterior.
- **Issues:** #24.

### 2.7 Future Log

- **En papel:** los meses siguientes en bloques, donde se aparca lo que tiene fecha lejana
  (cumpleaños, viajes, plazos) sin crear páginas vacías por adelantado.
- **En Bobbin:** la pantalla **Futuro** (#25): seis bloques de mes desde el siguiente al actual, hasta
  veinticuatro con "ver más". Cada entrada puede llevar un día, que se escribe como número, nunca en un
  selector de calendario. Programar una tarea desde cualquier sitio elige el mes de una lista. Al
  abrir el mes que llega, una línea pasiva, "N entradas del Future Log esperan", abre una revisión
  entrada por entrada con tres acciones: pasar al calendario del mes, dejarla o descartarla (#26).
- **No se hace:** notificaciones de fechas futuras; mover entradas solas al llegar el mes. Eso
  convierte un registro que se consulta en una app de recordatorios, y se salta la migración.
- **Por qué en v1.0:** el estado *programada* necesita un destino. Sin Future Log, `<` no significa
  nada.
- **Issues:** #25, #26.

### 2.8 Índice

- **En papel:** las primeras páginas del cuaderno, con el título de cada Monthly Log y de cada
  colección y su página, en orden de creación. Los Daily Log no se indexan.
- **En Bobbin:** la pantalla **Índice** (#29): meses con contenido y colecciones, por orden de
  creación, nunca alfabético. Un filtro por título, sin distinguir mayúsculas ni acentos. Las
  colecciones archivadas, al final y plegadas. Renombrar no cambia el orden.
- **No se hace:** números de página inventados, que son teatro sin función; sustituir el índice por un
  buscador, que quita la vista de conjunto.
- **Issues:** #29.

### 2.9 Colecciones

- **En papel:** cualquier lista temática con título propio (lecturas, regalos, un proyecto), en
  cualquier parte del cuaderno, localizada por el índice. El sistema no dicta qué colecciones tener.
- **En Bobbin:** una colección es un título y una lista de bullets, creada desde el Índice pidiendo
  solo el título (#30). Se escribe en ella con el mismo campo y la misma lista que Hoy, y es destino
  válido de una migración. El seguimiento (`kind TRACKER`, #50) es la única colección con forma propia:
  filas que define el usuario por días del mes, con una marca de un toque, porque un seguimiento no se
  escribe como lista.
- **No se hace:** catálogo de plantillas cerradas (gastos, lecturas, hábitos). El bujo es un sistema
  para construirte tu sistema.
- **Issues:** #30, #50.

### 2.10 Migración

El núcleo crítico. Sin esto no hay método, y automatizarlo sería peor que no tenerlo.

- **En papel:** al cerrar el mes (y el día) se repasa cada tarea abierta: se marca hecha, se tacha
  por irrelevante o se migra, y migrar obliga a reescribirla a mano en el mes nuevo o en una
  colección. Al cambiar de cuaderno, lo mismo a escala de año.
- **En Bobbin:** la pantalla **Revisar** (#27) enseña **una tarea cada vez** con cinco acciones:
  marcar hecha, migrar al mes nuevo (o a un día), programar al Future Log, mover a una colección o
  descartar. Migrar copia el texto: se quita la fricción de reescribir, no la de decidir. Desde la
  segunda migración de la misma tarea, la pantalla dice "migrada N veces", para empujar a una decisión
  distinta de migrar otra vez. La revisión se deja a medias y se retoma; la de un día suelto usa el
  mismo flujo. Al cambiar de mes con tareas abiertas, Hoy y Mes enseñan una línea ("Febrero sin
  cerrar: N abiertas"), nunca un modal. En el modelo (#13), migrar marca el original `MIGRATED` y crea
  una copia `OPEN` en el destino con `from` apuntando al original; programar hace lo mismo con
  `SCHEDULED` hacia el Future Log; descartar marca `IRRELEVANT` sin copia. Ninguna toca el texto del
  original.
- **No se hace:** `migrateAll`, un botón de "pasar todas", ni ninguna función que acepte una lista de
  tareas. Sin la pausa de decidir, las tareas se acumulan en un backlog infinito, que es justo lo que
  el bujo existe para evitar.
- **En v1.1:** la migración anual como flujo propio, **Nuevo cuaderno**, lanzable cuando el usuario
  quiera: cada colección (mantener, continuar con threading o archivar) y después cada tarea abierta,
  con las mismas cinco acciones y sin ninguna acción masiva (#64).
- **Issues:** #13, #27, #64.

### 2.11 Threading

- **En papel:** "continúa en la página X" al pie y "viene de la página Y" en la continuación, para una
  colección que crece más allá de su hueco, o entre cuadernos.
- **En Bobbin:** una colección digital no se parte en páginas, así que el problema físico no existe.
  Lo que sí se traduce es el enlace entre colecciones a lo largo del tiempo ("Lecturas 2025" sigue en
  "Lecturas 2026"): `threadFrom` en la colección, "Continuar en una colección nueva", y la cabecera
  con "viene de" y "sigue en", navegables. El Índice agrupa las del mismo hilo (#63). El campo existe
  en el formato desde v1.0 para no subir el esquema en v1.1.
- **No se hace:** páginas virtuales que imitan el papel; crear páginas o entradas por el hecho de
  enlazar.
- **Issues:** #63 (v1.1).

### 2.12 Reflexión

- **En papel:** detenerse a releer lo escrito (al final del día, al final del mes) antes de decidir
  qué migrar. Carroll lo trata como un paso propio, no como efecto secundario de migrar.
- **En Bobbin:** el primer paso de Revisar, antes de tocar ninguna tarea: el periodo entero (el día o
  el mes) en modo lectura, con un campo de nota opcional que se guarda como nota en el Monthly Log del
  mes revisado. "Saltar" pasa a la migración sin crear nada (#28). En v1.1, una pregunta opcional al
  azar junto al campo, para quien se queda en blanco (#65). El recordatorio de reflexión (§6) lleva
  aquí.
- **No se hace:** métricas, porcentajes ni gráficas ("12 tareas completadas esta semana"). Desplazan
  el foco de pensar sobre el contenido a mirar un número, que es lo contrario del propósito.
- **Issues:** #28, #65.

### La clave

La *key page* del cuaderno: una pantalla de lectura con los glifos de los tres bullets, los cinco
estados y los tres signifiers, abierta desde el icono de interrogación de Hoy (#31). Se cierra sin
marcar nada como visto. Es la única ayuda de la app: no hay tutorial (§6).

---

## 3. Funcionalidad por versión: v1.0, v1.1, v1.2 y lo descartado a propósito

El corte no es por pantalla sino por si su ausencia, o su automatización, deja de ser un Bullet
Journal. **v1.0 son las issues #1 a #62, v1.1 de la #63 a la #68 y v1.2 de la #69 a la #72.**

### v1.0: issues #1 a #62

- **Decisiones y documentos** (#1 a #6): nombre e identificadores, precio, este SPEC,
  `docs/tecnico.md`, `docs/pantallas.md` y `docs/textos.md`.
- **Cuentas, andamiaje, CI y prueba cerrada** (#7 a #10): altas en las consolas y RevenueCat, el
  esqueleto KMP copiado de line, los cuatro workflows y la pista cerrada de Play arrancada cuanto
  antes.
- **Modelo y almacén** (#11 a #15): `Entry`, `Place`, `Journal`, `DayClock`, la migración en el modelo
  y `journal.json` atómico con su migración de esquema.
- **Textos, tema e icono** (#16 a #18): cinco idiomas, papel y tinta, glifos dibujados, la bobina.
- **El método** (#19 a #31): navegación de cuatro destinos, captura rápida con prefijos, Hoy con el
  teclado arriba, estados y signifiers, editar y borrar con deshacer, Mes, Future Log y su aviso,
  revisión y migración tarea a tarea, reflexión, Índice, colecciones libres y clave de símbolos.
- **Ajustes** (#32 a #34): todo con valor por defecto, borrar todos los datos, tableta y pantallas
  grandes.
- **Búsqueda** (#35): todo el diario, con filtros por estado y signifier y `#etiquetas`.
- **Recordatorio de reflexión** (#36 a #38): Android que no se pierde, permiso denegado, iOS.
- **Bloqueo** (#39): biometría del sistema y multitarea oculta, gratis.
- **Widgets de hoy** (#40 a #42): `widget.json` sin texto, Glance y WidgetKit.
- **Compartir y copia** (#43 a #46): página punteada, zip con JSON y Markdown, importar fusionando,
  copia automática del sistema.
- **Pro** (#47 a #52): RevenueCat, paywall al chocar, portadas y papeles, seguimientos, widget del mes
  y widget de pantalla de bloqueo.
- **Calidad** (#53 a #56): accesibilidad, la lista de tests numerados, rendimiento con miles de
  entradas y la petición de valoración.
- **Tienda y salida** (#57 a #62): ficha, capturas, privacidad y formularios, beta, prueba en
  dispositivo físico y lanzamiento.

Y lo que atraviesa todo: cinco idiomas (inglés, español, portugués, alemán y francés) sin selector,
siguiendo al sistema; teléfono y tableta, vertical y horizontal; tema claro u oscuro según el sistema.

### v1.1: issues #63 a #68, lo que se pedirá en las reseñas

- **Threading** (#63): enlazar una colección con su continuación. Gratis.
- **Nuevo cuaderno** (#64): la migración anual, colección a colección y tarea a tarea. Gratis.
- **Reflexión guiada** (#65): un banco de preguntas opcionales en los cinco idiomas. Gratis.
- **Fechas en lenguaje natural** (#66): "mañana", "el viernes", "14 de marzo" en los cinco idiomas, en
  local, como una pastilla que nunca se aplica sola. Gratis.
- **Captura desde fuera** (#67, Pro): baldosa de Ajustes rápidos en Android y App Intent de iOS para
  Siri y Atajos, pasando por el mismo `RapidParse`.
- **Libro del cuaderno en PDF** (#68, Pro): clave, índice, meses y colecciones con los glifos y el
  papel elegidos. El día que sale, el precio sube a 9,99 EUR (§7).

### v1.2: issues #69 a #72, retención profunda

- **Sincronización** (#69, Pro): un único `bobbin-sync.json` en iCloud Drive o en una carpeta elegida
  con el selector del sistema, sin cuenta en la app y sin fusión silenciosa.
- **Resumen del año en texto** (#70, Pro): inspiraciones, prioridades cumplidas y las tareas más
  migradas. Solo texto.
- **Variaciones del índice** (#71): iconos, temas y subrayado de lo ya migrado, del método original.
  Gratis.
- **Importar de las apps hermanas** (#72): notas de Purl y MoodTraker y hábitos de Quilt, elegidas
  una a una. Gratis.

### Descartado a propósito

| Qué | Por qué |
|---|---|
| Migrar todas las tareas de golpe, o arrastrarlas solas al día o al mes siguiente | Elimina la decisión, que es el método. Es el error que el propio encargo señala como el más grave |
| Rejilla de calendario en el Monthly Log o selector de calendario para elegir fechas | El Monthly Log es textual; el selector de fechas es justo donde se bloquea la app oficial |
| Notificaciones del Future Log, de rachas o de hitos | Una notificación al día, la de reflexión, y ninguna más (§6) |
| Plantillas de colección | Cierran el sistema que el método deja abierto. El seguimiento es la única forma propia, porque no cabe en una lista |
| Etiquetas con gestor propio | Devuelve la complejidad organizativa que el método evita. `#palabra` en el texto basta (#35) |
| Rachas, puntos, insignias, gráficas de productividad | Un bujo no se mide en tareas hechas; la reflexión es sin números (§2.12) |
| Editor de texto rico, párrafos, entradas de varias líneas | Rapid logging es una línea. Para prosa larga está Purl, o una nota en una colección |
| Franjas horarias en el Daily Log | El día es una lista abierta, no una agenda (Structured ya existe) |
| Números de página en el Índice | No hay páginas: sería imitación vacía del papel |
| Fotos en las entradas | El método es texto. Una foto pesa en la copia, en el zip y en el widget, y no mejora ninguna regla |
| Cuenta, servidor o sincronización propia | Rompe la promesa de la familia y añade un coste recurrente que obligaría a suscripción (§7) |
| IA generativa sobre el contenido | Coste recurrente, red, y decide por el usuario lo que el método le pide decidir a él |
| Onboarding o tutorial | Cero toques antes de escribir. La clave está a un toque para quien la quiera (§2) |
| Selector de idioma dentro de la app | Las hermanas no lo tienen, y Android 13+ e iOS ya dan idioma por app |
| `FLAG_SECURE` en Android | Bloquea también las capturas que el propio usuario quiere hacer. `setRecentsScreenshotEnabled(false)` oculta la multitarea sin prohibir nada |
| Seguimiento de hábitos con estadísticas y recordatorios | Es Quilt. Aquí, un seguimiento de papel, y la ficha remite a la hermana (#50) |

### Explícitamente fuera de alcance

Cuentas de usuario, nada social, IA generativa, audio o vídeo, anuncios, analítica e informes de
fallos. Cada uno añade coste recurrente, permiso invasivo o red, y ninguno hace el método más fiel.

---

## 4. Decisiones que se toman aquí, no en el código

Son las preguntas que aparecen en la primera hora de programar y que, sin escribir, se descubren en
forma de fallo tres meses después.

### Tres reglas que no se negocian issue a issue

1. **La migración es una decisión por tarea, y no existe `migrateAll` en el modelo.** Ninguna función
   pública de `model/Migration.kt` acepta una lista, ningún botón mueve más de una tarea, y nada se
   mueve solo: ni ayer a hoy, ni un mes al siguiente, ni el Future Log al mes que llega, ni una
   importación de las hermanas al diario (#13, #27, #72).
2. **Ninguna pantalla exige configurar nada antes de escribir.** La app abre en Hoy con el campo
   enfocado y el teclado arriba; todo ajuste tiene valor por defecto; los estados vacíos invitan a
   escribir, nunca a abrir Ajustes (#21, #31, #32).
3. **Nada sale del teléfono salvo lo de RevenueCat.** Sin cuenta, sin servidor, sin analítica, sin
   informes de fallos. La única conexión es la de la compra, y la sincronización de v1.2 va a la nube
   del propio usuario (#59, #69).

Una issue que choque con cualquiera de las tres está mal escrita, y se corrige la issue.

### El resto

| Pregunta | Decisión |
|---|---|
| ¿A qué hora empieza el día? | A las **04:00 locales** por defecto: quien apunta a la una de la madrugada sigue en el día que está viviendo. Es un ajuste (#32), no una constante como en Purl, porque aquí el día agrupa tareas y quien trabaja de noche necesita correrlo. Cambiarlo **no mueve ninguna entrada ya escrita**: solo decide qué día es hoy desde ese momento |
| Zona horaria | Las fechas del diario son ISO **locales**, nunca UTC: cambiar de huso no reescribe nada. `createdAt` y `updatedAt` son instantes, porque ordenan y deciden la fusión |
| Primer día de la semana | El del sistema por defecto, ajustable (#24, #32) |
| Qué es una entrada | Una línea. Los saltos de línea se convierten en espacio. Tope de **500 puntos de código**, que se aplica **al capturar y al editar, nunca al leer ni al importar**: si el tope viviera en la carga, importar una copia truncaría el diario en silencio. El corte nunca parte una pareja suplente: media pareja no es texto válido y el lado Swift la rechaza (la lección de `habitIcon()` en Quilt) |
| Texto vacío | Si tras quitar los prefijos no queda texto, no se crea entrada (#20) |
| Dónde vive el diario | `journal.json` en el almacenamiento privado (`filesDir` en Android, Application Support en iOS), **nunca en el App Group**. Allí solo va `widget.json`, sin texto |
| Cuándo se guarda | **Tras cada cambio**, nunca solo al cerrar: cada acción es discreta (Intro, un toque en el glifo, una decisión de revisión). Escritura a `.tmp` y rename, `.bak` de la anterior, un solo escritor, fuera del hilo principal (#14) |
| Un fichero ilegible | Pasa a cuarentena, no se sobrescribe, y la app carga el `.bak` (#14) |
| Un esquema más nuevo que la app | No se toca: la app avisa de que hace falta actualizar. Un esquema viejo se convierte paso a paso (1 a 2, 2 a 3) con copia `.pre-migration`, y si un paso falla se restaura el original (#15) |
| Migrar, programar, descartar | Migrar marca el original `MIGRATED` y crea una copia `OPEN` en el destino con `from`; programar, igual con `SCHEDULED` hacia el Future Log; descartar marca `IRRELEVANT` sin copia. **Nunca se borra ni se reescribe el texto del original** (#13) |
| Cuántas veces se migró | `migrationCount` recorre la cadena de `from`. Desde la segunda, la revisión lo dice (#27) |
| Estados de eventos y notas | Solo las tareas tienen estado; un evento o una nota es siempre `OPEN` y la hoja no les ofrece estados de tarea (#11, #22) |
| Borrar | **Sin confirmación**, con un Deshacer de cinco segundos (#23). Borrar el original de una migración no borra la copia ni rompe su recuento: el cómo, en `docs/tecnico.md` 6.4 |
| Borrar todos los datos | Desde Ajustes, con un segundo paso explícito. Deja un diario vacío, limpia `.bak`, cuarentena y `widget.json`, y **no toca el derecho Pro** (#33) |
| Dónde viven los ajustes | Todos en `settings` dentro de `journal.json`, **también la portada y el papel**: un solo fichero es la fuente de verdad y los ajustes viajan en la exportación (#32). Donde #49 dice "no dentro de `journal.json`" quiere decir fuera de las entradas, y manda esta fila |
| Importar y los ajustes | La importación trae entradas y colecciones, no las preferencias de otro teléfono: los ajustes de la copia solo se aplican si el diario local está vacío (una reinstalación que se restaura) |
| Estado de compra y "valoración ya pedida" | Fuera del diario, en las preferencias de la plataforma: sobreviven a borrar los datos y a importar, y una copia no regala Pro (#33, #47, #56) |
| Importar una copia | **Fusiona, nunca reemplaza.** Unión por `id`; si un `id` está en los dos lados gana el `updatedAt` más reciente; un estado cerrado (`DONE`, `MIGRATED`, `SCHEDULED`, `IRRELEVANT`) nunca vuelve a `OPEN`. Antes de tocar nada, copia del diario actual y un resumen (N nuevas, M actualizadas); cancelar lo deja todo como estaba (#45) |
| Sincronizar (v1.2) | Un solo fichero. Si cambió un lado, gana ese lado; `.bak` antes de sobrescribir; si cambiaron los dos, se pregunta: este dispositivo, el otro, o fusionar con el resumen de #45. **Nunca fusión silenciosa** (#69) |
| Qué ve un widget | **Números, fechas y booleanos, nunca texto del diario**, y no puede verlo: el diario no está en su contenedor. Los widgets no escriben: tocarlos abre la app (#40) |
| Qué dice el recordatorio | Un texto fijo, igual cada día, que no cita ni una palabra del diario, con bloqueo o sin él (#36, #38) |
| Qué se ve con el bloqueo activo | Nada del diario fuera de la app: el widget y la notificación ya no lo llevan nunca, y la vista de multitarea sale tapada (#39) |
| Seguimiento al cambiar de mes | Nace una página nueva del mismo seguimiento, enlazada a la anterior por id y vacía, nunca copiada con las marcas del mes pasado (#50, `docs/tecnico.md` 6.18) |
| Copia automática del sistema | Android: `journal.json` entra, `widget.json` y la cuarentena no, y a la nube **solo si va cifrada de extremo a extremo**. iOS: Application Support entra en la copia de iCloud del dispositivo (#46, §9) |
| Idioma | Sigue al sistema, sin selector. Los símbolos del método no se traducen |
| Nombre, identificadores y App Group | Fijados antes de la primera línea de código (§8). Irreversibles tras publicar |

---

## 5. Diseño: papel, glifos, tipografía, navegación y accesibilidad

Tinta sobre papel, con la paleta de la familia para que las cuatro se lean como una. Las medidas, los
tokens y cada pantalla están en `docs/pantallas.md`.

### Papel

- Fondo crema `#FBF8F3` en claro y `#17150F` en oscuro. Nunca blanco puro ni negro puro. Claro u
  oscuro lo decide el sistema, sin ajuste propio.
- **Cuatro papeles** detrás de las listas: **punteado** (gratis y de serie, el del cuaderno de bujo),
  rayado, cuadrícula y liso (Pro, #49). El papel es fondo: nunca cambia el tamaño ni la posición de
  una línea, para que las capturas y la accesibilidad no dependan de él.
- **Ocho portadas** con los ocho pasteles de la familia y sus mismos hex: salvia (gratis y de serie),
  rosa, melocotón, mantequilla, menta, cielo, pervinca y lila. La portada tiñe la cabecera y la barra
  inferior, los widgets y, en v1.1, la cubierta del libro. Acento salvia `#6FAE9B`.
- Radios de 18 a 32 dp, bordes de 1 dp en vez de sombras. Sin tarjetas: el papel es el lienzo.
- **Sin rojo en ninguna parte.** Una tarea abierta es una tarea abierta, no un suspenso; migrada
  cinco veces es una pregunta, no una alarma.

### Glifos

Los símbolos son el idioma del método y la app los dibuja con trazo propio en `Canvas`
(`ui/BulletGlyph.kt`, #17), **nunca con emoji ni fuente de iconos**: en iOS varios caracteres escritos
como texto se pintan como emoji de color y rompen la escala de grises, y un glifo de fuente no alinea
igual en las dos plataformas.

| Qué | Glifo |
|---|---|
| Tarea abierta | Punto |
| Tarea hecha | X |
| Tarea migrada | `>` |
| Tarea programada | `<` |
| Tarea irrelevante | Punto y la entrada entera tachada |
| Evento | Círculo |
| Nota | Guion |
| Prioridad, inspiración, explorar | Asterisco, exclamación y ojo, delante del bullet |

Las migradas y programadas se pintan atenuadas, con un enlace a donde fue la copia (#22). El glifo no
cambia de tamaño con la escala de fuente del sistema; el texto sí.

### Tipografía

**Una sola tipografía para toda la app**: **Literata** (licencia OFL, pensada para leer en pantalla),
empaquetada para que el texto y los glifos se vean igual en las dos plataformas, la misma que usa Purl
para el texto del usuario. Jerarquía por tamaño y peso, no por familias. Los widgets usan la fuente
del sistema.

### Navegación

- **Cuatro destinos fijos en la barra inferior, y nada más**: Hoy, Mes, Futuro e Índice, en ese orden
  (#19).
- Colección, Revisar, Buscar, Clave, Ajustes y Pro se abren en pila sobre la barra, cada una con su
  `BackHandler`. Diez destinos en total; ninguno fuera de esa lista.
- Sin librería de navegación: un `enum Screen` en `App.kt`, como las hermanas.
- La app arranca siempre en Hoy. Sin splash con lógica ni pantalla de bienvenida. Al volver a primer
  plano se recalcula el día lógico.

### Los cuatro gestos

Son los únicos de la app; una pantalla nueva los reutiliza en vez de inventar uno:

1. **Tocar el glifo** de una tarea la completa (y otra vez, la reabre).
2. **Pulsación larga** sobre una entrada abre la hoja de estados y signifiers: migrar, programar,
   descartar, los tres signifiers, editar y borrar.
3. **Deslizar en horizontal** cambia de día en Hoy.
4. **Arrastrar** reordena dentro del mismo día o colección.

Tocar el texto lo edita en línea, sin otra pantalla (#23).

### Accesibilidad

- TalkBack y VoiceOver leen cada bullet como se ve: tipo, estado y signifiers, en los cinco idiomas
  ("Tarea completada, prioridad"). Completar, migrar y programar son acciones personalizadas del
  lector, sin depender de la pulsación larga (#53).
- La app se usa entera con la fuente al 200 % sin cortar texto. Todo lo que convive con el teclado
  scrollea con `imePadding()` y `verticalScroll`.
- Contraste AA en claro y en oscuro. Dianas tocables de 48 dp como mínimo, también el glifo.

### Pantallas grandes

Corte en **600 dp**, igual en Android y en iOS por su clase de tamaño equivalente (#34). Por encima,
Hoy y Mes limitan la columna de texto a **640 dp** centrada, el Índice pasa a dos columnas, el Future
Log a una rejilla de bloques, y Ajustes y el diálogo Pro van en un panel centrado. Sin diseños
propios de tableta más allá de eso.

### Principios

1. **Cero toques antes de escribir.** La app abre sobre el teclado.
2. **Nada se mueve solo.** Lo que cambia de sitio lo ha decidido el usuario.
3. **Nada de números de progreso.** Esto es un cuaderno, no un cuadro de mandos.

---

## 6. Enganche y retención: primera sesión, recordatorio, widgets, compartir y valoración

Un bujo digital muere de dos maneras: nadie lo abre porque abrirlo cuesta, o se abre y la lista de
abiertas crece sin que nadie la revise hasta que da vergüenza mirarla. Contra lo primero, abrir y
escribir cuesta cero toques. Contra lo segundo, la revisión es el bucle, y se ofrece sin empujar.
Nada necesita servidor.

### Primera sesión

Una sola pantalla: Hoy vacío, la fecha, el campo con el foco puesto y, hasta el primer bullet, una
pista corta con los prefijos (`- nota`, `o evento`, `* prioridad`). Un icono de interrogación abre la
clave. Sin tutorial, sin elegir portada, sin configurar nada (#31). Cada estado vacío de la app
invita a escribir su primer contenido, nunca a abrir Ajustes.

El recordatorio se ofrece **después** de guardar el primer bullet, una sola vez y dentro de Hoy, como
una línea discreta ("¿Te aviso para repasar el día a las 21:00?"). El permiso del sistema se pide
solo si el usuario dice que sí, y entonces el recordatorio queda encendido (#36, #38). Si no contesta,
la línea no vuelve.

### El bucle: capturar, revisar, migrar

La retención de un bujo no la fabrica una notificación: la fabrica que cerrar el mes sea agradable.

- Durante el día, capturar es una línea y un Intro.
- Al día siguiente, si quedaron tareas abiertas, Hoy enseña una línea: "Ayer quedaron N abiertas".
- Al cambiar de mes, Hoy y Mes enseñan "Febrero sin cerrar: N abiertas", y el mes que llega enseña
  "N entradas del Future Log esperan".
- Todas son **líneas dentro de la pantalla, nunca modales ni notificaciones**, y llevan a Revisar.
- Revisar empieza releyendo el periodo (reflexión) y sigue con una tarea cada vez. Cada decisión es un
  toque. "Migrada N veces" es la única presión, y es la del método.

### El recordatorio de reflexión

Una sola notificación al día, **apagada por defecto**, a las 21:00 si no se cambia. Su texto es fijo,
no asume ni reprende ("nunca has...", "vas a perder...") y **no cita ni una palabra del diario**.
Tocarla abre la revisión del día (#36, #38).

- **Android:** alarma inexacta (`setAndAllowWhileIdle`), sin `SCHEDULE_EXACT_ALARM`: un recordatorio
  de reflexión puede llegar unos minutos tarde, y así no hay permiso especial ni fricción en la
  revisión de Play. Se reprograma al arrancar el teléfono, al cambiar la hora o el huso y cada vez que
  se abre la app: es el fallo de la oficial, cuyo recordatorio desaparece. Con el permiso denegado, el
  interruptor de Ajustes sale apagado y explica cómo activarlo (#37).
- **iOS:** `UNCalendarNotificationTrigger` que se repite a la hora elegida, reprogramado al volver a
  primer plano si la hora cambió. Mismo tratamiento del permiso denegado.
- El permiso se pide en ese momento, desde la `Activity` en Android, nunca al arrancar.

Una notificación al día, bien escrita, y ninguna más: ni del Future Log, ni de rachas, ni de hitos.
Con un solo aviso semanal ya hay un 10 % que desactiva las notificaciones si el contenido no aporta; en
la franja de 6 a 10 por semana el abandono ronda el 30 %.

### Widgets: estructura, nunca contenido

Un bujo lleva tareas, citas y notas íntimas mezcladas, en una pantalla que ve cualquiera que mire de
reojo. **Los widgets enseñan números y glifos, nunca texto**, y no pueden hacer otra cosa: solo leen
`widget.json` (#40).

- **Widget de hoy** (gratis, #41, #42): glifos del método con las abiertas, hechas y eventos de hoy.
  Tocarlo abre Hoy con el campo enfocado y el teclado arriba (`bobbin://today?focus`). Sin
  `widget.json`, un estado vacío, nunca un cierre ni un hueco en blanco.
- **Widget del mes** (Pro, #51): un punto por cada día del mes con entradas y las abiertas de hoy.
- **Widget de pantalla de bloqueo** (Pro, iOS, #52): el glifo de punto con las abiertas y, en el
  rectangular, los eventos.
- Sin Pro, los widgets de pago se pintan bloqueados con "Bobbin Pro" y tocarlos abre el paywall. Se
  actualizan cuando cambia `widget.json`, sin sondeo.

### Compartir

Compartir es gratis (#43) y lo elige el usuario siempre: un día, un mes o una colección entera, como
**imagen de página punteada** dibujada con los glifos reales, o como texto plano con los mismos
símbolos en ASCII para donde no cabe una imagen. Una colección larga se reparte en varias imágenes sin
cortar una línea. Cada imagen lleva una marca pequeña de "Bobbin" en la esquina: es el canal de
crecimiento orgánico que el formato bujo ya tiene en redes. **La app nunca sugiere ni elige qué
compartir**: el diario es privado por defecto.

### Valoración

Una sola petición en toda la vida de la instalación, **justo después de cerrar la primera revisión
mensual completa**: es el momento en que el usuario ha demostrado que se queda y ha usado lo que
distingue a la app. Play In-App Review en Android, `SKStoreReviewController` en iOS. Nunca al
arrancar ni después de un error (#56). En esta categoría las valoraciones mueven el ranking más que
nada (§8).

### Sin rachas

No hay rachas, puntos ni estadísticas. En un bujo, la presión de racha empuja a apuntar relleno para
no romperla y a no migrar con honestidad; y un porcentaje de tareas hechas convierte la reflexión en
un examen. El progreso de un bujo es lo que queda escrito, y eso ya está en el papel.

---

## 7. Monetización: precio, gratis frente a Pro, paywall y lo que nunca se cobra

### El dato que decide el modelo

Igual que en las tres hermanas: **un usuario cuesta 0 EUR al mes**. Sin backend, sin cuentas, sin IA,
sin almacenamiento nuestro. Una suscripción no tendría nada que sostener, y en esta categoría la
suscripción es justo lo que se reprocha: el plan gratis de TickTick "casi no tiene sentido", Notion
bloquea el contenido al no renovar y cobra sin reembolso prorrateado, Reflectly llega a 59,99 $ al año.
El mercado ya trata "sin suscripción" como argumento de venta (Things 3, las opciones de compra única
de BujoFlow y Diarium).

**Decisión: una compra única no consumible como único producto de pago.** Producto `bullet_pro` en
RevenueCat, entitlement `pro`, las dos tiendas de una vez. **Sin suscripción, sin consumibles y sin
anuncios, en ningún plan y en ninguna versión.**

### El eje del paywall

Los ejes obvios rompen el método:

- Limitar **entradas, colecciones o meses** castiga el hábito diario que se quiere enganchar y cobra
  por escribir en tu propio cuaderno. Reseñas de una estrella.
- Limitar **la migración, la revisión o el Future Log** es cobrar el método, que es la promesa del
  producto y lo que la app oficial no da.
- No limitar **nada** deja la app sin producto que vender.

**Se cobran los adornos y las superficies. Nunca el método ni el contenido.** Es la misma regla de
Purl, traducida: el cuaderno entero es gratis; se paga por vestirlo (portadas y papeles), por llevarlo
fuera de la app (widgets de pago, libro en PDF, captura desde fuera) y por lo que tiene coste real de
construir (sincronización).

| Gratis para siempre | Pro en v1.0 | Pro en v1.1 | Pro en v1.2 |
|---|---|---|---|
| El método completo: rapid logging, tres bullets, cinco estados, tres signifiers | Siete de las ocho portadas (#49) | Libro del cuaderno en PDF (#68) | Sincronización por iCloud Drive o una carpeta de Drive (#69) |
| Hoy, Mes, Future Log e Índice | Papeles rayado, cuadrícula y liso (#49) | Captura desde fuera: Ajustes rápidos y Atajos de iOS (#67) | Resumen del año en texto (#70) |
| Colecciones ilimitadas | Colecciones de seguimiento desde la segunda (#50) | | |
| Revisión y migración diaria y mensual, reflexión | Widget del mes, Android e iOS (#51) | | |
| Clave de símbolos | Widget de pantalla de bloqueo, iOS (#52) | | |
| Búsqueda con filtros | | | |
| Recordatorio de reflexión y bloqueo con biometría | | | |
| Widget de hoy | | | |
| Compartir un día, un mes o una colección | | | |
| Exportar e importar (JSON y Markdown) y copia del sistema | | | |
| Una portada (salvia) y el papel punteado | | | |
| Un seguimiento | | | |
| v1.1: threading, nuevo cuaderno anual, preguntas de reflexión, fechas en lenguaje natural | | | |
| v1.2: variaciones del índice, importar de las apps hermanas | | | |

La distinción que hay que escribir para que la regla no se coma a sí misma: **exportar tus datos no
se cobra nunca; el libro en PDF maquetado no es una exportación de datos, es un producto**. Quien solo
quiere sus datos los tiene gratis y reimportables, en JSON y en Markdown legible con los símbolos del
método.

El seguimiento gratis es la perilla, igual que `FREE_HABIT_LIMIT` en Quilt: está para que todo el
mundo vea el formato antes de pagar, y el número se mide, no es un dogma (`FREE_TRACKER_LIMIT = 1`,
cuenta los seguimientos que existen ahora: archivar uno no libera hueco, borrarlo sí).

**Si se pierde Pro** (un reembolso), no se rompe nada ni se pierde una línea: la portada y el papel
activos vuelven a los gratis sin tocar el diario (#49), los seguimientos que ya existen siguen
legibles y editables y solo no se puede crear otro, y los widgets Pro pasan a su estado bloqueado.

### Lo que no se cobra nunca

1. **El método.** Migrar, programar, revisar, reflexionar, el Future Log y el Índice. Cobrar la
   migración sería cobrar lo único que distingue un Bullet Journal de una lista de tareas.
2. **El número de entradas, colecciones o meses.** Ni tope ni aviso.
3. **Exportar e importar.** Los datos atrapados son una de las quejas grandes de la categoría (§1), y
   aquí pesan más porque el diario es de uno.
4. **El recordatorio y el bloqueo.** Cobrar por proteger un diario es la misma señal de desconfianza
   que cobrar por la copia.
5. **Compartir.** Cada página compartida es marketing gratis.

### Precio

La escalera real de la familia, leída de sus repositorios:

| App | Escaparate |
|---|---|
| Quilt | 4,99 EUR |
| Purl | 5,99 EUR en v1.0, 8,99 EUR desde v1.1 |
| MoodTraker | 7,99 EUR |
| **Bobbin** | **7,99 EUR en v1.0, 9,99 EUR desde v1.1** |

**Decisión: 7,99 EUR en v1.0.** Menos de la mitad de un cuaderno de papel bueno, por debajo de los
9,99 $ de Things 3 y muy por debajo de cualquier compra única del sector (BujoFlow, 29,99 $; Daylio de
por vida, 59,99 $). Y un solo año de cualquier suscripción de la categoría ya cuesta más: TickTick
35,99 $, Daylio 35,99 $, Journey 29,99 $, Day One Silver 49,99 $. Bobbin sale más caro que Purl porque
el paquete de pago de v1.0 es más ancho (portadas, papeles, seguimientos y dos widgets) y porque el
método entero, gratis, ya vale más que el cuaderno de una línea.

**Sube a 9,99 EUR en v1.1**, el día que sale el libro del cuaderno en PDF (#68). La subida se debe a
que el libro añade coste de desarrollo real, no a estrategia de precios. **Quien compra antes de la
subida conserva Pro para siempre, con todo lo que llegue después, sin recargo**: es un no consumible,
la compra es de por vida y no por versión, y en RevenueCat subir el precio es tocar el panel, no el
código.

**Sin descuento de lanzamiento ni oferta por tiempo limitado.** El precio de salida es el precio final
de v1.0: un descuento vende la rebaja, no el producto, y enseña a esperar a la siguiente.

**Precios regionales activados en las dos tiendas desde el primer día**, no solo EUR y USD: es lo
único que hace que "asequible" no sea solo una palabra fuera de la eurozona, y no cuesta una línea de
código. Precio base en España; el resto de países, por la conversión automática de cada tienda
(#7, #47).

### Lo que llega al bolsillo

```
7,99 EUR escaparate            9,99 EUR escaparate
/ 1,21 (IVA 21%)  = 6,60 EUR   / 1,21 (IVA 21%)  = 8,26 EUR
- 15 % comisión   = 5,61 EUR   - 15 % comisión   = 7,02 EUR netos
```

| Objetivo | Ventas/mes a 7,99 | Descargas/mes a 7,99 | Ventas/mes a 9,99 | Descargas/mes a 9,99 |
|---|---|---|---|---|
| 500 EUR | ~90 | ~3.600 | ~72 | ~2.880 |
| 1.000 EUR | ~179 | ~7.160 | ~143 | ~5.720 |
| 3.000 EUR | ~535 | ~21.400 | ~428 | ~17.120 |

Descargas calculadas al 2,5 % de conversión, la hipótesis de trabajo de la familia, no un dato medido
de esta categoría. La comisión del 15 % exige el Small Business Program en Apple y el primer millón en
Google.

### Reglas

1. **El plan gratis es la prueba.** No existen pruebas gratuitas para compras únicas.
2. **El paywall aparece al chocar con un límite real** (#48): elegir una portada o un papel Pro, crear
   el segundo seguimiento, tocar un widget Pro colocado sin Pro, generar el PDF, usar la captura desde
   fuera, activar la sincronización o abrir el resumen del año. La única entrada que no es un choque
   es la fila "Bobbin Pro" de Ajustes, para quien quiere comprar a propósito. **Nunca al arrancar,
   nunca tras un número de usos.** Es el `ProDialog` de las hermanas: qué incluye, precio leído de la
   tienda (nunca un número fijo en el código), "compra única, sin suscripción" visible sin scroll,
   comprar, restaurar y cerrar. Cerrar sin comprar deja todo como estaba.
3. **"Restaurar compras" siempre visible** en Ajustes y dentro del diálogo, también con Pro activo.
   Requisito de Apple.
4. **El estado de compra nunca bloquea contenido.** Se guarda en local el último derecho conocido tras
   cada consulta a RevenueCat; sin red o con el servicio caído, se conserva. Ninguna pantalla espera a
   RevenueCat para pintarse. Y borrar el diario desde Ajustes no borra el derecho Pro (#33).
5. **Nada de anuncios.**
6. **RevenueCat KMP**, un producto no consumible, las dos tiendas de una vez (#47).
7. **Small Business Program de Apple solicitado antes de subir la primera build a revisión**: 15 % en
   vez de 30 %. Si la cuenta no está inscrita cuando llegan las primeras ventas, esas se cobran al
   30 %. Lo solicita el autor en #7.
8. **Venta cruzada discreta**: una sección "Más apps" en Ajustes con una fila por app hermana
   publicada en la tienda de esa plataforma (`SIBLINGS` en `data/AppInfo.kt`); si no hay ninguna, la
   sección no aparece. Sin banners, sin notificaciones, sin cruzar datos entre apps.

### Cuándo tocaría una suscripción

Solo si aparece un coste recurrente real: nube propia, IA o impresión física del cuaderno. Y aun
entonces, compra única para la app y cobro aparte solo para el servicio con coste, nunca convertir en
suscripción el método ni el diario. La sincronización de v1.2 no lo reabre: va sobre la nube del
propio usuario (iCloud Drive o una carpeta que él elige), sin servidor nuestro.

---

## 8. Identidad y ficha: nombre, icono y ASO

### El nombre

El repositorio se llama `bullet` y el identificador es `com.baltajmn.bullet`, pero el nombre visible
no coincide, igual que Quilt vive en `com.baltajmn.habit` y Purl en `com.baltajmn.line`. Y no puede
coincidir: "Bullet Journal" a secas es el nombre del método y de la app oficial, y la búsqueda ya la
ocupan al menos siete apps con esas palabras (Bullet Journal Companion, Bullet-Journal v2, Bullet:
Journal, Daily Planner, Bujo - Bullet Journal & Planner, BujoFlow, Bullet - Journal & Planner, May:
Beautiful Bullet Journal). Un nombre genérico no se recuerda ni se puede buscar por nombre exacto, y
rompe la convención de la familia, que nombra con metáfora.

**Decisión: Bobbin.** La bobina guarda el hilo: guiño al threading del método (§2, #63), que enlaza
una colección con su continuación, y sigue la familia textil de Quilt y Purl. Se lee igual en los
cinco idiomas y tiene pariente directo en tres (bobina, bobine).

El **nombre de tienda** lleva el método detrás: `Bobbin: Bullet Journal`, 22 caracteres, dentro de
los 30 de Play y de App Store. Llevar la keyword completa en el título es justo lo que hace subir a
Buju en el ranking con muy pocas valoraciones (§1). El **subtítulo de App Store** es
`Bujo, diario y agenda` (21 de 30).

**Reservas, en este orden:** `Skein` y después `Selvage`. Si Bobbin choca con algo en la comprobación
del autor (#1), se pasa a Skein sin reabrir ninguna otra issue: el nombre solo vive en los textos, la
ficha, el icono, el esquema de enlaces (`bobbin://`) y los nombres de fichero de copia
(`bobbin-AAAA-MM-DD.zip`, `bobbin-sync.json`), y esos se cambian todos a la vez antes de publicar.

**Aviso heredado, para que la comprobación no sea un trámite.** La búsqueda de nombre de Purl, en
septiembre de 2026 (`../line/SPEC.md` 7), ya tropezó con los tres candidatos: *Bobbin* salió con app
exacta en App Store o marca viva en clase 9; *Skein*, con app exacta en Play y marca viva en EUIPO en
clases 9 y 42; y *Selvedge* (la grafía británica de Selvage), con app exacta en App Store. Allí se
buscaban como nombre suelto de un diario; aquí el nombre de tienda es compuesto y la clase que pesa es
la 9 (software), así que la comprobación en Play, App Store, USPTO y EUIPO decide, y lo que salga se
anota en esta sección. Una lección que Purl pagó: un nombre se comprueba en las tiendas y en el
registro, no en un buscador web. En la misma búsqueda se mira si **"Bullet Journal"** está registrada
como marca y en qué clases, porque va en el nombre de tienda (§12).

### Identificadores

Fijados antes de la primera línea de código. **Son irreversibles tras publicar.** La tabla completa,
con los derivados (bundle id del widget, kinds de WidgetKit, canal de notificación), está en
`docs/tecnico.md` 1.

| Qué | Valor |
|---|---|
| Nombre visible | `Bobbin`, en los cinco idiomas |
| Nombre de tienda | `Bobbin: Bullet Journal` |
| Subtítulo de App Store | `Bujo, diario y agenda` (es); el de cada idioma, en `store/` (#57) |
| `applicationId` de Android y bundle id de iOS | `com.baltajmn.bullet` |
| App Group | `group.com.baltajmn.bullet` |
| Paquete Kotlin raíz | `com.baltajmn.bullet` |
| Producto de RevenueCat | `bullet_pro`, no consumible, que da el entitlement `pro` |
| Esquema de enlaces | `bobbin://` (`bobbin://today?focus`, `bobbin://review`, `bobbin://pro`) |
| Categoría en las dos tiendas | Estilo de vida (Lifestyle), nunca Productividad |
| Política de privacidad | `https://bullet.baltajmn.dev/`, con el slug interno, como `line.baltajmn.dev` |

**La categoría es una decisión de ASO, no de gusto.** La app oficial, Bullet Journal Companion, está
en Productividad y es la que pierde puestos en la búsqueda de "bullet journal" frente a apps con peor
ajuste al término. Productividad la dominan Todoist, TickTick y Notion; Estilo de vida es donde
compiten los diarios y planners que ganan esa búsqueda.

### El icono

El criterio de toda la familia: el icono es la propia metáfora del producto sobre fondo oscuro
(`#2C2820` a `#17150F`), porque en la comparativa a 48 px una versión crema desaparece sobre un
lanzador claro. Aquí, **una bobina con su hilo y un punto de bullet**: la bobina es el nombre y el
punto es la tarea, el glifo más reconocible del método. La geometría exacta está en
`docs/pantallas.md`, y el dibujo final lo aprueba el autor antes de generar tamaños (#18).

Sale todo de `tools/generate_icons.py` sobre `tools/icon-master.svg`, copiados de line: PNG de 1024
para iOS, adaptativo de Android con su capa `monochrome` de un solo color, PNG heredados en cinco
densidades e icono de notificación en blanco sobre transparente.

### ASO

Lo que dice el mercado de la keyword "bullet journal" en inglés (appfigures): popularidad 38 (demanda
de búsqueda directa baja), competitividad 91 sobre 100, unas 11.600 apps en resultados. Quién ocupa el
top y por qué:

| Puesto | App | Por qué está ahí |
|---|---|---|
| 1 | Zinnia | No lleva la keyword completa: gana por valoraciones |
| 2 | DailyBean | Las mejores valoraciones del grupo, pierde puestos por palabras clave |
| 3 | Buju | Sube con unas 15 valoraciones nuevas al mes por llevar la keyword en el nombre |
| 4 | Digital Planner | Keyword genérica de planner |
| 5 | Bullet Journal Companion (oficial) | Penalizada por estar en Productividad |

Tres palancas, y las tres cuestan cero código: la keyword en el nombre de tienda, la categoría Estilo
de vida, y acumular valoraciones pronto con una sola petición bien colocada (§6, #56).

Palabras clave:

- Inglés: `bullet journal`, `bujo`, `daily planner`, `digital planner`, `journal app`,
  `productivity journal`. `habit tracker` solo en la descripción, cruzado con Quilt, nunca en el
  nombre.
- Español: no hay datos de competencia medidos. Por analogía con el hueco del inglés, términos largos
  con menos competencia esperada: `diario bullet journal`, `bujo en español`, `agenda minimalista`,
  `diario personal sin cuenta`, en vez de pelear por "diario" o "agenda" a secas.
- Portugués, alemán y francés: sin datos; se escriben en #57 a partir del glosario de
  `docs/textos.md`, con "bullet journal" y "bujo" sin traducir, que es como se busca el método.

Los textos definitivos de las dos fichas, en los cinco idiomas, van en `store/listings/` (Play) y
`store/app-store/` (App Store), con sus topes comprobados (#57). Las reglas que no cambian por idioma:

- La **primera frase** de la descripción dice compra única, sin cuenta y sin anuncios.
- El ángulo es el método: rapid logging real, migración a mano, nada se mueve solo. **Ni una promesa
  de salud mental ni de productividad milagrosa**: convierte la app en producto de bienestar a ojos
  de la revisión y no mueve la conversión.
- Quilt y Purl solo se nombran dentro de la descripción larga, nunca en el título ni en el
  subtítulo. Quien busca seguimiento serio de hábitos, con recordatorios y estadísticas, se remite a
  Quilt (#50).
- Las capturas enseñan contenido que existe en la app, en los cinco idiomas (#58).

---

## 9. Cumplimiento de tienda: privacidad, formularios y edad

Bloque de trabajo del día 1, no del día 20: son los trámites que bloquean la publicación **después**
de que el código esté listo. Las respuestas completas van en `store/formularios.md` (#59).

| Qué | Detalle |
|---|---|
| **Trader status del DSA** | Obligatorio en la UE desde el 17/02/2025. Apple retira las apps que no lo declaran y publica la dirección del trader en la ficha. Si la cuenta ya lo declaró por una hermana, se hereda (#7) |
| **Manifiesto de privacidad de iOS** | `PrivacyInfo.xcprivacy` desde el primer archivo: sin seguimiento, los tipos de datos de RevenueCat y las razones de API de motivo obligatorio que usen nuestro código y los runtimes de Kotlin y Compose. RevenueCat trae el suyo. Se parte del de Purl. Sin manifiesto, rechazo automático `ITMS-91053` / `ITMS-91061` en la primera subida |
| **App Privacy y Data Safety** | Con RevenueCat dentro se declaran historial de compras e identificadores: obligatorio, no compartido, sin seguimiento. **El texto del diario no sale del dispositivo**, y eso se dice en los dos formularios y en la política. Sin anuncios en ningún plan, declarado (#59) |
| **Clasificación por edad** | Cuestionario de Apple con franjas 4+, 9+, 13+, 16+ y 18+: todo "no", resultado esperado 4+. El usuario escribe lo que quiere pero no lo comparte con nadie: no es contenido generado por usuarios a efectos de moderación. IARC: la clasificación más baja de cada sistema. En Play, público objetivo a partir de 13 para no entrar en la política de Familias |
| **Copia automática del sistema** | Android: Auto Backup sube hasta 25 MB por app al Drive del usuario; el diario es texto y cabe de sobra. `journal.json` entra; `widget.json` y la cuarentena, no. La nube se configura con `disableIfNoEncryptionCapabilities="true"`: solo sube si va cifrada de extremo a extremo, que en Android 9+ exige bloqueo de pantalla. Por debajo de Android 12, `fullBackupContent` con `requireFlags="clientSideEncryption"`. iOS: la copia de iCloud del dispositivo incluye Application Support. Se comprueba reinstalando en dispositivo real antes de prometerlo en la ficha (#46) |
| **Data Safety y la copia del sistema** | Google no declara lo que va cifrado de extremo a extremo y solo pueden leer emisor y receptor. Con la nube solo cifrada, la copia del sistema entra en esa excepción. **Es una inferencia sobre la definición**, porque la ayuda de Play no menciona la copia del sistema: se deja escrita en `store/` con su fuente y se revisa si Google publica algo concreto |
| **Prueba cerrada de Google** | 12 probadores durante 14 días continuos, **por app**, más hasta 7 días de revisión del acceso a producción. Si el número baja de 12 hay que recuperarlo y volver a sostenerlo. Es el camino crítico del calendario, y por eso arranca con el andamiaje vacío (#10), no al final. Primero los probadores de Quilt y Purl. El intercambio recíproco entre desarrolladores arrastra el riesgo de asociación de cuentas que documenta `HabitTracker/store/testers.md` |
| **La corrección de honestidad** | Con RevenueCat dentro, "sin red" no es cierto. El mensaje es "sin cuenta y sin analítica; la única conexión que hace la app es la de la compra" |
| **Cumplimiento de exportación** | `ITSAppUsesNonExemptEncryption = NO`: la única criptografía es el HTTPS del sistema que usa RevenueCat |
| **Permisos** | Notificaciones, solo cuando el usuario enciende el recordatorio. Biometría con `NSFaceIDUsageDescription` en iOS. Ninguna alarma exacta, ni ubicación, ni contactos, ni fotos |
| **iPad** | La app es universal como sus hermanas, así que App Store exige capturas de iPad además de las de iPhone (#58) |

---

## 10. Arquitectura prevista y trampas heredadas

El repositorio solo tiene documentos: esto es el plan, no el estado. El andamiaje se copia de line
(#8) con las **mismas versiones** que las hermanas, no las más nuevas: subir de versión es una tarea
para toda la familia a la vez. El contrato completo, fichero a fichero, está en `docs/tecnico.md`;
aquí queda el porqué.

### Árbol de `shared/src/commonMain/kotlin/com/baltajmn/bullet`

```
App.kt                   enum Screen (cuatro en la barra, seis en pila) + puerta de bloqueo + enlaces bobbin://
model/Entry.kt           Entry, Bullet, TaskStatus, Signifier; una línea y el tope de 500 al capturar
model/Place.kt           Daily(date), Monthly(yearMonth, day?), Future(yearMonth, day?), InCollection(id)
model/Journal.kt         entries, collections, settings, schemaVersion; solo operaciones puras
model/DayClock.kt        día lógico (04:00 por defecto), meses, primer día de la semana
model/Migration.kt       migrate, schedule, discard, migrationCount; una entrada por llamada, nunca una lista
model/RapidParse.kt      prefijos de bullet y signifier
data/Storage.kt          expect: journal.json atómico, .bak, cuarentena y migración de esquema
data/BobbinRepository.kt fuente única de verdad, estado Compose, escritor único
data/Search.kt           filtro en memoria con plegado de acentos y #etiquetas
data/WidgetState.kt      deriva widget.json y lo escribe en el App Group
data/Widgets.kt          expect: refrescar los widgets tras cada guardado
data/Merge.kt            fusión por id: importar y, en v1.2, sincronizar
data/Zip.kt              zip STORED de escritura y lectura con CRC32, puro común
data/Export.kt           journal.json y un Markdown por mes y por colección
data/FilePicker.kt       expect: elegir fichero para importar y destino para exportar
data/Backup.kt           expect: fecha de la última copia y reglas de la copia del sistema
data/Lock.kt             expect: biometría con respaldo al código, multitarea oculta
data/Reminders.kt        expect: el recordatorio de reflexión
data/Sharing.kt          expect: imagen de página punteada o texto, y hoja de compartir
data/AppInfo.kt          URL de la política, SIBLINGS, versión
billing/Billing.kt       expect: RevenueCat, comprar / restaurar / refrescar, último derecho conocido
i18n/Strings.kt          los cinco idiomas en una tabla, obligados por firma
ui/theme/Theme.kt        papel y tinta, portadas, papeles, Literata, corte de pantalla ancha
ui/BulletGlyph.kt        los glifos del método, en Canvas
ui/Icons.kt              los pocos iconos de la barra y la cabecera, en Canvas
ui/EntryList.kt          la lista y el campo de captura, compartidos por Hoy, Mes y Colección
ui/EntrySheet.kt         la hoja de la pulsación larga
ui/TodayScreen.kt        Hoy
ui/MonthScreen.kt        Mes
ui/FutureScreen.kt       Futuro
ui/IndexScreen.kt        Índice
ui/CollectionScreen.kt   una colección, o un seguimiento
ui/ReviewScreen.kt       reflexión y revisión tarea a tarea
ui/SearchScreen.kt       búsqueda
ui/KeyScreen.kt          la clave de símbolos
ui/SettingsScreen.kt     ajustes, borrar datos, Pro, más apps
ui/LockScreen.kt         overlay, se pinta antes que cualquier otra pantalla
ui/Pro.kt                ProDialog, el paywall de las hermanas
```

Fuera de `commonMain`: los widgets de Android en `androidMain/.../widget/` (Glance), y los de iOS en
`iosApp/BobbinWidget` con `BobbinStore.swift`, que decodifica `widget.json` y nada más.

### `journal.json`

Ilustrativo; el formato exacto, sus nombres y su validación están en `docs/tecnico.md` 4.1 y 4.6.

```json
{
  "schemaVersion": 1,
  "entries": [
    { "id": "e-3f9a1c2e", "bullet": "task", "text": "Llamar al fontanero", "status": "migrated",
      "signifiers": ["priority"], "place": { "daily": "2026-09-22" }, "order": 0,
      "createdAt": 1790064000000, "updatedAt": 1790150400000, "from": null },
    { "id": "e-8b21d0f4", "bullet": "task", "text": "Llamar al fontanero", "status": "open",
      "signifiers": ["priority"], "place": { "monthly": "2026-10" }, "order": 3,
      "createdAt": 1790150400000, "updatedAt": 1790150400000, "from": "e-3f9a1c2e" },
    { "id": "e-51c7aa09", "bullet": "event", "text": "Cumpleaños de Ana", "status": "open",
      "signifiers": [], "place": { "future": "2027-02", "day": 14 }, "order": 0,
      "createdAt": 1790150400000, "updatedAt": 1790150400000, "from": null }
  ],
  "collections": [
    { "id": "c-1d2e7a40", "title": "Lecturas 2026", "createdAt": 1790064000000,
      "archived": false, "kind": "notes", "threadFrom": null }
  ],
  "settings": {
    "dayStartHour": 4,
    "firstDayOfWeek": null,
    "reminderOn": false,
    "reminderHour": 21,
    "reminderMinute": 0,
    "reminderOffered": false,
    "lockOn": false,
    "cover": "sage",
    "paper": "dotted"
  }
}
```

- Una lista de entradas con `id` único, no un mapa por fecha: una entrada vive en un día, en un mes, en
  el Future Log o en una colección, y la fusión va por `id`.
- `place` distingue la línea del calendario (`day` presente) de la lista de tareas del mes (`day`
  ausente), en `monthly` y en `future`.
- `from` es la cadena de migración; `threadFrom`, el hilo entre colecciones. Los dos existen desde el
  esquema 1 aunque el threading llegue en v1.1.
- `firstDayOfWeek` a `null` significa "el del sistema".
- Los ajustes van en su propio objeto porque la importación los ignora salvo con el diario vacío (§4).

### `widget.json`

`journal.json` vive en el almacenamiento privado de la app, **fuera del App Group**. En el contenedor
compartido solo hay esto:

```json
{
  "date": "2026-09-23",
  "open": 3,
  "done": 2,
  "events": 1,
  "month": "2026-09",
  "monthMask": "110110011101111011101000000000",
  "reviewPending": true,
  "isPro": false
}
```

`monthMask` tiene un carácter por día del mes, `1` si ese día tiene entradas, para el widget del mes.
Ni un carácter de texto del diario. `BobbinStore.swift` decodifica estos campos y nada más: no hay
modelo del diario en Swift que mantener en paridad, y no hay forma de que un widget lea ni borre una
entrada. Un campo nuevo se añade en `WidgetState` y en `BobbinStore.swift` en el mismo commit, con un
fichero de ejemplo común que decodifican las dos plataformas en sus tests.

### La copia es un zip

```
bobbin-2026-09-23.zip
+-- journal.json         lo que reimporta la app, con los ajustes
+-- months/2026-09.md    un Markdown por mes, con los símbolos del método
+-- collections/<título>.md
```

- **STORED, sin compresión**, y CRC32 escrito a mano: `data/Zip.kt` se copia de line, unas 150 líneas
  con sus tests. Okio no sirve (solo lee, y solo en JVM).
- Se escribe **entrada a entrada** al destino, sin montar el archivo en memoria.
- Los nombres de dentro son fijos y en inglés en los cinco idiomas.
- iOS (Archivos), los gestores de Android y cualquier escritorio abren un zip con doble toque: el
  usuario comprueba por sí mismo que sus datos son suyos.
- La importación acepta el zip y, por compatibilidad, un `journal.json` suelto.

### El fichero crece, y se reescribe entero

Un bullet ocupa unos 250 bytes con la sobrecarga del JSON. Diez al día son unas 3.650 entradas y del
orden de 1 MB al año; cinco años, unas 18.000 entradas y 4 a 5 MB. Es una estimación, no una medida.

Lo que deja de valer no es el tamaño, es **reescribir el fichero entero en cada cambio**:

- **Un único escritor, fuera del hilo principal**, detrás de un `Mutex`, que escribe siempre la última
  instantánea. Dos escrituras cruzadas rotan la `.bak` dos veces y la copia buena se pierde.
- **iOS, protección `CompleteUntilFirstUserAuthentication`**, no `Complete`: con `Complete` el fichero
  deja de poder abrirse unos segundos después de bloquear el teléfono y el último guardado fallaría
  sin aviso.
- El diario se carga **una vez por sesión** y las pantallas leen de memoria (#55).
- La señal para partir el fichero **no son los KB**: es medir en el dispositivo más antiguo soportado
  el tiempo de serializar y escribir con 5.000 entradas (#55). Si el percentil 95 se acerca a 50 a
  100 ms, donde un guardado ya se nota como tirón, se parte por años y se mantiene en memoria el año
  activo. Las cadenas de migración que cruzan de año lo complican, y por eso es plan B, no plan A. El
  widget no cambia: nunca leyó el diario.

### Búsqueda sin base de datos

El diario ya está en memoria: filtrar con `contains()` sin distinguir mayúsculas ni acentos (una tabla
fija de plegado para las letras de los cinco idiomas) sobre unos miles de líneas cortas es cuestión de
milisegundos y no necesita índice (#35). **FTS5 no viene en la SQLite del sistema de la mayoría de
Android**: una base de datos exigiría empaquetar SQLite propia y un segundo almacén que rompe "un solo
fichero es la fuente de verdad". Plan B de verdad, no alternativa cómoda.

### Bloqueo

`data/Lock.kt` y `ui/LockScreen.kt` se copian de line (#39): `androidx.biometric` 1.1.0, la única
estable, con `MainActivity` como `FragmentActivity`; `LAContext` con `.deviceOwnerAuthentication` en
iOS. Sin PIN propio. Vuelve a pedirse a los **60 segundos** en segundo plano, con reloj monótono; un
arranque en frío siempre pide. Multitarea tapada con `setRecentsScreenshotEnabled(false)` en Android
13+ y con una vista puesta desde `iOSApp.swift` en iOS. Cifrar el fichero por encima es teatro: la
clave tendría que estar donde el propio proceso la lea sin el usuario.

### Sincronización, y por qué en v1.2

Lo caro no es activarla, es decidir qué pasa cuando dos dispositivos cambian lo mismo sin verse. Aquí
la unidad es la entrada con `id` y `updatedAt`, lo que hace la fusión más limpia que en Purl, pero un
estado cerrado en un lado y editado en el otro sigue siendo un conflicto. Por eso llega con la regla ya
escrita en §4 y nunca como fusión silenciosa (#69). iOS sobre iCloud Drive del usuario; Android sobre
una carpeta elegida con el Storage Access Framework.

### Trampas heredadas que siguen aplicando

Ya pagadas en Quilt, MoodTraker y Purl; cada una tiene su sitio en `docs/tecnico.md`:

1. **`purchases-kmp` 3.2.1 rompe el link de los tests de iOS**: su `linkerOpts` apunta al Xcode de la
   máquina de RevenueCat. Se arregla con `-L$(xcode-select -p)/.../usr/lib/swift/<sdk>` solo en Mac y
   solo para los binarios de test, en `shared/build.gradle.kts`.
2. **`CADisableMinimumFrameDurationOnPhone`** tiene que estar en el `Info.plist` y valer `true`, o
   Compose Multiplatform aborta al arrancar y parece que la app ni se lanza.
3. **`plutil -extract` sin `-o -` reescribe el fichero de entrada.**
4. **El App Group es un contrato frágil.** En Quilt y MoodTraker el widget de Swift reescribe el JSON
   entero y borra los campos que su `struct` no declara. Aquí el diario ni está en el App Group y los
   widgets no escriben, pero `widget.json` sigue exigiendo el mismo commit en Kotlin y en Swift.
5. **Parejas suplentes al cortar texto**: se cuenta en puntos de código y el corte nunca separa una
   pareja (`e8ef697` en Quilt).
6. **El `versionCode` no se reutiliza nunca**, ni entre canales de Play. Vive solo en
   `androidApp/build.gradle.kts` y lo sube quien etiqueta.
7. **La descripción larga de Play conserva los saltos de línea**: cada párrafo de `store/listings/`
   va en una sola línea.
8. **La capa `monochrome` del icono adaptativo es una máscara de un solo color.**
9. **`imePadding()` sin `verticalScroll`** encoge la pantalla y corta lo de abajo. Hoy abre con el
   teclado arriba: es la trampa que más probabilidades tiene de repetirse aquí.
10. **El permiso de notificaciones** se pide desde la `Activity` y en el momento en que se enciende
    el recordatorio, nunca al arrancar.
11. **Nada de alarmas exactas**: `setAndAllowWhileIdle` evita `SCHEDULE_EXACT_ALARM` y su revisión.
12. **La vista que tapa la multitarea en iOS** se pone desde `iOSApp.swift`: Compose no repinta antes
    de la foto del sistema.
13. **La previsualización del selector de widgets de Android** no es un render real: un XML aparte,
    mantenido a mano.
14. **Carpeta sincronizada de `iosApp/iosApp`**: un fichero nuevo entra en el target sin tocar el
    `.pbxproj`, y la versión vive en `Config.xcconfig`.
15. **Los textos de `AppIntents`** (v1.1, #67) tienen que ser literales: un valor de la tabla de
    idiomas rompe el build con `No AppIntents metadata have been exported`.
16. **Los tests de iOS en CI corren en `macos-26`**: `ui-uikit` de Compose referencia una clase que
    solo existe desde el SDK de iOS 26.

---

## 11. Plan de ataque: el número de issue de cada bloque

Cada paso es una o varias issues del repo; el orden es el de dependencias, y el contrato de cada una
está en `docs/tecnico.md` 11.

1. Nombre, identificadores y precio (#1, #2). El autor comprueba el nombre en tiendas y registros.
2. Los cuatro documentos: este SPEC, `docs/tecnico.md`, `docs/pantallas.md` y `docs/textos.md` (#3,
   #4, #5, #6).
3. Altas en las dos consolas, trader status, Small Business Program, RevenueCat y secretos (#7).
4. Andamiaje copiado de line y los cuatro workflows (#8, #9).
5. Primera build instalable y **alta de la prueba cerrada de Google con 12 probadores** (#10). El
   reloj de los 14 días arranca aquí, no al final.
6. Modelo y almacén: `Entry`, `Place`, `Journal`, `DayClock`, migración en el modelo, `journal.json`
   atómico y migración de esquema (#11, #12, #13, #14, #15).
7. Textos, tema, glifos e icono (#16, #17, #18).
8. Navegación, captura rápida, Hoy, estados y signifiers, editar y borrar (#19, #20, #21, #22, #23).
9. Mes, Future Log y su aviso, revisión y reflexión (#24, #25, #26, #27, #28).
10. Índice, colecciones y clave (#29, #30, #31).
11. Ajustes, borrar todos los datos y pantallas grandes (#32, #33, #34).
12. Búsqueda (#35).
13. Recordatorio de reflexión en las dos plataformas (#36, #37, #38).
14. Bloqueo y multitarea oculta (#39).
15. `widget.json` y los widgets de hoy (#40, #41, #42).
16. Compartir, zip y exportar, importar fusionando, copia del sistema (#43, #44, #45, #46).
17. Compras, paywall, portadas y papeles, seguimientos, widget del mes y de bloqueo (#47, #48, #49,
    #50, #51, #52).
18. Accesibilidad, tests numerados, rendimiento y valoración (#53, #54, #55, #56).
19. Ficha, capturas, privacidad y formularios (#57, #58, #59).
20. Beta, dispositivo físico y lanzamiento (#60, #61, #62).
21. **v1.1**: threading, nuevo cuaderno, reflexión guiada, fechas en lenguaje natural, captura desde
    fuera y libro en PDF con la subida a 9,99 EUR (#63 a #68).
22. **v1.2**: sincronización, resumen del año, variaciones del índice e importar de las hermanas (#69
    a #72).

### Estimación de v1.0

**Producto, unas 30 jornadas**

| Tarea | Jornadas |
|---|---|
| Andamiaje copiado y renombrado, CI | 1 |
| Modelo, migración en el modelo, almacén atómico, esquema | 2 |
| `Strings.kt` con los textos ya escritos, tema y glifos | 1 |
| Navegación, captura rápida, Hoy, estados, editar y borrar | 3 |
| Mes, Future Log y aviso del mes | 2 |
| Revisión y reflexión | 2 |
| Índice, colecciones y clave | 1,5 |
| Ajustes, borrar datos, pantallas grandes | 1,5 |
| Búsqueda | 1 |
| Recordatorio en las dos plataformas | 1,5 |
| Bloqueo biométrico y multitarea oculta, copiados de line | 1 |
| `widget.json` y widgets de hoy | 2 |
| Compartir como página punteada | 1 |
| Zip, exportar, importar fusionando, copia del sistema | 1,5 |
| RevenueCat y paywall, copiados de line | 1 |
| Portadas, papeles y seguimientos | 2 |
| Widget del mes y de pantalla de bloqueo | 1,5 |
| Accesibilidad, rendimiento, valoración | 2 |
| Pulido en dispositivo real, claro y oscuro, texto grande, tableta | 1,5 |

**Tienda, unas 3 jornadas**: altas y trámites 0,5; RevenueCat 0,5; probar compras de verdad 1;
capturas de teléfono y de tableta 1.

**Riesgo, 3 jornadas**: 2 de beta con gente real, 1 de colchón por rechazo.

**Total: unas 36 jornadas. De 7 a 9 semanas de calendario**, mandadas por los 14 días de prueba
cerrada de Google más hasta 7 de revisión del acceso a producción. Si la prueba cerrada no arranca
con el andamiaje (#10), el calendario se alarga aunque el código esté terminado.

---

## 12. Riesgos abiertos

1. **El nombre.** La búsqueda de Purl ya encontró *Bobbin*, *Skein* y *Selvedge* ocupados como nombre
   suelto (§8). Y "Bullet Journal" es el nombre del método de Ryder Carroll: si está registrado como
   marca en la clase del software, llevarlo en el nombre de tienda puede traer una reclamación o un
   rechazo, aunque muchas apps del nicho lo lleven. Los dos se resuelven con la comprobación del autor
   en #1, antes de dar de alta nada en las consolas. Nada del código depende del nombre salvo el
   esquema de enlaces y los nombres de fichero de copia.
2. **La migración a mano puede leerse como trabajo.** Es el método, y es previsible alguna reseña de
   "¿por qué no pasa las tareas sola?". La respuesta es hacer rápida cada decisión (un toque, líneas en
   vez de modales, "migrada N veces" solo desde la segunda), nunca automatizar. Si la queja crece, se
   itera la velocidad de Revisar, no la regla.
3. **El paquete de pago puede parecer fino** en v1.0: portadas, papeles, seguimientos y dos widgets.
   Se mide en la beta: si en dos semanas no compra nadie, el problema es el paquete, no el precio. Por
   eso el precio sube cuando llega el libro en PDF, no antes (§7).
4. **"He cambiado de móvil y he perdido mi bujo"** es la reseña de una estrella previsible hasta v1.2.
   La defensa es la copia cifrada del sistema, el traspaso entre dispositivos y exportar gratis.
5. **Reescribir el fichero entero en cada cambio** puede notarse tras años de uso. Se mide en #55 con
   5.000 entradas y hay plan B escrito (§10).
6. **Solapamiento con Quilt.** Los seguimientos de papel pueden leerse como un tracker de hábitos
   pobre. Se contienen como colección de papel, sin recordatorios ni estadísticas, y la ficha remite a
   Quilt.
7. **Una app oficial nueva** del autor del método se llevaría la marca. La ventaja que queda es
   Android, la fidelidad al método y la compra única.
8. **La prueba cerrada de Google es por app** y vuelve a aplicar aquí entera.
9. **Auto Backup y Data Safety**: con `disableIfNoEncryptionCapabilities`, quien no tiene bloqueo de
   pantalla se queda sin copia en la nube, y la exención de Data Safety es una inferencia sobre la
   definición de Google, no una frase suya.
10. **ASO sin datos en cuatro idiomas.** La competencia de la keyword solo está medida en inglés.
11. **Tableta y horizontal** multiplican las pantallas que revisar y exigen capturas de iPad. Se
    contiene con una columna de 640 dp y dos o tres reglas de reparto (§5).

### Correcciones de datos hechas durante la investigación

Van aquí para que nadie las vuelva a buscar:

- Day One Silver cuesta **49,99 $/año**, no 34,99: la cifra baja de la investigación de mercado es de
  una fuente vieja; la corrigió ya el SPEC de Purl.
- Quilt se vende a **4,99 EUR**, no a 9,99 como dice su SPEC original. MoodTraker, 7,99 EUR.
- La investigación del método proponía dejar el Future Log para v1.1. Se adelanta a v1.0 porque el
  estado *programada* necesita un destino (§2.7).
- *Selvage* y *Selvedge* son la misma palabra en dos grafías: la reserva es Selvage y la colisión que
  encontró Purl es con Selvedge (§8).
- **FTS5 no viene compilada en la SQLite del sistema de Android** en la mayoría de dispositivos.
- Okio lee zips pero no los escribe, y solo en JVM.
- `androidx.biometric` no tiene versión estable desde la 1.1.0 (2021).
- `setRecentsScreenshotEnabled(false)` existe desde Android 13 y oculta la multitarea sin bloquear
  las capturas, que es lo que hace `FLAG_SECURE`.

---

## Fuentes

- [What is the Bullet Journal Method?, bulletjournal.com](https://bulletjournal.com/blogs/faq/what-is-the-bullet-journal-method)
- [What is Rapid Logging?, bulletjournal.com](https://bulletjournal.com/blogs/faq/what-is-rapid-logging-understand-rapid-logging-bullets-and-signifiers)
- [Migration 101, bulletjournal.com](https://bulletjournal.com/blogs/faq/migration)
- [How to Bullet Journal, bulletjournal.com](https://bulletjournal.com/pages/how-to-bullet-journal)
- [Index mods and variations, bulletjournal.com](https://bulletjournal.com/blogs/bulletjournalist/index-mods-and-variations)
- [Keyword teardown "bullet journal", appfigures](https://appfigures.com/resources/keyword-teardowns/101-bullet-journal/amp)
- [Reseña de Bullet Journal Companion, Ink Journal](https://www.inkjournal.com/blogs/news/bullet-journal-companion-app-review)
- [Mejores apps de diario 2026, Hello Diary](https://www.hellodeardiary.com/guides/best-diary-app-2026.html)
- [Reseñas de Daylio, JustUseApp](https://justuseapp.com/en/app/1194023242/daylio-journal/reviews)
- [Apps de Bullet Journal, Bernard Zitzer](https://bernardzitzer.com/list-best-bullet-journal-apps/)
- [Apps de diario digital, ClickUp](https://clickup.com/blog/digital-journal-apps/)
- [Reseñas de TickTick, Capterra](https://www.capterra.com/p/170641/TickTick/reviews/?page=2)
- [Some thoughts about the Bullet Journal, progresspunk](https://progresspunk.substack.com/p/some-thoughts-about-the-bullet-journal)
- [Day One, planes y precios](https://dayoneapp.com/plans/)
- [Trader status del DSA, Apple Developer](https://developer.apple.com/news/upcoming-requirements/?id=02172025a)
- [Small Business Program, Apple](https://developer.apple.com/app-store/small-business-program/)
- [12 probadores y 14 días, Play Console Help](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Clasificación por edad, Apple Developer](https://developer.apple.com/news/?id=ks775ehf)
- [Auto Backup, Android Developers](https://developer.android.com/identity/data/autobackup)
- [Data Safety con RevenueCat](https://www.revenuecat.com/docs/platform-resources/google-platform-resources/google-plays-data-safety)
- [Data Safety: qué es recogida y la excepción del cifrado de extremo a extremo, Play Console Help](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [FTS5 no disponible en Android, SQLDelight #1977](https://github.com/sqldelight/sqldelight/issues/1977)
- [Protección de ficheros hasta la primera autenticación, Apple](https://developer.apple.com/documentation/foundation/nsdata/writingoptions/completefileprotectionuntilfirstuserauthentication)
- [Estadísticas de notificaciones push, Business of Apps](https://www.businessofapps.com/marketplace/push-notifications/research/push-notifications-statistics/)
- [Búsqueda de marcas de USPTO](https://tmsearch.uspto.gov)
- [TMview, marcas de la UE y nacionales](https://www.tmdn.org/tmview)
- [Literata, Google Fonts](https://fonts.google.com/specimen/Literata)
