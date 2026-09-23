# Textos de Bobbin

Todos los textos de la app en los cinco idiomas y el glosario fijo del método. De aquí salen
`i18n/Strings.kt` (tabla `S`, con `t(en, es, pt, de, fr)` como en las hermanas), el espejo `L` de los
widgets de iOS, los `InfoPlist.strings`, los `Localizable.strings` de la extensión de widgets y del App
Intent (v1.1) y los `strings.xml` de Android. Las claves son las que citan `docs/pantallas.md` y
`docs/tecnico.md`; si aquí cambia una frase, manda este documento y allí se corrige la cita. Las
secciones siguen el orden en que `Strings.kt` las lee, una sección del fichero por cada una de aquí.

La ficha de tienda (#57) y las capturas (#58) usan el glosario de este documento y ninguna otra palabra
para lo mismo.

## Reglas de tono

- Tuteo en español, `du` en alemán, `tu` en francés, `você` en portugués de Brasil.
- Frases cortas, en presente y sin exclamaciones. La app constata; no anima, no celebra, no reprocha.
  Prohibido cualquier equivalente de "nunca has...", "vas a perder..." o "llevas N días sin...".
- Ninguna promesa de salud mental, de bienestar ni de productividad milagrosa.
- Ningún número de progreso: ni porcentajes, ni rachas, ni "12 de 30 hechas". Los únicos números son
  los que ya son del método o de la pantalla (cuántas abiertas quedan, "3 de 12" en la revisión,
  "migrada 3 veces").
- Nada en rojo ni en tono de error. Un fallo dice lo que ha pasado y qué pasa después.
- Botones (claves marcadas con `*`): 22 caracteres como máximo en cualquier idioma. Etiquetas `Eyebrow`
  fijas: 24 como máximo; las que llevan una fecha pueden partir línea. Pestañas: una sola palabra en los
  cinco idiomas (`docs/pantallas.md` 3.1).
- Francés: espacio normal antes de `?`, `!`, `:` y `;`, nunca espacio duro; apóstrofo y comillas
  rectos.
- Alemán: sustantivos en mayúscula.
- Puntuación ASCII salvo la propia de cada idioma (`¿` y `¡` en español).
- `Bobbin`, `Bobbin Pro`, `Purl`, `Quilt` y `MoodTraker` no se traducen ni se declinan.
- Horas en formato de 24 horas en los cinco idiomas (`clock(h, m)`: `21:00`).

## Reglas del método

Fijadas aquí para la app, la ficha y las capturas, sin excepción por idioma.

1. **Los símbolos del método no se traducen nunca.** El punto de la tarea, el círculo del evento, el
   guion de la nota, el aspa de la hecha, `>` de la migrada, `<` de la programada, el tachado de la
   descartada, y los signifiers `*`, `!` y el ojo de explorar se dibujan igual en los cinco idiomas.
   Los prefijos de captura (`o `, `- `, `* `, `! `, `? `, `docs/tecnico.md` 6.2) son los mismos
   caracteres en todos: `o` es la letra latina minúscula, también en portugués aunque "o" sea un
   artículo. Los textos que los nombran (`prefixHint`, `keyEventHow`...) los escriben tal cual, sin
   comillas y sin cambiarlos por otra letra. Los encabezados del Markdown exportado van fijos en inglés
   (`docs/tecnico.md` 4.4) y no son textos de la app.
2. **Los textos del recordatorio son fijos y no citan ni una palabra del diario.** `reminderTitle` y
   `reminderBody` son literales: iguales cada día, con bloqueo o sin él, sin parámetros, sin fecha, sin
   número de tareas y sin nada que el usuario haya escrito. Ninguna otra notificación existe
   (`docs/tecnico.md` 6.12). Lo mismo para los widgets: solo números, fechas y las palabras fijas de la
   sección 19.
3. **Plurales propios por idioma, sin depender del locale del sistema.** `Strings.kt` elige la forma con
   su propia regla, nunca con `PluralRules`, `NSLocalizedString` con `stringsdict` ni `getQuantityString`:

   | Idioma | Forma singular | Forma plural |
   |---|---|---|
   | en, es, de | `n == 1` | cualquier otro `n`, también 0 |
   | pt | `n == 1` | cualquier otro `n`, también 0 ("0 tarefas") |
   | fr | `n == 0` o `n == 1` ("0 tâche", "1 tâche") | `n >= 2` |

   Las palabras que cambian con el número:

   | Palabra | en | es | pt | de | fr |
   |---|---|---|---|---|---|
   | entrada | entry / entries | entrada / entradas | entrada / entradas | Eintrag / Einträge | entrée / entrées |
   | tarea | task / tasks | tarea / tareas | tarefa / tarefas | Aufgabe / Aufgaben | tâche / tâches |
   | abierta | open / open | abierta / abiertas | aberta / abertas | offen / offen | ouverte / ouvertes |
   | hecha | done / done | hecha / hechas | feita / feitas | erledigt / erledigt | faite / faites |
   | evento | event / events | evento / eventos | evento / eventos | Ereignis / Ereignisse | événement / événements |
   | cambio | change / changes | cambio / cambios | alteração / alterações | Änderung / Änderungen | modification / modifications |
   | imagen | image / images | imagen / imágenes | imagem / imagens | Bild / Bilder | image / images |
   | vez | once / times | vez / veces | vez / vezes | einmal / -mal | fois / fois |

   Donde un 0 sonaría a fallo ("0 cambios", "0 entradas importadas") la clave tiene su propia frase
   para 0, escrita en su fila.

## Glosario del método

Cada término del método se dice siempre con la misma palabra: en la app, en la ficha de tienda y en las
capturas. Si una pantalla necesita el concepto, usa esta palabra y no un sinónimo.

| Concepto | en | es | pt | de | fr |
|---|---|---|---|---|---|
| Hoy (pestaña, Daily Log) | Today | Hoy | Hoje | Heute | Aujourd'hui |
| Mes (pestaña, Monthly Log) | Month | Mes | Mês | Monat | Mois |
| Futuro (pestaña del Future Log) | Future | Futuro | Futuro | Zukunft | Futur |
| Índice | Index | Índice | Índice | Index | Index |
| Colección / Colecciones | Collection / Collections | Colección / Colecciones | Coleção / Coleções | Sammlung / Sammlungen | Collection / Collections |
| Seguimiento (colección `TRACKER`) | Tracker | Seguimiento | Tracker | Tracker | Suivi |
| Migrar | Migrate | Migrar | Migrar | Migrieren | Migrer |
| Programar | Schedule | Programar | Agendar | Einplanen | Planifier |
| Descartar | Discard | Descartar | Descartar | Verwerfen | Écarter |
| Revisar | Review | Revisar | Revisar | Durchsehen | Revoir |
| Reabrir | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| Releer (paso de reflexión) | Read again | Releer | Reler | Nachlesen | Relire |
| Entrada | Entry | Entrada | Entrada | Eintrag | Entrée |
| Tarea / Evento / Nota | Task / Event / Note | Tarea / Evento / Nota | Tarefa / Evento / Nota | Aufgabe / Ereignis / Notiz | Tâche / Événement / Note |
| Abierta / Hecha | Open / Done | Abierta / Hecha | Aberta / Feita | Offen / Erledigt | Ouverte / Faite |
| Migrada / Programada / Descartada | Migrated / Scheduled / Discarded | Migrada / Programada / Descartada | Migrada / Agendada / Descartada | Migriert / Eingeplant / Verworfen | Migrée / Planifiée / Écartée |
| Prioridad / Inspiración / Explorar | Priority / Inspiration / Explore | Prioridad / Inspiración / Explorar | Prioridade / Inspiração / Explorar | Priorität / Inspiration / Erkunden | Priorité / Inspiration / Explorer |
| Clave (la *key page*) | Key | Clave | Legenda | Legende | Légende |
| Calendario (del mes) | Calendar | Calendario | Calendário | Kalender | Calendrier |
| Tareas del mes | Tasks of the month | Tareas del mes | Tarefas do mês | Aufgaben des Monats | Tâches du mois |
| Diario (todo lo escrito) | journal | diario | diário | Journal | journal |
| Cuaderno | Notebook | Cuaderno | Caderno | Notizbuch | Carnet |
| Portada / Papel | Cover / Paper | Portada / Papel | Capa / Papel | Umschlag / Papier | Couverture / Papier |
| Símbolo (un glifo del método) | symbol | símbolo | símbolo | Symbol | symbole |

Sin traducir en ningún idioma, como se busca el método: `bullet journal`, `bujo`, `bullet`,
`signifier`, `rapid logging`, `Daily Log`, `Monthly Log` y `Future Log`. En la app solo aparecen
`bullets` y `signifiers` (secciones de la Clave) y `Future Log` (la línea de Mes y su revisión); las
pestañas dicen Hoy, Mes y Futuro, y la ficha explica que son el Daily Log, el Monthly Log y el Future
Log.

## Forma en el código

- Texto sin parámetros: `val captureHint = t("...", "...", "...", "...", "...")`.
- Con parámetros: función, `fun earlierOpen(n: Int) = when (lang) { ... }`, con el plural resuelto por
  la regla del método 3 (`plural(n, one, other)`, privada en `S`).
- Fechas y horas: funciones de `S` sobre las tablas de la sección 1, escritas a mano como en line.
  Nada de formateadores de plataforma. `clock(h, m)` da `21:00` con dos cifras en los dos campos.
- En francés, el día 1 se escribe `1er` en toda fecha con el nombre del mes ("1er janvier",
  "Mercredi 1er"); y `de` delante de un mes que empieza por vocal se apostrofa ("Tâches d'août",
  "d'avril", "d'octobre").
- Las listas se juntan con `joinAnd(items)`: comas y la conjunción del idioma antes del último
  (`and`, `y`, `e`, `und`, `et`): "1, 2, 5 y 9".
- Las etiquetas `Eyebrow` se escriben aquí en caja normal y el estilo las pasa a mayúsculas con
  `uppercase()` al pintar (`docs/pantallas.md` 1.3). En alemán `ß` pasa a `SS`, que es correcto.
- Los tests cambian `S.lang` para recorrer los cinco idiomas (test 24); los textos sin parámetros se
  fijan una vez al arrancar, como en line.
- Una clave que otra ya dice igual no se duplica en el código: la columna Parámetros lo indica con
  "= clave".

---

## 1. Fechas y horas

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `monthNames` | | January, February, March, April, May, June, July, August, September, October, November, December | enero, febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre | janeiro, fevereiro, março, abril, maio, junho, julho, agosto, setembro, outubro, novembro, dezembro | Januar, Februar, März, April, Mai, Juni, Juli, August, September, Oktober, November, Dezember | janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre |
| `monthShort` | | Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec | ene, feb, mar, abr, may, jun, jul, ago, sept, oct, nov, dic | jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez | Jan., Feb., März, Apr., Mai, Juni, Juli, Aug., Sept., Okt., Nov., Dez. | janv., févr., mars, avr., mai, juin, juil., août, sept., oct., nov., déc. |
| `weekdayNames` | | Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday | lunes, martes, miércoles, jueves, viernes, sábado, domingo | segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado, domingo | Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag | lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche |
| `weekdayShort` | | Mon, Tue, Wed, Thu, Fri, Sat, Sun | lun, mar, mié, jue, vie, sáb, dom | seg, ter, qua, qui, sex, sáb, dom | Mo, Di, Mi, Do, Fr, Sa, So | lun, mar, mer, jeu, ven, sam, dim |
| `weekdayInitial` | día | M, T, W, T, F, S, S | L, M, X, J, V, S, D | S, T, Q, Q, S, S, D | M, D, M, D, F, S, S | L, M, M, J, V, S, D |
| `dayTitle` | fecha | Wednesday 23 | Miércoles 23 | Quarta-feira, 23 | Mittwoch, 23. | Mercredi 23 |
| `monthName` | mes | September | Septiembre | Setembro | September | Septembre |
| `monthYear` | mes | September 2026 | septiembre de 2026 | setembro de 2026 | September 2026 | septembre 2026 |
| `monthTitle` | mes | September 2026 | Septiembre 2026 | Setembro 2026 | September 2026 | Septembre 2026 |
| `longDateWithYear` | fecha | Tuesday, September 22, 2026 | Martes, 22 de septiembre de 2026 | Terça-feira, 22 de setembro de 2026 | Dienstag, 22. September 2026 | Mardi 22 septembre 2026 |
| `dayMonthYear` | fecha | January 1, 2027 | 1 de enero de 2027 | 1 de janeiro de 2027 | 1. Januar 2027 | 1er janvier 2027 |
| `shortDate` | fecha | September 14 | 14 de septiembre | 14 de setembro | 14. September | 14 septembre |
| `abbrDate` | fecha | Sep 24 | 24 sept | 24 set | 24. Sept. | 24 sept. |
| `abbrDateWithYear` | fecha | Oct 14, 2027 | 14 oct 2027 | 14 out 2027 | 14. Okt. 2027 | 14 oct. 2027 |
| `clock` | h, m | 21:00 | 21:00 | 21:00 | 21:00 | 21:00 |

- `weekdayInitial` va de lunes a domingo; Mes la pinta en el orden de la semana que toque.
- `dayTitle` es también la cabecera de cada día al releer (`docs/pantallas.md` 11.1), la vista previa de
  Ajustes y el día marcado de un seguimiento.
- `monthYear` es el subtítulo de Hoy y de un seguimiento; `monthTitle`, el título de un mes en el
  Índice, en Buscar (`monthGroup`) y en el selector de Programar.

## 2. Comunes

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `appName` | | Bobbin | Bobbin | Bobbin | Bobbin | Bobbin |
| `ok`* | | OK | Vale | OK | OK | OK |
| `cancel`* | | Cancel | Cancelar | Cancelar | Abbrechen | Annuler |
| `yes`* | | Yes | Sí | Sim | Ja | Oui |
| `notNow`* | | Not now | Ahora no | Agora não | Jetzt nicht | Pas maintenant |
| `close`* | | Close | Cerrar | Fechar | Schließen | Fermer |
| `back` | | Back | Volver | Voltar | Zurück | Retour |
| `undo`* | | Undo | Deshacer | Desfazer | Rückgängig | Annuler |
| `working` | | One moment... | Un momento... | Um momento... | Einen Moment... | Un instant... |

## 3. Navegación y cabeceras

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `tabToday` | | Today | Hoy | Hoje | Heute | Aujourd'hui |
| `tabMonth` | | Month | Mes | Mês | Monat | Mois |
| `tabFuture` | | Future | Futuro | Futuro | Zukunft | Futur |
| `tabIndex` | | Index | Índice | Índice | Index | Index |
| `backToToday`* | | Back to today | Volver a hoy | Voltar para hoje | Zurück zu heute | Revenir à aujourd'hui |
| `a11yBack` | | Back | Volver | Voltar | Zurück | Retour |
| `a11yPreviousDay` | | Previous day | Día anterior | Dia anterior | Vorheriger Tag | Jour précédent |
| `a11yNextDay` | | Next day | Día siguiente | Próximo dia | Nächster Tag | Jour suivant |
| `a11yPreviousMonth` | | Previous month | Mes anterior | Mês anterior | Vorheriger Monat | Mois précédent |
| `a11yNextMonth` | | Next month | Mes siguiente | Próximo mês | Nächster Monat | Mois suivant |
| `a11yClose` | | Close | Cerrar | Fechar | Schließen | Fermer |
| `a11yShare` | | Share this page | Compartir esta página | Compartilhar esta página | Diese Seite teilen | Partager cette page |
| `a11ySettings` | | Settings | Ajustes | Ajustes | Einstellungen | Réglages |
| `a11ySearch` | | Search the journal | Buscar en el diario | Buscar no diário | Im Journal suchen | Chercher dans le journal |
| `a11yKey` | | Symbol key | Clave de símbolos | Legenda dos símbolos | Legende der Symbole | Légende des symboles |
| `a11yMoreActions` | | More actions | Más acciones | Mais ações | Weitere Aktionen | Plus d'actions |
| `a11ySelected` | | selected | elegida | selecionada | ausgewählt | sélectionnée |

Los títulos de página de Futuro y del Índice son `tabFuture` y `tabIndex`; el de la Clave, `keyTitle`
(sección 12); el de Ajustes, `settingsTitle` (sección 13).

## 4. La entrada, la captura y la hoja

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `captureHint` | | Write here | Escribe aquí | Escreva aqui | Hier schreiben | Écris ici |
| `counter` | n, max | 480/500 | 480/500 | 480/500 | 480/500 | 480/500 |
| `bulletTask` | | Task | Tarea | Tarefa | Aufgabe | Tâche |
| `bulletEvent` | | Event | Evento | Evento | Ereignis | Événement |
| `bulletNote` | | Note | Nota | Nota | Notiz | Note |
| `stateDone` | | Done | Hecha | Feita | Erledigt | Faite |
| `stateMigrated` | | Migrated | Migrada | Migrada | Migriert | Migrée |
| `stateScheduled` | | Scheduled | Programada | Agendada | Eingeplant | Planifiée |
| `stateDiscarded` | | Discarded | Descartada | Descartada | Verworfen | Écartée |
| `signifierPriority` | | Priority | Prioridad | Prioridade | Priorität | Priorité |
| `signifierInspiration` | | Inspiration | Inspiración | Inspiração | Inspiration | Inspiration |
| `signifierExplore` | | Explore | Explorar | Explorar | Erkunden | Explorer |
| `wentToDay` | fecha | = `abbrDate`: Sep 24 | 24 sept | 24 set | 24. Sept. | 24 sept. |
| `wentToMonth` | mes | = `monthTitle`: October 2026 | Octubre 2026 | Outubro 2026 | Oktober 2026 | Octobre 2026 |
| `wentToFuture` | mes, día? | Future, Oct 14 / Future, October 2026 | Futuro, 14 oct / Futuro, octubre de 2026 | Futuro, 14 out / Futuro, outubro de 2026 | Zukunft, 14. Okt. / Zukunft, Oktober 2026 | Futur, 14 oct. / Futur, octobre 2026 |
| `migratedTimes` | n (>= 2) | Migrated 3 times | Migrada 3 veces | Migrada 3 vezes | 3-mal migriert | Migrée 3 fois |
| `actionMigrate` | | Migrate | Migrar | Migrar | Migrieren | Migrer |
| `actionSchedule` | | Schedule | Programar | Agendar | Einplanen | Planifier |
| `actionDiscard` | | Discard | Descartar | Descartar | Verwerfen | Écarter |
| `actionReopen` | | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| `actionGoToCopy` | | Go to the copy | Ir a la copia | Ir para a cópia | Zur Kopie | Aller à la copie |
| `actionEdit` | | Edit | Editar | Editar | Bearbeiten | Modifier |
| `actionDelete` | | Delete | Borrar | Apagar | Löschen | Supprimer |
| `toToday` | | Today | Hoy | Hoje | Heute | Aujourd'hui |
| `toTomorrow` | | Tomorrow | Mañana | Amanhã | Morgen | Demain |
| `toThisMonth` | | This month's tasks | Tareas de este mes | Tarefas deste mês | Aufgaben dieses Monats | Tâches de ce mois |
| `toDayOfMonth` | | A day this month | Un día de este mes | Um dia deste mês | Ein Tag in diesem Monat | Un jour de ce mois |
| `toCollection` | | To a collection | A una colección | Para uma coleção | In eine Sammlung | Vers une collection |
| `dayField` | | day | día | dia | Tag | jour |
| `dayFieldOptional` | | Day, optional | Día, opcional | Dia, opcional | Tag, optional | Jour, facultatif |
| `migrateAction`* | | Migrate | Migrar | Migrar | Migrieren | Migrer |
| `dayOutOfRange` | mes, n | September has no day 31. | Septiembre no tiene día 31. | Setembro não tem dia 31. | Der September hat keinen 31. Tag. | Septembre n'a pas de jour 31. |
| `dayPast` | | That day has passed. | Ese día ya pasó. | Esse dia já passou. | Dieser Tag ist vorbei. | Ce jour est passé. |
| `newCollection` | | New collection | Nueva colección | Nova coleção | Neue Sammlung | Nouvelle collection |
| `showMoreMonths` | | Show more months | Ver más meses | Ver mais meses | Mehr Monate zeigen | Voir plus de mois |
| `entryDeleted` | | Entry deleted. | Entrada borrada. | Entrada apagada. | Eintrag gelöscht. | Entrée supprimée. |
| `collectionDeleted` | | Collection deleted. | Colección borrada. | Coleção apagada. | Sammlung gelöscht. | Collection supprimée. |
| `rowDeleted` | | Row deleted. | Fila borrada. | Linha apagada. | Zeile gelöscht. | Ligne supprimée. |

- `wentToFuture` con día usa `abbrDate`; si el mes no es del año en curso, `abbrDateWithYear`
  ("Futuro, 14 oct 2027"). Sin día, "Futuro, " y `monthYear`. Una colección de destino se cita por su
  título, sin texto alrededor.
- `migratedTimes` solo se enseña desde `MIGRATION_SHOWN_FROM` (2). Con 1, por si acaso: Migrated once,
  Migrada 1 vez, Migrada 1 vez, Einmal migriert, Migrée 1 fois.
- Nombre de cada glifo (`glyphName`, `docs/pantallas.md` 1.5), para el lector de pantalla:

| Glifo | en | es | pt | de | fr |
|---|---|---|---|---|---|
| Tarea abierta | Task | Tarea | Tarefa | Aufgabe | Tâche |
| Tarea hecha | Done task | Tarea hecha | Tarefa feita | Erledigte Aufgabe | Tâche faite |
| Tarea migrada | Migrated task | Tarea migrada | Tarefa migrada | Migrierte Aufgabe | Tâche migrée |
| Tarea programada | Scheduled task | Tarea programada | Tarefa agendada | Eingeplante Aufgabe | Tâche planifiée |
| Tarea descartada | Discarded task | Tarea descartada | Tarefa descartada | Verworfene Aufgabe | Tâche écartée |
| Evento | Event | Evento | Evento | Ereignis | Événement |
| Nota | Note | Nota | Nota | Notiz | Note |
| Prioridad | Priority | Prioridad | Prioridade | Priorität | Priorité |
| Inspiración | Inspiration | Inspiración | Inspiração | Inspiration | Inspiration |
| Explorar | Explore | Explorar | Explorar | Erkunden | Explorer |

## 5. Hoy

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `prefixHint` | | No prefix, task. - note, o event, * priority. | Sin prefijo, tarea. - nota, o evento, * prioridad. | Sem prefixo, tarefa. - nota, o evento, * prioridade. | Ohne Präfix eine Aufgabe. - Notiz, o Ereignis, * Priorität. | Sans préfixe, une tâche. - note, o événement, * priorité. |
| `calendarToday` | | On the calendar | En el calendario | No calendário | Im Kalender | Au calendrier |
| `noticeCorrupt` | | Couldn't read the journal. The files were set aside and nothing was deleted. | No se ha podido leer el diario. Los ficheros se han guardado aparte y no se ha borrado nada. | Não foi possível ler o diário. Os arquivos foram guardados à parte e nada foi apagado. | Das Journal konnte nicht gelesen werden. Die Dateien wurden beiseitegelegt, gelöscht wurde nichts. | Impossible de lire le journal. Les fichiers ont été mis de côté et rien n'a été supprimé. |
| `noticeSaveFailed` | | Couldn't save. I'll try again with your next change. | No se ha podido guardar. Lo intento otra vez con tu próximo cambio. | Não foi possível salvar. Vou tentar de novo na sua próxima alteração. | Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut. | Impossible d'enregistrer. Je réessaierai avec ta prochaine modification. |
| `offerReminder` | hora | Remind you to go over the day at 21:00? | ¿Te aviso para repasar el día a las 21:00? | Quer que eu avise para repassar o dia às 21:00? | Soll ich dich um 21:00 erinnern, den Tag durchzugehen? | Je te rappelle de relire ta journée à 21:00 ? |
| `unclosedMonth` | mes, n | August not closed: 4 open / 1 open | Agosto sin cerrar: 4 abiertas / 1 abierta | Agosto sem fechar: 4 abertas / 1 aberta | August nicht abgeschlossen: 4 offen / 1 offen | Août pas clôturé : 4 ouvertes / 1 ouverte |
| `earlierOpen` | n | 3 still open from earlier days / 1 still open from earlier days | Quedan 3 abiertas de días anteriores / Queda 1 abierta de días anteriores | Restam 3 abertas de dias anteriores / Resta 1 aberta de dias anteriores | Noch 3 offen von früheren Tagen / Noch 1 offen von früheren Tagen | Il reste 3 tâches ouvertes des jours précédents / Il reste 1 tâche ouverte des jours précédents |

El título de Hoy es `dayTitle`, el subtítulo `monthYear` y la pista del campo `captureHint`. La oferta
del recordatorio lleva `notNow` y `yes`; el aviso de diario dañado, `ok`.

## 6. Mes

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `monthTasks` | | Tasks of the month | Tareas del mes | Tarefas do mês | Aufgaben des Monats | Tâches du mois |
| `calendarTitle` | | Calendar | Calendario | Calendário | Kalender | Calendrier |
| `futureWaiting` | n | 2 Future Log entries waiting / 1 Future Log entry waiting | 2 entradas del Future Log esperan / 1 entrada del Future Log espera | 2 entradas do Future Log esperam / 1 entrada do Future Log espera | 2 Einträge im Future Log warten / 1 Eintrag im Future Log wartet | 2 entrées du Future Log attendent / 1 entrée du Future Log attend |

El título de Mes es `monthName` y el subtítulo el año en cifras. La línea del mes sin cerrar es
`unclosedMonth` (sección 5).

## 7. Futuro

Sin textos propios: título `tabFuture`, bloques `monthTitle`, campo del día `dayField`, error
`dayOutOfRange`, `showMoreMonths` y `captureHint`.

## 8. Índice

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `indexFilterHint` | | Filter by title | Filtrar por título | Filtrar por título | Nach Titel filtern | Filtrer par titre |
| `indexMonth` | | Month | Mes | Mês | Monat | Mois |
| `indexCollection` | | Collection | Colección | Coleção | Sammlung | Collection |
| `indexTracker` | | Tracker | Seguimiento | Tracker | Tracker | Suivi |
| `newTracker` | | New tracker | Nuevo seguimiento | Novo tracker | Neuer Tracker | Nouveau suivi |
| `archivedToggle` | n | Archived (2) | Archivadas (2) | Arquivadas (2) | Archiviert (2) | Archivées (2) |
| `indexEmpty` | | Months show up here as soon as you write in them. | Los meses aparecen aquí en cuanto escribes en ellos. | Os meses aparecem aqui assim que você escreve neles. | Monate erscheinen hier, sobald du in ihnen schreibst. | Les mois apparaissent ici dès que tu y écris. |
| `indexNoMatch` | | No title with those letters. | Ningún título con esas letras. | Nenhum título com essas letras. | Kein Titel mit diesen Buchstaben. | Aucun titre avec ces lettres. |

Las filas de crear son `newCollection` (sección 4) y `newTracker`.

## 9. Colección y seguimiento

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `archive` | | Archive | Archivar | Arquivar | Archivieren | Archiver |
| `unarchive` | | Unarchive | Sacar del archivo | Tirar do arquivo | Aus dem Archiv holen | Désarchiver |
| `deleteCollection` | | Delete collection | Borrar colección | Apagar coleção | Sammlung löschen | Supprimer la collection |
| `archivedNote` | | Archived. | Archivada. | Arquivada. | Archiviert. | Archivée. |
| `trackerRowHint` | | New row | Nueva fila | Nova linha | Neue Zeile | Nouvelle ligne |
| `rowDelete` | | Delete row | Borrar fila | Apagar linha | Zeile löschen | Supprimer la ligne |

## 10. Revisar

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `reflectTitle` | mes | Read September again | Releer septiembre | Reler setembro | September nachlesen | Relire septembre |
| `reflectTitle` | días | Read up to today | Releer hasta hoy | Reler até hoje | Bis heute nachlesen | Relire jusqu'à aujourd'hui |
| `reflectHint` | mes | A note about this month, if you like | Una nota sobre este mes, si quieres | Uma nota sobre este mês, se quiser | Eine Notiz zu diesem Monat, wenn du magst | Une note sur ce mois, si tu veux |
| `reflectHint` | días | A note about these days, if you like | Una nota sobre estos días, si quieres | Uma nota sobre estes dias, se quiser | Eine Notiz zu diesen Tagen, wenn du magst | Une note sur ces jours, si tu veux |
| `skip`* | | Skip | Saltar | Pular | Überspringen | Passer |
| `saveAndGo`* | | Save and continue | Guardar y seguir | Salvar e seguir | Speichern und weiter | Valider et continuer |
| `reviewPosition` | i, n | 3 of 12 | 3 de 12 | 3 de 12 | 3 von 12 | 3 sur 12 |
| `fromDay` | fecha | From Monday 14 | Del lunes 14 | Da segunda-feira, 14 | Vom Montag, 14. | Du lundi 14 |
| `fromMonthTasks` | mes | August tasks | Tareas de agosto | Tarefas de agosto | Aufgaben im August | Tâches d'août |
| `fromCalendar` | fecha | Calendar, August 3 | Calendario, 3 de agosto | Calendário, 3 de agosto | Kalender, 3. August | Calendrier, 3 août |
| `fromFuture` | mes, día? | Future, September 14 / Future, September 2026 | Futuro, 14 de septiembre / Futuro, septiembre de 2026 | Futuro, 14 de setembro / Futuro, setembro de 2026 | Zukunft, 14. September / Zukunft, September 2026 | Futur, 14 septembre / Futur, septembre 2026 |
| `reviewDone` | | Done | Hecha | Feita | Erledigt | Faite |
| `reviewMigrate` | | Migrate | Migrar | Migrar | Migrieren | Migrer |
| `reviewSchedule` | | Schedule | Programar | Agendar | Einplanen | Planifier |
| `reviewToCollection` | | To a collection | A una colección | Para uma coleção | In eine Sammlung | Vers une collection |
| `reviewDiscard` | | Discard | Descartar | Descartar | Verwerfen | Écarter |
| `reviewAllDecided` | | All decided. | Todo decidido. | Tudo decidido. | Alles entschieden. | Tout est décidé. |
| `monthClosed` | mes | August, closed. | Agosto, cerrado. | Agosto, fechado. | August, abgeschlossen. | Août, clôturé. |
| `futureToCalendar` | | Move to the calendar | Pasar al calendario | Passar para o calendário | In den Kalender | Passer au calendrier |
| `futureLeave` | | Leave it | Dejarla | Deixar | Lassen | La laisser |
| `futureDiscard` | | Discard | Descartar | Descartar | Verwerfen | Écarter |
| `futureAllDecided` | | This month's Future Log is up to date. | El Future Log de este mes está al día. | O Future Log deste mês está em dia. | Das Future Log dieses Monats ist auf dem Stand. | Le Future Log de ce mois est à jour. |

Al releer, cada día lleva `dayTitle` y el mes `calendarTitle` y `monthTasks`. El fin lleva `close`.

## 11. Buscar

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `searchHint` | | A word or a #tag | Una palabra o una #etiqueta | Uma palavra ou uma #etiqueta | Ein Wort oder ein #Tag | Un mot ou un #tag |
| `filterOpen` | | Open | Abiertas | Abertas | Offen | Ouvertes |
| `monthGroup` | mes | = `monthTitle` | | | | |
| `futureGroup` | mes | = `wentToFuture` sin día: Future, October 2026 | Futuro, octubre de 2026 | Futuro, outubro de 2026 | Zukunft, Oktober 2026 | Futur, octobre 2026 |
| `searchEmpty` | | Search for a word, or type # and a tag. | Busca una palabra, o escribe # y una etiqueta. | Busque uma palavra, ou escreva # e uma etiqueta. | Such nach einem Wort, oder tippe # und einen Tag. | Cherche un mot, ou écris # et un tag. |
| `searchNothing` | | Nothing with those words. | Nada con esas palabras. | Nada com essas palavras. | Nichts mit diesen Wörtern. | Rien avec ces mots. |

Los otros tres filtros son los nombres de los signifiers (sección 4); el grupo de un día,
`longDateWithYear`; el de una colección, su título.

## 12. Clave

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `keyTitle` | | Key | Clave | Legenda | Legende | Légende |
| `keyBullets` | | Bullets | Bullets | Bullets | Bullets | Bullets |
| `keyStates` | | States | Estados | Estados | Zustände | États |
| `keySignifiers` | | Signifiers | Signifiers | Signifiers | Signifiers | Signifiers |
| `keyGestures` | | Gestures | Gestos | Gestos | Gesten | Gestes |
| `keyTaskHow` | | No prefix | Sin prefijo | Sem prefixo | Ohne Präfix | Sans préfixe |
| `keyEventHow` | | Starts with o and a space | Empieza con o y espacio | Começa com o e espaço | Beginnt mit o und Leerzeichen | Commence par o et un espace |
| `keyNoteHow` | | Starts with - and a space | Empieza con - y espacio | Começa com - e espaço | Beginnt mit - und Leerzeichen | Commence par - et un espace |
| `keyDoneHow` | | Tap the dot | Toca el punto | Toque no ponto | Tippe auf den Punkt | Touche le point |
| `keyMigratedHow` | | Press and hold: Migrate | Mantén pulsada: Migrar | Toque e segure: Migrar | Gedrückt halten: Migrieren | Appui long : Migrer |
| `keyScheduledHow` | | Press and hold: Schedule | Mantén pulsada: Programar | Toque e segure: Agendar | Gedrückt halten: Einplanen | Appui long : Planifier |
| `keyDiscardedHow` | | Press and hold: Discard | Mantén pulsada: Descartar | Toque e segure: Descartar | Gedrückt halten: Verwerfen | Appui long : Écarter |
| `keyMigratedLink` | | Tap the > to go where it went. | Toca el > para ir a donde fue. | Toque no > para ir aonde ela foi. | Tippe auf das >, um zu ihrer Kopie zu gehen. | Touche le > pour aller là où elle est partie. |
| `keyPriorityHow` | | Starts with * and a space | Empieza con * y espacio | Começa com * e espaço | Beginnt mit * und Leerzeichen | Commence par * et un espace |
| `keyInspirationHow` | | Starts with ! and a space | Empieza con ! y espacio | Começa com ! e espaço | Beginnt mit ! und Leerzeichen | Commence par ! et un espace |
| `keyExploreHow` | | Starts with ? and a space | Empieza con ? y espacio | Começa com ? e espaço | Beginnt mit ? und Leerzeichen | Commence par ? et un espace |
| `keyGestureTap` | | Tapping the symbol completes a task. | Tocar el símbolo completa una tarea. | Tocar no símbolo conclui uma tarefa. | Ein Tipp auf das Symbol erledigt eine Aufgabe. | Toucher le symbole termine une tâche. |
| `keyGestureHold` | | Holding an entry opens its actions. | Mantener pulsada una entrada abre sus acciones. | Tocar e segurar uma entrada abre suas ações. | Langes Drücken auf einen Eintrag öffnet seine Aktionen. | Un appui long sur une entrée ouvre ses actions. |
| `keyGestureSwipe` | | Swiping sideways on Today changes the day. | Deslizar a los lados en Hoy cambia de día. | Deslizar para os lados em Hoje muda o dia. | Seitlich wischen in Heute wechselt den Tag. | Glisser sur le côté dans Aujourd'hui change de jour. |
| `keyGestureDrag` | | Holding and dragging changes the order. | Mantener pulsada y arrastrar cambia el orden. | Segurar e arrastar muda a ordem. | Gedrückt halten und ziehen ändert die Reihenfolge. | Appuyer longuement et glisser change l'ordre. |
| `keyTapText` | | Tap the text to edit it. | Toca el texto para editarlo. | Toque no texto para editá-lo. | Tippe auf den Text, um ihn zu bearbeiten. | Touche le texte pour le modifier. |

El nombre de cada fila es `bulletTask`, `bulletEvent`, `bulletNote`, `stateDone`, `stateMigrated`,
`stateScheduled`, `stateDiscarded` y los tres `signifier*` (sección 4). Los símbolos que citan las
filas (`o`, `-`, `*`, `!`, `?`, `>`) son los del método, iguales en los cinco idiomas.

## 13. Ajustes

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `settingsTitle` | | Settings | Ajustes | Ajustes | Einstellungen | Réglages |
| `sectionDay` | | Day | Día | Dia | Tag | Jour |
| `dayStartRow` | | The day starts | El día empieza | O dia começa | Der Tag beginnt | La journée commence |
| `dayStartAt` | h | At 04:00 | A las 04:00 | Às 04:00 | Um 04:00 | À 04:00 |
| `weekStartRow` | | The week starts | La semana empieza | A semana começa | Die Woche beginnt | La semaine commence |
| `weekStartSystem` | día | Monday, like the system | El lunes, como el sistema | Na segunda-feira, como o sistema | Montag, wie im System | Le lundi, comme le système |
| `weekStartDay` | día | Sunday | El domingo | No domingo | Sonntag | Le dimanche |
| `weekStartFollowSystem` | | Like the system | Como el sistema | Como o sistema | Wie im System | Comme le système |
| `sectionReminder` | | Reminder | Recordatorio | Lembrete | Erinnerung | Rappel |
| `reminderRow` | | Reminder to go over the day | Recordatorio para repasar el día | Lembrete para repassar o dia | Erinnerung, den Tag durchzugehen | Rappel pour relire ta journée |
| `reminderOff` | | Off | Apagado | Desativado | Aus | Désactivé |
| `reminderAt` | hora | At 21:00 | A las 21:00 | Às 21:00 | Um 21:00 | À 21:00 |
| `reminderDenied` | | Bobbin's notifications are turned off in the system. | Las notificaciones de Bobbin están desactivadas en el sistema. | As notificações do Bobbin estão desativadas no sistema. | Die Benachrichtigungen von Bobbin sind im System deaktiviert. | Les notifications de Bobbin sont désactivées dans le système. |
| `openSystemSettings`* | | Open settings | Abrir ajustes | Abrir ajustes | Einstellungen öffnen | Ouvrir les réglages |
| `sectionPrivacy` | | Privacy | Privacidad | Privacidade | Datenschutz | Confidentialité |
| `lockRow` | | Lock the journal | Bloquear el diario | Bloquear o diário | Journal sperren | Verrouiller le journal |
| `lockSubtitle` | | Asks for your face, your fingerprint or your phone code | Pide tu cara, tu huella o el código del teléfono | Pede seu rosto, sua digital ou o código do telefone | Fragt nach deinem Gesicht, deinem Fingerabdruck oder dem Code des Handys | Demande ton visage, ton empreinte ou le code du téléphone |
| `lockUnavailable` | | Set a screen lock on your phone to use this. | Pon un bloqueo de pantalla en el teléfono para usarlo. | Configure um bloqueio de tela no telefone para usar isso. | Richte eine Bildschirmsperre auf dem Handy ein, um das zu nutzen. | Active un verrouillage d'écran sur ton téléphone pour l'utiliser. |
| `sectionNotebook` | | Notebook | Cuaderno | Caderno | Notizbuch | Carnet |
| `previewTask` | | Buy ink | Comprar tinta | Comprar tinta | Tinte kaufen | Acheter de l'encre |
| `previewEvent` | | Dinner with Ana | Cena con Ana | Jantar com a Ana | Abendessen mit Ana | Dîner avec Ana |
| `coverName` | id | Rose, Peach, Butter, Sage, Mint, Sky, Periwinkle, Lilac | Rosa, Melocotón, Mantequilla, Salvia, Menta, Cielo, Pervinca, Lila | Rosa, Pêssego, Manteiga, Sálvia, Hortelã, Céu, Pervinca, Lilás | Rosa, Pfirsich, Butter, Salbei, Minze, Himmel, Periwinkle, Flieder | Rose, Pêche, Beurre, Sauge, Menthe, Ciel, Pervenche, Lilas |
| `paperName` | id | dotted, lined, grid, blank | punteado, rayado, cuadrícula, liso | pontilhado, pautado, quadriculado, liso | gepunktet, liniert, kariert, blanko | pointillé, ligné, quadrillé, uni |
| `proLookHint` | | Sage and dotted are free; the rest come with Bobbin Pro. | Salvia y el punteado son gratis; lo demás, con Bobbin Pro. | Sálvia e o pontilhado são grátis; o resto vem com o Bobbin Pro. | Salbei und gepunktet sind kostenlos, der Rest kommt mit Bobbin Pro. | Sauge et le pointillé sont gratuits ; le reste vient avec Bobbin Pro. |
| `useThis`* | | Use this style | Usar este estilo | Usar este estilo | Diesen Stil nutzen | Utiliser ce style |
| `sectionBackup` | | Backup | Copia | Cópia | Sicherung | Sauvegarde |
| `exportRow` | | Export backup | Exportar copia | Exportar cópia | Kopie exportieren | Exporter une copie |
| `exportSubtitle` | | A zip with your journal in JSON and Markdown | Un zip con tu diario en JSON y en Markdown | Um zip com seu diário em JSON e em Markdown | Ein Zip mit deinem Journal als JSON und Markdown | Un zip avec ton journal en JSON et en Markdown |
| `exportNothing` | | There's nothing to back up yet. | Aún no hay nada que copiar. | Ainda não há nada para copiar. | Es gibt noch nichts zu sichern. | Il n'y a encore rien à copier. |
| `importRow` | | Import backup | Importar copia | Importar cópia | Kopie importieren | Importer une copie |
| `importSubtitle` | | Joins your journal, nothing gets deleted | Se junta con tu diario, sin borrar nada | Se junta ao seu diário, sem apagar nada | Wird mit deinem Journal zusammengeführt, nichts wird gelöscht | Se joint à ton journal, rien n'est supprimé |
| `sectionPro` | | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro |
| `proRow` | | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro |
| `proSubtitle` | | Covers, papers, trackers and widgets. One-time payment | Portadas, papeles, seguimientos y widgets. Pago único | Capas, papéis, trackers e widgets. Pagamento único | Umschläge, Papiere, Tracker und Widgets. Einmalzahlung | Couvertures, papiers, suivis et widgets. Paiement unique |
| `proOwned` | | Purchased. Thank you. | Comprado. Gracias. | Comprado. Obrigado. | Gekauft. Danke. | Acheté. Merci. |
| `restoreRow` | | Restore purchase | Restaurar compra | Restaurar compra | Kauf wiederherstellen | Restaurer l'achat |
| `restoreDone` | | Purchase restored. | Compra restaurada. | Compra restaurada. | Kauf wiederhergestellt. | Achat restauré. |
| `restoreNothing` | | There's no purchase to restore. | No hay ninguna compra que restaurar. | Não há nenhuma compra para restaurar. | Es gibt keinen Kauf zum Wiederherstellen. | Il n'y a aucun achat à restaurer. |
| `sectionMoreApps` | | More apps | Más apps | Mais apps | Weitere Apps | Plus d'apps |
| `siblingPurl` | | One line a day, read again every year | Una línea al día que vuelves a leer cada año | Uma linha por dia, relida a cada ano | Eine Zeile am Tag, jedes Jahr wieder gelesen | Une ligne par jour, relue chaque année |
| `siblingQuilt` | | Your habits, a year at a glance | Tus hábitos, un año a la vista | Seus hábitos, um ano à vista | Deine Gewohnheiten, ein Jahr im Blick | Tes habitudes, une année en un coup d'oeil |
| `siblingMood` | | How each day went, in colour | Cómo te ha ido cada día, en color | Como foi cada dia, em cores | Wie jeder Tag war, in Farbe | Comment chaque jour s'est passé, en couleur |
| `sectionAbout` | | About | Acerca de | Sobre | Über | À propos |
| `privacyRow` | | Privacy policy | Política de privacidad | Política de privacidade | Datenschutzerklärung | Politique de confidentialité |
| `version` | v | Version 1.0.0 | Versión 1.0.0 | Versão 1.0.0 | Version 1.0.0 | Version 1.0.0 |
| `wipeRow` | | Delete all data | Borrar todos los datos | Apagar todos os dados | Alle Daten löschen | Supprimer toutes les données |
| `wipeSubtitle` | | Leaves the journal empty on this phone. Bobbin Pro stays. | Deja el diario vacío en este teléfono. Bobbin Pro se conserva. | Deixa o diário vazio neste telefone. O Bobbin Pro continua. | Leert das Journal auf diesem Handy. Bobbin Pro bleibt. | Vide le journal sur ce téléphone. Bobbin Pro est conservé. |

- Los ids de `coverName` van en el orden de `docs/tecnico.md` 5 (`rose`, `peach`, `butter`, `sage`,
  `mint`, `sky`, `periwinkle`, `lilac`); los de `paperName`, `dotted`, `lined`, `grid`, `blank`. Bajo
  la vista previa se juntan con coma: "Salvia, punteado".
- La vista previa lleva `dayTitle` de un miércoles 23, `previewTask`, `previewEvent` y `tabToday`.
- Los diálogos de `dayStartRow` y `weekStartRow` se titulan con la fila; sus opciones son `clock(h, 0)`
  de 00:00 a 06:00, y `weekStartFollowSystem` más `weekdayNames` con la inicial en mayúscula.
- Más apps: el título de cada fila es el nombre de la app, que no se traduce.

## 14. Bobbin Pro

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `proTitle` | | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro | Bobbin Pro |
| `proOnce` | | One-time purchase, no subscription. | Compra única, sin suscripción. | Compra única, sem assinatura. | Einmalkauf, kein Abo. | Achat unique, sans abonnement. |
| `proCovers` | | Seven more covers | Siete portadas más | Mais sete capas | Sieben weitere Umschläge | Sept couvertures de plus |
| `proPapers` | | Lined, grid and blank paper | Rayado, cuadrícula y liso | Pautado, quadriculado e liso | Liniert, kariert und blanko | Ligné, quadrillé et uni |
| `proTrackers` | | More than one tracker | Más de un seguimiento | Mais de um tracker | Mehr als ein Tracker | Plus d'un suivi |
| `proMonthWidget` | | The month widget | El widget del mes | O widget do mês | Das Monats-Widget | Le widget du mois |
| `proLockWidget` | | The lock screen widget | El widget de la pantalla de bloqueo | O widget da tela de bloqueio | Das Sperrbildschirm-Widget | Le widget de l'écran verrouillé |
| `proFree` | | The whole method is free, and it will stay that way. | El método entero es gratis, y lo seguirá siendo. | O método inteiro é grátis, e vai continuar assim. | Die ganze Methode ist kostenlos und bleibt es. | Toute la méthode est gratuite, et le restera. |
| `buy`* | precio | Buy for 7.99 EUR | Comprar por 7,99 EUR | Comprar por 7,99 EUR | Für 7,99 EUR kaufen | Acheter pour 7,99 EUR |
| `restore`* | | Restore | Restaurar | Restaurar | Wiederherstellen | Restaurer |
| `storeUnavailable` | | The store is not available right now. | La tienda no está disponible ahora. | A loja não está disponível agora. | Der Store ist gerade nicht verfügbar. | La boutique n'est pas disponible pour le moment. |
| `buyFailed` | | The purchase could not be completed. | No se ha podido completar la compra. | Não foi possível concluir a compra. | Der Kauf konnte nicht abgeschlossen werden. | L'achat n'a pas pu être finalisé. |

- El precio de `buy` es el texto que devuelve la tienda, tal cual; nunca uno escrito en el código.
- `proLockWidget` solo sale en iOS. Cambiar esta lista toca en el mismo commit `SPEC.md` 7, las dos
  fichas y `store/revenuecat.md` (CLAUDE.md).
- Los botones de abajo son `buy`, `restore` y `notNow`; comprando, `working`.

## 15. Importar, exportar y borrar

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `importTitle` | | Import backup | Importar copia | Importar cópia | Kopie importieren | Importer une copie |
| `importSummary` | nuevas, actualizadas, iguales | The backup brings 12 new entries and 3 newer than yours; 40 were already here. Nothing gets deleted. | La copia trae 12 entradas nuevas y 3 más recientes que las tuyas; 40 ya estaban. No se borra nada. | A cópia traz 12 entradas novas e 3 mais recentes que as suas; 40 já estavam aqui. Nada é apagado. | Die Sicherung bringt 12 neue Einträge und 3, die neuer sind als deine; 40 waren schon da. Es wird nichts gelöscht. | La copie apporte 12 nouvelles entrées et 3 plus récentes que les tiennes ; 40 étaient déjà là. Rien n'est supprimé. |
| `importSummary` | 0, 0, iguales | The backup brings nothing new; 40 were already here. Nothing gets deleted. | La copia no trae nada nuevo; 40 ya estaban. No se borra nada. | A cópia não traz nada novo; 40 já estavam aqui. Nada é apagado. | Die Sicherung bringt nichts Neues; 40 waren schon da. Es wird nichts gelöscht. | La copie n'apporte rien de nouveau ; 40 étaient déjà là. Rien n'est supprimé. |
| `importAction`* | | Import | Importar | Importar | Importieren | Importer |
| `importDone` | n | Journal up to date: 15 changes. | Diario al día: 15 cambios. | Diário em dia: 15 alterações. | Journal aktuell: 15 Änderungen. | Journal à jour : 15 modifications. |
| `importDone` | 0 | Journal up to date: there was nothing to change. | Diario al día: no había nada que cambiar. | Diário em dia: não havia nada para mudar. | Journal aktuell: es gab nichts zu ändern. | Journal à jour : il n'y avait rien à changer. |
| `importFailedTitle` | | Couldn't import | No se ha podido importar | Não foi possível importar | Import fehlgeschlagen | Échec de l'import |
| `importNotBackup` | | That file is not a Bobbin backup. | Ese fichero no es una copia de Bobbin. | Esse arquivo não é uma cópia do Bobbin. | Diese Datei ist keine Sicherung von Bobbin. | Ce fichier n'est pas une copie de Bobbin. |
| `importDamaged` | | The backup is incomplete or damaged. Your journal wasn't touched. | La copia está incompleta o dañada. Tu diario no se ha tocado. | A cópia está incompleta ou danificada. Seu diário não foi alterado. | Die Sicherung ist unvollständig oder beschädigt. Dein Journal wurde nicht verändert. | La copie est incomplète ou endommagée. Ton journal n'a pas été touché. |
| `importTooNew` | | This backup is from a newer version of Bobbin. Update the app and try again. | Esta copia es de una versión más nueva de Bobbin. Actualiza la app y vuelve a probar. | Esta cópia é de uma versão mais nova do Bobbin. Atualize o app e tente de novo. | Diese Sicherung stammt aus einer neueren Version von Bobbin. Aktualisiere die App und versuch es erneut. | Cette copie vient d'une version plus récente de Bobbin. Mets à jour l'app et réessaie. |
| `importEmpty` | | The backup has no entries or collections. | La copia no tiene entradas ni colecciones. | A cópia não tem entradas nem coleções. | Die Sicherung enthält keine Einträge und keine Sammlungen. | La copie ne contient ni entrées ni collections. |
| `importIsSibling` | app | This is a backup from Purl. You'll be able to bring its entries in a future version. | Es una copia de Purl. Podrás traer sus entradas en una próxima versión. | Isso é uma cópia do Purl. Você vai poder trazer as entradas dele numa próxima versão. | Das ist eine Sicherung von Purl. Du kannst ihre Einträge in einer späteren Version übernehmen. | C'est une copie de Purl. Tu pourras importer ses entrées dans une prochaine version. |
| `exportFailed` | | Couldn't save the backup. | No se ha podido guardar la copia. | Não foi possível salvar a cópia. | Die Sicherung konnte nicht gespeichert werden. | Impossible d'enregistrer la copie. |
| `wipeTitle` | | Delete the whole journal? | ¿Borrar todo el diario? | Apagar todo o diário? | Das ganze Journal löschen? | Supprimer tout le journal ? |
| `wipeText` | | All entries, collections and settings on this phone are deleted. Backups you exported are not touched, and Bobbin Pro stays. | Se borran todas las entradas, colecciones y ajustes de este teléfono. Las copias que hayas exportado no se tocan, y Bobbin Pro se conserva. | Todas as entradas, coleções e ajustes deste telefone são apagados. As cópias que você exportou não são alteradas, e o Bobbin Pro continua. | Alle Einträge, Sammlungen und Einstellungen auf diesem Handy werden gelöscht. Exportierte Sicherungen bleiben unberührt, und Bobbin Pro bleibt. | Toutes les entrées, collections et réglages de ce téléphone sont supprimés. Les copies que tu as exportées ne sont pas touchées, et Bobbin Pro est conservé. |
| `wipeContinue`* | | Continue | Continuar | Continuar | Weiter | Continuer |
| `wipeConfirmTitle` | palabra | Type the word delete to confirm | Escribe la palabra borrar para confirmar | Digite a palavra apagar para confirmar | Gib das Wort löschen ein, um zu bestätigen | Écris le mot effacer pour confirmer |
| `wipeWord` | | delete | borrar | apagar | löschen | effacer |
| `wipeAction`* | | Delete everything | Borrar todo | Apagar tudo | Alles löschen | Tout supprimer |

- `importSummary` cuenta entradas y colecciones juntas (`added`, `updated` y `same` de
  `docs/tecnico.md` 6.9) y las llama entradas. Plurales por la regla del método 3 ("1 entrada nueva",
  "1 más reciente que las tuyas", "1 ya estaba"). Una parte que vale 0 no aparece en la frase; si
  `nuevas` y `actualizadas` son 0, la fila de "nada nuevo". Si `iguales` es 0, la frase acaba tras las
  nuevas y actualizadas, antes de "No se borra nada.".
- `importDone(n)` cuenta `added + updated`, con su frase propia para 0: importar la copia que ya se
  tiene no cambia nada, y "0 cambios" suena a fallo.
- `importIsSibling` recibe el nombre de la app reconocida (`Purl`, `MoodTraker` o `Quilt`, 4.5) y
  vale para v1.0 y v1.1; en v1.2 la importación se desvía (sección 24).
- `wipeConfirmTitle` lleva dentro `wipeWord` del mismo idioma, y la comparación es con `fold`
  (`docs/pantallas.md` 15.4): en alemán vale "löschen" o "loschen".
- Exportar no tiene más textos: `exportFailed` con `ok`; cancelar no enseña nada.

## 16. Bloqueo y avisos de carga

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `unlock`* | | Unlock | Desbloquear | Desbloquear | Entsperren | Déverrouiller |
| `lockPromptTitle` | | Open Bobbin | Abrir Bobbin | Abrir Bobbin | Bobbin öffnen | Ouvrir Bobbin |
| `lockPromptSubtitle` | | Your journal is locked | Tu diario está bloqueado | Seu diário está bloqueado | Dein Journal ist gesperrt | Ton journal est verrouillé |
| `updateNeeded` | | This journal was written with a newer version of Bobbin. Update the app to open it; nothing was touched. | Este diario se escribió con una versión más nueva de Bobbin. Actualiza la app para abrirlo; no se ha tocado nada. | Este diário foi escrito com uma versão mais nova do Bobbin. Atualize o app para abri-lo; nada foi alterado. | Dieses Journal wurde mit einer neueren Version von Bobbin geschrieben. Aktualisiere die App, um es zu öffnen; nichts wurde verändert. | Ce journal a été écrit avec une version plus récente de Bobbin. Mets à jour l'app pour l'ouvrir ; rien n'a été touché. |
| `openStore`* | | Open the store | Abrir la tienda | Abrir a loja | Store öffnen | Ouvrir la boutique |
| `migrationFailed` | | Couldn't prepare the journal for this version. It is intact and nothing was touched. | No se ha podido preparar el diario para esta versión. Sigue intacto y no se ha tocado nada. | Não foi possível preparar o diário para esta versão. Ele continua intacto e nada foi alterado. | Das Journal konnte nicht für diese Version vorbereitet werden. Es ist unversehrt, nichts wurde verändert. | Impossible de préparer le journal pour cette version. Il est intact et rien n'a été touché. |

La pantalla de bloqueo lleva `appName` y `unlock`. `lockPromptSubtitle` es también el
`localizedReason` de iOS (`docs/tecnico.md` 6.15).

## 17. Compartir

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `shareImage` | | Share as image | Compartir como imagen | Compartilhar como imagem | Als Bild teilen | Partager en image |
| `shareText` | | Share as text | Compartir como texto | Compartilhar como texto | Als Text teilen | Partager en texte |
| `sharePages` | n (>= 2) | 3 images | 3 imágenes | 3 imagens | 3 Bilder | 3 images |
| `pageOf` | i, n | 2 of 3 | 2 de 3 | 2 de 3 | 2 von 3 | 2 sur 3 |

La marca de la imagen y la última línea del texto son `appName`. El título de la página compartida es
`longDateWithYear` (un día), `monthTitle` (un mes) o el título de la colección; el mes añade
`monthTasks`.

## 18. Notificación

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `reminderTitle` | | A moment to go over the day | Un momento para repasar el día | Um momento para repassar o dia | Ein Moment, um den Tag durchzugehen | Un moment pour relire ta journée |
| `reminderBody` | | Read today back and decide what comes next. | Relee lo de hoy y decide qué sigue. | Releia o dia de hoje e decida o que vem depois. | Lies den heutigen Tag nach und entscheide, wie es weitergeht. | Relis ta journée et décide de la suite. |
| `reminderChannel` | | Reminder to go over the day | Recordatorio para repasar el día | Lembrete para repassar o dia | Erinnerung, den Tag durchzugehen | Rappel pour relire ta journée |

Regla del método 2: sin parámetros, nunca. `reminderChannel` es el nombre del canal
`bobbin-reflection` que Android enseña en sus ajustes.

## 19. Widgets

Van en `Strings.kt` (Glance) y en el `enum L` de `BobbinWidget.swift` (WidgetKit), con los mismos
textos y la misma regla de plurales.

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `widgetDate` | fecha | WED 23 SEP | MIÉ 23 SEPT | QUA 23 SET | MI 23 SEPT | MER 23 SEPT |
| `widgetWeekday` | fecha | Wednesday | miércoles | quarta-feira | Mittwoch | mercredi |
| `widgetMonth` | fecha | September | septiembre | setembro | September | septembre |
| `widgetMonthName` | mes | September | Septiembre | Setembro | September | Septembre |
| `widgetOpen` | n | open | abierta / abiertas | aberta / abertas | offen | ouverte / ouvertes |
| `widgetDone` | n | done | hecha / hechas | feita / feitas | erledigt | faite / faites |
| `widgetEvents` | n | event / events | evento / eventos | evento / eventos | Ereignis / Ereignisse | événement / événements |
| `widgetReview` | | To review | Por revisar | Para revisar | Durchsehen | À revoir |
| `widgetUnlock` | | Tap to turn it on | Toca para activarlo | Toque para ativar | Tippen zum Aktivieren | Touche pour l'activer |
| `pickerTodayName` | | Today | Hoy | Hoje | Heute | Aujourd'hui |
| `pickerTodayDescription` | | Today's open, done and events, without any text | Abiertas, hechas y eventos de hoy, sin ningún texto | Abertas, feitas e eventos de hoje, sem nenhum texto | Offene und erledigte Aufgaben und Ereignisse von heute, ohne Text | Ouvertes, faites et événements du jour, sans aucun texte |
| `pickerMonthName` | | The month | El mes | O mês | Der Monat | Le mois |
| `pickerMonthDescription` | | One dot for each day with entries | Un punto por cada día con entradas | Um ponto para cada dia com entradas | Ein Punkt für jeden Tag mit Einträgen | Un point pour chaque jour avec des entrées |
| `pickerLockName` | | Open tasks | Tareas abiertas | Tarefas abertas | Offene Aufgaben | Tâches ouvertes |
| `pickerLockDescription` | | Today's open tasks on the lock screen | Las tareas abiertas de hoy en la pantalla de bloqueo | As tarefas abertas de hoje na tela de bloqueio | Die offenen Aufgaben von heute auf dem Sperrbildschirm | Les tâches ouvertes du jour sur l'écran verrouillé |

- `widgetDate` es `weekdayShort` y `monthShort` sin punto, en mayúsculas, con el día en medio.
  `widgetMonthName` se pinta en mayúsculas (`SEPTIEMBRE`).
- `widgetOpen`, `widgetDone` y `widgetEvents` devuelven solo la palabra, en la forma que pide `n`: el
  número va aparte en su tamaño (`docs/pantallas.md` 18.1). El rectangular de la pantalla de bloqueo
  los junta con un espacio: "3 abiertas", "1 evento".
- Sin Pro, el widget del mes y el de bloqueo llevan `proTitle` y `widgetUnlock`.
- `picker*` son el nombre y la descripción que enseña el selector de widgets de cada sistema; no se
  pintan dentro del widget (secciones 21 y 22).

## 20. Accesibilidad

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `entryDescription` | bullet, estado, signifiers, texto | Done task, priority: buy bread | Tarea hecha, prioridad: comprar pan | Tarefa feita, prioridade: comprar pão | Erledigte Aufgabe, Priorität: Brot kaufen | Tâche faite, priorité : acheter du pain |
| `a11yWentTo` | destino | Moved to Sep 24 | Fue a 24 sept | Foi para 24 set | Verschoben nach 24. Sept. | Partie vers 24 sept. |
| `a11yComplete` | | Complete | Completar | Concluir | Erledigen | Terminer |
| `a11yReopen` | | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| `a11yMoveUp` | | Move up | Subir | Subir | Nach oben | Monter |
| `a11yMoveDown` | | Move down | Bajar | Descer | Nach unten | Descendre |
| `a11yCapture` | | New entry | Nueva entrada | Nova entrada | Neuer Eintrag | Nouvelle entrée |
| `a11yDayRow` | día, nombre, n | 3, Thursday, 2 entries | 3, jueves, 2 entradas | 3, quinta-feira, 2 entradas | 3., Donnerstag, 2 Einträge | 3, jeudi, 2 entrées |
| `a11yDayRow` | día, nombre, 0 | 3, Thursday, no entries | 3, jueves, sin entradas | 3, quinta-feira, sem entradas | 3., Donnerstag, keine Einträge | 3, jeudi, aucune entrée |
| `a11yTrackerCell` | fila, fecha, marcada | Two litres, September 23, marked / not marked | Dos litros, 23 de septiembre, marcada / sin marcar | Dois litros, 23 de setembro, marcada / sem marcar | Zwei Liter, 23. September, markiert / nicht markiert | Deux litres, 23 septembre, cochée / pas cochée |
| `a11yTrackerMarked` | días | Marked: 1, 2, 5 and 9 | Marcados: 1, 2, 5 y 9 | Marcados: 1, 2, 5 e 9 | Markiert: 1, 2, 5 und 9 | Cochés : 1, 2, 5 et 9 |
| `a11yTrackerMarked` | ninguno | No day marked | Ningún día marcado | Nenhum dia marcado | Kein Tag markiert | Aucun jour coché |

- `entryDescription` junta `glyphName` del estado (sección 4), los signifiers puestos por su nombre en
  el orden prioridad, inspiración, explorar, separados por coma, dos puntos y el texto. Los nombres de
  los signifiers van en minúscula salvo en alemán. Sin signifiers, el glifo, dos puntos y el texto:
  "Evento: cena con Ana". Una migrada o programada añade `. ` y `a11yWentTo` con su destino
  (`wentTo*`, sección 4), si la copia existe.
- El texto del usuario se lee tal cual, sin cambiarle la caja; "comprar pan" es solo el ejemplo.
- Las acciones personalizadas son `a11yComplete` o `a11yReopen`, `actionMigrate`, `actionSchedule`,
  `a11yMoveUp`, `a11yMoveDown` y `a11yMoreActions`. Los iconos usan las `a11y*` de la sección 3.
- `a11yTrackerCell` lee el título de la fila, `shortDate` y el estado; `a11yTrackerMarked` junta los
  días con `joinAnd`. Sin totales ni porcentajes.

## 21. iOS

`iosApp/iosApp/<lang>.lproj/InfoPlist.strings`:

| Clave | en | es | pt | de | fr |
|---|---|---|---|---|---|
| `CFBundleDisplayName` | Bobbin | Bobbin | Bobbin | Bobbin | Bobbin |
| `NSFaceIDUsageDescription` | To open your journal with Face ID when the lock is on. | Para abrir tu diario con Face ID cuando el bloqueo está puesto. | Para abrir seu diário com Face ID quando o bloqueio estiver ativado. | Um dein Journal mit Face ID zu öffnen, wenn die Sperre aktiv ist. | Pour ouvrir ton journal avec Face ID quand le verrouillage est actif. |

No hay `NSPhotoLibraryAddUsageDescription`: compartir abre la hoja del sistema y nada se guarda en la
galería por su cuenta (`docs/tecnico.md` 6.10).

`iosApp/BobbinWidget/<lang>.lproj/Localizable.strings`: `configurationDisplayName` y `description` de
cada widget son literales en inglés en Swift (`pickerTodayName`, `pickerTodayDescription`,
`pickerMonthName`, `pickerMonthDescription`, `pickerLockName`, `pickerLockDescription`, columna en) y
se traducen aquí con la clave igual al literal inglés y el valor de la columna de cada idioma
(sección 19).

## 22. Android: `strings.xml`

| Clave | en | es | pt | de | fr |
|---|---|---|---|---|---|
| `app_name` | Bobbin | Bobbin | Bobbin | Bobbin | Bobbin |
| `widget_today_description` | = `pickerTodayDescription` | | | | |
| `widget_month_description` | = `pickerMonthDescription` | | | | |
| `widget_preview_date` | WED 23 SEP | MIÉ 23 SEPT | QUA 23 SET | MI 23 SEPT | MER 23 SEPT |
| `widget_preview_open` | open | abiertas | abertas | offen | ouvertes |
| `widget_preview_done` | done | hechas | feitas | erledigt | faites |
| `widget_preview_events` | events | eventos | eventos | Ereignisse | événements |
| `widget_preview_month` | SEPTEMBER | SEPTIEMBRE | SETEMBRO | SEPTEMBER | SEPTEMBRE |

Las `widget_preview_*` las leen los XML de previsualización del selector, que se mantienen a mano
(`docs/tecnico.md` 8.2): enseñan un miércoles 23 de septiembre con números de ejemplo en plural.
`values/strings.xml` es inglés; `values-es`, `values-pt`, `values-de` y `values-fr`, los demás.

---

## 23. v1.1

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `continueCollection` | | Continue in a new collection | Continuar en una colección nueva | Continuar em uma nova coleção | In einer neuen Sammlung fortsetzen | Continuer dans une nouvelle collection |
| `continueTitle` | | Continue in... | Continuar en... | Continuar em... | Fortsetzen in... | Continuer dans... |
| `create`* | | Create | Crear | Criar | Erstellen | Créer |
| `threadFrom` | título | Continued from Reading 2025 | Viene de Lecturas 2025 | Vem de Leituras 2025 | Fortsetzung von Lektüre 2025 | Suite de Lectures 2025 |
| `threadNext` | título | Continues in Reading 2027 | Sigue en Lecturas 2027 | Continua em Leituras 2027 | Weiter in Lektüre 2027 | Continue dans Lectures 2027 |
| `newNotebookRow` | | New notebook | Nuevo cuaderno | Novo caderno | Neues Notizbuch | Nouveau carnet |
| `newNotebookSub` | | Go through collections and tasks to start another | Repasa colecciones y tareas para empezar otro | Repasse coleções e tarefas para começar outro | Geh Sammlungen und Aufgaben durch, um ein neues zu beginnen | Passe en revue collections et tâches pour en commencer un autre |
| `notebookKeep` | | Keep | Mantener | Manter | Behalten | Garder |
| `notebookContinue` | | Continue in a new one | Continuar en una nueva | Continuar em uma nova | In einer neuen fortsetzen | Continuer dans une nouvelle |
| `notebookArchive` | | Archive | Archivar | Arquivar | Archivieren | Archiver |
| `notebookDone` | fecha | New notebook from January 1, 2027. | Cuaderno nuevo desde el 1 de enero de 2027. | Caderno novo desde 1 de janeiro de 2027. | Neues Notizbuch seit dem 1. Januar 2027. | Nouveau carnet depuis le 1er janvier 2027. |
| `notebookUntil` | fecha | Notebook until December 31, 2026 | Cuaderno hasta el 31 de diciembre de 2026 | Caderno até 31 de dezembro de 2026 | Notizbuch bis 31. Dezember 2026 | Carnet jusqu'au 31 décembre 2026 |
| `scheduleFor` | fecha | Schedule for Mar 14 | Programar para el 14 mar | Agendar para 14 mar | Für 14. März einplanen | Planifier pour le 14 mars |
| `tileLabel` | | Note in Bobbin | Anotar en Bobbin | Anotar no Bobbin | In Bobbin notieren | Noter dans Bobbin |
| `captureSaved` | | Noted in Bobbin. | Anotado en Bobbin. | Anotado no Bobbin. | In Bobbin notiert. | Noté dans Bobbin. |
| `captureEmpty` | | There was nothing to note. | No había nada que anotar. | Não havia nada para anotar. | Es gab nichts zu notieren. | Il n'y avait rien à noter. |
| `captureNeedsPro` | | Noting from outside the app is part of Bobbin Pro. Open the app to see it. | Anotar desde fuera de la app es de Bobbin Pro. Abre la app para verlo. | Anotar de fora do app é do Bobbin Pro. Abra o app para ver. | Von außerhalb der App notieren gehört zu Bobbin Pro. Öffne die App, um es zu sehen. | Noter depuis l'extérieur de l'app fait partie de Bobbin Pro. Ouvre l'app pour le voir. |
| `proBook` | | The book as a PDF | El libro en PDF | O livro em PDF | Das Buch als PDF | Le livre en PDF |
| `proCapture` | Android | Note from Quick Settings | Anotar desde Ajustes rápidos | Anotar pelas Configurações rápidas | Aus den Schnelleinstellungen notieren | Noter depuis les réglages rapides |
| `proCapture` | iOS | Note with Siri and Shortcuts | Anotar desde Siri y Atajos | Anotar pela Siri e Atalhos | Mit Siri und Kurzbefehlen notieren | Noter avec Siri et Raccourcis |
| `bookRow` | | Book as PDF | Libro en PDF | Livro em PDF | Buch als PDF | Livre en PDF |
| `bookSub` | | The notebook laid out, to print or keep | El cuaderno maquetado, para imprimir o guardar | O caderno diagramado, para imprimir ou guardar | Das Notizbuch gesetzt, zum Drucken oder Aufbewahren | Le carnet mis en page, à imprimer ou à garder |
| `bookAction` | | Make the book as a PDF | Hacer el libro en PDF | Fazer o livro em PDF | Das Buch als PDF erstellen | Faire le livre en PDF |
| `bookRange` | desde, hasta | September 2026 to August 2027 | septiembre de 2026 a agosto de 2027 | setembro de 2026 a agosto de 2027 | September 2026 bis August 2027 | septembre 2026 à août 2027 |
| `bookMaking` | n, total | Laying out the book: page 12 of 80 | Maquetando el libro: página 12 de 80 | Diagramando o livro: página 12 de 80 | Buch wird gesetzt: Seite 12 von 80 | Mise en page du livre : page 12 sur 80 |
| `bookFailed` | | Couldn't create the book. | No se ha podido crear el libro. | Não foi possível criar o livro. | Das Buch konnte nicht erstellt werden. | Impossible de créer le livre. |

- `scheduleFor` usa `abbrDate` ("14 mar"); en inglés, "Mar 14".
- `proCapture` cambia de texto según la plataforma (`onIos`), no de clave.
- El libro reutiliza `appName`, `bookRange`, las secciones de la Clave (sección 12, sin los gestos),
  el Índice con número de página en vez de etiqueta, `monthTitle`, `calendarTitle`, `monthTasks` y
  `longDateWithYear`.
- `bookRange` usa `monthYear` en los dos extremos.

### Palabras de las fechas en lenguaje natural

Tablas de `dateHint` (`docs/tecnico.md` 12.4), en `Strings.kt` junto a las demás. Los días de la
semana y los meses son los de `weekdayNames` y `monthNames`, comparados con `fold`.

| Qué | en | es | pt | de | fr |
|---|---|---|---|---|---|
| Hoy | today | hoy | hoje | heute | aujourd'hui |
| Mañana | tomorrow | mañana | amanhã | morgen | demain |
| Pasado mañana | day after tomorrow | pasado mañana | depois de amanhã | übermorgen | après-demain |
| Un día del mes | on the 14th, the 14th | el 14 | dia 14, no dia 14 | am 14. | le 14 |
| Día y mes | March 14, 14 March | 14 de marzo | 14 de março | 14. März | 14 mars |
| Día y mes en cifras | 3/14 | 14/3 | 14/3 | 14.3. | 14/3 |

En inglés, "3/14" es mes y día; en los otros cuatro, día y mes. `dateHint` recibe el idioma de `S`.

### Siri y Atajos (iOS)

El código Swift usa los literales en inglés; las traducciones van en `<lang>.lproj/Localizable.strings`
(título, descripción, parámetro, diálogos) y `<lang>.lproj/AppShortcuts.strings` (frases), con la clave
igual al literal inglés (`docs/tecnico.md` 12.5).

| Clave (literal inglés) | es | pt | de | fr |
|---|---|---|---|---|
| `Note in Bobbin` (título) | Anotar en Bobbin | Anotar no Bobbin | In Bobbin notieren | Noter dans Bobbin |
| `Adds an entry to today in your journal.` (descripción) | Añade una entrada al día de hoy en tu diario. | Adiciona uma entrada ao dia de hoje no seu diário. | Fügt dem heutigen Tag in deinem Journal einen Eintrag hinzu. | Ajoute une entrée à la journée d'aujourd'hui dans ton journal. |
| `Text` (parámetro) | Texto | Texto | Text | Texte |
| `What do you want to note?` (petición del parámetro) | ¿Qué quieres anotar? | O que você quer anotar? | Was möchtest du notieren? | Qu'est-ce que tu veux noter ? |
| `Noted in Bobbin.` (`captureSaved`) | Anotado en Bobbin. | Anotado no Bobbin. | In Bobbin notiert. | Noté dans Bobbin. |
| `There was nothing to note.` (`captureEmpty`) | No había nada que anotar. | Não havia nada para anotar. | Es gab nichts zu notieren. | Il n'y avait rien à noter. |
| `Noting from outside the app is part of Bobbin Pro. Open the app to see it.` (`captureNeedsPro`) | Anotar desde fuera de la app es de Bobbin Pro. Abre la app para verlo. | Anotar de fora do app é do Bobbin Pro. Abra o app para ver. | Von außerhalb der App notieren gehört zu Bobbin Pro. Öffne die App, um es zu sehen. | Noter depuis l'extérieur de l'app fait partie de Bobbin Pro. Ouvre l'app pour le voir. |
| `Note in ${applicationName}` (frase) | Anota en ${applicationName} | Anote no ${applicationName} | Notiere in ${applicationName} | Note dans ${applicationName} |
| `Add an entry to ${applicationName}` (frase) | Añade una entrada en ${applicationName} | Adicione uma entrada ao ${applicationName} | Füge einen Eintrag zu ${applicationName} hinzu | Ajoute une entrée à ${applicationName} |

El texto que llega pasa por `rapidParse` como en Hoy: sin prefijo, la entrada es una tarea.

### Banco de preguntas de la reflexión

`S.question(i)`, sesenta, en este orden (`QUESTION_COUNT`, `docs/tecnico.md` 12.3). Sirven para
releer un día o un mes, así que hablan de "estos días" y nunca de una fecha. Breves, sobre lo escrito
y lo vivido; ninguna presupone algo triste, habla de salud mental ni pide un número.

| # | en | es | pt | de | fr |
|---|---|---|---|---|---|
| 1 | What wouldn't you migrate if you had to rewrite it by hand? | ¿Qué no migrarías si tuvieras que reescribirlo a mano? | O que você não migraria se tivesse que reescrever à mão? | Was würdest du nicht migrieren, wenn du es von Hand neu schreiben müsstest? | Que ne migrerais-tu pas s'il fallait le réécrire à la main ? |
| 2 | Which task has been waiting the longest? | ¿Qué tarea lleva más tiempo esperando? | Qual tarefa está esperando há mais tempo? | Welche Aufgabe wartet schon am längsten? | Quelle tâche attend depuis le plus longtemps ? |
| 3 | What did you do that you never wrote down? | ¿Qué hiciste que no llegaste a apuntar? | O que você fez e não chegou a anotar? | Was hast du getan, ohne es aufzuschreiben? | Qu'as-tu fait sans jamais le noter ? |
| 4 | Which event from these days do you want to remember? | ¿Qué evento de estos días quieres recordar? | Qual evento destes dias você quer lembrar? | An welches Ereignis dieser Tage willst du dich erinnern? | De quel événement de ces jours veux-tu te souvenir ? |
| 5 | Which note surprised you when you read it again? | ¿Qué nota te ha sorprendido al releerla? | Qual nota te surpreendeu ao reler? | Welche Notiz hat dich beim Nachlesen überrascht? | Quelle note te surprend en la relisant ? |
| 6 | What would you cross out today without regret? | ¿Qué tacharías hoy sin pena? | O que você riscaria hoje sem pena? | Was würdest du heute ohne Bedauern durchstreichen? | Que barrerais-tu aujourd'hui sans regret ? |
| 7 | What was worth the time it took? | ¿Qué mereció el tiempo que le dedicaste? | O que valeu o tempo que você dedicou? | Was war die Zeit wert, die es gekostet hat? | Qu'est-ce qui valait le temps que tu y as mis ? |
| 8 | What did you say yes to that you'd say no to now? | ¿A qué dijiste que sí y ahora dirías que no? | Para o que você disse sim e agora diria não? | Wozu hast du Ja gesagt, wozu du jetzt Nein sagen würdest? | À quoi as-tu dit oui et dirais-tu non maintenant ? |
| 9 | Which task turned out smaller than you thought? | ¿Qué tarea resultó más pequeña de lo que pensabas? | Qual tarefa foi menor do que você pensava? | Welche Aufgabe war kleiner als gedacht? | Quelle tâche s'est révélée plus petite que prévu ? |
| 10 | What will you do first tomorrow? | ¿Qué es lo primero que harás mañana? | O que você vai fazer primeiro amanhã? | Was machst du morgen als Erstes? | Que feras-tu en premier demain ? |
| 11 | What deserves a collection of its own? | ¿Qué merece una colección propia? | O que merece uma coleção própria? | Was verdient eine eigene Sammlung? | Qu'est-ce qui mérite sa propre collection ? |
| 12 | Which idea keeps coming back? | ¿Qué idea vuelve una y otra vez? | Que ideia volta sempre? | Welche Idee kommt immer wieder? | Quelle idée revient sans cesse ? |
| 13 | Who shows up most in these pages? | ¿Quién aparece más en estas páginas? | Quem aparece mais nestas páginas? | Wer taucht auf diesen Seiten am häufigsten auf? | Qui apparaît le plus dans ces pages ? |
| 14 | What did you finish that you started long ago? | ¿Qué terminaste que habías empezado hace tiempo? | O que você terminou que tinha começado há tempos? | Was hast du beendet, das du vor langer Zeit angefangen hattest? | Qu'as-tu terminé que tu avais commencé il y a longtemps ? |
| 15 | What would you do differently with the same days? | ¿Qué harías distinto con los mismos días? | O que você faria diferente com os mesmos dias? | Was würdest du mit denselben Tagen anders machen? | Que ferais-tu autrement avec les mêmes jours ? |
| 16 | What did you learn that you want to keep? | ¿Qué aprendiste que quieras conservar? | O que você aprendeu e quer guardar? | Was hast du gelernt, das du behalten willst? | Qu'as-tu appris que tu veux garder ? |
| 17 | Which task is really several tasks? | ¿Qué tarea son en realidad varias? | Qual tarefa é na verdade várias? | Welche Aufgabe sind eigentlich mehrere? | Quelle tâche en cache en fait plusieurs ? |
| 18 | What can wait? | ¿Qué puede esperar? | O que pode esperar? | Was kann warten? | Qu'est-ce qui peut attendre ? |
| 19 | What went better than you expected? | ¿Qué salió mejor de lo que esperabas? | O que saiu melhor do que você esperava? | Was lief besser als erwartet? | Qu'est-ce qui s'est mieux passé que prévu ? |
| 20 | What are you glad you wrote down? | ¿Qué te alegra haber apuntado? | O que você gosta de ter anotado? | Worüber bist du froh, es aufgeschrieben zu haben? | Qu'est-ce que tu as bien fait de noter ? |
| 21 | What did you notice only when reading it again? | ¿Qué has visto solo al releerlo? | O que você só percebeu ao reler? | Was ist dir erst beim Nachlesen aufgefallen? | Qu'as-tu remarqué seulement en relisant ? |
| 22 | What did you leave unfinished on purpose? | ¿Qué dejaste a medias a propósito? | O que você deixou pela metade de propósito? | Was hast du absichtlich unfertig gelassen? | Qu'as-tu laissé inachevé exprès ? |
| 23 | Which place do you remember from these days? | ¿Qué lugar recuerdas de estos días? | Que lugar você lembra destes dias? | An welchen Ort dieser Tage erinnerst du dich? | De quel lieu te souviens-tu ces jours-ci ? |
| 24 | Which conversation would you write down in full? | ¿Qué conversación apuntarías entera? | Que conversa você anotaria inteira? | Welches Gespräch würdest du ganz aufschreiben? | Quelle conversation noterais-tu en entier ? |
| 25 | What small thing made a day better? | ¿Qué cosa pequeña mejoró un día? | Que coisa pequena melhorou um dia? | Welche Kleinigkeit hat einen Tag besser gemacht? | Quelle petite chose a rendu une journée meilleure ? |
| 26 | Which task do you keep migrating, and why? | ¿Qué tarea sigues migrando, y por qué? | Qual tarefa você continua migrando, e por quê? | Welche Aufgabe migrierst du immer wieder, und warum? | Quelle tâche continues-tu à migrer, et pourquoi ? |
| 27 | What would you like more time for? | ¿Para qué te gustaría tener más tiempo? | Para que você gostaria de ter mais tempo? | Wofür hättest du gern mehr Zeit? | Pour quoi aimerais-tu avoir plus de temps ? |
| 28 | What would you remove from your list if nobody asked? | ¿Qué quitarías de tu lista si nadie te lo pidiera? | O que você tiraria da sua lista se ninguém pedisse? | Was würdest du von deiner Liste streichen, wenn niemand danach fragte? | Que retirerais-tu de ta liste si personne ne le demandait ? |
| 29 | What did you do for someone else? | ¿Qué hiciste por otra persona? | O que você fez por outra pessoa? | Was hast du für jemand anderen getan? | Qu'as-tu fait pour quelqu'un d'autre ? |
| 30 | What did someone do for you? | ¿Qué hizo alguien por ti? | O que alguém fez por você? | Was hat jemand für dich getan? | Qu'est-ce que quelqu'un a fait pour toi ? |
| 31 | What do you want to repeat? | ¿Qué quieres repetir? | O que você quer repetir? | Was willst du wiederholen? | Que veux-tu refaire ? |
| 32 | What was the best ordinary moment? | ¿Cuál fue el mejor momento corriente? | Qual foi o melhor momento comum? | Was war der schönste gewöhnliche Moment? | Quel a été le meilleur moment ordinaire ? |
| 33 | Which question is still open? | ¿Qué pregunta sigue abierta? | Que pergunta continua aberta? | Welche Frage ist noch offen? | Quelle question reste ouverte ? |
| 34 | What did you read, watch or hear that deserves a note? | ¿Qué leíste, viste u oíste que merezca una nota? | O que você leu, viu ou ouviu que merece uma nota? | Was hast du gelesen, gesehen oder gehört, das eine Notiz verdient? | Qu'as-tu lu, vu ou entendu qui mérite une note ? |
| 35 | What did you decide, and what came of it? | ¿Qué decidiste, y qué salió de ello? | O que você decidiu, e o que saiu disso? | Was hast du entschieden, und was ist daraus geworden? | Qu'as-tu décidé, et qu'en est-il sorti ? |
| 36 | What did you start without planning it? | ¿Qué empezaste sin planearlo? | O que você começou sem planejar? | Was hast du ungeplant angefangen? | Qu'as-tu commencé sans le prévoir ? |
| 37 | Which task would you hand to someone else? | ¿Qué tarea le darías a otra persona? | Qual tarefa você passaria para outra pessoa? | Welche Aufgabe würdest du jemand anderem geben? | Quelle tâche confierais-tu à quelqu'un d'autre ? |
| 38 | What did you put off that turned out fine? | ¿Qué aplazaste que al final salió bien? | O que você adiou e no fim deu certo? | Was hast du aufgeschoben, und es ging trotzdem gut? | Qu'as-tu repoussé qui s'est finalement bien passé ? |
| 39 | What is missing from these pages? | ¿Qué falta en estas páginas? | O que falta nestas páginas? | Was fehlt auf diesen Seiten? | Que manque-t-il dans ces pages ? |
| 40 | What would you explore on a free afternoon? | ¿Qué explorarías en una tarde libre? | O que você exploraria numa tarde livre? | Was würdest du an einem freien Nachmittag erkunden? | Qu'explorerais-tu pendant un après-midi libre ? |
| 41 | Which priority changed along the way? | ¿Qué prioridad cambió por el camino? | Que prioridade mudou pelo caminho? | Welche Priorität hat sich unterwegs geändert? | Quelle priorité a changé en route ? |
| 42 | What did you fix, at home or elsewhere? | ¿Qué arreglaste, en casa o fuera? | O que você consertou, em casa ou fora? | Was hast du repariert, zu Hause oder anderswo? | Qu'as-tu réparé, chez toi ou ailleurs ? |
| 43 | Which day would you read again first? | ¿Qué día releerías primero? | Que dia você releria primeiro? | Welchen Tag würdest du zuerst nachlesen? | Quel jour relirais-tu en premier ? |
| 44 | What did you buy that was worth it? | ¿Qué compraste que mereció la pena? | O que você comprou que valeu a pena? | Was hast du gekauft, das sich gelohnt hat? | Qu'as-tu acheté qui en valait la peine ? |
| 45 | What did you cook or eat that is worth remembering? | ¿Qué cocinaste o comiste que valga la pena recordar? | O que você cozinhou ou comeu que vale lembrar? | Was hast du gekocht oder gegessen, das sich zu merken lohnt? | Qu'as-tu cuisiné ou mangé qui vaut la peine d'être retenu ? |
| 46 | Which habit showed up without you noticing? | ¿Qué costumbre apareció sin que te dieras cuenta? | Que costume apareceu sem você perceber? | Welche Gewohnheit ist unbemerkt aufgetaucht? | Quelle habitude est apparue sans que tu t'en rendes compte ? |
| 47 | What would make the coming days simpler? | ¿Qué haría más sencillos los próximos días? | O que deixaria os próximos dias mais simples? | Was würde die nächsten Tage einfacher machen? | Qu'est-ce qui rendrait les prochains jours plus simples ? |
| 48 | What did you say no to, and how did it go? | ¿A qué dijiste que no, y cómo fue? | Para o que você disse não, e como foi? | Wozu hast du Nein gesagt, und wie war es? | À quoi as-tu dit non, et comment ça s'est passé ? |
| 49 | Which task could you do in five minutes? | ¿Qué tarea podrías hacer en cinco minutos? | Qual tarefa você poderia fazer em cinco minutos? | Welche Aufgabe könntest du in fünf Minuten erledigen? | Quelle tâche pourrais-tu faire en cinq minutes ? |
| 50 | What surprised you about how you spent your time? | ¿Qué te sorprendió de cómo pasaste el tiempo? | O que te surpreendeu em como você passou o tempo? | Was hat dich daran überrascht, wie du deine Zeit verbracht hast? | Qu'est-ce qui te surprend dans ta façon de passer le temps ? |
| 51 | What do you want to remember from these days in a year? | ¿Qué quieres recordar de estos días dentro de un año? | O que você quer lembrar destes dias daqui a um ano? | Woran willst du dich in einem Jahr von diesen Tagen erinnern? | De quoi veux-tu te souvenir de ces jours dans un an ? |
| 52 | What was harder than it looked? | ¿Qué fue más difícil de lo que parecía? | O que foi mais difícil do que parecia? | Was war schwieriger, als es aussah? | Qu'est-ce qui a été plus difficile que prévu ? |
| 53 | Where did you go for the first time? | ¿Adónde fuiste por primera vez? | Aonde você foi pela primeira vez? | Wo warst du zum ersten Mal? | Quel endroit as-tu découvert ? |
| 54 | What did you make with your hands? | ¿Qué hiciste con las manos? | O que você fez com as mãos? | Was hast du mit den Händen gemacht? | Qu'as-tu fait de tes mains ? |
| 55 | Which plan changed, and what replaced it? | ¿Qué plan cambió, y qué lo sustituyó? | Que plano mudou, e o que o substituiu? | Welcher Plan hat sich geändert, und was kam stattdessen? | Quel plan a changé, et qu'est-ce qui l'a remplacé ? |
| 56 | Which piece of news marked these days? | ¿Qué noticia marcó estos días? | Que notícia marcou estes dias? | Welche Nachricht hat diese Tage geprägt? | Quelle nouvelle a marqué ces jours ? |
| 57 | What can you set aside? | ¿Qué puedes dejar de lado? | O que você pode deixar de lado? | Was kannst du beiseitelegen? | Que peux-tu mettre de côté ? |
| 58 | What were you waiting for, and did it arrive? | ¿Qué esperabas, y llegó? | O que você esperava, e chegou? | Worauf hast du gewartet, und ist es gekommen? | Qu'attendais-tu, et est-ce arrivé ? |
| 59 | What would you write on the first line of the next page? | ¿Qué escribirías en la primera línea de la página siguiente? | O que você escreveria na primeira linha da próxima página? | Was würdest du in die erste Zeile der nächsten Seite schreiben? | Qu'écrirais-tu sur la première ligne de la page suivante ? |
| 60 | How would you sum up these days in one line? | ¿Cómo resumirías estos días en una línea? | Como você resumiria estes dias em uma linha? | Wie würdest du diese Tage in einer Zeile zusammenfassen? | Comment résumerais-tu ces jours en une ligne ? |

---

## 24. v1.2

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `syncRow` | | Sync | Sincronizar | Sincronizar | Synchronisieren | Synchroniser |
| `syncOff` | | Off | Apagada | Desativada | Aus | Désactivée |
| `syncICloud` | | With iCloud Drive | Con iCloud Drive | Com o iCloud Drive | Mit iCloud Drive | Avec iCloud Drive |
| `syncFile` | nombre | With bobbin-sync.json, in the folder you chose | Con bobbin-sync.json, en la carpeta que elegiste | Com bobbin-sync.json, na pasta que você escolheu | Mit bobbin-sync.json im Ordner, den du gewählt hast | Avec bobbin-sync.json, dans le dossier que tu as choisi |
| `syncLast` | hora | Last time, 18:40 | Última vez, 18:40 | Última vez, 18:40 | Zuletzt um 18:40 | Dernière fois, 18:40 |
| `syncConflictTitle` | | Both sides have changed | Los dos lados han cambiado | Os dois lados mudaram | Beide Seiten haben sich geändert | Les deux côtés ont changé |
| `syncConflictText` | | This phone and the other one have different changes since last time. Choose which one to keep, or merge them after seeing a summary. | Este teléfono y el otro tienen cambios distintos desde la última vez. Elige con cuál quedarte, o júntalos tras ver un resumen. | Este telefone e o outro têm alterações diferentes desde a última vez. Escolha qual manter, ou junte os dois depois de ver um resumo. | Dieses Handy und das andere haben seit dem letzten Mal unterschiedliche Änderungen. Wähle, welches du behältst, oder führe sie nach einer Übersicht zusammen. | Ce téléphone et l'autre ont des modifications différentes depuis la dernière fois. Choisis lequel garder, ou fusionne-les après avoir vu un résumé. |
| `syncKeepThis`* | | Keep this phone | Usar este teléfono | Manter este telefone | Dieses Handy behalten | Garder ce téléphone |
| `syncKeepOther`* | | Keep the other | Usar el otro | Manter o outro | Das andere behalten | Garder l'autre |
| `syncMerge`* | | Merge both | Juntar los dos | Juntar os dois | Beide zusammenführen | Fusionner les deux |
| `proSync` | | Sync through your own cloud | Sincronizar con tu propia nube | Sincronizar pela sua própria nuvem | Synchronisieren über deine eigene Cloud | Synchroniser via ton propre cloud |
| `proYearSummary` | | The year summary | El resumen del año | O resumo do ano | Der Jahresrückblick | Le bilan de l'année |
| `yearSummaryRow` | | Year summary | Resumen del año | Resumo do ano | Jahresrückblick | Bilan de l'année |
| `yearSummaryTitle` | año | Your 2026 | Tu 2026 | Seu 2026 | Dein 2026 | Ton 2026 |
| `summaryInspirations` | | Inspirations | Inspiraciones | Inspirações | Inspirationen | Inspirations |
| `summaryPriorities` | | Priorities done | Prioridades hechas | Prioridades feitas | Erledigte Prioritäten | Priorités faites |
| `summaryMostMigrated` | | Travelled the most | Las que más viajaron | As que mais viajaram | Am weitesten gereist | Les plus voyageuses |
| `summaryEmpty` | | This year you marked no inspirations or priorities. Next year starts blank. | Este año no marcaste inspiraciones ni prioridades. El que viene está en blanco. | Este ano você não marcou inspirações nem prioridades. O próximo está em branco. | Dieses Jahr hast du keine Inspirationen oder Prioritäten markiert. Das nächste ist noch leer. | Cette année, tu n'as marqué ni inspirations ni priorités. La prochaine est vierge. |
| `iconAndTheme` | | Icon and theme | Icono y tema | Ícone e tema | Symbol und Thema | Icône et thème |
| `iconNone` | | No icon | Sin icono | Sem ícone | Kein Symbol | Sans icône |
| `themeHint` | | Theme | Tema | Tema | Thema | Thème |
| `themeNone` | | No theme | Sin tema | Sem tema | Ohne Thema | Sans thème |
| `iconName` | id | Star, Heart, Book, Home, Work, Travel, Money, Health, Food, Music, Idea, People | Estrella, Corazón, Libro, Casa, Trabajo, Viaje, Dinero, Salud, Comida, Música, Idea, Personas | Estrela, Coração, Livro, Casa, Trabalho, Viagem, Dinheiro, Saúde, Comida, Música, Ideia, Pessoas | Stern, Herz, Buch, Zuhause, Arbeit, Reise, Geld, Gesundheit, Essen, Musik, Idee, Menschen | Étoile, Coeur, Livre, Maison, Travail, Voyage, Argent, Santé, Cuisine, Musique, Idée, Personnes |
| `siblingFrom` | app, fecha | Purl, March 14, 2025 | Purl, 14 de marzo de 2025 | Purl, 14 de março de 2025 | Purl, 14. März 2025 | Purl, 14 mars 2025 |
| `importToDay` | | Import to that day | Importar a ese día | Importar para esse dia | An diesem Tag importieren | Importer à ce jour |
| `importElsewhere` | | To another day or collection | A otro día o colección | Para outro dia ou coleção | An einen anderen Tag oder in eine Sammlung | Vers un autre jour ou une collection |
| `importSkip` | | Skip | Saltar | Pular | Überspringen | Passer |
| `importSiblingDone` | n | 15 entries imported. / 1 entry imported. | 15 entradas importadas. / 1 entrada importada. | 15 entradas importadas. / 1 entrada importada. | 15 Einträge importiert. / 1 Eintrag importiert. | 15 entrées importées. / 1 entrée importée. |
| `importSiblingDone` | 0 | Nothing was imported. | No se ha importado nada. | Nada foi importado. | Nichts wurde importiert. | Rien n'a été importé. |
| `importNotSibling` | | That file is not a backup from Bobbin, Purl, MoodTraker or Quilt. | Ese fichero no es una copia de Bobbin, Purl, MoodTraker ni Quilt. | Esse arquivo não é uma cópia do Bobbin, Purl, MoodTraker nem Quilt. | Diese Datei ist keine Sicherung von Bobbin, Purl, MoodTraker oder Quilt. | Ce fichier n'est pas une copie de Bobbin, Purl, MoodTraker ou Quilt. |

- `iconName` va en el orden de `INDEX_ICONS` (`star`, `heart`, `book`, `home`, `work`, `travel`,
  `money`, `health`, `food`, `music`, `idea`, `people`) y es la descripción de cada icono para el lector
  de pantalla.
- `siblingFrom` es el nombre de la app y `dayMonthYear`; como `Eyebrow`, sale en mayúsculas.
