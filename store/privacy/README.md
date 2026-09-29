# Política de privacidad de Bobbin

`index.html` es la política, en inglés y español en la misma página. Un solo fichero, sin
dependencias externas: se abre igual en local que alojado.

## Dónde se publica

**https://bullet.baltajmn.dev/**, con el slug interno igual que `line.baltajmn.dev` y `mood.baltajmn.dev`: el dominio no se
ve en la ficha y no hace falta comprar otro.

Por GitHub Pages desde **este mismo repositorio**, con `.github/workflows/pages.yml`, que publica solo
este `index.html` cada vez que cambia en `main`. Es como están Quilt y Chroma (`~/keys/LEEME.md`,
Cloudflare): un solo original, sin una copia en otro repositorio que pueda divergir.

La misma URL va en cuatro sitios, y los cuatro tienen que coincidir: la Play Console (*Contenido de la
aplicación > Política de privacidad*), App Store Connect (política y URL de soporte), la ficha de
Play (sitio web) y la app (`PRIVACY_URL` en `data/AppInfo.kt`, fila de Ajustes).

El correo de contacto de la página es `baltajmn@gmail.com`, el de las hermanas. Tiene que ser el
mismo que el de contacto de las dos fichas, o la revisión lo marca como incoherencia.

`baltajmn.dev` se registra en Porkbun, pero **la zona la sirve Cloudflare**: el registro DNS va en
Cloudflare. Si el dominio caduca, muere la URL y con ella las dos fichas: auto-renew puesto.

## Cómo se monta

Una vez, con el token de Cloudflare de `~/keys` (`LEEME.md`, "Web de cada app"):

1. Pages con despliegue por workflow, y la primera publicación:

   ```bash
   gh api -X POST repos/BaltaJmn/bullet/pages -f build_type=workflow
   gh workflow run pages.yml --repo BaltaJmn/bullet --ref main
   ```

2. En Cloudflare, zona `baltajmn.dev`: `CNAME` `bullet` a `baltajmn.github.io`, **con proxy**. El HTTPS
   lo pone Cloudflare; *Enforce HTTPS* de GitHub se queda sin marcar, porque detrás del proxy no puede
   emitir certificado y no hace falta.
3. Dominio propio en Pages:

   ```bash
   gh api -X PUT repos/BaltaJmn/bullet/pages -f cname=bullet.baltajmn.dev
   ```

Comprobación final: `curl -sI https://bullet.baltajmn.dev/` devuelve 200. Si alterna 404 y 200, el CDN
de GitHub guardó un 404 viejo: otro `gh workflow run pages.yml` lo purga.

## Qué dice, y los hechos que la sostienen

Escrita contra `docs/tecnico.md`, no contra una plantilla:

- Permisos propios del manifiesto (`docs/tecnico.md` 8.1): `INTERNET`, `POST_NOTIFICATIONS`,
  `RECEIVE_BOOT_COMPLETED`, `USE_BIOMETRIC`. Del SDK de RevenueCat, `ACCESS_NETWORK_STATE` y
  `com.android.vending.BILLING`; de Glance, a través de WorkManager, `WAKE_LOCK` y
  `FOREGROUND_SERVICE`; de biometric, `USE_FINGERPRINT`. Ni ubicación, ni contactos, ni cámara, ni
  almacenamiento, ni fotos.
- RevenueCat se configura sin `appUserID`: el identificador es anónimo.
- `widget.json` lleva números, fechas, booleanos y el id de la portada, nunca texto
  (`docs/tecnico.md` 4.2).
- La notificación del recordatorio es un texto fijo que no cita el diario (`docs/tecnico.md` 6.12).
- La valoración la pide el sistema (`StoreReview`), una vez, sin enviar nada.
- La copia del sistema: `data_extraction_rules.xml` y `backup_rules.xml` de `docs/tecnico.md` 8.2.
- Cero SDK de analítica, publicidad o informes de fallos en el árbol de dependencias.

**Si cambia cualquiera de esas cosas, esta política deja de ser cierta.** Un SDK nuevo, un
`Purchases.logIn()`, un permiso más, texto del diario en `widget.json`, la captura desde fuera de
v1.1 o la sincronización de v1.2: se toca aquí, en `../formularios.md` y en `PrivacyInfo.xcprivacy`
en el mismo commit, y se cambia la fecha de arriba.

## Aviso

No es asesoramiento legal. Es un documento honesto escrito sobre hechos del código, que para una app
sin cuentas y sin servidor cubre lo que piden las dos tiendas.
