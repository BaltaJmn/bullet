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

_Pendiente: lo escribe #2._

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
