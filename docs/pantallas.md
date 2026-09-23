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

**Tesis: cada pantalla es una página del cuaderno punteado.** Quien usa Bobbin no maneja una app de
tareas: escribe en su cuaderno, y la interfaz se aparta. La revisión final de diseño (#73) pule esta
sección, no la cambia.

Las diez decisiones, enteras:

1. **Papel y rejilla.** Toda la interfaz vive sobre una rejilla de 24 dp: interlineado, márgenes,
   alturas de fila y dianas son múltiplos de 24 (una diana son 48 dp, dos filas). El papel por defecto
   es punteado: un punto de 1,5 dp cada 24 dp en `outlineVariant`, alineado con las líneas base del
   texto. Con el texto del sistema agrandado la fila crece con él (24 dp por la escala de fuente, nunca
   menos de 24 dp): rejilla y puntos escalan juntos y nada se solapa. En tablet la página mide como
   mucho 24 columnas de puntos (576 dp) y se centra; a los lados sigue el papel.
2. **El bullet es un punto de la rejilla.** La columna de bullets cae sobre una columna de puntos: la
   tarea es ese mismo punto hecho tinta (5 dp, `onBackground`). Los glifos se dibujan con `Canvas`,
   nunca con caracteres de fuente, para que se vean igual en Android e iOS: tarea, punto relleno;
   evento, círculo; nota, guion; hecha, aspa sobre el punto; migrada, ángulo hacia la derecha (`>`);
   programada, ángulo hacia la izquierda (`<`); irrelevante, el texto tachado. Los signifiers van en su
   propia columna de margen, a la izquierda: prioridad (`*`), inspiración (`!`), explorar (ojo).
3. **Color.** Los `colorScheme` de la familia (`line/docs/pantallas.md` 1.1) con dos cambios en claro,
   por contraste AA: `onSurfaceVariant` pasa de `8B8479` (3,5:1 sobre el papel) a `736D63`, y
   `primary` pasa de `6FAE9B` (2,4:1) a `3F7A69`. El oscuro queda igual: ya cumple. Sin rojo en ninguna
   parte. `primary` solo en acciones de texto. Una tarea hecha, migrada o programada baja su texto a
   `onSurfaceVariant`: el estado lo cuenta el glifo, no el color. La portada Pro tiñe solo el punto de
   la fecha de hoy, la marca de la pestaña activa y los widgets.
4. **Tipografía.** Dos voces. La tinta del usuario en Literata, única fuente empaquetada como en line:
   `Ink` 17/24 para cada bullet y `PageTitle` 24/48 para la cabecera de cada página (la fecha, el mes,
   el título de una colección), como quien la escribe a mano arriba. La interfaz en la fuente del
   sistema: `Body` 15/24, `Secondary` 13/24 en `onSurfaceVariant`, `Eyebrow` 11/24 en mayúsculas para
   las etiquetas de bloque. Sin negrita ni cursiva en lo que escribe el usuario.
5. **Navegación.** Cuatro pestañas de texto abajo, sin iconos: Hoy, Mes, Futuro, Índice. La activa en
   `onBackground` con una marca de 2 dp debajo; las demás en `onSurfaceVariant`. Todo lo demás
   (colección, revisión, búsqueda, clave, ajustes, Pro) se abre encima y se cierra con atrás. La
   cabecera de Hoy lleva flechas visibles para el día anterior y el siguiente, además del gesto de pasar
   página: nadie tiene que adivinar un gesto.
6. **Escribir.** La captura es la siguiente fila vacía de la página, no una barra aparte ni un botón
   flotante. Al abrir Hoy el cursor ya está en ella. Mientras se escribe, el prefijo se convierte en su
   glifo en la misma fila (`o ` evento, `- ` nota, sin prefijo tarea; `* ` y `! ` signifier). Encima del
   teclado, una fila con los tres glifos y su nombre, para quien prefiere tocar.
7. **Estados.** Un toque en el glifo de una tarea la marca hecha, y el aspa se traza sobre el punto en
   150 ms: es la única animación de la app, y con movimiento reducido es instantánea. La pulsación
   larga abre una hoja con cada acción escrita con su glifo y su nombre, para que el símbolo se aprenda
   solo.
8. **Páginas.** Hoy: la fecha y la lista. Mes: el calendario como lista de días (una fila por día,
   `1 L`) y debajo las tareas del mes. Futuro: seis bloques de mes. Índice: cada página con su título,
   una línea de puntos guía y, a la derecha, qué es (Mes, Colección), como el índice del cuaderno.
   Revisión: una tarea por pantalla, en grande, con sus cinco acciones apiladas como botones de texto y
   "3 de 12" discreto arriba.
9. **Vacíos y textos.** Un vacío invita a escribir, nunca a configurar: en Hoy, la fila de captura con
   una pista corta de los prefijos. Deshacer es una línea de texto abajo durante cinco segundos, sin
   tarjeta. Nada se anuncia con exclamaciones.
10. **Accesibilidad.** Cada glifo tiene nombre para el lector de pantalla ("Tarea hecha: comprar pan").
    Dianas de 48 dp. Todo texto con contraste AA sobre el papel en claro y en oscuro; los puntos nunca
    compiten con el texto.

Y tres principios que cortan cualquier duda (SPEC §5): cero toques antes de escribir, nada se mueve
solo, nada de números de progreso. Sin tarjetas ni cajas: el papel es el lienzo, y un aviso es una
línea de texto sobre él.

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
- La barra de pestañas, la fila de accesorios y la línea de deshacer son `background` liso con una
  línea superior de 1 dp `outlineVariant`: tapan el papel que pasa por debajo.
- El papel es fondo: nunca cambia el tamaño ni la posición de una línea (SPEC §5).

### 1.2 Color

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `FBF8F3` | `17150F` | el papel |
| `onBackground` | `39352E` | `ECE5D9` | la tinta: texto del usuario, glifos, títulos, pestaña activa |
| `surface` | `FFFFFF` | `201D16` | diálogos, hojas inferiores, la entrada levantada al arrastrar |
| `surfaceVariant` | `F0EBE2` | `2C2820` | ningún texto encima. Solo el hueco de un interruptor apagado |
| `onSurfaceVariant` | `736D63` | `9C9486` | texto secundario, etiquetas, tareas cerradas, pestañas inactivas, iconos |
| `outline` | `E3DCD1` | `3A352B` | bordes de 1 dp, anillos de las muestras |
| `outlineVariant` | `EFE9DF` | `2C2820` | los puntos y líneas del papel, separadores de semana, línea superior de las barras |
| `primary` | `3F7A69` | `8FC9B6` | solo acciones de texto e interruptores encendidos |
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
`surfaceVariant` no lleva texto. Los puntos (`outlineVariant` sobre `background`, 1,1:1 en claro y
1,2:1 en oscuro) están por debajo a propósito: no compiten con la tinta.

**Portada.** La portada activa (`activeCover`, `docs/tecnico.md` 6.17; ids y hex en `docs/tecnico.md`
5) tiñe exactamente cuatro cosas: el punto de la fecha de hoy en la cabecera de Hoy, la marca de 2 dp
de la pestaña activa, los widgets y, en v1.1, la cubierta del libro. El mismo hex en claro y en oscuro.
Nada más cambia de color con la portada.

### 1.3 Tipografía

`ui/theme/Type.kt`. Cada estilo lleva ya su color. El interlineado es `u` (o `2u`), así que sigue a la
rejilla con cualquier escala de fuente.

| Estilo | Fuente | Tamaño / interlineado | Peso | Color | Dónde |
|---|---|---|---|---|---|
| `Ink` | Literata | 17 / 24 sp | Regular | `onBackground` | cada entrada, la fila de captura, títulos del Índice, nombres de filas de seguimiento, el campo de búsqueda y el de reflexión |
| `PageTitle` | Literata | 24 / 48 sp | Regular | `onBackground` | la cabecera de cada página, y la tarea en grande de Revisar |
| `Body` | sistema | 15 / 24 sp | Normal | `onBackground` | pestañas, filas de Ajustes, hojas, avisos, diálogos |
| `Secondary` | sistema | 13 / 24 sp | Normal | `onSurfaceVariant` | subtítulos, el mes bajo la fecha, el destino de una migrada, pistas, contador |
| `Eyebrow` | sistema | 11 / 24 sp | Medium, 1,4 sp de espaciado, `uppercase()` | `onSurfaceVariant` | etiquetas de bloque y de sección |

- Una acción de texto es `Body` en `primary`. El título de un diálogo es `Body` en `Medium`. No hay más
  estilos.
- Literata Regular es la única fuente empaquetada (`composeResources/font/literata_regular.ttf`). Sin
  negrita ni cursiva en la tinta. Las cifras de Mes y Futuro, en `Body` con cifras tabulares
  (`fontFeatureSettings = "tnum"`), para que los días se alineen.
- Los widgets usan la fuente del sistema (sección 18).

### 1.4 Medidas

| Qué | Valor |
|---|---|
| Columnas de una página | 0 a 48: margen izquierdo, que aloja los signifiers. 48 a 72: columna de bullets (centro en x 60, una columna de puntos). Desde 72: texto. Últimos 24: margen derecho |
| Página de 360 dp | 264 dp de texto |
| Columna de fecha (Mes, Futuro) | 0 a 48: el número del día en 0 a 24, alineado a la derecha, y la inicial de la semana centrada en 24 a 48. En esas páginas todo lo demás se corre 48 dp: signifiers de 48 a 96, bullet con centro en x 108, texto desde 120 |
| Glifo | caja de 24 x 24 dp centrada en (columna de bullets, línea base de la primera línea): el bullet se pinta sobre el punto del papel |
| Signifiers | centros en x 36, 22 y 8 (14 dp de paso), de derecha a izquierda en el orden prioridad, inspiración, explorar, en la línea base de la primera línea. Uno solo cae en x 36, una columna de puntos |
| Fila de entrada | `n` líneas de texto x `u`, más una fila de `u` en blanco debajo |
| Diana | 48 x 48 dp como mínimo, también el glifo: centrada en el glifo, cubre media fila por arriba y la fila en blanco por abajo, así que dos entradas seguidas no se pisan |
| Cabecera | fila de iconos (`2u`), fila de título (`2u`), fila de subtítulo (`u`, opcional), una fila en blanco (`u`). Se desplaza con la página |
| Barra de pestañas | `2u`, cuatro pestañas de igual ancho |
| Fila de accesorios | `2u`, encima del teclado |
| Línea de deshacer | `2u` |
| Filas de Ajustes, de hojas y de listas de diálogo | `2u`: título en una fila y subtítulo en la otra, o una sola línea centrada |
| Hueco entre secciones | `2u` en Ajustes y en la Clave; `u` entre bloques de Mes, Futuro y la búsqueda |
| Marca de activo | 2 dp de alto, ancho del texto, 4 dp bajo su línea base. En pestañas, filtros y la fila de accesorios |
| Radios | diálogos y hojas, 24 (solo las esquinas de arriba en las hojas); vista previa del cuaderno, 12; muestra de papel, 6 |
| Bordes | 1 dp `outline`. Sin sombras ni elevación tonal en ningún sitio |
| Iconos | 20 dp dentro de una diana de 48, trazo del 9 % del lado (sección 2) |

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

Una sola animación propia: el aspa de 1.5. Con movimiento reducido del sistema (`Settings.Global
.ANIMATOR_DURATION_SCALE == 0` en Android, `UIAccessibility.isReduceMotionEnabled` en iOS) se pinta
entera al instante. Cambiar de pestaña, de día, de mes o de página de revisión es instantáneo, como en
las hermanas. Solo quedan las del sistema: el teclado, los diálogos, las hojas inferiores, el
interruptor y el diálogo de biometría.

### 1.7 Tono de los textos

Frases cortas, en presente, sin exclamaciones, sin reproches ("nunca has...", "vas a perder...") y sin
números de progreso. Un aviso dice qué pasa y, si hay algo que hacer, lo ofrece como acción de texto.
Los símbolos del método no se traducen.

---

## 2. Iconos

`ui/Icons.kt`, de line: coordenadas en fracción del lado, trazo del 9 % del lado, extremos y uniones
redondeados, color `onSurfaceVariant`, 20 dp dentro de un botón de 48 (`GlyphButton`). Son iconos de
la interfaz; los del método son glifos (1.5). La barra de pestañas no lleva ninguno.

| Icono | Trazos (x, y en fracción del lado) | Descripción para accesibilidad |
|---|---|---|
| `BACK` | (0.62, 0.18) (0.34, 0.50) (0.62, 0.82) | `a11yBack` (Volver). En Hoy, `a11yPreviousDay` (Día anterior); en Mes, `a11yPreviousMonth` (Mes anterior) |
| `FORWARD` | (0.40, 0.18) (0.68, 0.50) (0.40, 0.82) | `a11yNextDay` (Día siguiente); en Mes, `a11yNextMonth` (Mes siguiente) |
| `CLOSE` | (0.24, 0.24) (0.76, 0.76); y (0.76, 0.24) (0.24, 0.76) | `a11yClose` (Cerrar) |
| `SHARE` | (0.50, 0.88) (0.50, 0.16); y (0.26, 0.40) (0.50, 0.16) (0.74, 0.40) | `a11yShare` (Compartir esta página) |
| `SETTINGS` | dos líneas (0.14, y) (0.86, y) en y = 0.34 y 0.62, con un círculo lleno de radio 0.11 en x = 0.66 y x = 0.38 | `a11ySettings` (Ajustes) |
| `SEARCH` | círculo de radio 0.24 en (0.44, 0.44); línea (0.62, 0.62) (0.84, 0.84) | `a11ySearch` (Buscar en el diario) |
| `KEY` | polilínea (0.32, 0.36) (0.36, 0.24) (0.50, 0.18) (0.64, 0.24) (0.68, 0.36) (0.60, 0.46) (0.50, 0.52) (0.50, 0.62); círculo lleno de radio 0.06 en (0.50, 0.80) | `a11yKey` (Clave de símbolos) |
| `MORE` | tres círculos llenos de radio 0.07 en (0.22, 0.50), (0.50, 0.50) y (0.78, 0.50) | `a11yMore` (Más acciones) |
| `CHECK` | (0.22, 0.52) (0.42, 0.72) (0.78, 0.30) | ninguno: el estado lo dice la fila |

Un icono que no responde en un extremo (la flecha del día siguiente en mañana, la del mes siguiente en
el mes actual) no se pinta: nada al 30 % que parezca roto.

---

## 3. Estructura y navegación

```
App
+-- barra de pestañas: Hoy | Mes | Futuro | Índice      (Screen.TODAY, MONTH, FUTURE, INDEX)
+-- en pila, por encima de la barra:
|   +-- COLLECTION   (desde el Índice, Revisar, la búsqueda o un enlace de una migrada)
|   +-- REVIEW       (desde las líneas de Hoy y Mes, la notificación, bobbin://review y Ajustes)
|   +-- SEARCH       (icono SEARCH de las cuatro pestañas)
|   +-- KEY          (icono KEY de Hoy)
|   +-- SETTINGS     (icono SETTINGS de las cuatro pestañas)
|   +-- PRO          (ProDialog: un choque de docs/tecnico.md 6.16, la fila de Ajustes o bobbin://pro)
+-- puertas, por encima de todo: bloqueo (sección 16) y avisos de carga (16.2)
```

Diez destinos y ninguno más (SPEC §5): hojas inferiores y diálogos (la hoja de una entrada, la de
compartir, el selector de destino, los de Ajustes) no son destinos, se abren sobre la pantalla que los
lanza y se cierran con atrás o tocando fuera.

- **Arranque.** Siempre en Hoy, en el día lógico de hoy, con la fila de captura enfocada y el teclado
  arriba. Sin splash con lógica, sin bienvenida, sin paywall (test 37).
- **Atrás** (sistema y `BackHandler`): primero cierra el teclado; después la hoja o el diálogo
  abierto; después la pantalla de la pila de arriba; en Mes, Futuro o Índice vuelve a Hoy; en Hoy
  viendo otro día, vuelve a hoy; en Hoy, hoy, sale de la app.
- **Pestañas.** Tocar la activa desplaza su página al principio (en Hoy, a la fila de captura). Abrir
  una pestaña distinta enseña su página tal como se dejó en esta sesión, salvo Hoy, que siempre
  enfoca la captura.
- **Teclado.** Con el teclado del sistema arriba, la barra de pestañas se esconde y en su sitio queda
  la fila de accesorios (5.3). Al bajar el teclado vuelve la barra. Con teclado físico, las dos a la
  vez: la fila de accesorios encima de la barra.
- **Enlaces** (`docs/tecnico.md` 7): `bobbin://today` abre Hoy en hoy; con `?focus`, además la
  captura enfocada con el teclado; `bobbin://review` abre REVIEW con alcance `Day(today)`;
  `bobbin://pro` abre Hoy con el `ProDialog` encima. Con el bloqueo puesto, el enlace espera a
  desbloquear. Al volver a primer plano se recalcula el día lógico: si cambió y se estaba viendo "hoy",
  Hoy pasa al día nuevo.

### 3.1 Barra de pestañas

`2u` de alto, `background` liso con línea superior de 1 dp `outlineVariant`. Cuatro etiquetas `Body`
centradas, cada una en un cuarto del ancho de la página: `tabToday` (Hoy), `tabMonth` (Mes),
`tabFuture` (Futuro), `tabIndex` (Índice). La activa en `onBackground` con la marca de 2 dp del color
de la portada; las demás en `onSurfaceVariant`. Toda la celda responde. Las etiquetas limitan su escala
a 1,5 y nunca se cortan: `docs/textos.md` las mantiene de una palabra en los cinco idiomas.

### 3.2 Cabecera de página

Parte de la página: se desplaza con ella.

- **Fila de iconos** (`2u`). En las superpuestas, `BACK` (o `CLOSE` en Revisar) pegado a la izquierda.
  A la derecha, los iconos de la página, pegados al borde, en este orden de izquierda a derecha:
  `SHARE` (solo si la página tiene algo que compartir), `KEY` (solo en Hoy), `SEARCH` y `SETTINGS`
  (solo en las cuatro pestañas), `MORE` (solo en Colección).
- **Fila de título** (`2u`). `PageTitle` desde x 48 (en Mes y Futuro también desde x 48: la columna de
  fecha no empuja el título). A la derecha, las flechas de Hoy y de Mes, `BACK` y `FORWARD` (96 dp).
  Un título que no cabe hace salto de línea y la fila crece de `2u` en `2u`.
- **Fila de subtítulo** (`u`, opcional). `Secondary` desde x 48.
- Una fila en blanco y empieza el contenido.

### 3.3 Franja de avisos

Bajo la cabecera de Hoy y de Mes, antes de la lista, las líneas que apliquen, una debajo de otra, en el
orden de cada pantalla (6.3, 7.2). Cada una es texto sobre el papel, sin caja: `Body` en `onBackground`
con sus acciones de texto detrás en la misma línea o en la siguiente, o, si toda la línea lleva a otra
pantalla, `Body` en `primary` entera. Cada línea ocupa `2u` (su texto centrado en la primera fila y una
en blanco), o más si hace salto de línea. Nunca un modal (SPEC §6).

---

## 4. Los cuatro gestos

Son los únicos gestos de la app. Una pantalla nueva reutiliza estos cuatro en vez de inventar uno
propio. Todos tienen además una acción visible o una acción del lector de pantalla (sección 22), porque
nadie tiene que adivinar un gesto.

| Gesto | Qué hace | Dónde vale |
|---|---|---|
| 1. **Tocar el glifo** | Una tarea `OPEN` pasa a `DONE` (el aspa se traza, 1.6); una `DONE` vuelve a `OPEN` (`toggleDone`). En una `MIGRATED` o `SCHEDULED`, lleva a la copia (5.5). En una `IRRELEVANT`, un evento o una nota, nada: su diana no existe | toda lista de entradas: Hoy, Mes, Futuro, Colección, Buscar |
| 2. **Pulsación larga** | Abre la hoja de la entrada (5.6): estados, signifiers, editar y borrar, cada acción con su glifo y su nombre | las mismas listas; en un seguimiento, sobre una fila (10.2) |
| 3. **Deslizar en horizontal** | Cambia de día. Hacia la derecha, el anterior; hacia la izquierda, el siguiente. Cuenta si el dedo recorre 72 dp (`3u` a escala 1) y el doble en horizontal que en vertical; el cambio es instantáneo al soltar | Hoy (6); en un seguimiento, el día marcado (10.2) |
| 4. **Arrastrar** | Tras la pulsación larga, mover el dedo más de 12 dp levanta la entrada en vez de abrir la hoja: la entrada se pinta sobre `surface` con borde de 1 dp `outline`, sigue al dedo y las demás le hacen sitio de fila en fila. Al soltar, `reorder(place, ids)` | dentro del mismo lugar: un día, la línea de un día del Mes, las tareas del mes, un bloque de Futuro, una colección, las filas de un seguimiento. No cruza de un lugar a otro: eso es migrar, y se decide en la hoja |

Además, fuera de los cuatro, lo que cualquier campo de texto hace: **tocar el texto** de una entrada lo
edita en línea (5.4), y tocar el papel fuera de un campo baja el teclado.

---

## 5. La entrada, la captura y la hoja

`ui/EntryList.kt` y `ui/EntrySheet.kt`: los mismos componentes en Hoy, Mes, Futuro, Colección y
Buscar (#23, #30).

### 5.1 Una entrada

```
 margen      bullet  texto
 0     48    60  72                                            -24
 |  !  *     o   Cena con Ana en el sitio de siempre,           |   fila 1, Ink
 |               a las nueve                                    |   fila 2, si hace salto
 |                                                              |   fila en blanco
```

- Glifo del bullet o del estado (1.5) en la columna de bullets, sobre la primera línea base.
- Signifiers en el margen izquierdo (1.4).
- Texto en `Ink`: `onBackground` si está abierta o es evento o nota; `onSurfaceVariant` si es una
  tarea `DONE`, `MIGRATED` o `SCHEDULED`; tachado entero si es `IRRELEVANT` (1.5). El texto hace salto
  de línea, nunca se corta con `...` en una lista.
- Una tarea `MIGRATED` o `SCHEDULED` lleva detrás de su texto, a dos espacios y en `Secondary`, a dónde
  fue la copia (`copyOf`, `docs/tecnico.md` 6.3): `wentToDay(fecha)` (24 sep), `wentToMonth(mes)`
  (Octubre 2026), `wentToFuture(mes, día?)` (Futuro, 14 oct) o el título de la colección. Si la copia
  ya no existe, nada.
- Los esqueletos (`gone`) no se pintan.
- Orden: `order`, después `createdAt`, después `id` (`docs/tecnico.md` 6.3).

### 5.2 La fila de captura

La siguiente fila vacía de la página, con la misma anatomía que una entrada. Un campo de una línea
lógica (hace salto de línea visual) sin borde ni fondo, en `Ink`, cursor `onBackground`.

- **Glifo en vivo.** En la columna de bullets, el glifo que tendrá la entrada, en `onSurfaceVariant`
  mientras el campo está vacío y en `onBackground` en cuanto hay texto. `rapidParse` se aplica al
  escribir: al teclear `o ` al principio, el prefijo desaparece del campo y el glifo pasa a círculo;
  `- `, a guion; `* `, `! ` y `? ` pintan su signifier en el margen. Borrar con la tecla de retroceso
  justo al principio del texto devuelve el último prefijo consumido como texto, para poder corregirlo.
- **Pista.** `captureHint` (Escribe aquí) en `onSurfaceVariant` cuando el campo está vacío y tiene el
  foco, o cuando es la única fila de su bloque (un día, un bloque de Futuro o una colección vacíos).
- **Intro** guarda (`rapidParse`, el tope de 500 de `docs/tecnico.md` 6.2) y deja el foco en la nueva
  fila vacía, sin bajar el teclado nunca. Con el campo vacío o solo prefijos, Intro no hace nada. Un
  salto de línea pegado se convierte en espacio.
- **Contador.** Desde `COUNTER_FROM` (450) puntos de código, `counter(n, 500)` (480/500) en
  `Secondary` a la derecha de la fila de accesorios. Al llegar a 500 el campo no admite más.
- Al enfocar la captura, la página se desplaza para dejarla a la vista encima de la fila de
  accesorios, con una fila de aire debajo (`imePadding()` y lista que desplaza, `docs/tecnico.md` 6.2).

### 5.3 La fila de accesorios

Encima del teclado mientras la fila de captura tiene el foco. `2u`, `background` liso, línea superior
de 1 dp `outlineVariant`. Tres elecciones de 48 de alto, alineadas a la izquierda desde x 24: el glifo
y su nombre en `Body`, `bulletTask` (Tarea), `bulletEvent` (Evento), `bulletNote` (Nota). La elegida
en `onBackground` con la marca de 2 dp; las demás en `onSurfaceVariant`.

- Es el `picked` de `rapidParse`. Por defecto, Tarea. Un prefijo escrito gana, y la fila lo refleja al
  momento. Tras guardar, vuelve a Tarea: cada entrada se decide al escribirla, como en el papel.
- No ofrece signifiers: se escriben con su prefijo o se ponen después desde la hoja (#22).
- A la derecha, el contador de 5.2.
- Editando una entrada en línea (5.4), la fila solo enseña el contador.

### 5.4 Editar en línea

Tocar el texto de una entrada lo convierte en campo en el mismo sitio, con el cursor donde se tocó.
Mismo estilo, mismo tope (`limitEdit`), sin prefijos: aquí `- ` es texto. Intro o tocar fuera guarda
(`editText`); si el texto queda vacío, no guarda y vuelve el anterior (borrar es de la hoja). Atrás
baja el teclado y guarda.

### 5.5 El enlace de una migrada o programada

Tocar el glifo `>` o `<` lleva a la copia: su día en Hoy, su mes en Mes, su bloque en Futuro o su
colección, con la página desplazada hasta ella. Así el gesto 1 sigue siendo "tocar el glifo actúa sobre
el estado".

### 5.6 La hoja de una entrada

`ModalBottomSheet` sobre `surface`, radio 24 arriba, sin sombra, ancho máximo 576 centrada. Arriba, la
entrada tal como se ve (glifo, signifiers y texto en `Ink`, hasta 3 líneas con `...`) y, en una tarea
con `migrationCount >= 2`, `migratedTimes(n)` (Migrada 3 veces) en `Secondary`. Debajo, una fila de
`2u` por acción: el glifo en la columna de bullets y el nombre en `Body` desde x 72.

| La entrada es | Acciones, en este orden |
|---|---|
| Tarea `OPEN` | `actionMigrate` (> Migrar), `actionSchedule` (< Programar), `actionDiscard` (Descartar, con el punto tachado); los tres signifiers; `actionEdit` (Editar); `actionDelete` (Borrar) |
| Tarea `DONE` o `IRRELEVANT` | `actionReopen` (Reabrir, con el punto); los tres signifiers; Editar; Borrar |
| Tarea `MIGRATED` o `SCHEDULED` | `actionGoToCopy` (Ir a la copia, con su `>` o `<`), solo si la copia existe; los tres signifiers; Editar; Borrar |
| Evento o nota | los tres signifiers; Editar; Borrar. Nunca un estado de tarea (#22) |

- Signifiers: `signifierPriority` (Prioridad), `signifierInspiration` (Inspiración), `signifierExplore`
  (Explorar), cada uno con su glifo; el que está puesto lleva `CHECK` a la derecha. Tocar alterna
  (`toggleSignifier`) y la hoja sigue abierta.
- Editar cierra la hoja y abre la edición en línea (5.4). Borrar cierra la hoja, borra sin confirmar y
  enseña la línea de deshacer (5.8).
- Migrar y Programar cambian el contenido de la hoja por el selector de destino (5.7).

### 5.7 El selector de destino

El mismo en la hoja, en Revisar y en el Nuevo cuaderno. Sustituye a las acciones en el mismo sitio, con
una primera fila `back` (Volver) que las devuelve. Nunca un calendario visual (#25).

**Migrar** (`migrate`, `docs/tecnico.md` 6.4): filas `toToday` (Hoy), `toTomorrow` (Mañana),
`toThisMonth` (Tareas de este mes), `toDayOfMonth` (Un día de este mes) y `toCollection` (A una
colección). Un destino que la función rechazaría (el mismo lugar, un día pasado) no se pinta.

- Un día de este mes: la fila se convierte en un campo numérico de dos cifras (`dayField`, día) con la
  acción `migrateAction` (Migrar). Un día que no está en el mes, o anterior a hoy, no hace nada y
  enseña `dayOutOfRange(mes, n)` (Septiembre no tiene día 31.) o `dayPast` (Ese día ya pasó.) en
  `Secondary`.
- A una colección: la lista de colecciones no archivadas por orden de creación (título en `Ink`) y, al
  final, una fila de captura con la pista `newCollection` (Nueva colección): escribir un título e Intro
  la crea (`createCollection`) y migra a ella en el mismo gesto.

**Programar** (`schedule`): arriba, el campo `dayField` opcional (Día, opcional); debajo, un mes por
fila desde el siguiente al actual, `FUTURE_MONTHS` (6) y `showMoreMonths` (Ver más meses) de seis en
seis hasta `FUTURE_MONTHS_MAX` (24), cada mes en `Body` (`monthTitle`: Octubre 2026). Tocar un mes
programa con el día escrito o sin día. Un día que no cabe en ese mes no hace nada y enseña
`dayOutOfRange`.

### 5.8 La línea de deshacer

Abajo, encima de la barra de pestañas o de la fila de accesorios, durante `UNDO_MS` (5 s): `2u`,
`background` liso con línea superior de 1 dp `outlineVariant`, el texto en `Body` desde x 24
(`entryDeleted`: Entrada borrada.; `collectionDeleted`: Colección borrada.; `rowDeleted`: Fila
borrada.) y `undo` (Deshacer) a la derecha en `primary`. Un segundo borrado sustituye la línea y deja
firme el primero. Sin tarjeta, sin temporizador visible.

---

## 6. Hoy

`ui/TodayScreen.kt` (#21). El Daily Log: `ofDay(d)` del día que se ve (`docs/tecnico.md` 6.5).

### 6.1 Composición

```
+------------------------------------------------+
|                    [SHARE] [KEY] [SRCH] [SET]  |  iconos, 2u
|  o Miércoles 23                       [<] [>]  |  PageTitle, 2u; punto de portada en x 36
|    septiembre de 2026                          |  Secondary, u
|                                                |
|    Agosto sin cerrar: 4 abiertas               |  franja de avisos (6.3)
|                                                |
| *  .  Llamar al fontanero                      |  Daily Log de d
|                                                |
|    o  Cena con Ana                             |
|                                                |
|    x  Comprar tinta                            |
|                                                |
|    .  |Escribe aquí                            |  fila de captura, con el foco
|                                                |
|    EN EL CALENDARIO                            |  Eyebrow, solo si hay Monthly(mes, día)
|    .  Pagar el alquiler                        |
+------------------------------------------------+
| . Tarea   o Evento   - Nota            480/500 |  fila de accesorios
+------------------------------------------------+
|                   teclado                      |
```

- **Cabecera.** Iconos: `SHARE` si `ofDay(d)` no está vacío (sección 17), `KEY`, `SEARCH`, `SETTINGS`.
  Título `dayTitle(d)` (Miércoles 23). Si `d` es hoy, a su izquierda, centrado en x 36 y en su línea
  base, un punto de 8 dp del color de la portada. A la derecha, `BACK` y `FORWARD`. Subtítulo
  `monthYear(d)` (septiembre de 2026) y, si `d` no es hoy, a su derecha la acción `backToToday` (Volver
  a hoy). Tocar el título cuando `d` no es hoy también vuelve a hoy.
- **Días que se ven.** Hacia atrás, sin límite. Hacia delante, hasta mañana: `Daily(mañana)` es un
  destino de migración y hay que poder verlo. Pasado mañana y más allá son de Mes y de Futuro. En
  mañana, `FORWARD` no se pinta y el deslizamiento no avanza.
- **Lista.** Las entradas de `Daily(d)`. Después, la fila de captura, que escribe en `Daily(d)`.
- **El calendario del mes.** Si hay `Monthly(monthOf(d), d.day)`, debajo de la captura, con una fila en
  blanco: `calendarToday` (EN EL CALENDARIO) en `Eyebrow` y esas entradas, con los mismos gestos.
  Arrastrar no las mezcla con las del Daily Log.
- **Foco.** Al abrir Hoy (arranque, pestaña, enlace con `focus`, vuelta desde otra pantalla) la captura
  tiene el foco y el teclado sube sin ningún toque, también con el día lleno: la página se desplaza
  hasta ella. Cambiar de día con las flechas o el gesto conserva el foco en la captura del día nuevo.
- Nunca se copian las tareas abiertas de ayer: están en su día, y la franja de avisos lo dice (6.3).

### 6.2 Estados

| Estado | Qué se ve |
|---|---|
| A. Primera vez (el diario no tiene ninguna entrada) | Cabecera, la captura enfocada con el teclado arriba y, debajo, en `Secondary`, `prefixHint` (Sin prefijo, tarea. - nota, o evento, * prioridad.). Nada más. La pista no vuelve en cuanto existe la primera entrada: no hay bandera, sale de los datos |
| B. Día vacío | La captura enfocada con `captureHint`. Nada invita a configurar nada |
| C. Día con entradas | La lista y la captura enfocada al final |
| D. Otro día | Igual que C o B, sin franja de avisos, con `backToToday`. Un día pasado se edita igual que hoy |
| E. Tras el primer bullet de la instalación | La oferta del recordatorio en la franja (6.3), una sola vez |
| F. Cualquier aviso | Su línea en la franja (6.3) |

### 6.3 Franja de avisos de Hoy

Solo cuando se ve hoy. Todas las que apliquen, en este orden:

| Línea | Cuándo | Texto y acciones |
|---|---|---|
| Diario dañado | la carga puso el diario en cuarentena (`docs/tecnico.md` 6.14), hasta que se descarte | `noticeCorrupt` (No se ha podido leer el diario. Los ficheros se han guardado aparte y no se ha borrado nada.) y `ok` (Vale) |
| Guardado fallido | `saveFailed`, hasta el siguiente guardado bueno | `noticeSaveFailed` (No se ha podido guardar. Lo intento otra vez con tu próximo cambio.), sin acción |
| Oferta del recordatorio | tras guardar el primer bullet, si `reminderOffered` es falso; se pone a cierto al enseñarla | `offerReminder` (¿Te aviso para repasar el día a las 21:00?) con `notNow` (Ahora no) y `yes` (Sí). `yes` pide el permiso (`docs/tecnico.md` 6.12). Se va al contestar, al cambiar de día o al cerrar la app, y no vuelve |
| Mes sin cerrar | `unclosedMonth(hoy)` no es nulo | `unclosedMonth(mes, n)` (Agosto sin cerrar: 4 abiertas), entera en `primary`: abre REVIEW `Month(m)` |
| Abiertas de días anteriores | `openTasksBefore(hoy)` no vacía | `earlierOpen(n)` (Quedan 3 abiertas de días anteriores), entera en `primary`: abre REVIEW `Earlier(hoy)` |

---

## 7. Mes

`ui/MonthScreen.kt` (#24, #26). El Monthly Log de `m`: el calendario como lista y las tareas del mes
(`docs/tecnico.md` 6.5).

### 7.1 Composición

```
+------------------------------------------------+
|                           [SHARE] [SRCH] [SET] |
|    Septiembre                         [<] [>]  |  PageTitle
|    2026                                        |  Secondary
|                                                |
|    2 entradas del Future Log esperan           |  franja (7.2)
|                                                |
|  1 L                                           |  un día vacío: 2u
|  2 M                                           |
|  3 X     o  Dentista, 17:30                    |  el día con su primera entrada
|             .  Recoger las gafas               |  la segunda, debajo
|                                                |
|  4 J                                           |
| -------------------------------------------    |  separador de semana
|  7 L                                           |
|  ...                                           |
| 30 X                                           |
|                                                |
|    TAREAS DEL MES                              |  Eyebrow
|          .  Renovar el pasaporte               |
|          .  |                                  |  captura de las tareas del mes
+------------------------------------------------+
```

- **Cabecera.** Iconos: `SHARE` si el mes tiene alguna entrada en su calendario o en sus tareas,
  `SEARCH`, `SETTINGS`. Título `monthName(m)` (Septiembre), subtítulo el año. `BACK` y `FORWARD`
  cambian de mes: hacia atrás hasta el mes más antiguo con contenido (o el actual si no hay ninguno),
  hacia delante hasta el actual. Los meses que vienen son de Futuro. Se abre en el mes actual y
  desplazado hasta la fila de hoy.
- **Calendario.** Una fila por día de 1 a `monthDays(m)` (28, 29, 30 o 31, sin ajuste manual). En la
  columna de fecha (1.4), el número en `Body` con cifras tabulares, y la inicial `S.weekdayInitial` en
  `Secondary`. El número de hoy en `onBackground`; los demás en `onSurfaceVariant`. Un día sin entradas
  ocupa `2u`. Con entradas, la primera va en la misma fila que el número y las siguientes debajo, con
  la anatomía de 5.1 corrida 48 dp.
- **Semanas.** Antes de cada día de `weekStarts(m, firstDayOfWeek(...))` salvo el 1, una línea de 1 dp
  `outlineVariant` de lado a lado de la página, en el borde de arriba de su fila. El primer día de la
  semana es el del ajuste o, sin ajuste, el del sistema.
- **Escribir en un día.** Tocar la fila de un día (su número, su inicial o el hueco a la derecha)
  abre una fila de captura debajo de sus entradas, con el foco y el teclado, que escribe en
  `Monthly(m, día)`. Es la misma captura de Hoy, con su fila de accesorios. Intro deja el foco en ese
  mismo día; atrás o tocar fuera la cierra.
- **Tareas del mes.** Tras una fila en blanco, `monthTasks` (TAREAS DEL MES) en `Eyebrow`, las
  `Monthly(m, null)` y al final su fila de captura, siempre presente, que escribe en `Monthly(m, null)`.
  Una entrada con día nunca aparece aquí.
- Mes se abre sin foco ni teclado: se lee antes de escribir.

### 7.2 Franja de avisos de Mes

Solo en el mes actual. Todas las que apliquen, en este orden, enteras en `primary`:

| Línea | Cuándo | Lleva a |
|---|---|---|
| `unclosedMonth(mes, n)` (Agosto sin cerrar: 4 abiertas) | `unclosedMonth(hoy)` no es nulo | REVIEW `Month(m)` |
| `futureWaiting(n)` (2 entradas del Future Log esperan) | `futureWaiting(hoy)` no vacía y `settings.futureSeen != monthOf(hoy)` | REVIEW del Future Log (11.4). El número baja con cada decisión |

### 7.3 Estados

| Estado | Qué se ve |
|---|---|
| Mes vacío | Las filas de todos los días, `TAREAS DEL MES` y su captura con `captureHint`. El mes nace vacío: nada del anterior se copia ni se sugiere |
| Mes pasado | Sin franja, sin número de hoy destacado. Se escribe igual |
| Con avisos | Su franja |

---

## 8. Futuro

`ui/FutureScreen.kt` (#25). El Future Log: bloques de mes desde el siguiente al actual.

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|    Futuro                                      |  PageTitle
|                                                |
|    OCTUBRE 2026                                |  Eyebrow
| 14 X     o  Cumpleaños de Ana                  |  con día
|          .  Revisar el seguro                  |  sin día
| [día]    .  |                                  |  captura del bloque
|                                                |
|    NOVIEMBRE 2026                              |
| [día]    .  Escribe aquí                       |  bloque vacío
|  ...                                           |
|                                                |
|    Ver más meses                               |  primary
+------------------------------------------------+
```

- Sin subtítulo. `futureMonths(hoy, n)`: seis bloques y `showMoreMonths` (Ver más meses) añade seis
  cada vez hasta 24; en 24 la acción desaparece.
- **Bloque.** `monthTitle(m)` en `Eyebrow` (OCTUBRE 2026); las `Future(m, día)` por día ascendente, con
  el día y su inicial en la columna de fecha; después las `Future(m, null)` por `order`, con la columna
  de fecha vacía; al final, la fila de captura del bloque. Una fila en blanco entre bloques.
- **Captura con día.** En la captura de un bloque, la columna de fecha es un campo numérico de dos
  cifras con la pista `dayField` (día) en `onSurfaceVariant`, 48 x 48. Vacío, la entrada va sin día.
  Un día fuera de `1..monthDays(m)` no crea nada al pulsar Intro y enseña `dayOutOfRange(mes, n)`
  (Noviembre no tiene día 31.) en `Secondary` bajo la fila. Nunca un selector de calendario.
- El Future Log no avisa ni mueve nada solo. Futuro se abre sin foco.
- Programar desde cualquier sitio (5.7) aterriza aquí.

| Estado | Qué se ve |
|---|---|
| Futuro vacío | Los seis bloques con su etiqueta y su captura con `captureHint`. Nada más |
| Un bloque vacío | Su etiqueta y su captura con `captureHint` |

---

## 9. Índice

`ui/IndexScreen.kt` (#29, #30). Meses con contenido y colecciones, por orden de creación
(`indexItems`, `docs/tecnico.md` 6.7).

```
+------------------------------------------------+
|                                  [SRCH] [SET]  |
|    Índice                                      |  PageTitle
|                                                |
|    Filtrar por título                          |  campo, Ink
|                                                |
|    Septiembre 2026 . . . . . . . . . . . Mes   |
|    Lecturas 2026 . . . . . . . . .  Colección  |
|    Agua . . . . . . . . . . . . . Seguimiento  |
|    Octubre 2026 . . . . . . . . . . . . . Mes  |
|    Nueva colección                             |  captura, Ink en onSurfaceVariant
|    Nuevo seguimiento                           |  captura
|                                                |
|    Archivadas (2)                              |  Body, onSurfaceVariant
+------------------------------------------------+
```

- **Filtro.** Un campo de `2u` en `Ink`, sin borde, con la pista `indexFilterHint` (Filtrar por título)
  y `CLOSE` a la derecha cuando tiene texto. `filterIndex`: sin distinguir mayúsculas ni acentos. Solo
  aparece si hay algún elemento.
- **Fila** (`2u`). Título en `Ink` desde x 48 (en v1.2, el icono en la columna de bullets, 23.9);
  una línea de puntos guía, círculos de 1,5 dp cada 6 dp en `onSurfaceVariant` sobre la línea base,
  de 8 dp tras el título a 8 dp antes de la etiqueta; a la derecha, en `Secondary`, `indexMonth` (Mes),
  `indexCollection` (Colección) o `indexTracker` (Seguimiento). Un título largo hace salto de línea y
  los puntos guía van en su última línea. Sin números de página.
- Tocar un mes abre la pestaña Mes en ese mes. Tocar una colección abre COLLECTION.
- **Crear.** Tras el último elemento activo, dos filas de captura: `newCollection` (Nueva colección) y
  `newTracker` (Nuevo seguimiento). Solo piden el título: Intro lo crea (`createCollection`, con
  `kind = TRACKER` en la segunda) y abre la colección con su captura enfocada. Sin plantillas ni campos
  más. Sin Pro y con un seguimiento ya creado (`canCreateTracker` falso), tocar `newTracker` abre el
  `ProDialog` en vez de enfocar el campo, y no se crea nada.
- **Archivadas.** Al final, `archivedToggle(n)` (Archivadas (2)) en `Body` `onSurfaceVariant`, plegado.
  Tocarlo despliega debajo las archivadas, con el título en `onSurfaceVariant`. Se pliega al salir.
- Filtrando, las filas de crear y las archivadas que no casan se esconden.

| Estado | Qué se ve |
|---|---|
| Índice vacío (diario vacío) | El título, `indexEmpty` (Los meses aparecen aquí en cuanto escribes en ellos.) en `Secondary`, y las dos filas de crear. Sin filtro |
| Filtro sin resultados | `indexNoMatch` (Ningún título con esas letras.) en `Secondary` |

En ancho (sección 21) las filas se reparten en doble página.

---

## 10. Colección

`ui/CollectionScreen.kt` (#30, #50). La misma lista y la misma captura que Hoy (#21, #22).

### 10.1 Colección de notas

```
+------------------------------------------------+
|  [<]                             [SHARE] [...] |
|    Lecturas 2026                               |  PageTitle, se toca para renombrar
|    Viene de Lecturas 2025                      |  v1.1 (23.1), primary
|                                                |
|    .  El infinito en un junco                  |
|    x  Klara y el Sol                           |
|    .  |                                        |  captura
+------------------------------------------------+
```

- **Cabecera.** `BACK`; `SHARE` si tiene entradas; `MORE`. Título en `PageTitle`. Tocar el título lo
  edita en línea como una entrada (`renameCollection`, `oneLine`, tope `COLLECTION_TITLE_MAX` 60);
  vacío, no guarda. Renombrar no cambia su puesto en el Índice.
- **Lista y captura** como en Hoy, en `InCollection(id)`. Se abre con la captura enfocada si viene de
  crearla; si no, sin foco.
- **`MORE`** abre una hoja (5.6) con: `archive` (Archivar) o `unarchive` (Sacar del archivo);
  `deleteCollection` (Borrar colección); v1.1 `continueCollection` (Continuar en una colección nueva,
  23.1); v1.2 `iconAndTheme` (Icono y tema, 23.9). Archivar no toca ninguna entrada y vuelve al Índice.
  Borrar no pide confirmación: vuelve al Índice con la línea de deshacer (`collectionDeleted`).
- Una colección archivada se abre y se escribe igual, con `archivedNote` (Archivada.) como subtítulo;
  no es destino de migración.

| Estado | Qué se ve |
|---|---|
| Colección vacía | El título y la captura con `captureHint`. Nada más |

### 10.2 Seguimiento

Una `BulletCollection` con `kind = TRACKER` (`docs/tecnico.md` 6.18): filas que define el usuario, con
una marca por día.

```
+------------------------------------------------+
|  [<]                             [SHARE] [...] |
|    Agua                                        |  PageTitle
|    septiembre de 2026                [<] [>]   |  Secondary; páginas del hilo
|                                                |
|    [<]   Miércoles 23   [>]                    |  el día marcado, Body
|                                                |
|    Dos litros                            (o)   |  Ink y la celda del día, 48 x 48
|    . . o . o o . o o o . . . . . . . . . . .   |  la tira del mes
|                                                |
|    Estirar                               ( )   |
|    o . . o . . o . . o . . o . . . . . . . .   |
|                                                |
|    Nueva fila                                  |  captura
+------------------------------------------------+
```

- **Página.** Subtítulo `monthYear` del mes de la página; `BACK` y `FORWARD` a su derecha van a la
  página anterior y a la siguiente del hilo, y no se pintan si no existen. Se abre en la página del mes
  actual (`trackerPage`, que no se guarda hasta la primera marca o el primer cambio de filas).
- **Día marcado.** Una fila de `2u` con `BACK`, `dayTitle(d)` en `Body` y `FORWARD`. Por defecto, hoy
  si la página es del mes actual; el último día del mes en una página pasada. No sale del mes de la
  página. El gesto 3 (deslizar) también lo cambia.
- **Fila del seguimiento** (`TrackerRow`). Título en `Ink` desde x 48. A la derecha, la celda del día
  marcado, 48 x 48: marcada, un círculo relleno de 10 dp `onBackground`; sin marcar, una circunferencia
  de 10 dp de trazo 1,5 dp `onSurfaceVariant`. Un toque la alterna. Debajo, en una fila de `u`, la tira
  del mes a lo ancho de la columna de texto: una celda por día de `min(8 dp, ancho / días)`; día
  marcado, punto de 5 dp `onBackground`; sin marcar, punto de 2 dp `onSurfaceVariant`; el día marcado
  lleva la marca de 2 dp debajo. La tira no responde: se marca en la celda. Después, una fila en blanco.
- **Gestos.** Tocar el título edita el nombre (5.4). Pulsación larga sobre una fila abre una hoja con
  `actionEdit` y `rowDelete` (Borrar fila, con deshacer `rowDeleted`). Arrastrar reordena las filas.
- **Crear filas.** Al final, una captura con la pista `trackerRowHint` (Nueva fila). Sin prefijos.
- **Pro.** Sin Pro, los seguimientos que ya existen se leen y se editan igual, y sus páginas nuevas se
  siguen creando (`docs/tecnico.md` 6.18).
- Sin números: ni "12 de 30", ni porcentajes, ni rachas.

| Estado | Qué se ve |
|---|---|
| Seguimiento nuevo | El título, el mes, el día marcado y la captura con `trackerRowHint` enfocada |
| Página de un mes nuevo sin marcas | Las mismas filas con las tiras vacías: nada copiado del mes pasado |

---

## 11. Revisar

`ui/ReviewScreen.kt` (#26, #27, #28). Una cosa por pantalla. Los alcances y las consultas, en
`docs/tecnico.md` 6.6; en v1.1 y v1.2 la misma pantalla sirve al Nuevo cuaderno (23.2), al resumen del
año (23.8) y a importar de las hermanas (23.10).

Cabecera común: `CLOSE` a la izquierda (sale conservando lo decidido: cada decisión ya cambió su
tarea) y, en el paso de tareas, la posición `reviewPosition(i, n)` (3 de 12) en `Secondary` a la
derecha de la fila de iconos. Sin barra de progreso, sin porcentajes.

### 11.1 Paso 1, releer

```
+------------------------------------------------+
|  [x]                                           |
|    Releer septiembre                           |  PageTitle
|                                                |
|    LUNES 14                                    |  Eyebrow por día
|    .  Llamar al fontanero                      |  solo lectura
|    o  Cena con Ana                             |
|    ...                                         |
|    TAREAS DEL MES                              |
|    .  Renovar el pasaporte                     |
|                                                |
|    -  Una nota sobre este mes                  |  campo de nota, Ink
+------------------------------------------------+
|    Saltar                                      |  barra de 2u, primary
+------------------------------------------------+
```

- Título `reflectTitle(periodo)`: Releer septiembre (mes) o Releer hasta hoy (día y días anteriores).
- El periodo en modo lectura, con el aspecto de sus páginas: por cada día con entradas, su fecha
  `dayTitle` en `Eyebrow` (LUNES 14) y sus entradas; en un mes, después, `calendarTitle`
  (CALENDARIO) con las líneas de día que tienen algo, y `monthTasks`. Sin gestos, sin ningún número.
- Al final, un campo de nota en `Ink` con el glifo de nota en gris y la pista `reflectHint(periodo)`
  (Una nota sobre este mes, si quieres). En v1.1 la pista es la pregunta del día (23.3).
- Barra fija abajo, `2u`, con una sola acción a la izquierda: `skip` (Saltar) con el campo vacío;
  `saveAndGo` (Guardar y seguir) con texto, que crea la `NOTE` en `Monthly(m, null)` o en
  `Monthly(mes, hoy.day)` (`docs/tecnico.md` 6.6).

### 11.2 Paso 2, una tarea cada vez

```
+------------------------------------------------+
|  [x]                                  3 de 12  |
|    DEL LUNES 14                                |  Eyebrow: de dónde viene
|                                                |
| *  .  Llamar al fontanero por                  |  PageTitle, glifo y signifiers
|       la gotera                                |
|       Migrada 3 veces                          |  Secondary, desde 2
|                                                |
|    x  Hecha                                    |  cinco acciones, 2u cada una
|    >  Migrar                                   |
|    <  Programar                                |
|    >  A una colección                          |
|    -.-  Descartar                              |
+------------------------------------------------+
```

- **Origen** en `Eyebrow`: `fromDay(fecha)` (DEL LUNES 14), `fromMonthTasks(mes)` (TAREAS DE AGOSTO),
  `fromCalendar(fecha)` (CALENDARIO, 3 DE AGOSTO).
- **La tarea** en `PageTitle`, con su glifo y sus signifiers centrados en la primera línea base; hace
  salto de línea sin cortarse. Desde `MIGRATION_SHOWN_FROM` (2), `migratedTimes(n)` (Migrada 3 veces)
  debajo en `Secondary`. Es la única presión, y es la del método.
- **Cinco acciones**, apiladas, una por fila de `2u`: el glifo en la columna de bullets en
  `onBackground` y el nombre en `Body` `primary`. `reviewDone` (Hecha, aspa), `reviewMigrate` (Migrar,
  `>`), `reviewSchedule` (Programar, `<`), `reviewToCollection` (A una colección, `>`) y
  `reviewDiscard` (Descartar, el punto tachado). Migrar, Programar y A una colección abren el selector
  de destino (5.7) en el mismo sitio; las otras dos deciden con un toque y son reversibles desde la
  hoja (Reabrir).
- Decidir enseña la tarea siguiente al instante. La cola se recalcula al abrir, así que las ya
  decididas no vuelven. Ningún botón decide más de una.

### 11.3 Fin

Sin tareas que quedan (también si se abre y ya no queda ninguna): `reviewAllDecided` (Todo decidido.)
en `Body` y `close` (Cerrar) en `primary`. Si el alcance era un mes, `monthClosed(mes)` (Agosto,
cerrado.) en su lugar; la primera vez se pide la valoración al sistema (`docs/tecnico.md` 6.6), que
decide si la enseña.

### 11.4 Revisión del Future Log

Desde la línea `futureWaiting` de Mes. Sin paso de releer. Una entrada cada vez (tareas abiertas,
eventos y notas), con el origen `fromFuture(mes, día?)` (FUTURO, 14 DE SEPTIEMBRE) y tres acciones:
`futureToCalendar` (Pasar al calendario, `>`), `futureLeave` (Dejarla, sin glifo) y `futureDiscard`
(Descartar: una tarea queda tachada; un evento o una nota se borra con la línea de deshacer). Al final,
`futureAllDecided` (El Future Log de este mes está al día.) y `close`.

---

## 12. Buscar

`ui/SearchScreen.kt` (#35). Todo el diario en memoria (`search`, `docs/tecnico.md` 6.8).

```
+------------------------------------------------+
|  [<]                                           |
|    |cafe                                  [x]  |  campo, Ink, con el foco
|                                                |
|    . Abiertas   * Prioridad   ! Inspiración    |  filtros, Body
|    (o) Explorar                                |
|                                                |
|    MARTES, 22 DE SEPTIEMBRE DE 2026            |  Eyebrow: grupo, se toca
|    .  Café con Marta #trabajo                  |
|                                                |
|    LECTURAS 2026                               |
|    -  El café de los libros                    |
+------------------------------------------------+
```

- **Campo** en la fila de título: `Ink`, sin borde, con el foco y el teclado al abrir, pista
  `searchHint` (Una palabra o una #etiqueta), `CLOSE` a la derecha cuando tiene texto (vacía). Los
  resultados salen mientras se escribe; la tecla de buscar del teclado solo lo baja.
- **Filtros**, en una o varias filas: `filterOpen` (Abiertas, con el punto), `signifierPriority`
  (Prioridad), `signifierInspiration` (Inspiración), `signifierExplore` (Explorar), cada uno con su glifo y
  su nombre en `Body`, 48 de alto. Activo: `onBackground` con la marca de 2 dp; inactivo:
  `onSurfaceVariant`. Se combinan con Y.
- **Grupos.** Etiqueta en `Eyebrow`, en una fila de `2u` que se toca para abrir su página: un día,
  `longDateWithYear` (MARTES, 22 DE SEPTIEMBRE DE 2026), abre Hoy en ese día; un mes,
  `monthGroup(mes)` (SEPTIEMBRE 2026) abre Mes; `futureGroup(mes)` (FUTURO, OCTUBRE 2026) abre
  Futuro; una colección, su título, abre COLLECTION. Debajo, sus entradas con los gestos de siempre
  salvo arrastrar.
- Una `#etiqueta` se busca escribiéndola: sin gestor ni pantalla aparte.

| Estado | Qué se ve |
|---|---|
| Sin texto ni filtros | `searchEmpty` (Busca una palabra, o escribe # y una etiqueta.) en `Secondary`. Sin resultados |
| Sin resultados | `searchNothing` (Nada con esas palabras.) en `Secondary` |
| Solo filtros | Todo lo que cumple los filtros |

---

## 13. Clave

`ui/KeyScreen.kt` (#31). La *key page* del cuaderno: una sola página de lectura. Se cierra con `BACK`
o atrás sin marcar nada como visto.

```
+------------------------------------------------+
|  [<]                                           |
|    Clave                                       |  PageTitle
|                                                |
|    BULLETS                                     |
|    .  Tarea          Sin prefijo               |
|    o  Evento         Empieza con o y espacio   |
|    -  Nota           Empieza con - y espacio   |
|                                                |
|    ESTADOS                                     |
|    x  Hecha          Toca el punto             |
|    >  Migrada        Mantén pulsada: Migrar    |
|    <  Programada     Mantén pulsada: Programar |
|    -.-  Descartada   Mantén pulsada: Descartar |
|                                                |
|    SIGNIFIERS                                  |
| *     Prioridad      Empieza con * y espacio   |
| !     Inspiración    Empieza con ! y espacio   |
| (o)   Explorar       Empieza con ? y espacio   |
|                                                |
|    GESTOS                                      |
|    Tocar el glifo completa una tarea.          |
|    ...                                         |
+------------------------------------------------+
```

- Secciones `keyBullets` (BULLETS), `keyStates` (ESTADOS), `keySignifiers` (SIGNIFIERS) y `keyGestures`
  (GESTOS) en `Eyebrow`, `2u` entre ellas.
- Cada fila, `2u`: el glifo en su columna (los signifiers en el margen), el nombre en `Body` desde x 72
  y, debajo del nombre, cómo se escribe o se hace en `Secondary` (`keyTaskHow`, `keyEventHow`,
  `keyNoteHow`, `keyDoneHow`, `keyMigratedHow`, `keyScheduledHow`, `keyDiscardedHow`,
  `keyPriorityHow`, `keyInspirationHow`, `keyExploreHow`). Una migrada lleva además
  `keyMigratedLink` (Toca el > para ir a donde fue.).
- GESTOS: cuatro líneas en `Body`, `keyGestureTap`, `keyGestureHold`, `keyGestureSwipe` y
  `keyGestureDrag`, más `keyTapText` (Toca el texto para editarlo.).
- No es un tutorial: nada la abre sola y no tiene pasos.

---

## 14. Ajustes

`ui/SettingsScreen.kt` (#32, #33). Todo tiene valor por defecto: es la única pantalla que se puede no
visitar nunca. Todo lo que guarda va a `Journal.settings`, salvo Pro (`Prefs`).

```
+------------------------------------------------+
|  [<]                                           |
|    Ajustes                                     |  PageTitle
|                                                |
|    DÍA                                         |
|    El día empieza                              |
|    A las 04:00                                 |
|    La semana empieza                           |
|    El lunes, como el sistema                   |
|                                                |
|    RECORDATORIO                                |
|    Recordatorio para repasar el día     [OFF]  |
|    Apagado                                     |
|                                                |
|    PRIVACIDAD                                  |
|    Bloquear el diario                   [OFF]  |
|    Pide tu cara, tu huella o el código         |
|                                                |
|    CUADERNO                                    |
|    +----------------------------+              |  vista previa, radio 12
|    | o Miércoles 23             |              |
|    |   .  Comprar tinta         |              |
|    |   o  Cena con Ana          |              |
|    |   Hoy                      |              |
|    +----------------------------+              |
|    (o) (o) (o) (v)                             |  portadas
|    (o) (o) (o) (o)                             |
|    [:] [=] [#] [ ]                             |  papeles
|    Salvia, punteado                            |
|                                                |
|    COPIA                                       |
|    Exportar copia                              |
|    Importar copia                              |
|                                                |
|    BOBBIN PRO / MÁS APPS / ACERCA DE           |
|                                                |
|    Borrar todos los datos                      |
+------------------------------------------------+
```

Secciones con etiqueta `Eyebrow`, `2u` entre secciones. Cada fila, `2u` como mínimo: título en `Body`,
subtítulo en `Secondary`, interruptor a la derecha (`Switch` de Material3 con `primary`); toda la fila
responde. En ancho, la página de 576 centrada (sección 21).

| Fila | Estados |
|---|---|
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

Secciones: `sectionDay` (DÍA), `sectionReminder` (RECORDATORIO), `sectionPrivacy` (PRIVACIDAD),
`sectionNotebook` (CUADERNO), `sectionBackup` (COPIA), `sectionPro` (BOBBIN PRO), `sectionMoreApps`
(MÁS APPS), `sectionAbout` (ACERCA DE). v1.1 añade a CUADERNO `newNotebookRow` y `bookRow` (23.2,
23.6); v1.2, `yearSummaryRow` y a COPIA `syncRow` (23.7, 23.8).

### 14.1 Portada y papel

(#49.) Todas se previsualizan sin comprar.

- **Vista previa.** Una página de muestra del ancho de la columna de texto y `8u` de alto, radio 12,
  borde 1 dp `outline`, con el papel y la portada que se están mirando: el punto de la portada y
  `dayTitle` de un miércoles 23 (Miércoles 23) en `PageTitle`, dos entradas fijas (`previewTask`: Comprar tinta, tarea;
  `previewEvent`: Cena con Ana, evento) y `tabToday` (Hoy) con la marca de 2 dp de la portada.
- **Portadas.** Ocho círculos de 32 dp en dianas de 48, en dos filas de cuatro, en el orden de
  `docs/tecnico.md` 5. El que se está mirando lleva un anillo de 2 dp `onBackground` a 3 dp; el
  guardado, `CHECK` en `onBackground` al 70 % dentro. Cada uno se describe con `coverName(id)` y, si es
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

(#43, `docs/tecnico.md` 6.10.) Gratis siempre. Lo elige el usuario: el `SHARE` de Hoy comparte ese
día, el de Mes ese mes (calendario y tareas), el de una colección esa colección. La app nunca sugiere
qué compartir.

### 17.1 La hoja

`ui/ShareScreen.kt` es una hoja inferior, no un destino: la primera página a escala (ancho de la
columna, radio 12, borde 1 dp `outline`) y, si hay más, `sharePages(n)` (3 imágenes) en `Secondary`.
Dos acciones de `2u`: `shareImage` (Compartir como imagen) y `shareText` (Compartir como texto), que
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
`docs/tecnico.md` 4.4, sin el `- ` de lista. Al final, una línea en blanco y `Bobbin`.

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

- **Página.** Hoy, Mes, Colección, Revisar, Buscar, Clave y Ajustes: una página de como mucho
  `MAX_CONTENT_WIDTH` (576 dp, 24 columnas de puntos) centrada, con el borde izquierdo en un múltiplo de
  24. A los lados sigue el papel con la misma rejilla.
- **Doble página.** Índice y Futuro, como un cuaderno abierto: dos columnas de hasta 576 dp separadas
  por un lomo de 48 dp (una línea vertical de 1 dp `outlineVariant` en su centro), el conjunto centrado
  y de como mucho 1200 dp. Futuro reparte los bloques de mes alternando izquierda y derecha, en orden;
  el Índice llena la izquierda y sigue en la derecha, en orden de creación. Por debajo de 600, una
  columna.
- **Barra de pestañas** a todo el ancho, con las cuatro etiquetas repartidas en el ancho de la página
  centrada.
- **Hojas, diálogos y `ProDialog`**: ancho máximo 576, centrados.
- Nada más cambia: ni tamaños de letra ni diseños propios (SPEC §5). **[autor]** comprueba a mano en
  iPad y en un emulador de 10 pulgadas (test 47).

---

## 22. Accesibilidad

(#53.)

- **Cada entrada es un nodo** que lee `S.entryDescription(bullet, status, signifiers, text)`: Tarea
  hecha, prioridad: comprar pan. Una migrada añade su destino (`wentTo...`). Su acción principal es
  editar. Acciones personalizadas (`CustomAccessibilityAction` en Android, `accessibilityCustomActions`
  en iOS vía Compose): `a11yComplete` (Completar) o `a11yReopen` (Reabrir), `actionMigrate`,
  `actionSchedule`, `a11yMoveUp` (Subir) y `a11yMoveDown` (Bajar) como alternativa a arrastrar, y
  `a11yMoreActions` (Más acciones), que abre la hoja. Nada depende solo de la pulsación larga.
- **Iconos** con su descripción (sección 2). Pestañas: su nombre y `a11ySelected` en la activa.
- **Fila de captura**: `a11yCapture` (Nueva entrada) y el tipo elegido en la fila de accesorios.
- **Mes**: cada fila de día lee `a11yDayRow(día, nombre, n)` (3, jueves, 2 entradas) y su acción es
  escribir en él.
- **Seguimiento**: la celda lee `a11yTrackerCell(fila, fecha, marcada)` (Dos litros, 23 de septiembre,
  marcada); la tira no es un nodo, y la fila lee `a11yTrackerMarked(días)` (Marcados: 1, 2, 5 y 9),
  sin totales ni porcentajes.
- **Orden de lectura en Hoy**: iconos, título, subtítulo, franja de avisos, entradas, captura,
  calendario del día.
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
