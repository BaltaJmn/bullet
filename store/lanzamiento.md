# Lanzamiento, paso a paso

Checklist de Bobbin. **[autor]** es lo que solo puede hacer el autor (cuentas, contraseñas,
formularios, subidas por la Console) y **[código]** lo que queda hecho desde el repositorio. Cada
paso lleva la issue que lo cierra (`SPEC.md` 11).

Regla de la familia: **el código no marca la fecha de salida, la marcan los trámites.** La prueba
cerrada de 14 días y la verificación de Apple son las dos latencias largas; todo lo que es código cabe
dentro de ellas. Por eso la fase 1 va el primer día, no el último (#10).

Pasos de cuenta que ya se hicieron para Quilt, MoodTraker o Purl y no se repiten (perfil de pagos,
papeleo fiscal de Play, cuenta de servicio de RevenueCat): contados largo en
`../../HabitTracker/store/{lanzamiento,revenuecat}.md` y en `../line/store/{lanzamiento,revenuecat}.md`.
Aquí solo va lo que cambia de una app a otra.

---

## Identificadores, que son irreversibles

| Qué | Valor |
|---|---|
| `applicationId` de Android | `com.baltajmn.bullet` |
| Bundle id de iOS | `com.baltajmn.bullet` (`APP_BUNDLE_ID` en `iosApp/Configuration/Config.xcconfig`) |
| Bundle id del widget | `com.baltajmn.bullet.widget` |
| App Group | `group.com.baltajmn.bullet` |
| Producto de compra | `bullet_pro`, no consumible, en las dos tiendas |
| Derecho de RevenueCat | `pro` |
| Nombre visible | Bobbin |
| Título de ficha | `Bobbin: Bullet Journal` (y su traducción por idioma, `listings/`) |

En cuanto la primera build entra en cualquiera de las dos tiendas, los cinco primeros no cambian
nunca. Un identificador de producto borrado tampoco se reutiliza. El nombre visible sí se puede
cambiar después, pero cambiarlo con reseñas tira la búsqueda por nombre.

## Fase 0. Decisiones

Todas tomadas. El porqué de cada una, en `SPEC.md`.

- [x] Nombre: **Bobbin** (SPEC §8). Reservas si App Store Connect lo rechaza al crear la app:
      **Skein**, y despues **Selvage**. Cambiarlo es buscar `Bobbin` en `docs/textos.md`, `PRODUCT_NAME`
      del xcconfig, los ficheros de `listings/` y `app-store/`, y la política. Los identificadores de
      la tabla de arriba no cambian: el nombre solo vive en textos, ficha, icono, esquema `bobbin://`
      y nombres de fichero de copia.
- [x] Precio: **7,99 EUR** en v1.0, **9,99 EUR** desde v1.1, sin descuento de lanzamiento, precios
      regionales activados (SPEC §7, `revenuecat.md`).
- [x] Qué es Pro en v1.0: siete portadas, tres papeles, seguimientos a partir del segundo, widget del
      mes y widget de pantalla de bloqueo de iOS (SPEC §7).
- [x] iPad: sí, universal como las hermanas (SPEC §8). Obliga a capturas de iPad (#58).
- [x] Política de privacidad en `https://bullet.baltajmn.dev/`.

## Fase 1. El primer día: lo que tiene latencia

Arrancar todo esto antes de escribir una línea de código. Ninguno depende del código.

- [ ] **[autor] Crear la app en Play Console** con el nombre Bobbin, idioma por defecto `en-US`, app
      gratuita. El paquete se fija con el primer AAB, no aquí. (#7)
- [ ] **[autor] Comprobar la cuenta de Apple Developer**, ya abierta con las hermanas: no hace falta
      pagarla dos veces. Contratos, fiscalidad y datos bancarios ya están en App Store Connect. (#7)
- [ ] **[autor] Crear la app en App Store Connect** con el bundle id de la tabla (hay que registrarlo
      antes en *Certificates, Identifiers & Profiles*, con App Groups activado, y lo mismo para
      `com.baltajmn.bullet.widget`). **Es la comprobación de que el nombre Bobbin está libre**: si App
      Store Connect dice que el nombre ya está en uso, se aplica la reserva de la fase 0 ese mismo
      día. (#7)
- [ ] **[autor] Declarar la condición de comerciante (DSA)** en las dos consolas si no se hizo ya con
      una hermana. Es de cuenta, no de app: si ya está, se hereda. Apple retira de la UE las apps sin
      ella. (#7, #59)
- [ ] **[autor] Solicitar el Small Business Program de Apple** si no está ya activo desde una
      hermana: 15 % en vez de 30 % en las comisiones. Si llega tarde, las primeras ventas se cobran al
      30 %. (#7)
- [ ] **[autor] Registro DNS de la política**: en **Cloudflare**, que sirve la zona `baltajmn.dev`
      aunque el dominio se registre en Porkbun, un `CNAME` con host `bullet` y destino
      `baltajmn.github.io`, igual que las hermanas. (#59)
- [ ] **[autor] Reclutar 16 probadores**, no 12. Empieza por los de Quilt, MoodTraker y Purl: ya
      dijeron que sí una vez. El requisito de Play es **por app**, así que Bobbin hace su propia
      prueba cerrada aunque otra hermana ya tenga acceso a producción. Cómo reclutar y qué no hacer:
      `../../HabitTracker/store/testers.md`. (#10)

## Fase 2. Infraestructura

- [x] Repositorio `BaltaJmn/bullet`, público. Los minutos de Actions no se facturan.
- [ ] **[código] Andamiaje y CI**: los cuatro workflows de line con los cambios de `docs/tecnico.md`
      9. (#9)
- [ ] **[autor] Crear el almacén de subida.** Un comando, y el `CN=Baltasar` no es opcional: es lo que
      comprueba el workflow compartido (`ci.md`).

      ```bash
      keytool -genkeypair -v -keystore ~/keys/bobbin-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Baltasar, O=BaltaJmn, C=ES"
      ```

      Después, `keystore.properties` en la raíz del repositorio (git-ignorado) con `storeFile`,
      `storePassword`, `keyAlias=upload` y `keyPassword`. Apunta aquí la huella SHA-256 que imprime
      `keytool -list -v -keystore ~/keys/bobbin-upload.jks -J-Duser.language=en -J-Duser.country=US`,
      para contrastarla con la que enseñe Play al subir el primer AAB. Copia del `.jks` fuera de este
      Mac. (#7)
- [ ] **[autor] Los cinco secretos de firma y publicación** en GitHub (`ci.md`). (#7)
- [ ] **[autor] Publicar la política**: repositorio público `BaltaJmn/bullet-privacy` con
      `privacy/index.html` y GitHub Pages, igual que las hermanas. (#59)

## Fase 3. Play

**El orden importa.** La API de Android Publisher no responde hasta que la app tiene un binario
subido a mano, así que `release.yml` y `listings.yml` fallan si se ejecutan antes.

1. **[autor] Contenido de la aplicación** en la Console: política, seguridad de los datos,
   clasificación, público objetivo, declaraciones. Respuestas una a una en `store/formularios.md`,
   que se escribe en la issue #59 junto con las secciones de `docs/tecnico.md` que gobierna (SPEC
   §9). (#59)
2. **[autor] La primera subida, a mano.** `./gradlew :androidApp:bundleRelease` y subir
   `androidApp/build/outputs/bundle/release/androidApp-release.aab` en *Probar y publicar > Pruebas
   internas > Crear versión*. Comprobar que la huella del certificado de subida coincide con la
   apuntada en la fase 2. (#10)
3. **[código] Subir el `versionCode` a 2 y commitearlo.** El 1 queda gastado en la prueba interna y
   Play no lo acepta en ningún otro canal.
4. **[código] Ficha**, ya con la API viva: `gh workflow run listings.yml --ref main -f accion=subir`.
   Textos en `listings/`, con los topes comprobados. (#57)
5. **[autor] Imágenes**: icono de 512, gráfico de 1024x500 y capturas. La API de listings solo escribe
   texto. (#58)
6. **[autor] Producto `bullet_pro`** a 7,99 EUR (`revenuecat.md` §1). (#47)
7. **[autor] Abrir la prueba cerrada** (canal `alpha`) con la lista de probadores como Grupo de
   Google, y **[código]** etiquetar: `git tag v1.0.0 && git push origin v1.0.0`, que publica en
   `alpha`. (#10)

## Fase 4. RevenueCat

Paso a paso en `revenuecat.md`. Resumen: proyecto Bobbin, las dos tiendas, derecho `pro`, oferta
`default` como *Current* con un paquete *Lifetime*, y las dos claves públicas pegadas en
`Billing.android.kt` y `Billing.ios.kt`. Mientras sean `null`, la app funciona entera en modo gratis
y no revienta. (#47)

**[autor] Probar una compra real** en un móvil con la app instalada desde la prueba interna y la
cuenta en *Licencia para testing*, y *Restaurar compra* tras desinstalar. En el emulador no se puede:
no trae Play Billing.

## Fase 5. La prueba cerrada, el camino crítico

- [ ] **[autor] Sostener 12 aceptaciones durante 14 días seguidos.** El contador arranca cuando la
      versión está aprobada **y** hay 12 aceptaciones a la vez, y vuelve a cero si un solo día baja de
      12. La Console no enseña contador: el requisito se tacha solo al cumplirse. Subir versiones
      nuevas al canal durante la ventana es normal y no reinicia nada. (#10)
- [ ] **[autor] Pedir a los probadores lo que Google mira**: que escriban de verdad, a diario. Un
      apunte rápido en el diario es justo el uso diario que la revisión quiere ver.
- [ ] **[código] Apuntar lo que salga de la prueba** (fallos, arreglos, mensajes) con el commit que lo
      prueba: es la materia del formulario de acceso a producción (`store/formularios.md`).
- [ ] **[autor] Solicitar acceso a producción** al terminar. Hasta 7 días de revisión.

## Fase 6. Apple

- [ ] **[autor] Clave de la App Store Connect API** (rol *App Manager*) y los cuatro secretos de
      Apple (`ci.md`). Con ellos, la misma etiqueta `v*` archiva y sube a TestFlight. (#7)
- [ ] **[autor] `TEAM_ID`** en `Config.xcconfig`. **[código]** lo commitea: no es secreto. (#8)
- [ ] **[autor] Producto `bullet_pro`** en App Store Connect (`revenuecat.md` §6) con su captura de
      revisión. (#47)
- [ ] **[autor] App Privacy, clasificación por edad, cumplimiento de exportación** y notas para el
      revisor: `store/formularios.md`. (#59)
- [ ] **[autor] Ficha**: los cinco ficheros por idioma de `app-store/`, capturas de iPhone y de iPad
      (#58), URL de la política y de soporte. (#57)
- [ ] **[código] Informe de privacidad de Xcode** sobre el primer archivo (*Product > Archive >
      Generate Privacy Report*), contrastado con `PrivacyInfo.xcprivacy`. Lo que salga de más se añade
      al manifiesto y a `store/formularios.md` en el mismo commit. (#59)
- [ ] **[autor] Probar la compra en el sandbox** de Apple y *Restaurar compra*, que el revisor mira.
- [ ] **[autor] Enviar a revisión** con la compra adjunta a la versión: la primera compra de una app
      solo se revisa junto a una versión.

## Fase 7. Salida (#62)

- [ ] **[código] Rellenar `SIBLINGS`** en `data/AppInfo.kt` (`docs/tecnico.md` 6.16) con la URL de
      tienda de Quilt, Purl y MoodTraker **solo donde estén en producción ese día**. Comprobarlo
      abriendo la URL pública sin sesión iniciada:
      `https://play.google.com/store/apps/details?id=com.baltajmn.habit`,
      `https://play.google.com/store/apps/details?id=com.baltajmn.line`,
      `https://play.google.com/store/apps/details?id=com.baltajmn.mood`, y la búsqueda de cada nombre
      en App Store. Lo que no cargue, `null`.
- [ ] **[código] Versión final**: `versionCode` siguiente, `versionName` `1.0.0`, notas en
      `store/whatsnew/`.
- [ ] **[autor] Producción en Play** con despliegue al 100 % (una app nueva no tiene usuarios que
      proteger con un despliegue escalonado) y **publicación manual en App Store** (*Manually release
      this version*), para que las dos salgan el mismo día.
- [ ] **[autor] Comprobar tras publicar**: la ficha carga sin sesión en las dos tiendas, la política
      abre desde Ajustes, una compra real con tarjeta propia se reembolsa en la Console.

## Después de la salida

- **v1.1 sube el precio a 9,99 EUR** el día que sale el libro en PDF, no antes (`revenuecat.md` §7,
  #68). Las notas de versión y la descripción larga dicen entonces "libro en PDF" en la lista de Pro,
  en el mismo commit.
- **Actualizar la política y los formularios** en el mismo commit que añada cualquier cosa que salga
  del dispositivo o cualquier permiso nuevo.

---

## Qué issue cierra cada fase

| Issue | Dónde |
|---|---|
| #7 Altas y cuentas | Fases 1, 2 y 6 |
| #8 Andamiaje | Fase 6 (`Config.xcconfig`) |
| #9 CI | Fase 2 |
| #10 Primera build y prueba cerrada | Fases 1, 3 y 5 |
| #47 Compras | Fase 4, `revenuecat.md` |
| #57 Ficha | Fases 3 y 6, `listings/`, `app-store/` |
| #58 Capturas | Fases 3 y 6, `capturas.md` |
| #59 Privacidad y formularios | Fases 1, 3 y 6, `store/formularios.md`, `privacy/` |
| #62 Lanzamiento | Fase 7 |
