# Compras y RevenueCat, paso a paso

El código lo deja escrito la issue #47 (`docs/tecnico.md` 6.16): `Billing` es el de line, busca un
derecho llamado **exactamente `pro`** y coge el **primer paquete de la oferta actual**. Si el nombre no
coincide o no hay oferta marcada como *Current*, el `ProDialog` sale sin precio y sin botón. Aquí queda
lo que no es código, en el orden en que se hace. Los menús de la Play Console con capturas de dónde
está cada cosa: `../../HabitTracker/store/revenuecat.md`. Los pasos que ya se hicieron para Quilt,
MoodTraker o Purl y sirven tal cual: `../line/store/revenuecat.md`.

---

## 0. Qué se vende, decidido

| | |
|---|---|
| Producto | Uno solo, compra única, **no consumible**. Nunca suscripción |
| Identificador | `bullet_pro` en las dos tiendas. **Irreversible**: un id borrado no se reutiliza |
| Nombre visible | Bobbin Pro |
| Precio | **1,99 EUR** de base en España, conversión automática al resto **con redondeo**. No sube con la v1.1 ni con la v1.2 (SPEC §7) |
| Descuento de lanzamiento | No |
| Países | Todos |
| Prueba gratuita | No. El plan gratis es la prueba |

Qué abre el pago hoy (v1.0, v1.1 y v1.2, todo ya en `main`), y solo esto (SPEC §7):

