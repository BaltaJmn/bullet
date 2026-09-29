# Formularios de las dos tiendas, respuesta a respuesta

Todo lo que las consolas preguntan y no tiene API, con la respuesta cerrada y el hecho del código que
la sostiene. Se pegan a mano (o los rellena Claude en el navegador, con el sí del autor). Si el código
cambia algo de lo que aquí se afirma (un permiso, un SDK, un dato que sale del teléfono), se cambian en
el mismo commit este fichero, `privacy/index.html` y `iosApp/iosApp/PrivacyInfo.xcprivacy`.

Hechos de partida, todos de `docs/tecnico.md`:

- El diario (entradas, colecciones, seguimientos, ajustes) vive en `journal.json`, en `filesDir` en
  Android y en `Application Support` en iOS. No hay servidor, ni cuenta, ni analítica, ni publicidad,
  ni informes de fallos.
- Lo único que sale del teléfono es lo de **RevenueCat**: un identificador anónimo de instalación
  (se configura sin `appUserID`), el historial de compras y datos técnicos del dispositivo.
- Los widgets leen `widget.json`, que lleva números, fechas, booleanos y el id de la portada, nunca
  texto del diario (4.2).
- La notificación del recordatorio es un texto fijo que no cita el diario (6.12).
- La valoración la pide el sistema una sola vez (`StoreReview`, 6.6) y la app no manda nada con ella.

---

## 1. Play: seguridad de los datos

*Política > Contenido de la aplicación > Seguridad de los datos.*

| Pregunta | Respuesta |
|---|---|
| ¿Tu app recoge o comparte alguno de los tipos de datos obligatorios? | Sí |
| ¿Se cifran en tránsito todos los datos recogidos? | Sí (HTTPS del SDK de RevenueCat) |
| ¿Qué métodos de creación de cuenta admite? | *Mi aplicación no permite que los usuarios creen una cuenta*. Por eso no hace falta URL de borrado de cuenta |
| ¿Se puede iniciar sesión con cuentas creadas fuera de la app? | No |
| ¿Ofreces una forma de pedir que se borren los datos? | Sí. URL de eliminación: `https://bullet.baltajmn.dev/`, que nombra Bobbin y explica los pasos: borrar una entrada, Ajustes > Borrar todos los datos, desinstalar, y para lo de RevenueCat, un correo con el número de pedido |

Tipos de datos, los únicos dos que se marcan:

| Tipo | Recogido | Compartido | Efímero | Obligatorio | Finalidad |
|---|---|---|---|---|---|
| Información financiera > Historial de compras | Sí | No | No | Sí | Funcionalidad de la app |
| IDs de dispositivo o de otro tipo | Sí | No | No | Sí | Funcionalidad de la app |

RevenueCat recibe los datos como encargado del tratamiento, así que no cuenta como "compartido".
Play no deja enviar este cuestionario hasta que *Público objetivo* (§3) está hecho: mientras, se
guarda en borrador con todo lo de arriba.

Lo que **no** se marca, y por qué:

| Tipo | Por qué no |
|---|---|
| Otro contenido generado por el usuario (el texto del diario) | No sale del dispositivo |
| Fotos, mensajes, contactos, ubicación, salud, actividad | La app no los toca |
| Registros de fallos, diagnóstico | No hay SDK que los mande |

