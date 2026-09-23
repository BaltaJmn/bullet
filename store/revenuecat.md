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
| Precio v1.0 | **7,99 EUR** de base en España, conversión automática al resto **con redondeo** |
| Precio desde v1.1 | **9,99 EUR**, el día que sale el libro del cuaderno en PDF (§7) |
| Descuento de lanzamiento | No |
| Países | Todos |
| Prueba gratuita | No. El plan gratis es la prueba |

Qué abre el pago en v1.0, y solo esto (SPEC §7):

- **Siete portadas**: todas menos `sage`.
- **Tres papeles**: rayado, cuadrícula y liso; todos menos `dotted`.
- **Seguimientos a partir del segundo** (`FREE_TRACKER_LIMIT = 1`, cuenta los que existen ahora).
- **Widget del mes**, Android e iOS.
- **Widget de pantalla de bloqueo**, solo iOS.

Qué no se cobra nunca, porque lo prometen la ficha y la política: el método entero (rapid logging, los
cinco estados, la migración, la revisión y la reflexión), Hoy, Mes, Future Log e Índice, colecciones
ilimitadas, la clave de símbolos, la búsqueda, el recordatorio y el bloqueo, el widget de hoy,
compartir, exportar e importar, y una portada (salvia) y un papel (punteado).

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

1. Play Console, **dentro de Bobbin**: *Monetizar con Play > Productos > Productos integrados en la
   aplicación > Crear producto*.
2. Id `bullet_pro`. Nombre y descripción por idioma, de la tabla.
3. Precio 7,99 EUR, *Convertir* al resto de países y **Redondear precios**: sin eso salen cifras que
   leen como un error de la tienda.
4. **Activarlo.** Un producto inactivo no sale por la API y el diálogo se queda sin precio.

Play deriva el id de la opción de compra quitando el guion bajo (`bulletpro`) y la marca
*Retrocompatible*. Es lo normal y lo que necesita RevenueCat.

| Idioma | Nombre | Descripción (tope 200) |
|---|---|---|
| en-US | Bobbin Pro | Seven covers, three papers, trackers past the first, the month widget and the lock screen widget. One-time payment, not a subscription. The method, the daily and monthly logs, export and the reminder stay free. |
| es-ES | Bobbin Pro | Siete portadas, tres papeles, seguimientos a partir del segundo, el widget del mes y el de pantalla de bloqueo. Pago único, no es una suscripción. El método, el diario y el mes, exportar y el recordatorio siguen gratis. |
| pt-BR | Bobbin Pro | Sete capas, tres papeis, rastreadores a partir do segundo, o widget do mes e o de tela de bloqueio. Pagamento unico, nao e assinatura. O metodo, o diario e o mes, exportar e o lembrete continuam gratis. |
| de-DE | Bobbin Pro | Sieben Umschlagfarben, drei Papiere, Tracker ab dem zweiten, das Monats-Widget und das Sperrbildschirm-Widget. Einmalzahlung, kein Abo. Die Methode, Tages- und Monatslog, Export und Erinnerung bleiben kostenlos. |
| fr-FR | Bobbin Pro | Sept couvertures, trois papiers, des suivis a partir du deuxieme, le widget du mois et celui de l'ecran verrouille. Paiement unique, pas d'abonnement. La methode, le jour et le mois, l'export et le rappel restent gratuits. |

El widget de pantalla de bloqueo no se nombra en Play: en Android no existe.

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
| Android | `goog_` | `revenueCatApiKey` en `shared/src/androidMain/.../data/Billing.android.kt` |
| iOS | `appl_` | `revenueCatApiKey` en `shared/src/iosMain/.../data/Billing.ios.kt` |

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
| Precio | País base **España**, 7,99 EUR; el resto, por la equivalencia automática de Apple |
| Disponibilidad | Todos los países |
| Captura para la revisión | El `ProDialog` abierto en un iPhone, desde el simulador |
| Nota para la revisión | `Unlocks seven cover colors, three papers, trackers past the first, the month widget and the lock screen widget. One-time purchase. Restore Purchase is in Settings and in this dialog.` |

Nombre visible (tope 30) y descripción (tope 45) por idioma:

| Idioma | Nombre | Descripción |
|---|---|---|
| en-US | Bobbin Pro | Covers, papers, trackers, widgets. Pay once. |
| es-ES | Bobbin Pro | Portadas, papeles, seguimientos, widgets. Pago único. |
| pt-BR | Bobbin Pro | Capas, papeis, rastreadores, widgets. Pagamento unico. |
| de-DE | Bobbin Pro | Umschlage, Papiere, Tracker, Widgets. Einmalzahlung. |
| fr-FR | Bobbin Pro | Couvertures, papiers, suivis, widgets. Achat unique. |

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

## 7. Subir a 9,99 EUR con la v1.1

Se hace **el día que la v1.1 se publica**, no antes: el precio sube porque crece lo que se da (#68).

- **Play**: *Productos integrados*, `bullet_pro`, precio 9,99 EUR, *Convertir* y *Redondear*. Cambia
  en unas horas.
- **App Store**: *Programación de precios* del producto, *Añadir cambio de precio* con fecha de
  inicio el día de publicación, país base España, 9,99 EUR.
- **Descripciones**: se añade el libro en PDF a la lista de Pro en la tabla del §1, en `pro*` de
  `docs/textos.md` y en las dos fichas, en el mismo commit que etiqueta la v1.1.
- Quien compró a 7,99 conserva Pro para siempre: es un no consumible y el derecho no caduca.