- **Siete portadas**: todas menos `sage`.
- **Tres papeles**: rayado, cuadrícula y liso; todos menos `dotted`.
- **Seguimientos a partir del segundo** (`FREE_TRACKER_LIMIT = 1`, cuenta los que existen ahora).
- **Widget del mes**, Android e iOS.
- **Widget de pantalla de bloqueo**, solo iOS.
- **El libro del cuaderno en PDF** (v1.1, #68): Ajustes, Cuaderno, libro. El diario entero maquetado
  como libro A5 imprimible, con portada, clave, índice y los resúmenes del año de apéndice.
- **Captura desde fuera** (v1.1, #67): baldosa de Ajustes rápidos en Android, y Siri y Atajos
  ("Anotar en Bobbin") en iOS. Cada una escribe una línea en hoy.
- **Sincronización por tu propia nube** (v1.2, #69): un único `bobbin-sync.json` en iCloud Drive (iOS)
  o en el fichero que el usuario elige con el selector del sistema, por ejemplo en Drive (Android).
  Sin cuenta ni servidor nuestros; si cambiaron los dos lados, pregunta (usar este, usar el otro o
  juntar tras un resumen), nunca fusiona en silencio.
- **El resumen del año en texto** (v1.2, #70): inspiraciones, prioridades hechas y las tareas que más
  viajaron.

Qué no se cobra nunca, porque lo prometen la ficha y la política: el método entero (rapid logging, los
cinco estados, la migración, la revisión y la reflexión), Hoy, Mes, Future Log e Índice, colecciones
ilimitadas, la clave de símbolos, la búsqueda, el recordatorio y el bloqueo, el widget de hoy,
compartir, exportar e importar, y una portada (salvia) y un papel (punteado). Y lo nuevo del método
en v1.1 y v1.2, que es método y por eso es gratis: continuar una colección en otra (threading), el
nuevo cuaderno anual, las preguntas de la reflexión guiada, las fechas en lenguaje natural al escribir
(se leen en el dispositivo), las variaciones del índice (icono y tema por colección o mes, filtro por
tema, meses ya migrados subrayados) e importar de Purl, MoodTraker y Quilt, entrada por entrada.

> Si cambia cualquiera de las dos listas, se tocan en el **mismo commit** las claves `pro*` de
> `docs/textos.md`, la descripción larga de las dos fichas (`listings/`, `app-store/`) y las
> descripciones del producto de abajo. Vender algo que la versión gratis ya da es tergiversación, y
> las dos tiendas lo tratan como tal.

Quien pierde Pro (un reembolso) no pierde nada de lo que tiene: los seguimientos ya creados siguen
legibles y editables y solo no se puede crear otro, la portada y el papel activos vuelven a los
gratis sin tocar el diario, y los widgets Pro se pintan bloqueados. Solo deja de poder añadir lo que
es de Pro.

## 1. El producto en Play

Precondiciones: **un AAB subido a algún canal** (`lanzamiento.md`, fase 3) y el perfil de pagos
verificado, que ya lo está desde Quilt.

1. Play Console, **dentro de Bobbin**: *Monetizar con Play > Productos > Productos únicos > Crear
   producto único*.
2. Id `bullet_pro`. Nombre y descripción por idioma, de la tabla (*Gestionar traducciones* añade
   los cuatro idiomas que no son el predeterminado). Categoría fiscal, la que trae: *Ventas de apps
   digitales*.
3. Opción de compra: id `bulletpro` (Play no admite guion bajo aquí), tipo *Comprar*, contenido
   digital, una unidad por compra.
4. Precio 1,99 EUR, *Convertir* al resto de países y **Redondear precios**: sin eso salen cifras que
   leen como un error de la tienda. Play no guarda el producto sin precio, ni como borrador.
5. **Activarlo.** Un producto inactivo no sale por la API y el diálogo se queda sin precio.

Play marca la primera opción de compra como *Retrocompatible*. Es lo normal y lo que necesita
RevenueCat.

| Idioma | Nombre | Descripción (tope 200) |
|---|---|---|
| en-US | Bobbin Pro | Seven more covers, lined, grid and blank paper, more than one tracker, month widget, PDF book, Quick Settings note, sync via your own cloud and year summary. Paid once. The method stays free. |
| es-ES | Bobbin Pro | Siete portadas más, papeles, más de un seguimiento, widget del mes, libro en PDF, anotar desde Ajustes rápidos, sincronización por tu nube y resumen del año. Pago único. El método sigue gratis. |
| pt-BR | Bobbin Pro | Mais sete capas, papéis, mais de um tracker, widget do mês, livro em PDF, anotar pelas Configurações rápidas, sincronização pela sua nuvem e resumo do ano. Pagamento único. O método segue grátis. |
| de-DE | Bobbin Pro | Sieben weitere Umschläge, Papiere, mehr als ein Tracker, Monats-Widget, PDF-Buch, Notieren per Schnelleinstellungen, Sync über deine Cloud, Jahresrückblick. Einmalzahlung. Methode bleibt kostenlos. |
| fr-FR | Bobbin Pro | Sept couvertures de plus, papiers, plus d'un suivi, widget du mois, livre PDF, noter via les réglages rapides, synchro via ton cloud et bilan de l'année. Paiement unique. La méthode reste gratuite. |

Ninguna llega a 200 caracteres. No nombran ni el widget de pantalla de bloqueo ni Siri y Atajos: en
Android no existen, y esta tabla es la de Play.

## 2. La cuenta de servicio de RevenueCat en Google

Es la que deja a RevenueCat preguntarle a Google si una compra es real. **Solo lee pedidos, no
publica**, y nunca es la misma que la de `ci.md`.

**La de Quilt sirve**: es `revenuecat@<proyecto>.iam.gserviceaccount.com`, con las tres APIs ya
activadas (Android Developer, Developer Reporting, Pub/Sub) y sus roles de Cloud puestos. Dos pasos:

1. Play Console, nivel de cuenta, *Usuarios y permisos*: si sus permisos son por app, añadirle Bobbin
   con los mismos cuatro de Quilt (ver información de la app, ver datos financieros, gestionar
   pedidos, gestionar la presencia en la tienda). Ninguno de publicar.
2. Google Cloud, pantalla *Claves* de esa cuenta: clave JSON nueva, que se sube tal cual en el paso 3.
   No se pega en un chat ni se guarda en el repositorio.

Hasta 36 horas para que Google acepte las credenciales. Mientras tanto RevenueCat da errores de
validación y no significa que esté mal montado.

## 3. El proyecto en RevenueCat

Un proyecto propio, **Bobbin**, separado de los de Quilt, MoodTraker y Purl: cada uno tiene su derecho
y su producto, y mezclarlos haría que comprar una desbloqueara otra.

1. *Create new project*, nombre Bobbin.
2. *Apps > + Play Store*: package `com.baltajmn.bullet`, sube el JSON del paso 2.
3. *Products > + New*: Play Store, `bullet_pro`.
4. *Entitlements > + New*: identificador **`pro`**, en minúsculas. *Attach* el producto.
5. *Offerings > + New*: identificador `default`, **márcala como Current**. Dentro, *+ Package* de
   tipo *Lifetime* (`$rc_lifetime`) con el producto.

Comprobación: si no hay oferta *Current* o esa oferta no tiene paquete, la app enseña el diálogo sin
precio.

## 4. Las claves

*Project settings > API keys*. Se copian las **públicas**:

| Plataforma | Prefijo | Dónde va |
|---|---|---|
| Android | `goog_` | `revenueCatApiKey` en `shared/src/androidMain/.../billing/Billing.android.kt` |
| iOS | `appl_` | `revenueCatApiKey` en `shared/src/iosMain/.../billing/Billing.ios.kt` |

Son públicas: viajan dentro del binario y cualquiera puede sacarlas. Van como literal en el código.
**La clave secreta `sk_...` no sale nunca del panel de RevenueCat**: ni en el código, ni en un secreto
de GitHub, ni en un chat. La app no la necesita.

Mientras la clave sea `null`, `Billing.configure()` no hace nada y la app funciona entera en modo
gratis. Así se puede trabajar y probar todo lo demás antes de tener el panel montado.

## 5. Probar una compra de verdad en Android

1. Play Console, nivel de cuenta, *Ajustes > Monetización > Licencia para testing*: el correo de
   Google del móvil de pruebas. Compra con el diálogo real y sin cargo.
2. Instalar **desde la prueba interna**, no por `adb`: una compra solo funciona si el binario viene de
   Play. En el emulador no hay Play Billing.
3. Comprar, y ver en RevenueCat, *Customer History*, el evento y el derecho `pro` activo.
4. Desinstalar, reinstalar, *Restaurar compra* desde Ajustes. Es el camino que más se rompe.
5. Reembolsar desde la Console y comprobar que, al volver a abrir la app, los widgets Pro se pintan
   bloqueados, la portada y el papel activos vuelven a los gratis, y los seguimientos ya creados
   siguen legibles.

## 6. iOS

En App Store Connect, dentro de Bobbin, *Monetización > Compras dentro de la app > +*:

| Campo | Valor |
|---|---|
| Tipo | No consumible |
| Nombre de referencia | Bobbin Pro |
| Id de producto | `bullet_pro` |
| Precio | País base **España**, 1,99 EUR; el resto, por la equivalencia automática de Apple |
| Disponibilidad | Todos los países |
| Captura para la revisión | El `ProDialog` abierto en un iPhone, desde el simulador |
| Nota para la revisión | `Unlocks seven cover colors, three papers, trackers past the first, the month widget, the lock screen widget, the notebook as a PDF book, noting with Siri and Shortcuts, sync through the user's own iCloud Drive and the year summary. One-time purchase. Restore Purchase is in Settings and in this dialog.` |

Nombre visible (tope 30) y descripción (tope 45) por idioma:

| Idioma | Nombre | Descripción |
|---|---|---|
| en-US | Bobbin Pro | Covers, papers, widgets, PDF, sync. Pay once. |
| es-ES | Bobbin Pro | Portadas, widgets, PDF, sync. Pago único. |
| pt-BR | Bobbin Pro | Capas, widgets, PDF, sync. Pagamento único. |
| de-DE | Bobbin Pro | Umschläge, Widgets, PDF, Sync. Einmalzahlung. |
| fr-FR | Bobbin Pro | Couvertures, PDF, synchro. Achat unique. |

Las cinco caben en el tope de 45. Con tan poco sitio no cabe todo: el detalle de qué incluye Pro lo
da la nota para la revisión de arriba y el `ProDialog`.

**La primera compra solo se revisa junto a una versión**: se adjunta en la página de la versión, en
*Compras dentro de la app*, antes de enviar a revisión.

En RevenueCat, mismo proyecto Bobbin:

1. *Apps > + App Store*, bundle id `com.baltajmn.bullet`, y la **In-App Purchase Key** (`.p8` de App
   Store Connect, *Usuarios y acceso > Integraciones > Compra dentro de la app*). Si el panel pide
   además el *App-Specific Shared Secret*, se genera en la página de la app y se pega también.
2. *Products*: `bullet_pro` de App Store.
3. Adjuntarlo al **mismo derecho `pro`** y al **mismo paquete** de la oferta `default`. Así quien
   compró en una plataforma restaura en la otra si usa el mismo identificador, y el código no
   distingue tiendas.
4. Copiar la `appl_...` al código (§4).

Probar en el sandbox: cuenta de sandbox en *Usuarios y acceso > Sandbox*, sesión iniciada en el
iPhone en *Ajustes > App Store > Cuenta de sandbox*, comprar, borrar la app, reinstalar y restaurar.

## 7. La v1.1 y la v1.2: el libro, la captura, la sincronización y el resumen del año entran en Pro sin cambiar el precio

`main` ya lleva la v1.1 y la v1.2. El precio se queda en 1,99 EUR (SPEC §7), el producto sigue siendo
`bullet_pro` y el derecho sigue siendo `pro`: no hay nada nuevo que crear en las tiendas ni en
RevenueCat.

- **Listas y descripciones**: las listas del §0 ya incluyen el libro en PDF y la captura desde fuera
  (v1.1) y la sincronización y el resumen del año en texto (v1.2). Lo mismo valen las tablas de
  descripciones del §1 (Play) y del §6 (App Store), los `pro*` de `docs/textos.md` y
  `store/listings/`. Lo demás de las dos versiones (threading, nuevo cuaderno, reflexión guiada, fechas
  en lenguaje natural, variaciones del índice, importar de las hermanas) es método y es gratis.
- **En las consolas**: cuando salga la versión, el autor pega las descripciones del §1 y del §6 en
  Play Console y App Store Connect, y la nota para la revisión del §6 en la versión que las incluya.
- **Quien ya compró** tiene todo lo anterior sin pagar de nuevo: es un no consumible y el derecho `pro`
  no caduca. Quien compre después paga lo mismo y recibe lo mismo.
