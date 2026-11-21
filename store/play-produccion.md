# Play: solicitud de acceso a produccion (ENVIADA 21-11-2026)

Para el formulario de Play Console > Panel de control > "Solicitar acceso a produccion", cuando la consola
cuente 12 testers durante 14 dias (el 20-11 contaba 12 dias). Las respuestas van en ingles. Lo marcado
[COMPROBAR] se mira en la consola antes de pegar; si no se cumple, se quita la frase, no se adorna.

## Parte 1: la prueba cerrada

**How easy was it to recruit testers?** Difficult.

**Tester engagement.**
Testers came from Google Groups where independent developers test each other's apps. They installed
the closed test build and opened it over the test period; most of them tried the core of the method
(daily log, marking tasks done, migrating open tasks), but they are not bullet journal users, so they
used it for short sessions and did not keep a journal for weeks. Production users will write every
day for months, so the long-term parts (monthly and future logs, search across months, the year
review, reminders) got less use in the test than they will in production. [COMPROBAR: que la consola
muestre actividad de los testers en varios dias.]

**Feedback summary and how it was collected.**
Feedback could be sent to the closed test's feedback email address and through Play's private
feedback. Testers sent little written feedback and reported no crashes or data loss. Because of
that, we also tested the app ourselves every day of the test and compared it with public reviews of
other journaling apps; the changes listed in part 3 came from that work.

## Parte 2: la app

**Target audience.** Adults who keep, or want to keep, a bullet journal (rapid logging, manual
migration) on their phone without losing the method: students, people who plan their work week,
and paper journal users who want search and reminders.

**Value proposition.** Bobbin is a faithful digital bullet journal. The whole method is free, with
no ads and no account: daily, monthly and future logs, migration by hand, unlimited collections,
search, a reminder, a lock and a home screen widget. The journal stays on the device. An optional
one-time purchase adds covers and papers, more trackers, a PDF book of the journal and sync through
the user's own cloud folder.

**Estimated downloads in the first year.** 1,000 to 10,000.

## Parte 3: preparacion para produccion

**Changes made after the closed test.**
- The request to rate the app now waits until the user finishes a review of the day, after at
  least two days of use, and never on the same day as the Pro offer.
- The Pro card no longer covers the day; it can be dismissed.
- The app and its store listing were translated into Arabic, Simplified Chinese, Hindi and
  Vietnamese, and the translations were reviewed again.
- Store screenshots and listing texts were redone to match the app as it is.
- Version 1.0.1 (versionCode 4) carries these changes and is the build in the closed test.

**How we know it is ready.**
The same app (shared Kotlin Multiplatform code) is on sale on the App Store and passed Apple's
review. The Android build passes the project's 269 automated tests, and the closed test showed no
crashes or data loss. The app needs no login, so reviewers can use every feature directly.

(20-11: los 269 tests de `:shared:testAndroidHostTest` en verde con el codigo de la version 4. Play
no tiene informe previo al lanzamiento de Bobbin: no se menciona.)

## Lo que se envio el 21-11 (cada campo con tope de 300 caracteres)

1. Reclutamiento: Google Groups y comunidades de desarrolladores independientes; sin proveedor de pago. Dificultad: Difficult.
2. Interaccion: testers desarrolladores, no usuarios de bullet journal; sesiones cortas en el registro diario y la guia; sin analitica.
3. Comentarios: feedback privado de Play y correo de la prueba; un tester elogio la guia y la sencillez; sin fallos ni perdida de datos.
4. Audiencia y valor: los de la parte 2, resumidos. Descargas: 0 a 10.000.
5. Cambios y por que esta lista: los de la parte 3, resumidos.