**La copia automática del sistema.** Android puede subir `journal.json` al Drive del usuario con su
copia de seguridad. La configuración (`docs/tecnico.md` 8.2) solo lo permite **cifrado de extremo a
extremo** (`disableIfNoEncryptionCapabilities="true"` y `clientSideEncryption`), y la definición de
Google excluye de "recogido" lo que va cifrado de extremo a extremo y solo pueden leer emisor y
receptor ([Seguridad de los datos, ayuda de Play Console](https://support.google.com/googleplay/android-developer/answer/10787469)).
Es una **inferencia**: la ayuda no nombra la copia del sistema (SPEC §9). Si Google publica algo
concreto, se revisa aquí.

## 2. Play: clasificación de contenido (IARC)

| Pregunta | Respuesta |
|---|---|
| Correo para IARC | `baltajmn@gmail.com` |
| Categoría | Utilidad, productividad, comunicación u otras |
| Violencia, sexo, lenguaje soez, drogas, apuestas, miedo | No a todo |
| ¿Los usuarios pueden interactuar o intercambiar contenido? | No. Una página se comparte con la hoja del sistema, fuera de la app |
| ¿Comparte la ubicación del usuario? | No |
| ¿Permite comprar bienes digitales? | Sí |
| ¿Contiene anuncios? | No |
| ¿Acceso sin restricciones a internet? | No |

Resultado esperado: PEGI 3, ESRB Everyone, USK 0, el más bajo de cada sistema. Enviar el cuestionario
acepta los términos de uso de IARC.

## 3. Play: público objetivo y declaraciones

| Campo | Valor |
|---|---|
| Grupos de edad | 13-15, 16-17, 18 y más |
| ¿Atrae a menores de 13? | No |
| Anuncios | No contiene anuncios |
| Datos de inicio de sesión (antes "Acceso a la app") | **Sí**, aunque no haya cuentas: la redacción de 2026 cuenta como restringido cualquier pago, y el revisor no compra con cuentas personales. Se le deja un código promocional de `bullet_pro` con las instrucciones de abajo. Play no deja empezar *Público objetivo* sin esta sección |
| App de noticias | No |
| Salud | No tiene funciones de salud. Es un diario y una agenda |
| Funciones financieras | No |
| App de gobierno | No |

13+ deja la app fuera del programa Familias, que trae requisitos que no aplican.

*Datos de inicio de sesión > Añade detalles*: nombre `Bobbin Pro for review`, usuario y contraseña
vacíos, y en *Cualquier otra información* (tope 500, en inglés), con el código de la promoción en
lugar de `CODE`:

```
Bobbin has no account and no login: everything stays on the device.

Paid content: Bobbin Pro, a one-time purchase (bullet_pro). Redeem this promo code in the Play Store app (profile icon > Payments & subscriptions > Redeem code): CODE. Then open Bobbin > Settings > Restore purchase.

The lock (Settings > Privacy) is optional, off by default, and uses the device's own screen lock or biometrics.
```

La casilla de acceso completo, contenido de pago incluido, solo se marca con el código puesto. Los
códigos salen de una promoción de `bullet_pro` (*Monetizar con Play > Códigos promocionales*), que
existe solo cuando el producto existe (#47); el CSV se guarda en `~/keys/`, fuera del repositorio, y
al caducar se crea otra y se cambia el código aquí. Crear la promoción acepta los términos de los
códigos promocionales de Play. Orden que impone Play: esta sección, luego *Público objetivo*, y solo
entonces se puede enviar *Seguridad de los datos*.

Permisos del manifiesto fusionado (comprobados sobre el de `assembleDebug`, septiembre de 2026):
`INTERNET`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `USE_BIOMETRIC` propios;
`ACCESS_NETWORK_STATE` y `com.android.vending.BILLING` de RevenueCat; `WAKE_LOCK` y
`FOREGROUND_SERVICE` de WorkManager a través de Glance; `USE_FINGERPRINT` de biometric. Ninguno pide
declaración. No se usa `SCHEDULE_EXACT_ALARM` (el recordatorio va con `setAndAllowWhileIdle`) ni
ningún permiso de fotos o almacenamiento. Comprobar la lista sobre el AAB:

```bash
grep -oE '<uses-permission[^>]*android:name="[^"]*"' \
  androidApp/build/intermediates/merged_manifest/release/*/AndroidManifest.xml | sort -u
```

`com.android.vending.BILLING` está en el binario desde la primera subida, así que "¿Tiene compras en
la aplicación?" es **sí** desde la primera subida aunque las claves sean `null`.

## 4. Play: ficha, categoría y contacto

| Campo | Valor |
|---|---|
| Nombre de la app | Bobbin: Bullet Journal |
| Idioma predeterminado | Inglés (Estados Unidos), `en-US` |
| Tipo | Aplicación |
| Gratuita o de pago | Gratuita |
| Categoría | **Estilo de vida** |
| Etiquetas | *Bloc de notas*, *Estilo de vida* y *Productividad*. La lista cerrada de Play no tiene diario; *Autoayuda* se descarta por lo mismo que la categoría de salud |
| Correo de contacto | `baltajmn@gmail.com`, el mismo de la política |
| Sitio web | `https://bullet.baltajmn.dev/` |
| Teléfono | Vacío |
| Política de privacidad | `https://bullet.baltajmn.dev/` |

Estilo de vida y no Productividad ni Salud y bienestar: la categoría Productividad es la que penaliza
a la app oficial del método (SPEC §8), y la de salud trae la declaración de salud y más escrutinio a
cambio de nada, cuando la ficha no hace ni una promesa de salud mental.

Textos de la ficha: `listings/<idioma>/`. Notas de versión: `whatsnew/`. Gráficos y capturas:
`play/` y `screenshots/play/` (#58).

## 5. App Store: privacidad de la app

*App Store Connect > Bobbin > Privacidad de la app.*

| Pregunta | Respuesta |
|---|---|
| ¿Recoges datos de esta app? | Sí |
| Compras > Historial de compras | Recogido. Finalidad: funcionalidad de la app. **No** vinculado a la identidad. **No** usado para rastreo |
| Identificadores > ID de usuario | Recogido. Funcionalidad de la app. No vinculado. No rastreo |
| El resto de tipos | No recogidos |

Es exactamente lo que dice `PrivacyInfo.xcprivacy` (`docs/tecnico.md` 8.3). Si el informe de
privacidad de Xcode sobre el primer archivo añade algo, se añade en los dos sitios.

URL de la política: `https://bullet.baltajmn.dev/`.

## 6. App Store: el resto de la ficha

| Campo | Valor |
|---|---|
| Categoría principal | **Estilo de vida** |
| Categoría secundaria | Productividad |
| Clasificación por edad | Cuestionario de 2025: **ninguno** en todos los contenidos; **no** en contenido generado por usuarios, mensajería, publicidad, acceso web sin restricciones, temas médicos o de bienestar, concursos y apuestas. Resultado esperado **4+** |
| Derechos de contenido | No contiene ni accede a contenido de terceros |
| Cumplimiento de exportación | No pregunta: `ITSAppUsesNonExemptEncryption = false` en el `Info.plist`. La única criptografía es el HTTPS del sistema |
| Copyright | `2026 Baltasar Jiménez` |
| URL de soporte | `https://bullet.baltajmn.dev/` (lleva el correo de contacto) |
| URL de marketing | Vacía |
| Inicio de sesión para la revisión | No hace falta: la app no tiene cuentas |
| Publicación | Manual, para salir el mismo día que Play |

El usuario escribe lo que quiere, pero nadie más lo ve ni la app lo publica: no es contenido generado
por usuarios a efectos de moderación, y por eso la respuesta es no.

Notas para el revisor, en inglés:

```
Bobbin has no account and no server. Everything is stored on the device, so no demo account is needed.

Bobbin is a bullet journal: the app opens on Today with the keyboard up. Type a line and press return. Long press an entry for its states; Review (from the line "N still open from earlier days" on Today, or "September not closed" on Month) migrates open tasks one at a time.

The optional lock (Settings) uses Face ID or the device passcode through LocalAuthentication. It is off by default.

Bobbin Pro is a one-time non-consumable purchase (bullet_pro). It opens from Settings > Bobbin Pro, from choosing a non-default cover or paper, from creating a second tracker, or from a Pro widget. Restore Purchase is in Settings and in the purchase dialog.
```

Datos de contacto de la revisión: nombre, teléfono y correo, **a mano**.

## 7. Play: solicitud de acceso a producción

Se envía al terminar los 14 días de la prueba cerrada. Ocho respuestas libres de 300 caracteres y dos
desplegables; si Google la rechaza hay que rellenarla entera otra vez, por eso vive aquí. En inglés:
el revisor no tiene garantizado el español. Google contrasta cada afirmación con las estadísticas de
la prueba, así que **todo lo que dependa de la prueba se escribe al final con datos reales**, con el
commit o el mensaje que lo prueba.

Fijas desde hoy:

**¿A qué audiencia va dirigida?**

```
Adults and teenagers from 13 up who keep, or want to keep, a bullet journal: people who plan their days with rapid logging and migrate tasks by hand. It suits paper bujo users who want it on the phone and privacy minded users, since there is no account and the journal never leaves the phone.
```

**Describe cómo aporta valor**

```
Planner apps move unfinished tasks by themselves and bury the method. Bobbin keeps the paper method: three bullets, five task states, and a monthly review that migrates one task at a time. The whole method, search, export, the lock and the reminder are free. No account, no ads, no analytics.
```

**Descargas esperadas el primer año**: entre 0 y 10.000.

Al final de la prueba, con los apuntes de `lanzamiento.md` fase 5:

| Pregunta | Qué contar |
|---|---|
| ¿Cómo reclutaste usuarios? | De dónde salió cada grupo, sin servicios de pago ni intercambios de testers |
| ¿Cómo de fácil te ha resultado? | Lo que fue |
| Interacciones de los testers | Qué usaron de verdad: escribir a diario, revisar el mes, migrar, las colecciones, la copia |
| Comentarios y cómo los recogiste | Canal y los dos o tres comentarios concretos |
| Cambios a partir de la prueba | Cada arreglo con su commit |
| ¿Cómo decidiste que estaba lista? | La última versión sin informes nuevos, los tests en cada push, la publicación por CI |
