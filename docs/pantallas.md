# Interfaz de Bobbin

Pantalla a pantalla, con su estado vacío, sus gestos y sus medidas. Lo que aquí no se dice se hace como
en Purl (`line/docs/pantallas.md`, `line/.../ui/`). El porqué de cada decisión está en `SPEC.md` §2 y
§5; los algoritmos que alimentan cada pantalla, en `docs/tecnico.md` 6.

Los textos se citan por su clave (`captureHint`) y su versión en español. `docs/textos.md` (#6) los
escribe en los cinco idiomas a partir de estas claves; si allí cambia una frase, manda `docs/textos.md`
y aquí se corrige la cita. Las medidas son `dp` en la app, `sp` en el texto, `px` en las imágenes de
compartir y `pt` en el libro. `u` es `gridUnit` (1.1).

---

## 1. Dirección de diseño

**Tesis: cada pantalla es una página del cuaderno punteado, y cada acción dice lo que va a pasar.**
Quien usa Bobbin escribe en su cuaderno; la interfaz se aparta, pero nunca obliga a adivinar. El
rediseño de v1.0 (prototipo "Bobbin en limpio") mantiene el papel, la tinta y los símbolos del método,
y añade tres cosas que el primer diseño daba por sabidas: una barra de escribir fija abajo, una hoja que
explica la consecuencia de cada acción y una guía de cuatro pasos en el primer arranque.

Las diez decisiones, enteras:

1. **Papel y rejilla.** El papel punteado de 24 dp (1.1) sigue debajo de todo: es el fondo de la
   página, no una regla que mida cada fila. Las filas de entrada miden 40 dp de diana y el texto
   respira; los puntos no compiten con la tinta.
2. **El bullet es un punto.** Los glifos del método (1.5), dibujados con `Canvas`, iguales en Android e
   iOS: tarea, punto; evento, círculo; nota, guion; hecha, aspa; pasada, `>`; llevada a otro mes, `<`;
   descartada, tachada. Los signifiers van en el margen izquierdo.
3. **Color.** Los `colorScheme` de la familia, sin rojo. La portada (1.2) pone el acento: el punto de
   hoy, el lavado de la pestaña activa, las tarjetas de aviso y el número de hoy en Mes. `primary` es el
   de las acciones: el botón lleno de la barra de escribir, los enlaces y los botones de las tarjetas.
4. **Tipografía.** Dos voces. La tinta del usuario y los títulos en Literata (`Ink`, `PageTitle`,
   `Heading`); la interfaz en la fuente del sistema (`Body`, `Label`, `Secondary`, `Eyebrow`).
5. **Navegación.** Cuatro pestañas abajo, cada una con su icono y su nombre: Hoy, Mes, Futuro,
   Índice. Buscar y Ajustes arriba a la derecha en las cuatro. Todo lo demás se abre encima y se cierra
   con atrás o con "< Volver".
6. **Escribir.** Una barra fija abajo en Hoy, Mes, Futuro y cada lista: tres fichas (Tarea, Evento,
   Nota), "En <dónde>" a la derecha, el campo y "Añadir". Siempre se ve dónde va a caer lo que se
   escribe. Los prefijos del método (`o `, `- `, `* `, `! `, `? `) siguen valiendo y encienden su
   ficha.
7. **Actuar.** Tocar el punto de una tarea la marca hecha; tocar el texto de cualquier entrada abre su
   hoja. La hoja nombra cada acción con palabras de todos los días y dice debajo qué pasará ("Se copia
   a mañana. Aquí queda una >."). Todo lo que cambia una entrada se puede deshacer desde el aviso de
   abajo.
8. **Páginas.** Cada página abre con su título, una línea que dice qué es y, en las que lo necesitan,
   una frase que explica para qué sirve. Hoy: el día. Mes: el calendario como lista de días y las
   tareas del mes. Futuro: los meses que vienen. Índice: meses, listas y seguimientos.
9. **Vacíos y avisos.** Un vacío dice qué hacer ("Nada escrito hoy. Escribe abajo para empezar."). Un
   aviso es una tarjeta sobre el lavado de la portada con su título, una frase y su botón. Nunca un
   modal, nunca una exclamación.
10. **Accesibilidad.** Cada entrada es un nodo con nombre y acciones; dianas de 40 a 48 dp; contraste
    AA en claro y oscuro; movimiento reducido respetado.

Y tres principios que cortan cualquier duda (SPEC §5): cero configuración antes de escribir, nada se
mueve solo, nada de números de progreso. La guía del primer arranque (13.1) no pide nada: se salta con
un toque y no vuelve.

### 1.1 Papel y rejilla

`ui/theme/Grid.kt` y `ui/theme/Paper.kt` (#17). `u` es la unidad vertical; lo horizontal se mide en
columnas fijas de 24 dp, que no crecen con la fuente para que el texto conserve su ancho de línea.

| Token | Valor |
|---|---|
| `gridUnit` (`u`) | `24.dp * max(1f, fontScale)` |
| Paso vertical de la rejilla | `u`. Toda altura de fila, interlineado y hueco vertical es un múltiplo de `u` |
| Paso horizontal | 24 dp fijos. Columnas de puntos en x = 12 + 24k desde el borde izquierdo de la página |
| Filas de puntos | en la línea base de `Ink` de cada fila: y = arriba + k * `u` + `b`, con `b` la línea base de `Ink` en su caja de línea, medida con `TextMeasurer` (unos 18 dp a escala 1, y crece con ella) |
| Punto | círculo de 1,5 dp de diámetro, `outlineVariant`. No crece con la fuente |
| Papel punteado (`dotted`, gratis y por defecto) | los puntos |
| Papel rayado (`lined`, Pro) | una línea de 1 dp en cada fila de puntos, de lado a lado, `outlineVariant` |
| Papel cuadrícula (`grid`, Pro) | el rayado más una línea vertical de 1 dp en cada columna de puntos |
| Papel liso (`blank`, Pro) | nada |
| Página en ancho (sección 21) | como mucho 24 columnas (576 dp), centrada, con el borde izquierdo en un múltiplo de 24 para que la rejilla de la página y la de los lados sean la misma |

- El papel cubre todas las pantallas completas, cabecera incluida, y se desplaza con el contenido: es
  la hoja, no un fondo fijo. No lo llevan los diálogos, las hojas inferiores, el bloqueo ni los avisos
  de carga (sección 16), que van sobre `surface` o `background` lisos.
- La barra de pestañas y la barra de escribir son `background` liso con una línea superior de 1 dp
  `outline`: tapan el papel que pasa por debajo.
- El papel es fondo: nunca cambia el tamaño ni la posición de una línea (SPEC §5).

### 1.2 Color

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `FBF8F3` | `17150F` | el papel |
| `onBackground` | `39352E` | `ECE5D9` | la tinta: texto del usuario, glifos, títulos, pestaña activa |
| `surface` | `FFFFFF` | `201D16` | diálogos, hojas inferiores, la entrada levantada al arrastrar |
| `surfaceVariant` | `F0EBE2` | `2C2820` | la fila con la hoja abierta, el día elegido en Mes, etiquetas pequeñas. Texto encima solo en `onBackground` |
| `onSurfaceVariant` | `736D63` | `9C9486` | texto secundario, etiquetas, tareas cerradas, pestañas inactivas, iconos |
| `outline` | `E3DCD1` | `3A352B` | bordes de 1 dp, anillos de las muestras |
| `outlineVariant` | `EFE9DF` | `2C2820` | los puntos y líneas del papel, separadores de semana, línea superior de las barras |
| `primary` | `3F7A69` | `8FC9B6` | botones llenos, enlaces, botones discretos e interruptores encendidos |
| `onPrimary` | `FFFFFF` | `12271F` | el pulgar del interruptor encendido |

`error` no se usa en ninguna pantalla. Por si un componente de Material lo busca solo, vale lo mismo
que `onSurfaceVariant`. Ningún borrado ni aviso se pinta en rojo ni en un pariente.

Contraste de cada par de texto y fondo que existe en la app (test 25 lo calcula en `commonTest`):

| Texto sobre fondo | Claro | Oscuro |
|---|---|---|
| `onBackground` sobre `background` | 11,5:1 | 14,6:1 |
| `onBackground` sobre `surface` | 12,2:1 | 13,4:1 |
| `onSurfaceVariant` sobre `background` | 4,8:1 | 6,1:1 |
| `onSurfaceVariant` sobre `surface` | 5,1:1 | 5,6:1 |
| `primary` sobre `background` | 4,7:1 | 9,7:1 |
| `primary` sobre `surface` | 5,0:1 | 9,0:1 |

`onSurfaceVariant` y `primary` sobre `surfaceVariant` bajan de 4,5:1 en claro (4,3 y 4,2): por eso
sobre `surfaceVariant` el texto es `onBackground`, o `Secondary` solo en lo que ya se lee en otra parte. Los puntos (`outlineVariant` sobre `background`, 1,1:1 en claro y
1,2:1 en oscuro) están por debajo a propósito: no compiten con la tinta.

**Portada.** La portada activa (`activeCover`, `docs/tecnico.md` 6.17; ids y hex en `docs/tecnico.md`
5) da el acento en dos intensidades: su color entero (`Cover.color`) en el punto de hoy de la cabecera,
el círculo del número de hoy en Mes, las celdas marcadas de un seguimiento y los widgets; y su lavado
(`coverSoft`: la portada mezclada con el papel, 35 % en claro y 16 % en oscuro) en el fondo de la
pestaña activa, de las tarjetas de aviso y de las pastillas de la guía. El texto sobre el lavado es
`onBackground`, con contraste AA en las ocho portadas. Nada más cambia de color con la portada.

### 1.3 Tipografía

`ui/theme/Type.kt`. Cada estilo lleva ya su color.

| Estilo | Fuente | Tamaño / interlineado | Peso | Color | Dónde |
|---|---|---|---|---|---|
| `Ink` | Literata | 17 / 24 sp | Regular | `onBackground` | cada entrada, el campo de la barra de escribir, filas del Índice, el texto de la hoja |
| `PageTitle` | Literata | 30 / 36 sp | Regular | `onBackground` | el título de cada página (el día, el mes, Futuro, Índice, el título de una lista) |
| `Heading` | Literata | 26 / 32 sp | Regular | `onBackground` | títulos de Revisar y de la guía, la tarea en grande de Revisar |
| `Body` | sistema | 15 / 24 sp | Normal | `onBackground` | filas de Ajustes, acciones de la hoja, tarjetas |
| `Label` | sistema | 15 / 20 sp | SemiBold | `onBackground` | botones, títulos de tarjeta |
| `Secondary` | sistema | 13 / 24 sp | Normal | `onSurfaceVariant` | subtítulos, explicaciones, lo que hará cada acción, "Pasada a..." |
| `Eyebrow` | sistema | 11 / 24 sp | Medium, 1,4 sp de espaciado, `uppercase()` | `onSurfaceVariant` | etiquetas de bloque, con su pista corta al lado en `Secondary` |

- Literata Regular es la única fuente empaquetada. Sin negrita ni cursiva en lo que escribe el usuario.
  Las cifras de Mes, en cifras tabulares.
- Los widgets usan la fuente del sistema (sección 18).

### 1.4 Medidas

| Qué | Valor |
|---|---|
| Margen de la cabecera | 22 dp (`HEAD_START`): título, subtítulo, explicación, etiquetas y tarjetas empiezan ahí |
| Fila de entrada | el margen de signifiers (22 dp), el glifo en una diana redonda de 40 x 40, y el texto en `Ink` con 8 dp arriba y abajo. Hace salto de línea, nunca se corta |
| Barra de escribir | fichas de 32 dp, campo de 44 dp con borde de 1 dp y radio 14, botón de 44 dp. Fondo `background` con línea superior de 1 dp `outline` |
| Barra de pestañas | 56 dp por pestaña más el borde del sistema: icono de 21 dp en una pastilla de 52 x 30 y el nombre en 12 sp |
| Botón principal | 50 dp de alto a todo el ancho (o 40 dp en línea), radio 25, `primary` con texto `onPrimary` en `Label` |
| Botón discreto | 44 dp de alto, texto `Label` en `primary`, sin fondo |
| Tarjeta de aviso | radio 18, fondo `coverSoft`, 16 dp de relleno; título en `Label`, frase en `Secondary`, botones en línea |
| Aviso de abajo | radio 14, fondo `onBackground`, texto `background`, 46 dp; `undo` en el color de la portada |
| Hojas | `ModalBottomSheet` sobre `surface`, radio 24 arriba, ancho máximo 576, asa de 36 x 4 |
| Diana | 48 x 48 dp los iconos, 40 x 40 el glifo de una entrada y cada fila, 44 los botones |
| Bordes | 1 dp `outline`. Sin elevación tonal; solo la tarjeta de la guía lleva sombra |

### 1.5 Glifos

`ui/BulletGlyph.kt` (#17), en `Canvas`, sobre una caja de 24 x 24 dp con origen arriba a la
izquierda. Trazo de 1,5 dp con extremos y uniones redondos. Color `onBackground` en todos los estados:
lo que baja a `onSurfaceVariant` es el texto, no el glifo. No crecen con la escala de fuente (SPEC §5).
Las mismas coordenadas, escaladas, sirven a la imagen de compartir (sección 17), a los widgets (sección
18) y al libro (23.6).

| Glifo | Dibujo en la caja de 24 | Nombre (`S.glyphName`) |
|---|---|---|
| Tarea abierta | círculo relleno de 5 dp de diámetro con centro (12, 12) | Tarea |
| Tarea hecha | el punto y encima el aspa: (8, 8) a (16, 16) y (16, 8) a (8, 16) | Tarea hecha |
| Tarea migrada | polilínea (10, 8) (14,5, 12) (10, 16), sin punto | Tarea migrada |
| Tarea programada | polilínea (14, 8) (9,5, 12) (14, 16), sin punto | Tarea programada |
| Tarea irrelevante | el punto, y la entrada entera tachada: una línea de 1,5 dp del color del texto a media altura de la x de cada línea, desde x 56 hasta el final del texto de esa línea | Tarea descartada |
| Evento | circunferencia de 8 dp de diámetro (por el centro del trazo) con centro (12, 12) | Evento |
| Nota | línea (8, 12) a (16, 12) | Nota |
| Prioridad | tres trazos de 8 dp que se cruzan en (12, 12): vertical, a 30 y a 150 grados | Prioridad |
| Inspiración | línea (12, 7,5) a (12, 13,5) y círculo relleno de 2 dp en (12, 16,5) | Inspiración |
| Explorar | un ojo: dos curvas cuadráticas de (6,5, 12) a (17,5, 12) con control en (12, 7,5) y en (12, 16,5), y pupila rellena de 3 dp en (12, 12) | Explorar |

El aspa se traza en 150 ms, primero de (8, 8) a (16, 16) y después la otra, lineal (1.6). Reabrir la
quita al instante.

### 1.6 Movimiento

Dos animaciones propias: el aspa de 1.5 al marcar hecha, entera al instante con movimiento reducido
del sistema, y los dibujos de la guía (13.1), que se repiten en bucle mientras se ven. Cambiar de pestaña, de día, de mes o de paso de
revisión es instantáneo. Lo demás es del sistema: el teclado, las hojas, los diálogos.

### 1.7 Tono de los textos

Frases cortas, en presente, sin exclamaciones, sin reproches ("nunca has...", "vas a perder...") y sin
números de progreso. Un aviso dice qué pasa y, si hay algo que hacer, lo ofrece como acción de texto.
Los símbolos del método no se traducen.

---

## 2. Iconos

`ui/Icons.kt`: dibujados con `Canvas` sobre 24 unidades, trazo de 1,7 unidades con extremos y uniones
redondos, color `onSurfaceVariant` salvo que se diga otro. 22 dp dentro de un botón de 48
(`GlyphButton`), o sueltos (`GlyphIcon`) en filas y pestañas. Son iconos de la interfaz; los del método
son glifos (1.5).

| Icono | Dibujo | Dónde |
|---|---|---|
| `BACK`, `FORWARD` | ángulo a la izquierda o a la derecha | flechas de día y de mes; `BACK` en "< Volver"; `FORWARD` al final de una fila del Índice |
| `CLOSE` | aspa | cerrar Revisar y las hojas |
| `SHARE` | flecha hacia arriba saliendo de una bandeja | "Compartir este día / mes / lista" al pie de la página |
| `SETTINGS` | engranaje de ocho dientes con su anillo | arriba a la derecha de las cuatro pestañas (`a11ySettings`) |
| `SEARCH` | lupa | arriba a la derecha de las cuatro pestañas (`a11ySearch`) |
| `MORE` | tres puntos | lista y seguimiento (`a11yMoreActions`) |
| `CHECK` | marca | una marca de la hoja que está puesta |
| `TODAY`, `MONTH`, `FUTURE`, `INDEX` | una hoja con su punto, un calendario, un calendario con un `>`, una lista | las cuatro pestañas; `MONTH` en las filas de mes del Índice |
| `EDIT`, `TRASH`, `PLUS`, `LIST`, `GRID`, `BOOK`, `LATER` | lápiz, papelera, más, líneas, rejilla, libro, reloj | acciones de las hojas, filas del Índice, "Decidir luego" |

Un icono que no responde en un extremo (la flecha del día siguiente en mañana, la del mes siguiente en
el mes actual) no se pinta: nada al 30 % que parezca roto.

---

## 3. Estructura y navegación

```
App
+-- barra de pestañas: Hoy | Mes | Futuro | Índice      (Screen.TODAY, MONTH, FUTURE, INDEX)
+-- en pila, por encima de la barra:
|   +-- COLLECTION   (una lista o un seguimiento: desde el Índice, Buscar o el enlace de una pasada)
|   +-- REVIEW       (desde las tarjetas de Hoy y Mes, la notificación, bobbin://review)
|   +-- SEARCH       (icono SEARCH de las cuatro pestañas)
|   +-- SETTINGS     (icono SETTINGS de las cuatro pestañas)
|   +-- KEY          (desde Ajustes, sección Ayuda)
|   +-- GUIDE        (primer arranque, y desde Ajustes, sección Ayuda)
|   +-- PRO          (ProDialog sobre lo que lo abrió: un choque de docs/tecnico.md 6.16, la fila de Ajustes o bobbin://pro)
+-- la hoja de una entrada (5.6), una sola para toda la app, sobre cualquier página
+-- puertas, por encima de todo: bloqueo (sección 16) y avisos de carga (16.2)
```

Once destinos y ninguno más (SPEC §5): hojas inferiores y diálogos no son destinos, se abren sobre la
pantalla que los lanza y se cierran con atrás, con su `CLOSE` o tocando fuera.

- **Arranque.** La primera vez de la instalación, la guía (13.1); al acabarla o saltarla, Hoy con la
  barra de escribir enfocada. Las demás veces, directamente Hoy en el día lógico de hoy con el foco y
  el teclado. Sin splash con lógica, sin alta, sin paywall (test 37).
- **Atrás** (sistema y `BackHandler`): primero cierra el teclado; después la hoja o el diálogo
  abierto; después el día elegido en Mes o el mes elegido en Futuro; después la pantalla de la pila de
  arriba (la Clave vuelve a Ajustes); en Mes, Futuro o Índice vuelve a Hoy; en Hoy viendo otro día,
  vuelve a hoy; en Hoy, hoy, sale de la app.
- **Pestañas.** Abrir una pestaña enseña su página tal como se dejó en esta sesión, salvo Hoy, que
  siempre enfoca la barra de escribir (menos con la guía delante).
- **Teclado.** Con el teclado arriba, la barra de pestañas se esconde y la barra de escribir queda
  justo encima del teclado. Al bajarlo vuelve la barra de pestañas.
- **Enlaces** (`docs/tecnico.md` 7): `bobbin://today` abre Hoy en hoy; con `?focus`, además la barra
  de escribir enfocada con el teclado; `bobbin://review` abre REVIEW con alcance `Day(today)`;
  `bobbin://pro` abre Hoy con el `ProDialog` encima. Con el bloqueo puesto, el enlace espera a
  desbloquear. Al volver a primer plano se recalcula el día lógico: si cambió y se estaba viendo "hoy",
  Hoy pasa al día nuevo.

### 3.1 Barra de pestañas

Fondo `background` con línea superior de 1 dp `outline`, por encima del borde de navegación del
sistema. Cuatro pestañas de igual ancho (el de la página en tableta, sección 21): el icono en una
pastilla de 52 x 30 y debajo el nombre en 12 sp, `tabToday` (Hoy), `tabMonth` (Mes), `tabFuture`
(Futuro), `tabIndex` (Índice). La activa: pastilla con el lavado de la portada, icono y nombre en
`onBackground`, nombre en SemiBold. Las demás: sin pastilla, en `onSurfaceVariant`. Toda la celda
responde. Las etiquetas limitan su escala a 1,5 y nunca se cortan: `docs/textos.md` las mantiene de
una palabra en los cinco idiomas.

### 3.2 Cabecera de página

Parte de la página: se desplaza con ella.

- **Barra de arriba.** En las cuatro pestañas, `SEARCH` y `SETTINGS` a la derecha. En las superpuestas,
  "< Volver" (o "< Índice" en una lista), que es un botón con su texto y no solo un icono, y a la
  derecha lo propio de la página (`MORE` en lista y seguimiento). Revisar lleva `CLOSE`.
- **Título** en `PageTitle` desde 22 dp. En Hoy, si se ve hoy, un punto de 9 dp del color de la
  portada delante. A la derecha, las flechas de día o de mes cuando la página las tiene.
- **Subtítulo** en `Secondary`: qué día es respecto a hoy (`daySubtitle`: Hoy, septiembre de 2026), el
  año y si el mes ya pasó (`monthSubtitle`), cuántos meses enseña Futuro, qué es el Índice, "Lista".
- **Acción de vuelta**, cuando se está lejos: `backToToday` (Volver a hoy) o `backToMonth` (Volver a
  septiembre), como enlace en `primary` bajo el subtítulo.
- **Explicación**, en Mes, Futuro y seguimiento: una o dos frases en `Secondary` que dicen para qué
  sirve la página (`monthExplain`, `futureExplain`, `trackerExplain`).

### 3.3 Tarjetas de aviso

Bajo la cabecera de Hoy y de Mes, antes de la lista, las que apliquen, una debajo de otra, en el orden
de cada pantalla (6.3, 7.2). Cada una es una tarjeta con el lavado de la portada: un título en `Label`
que dice qué pasa ("Agosto tiene 4 tareas sin cerrar"), una frase en `Secondary` que dice por qué
importa, y sus botones (el principal lleno, el otro discreto). Nunca un modal (SPEC §6).

---

## 4. Los gestos

Pocos y visibles. Todos tienen además una acción del lector de pantalla (sección 22).

| Gesto | Qué hace | Dónde vale |
|---|---|---|
| 1. **Tocar el glifo** | Una tarea `OPEN` pasa a `DONE` (el aspa se traza, 1.6) con el aviso "Hecha." y `undo`; una `DONE` vuelve a `OPEN` igual. En una pasada o llevada a otro mes, lleva a la copia (5.5). En una descartada, un evento o una nota, abre la hoja | toda lista de entradas: Hoy, Mes, Futuro, lista, Buscar |
| 2. **Tocar el texto** | Abre la hoja de la entrada (5.6), con la fila marcada en `surfaceVariant` mientras está abierta | las mismas listas |
| 3. **Deslizar en horizontal el título de Hoy** | Cambia de día. Hacia la derecha, el anterior; hacia la izquierda, el siguiente, hasta mañana. Cuenta si el dedo recorre 72 dp; el cambio es instantáneo | Hoy (6) |
| 4. **Mantener y arrastrar** | Mantener pulsada una entrada y moverla más de 12 dp la levanta: se pinta sobre `surface` con borde de 1 dp y las demás le hacen sitio. Al soltar, `reorder(place, ids)`. Mantener sin mover abre la hoja | dentro del mismo lugar: un día, un día del Mes, las tareas del mes, un bloque de Futuro, una lista. No cruza de un lugar a otro: eso es pasar, y se decide en la hoja |

Editar el texto es una acción de la hoja (`actionEdit`), no un gesto.

---

## 5. La entrada, la barra de escribir y la hoja

`ui/EntryList.kt`, `ui/Composer.kt` y `ui/EntrySheet.kt`: los mismos componentes en Hoy, Mes, Futuro,
lista y Buscar.

### 5.1 Una entrada

```
 margen  glifo   texto
 |  !*    ( o )  Cena con Ana en el sitio de siempre,          |   Ink
 |               a las nueve                                    |   salto de línea
 |               Pasada a mañana, jueves 24                     |   Secondary, el lugar subrayado en primary
```

- Signifiers en el margen, a la izquierda del glifo. El glifo, en su diana redonda de 40 dp.
- Texto en `Ink`: `onBackground` si está abierta o es evento o nota; `onSurfaceVariant` si es una
  tarea hecha, pasada o llevada a otro mes; tachado si está descartada.
- **Debajo, a dónde fue o de dónde vino**, en `Secondary`: una tarea pasada lleva `movedTo(lugar)`
  (Pasada a mañana, jueves 24) y una llevada a otro mes `scheduledTo(lugar)` (Llevada a octubre, en
  Futuro), con el lugar subrayado en `primary`: tocarlo lleva a la copia. La copia lleva
  `cameFrom(lugar)` (Viene de ayer, martes 22). El lugar se nombra con `placeLabel` (hoy, mañana, el
  lunes 28, las tareas de septiembre, la lista Lecturas). Si la copia ya no existe, nada.
- Los esqueletos (`gone`) no se pintan. Orden: `order`, después `createdAt`, después `id`.

### 5.2 La barra de escribir

`Composer`, fija abajo en Hoy, Mes, Futuro y cada lista, encima de la barra de pestañas o del teclado.

```
+------------------------------------------------+
| (. Tarea) (o Evento) (- Nota)       En  hoy    |  fichas; a la derecha, dónde cae
| [día] [ Nueva tarea...                ] [Añadir]|  el día solo en Futuro
+------------------------------------------------+
```

- **Fichas.** Tarea, Evento y Nota, cada una con su glifo. La elegida, rellena de tinta. Un prefijo
  escrito (`o `, `- `) gana y enciende su ficha al momento; tras guardar vuelve a la sugerida de la
  página (Tarea; Evento en un día de Mes). Los signifiers se escriben con su prefijo o se ponen luego
  desde la hoja.
- **Dónde cae**, a la derecha: `composeFor` (En) y el lugar en negrita (`targetLabel`: hoy, tareas de
  septiembre, el 30 de septiembre, octubre, el título de la lista). Cambia en cuanto se elige otro día
  en Mes o otro mes en Futuro.
- **Campo** de 44 dp con borde, `Ink`, pista `composeHint` según la ficha (Nueva tarea...). Una línea
  lógica; un salto de línea pegado se convierte en espacio. Tope de 500 (`docs/tecnico.md` 6.2); desde
  `COUNTER_FROM` (450), `counter(n, 500)` en `Secondary` sobre el campo.
- **Añadir** (`add`), botón lleno. Intro hace lo mismo y deja el foco en el campo, sin bajar el
  teclado. Vacío o solo prefijos, no hace nada.
- **Al guardar**, el aviso de abajo dice `added(tipo, lugar)` (Tarea añadida en hoy.) y la página se
  desplaza hasta la línea nueva.
- En Futuro, antes del campo, el día del mes elegido: dos cifras con la pista `dayField` (día); vacío,
  sin día. Un día que no está en ese mes no guarda y avisa `dayOutOfRange`.

### 5.3 La fila de accesorios

No existe en el rediseño: las fichas de la barra de escribir hacen su papel y están siempre a la vista.

### 5.4 Editar

`actionEdit` (Editar el texto) en la hoja cambia su contenido por un campo con el texto, el cursor al
final y `save` (Guardar). Mismo estilo y mismo tope (`limitEdit`), sin prefijos: aquí `- ` es texto.
Vacío, no guarda. Guardar enseña `toastSaved` con `undo`.

### 5.5 El enlace de una pasada o llevada a otro mes

Tocar el glifo `>` o `<`, o el lugar subrayado de su línea, lleva a la copia: su día en Hoy, su día o
sus tareas en Mes, su bloque en Futuro o su lista, con la página desplazada hasta ella. En la hoja,
`actionGoToCopy` (Ir a la copia) hace lo mismo y dice dónde está (`copyIsAt`).

### 5.6 La hoja de una entrada

`EntrySheetHost`, una sola para toda la app. Arriba, qué es (`entryKind`: Tarea pendiente, Evento,
Nota con prioridad...) en `Eyebrow`, el texto en `Ink` y `CLOSE`. Debajo, una fila por acción: su icono
(el glifo que dejará en la página), su nombre en `Body` y, debajo del nombre, **lo que pasará**, en
`Secondary`.

| La entrada es | Acciones, en este orden |
|---|---|
| Tarea abierta | `actionDone` (Hecha: `doneHow`); el paso con nombre, `moveTomorrow` (Pasar a mañana) si es de hoy, `moveNextDay` (Pasar al día siguiente) si es de un día que viene, `moveToday` (Pasar a hoy) si es de antes o de Mes o Futuro, con `copiesTo(lugar)` y `leavesMark(">")`; `moveOtherMonth` (Llevar a otro mes: `otherMonthHow` y `leavesMark("<")`); `moveElsewhere` (Pasar a otro sitio: `elsewhereHow`); `actionDiscard` (Descartar: `discardHow`) |
| Tarea hecha o descartada | `actionReopen` (Reabrir: `reopenHow`) |
| Tarea pasada o llevada a otro mes | `actionGoToCopy` (Ir a la copia: `copyIsAt`), solo si la copia existe |
| Evento o nota | nada de estados (#22) |

Después, en todas: `priorityOn` o `priorityOff` (Marcar como prioridad / Quitar la prioridad, con su
`*How`), `otherMarks` (Otras marcas del margen: inspiración y explorar, cada una con `CHECK` si está
puesta), `actionEdit` y `actionDelete` (Borrar: `deleteHow`).

- **Llevar a otro mes** cambia el contenido por los meses que vienen, uno por fila con `waitsIn(mes)`,
  seis y `showMoreMonths` hasta 24, y `backToOptions` (Volver a las opciones).
- **Pasar a otro sitio**: `toToday`, `toTomorrow`, `toThisMonth` (Tareas de este mes), `toDayOfMonth`
  (Un día de este mes, con el campo `dayField` y `moveAction` Pasar) y `toCollection` (A una lista: las
  listas no archivadas, por orden de creación). Un destino que la función rechazaría (el mismo lugar, un
  día pasado) no se pinta. Un día que no está en el mes o ya pasó avisa `dayOutOfRange` o `dayPast`.
- Cada acción cierra la hoja, hace su cambio con `undoable` y enseña su aviso (5.8). Marcar y quitar
  marcas deja la hoja abierta. Nunca un calendario visual (#25).

### 5.7 El selector de destino

Es el contenido de "Llevar a otro mes" y "Pasar a otro sitio" en la hoja (5.6), el mismo en Revisar.

### 5.8 El aviso de abajo

`ToastLine`, flotando sobre el pie de la página y encima de la barra de escribir, durante `UNDO_MS`
(5 s): una pastilla de tinta con el texto en `background` y, si se puede deshacer, `undo` (Deshacer)
en el color de la portada. Dice lo que acaba de pasar con el mismo verbo de la acción: `toastDone`
(Hecha.), `movedTo(lugar)` (Pasada a mañana, jueves 24.), `toastScheduled(mes)`, `toastDiscarded`,
`toastPriorityOn`, `toastSaved`, `entryDeleted`, `collectionDeleted`, `rowDeleted`; y sin deshacer,
lo que se añadió (`added`) o se creó (`listCreated`). Un aviso nuevo sustituye al anterior y deja firme
lo que decía. Deshacer vuelve exactamente a antes si nada más cambió; si algo cambió entre medias,
devuelve solo las entradas que tocó la acción y quita la copia que hubiera creado (`docs/tecnico.md`
6.4).

---

## 6. Hoy

`ui/TodayScreen.kt` (#21). El Daily Log: `ofDay(d)` del día que se ve (`docs/tecnico.md` 6.5).

### 6.1 Composición

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|  o Miércoles 23                       [<] [>]  |  PageTitle, punto de portada
|    Hoy, septiembre de 2026                     |  daySubtitle
|  +------------------------------------------+  |
|  | Agosto tiene 4 tareas sin cerrar         |  |  tarjetas (6.3)
|  | Decide qué pasa con cada una...          |  |
|  | [Repasar agosto]                         |  |
|  +------------------------------------------+  |
| *  .  Llamar al fontanero                      |  el día
|    o  Cena con Ana                             |
|    x  Comprar tinta                            |
|  +-- - - - - - - - - - - - - - - - - - - - --+  |
|  | Toca el punto de una tarea para...  Vale |  |  pista, una vez
|  +-- - - - - - - - - - - - - - - - - - - - --+  |
|    EN EL CALENDARIO                            |  si hay Monthly(mes, día)
|    .  Pagar el alquiler                        |
|    ^ Compartir este día                        |
+------------------------------------------------+
| (. Tarea) (o Evento) (- Nota)       En hoy     |  barra de escribir (5.2)
| [ Nueva tarea...                    ] [Añadir] |
+------------------------------------------------+
|  Hoy     Mes     Futuro     Índice             |
+------------------------------------------------+
```

- **Cabecera.** Título `dayTitle(d)`; si `d` es hoy, el punto de la portada. `BACK` y `FORWARD`.
  Subtítulo `daySubtitle(d, hoy)`. Si `d` no es hoy, `backToToday` debajo. Deslizar el título cambia de
  día (gesto 3).
- **Días que se ven.** Hacia atrás, sin límite. Hacia delante, hasta mañana: `Daily(mañana)` es destino
  de "Pasar a mañana". En mañana, `FORWARD` no se pinta.
- **Lista.** Las entradas de `Daily(d)`. La barra de escribir escribe en `Daily(d)`.
- **El calendario del mes.** Si hay `Monthly(monthOf(d), d.day)`, `calendarToday` (EN EL CALENDARIO)
  en `Eyebrow` y esas entradas. Arrastrar no las mezcla con las del día.
- **Compartir.** Si el día tiene algo, `shareDay` (Compartir este día) al pie, con `SHARE` (sección 17).
- **Foco.** Al abrir Hoy (arranque, pestaña, enlace con `focus`, fin de la guía) la barra de escribir
  tiene el foco y el teclado sube sin ningún toque, también con el día lleno. Mientras la guía está
  delante, no.
- Nunca se copian las tareas abiertas de ayer: están en su día, y las tarjetas lo dicen (6.3).

### 6.2 Estados

| Estado | Qué se ve |
|---|---|
| A. Primera vez | La guía (13.1). Al acabarla, Hoy vacío con `dayEmpty` y `writeBelow` (Nada escrito hoy. Escribe abajo para empezar.) y la barra enfocada |
| B. Día vacío | `dayEmpty(d, hoy)` y `writeBelow` en `Secondary`. Nada invita a configurar nada |
| C. Día con entradas | La lista; la pista `hintTap` en una tarjeta de borde discontinuo, hasta que se toca `gotIt` (Entendido), el punto de una tarea o el texto de una entrada (`hintSeen`, `docs/tecnico.md` 7) |
| D. Otro día | Igual que C o B, sin tarjetas, con `backToToday`. Un día pasado se escribe igual que hoy |
| E. Tras el primer bullet de la instalación | La oferta del recordatorio (6.3), una sola vez |

### 6.3 Tarjetas de Hoy

Solo cuando se ve hoy. Todas las que apliquen, en este orden:

| Tarjeta | Cuándo | Título, frase y botones |
|---|---|---|
| Diario dañado | la carga puso el diario en cuarentena (`docs/tecnico.md` 6.14), hasta que se descarte | `noticeCorrupt` y `ok` |
| Guardado fallido | `saveFailed`, hasta el siguiente guardado bueno | `noticeSaveFailed`, sin botón |
| Oferta del recordatorio | tras guardar el primer bullet, si `reminderOffered` es falso; se pone a cierto al enseñarla | `offerReminder` (¿Te aviso para repasar el día a las 21:00?) con `yes` y `notNow`. `yes` pide el permiso (`docs/tecnico.md` 6.12). Se va al contestar, al cambiar de día o al cerrar la app, y no vuelve |
| Mes sin cerrar | `unclosedMonth(hoy)` no es nulo | `unclosedMonth(mes, n)` (Agosto tiene 4 tareas sin cerrar), `unclosedBody` y `reviewMonth(mes)` (Repasar agosto): abre REVIEW `Month(m)` |
| Tareas de días anteriores | `openTasksBefore(hoy)` no vacía | `earlierOpen(n)` (Quedan 3 tareas abiertas de días anteriores), `earlierBody` y `reviewEarlier`: abre REVIEW `Earlier(hoy)` |

---

## 7. Mes

`ui/MonthScreen.kt` (#24, #26). El Monthly Log de `m`: el calendario como lista y las tareas del mes
(`docs/tecnico.md` 6.5).

### 7.1 Composición

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|    Septiembre                         [<] [>]  |  PageTitle
|    2026                                        |  monthSubtitle
|    El calendario del mes y sus tareas...       |  monthExplain
|  [tarjetas (7.2)]                              |
|    CALENDARIO   Toca un día para apuntar en él |  Eyebrow y pista
|  L  (1)                                        |  inicial y número
|  M  (2)                                        |
|  X  (3)  o  Dentista, 17:30                    |
|  ------------------------------------------    |  separador de semana
|  ...                                           |
|  X (23)  Aquí se apunta                        |  el día elegido, sobre surfaceVariant
|    TAREAS DEL MES                              |
|       .  Renovar el pasaporte                  |
|    ^ Compartir este mes                        |
+------------------------------------------------+
| (. Tarea) (o Evento) (- Nota)  En el 23 de ... |  barra de escribir
+------------------------------------------------+
```

- **Cabecera.** Título `monthName(m)`, subtítulo `monthSubtitle(m, pasado)` y la explicación
  `monthExplain`. `BACK` y `FORWARD` cambian de mes: hacia atrás hasta el mes más antiguo con
  contenido, hacia delante hasta el actual (los que vienen son de Futuro). En un mes pasado,
  `backToMonth(actual)`. Se abre en el mes actual, desplazado para que la fila de hoy quede a la vista
  con la semana que sigue debajo.
- **Calendario.** `calendarTitle` (CALENDARIO) con la pista `calendarHint`. Una fila por día de 1 a
  `monthDays(m)`: la inicial del día de la semana y el número en un círculo de 26 dp; hoy, sobre el
  color de la portada; sábado y domingo en `onSurfaceVariant`. Con entradas, van a la derecha del
  número. Antes de cada inicio de semana (ajuste o sistema) salvo el 1, una línea de 1 dp.
- **Elegir un día.** Tocar la fila de un día la marca en `surfaceVariant`, escribe `writingHere` (Aquí
  se apunta) si está vacía, enfoca la barra de escribir y la pone en ese día con la ficha Evento
  sugerida. Tocarlo otra vez, o atrás, lo suelta: la barra vuelve a las tareas del mes.
- **Tareas del mes.** `monthTasks` (TAREAS DEL MES) y las `Monthly(m, null)`; vacías,
  `monthTasksEmpty`. Una entrada con día nunca aparece aquí.
- **Compartir.** `shareMonth` al pie si el mes tiene algo.
- Mes se abre sin foco ni teclado: se lee antes de escribir.

### 7.2 Tarjetas de Mes

Solo en el mes actual, en este orden:

| Tarjeta | Cuándo | Botón |
|---|---|---|
| `unclosedMonth(mes, n)` con `unclosedBody` | `unclosedMonth(hoy)` no es nulo | `reviewMonth(mes)`: REVIEW `Month(m)` |
| `futureWaiting(n)` (2 entradas de Futuro esperan a este mes) con `futureWaitingBody` | `futureWaiting(hoy)` no vacía y `settings.futureSeen != monthOf(hoy)` | `reviewFuture`: revisión de Futuro (11.4) |

### 7.3 Estados

| Estado | Qué se ve |
|---|---|
| Mes vacío | Las filas de todos los días y `monthTasksEmpty`. El mes nace vacío: nada del anterior se copia ni se sugiere |
| Mes pasado | Sin tarjetas, con `backToMonth`. Se escribe igual |

---

## 8. Futuro

`ui/FutureScreen.kt` (#25). El Future Log: bloques de mes desde el siguiente al actual.

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|    Futuro                                      |
|    Los próximos 6 meses                        |  futureSubtitle
|    Apunta lo que aún no es de este mes...      |  futureExplain
|  +------------------------------------------+  |
|  | Octubre 2026               Aquí se apunta |  |  el mes elegido
|  |    o  Cumpleaños de Ana                   |  |
|  |       Día 14                              |  |  onDay
|  |    .  Revisar el seguro                   |  |
|  +------------------------------------------+  |
|    Noviembre 2026                              |
|    Nada todavía.                               |  futureEmpty
|    Ver más meses                               |
+------------------------------------------------+
| (. Tarea) ...                     En octubre   |
| [día] [ Nueva tarea...          ] [Añadir]     |
+------------------------------------------------+
```

- Título `tabFuture`, subtítulo `futureSubtitle(n)`, explicación `futureExplain`. `futureMonths(hoy,
  n)`: seis bloques, y `showMoreMonths` añade seis cada vez hasta 24.
- **Bloque.** Cabecera con `monthTitle(m)` en `Ink`; las `Future(m, día)` por día ascendente, cada una
  con `onDay(día)` debajo; después las `Future(m, null)` por `order`, que se arrastran entre ellas.
  Vacío, `futureEmpty`.
- **Elegir un mes.** Tocar la cabecera de un bloque lo marca (`writingHere`) y pone en él la barra de
  escribir, que en Futuro lleva el campo del día (5.2). Por defecto, el mes siguiente.
- El Future Log no avisa ni mueve nada solo; al llegar su mes, Mes lo dice (7.2). Futuro se abre sin
  foco. "Llevar a otro mes" aterriza aquí, y el enlace de una llevada abre su bloque aunque esté más
  lejos de los seis abiertos, con el tope de 24.

---

## 9. Índice

`ui/IndexScreen.kt` (#29, #30). Meses con contenido, listas y seguimientos, cada grupo por orden de
creación (`indexItems`, `docs/tecnico.md` 6.7).

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|    Índice                                      |
|    Todo tu diario, en un sitio                 |  indexSubtitle
|    MESES                                       |
|    [cal] Septiembre 2026                    >  |
|    LISTAS   Viajes, ideas, libros: lo que...   |  Eyebrow y pista
|    [=]   Lecturas 2026                      >  |
|    +     Nueva lista                           |
|    SEGUIMIENTOS   Una fila por hábito...       |
|    [#]   Agua                               >  |
|    +     Nuevo seguimiento               PRO   |
|    Archivadas (2)                              |
+------------------------------------------------+
```

- **Grupos** `indexMonths`, `indexLists` e `indexTrackers`, cada uno con su pista corta al lado
  (`indexListsHint`, `indexTrackersHint`). Sin meses, `indexEmpty` bajo MESES.
- **Fila**: el icono de lo que es (`MONTH`, `LIST`, `GRID`), el título en `Ink` (hace salto de línea,
  nunca se corta) y `FORWARD`. Sin números de página. Tocar un mes abre la pestaña Mes en ese mes;
  tocar una lista o un seguimiento abre COLLECTION.
- **Crear.** Al final de cada grupo, `newList` (Nueva lista) o `newTracker` (Nuevo seguimiento) en
  `primary` con `PLUS`. Tocarla la cambia por un campo con la pista `listNameHint` o
  `trackerNameHint` y `create` (Crear): crear una lista la abre con su barra de escribir enfocada y
  avisa `listCreated`; un seguimiento abre con el campo de su primera fila. Sin Pro y con un
  seguimiento ya creado (`canCreateTracker` falso), la fila lleva una etiqueta PRO y abre el
  `ProDialog`; no se crea nada.
- **Archivadas.** Al final, `archivedToggle(n)` como botón discreto, plegado. Tocarlo despliega las
  archivadas en `onSurfaceVariant`. Se pliega al salir.

En ancho (sección 21) las filas se reparten en doble página.

---

## 10. Lista y seguimiento

`ui/CollectionScreen.kt` (#30, #50).

### 10.1 Lista

```
+------------------------------------------------+
|  < Índice                                [...] |
|    Lecturas 2026                               |  PageTitle, se toca para renombrar
|    Lista                                       |  listSubtitle
|    .  El infinito en un junco                  |
|    x  Klara y el Sol                           |
+------------------------------------------------+
| (. Tarea) (o Evento) (- Nota)   En Lecturas... |
+------------------------------------------------+
```

- **Cabecera.** "< Índice" y `MORE`. Título en `PageTitle`; tocarlo lo cambia por un campo
  (`renameCollection`, `oneLine`, tope `COLLECTION_TITLE_MAX` 60); vacío, no guarda. Renombrar no
  cambia su puesto en el Índice. Archivada, el subtítulo añade `archivedNote`.
- **Lista y barra de escribir** como en Hoy, en `InCollection(id)`. Enfocada si viene de crearla.
  Vacía, `listEmpty`.
- **`MORE`** abre una hoja con `shareList` (si tiene entradas), `rename` (Cambiar el nombre),
  `archive` o `unarchive`, y `deleteCollection` (Borrar, con `deleteHow`). Archivar no toca ninguna
  entrada y vuelve al Índice. Borrar no pide confirmación: vuelve al Índice con el aviso
  `collectionDeleted` y `undo`. v1.1 añade `continueCollection` (23.1); v1.2, `iconAndTheme` (23.9).
- Una lista archivada se abre y se escribe igual; no es destino de "Pasar a otro sitio".

### 10.2 Seguimiento

Una `BulletCollection` con `kind = TRACKER` (`docs/tecnico.md` 6.18): filas que define el usuario, con
una marca por día.

```
+------------------------------------------------+
|  < Índice                                [...] |
|    Agua                               [<] [>]  |  páginas del hilo
|    Seguimiento de septiembre                   |  trackerSubtitle
|    Una fila por hábito, una casilla por día... |  trackerExplain
|               1  2  3  4  5  6  7  8 ...       |  días, se desliza en horizontal
|    Dos litros [x][ ][x][x][ ][ ][x][ ]         |  casillas de 28 dp
|    Estirar    [ ][x][ ][ ][x][ ][ ][ ]         |
|    + Nueva fila                                |
+------------------------------------------------+
```

- **Página.** Subtítulo `trackerSubtitle(mes)`; `BACK` y `FORWARD` van a la página anterior y a la
  siguiente del hilo, y no se pintan si no existen. Se abre en la página del mes actual
  (`trackerPage`, que no se guarda hasta la primera marca o el primer cambio de filas).
- **Rejilla.** Los nombres de las filas en una columna fija a la izquierda; a su derecha, una casilla
  de 28 dp por día del mes, con los números arriba, todo desplazable en horizontal. Marcada, rellena
  con el color de la portada; hoy, con borde. Un toque la alterna.
- **Filas.** Tocar el nombre de una fila abre su hoja: `rename`, `a11yMoveUp`, `a11yMoveDown` y
  `rowDelete` (con el aviso `rowDeleted` y `undo`). Al final, "+ Nueva fila" abre el campo
  `trackerRowHint` con `add`; un seguimiento recién creado lo trae abierto.
- **Pro.** Sin Pro, los seguimientos que ya existen se leen y se editan igual, y sus páginas nuevas se
  siguen creando (`docs/tecnico.md` 6.18).
- Sin números: ni "12 de 30", ni porcentajes, ni rachas. Sin compartir: la tabla sale en la
  exportación. `MORE` abre la hoja de 10.1 sin compartir.

| Estado | Qué se ve |
|---|---|
| Seguimiento nuevo | El título, el mes, la explicación y el campo de la primera fila enfocado |
| Página de un mes nuevo sin marcas | Las mismas filas con las casillas vacías: nada copiado del mes pasado |

---

## 11. Revisar

`ui/ReviewScreen.kt` (#26, #27, #28). Tres pasos: releer, decidir una tarea cada vez, y el final. Los
alcances y las consultas, en `docs/tecnico.md` 6.6.

Arriba, `CLOSE` a la izquierda (sale conservando lo decidido: cada decisión ya cambió su tarea) y, en
el paso de tareas, `taskOf(i, n)` (Tarea 2 de 4) con un punto por tarea, el actual alargado. Sin
barra de progreso ni porcentajes.

### 11.1 Paso 1, releer

```
+------------------------------------------------+
|  [x]                                           |
|    REPASAR AGOSTO                              |  Eyebrow: reviewMonth / reviewEarlier / reviewDay
|    Antes de decidir, relee agosto              |  Heading
|    Quedaron 4 tareas abiertas. Después...      |  rereadLead
|    LUNES 14                                    |  por día
|    .  Llamar al fontanero                      |  solo lectura
|    ...                                         |
|    Una nota sobre agosto (opcional)            |
|    [                                     ]     |  campo de nota
+------------------------------------------------+
|    [      Decidir las 4 tareas      ]          |  botón principal
|              Ahora no                          |
+------------------------------------------------+
```

- Título `rereadTitle(mes?)` y `rereadLead(n)`: dice cuántas quedaron y qué viene después.
- El periodo en modo lectura: por cada día con entradas, `dayTitle` y sus entradas; en un mes, después,
  `calendarTitle` con los días que tienen algo, y `monthTasks`. Sin gestos.
- La nota: `rereadNote(mes?)` y un campo con `rereadNoteHint`. Con texto, al seguir se guarda como
  `NOTE` en `Monthly(m, null)` o en `Monthly(mes, hoy.day)` (`docs/tecnico.md` 6.6).
- Abajo, `decideTasks(n)` (Decidir las 4 tareas; Terminar si no queda ninguna) y `notNow`, que cierra.

### 11.2 Paso 2, una tarea cada vez

```
+------------------------------------------------+
|  [x]  Tarea 2 de 4   . - . .                   |
|    Del lunes 14                                |  origen
|    Llamar al fontanero por la gotera           |  Heading
|    (Ya la has pasado 3 veces)                  |  timesMoved, desde 2
|    x  Hecha                                    |
|       Se queda en agosto, marcada con una x.   |
|    >  Pasar a hoy                              |
|       Se copia a hoy. En agosto queda una >.   |
|    <  Llevar a otro mes                        |
|    -.- Descartar                               |
|    ()  Decidir luego                           |
+------------------------------------------------+
```

- **Origen**: `fromDay(fecha)`, `fromMonthTasks(mes)` o `fromCalendar(fecha)`.
- **La tarea** en `Heading`; desde `MIGRATION_SHOWN_FROM` (2), `timesMoved(n)` en una etiqueta. Es la
  única presión, y es la del método.
- **Cinco salidas**, las de la hoja con su consecuencia escrita para este mes: `actionDone` con
  `doneStaysIn(mes)`; el paso con nombre (5.6) con `copiesTo` y `leavesMark(">", mes)`;
  `moveOtherMonth` con `leavesMark("<", mes)`, que cambia el contenido por los meses; `actionDiscard`; y
  `decideLater` (Decidir luego: `decideLaterHow`), que la deja sin tocar solo para esta revisión.
- Cada decisión enseña su aviso con `undo` y pasa a la siguiente al instante. Ningún botón decide más
  de una. El `n` se cuenta al abrir y no se mueve.

### 11.3 Fin

`reviewFinished` en `Eyebrow`, `reviewEndTitle(mes?, quedan)` (Agosto, repasado) y
`reviewEndLead(quedan)`, que recuerda dónde fue cada cosa o cuántas quedan para luego. Un botón,
`backToToday`, que cierra y abre Hoy en hoy. Si el alcance era un mes y no queda ninguna abierta, la
primera vez se pide la valoración al sistema (`docs/tecnico.md` 6.6), que decide si la enseña.

### 11.4 Revisión de Futuro

Desde la tarjeta `futureWaiting` de Mes. Sin paso de releer. Una entrada cada vez (tareas abiertas,
eventos y notas), `entryOf(i, n)` arriba, el origen `fromFuture(mes, día?)` y tres salidas:
`futureToCalendar` (Pasar al calendario: `goesTo(lugar)`), `futureLeave` (Dejarla: `futureLeaveHow`)
y `actionDiscard` (una tarea queda tachada, `discardHow`; un evento o una nota se borra, `deleteHow`,
con `undo`). Al final, `futureAllDecided` y `close`.

---

## 12. Buscar

`ui/SearchScreen.kt` (#35). Todo el diario en memoria (`search`, `docs/tecnico.md` 6.8).

```
+------------------------------------------------+
|  < Volver                                      |
|  [ cafe                                  (x) ] |  campo con borde, con el foco
|  (. Abiertas) (* Prioridad) (! Inspiración)    |  fichas de filtro
|  (o Explorar)                                  |
|    MARTES, 22 DE SEPTIEMBRE DE 2026            |  grupo, se toca
|    .  Café con Marta #trabajo                  |
+------------------------------------------------+
```

- **Campo** de 46 dp con borde, `Ink`, con el foco y el teclado al abrir, pista `searchHint`, `CLOSE`
  para vaciarlo. Los resultados salen mientras se escribe.
- **Filtros**: `filterOpen` y los tres signifiers, cada uno una ficha con su glifo; activa, rellena de
  tinta. Se combinan con Y.
- **Grupos.** Etiqueta en `Eyebrow` que se toca para abrir su página: un día (`longDateWithYear`) abre
  Hoy en ese día; `monthGroup(mes)` abre Mes; `futureGroup(mes)` abre Futuro; el título de una lista
  abre COLLECTION. Debajo, sus entradas con los gestos de siempre salvo arrastrar.

| Estado | Qué se ve |
|---|---|
| Sin texto ni filtros | `searchEmpty` |
| Sin resultados | `searchNothing` |
| Solo filtros | Todo lo que cumple los filtros |

---

## 13. Clave

`ui/KeyScreen.kt` (#31). La *key page* del cuaderno: una página de lectura, desde Ajustes (Ayuda).

```
+------------------------------------------------+
|  < Volver                                      |
|    Clave de símbolos                           |  keyTitle
|    Cada símbolo, lo que significa...           |  keyWhat
|    LO QUE APUNTAS                              |
|    .  Tarea      Algo que hacer...             |
|    o  Evento     ...                           |
|    -  Nota       ...                           |
|    QUÉ PASÓ CON UNA TAREA                      |
|    x  Hecha      ...                           |
|    >  Pasada     ... En el método: migrada.    |
|    <  Llevada a otro mes  ... programada.      |
|   -.- Descartada ...                           |
|    EN EL MARGEN                                |
|    *  Prioridad  ...                           |
|    Al escribir: empieza con o para un evento...|  keyPrefixes
+------------------------------------------------+
```

- Grupos `keyWrite`, `keyHappened` y `keyMargin`. Cada fila: el glifo, el nombre en `Body` y lo que
  significa en `Secondary` (`keyTask`, `keyEvent`, `keyNote`, `keyDone`, `keyMoved`, `keyOtherMonth`,
  `keyDiscarded`, `keyPriority`, `keyInspiration`, `keyExplore`). Pasada y llevada dicen su nombre en el
  método.
- Al pie, `keyPrefixes` con los prefijos en negrita.
- No es un tutorial: no tiene pasos ni se abre sola. Atrás vuelve a Ajustes.

### 13.1 Guía del primer arranque

`ui/Guide.kt`. Cuatro pasos, cada uno con un dibujo animado de una página inventada y una frase. No
pide nada: ni alta, ni permisos, ni ajustes. Se abre sola una vez por instalación (`guideSeen` en
`Prefs`, `docs/tecnico.md` 7) y desde Ajustes (`guideAgain`).

```
+------------------------------------------------+
|  Bobbin                               Saltar   |
|        +------------------------------+        |
|        | HOY                          |        |  el dibujo, en una tarjeta de surface
|        | .  Comprar tinta       Tarea |        |
|        | o  Cena con Ana       Evento |        |
|        | -  Abre a las 10        Nota |        |
|        +------------------------------+        |
|  Apunta en una línea                           |  Heading
|  Cada línea es una tarea, un evento o...       |  Secondary
|  = . . .                                       |  un punto por paso
|  [Atrás]  [          Siguiente           ]     |
+------------------------------------------------+
```

| Paso | Título | Dibujo |
|---|---|---|
| 1 | `guideTitles[0]` (Apunta en una línea) | las tres clases aparecen una tras otra con su nombre al lado |
| 2 | `guideTitles[1]` (Toca el punto cuando esté hecha) | un anillo pulsa sobre el punto, el aspa se traza y el texto pasa a gris |
| 3 | `guideTitles[2]` (Lo pendiente lo mueves tú) | la tarea de ayer cambia su punto por `>` y su copia aparece en hoy, con "Pasada" y "Copia" |
| 4 | `guideTitles[3]` (Una vez al mes, repasa) | el mes pasado con `guideUnclosed(4)` y cuatro salidas que se encienden por turnos: Hecha, A hoy, A otro mes, Descartar |

- `skip` (Saltar) arriba a la derecha en todos los pasos. `previous` (Atrás) desde el segundo;
  `next` (Siguiente) y, en el último, `guideStart` (Empezar a escribir). Atrás del sistema vuelve un
  paso o, en el primero, cierra.
- Saltar o empezar guardan `guideSeen`, abren Hoy en hoy y enfocan la barra de escribir.
- Los dibujos no son un nodo de accesibilidad; el lector lee el título, la frase y `guideStep(i, n)`.

---

## 14. Ajustes

`ui/SettingsScreen.kt` (#32, #33). Todo tiene valor por defecto: es la única pantalla que se puede no
visitar nunca. Todo lo que guarda va a `Journal.settings`, salvo Pro y lo visto de la ayuda (`Prefs`).

```
+------------------------------------------------+
|  < Volver                                      |
|    Ajustes                                     |  PageTitle
|    AYUDA                                       |
|    Ver la guía otra vez                        |
|    Los cuatro pasos del principio              |
|    Qué significa cada símbolo                  |
|    La clave del cuaderno...                    |
|    DÍA                                         |
|    El día empieza                              |
|    A las 04:00                                 |
|    ...                                         |
|    RECORDATORIO / PRIVACIDAD / CUADERNO        |
|    COPIA / BOBBIN PRO / MÁS APPS / ACERCA DE   |
|    Borrar todos los datos                      |
+------------------------------------------------+
```

Secciones con etiqueta `Eyebrow`, `2u` entre secciones. Cada fila, `2u` como mínimo: título en `Body`,
subtítulo en `Secondary`, interruptor a la derecha (`Switch` de Material3 con `primary`); toda la fila
responde. En ancho, la página de 576 centrada (sección 21).

| Fila | Estados |
|---|---|
| `guideAgain` (Ver la guía otra vez) | Subtítulo `guideAgainSub`. Abre la guía (13.1) |
| `keyWhat` | Subtítulo `keyRowSub`. Abre la Clave (13) |
| `dayStartRow` (El día empieza) | Subtítulo `dayStartAt(h)` (A las 04:00). Abre un diálogo con siete filas, de 00:00 a 06:00, marcada la actual con `CHECK`; tocar una la guarda y cierra. Cambiarlo no mueve ninguna entrada: solo qué día es hoy |
| `weekStartRow` (La semana empieza) | `weekStartSystem(día)` (El lunes, como el sistema) o `weekStartDay(día)` (El domingo). Diálogo con `weekStartFollowSystem` (Como el sistema) y los siete días |
| `reminderRow` (Recordatorio para repasar el día) | Apagado por defecto: `reminderOff` (Apagado). Encendido: `reminderAt(hora)` (A las 21:00); tocar la fila abre el `TimePicker` de Material3 en un diálogo con `ok` y `cancel`. Sin permiso: el interruptor apagado y `reminderDenied` (Las notificaciones de Bobbin están desactivadas en el sistema.) con la acción `openSystemSettings` (Abrir ajustes) (`docs/tecnico.md` 6.12) |
| `lockRow` (Bloquear el diario) | `lockSubtitle` (Pide tu cara, tu huella o el código del teléfono). Sin bloqueo de pantalla: interruptor desactivado y `lockUnavailable` (Pon un bloqueo de pantalla en el teléfono para usarlo.). Encender pide autenticar; si falla, sigue apagado |
| Cuaderno | 14.1 |
| `exportRow` (Exportar copia) | `exportSubtitle` (Un zip con tu diario en JSON y en Markdown). Sin entradas ni colecciones: `exportNothing` (Aún no hay nada que copiar.) y la fila no responde |
| `importRow` (Importar copia) | `importSubtitle` (Se junta con tu diario, sin borrar nada) |
| `proRow` (Bobbin Pro) | Sin Pro: `proSubtitle` (Portadas, papeles, seguimientos y widgets. Pago único); abre el `ProDialog`. Con Pro: `proOwned` (Comprado. Gracias.), no responde |
| `restoreRow` (Restaurar compra) | Siempre, también con Pro. Al terminar, `restoreDone` (Compra restaurada.) o `restoreNothing` (No hay ninguna compra que restaurar.) en un diálogo de un botón |
| Más apps | Solo si `SIBLINGS` tiene alguna con tienda en esta plataforma (`docs/tecnico.md` 6.16). Título el nombre, subtítulo su lema (`siblingPurl`, `siblingQuilt`, `siblingMood`); abre la tienda |
| `privacyRow` (Política de privacidad) | Abre `PRIVACY_URL` |
| `version(v)` (Versión 1.0.0) | No responde |
| `wipeRow` (Borrar todos los datos) | Sola al final, tras `2u`, en `Body` `onBackground`, sin rojo. Subtítulo `wipeSubtitle` (Deja el diario vacío en este teléfono. Bobbin Pro se conserva.). Diálogo de dos pasos (15.4) |

Secciones: `sectionHelp` (AYUDA), `sectionDay` (DÍA), `sectionReminder` (RECORDATORIO), `sectionPrivacy` (PRIVACIDAD),
`sectionNotebook` (CUADERNO), `sectionBackup` (COPIA), `sectionPro` (BOBBIN PRO), `sectionMoreApps`
(MÁS APPS), `sectionAbout` (ACERCA DE). v1.1 añade a CUADERNO `newNotebookRow` y `bookRow` (23.2,
23.6); v1.2, `yearSummaryRow` y a COPIA `syncRow` (23.7, 23.8).

### 14.1 Portada y papel

(#49.) Todas se previsualizan sin comprar.

- **Vista previa.** Una página de muestra del ancho de la columna de texto y `8u` de alto, radio 12,
  borde 1 dp `outline`, con el papel y la portada que se están mirando: el punto de la portada y
  `dayTitle` de un miércoles 23 (Miércoles 23) en `PageTitle`, dos entradas fijas (`previewTask`: Comprar tinta, tarea;
  `previewEvent`: Cena con Ana, evento) y la pestaña `tabToday` (Hoy) activa, con el lavado de la portada (3.1).
- **Portadas.** Ocho círculos de 32 dp en dianas de 48, en dos filas de cuatro, en el orden de
  `docs/tecnico.md` 5. El que se está mirando lleva un anillo de 2 dp `onBackground` a 3 dp; el
  guardado, `CHECK` en `onBackground` al 70 % dentro. "Guardado" es el que pinta la app
  (`activeCover`, `activePaper`): sin Pro tras un reembolso, Salvia y punteado. Cada uno se describe con `coverName(id)` y, si es
  el guardado, `a11ySelected` (elegida).
- **Papeles.** Cuatro muestras de 32 x 32, radio 6, borde 1 dp `outline`, con su dibujo en miniatura
  (puntos, rayas, cuadrícula, nada) en `outline`. Mismo anillo y mismo `CHECK`. `paperName(id)`.
- Debajo, en `Secondary`, los nombres de lo que se mira: `coverName`, `paperName` (Salvia, punteado).
- **Tocar** una portada o un papel lo enseña en la vista previa. Si es gratis o hay Pro, además se
  guarda al momento. Si es Pro y no hay Pro, no se guarda: aparece `proLookHint` (Salvia y el punteado
  son gratis; lo demás, con Bobbin Pro.) y la acción `useThis` (Usar este), que abre el `ProDialog`.
  Salir de Ajustes olvida lo que solo se estaba mirando.
- Sin Pro tras un reembolso, la app pinta `activeCover` y `activePaper`: lo guardado vuelve si vuelve
  Pro (`docs/tecnico.md` 6.17).

---

## 15. Diálogos

`AlertDialog` de Material3 sobre `surface`, radio 24, sin elevación tonal. Título en `Body` `Medium`,
texto en `Body`, botones de texto en `primary`. Ningún botón destructivo en rojo. Ancho máximo 576.

### 15.1 `ProDialog`

(`ui/Pro.kt`, #48.) Solo al chocar con un límite de `docs/tecnico.md` 6.16 o desde la fila de
Ajustes. Nunca al arrancar ni tras un número de usos.

```
Bobbin Pro
Compra única, sin suscripción.

-  Siete portadas más
-  Rayado, cuadrícula y liso
-  Más de un seguimiento
-  El widget del mes
-  El widget de la pantalla de bloqueo        (solo iOS)

El método entero es gratis, y lo seguirá siendo.

                          Comprar por 7,99 EUR
                                     Restaurar
                                      Ahora no
```

- Título `proTitle` (Bobbin Pro) y, justo debajo, `proOnce` (Compra única, sin suscripción.): visible
  sin desplazar en cualquier pantalla y con la fuente al 200 %, porque va antes de la lista.
- Una línea por cosa, con el glifo de nota dibujado delante (no un guion de texto): `proCovers`,
  `proPapers`, `proTrackers`, `proMonthWidget` y, en iOS, `proLockWidget`. v1.1 añade `proBook` y
  `proCapture` (Anotar desde Ajustes rápidos en Android; desde Siri y Atajos en iOS); v1.2 `proSync` y
  `proYearSummary`.
- Debajo, en `Secondary`, `proFree` (El método entero es gratis, y lo seguirá siendo.).
- Botones apilados a la derecha, uno por fila: `buy(precio)` (Comprar por 7,99 EUR) con el precio que
  devuelve la tienda, nunca uno escrito en el código; `restore` (Restaurar), siempre, también con Pro;
  `notNow` (Ahora no).
- Sin tienda: `storeUnavailable` (La tienda no está disponible ahora.) y sin `buy`.
- Comprando: `buy` pasa a `working` (Un momento...). Éxito: se cierra y el choque que lo abrió se
  cumple (la portada elegida se guarda, el seguimiento se crea). Cancelado: nada. Error: `buyFailed`
  (No se ha podido completar la compra.) bajo las líneas. Cerrar sin comprar deja todo como estaba.

### 15.2 Importar

(#45, `docs/tecnico.md` 6.9.) Tras el selector del sistema:

- Resumen antes de tocar nada: `importTitle` (Importar copia) y `importSummary(nuevas, actualizadas,
  iguales)` (La copia trae 12 entradas nuevas y 3 más recientes que las tuyas; 40 ya estaban. No se
  borra nada.), con `importAction` (Importar) y `cancel` (Cancelar). Importando, `importAction` pasa a
  `working`.
- Hecho: `importDone(n)` (Diario al día: 15 cambios.) con `ok`.
- Error: `importFailedTitle` (No se ha podido importar) y el texto de su clave (`importNotBackup`,
  `importDamaged`, `importTooNew`, `importEmpty`, `importIsSibling`, `docs/tecnico.md` 4.6), con `ok`.

### 15.3 Exportar

Sin diálogo propio: el selector del sistema con `bobbin-AAAA-MM-DD.zip`. Cancelar no enseña nada. Si
falla, `exportFailed` (No se ha podido guardar la copia.) con `ok`.

### 15.4 Borrar todos los datos

(#33.) Dos pasos, sin rojo:

1. `wipeTitle` (¿Borrar todo el diario?) y `wipeText` (Se borran todas las entradas, colecciones y
   ajustes de este teléfono. Las copias que hayas exportado no se tocan, y Bobbin Pro se conserva.),
   con `wipeContinue` (Continuar) y `cancel`.
2. `wipeConfirmTitle` (Escribe la palabra borrar para confirmar), un campo de texto y `wipeAction`
   (Borrar todo), que solo responde cuando el campo, plegado con `fold`, es `wipeWord` (borrar, en cada
   idioma su palabra). `cancel`.

Al confirmar, la app vuelve a Hoy vacío (estado A de 6.2), con la captura enfocada, sin reiniciar.

### 15.5 Listas de un ajuste

Los de `dayStartRow` y `weekStartRow`: título la fila, una opción por fila de `2u` en `Body`, `CHECK`
en la actual, sin botones: tocar guarda y cierra; fuera o atrás cierra sin cambiar.

---

## 16. Bloqueo y avisos de carga

### 16.1 Bloqueo

(#39, `ui/LockScreen.kt` de line.) `background` liso, sin papel, sin nada del diario: `appName`
(Bobbin) en `PageTitle` centrado y, debajo, `unlock` (Desbloquear) en `primary`. Al aparecer quita el
foco de la captura (si no, el teclado vuelve a subir encima del bloqueo) y lanza el diálogo del
sistema; si se cancela, queda el botón. Textos del diálogo: `lockPromptTitle` (Abrir Bobbin) y
`lockPromptSubtitle` (Tu diario está bloqueado). La vista de multitarea en iOS es solo el color
`background`, puesta desde `iOSApp.swift`.

### 16.2 Avisos de carga

Por encima de todo, `background` liso, texto `Body` centrado en la columna, sin barra de pestañas:

- `TooNew`: `updateNeeded` (Este diario se escribió con una versión más nueva de Bobbin. Actualiza la
  app para abrirlo; no se ha tocado nada.) y `openStore` (Abrir la tienda).
- `MigrationFailed`: `migrationFailed` (No se ha podido preparar el diario para esta versión. Sigue
  intacto y no se ha tocado nada.), sin acción.

---

## 17. Compartir

(#43, `docs/tecnico.md` 6.10.) Gratis siempre. Lo elige el usuario, desde el pie de la página:
`shareDay` (Compartir este día) en Hoy, `shareMonth` en Mes (calendario y tareas) y `shareList` en la
hoja `MORE` de una lista. Solo se ofrece si la página tiene algo. La app nunca sugiere qué compartir.

### 17.1 La hoja

`ui/ShareScreen.kt` es una hoja inferior, no un destino: la primera página a escala (ancho de la
columna, radio 12, borde 1 dp `outline`) y, si hay más, `sharePages(n)` (3 imágenes) en `Secondary`.
Dos acciones (`ActionRow`): `shareImage` (Compartir como imagen) y `shareText` (Compartir como texto), que
abren la hoja del sistema (`Sharing.sharePngs` o `Sharing.shareText`). Nada se guarda en la galería
por su cuenta.

### 17.2 La página como imagen (1080 x 1350 px)

`renderSharePages` (`ui/SharePage.kt`). Siempre en claro, como las tarjetas de las hermanas, con el
papel activo (`activePaper`). Todas las medidas son píxeles de la imagen: se dibuja y se mide el texto
con densidad 1 (un `TextMeasurer` de `rememberTextMeasurer()` trae la de la pantalla y pinta al doble o
al triple).

La rejilla de la imagen es la de la app a 2,25: unidad de 54 px, 20 columnas por 25 filas.

| Elemento | Posición y tamaño |
|---|---|
| Fondo | `FBF8F3` |
| Papel | puntos de 3,4 px `EFE9DF` en x = 27 + 54k y en la línea base de cada fila; rayado y cuadrícula con líneas de 2 px del mismo color; liso, nada |
| Título | Literata 54 px, interlineado 108, `39352E`, desde x 162 y la fila de y 108; si no cabe hasta x 1026, salto de línea. Un día: `longDateWithYear` (Martes, 22 de septiembre de 2026); un mes: `monthTitle`; una colección: su título |
| Entradas | desde y 270 hasta y 1188: margen de signifiers de x 54 a 162, columna de bullets de 162 a 216 (centro x 189), texto de x 216 a 1026 en Literata 38 px, interlineado 54, `39352E`; cerradas en `736D63`, con el tachado y el destino de 5.1. Glifos de 1.5 a 2,25 (punto de 11 px, trazo de 3,4 px). Cada entrada, sus líneas más una en blanco |
| Mes | las líneas de día que tienen algo (el día en la columna de 54 a 162) y `monthTasks` en sistema 25 px Medium `736D63` con 3 px de espaciado; los días vacíos no se pintan |
| Marca | `Bobbin` en Literata 30 px `736D63`, alineada a la derecha en x 1026, línea base y 1296 |
| Página | con más de una, `pageOf(i, n)` (2 de 3) en sistema 30 px `736D63` desde x 162, línea base y 1296 |

Reparto con `paginate(alturas, 918)`: una entrada nunca se parte entre dos imágenes; la que no cabe ni
en una vacía va sola y se corta abajo. Cada página repite el título.

### 17.3 Como texto

`shareText`: el título en la primera línea y una línea por entrada con los símbolos ASCII de
`docs/tecnico.md` 4.4, sin el `- ` de lista. En un mes, la primera entrada de cada día del calendario
lleva delante `(día)`, como el Future log del Markdown, y las tareas del mes van tras la línea
`monthTasks`. Al final, una línea en blanco y `Bobbin`.

---

## 18. Widgets

(#41, #42, #51, #52, `docs/tecnico.md` 6.13.) Leen `widget.json` y nada más: números, fechas y
booleanos. **Ningún texto del diario, nunca.** Fondo `background` del sistema claro u oscuro, radio el
del sistema, relleno 16 en iOS y 8 en Android (desde Android 12 el escritorio ya mete el suyo). Fuente
del sistema. Glifos con la geometría de 1.5: en iOS con `Path`, en Glance como `VectorDrawable`
(`res/drawable/glyph_task.xml`, `glyph_done.xml`, `glyph_event.xml`, `glyph_migrated.xml`) teñidos con
`ColorFilter`, porque Glance no tiene lienzo. La previsualización del selector de Android
(`today_widget_preview.xml`, `month_widget_preview.xml`) usa esos mismos `VectorDrawable` y se mantiene a
mano (`docs/tecnico.md` 8.2).

Sin `widget.json`, o ilegible: estado vacío con la fecha lógica de ahora (con `dayStartHour` 4) y
ceros. Nunca un cierre ni un hueco en blanco.

### 18.1 Hoy (gratis)

| Tamaño | Contenido |
|---|---|
| Android 2x2 (mínimo 110 x 110) e iOS `systemSmall` | Arriba, un punto de 8 dp de la portada y `widgetDate` (MIÉ 23 SEP) en 12 sp Medium `onSurfaceVariant`. Debajo, tres filas de 24 dp: glifo de tarea con `open`, aspa sobre punto con `done`, círculo con `events`, cada número en 17 sp Medium `onBackground`; con 150 dp de ancho o más, detrás, `widgetOpen(n)` (abiertas), `widgetDone(n)` (hechas), `widgetEvents(n)` (eventos) en 13 sp `onSurfaceVariant`. Con 150 dp de alto o más y `reviewPending`, abajo el glifo `>` y `widgetReview` (Por revisar) en 12 sp |
| iOS `systemMedium` | A la izquierda, en columna: `widgetWeekday` (miércoles) 13 sp, el día del mes en 34 sp Medium y `widgetMonth` (septiembre) 13 sp; el punto de la portada junto al día. A la derecha, las tres filas con su etiqueta y, si `reviewPending`, la de revisar |

Tocar: `bobbin://today?focus`, Hoy con la captura enfocada y el teclado.

### 18.2 Mes (Pro)

| Tamaño | Contenido |
|---|---|
| Android 3x2 (mínimo 180 x 110), iOS `systemSmall` y `systemMedium` | Arriba, `widgetMonthName` (SEPTIEMBRE) en 12 sp Medium `onSurfaceVariant` y, a la derecha, el glifo de tarea con `open` en 17 sp Medium. Debajo, un punto por día en dos filas, del 1 al 16 y del 17 al último: con entradas (`monthMask`), círculo relleno de 6 dp del color de la portada; sin entradas, circunferencia de 6 dp con trazo de 1 dp `outline`; hoy, además, un anillo de 1,5 dp `onBackground` a 2 dp. El paso es el ancho entre 16. No es un calendario: sin días de la semana, como el Monthly Log |
| Sin Pro (`isPro` falso) | Los puntos, todos vacíos y al 40 %, y encima, centrados, `proTitle` (Bobbin Pro) en 15 sp Medium y `widgetUnlock` (Toca para activarlo) en 12 sp |

Android pinta los puntos en un `Bitmap` (`yearBitmap` de Quilt y line); iOS con `Canvas`. Tocar:
`bobbin://today`; sin Pro, `bobbin://pro`.

### 18.3 Pantalla de bloqueo (Pro, iOS)

Monocromo, como pinta el sistema. Solo números y glifos.

| Tamaño | Contenido |
|---|---|
| `accessoryCircular` | El glifo de tarea arriba y `open` debajo en 20 pt. Sin Pro: `Image(systemName: "lock")` |
| `accessoryRectangular` | Dos filas: glifo de tarea y `widgetOpen(n)` (3 abiertas); círculo y `widgetEvents(n)` (1 evento). Sin Pro: `proTitle` y `widgetUnlock` |

Mismo `TimelineProvider` que el de hoy. Tocar: `bobbin://today`; sin Pro, `bobbin://pro`.

---

## 19. Notificación

(#36, #38, `docs/tecnico.md` 6.12.) Una al día, apagada por defecto, con texto fijo que no cita el
diario ni una fecha.

- Android: icono pequeño `ic_notification`, `setColor` `3F7A69`, título `reminderTitle` (Un momento para
  repasar el día), texto `reminderBody` (Relee lo de hoy y decide qué sigue.), sin icono grande,
  `autoCancel`, abre `bobbin://review`.
- iOS: el icono de la app, los mismos textos, sonido por defecto, abre `bobbin://review`.

---

## 20. Icono de la app

(#18, SPEC §8.) **Metáfora**: una bobina con su hilo, y el hilo acaba en un punto de bullet. La bobina
es el nombre; el punto, la tarea. El dibujo final lo aprueba el autor antes de generar tamaños.

Lienzo de 1024 x 1024:

- **Fondo**: degradado lineal vertical de `2C2820` (arriba) a `17150F` (abajo). En iOS, sin esquinas
  (las pone el sistema).
- **Bobina**, de x 212 a 612:
  - Dos pestañas en crema `FBF8F3`: rectángulos de 400 x 60 con radio 30, de y 232 a 292 y de y 732 a
    792.
  - El hilo enrollado, entre las dos: siete cápsulas de 280 x 48 con radio 24, de x 272 a 552,
    separadas 16 en vertical, de y 296 a 728, en salvia `B6D6AB`. El mismo lenguaje de cápsulas que el
    icono de Purl, ahora como vueltas de hilo.
- **Hilo suelto**: una curva cuadrática de trazo 20 con extremos redondos, en salvia, desde el borde
  derecho de la quinta cápsula (552, 576) con control en (660, 700) hasta (756, 640).
- **Punto**: círculo crema `FBF8F3` de radio 56 con centro (756, 640). El bloque entero va de x 212 a
  812 y de y 232 a 792: centrado en 512.

Android adaptativo (lienzo de 108 dp): capa de fondo de color `221E17`; capa frontal con el bloque
centrado y ajustado a la diagonal del círculo seguro de 66 dp. Capa `monochrome`: las mismas formas en
un solo color blanco sobre transparente, una máscara, sin tonos.

Icono de notificación (`ic_notification.xml`, 24 x 24, blanco sobre transparente): pestañas de (4, 4) a
(14, 6) y de (4, 18) a (14, 20) con radio 1; hilo, rectángulo de (6, 6,5) a (12, 17,5); punto de radio
2,5 en (18,5, 15).

Se genera todo con `tools/generate_icons.py` sobre `tools/icon-master.svg` (1024 para iOS, adaptativo,
cinco densidades heredadas, `monochrome` y notificación) y se compara a 48 px sobre lanzador claro y
oscuro antes de darlo por bueno.

**Arranque**: Android, `windowBackground` `FBF8F3` o `17150F` según el tema (`docs/tecnico.md` 8.1);
iOS, el de la plantilla de las hermanas. Nada más: la primera imagen es Hoy.

---

## 21. Tableta y pantallas grandes

(#34.) Corte en `WIDE_SCREEN_FROM` (600 dp de ancho de ventana), igual en Android y en iOS
(`isWideScreen`). Vertical y horizontal se tratan igual: manda el ancho.

- **Página.** Hoy, Mes, lista, seguimiento, Revisar, Buscar, Clave, guía y Ajustes: una página de como mucho
  `MAX_CONTENT_WIDTH` (576 dp, 24 columnas de puntos) centrada, con el borde izquierdo en un múltiplo de
  24. A los lados sigue el papel con la misma rejilla.
- **Doble página.** Índice y Futuro, como un cuaderno abierto: dos columnas de hasta 576 dp separadas
  por un lomo de 48 dp (una línea vertical de 1 dp `outlineVariant` en su centro), el conjunto centrado
  y de como mucho 1200 dp. Futuro reparte los bloques de mes alternando izquierda y derecha, en orden;
  el Índice llena la izquierda y sigue en la derecha, en orden de creación. Por debajo de 600, una
  columna.
- **Barra de pestañas y barra de escribir** a todo el ancho, con su contenido repartido en el ancho de
  la página centrada.
- **Hojas, diálogos y `ProDialog`**: ancho máximo 576, centrados.
- Nada más cambia: ni tamaños de letra ni diseños propios (SPEC §5). **[autor]** comprueba a mano en
  iPad y en un emulador de 10 pulgadas (test 47).

---

## 22. Accesibilidad

(#53.)

- **Cada entrada es un nodo** que lee `S.entryDescription(bullet, status, signifiers, text)`: Tarea
  hecha, prioridad: comprar pan. Una pasada o llevada añade la línea que se ve debajo (`movedTo`,
  `scheduledTo`) y una copia la suya (`cameFrom`). Su acción principal es `a11yOptions` (Ver opciones),
  que abre la hoja: todo lo que hace la hoja está al alcance sin gestos. Acciones personalizadas
  (`CustomAccessibilityAction` en Android, `accessibilityCustomActions` en iOS vía Compose):
  `a11yComplete` (Completar) o `a11yReopen` (Reabrir), `actionGoToCopy` en una pasada con copia, y
  `a11yMoveUp` (Subir) y `a11yMoveDown` (Bajar) como alternativa a arrastrar. Nada depende de mantener
  pulsado.
- **Iconos** con su descripción (sección 2). Los botones con texto ("< Volver", "Compartir este día")
  se leen por su texto. Pestañas: su nombre y `a11ySelected` en la activa.
- **Barra de escribir**: las fichas son botones con su nombre; el campo lee su pista (`composeHint`) y
  "En ..." dice dónde cae.
- **Hoja**: cada acción se lee con su nombre y lo que hará.
- **Mes**: cada fila de día lee `a11yDayRow(día, nombre, n)` (3, jueves, 2 entradas) y su acción es
  escribir en él.
- **Seguimiento**: cada casilla es una casilla de verificación que lee `a11yTrackerCell(fila, fecha,
  marcada)` (Dos litros, 23 de septiembre, marcada); el nombre de la fila lee su título y
  `a11yTrackerMarked(días)` (Marcados: 1, 2, 5 y 9), sin totales ni porcentajes, y abre su hoja.
- **Orden de lectura en Hoy**: iconos, título, subtítulo, tarjetas, entradas, calendario del día,
  compartir, barra de escribir, pestañas.
- **Guía**: los dibujos no son un nodo; se leen el título, la frase, `guideStep(i, n)` y los botones.
- **Texto grande (200 %)**: todo el texto crece en `sp` y hace salto de línea; `u` crece con él, así
  que filas, puntos y dianas se separan en proporción y nada se solapa. Las columnas no crecen, para
  que el texto conserve ancho. Glifos y puntos del papel, en `dp`, no crecen. El título de Hoy parte en
  dos líneas. Las pestañas limitan su escala a 1,5 (3.1).
- **Contraste** AA en claro y en oscuro (1.2), comprobado por el test 25.
- **Dianas** de 48 x 48 dp como mínimo, también el glifo, la celda de un seguimiento y cada portada.
- **Movimiento reducido**: el aspa sale entera (1.6).
- Recorrido de Hoy y de Revisar con TalkBack y VoiceOver (test 45).

---

## 23. v1.1 y v1.2

### 23.1 Threading (v1.1, #63)

- Hoja `MORE` de una colección: `continueCollection` (Continuar en una colección nueva), solo si aún no
  tiene continuación. Abre un diálogo con un campo con el título actual propuesto
  (`continueTitle`: Continuar en...) y `create` (Crear). Crea la colección enlazada y la abre.
- Cabecera de la colección, en la fila de subtítulo: `threadFrom(título)` (Viene de Lecturas 2025) y
  `threadNext(título)` (Sigue en Lecturas 2027), cada uno en `primary` y navegable, también si la otra
  está archivada.
- Índice: el hilo sale en la posición de su primera colección y las siguientes debajo, sangradas 24 dp,
  con los puntos guía y la etiqueta de siempre.
- Gratis: nunca pasa por el `ProDialog`.

### 23.2 Nuevo cuaderno (v1.1, #64)

Ajustes, CUADERNO: `newNotebookRow` (Nuevo cuaderno) con `newNotebookSub` (Repasa colecciones y tareas
para empezar otro). Abre REVIEW con alcance de cuaderno, gratis, un elemento cada vez:

1. **Cada colección** no archivada, incluidos los seguimientos: su título en `PageTitle`, la etiqueta
   del Índice en `Secondary` y tres acciones de `2u` en `primary`: `notebookKeep` (Mantener),
   `notebookContinue` (Continuar en una nueva, 23.1) y `notebookArchive` (Archivar).
2. **Cada tarea abierta**, salvo las del Future Log de meses que vienen, con las cinco acciones de 11.2.

Posición `reviewPosition` arriba, como en 11.2. Al terminar, `notebookDone(fecha)` (Cuaderno nuevo desde
el 1 de enero de 2027.) y `close`. El Índice pliega lo anterior bajo `notebookUntil(fecha)` (Cuaderno
hasta el 31 de diciembre de 2026), como las archivadas: sigue legible, no se borra nada.

### 23.3 Reflexión guiada (v1.1, #65)

En el paso de releer (11.1), la pista del campo de nota es la pregunta del día (`S.question(i)`) en vez
de `reflectHint`. Se ignora escribiendo otra cosa o saltando: nada más en la pantalla. Nunca sale fuera
de este campo.

### 23.4 Fechas en lenguaje natural (v1.1, #66)

Mientras se escribe en una captura, si `dateHint` encuentra una fecha, bajo la fila aparece una línea
de `2u` con la acción `scheduleFor(fecha)` (Programar para el 14 mar) en `primary`. Nunca se aplica
sola: Intro sigue guardando en el lugar de la captura. Tocar la acción guarda la entrada en
`Monthly(mes, día)` o `Future(mes, día)` (`docs/tecnico.md` 12.4) y deja el foco en la captura.

### 23.5 Captura desde fuera (v1.1, #67, Pro)

- Android: la baldosa de Ajustes rápidos, `tileLabel` (Anotar en Bobbin) con `ic_notification`. Abre
  `CaptureActivity`, una hoja sobre `surface` pegada al teclado con solo la fila de captura y la de
  accesorios; Intro guarda en `Daily(hoy)` y cierra, con `captureSaved` (Anotado en Bobbin.) como
  aviso del sistema. Sin Pro, abre la app con el `ProDialog`.
- iOS: el App Intent `Note in Bobbin` (texto literal en inglés; las traducciones en
  `Localizable.strings`), sin pantalla propia; responde `captureSaved`.

### 23.6 Libro en PDF (v1.1, #68, Pro)

Ajustes, CUADERNO: `bookRow` (Libro en PDF) con `bookSub` (El cuaderno maquetado, para imprimir o
guardar); también en el Índice, como última fila tras las de crear, `bookAction` (Hacer el libro en
PDF) en `primary`. Sin Pro, abre el `ProDialog`.

- A5, 420 x 595 pt. Rejilla de 17,5 pt: 24 columnas por 34 filas, la misma página de 24 columnas de la
  app. Márgenes de dos unidades (35 pt). Papel activo con puntos de 1,1 pt. Glifos de 1.5 escalados a
  17,5 / 24.
- Portada: fondo del color de la portada; `appName` en Literata 35 pt `39352E` centrado a un tercio de
  la altura; debajo, el rango de fechas del cuaderno (`bookRange`: septiembre de 2026 a agosto de 2027)
  en 12 pt al 70 %.
- Después, la Clave (sección 13 sin los gestos), el Índice (sección 9, con número de página del libro a
  la derecha en vez de la etiqueta: aquí sí hay páginas), cada mes (su calendario con los días que
  tienen algo, sus tareas y cada Daily Log con su fecha) y cada colección, en orden de creación. `Ink`
  a 12,4 pt con interlineado 17,5; `PageTitle` a 17,5 pt con 35.
- Pie: número de página en 8 pt `736D63` centrado a 17,5 pt del borde inferior.
- Progreso: diálogo con `bookMaking(n, total)` (Maquetando el libro: página 12 de 80) y `cancel`.

### 23.7 Sincronización (v1.2, #69, Pro)

Ajustes, COPIA: `syncRow` (Sincronizar) con interruptor. Subtítulo: `syncOff` (Apagada), `syncICloud`
(Con iCloud Drive) o `syncFile(nombre)` (Con bobbin-sync.json, en la carpeta que elegiste), y
`syncLast(hora)` (Última vez, 18:40). Encender sin Pro abre el `ProDialog`; en Android abre el selector
del sistema. Conflicto (`docs/tecnico.md` 12.7): diálogo `syncConflictTitle` (Los dos lados han
cambiado) con `syncConflictText`, y tres botones apilados: `syncKeepThis` (Usar este
teléfono), `syncKeepOther` (Usar el otro) y `syncMerge` (Juntar los dos), que enseña el resumen
de 15.2 antes de aplicar. Nunca fusión silenciosa.

### 23.8 Resumen del año (v1.2, #70, Pro)

Ajustes, CUADERNO: `yearSummaryRow` (Resumen del año); también como último paso del Nuevo cuaderno. Es
REVIEW en modo lectura, de una sola página: `yearSummaryTitle(año)` (Tu 2026) en `PageTitle` y tres
bloques con su `Eyebrow`, `summaryInspirations` (INSPIRACIONES), `summaryPriorities` (PRIORIDADES
HECHAS) y `summaryMostMigrated` (LAS QUE MÁS VIAJARON), cada uno con sus entradas como en una lista (en
la última, `migratedTimes(n)` detrás de cada una). Solo texto. Un año sin nada: `summaryEmpty` (Este
año no marcaste inspiraciones ni prioridades. El que viene está en blanco.) en `Body`. Sin Pro, el
`ProDialog`.

### 23.9 Variaciones del índice (v1.2, #71)

Gratis. `MORE` de una colección (y pulsación larga sobre un mes en el Índice): `iconAndTheme` (Icono y
tema). Una hoja con los 12 iconos de `INDEX_ICONS` en una rejilla de 4 x 3 dianas de 48 (dibujados como
los glifos, trazo de 1,5 dp en caja de 24), `iconNone` (Sin icono), y un campo de una línea `themeHint`
(Tema) de hasta 24 puntos de código.

- El icono se pinta en la columna de bullets de la fila del Índice.
- Si alguna colección tiene tema, bajo el filtro del Índice sale una fila de filtros por tema, como los
  de Buscar, y `groupByTheme` agrupa con `Eyebrow` por tema y los sin tema al final (`themeNone`: SIN
  TEMA).
- Un mes pasado ya migrado por completo (`docs/tecnico.md` 12.9) lleva su título subrayado con 1 dp
  `onSurfaceVariant` a 3 dp bajo la línea base, como en el papel.
- Sin iconos ni temas, el Índice se ve exactamente igual que en v1.0.

### 23.10 Importar de las hermanas (v1.2, #72)

La misma fila `importRow`. Si el fichero es de Purl, MoodTraker o Quilt, en vez del resumen de 15.2 se
abre REVIEW candidato a candidato: el origen en `Eyebrow` (`siblingFrom(app, fecha)`: PURL, 14 DE MARZO
DE 2025), la entrada en `PageTitle` con su glifo (evento; tarea hecha para Quilt) y tres acciones:
`importToDay` (Importar a ese día), `importElsewhere` (A otro día o colección, con el selector de 5.7) y
`importSkip` (Saltar). Nada se escribe hasta decidir cada uno. Al final, `importSiblingDone(n)` (15
entradas importadas.) y `close`. Un fichero que no es de ninguna: `importNotSibling` en el diálogo de
error de 15.2.

---

## 24. Qué pantalla cubre cada issue

| Issue | Secciones |
|---|---|
| #19 Navegación | 3, 3.1, 3.2 |
| #20 Captura rápida | 5.2, 5.3 |
| #21 Hoy | 5.2, 5.3, 6 |
| #22 Estados y signifiers | 1.5, 4, 5.1, 5.5, 5.6, 5.7 |
| #23 Editar, reordenar, borrar | 4, 5.4, 5.8 |
| #24 Mes | 7 |
| #25 Future Log | 5.7, 8 |
| #26 Aviso del Future Log | 7.2, 11.4 |
| #27 Revisión | 11.2, 11.3 |
| #28 Reflexión | 11.1 |
| #29 Índice | 9 |
| #30 Colecciones | 9, 10.1 |
| #31 Clave y primer arranque | 6.2 (estado A), 13 |
| #32 Ajustes | 14, 14.1, 15.5 |
| #33 Borrar todos los datos | 14, 15.4 |
| #34 Tableta | 21 |
| #35 Búsqueda | 12 |
| #43 Compartir | 17 |
| #48 Paywall | 15.1 |
| #49 Portadas y papeles | 1.1, 1.2, 14.1 |
| #50 Seguimientos | 9, 10.2 |
| #51 Widget del mes | 18.2 |
| #52 Widget de bloqueo | 18.3 |
| #53 Accesibilidad | 1.2, 22 |
| #63 a #72 | 23.1 a 23.10 |

Y fuera de las pantallas: el icono (#18) en la sección 20, la notificación (#36, #38) en la 19, el
bloqueo (#39) en la 16 y los widgets de hoy (#41, #42) en la 18.1.
