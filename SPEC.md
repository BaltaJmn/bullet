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

_Pendiente: lo escribe #3._

---

## 2. El método, regla a regla

_Pendiente: lo escribe #3._

---

## 3. Funcionalidad por versión

_Pendiente: lo escribe #3._

---

## 4. Decisiones que se toman aquí, no en el código

_Pendiente: lo escribe #3._

---

## 5. Diseño

_Pendiente: lo escribe #3._

---

## 6. Enganche y retención

_Pendiente: lo escribe #3._

---

## 7. Monetización

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

## 8. Identidad y ficha

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

## 9. Cumplimiento de tienda

_Pendiente: lo escribe #3._

---

## 10. Arquitectura prevista y trampas heredadas

_Pendiente: lo escribe #3._

---

## 11. Plan de ataque

_Pendiente: lo escribe #3._

---

## 12. Riesgos abiertos

_Pendiente: lo escribe #3._
