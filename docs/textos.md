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
   artículo. Los textos que los nombran (`keyPrefixes`) los escriben tal cual, sin
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
| Lista / Listas (colección de notas) | List / Lists | Lista / Listas | Lista / Listas | Liste / Listen | Liste / Listes |
| Seguimiento (colección `TRACKER`) | Tracker | Seguimiento | Tracker | Tracker | Suivi |
| Pasar (migrar: a hoy, a mañana, a otro día o a una lista) | Move | Pasar | Passar | Verschieben | Déplacer |
| Llevar a otro mes (programar en Futuro) | Move to another month | Llevar a otro mes | Levar para outro mês | In einen anderen Monat | Reporter à un autre mois |
| Descartar | Discard | Descartar | Descartar | Verwerfen | Écarter |
| Revisar | Review | Revisar | Revisar | Durchsehen | Revoir |
| Reabrir | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| Releer (paso de reflexión) | Read again | Releer | Reler | Nachlesen | Relire |
| Entrada | Entry | Entrada | Entrada | Eintrag | Entrée |
| Tarea / Evento / Nota | Task / Event / Note | Tarea / Evento / Nota | Tarefa / Evento / Nota | Aufgabe / Ereignis / Notiz | Tâche / Événement / Note |
| Abierta / Hecha | Open / Done | Abierta / Hecha | Aberta / Feita | Offen / Erledigt | Ouverte / Faite |
| Pasada / Llevada a otro mes / Descartada | Moved / Moved to another month / Discarded | Pasada / Llevada a otro mes / Descartada | Passada / Levada para outro mês / Descartada | Verschoben / In einen anderen Monat verschoben / Verworfen | Déplacée / Reportée à un autre mois / Écartée |
| Migrada / Programada (solo en la Clave, como nombre del método) | Migrated / Scheduled | Migrada / Programada | Migrada / Agendada | Migriert / Eingeplant | Migrée / Planifiée |
| Prioridad / Inspiración / Explorar | Priority / Inspiration / Explore | Prioridad / Inspiración / Explorar | Prioridade / Inspiração / Explorar | Priorität / Inspiration / Erkunden | Priorité / Inspiration / Explorer |
| Clave (la *key page*) | Key | Clave | Legenda | Legende | Légende |
| Calendario (del mes) | Calendar | Calendario | Calendário | Kalender | Calendrier |
| Tareas del mes | Tasks of the month | Tareas del mes | Tarefas do mês | Aufgaben des Monats | Tâches du mois |
| Diario (todo lo escrito) | journal | diario | diário | Journal | journal |
| Cuaderno | Notebook | Cuaderno | Caderno | Notizbuch | Carnet |
| Portada / Papel | Cover / Paper | Portada / Papel | Capa / Papel | Umschlag / Papier | Couverture / Papier |
| Símbolo (un glifo del método) | symbol | símbolo | símbolo | Symbol | symbole |

Sin traducir en ningún idioma, como se busca el método: `bullet journal`, `bujo`, `bullet`,
`signifier`, `rapid logging`, `Daily Log`, `Monthly Log` y `Future Log`. En la app no aparece ninguno:
las pestañas dicen Hoy, Mes y Futuro, la Clave agrupa por lo que apuntas, lo que pasó con una tarea y
el margen, y dice entre paréntesis el nombre del método de cada estado ("En el método: migrada"). La
ficha explica que las pestañas son el Daily Log, el Monthly Log y el Future Log.

La interfaz habla con verbos de todos los días (pasar, llevar, descartar) y enseña la consecuencia de
cada acción antes de tocarla; los nombres del método (migrar, programar) viven en la Clave y en la
ficha. Una colección de notas se llama lista, y un seguimiento, seguimiento. La ficha de tienda, que explica el
método a quien lo busca por su nombre, puede llamarlas colecciones (Collections), como el método, igual
que dice Daily Log.

## Forma en el código

- Texto sin parámetros: `val undo = t("...", "...", "...", "...", "...")`.
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
| `add`* | | Add | Añadir | Adicionar | Hinzufügen | Ajouter |
| `save`* | | Save | Guardar | Salvar | Speichern | Enregistrer |
| `gotIt`* | | Got it | Entendido | Entendi | Verstanden | Compris |
| `previous`* | | Back | Atrás | Voltar | Zurück | Retour |
| `next`* | | Next | Siguiente | Próximo | Weiter | Suivant |

`taskCount(n)` y `entryCount(n)` (decisión de #16, sin fila propia porque no tienen texto fijo: son
"$n " mas la palabra de la fila 2 de la tabla de la regla del metodo 3, tarea o entrada) dan el
conteo generico ("3 tareas", "1 entrada") para donde haga falta uno suelto, fuera de una pantalla
concreta.

## 3. Navegación y cabeceras

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `tabToday` | | Today | Hoy | Hoje | Heute | Aujourd'hui |
| `tabMonth` | | Month | Mes | Mês | Monat | Mois |
| `tabFuture` | | Future | Futuro | Futuro | Zukunft | Futur |
| `tabIndex` | | Index | Índice | Índice | Index | Index |
| `backToToday`* | | Back to today | Volver a hoy | Voltar para hoje | Zurück zu heute | Revenir à aujourd'hui |
| `daySubtitle` | hoy | Today, September 2026 | Hoy, septiembre de 2026 | Hoje, setembro de 2026 | Heute, September 2026 | Aujourd'hui, septembre 2026 |
| `daySubtitle` | ayer | Yesterday, September 2026 | Ayer, septiembre de 2026 | Ontem, setembro de 2026 | Gestern, September 2026 | Hier, septembre 2026 |
| `daySubtitle` | mañana | Tomorrow, September 2026 | Mañana, septiembre de 2026 | Amanhã, setembro de 2026 | Morgen, September 2026 | Demain, septembre 2026 |
| `daySubtitle` | otro | September 2026 | Septiembre de 2026 | Setembro de 2026 | September 2026 | Septembre 2026 |
| `monthSubtitle` | actual | 2026 | 2026 | 2026 | 2026 | 2026 |
| `monthSubtitle` | pasado | 2026, a past month | 2026, un mes pasado | 2026, um mês passado | 2026, ein vergangener Monat | 2026, un mois passé |
| `backToMonth`* | mes | Back to September | Volver a septiembre | Voltar para setembro | Zurück zum September | Revenir à septembre |
| `a11yPreviousDay` | | Previous day | Día anterior | Dia anterior | Vorheriger Tag | Jour précédent |
| `a11yNextDay` | | Next day | Día siguiente | Próximo dia | Nächster Tag | Jour suivant |
| `a11yPreviousMonth` | | Previous month | Mes anterior | Mês anterior | Vorheriger Monat | Mois précédent |
| `a11yNextMonth` | | Next month | Mes siguiente | Próximo mês | Nächster Monat | Mois suivant |
| `a11yClose` | | Close | Cerrar | Fechar | Schließen | Fermer |
| `a11ySettings` | | Settings | Ajustes | Ajustes | Einstellungen | Réglages |
| `a11ySearch` | | Search the journal | Buscar en el diario | Buscar no diário | Im Journal suchen | Chercher dans le journal |
| `a11yMoreActions` | | More actions | Más acciones | Mais ações | Weitere Aktionen | Plus d'actions |
| `a11ySelected` | | selected | elegida | selecionada | ausgewählt | sélectionnée |

Los títulos de página de Futuro y del Índice son `tabFuture` y `tabIndex`; el de la Clave, `keyTitle`
(sección 12); el de Ajustes, `settingsTitle` (sección 13). `daySubtitle` dice qué día es respecto a
hoy (hoy, ayer, mañana) antes del mes; `monthSubtitle` avisa de que un mes ya pasó.

## 4. La entrada, la barra de escribir y la hoja

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
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
| `timesMoved` | n | You've moved it 3 times / You've moved it once | Ya la has pasado 3 veces / Ya la has pasado una vez | Você já a passou 3 vezes / Você já a passou uma vez | Schon 3-mal verschoben / Schon einmal verschoben | Déjà déplacée 3 fois / Déjà déplacée une fois |
| `actionDone` | | Done | Hecha | Feita | Erledigt | Faite |
| `actionDiscard` | | Discard | Descartar | Descartar | Verwerfen | Écarter |
| `actionReopen` | | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| `actionRecover` | | Bring back | Recuperar | Recuperar | Zurückholen | Récupérer |
| `actionGoToCopy` | | Go to the copy | Ir a la copia | Ir para a cópia | Zur Kopie | Aller à la copie |
| `actionEdit` | | Edit the text | Editar el texto | Editar o texto | Text bearbeiten | Modifier le texte |
| `actionDelete` | | Delete | Borrar | Apagar | Löschen | Supprimer |
| `moveToday` | | Move to today | Pasar a hoy | Passar para hoje | Auf heute verschieben | Passer à aujourd'hui |
| `moveTomorrow` | | Move to tomorrow | Pasar a mañana | Passar para amanhã | Auf morgen verschieben | Passer à demain |
| `moveNextDay` | | Move to the next day | Pasar al día siguiente | Passar para o dia seguinte | Auf den nächsten Tag | Passer au jour suivant |
| `moveOtherMonth` | | Move to another month | Llevar a otro mes | Levar para outro mês | In einen anderen Monat | Reporter à un autre mois |
| `moveElsewhere` | | Move somewhere else | Pasar a otro sitio | Passar para outro lugar | Woandershin verschieben | Déplacer ailleurs |
| `priorityOn` | | Mark as priority | Marcar como prioridad | Marcar como prioridade | Als Priorität markieren | Marquer comme priorité |
| `priorityOff` | | Remove priority | Quitar prioridad | Tirar prioridade | Priorität entfernen | Retirer la priorité |
| `otherMarks` | | Other margin marks | Otras marcas del margen | Outras marcas da margem | Weitere Randzeichen | Autres marques de la marge |
| `backToOptions` | | Back to the options | Volver a las opciones | Voltar às opções | Zurück zu den Optionen | Retour aux options |
| `doneHow` | | Stays where it is, marked with an x. | Se queda en su sitio, marcada con una x. | Fica onde está, marcada com um x. | Bleibt, wo sie ist, mit einem x markiert. | Reste à sa place, marquée d'un x. |
| `otherMonthHow` | | You pick the month and it waits in Future. | Eliges el mes y espera en Futuro. | Você escolhe o mês e ela espera no Futuro. | Du wählst den Monat, dort wartet sie in Zukunft. | Tu choisis le mois et elle attend dans Futur. |
| `discardHow` | | No longer needed. It stays crossed out. | Ya no hace falta. Se queda tachada. | Não é mais necessária. Fica riscada. | Nicht mehr nötig. Bleibt durchgestrichen. | Plus nécessaire. Elle reste barrée. |
| `reopenHow` | | It's open again. | Vuelve a estar pendiente. | Volta a ficar pendente. | Ist wieder offen. | Elle est de nouveau à faire. |
| `priorityOnHow` | | An asterisk in the margin. | Un asterisco en el margen. | Um asterisco na margem. | Ein Sternchen am Rand. | Un astérisque dans la marge. |
| `priorityOffHow` | | The asterisk leaves the margin. | Se va el asterisco del margen. | O asterisco sai da margem. | Das Sternchen verschwindet vom Rand. | L'astérisque quitte la marge. |
| `otherMarksHow` | | Inspiration or explore. | Inspiración o explorar. | Inspiração ou explorar. | Inspiration oder Erkunden. | Inspiration ou explorer. |
| `elsewhereHow` | | A day this month, the month's tasks or a list. | Un día de este mes, las tareas del mes o una lista. | Um dia deste mês, as tarefas do mês ou uma lista. | Ein Tag in diesem Monat, die Aufgaben des Monats oder eine Liste. | Un jour de ce mois, les tâches du mois ou une liste. |
| `deleteHow` | | It leaves the journal. You can undo it for a few seconds. | Desaparece del diario. Puedes deshacerlo unos segundos. | Sai do diário. Dá para desfazer por alguns segundos. | Verschwindet aus dem Journal. Ein paar Sekunden lang rückgängig zu machen. | Elle quitte le journal. Tu peux annuler pendant quelques secondes. |
| `leavesMark` | marca, mes? | A > stays in August. / A > stays here. | En agosto queda una >. / Aquí queda una >. | Em agosto fica um >. / Aqui fica um >. | Im August bleibt ein >. / Hier bleibt ein >. | En août reste un >. / Ici reste un >. |
| `waitsIn` | mes | Waits in Future, in October. | Espera en Futuro, en octubre. | Espera no Futuro, em outubro. | Wartet in Zukunft, im Oktober. | Attend dans Futur, en octobre. |
| `placeLabel` | Daily(hoy) | today, Wednesday 23 | hoy, miércoles 23 | hoje, quarta-feira, 23 | heute, Mittwoch, 23. | aujourd'hui, mercredi 23 |
| `placeLabel` | Daily(mañana) | tomorrow, Thursday 24 | mañana, jueves 24 | amanhã, quinta-feira, 24 | morgen, Donnerstag, 24. | demain, jeudi 24 |
| `placeLabel` | Daily(ayer) | yesterday, Tuesday 22 | ayer, martes 22 | ontem, terça-feira, 22 | gestern, Dienstag, 22. | hier, mardi 22 |
| `placeLabel` | Daily(otro) | Monday 28 | el lunes 28 | segunda-feira, 28 | Montag, 28. | lundi 28 |
| `placeLabel` | Monthly(mes) | September's tasks | las tareas de septiembre | as tarefas de setembro | Aufgaben im September | les tâches de septembre |
| `placeLabel` | Monthly(mes, día) | September 30 | el 30 de septiembre | 30 de setembro | 30. September | le 30 septembre |
| `placeLabel` | Future(mes) | October, in Future | octubre, en Futuro | outubro, no Futuro | Oktober, in Zukunft | octobre, dans Futur |
| `placeLabel` | Future(mes, día) | October 14, in Future | el 14 de octubre, en Futuro | 14 de outubro, no Futuro | 14. Oktober, in Zukunft | le 14 octobre, dans Futur |
| `placeLabel` | InCollection | the list Lecturas | la lista Lecturas | a lista Lecturas | Liste Lecturas | la liste Lecturas |
| `movedTo` | lugar | Moved to tomorrow, Thursday 24 | Pasada a mañana, jueves 24 | Passada para amanhã, quinta-feira, 24 | Verschoben: morgen, Donnerstag, 24. | Déplacée vers demain, jeudi 24 |
| `scheduledTo` | lugar | Moved to October, in Future | Llevada a octubre, en Futuro | Levada para outubro, no Futuro | Verschoben: Oktober, in Zukunft | Reportée vers octobre, dans Futur |
| `cameFrom` | lugar | Came from yesterday, Tuesday 22 | Viene de ayer, martes 22 | Origem: ontem, terça-feira, 22 | Kommt von: gestern, Dienstag, 22. | Origine : hier, mardi 22 |
| `copiesTo` | lugar | A copy goes to tomorrow, Thursday 24. | Se copia a mañana, jueves 24. | Uma cópia vai para amanhã, quinta-feira, 24. | Kopie: morgen, Donnerstag, 24. | Une copie va vers demain, jeudi 24. |
| `copyIsAt` | lugar | Copy: tomorrow, Thursday 24. | Está en mañana, jueves 24. | Cópia: amanhã, quinta-feira, 24. | Kopie: morgen, Donnerstag, 24. | Copie : demain, jeudi 24. |
| `composeHint` | tarea | New task... | Nueva tarea... | Nova tarefa... | Neue Aufgabe... | Nouvelle tâche... |
| `composeHint` | evento | New event... | Nuevo evento... | Novo evento... | Neues Ereignis... | Nouvel événement... |
| `composeHint` | nota | New note... | Nueva nota... | Nova nota... | Neue Notiz... | Nouvelle note... |
| `composeFor` | | For | En | Para | Für | Pour |
| `targetLabel` | Daily(hoy) | today | hoy | hoje | heute | aujourd'hui |
| `targetLabel` | Monthly(mes) | September's tasks | tareas de septiembre | tarefas de setembro | Aufgaben im September | tâches de septembre |
| `targetLabel` | Monthly(mes, día) | September 30 | el 30 de septiembre | 30 de setembro | 30. September | le 30 septembre |
| `targetLabel` | Future(mes) | October | octubre | outubro | Oktober | octobre |
| `targetLabel` | InCollection | Lecturas | Lecturas | Lecturas | Lecturas | Lecturas |
| `added` | bullet, lugar | Task added for today. / Event added for September 30. | Tarea añadida en hoy. / Evento añadido en el 30 de septiembre. | Tarefa adicionada para hoje. / Evento adicionado para 30 de setembro. | Aufgabe hinzugefügt: heute. / Ereignis hinzugefügt: 30. September. | Tâche ajoutée pour aujourd'hui. / Événement ajouté pour le 30 septembre. |
| `toastDone` | | Done. | Hecha. | Feita. | Erledigt. | Faite. |
| `toastDiscarded` | | Discarded. | Descartada. | Descartada. | Verworfen. | Écartée. |
| `toastPriorityOn` | | Marked as priority. | Marcada como prioridad. | Marcada como prioridade. | Als Priorität markiert. | Marquée comme priorité. |
| `toastPriorityOff` | | No longer a priority. | Sin prioridad. | Sem prioridade. | Keine Priorität mehr. | Plus prioritaire. |
| `toastSaved` | | Saved. | Guardado. | Salvo. | Gespeichert. | Enregistré. |
| `toastScheduled` | mes | Moved to October. It waits in Future. | Llevada a octubre. Espera en Futuro. | Levada para outubro. Espera no Futuro. | Verschoben: Oktober. Wartet in Zukunft. | Reportée en octobre. Elle attend dans Futur. |
| `entryKind` | tarea abierta | Open task | Tarea pendiente | Tarefa pendente | Offene Aufgabe | Tâche à faire |
| `entryKind` | tarea hecha | Done task | Tarea hecha | Tarefa feita | Erledigte Aufgabe | Tâche faite |
| `entryKind` | evento con prioridad | Evento, priority | Evento, prioridad | Evento, prioridade | Evento, Priorität | Evento, priorité |
| `toToday` | | Today | Hoy | Hoje | Heute | Aujourd'hui |
| `toTomorrow` | | Tomorrow | Mañana | Amanhã | Morgen | Demain |
| `toThisMonth` | | This month's tasks | Tareas de este mes | Tarefas deste mês | Aufgaben dieses Monats | Tâches de ce mois |
| `toDayOfMonth` | | A day this month | Un día de este mes | Um dia deste mês | Ein Tag in diesem Monat | Un jour de ce mois |
| `toCollection` | | To a list | A una lista | Para uma lista | In eine Liste | Vers une liste |
| `dayField` | | day | día | dia | Tag | jour |
| `moveAction`* | | Move | Pasar | Passar | Verschieben | Déplacer |
| `dayOutOfRange` | mes, n | September has no day 31. | Septiembre no tiene día 31. | Setembro não tem dia 31. | Der September hat keinen 31. Tag. | Septembre n'a pas de jour 31. |
| `dayPast` | | That day has passed. | Ese día ya pasó. | Esse dia já passou. | Dieser Tag ist vorbei. | Ce jour est passé. |
| `newList` | | New list | Nueva lista | Nova lista | Neue Liste | Nouvelle liste |
| `showMoreMonths` | | Show more months | Ver más meses | Ver mais meses | Mehr Monate zeigen | Voir plus de mois |
| `entryDeleted` | | Entry deleted. | Entrada borrada. | Entrada apagada. | Eintrag gelöscht. | Entrée supprimée. |
| `collectionDeleted` | | List deleted. | Lista borrada. | Lista apagada. | Liste gelöscht. | Liste supprimée. |
| `rowDeleted` | | Row deleted. | Fila borrada. | Linha apagada. | Zeile gelöscht. | Ligne supprimée. |

- **Verbos de la interfaz.** La app dice lo que pasa con palabras de todos los días y deja los nombres
  del método para la Clave: "Pasar a hoy" y no Migrar, "Llevar a otro mes" y no Programar. Una tarea
  migrada se llama "Pasada" (`movedName`); una programada, "Llevada a otro mes" (`otherMonthName`). El
  símbolo es el mismo, y la Clave dice su nombre en el método.
- **Cada acción dice su consecuencia** (`*How`, `leavesMark`, `copiesTo`, `waitsIn`): qué queda en la
  página y a dónde va la copia, antes de tocarla.
- **El aviso de abajo** (`toast*`, `added`, `movedTo`) repite el nombre de la acción y lleva `undo`
  cuando se puede deshacer. `sentence` evita el doble punto cuando el lugar ya acaba en punto (alemán:
  "24.").
- `placeLabel` nombra un lugar dentro de una frase (hoy, mañana, el lunes 28, las tareas de
  septiembre, la lista Lecturas); `targetLabel`, el mismo lugar en la etiqueta corta "En ..." de la
  barra de escribir. En español se contrae con `a` y `de` ("al 30 de septiembre").
- `wentToDay`, `wentToMonth` y `wentToFuture` quedan para Buscar y para compartir: `wentToFuture` con
  día usa `abbrDate`, y `abbrDateWithYear` si el mes no es del año en curso; sin día, "Futuro, " y
  `monthYear`. `wentToFuture(month, day, withYear)` recibe ese booleano ya calculado por quien la
  llama, porque `Strings.kt` no lleva reloj.
- `timesMoved` solo se enseña desde `MIGRATION_SHOWN_FROM` (2).
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
| `calendarToday` | | On the calendar | En el calendario | No calendário | Im Kalender | Au calendrier |
| `noticeCorrupt` | | Couldn't read the journal. The files were set aside and nothing was deleted. | No se ha podido leer el diario. Los ficheros se han guardado aparte y no se ha borrado nada. | Não foi possível ler o diário. Os arquivos foram guardados à parte e nada foi apagado. | Das Journal konnte nicht gelesen werden. Die Dateien wurden beiseitegelegt, gelöscht wurde nichts. | Impossible de lire le journal. Les fichiers ont été mis de côté et rien n'a été supprimé. |
| `noticeSaveFailed` | | Couldn't save. I'll try again with your next change. | No se ha podido guardar. Lo intento otra vez con tu próximo cambio. | Não foi possível salvar. Vou tentar de novo na sua próxima alteração. | Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut. | Impossible d'enregistrer. Je réessaierai avec ta prochaine modification. |
| `offerReminder` | hora | Remind you to go over the day at 21:00? | ¿Te aviso para repasar el día a las 21:00? | Quer que eu avise para repassar o dia às 21:00? | Soll ich dich um 21:00 erinnern, den Tag durchzugehen? | Je te rappelle de relire ta journée à 21:00 ? |
| `unclosedMonth` | mes, n | August still has 4 open tasks / August still has 1 open task | Agosto tiene 4 tareas sin cerrar / Agosto tiene 1 tarea sin cerrar | Agosto tem 4 tarefas sem fechar / Agosto tem 1 tarefa sem fechar | Im August sind 4 Aufgaben offen / Im August ist 1 Aufgabe offen | Août a 4 tâches non clôturées / Août a 1 tâche non clôturée |
| `unclosedBody` | | Decide what happens to each one: done, to today, to another month or discarded. Nothing moves unless you say so. | Decide qué pasa con cada una: hecha, a hoy, a otro mes o descartada. Nada se mueve si no lo dices tú. | Decida o que acontece com cada uma: feita, para hoje, para outro mês ou descartada. Nada se move se você não disser. | Entscheide, was mit jeder passiert: erledigt, auf heute, in einen anderen Monat oder verworfen. Nichts bewegt sich, wenn du es nicht sagst. | Décide ce que devient chacune : faite, à aujourd'hui, à un autre mois ou écartée. Rien ne bouge si tu ne le dis pas. |
| `reviewMonth`* | mes | Review August | Repasar agosto | Revisar agosto | August durchsehen | Revoir août |
| `earlierOpen` | n | 3 tasks still open from earlier days / 1 task still open from earlier days | Quedan 3 tareas abiertas de días anteriores / Queda 1 tarea abierta de días anteriores | Restam 3 tarefas abertas de dias anteriores / Resta 1 tarefa aberta de dias anteriores | Noch 3 Aufgaben offen von früheren Tagen / Noch 1 Aufgabe offen von früheren Tagen | Il reste 3 tâches ouvertes des jours précédents / Il reste 1 tâche ouverte des jours précédents |
| `earlierBody` | | They're from days of this month that have passed. Decide what happens to each one. | Son de días de este mes que ya pasaron. Decide qué pasa con cada una. | São de dias deste mês que já passaram. Decida o que acontece com cada uma. | Sie sind von vergangenen Tagen dieses Monats. Entscheide, was mit jeder passiert. | Elles viennent de jours passés de ce mois. Décide ce que devient chacune. |
| `reviewEarlier`* | | Review those days | Repasar esos días | Revisar esses dias | Diese Tage durchsehen | Revoir ces jours |
| `reviewDay`* | | Review the day | Repasar el día | Revisar o dia | Den Tag durchsehen | Revoir la journée |
| `hintTap` | | ^Tap the dot^ of a task to mark it done. ^Tap the text^ to see everything you can do with it. | ^Toca el punto^ de una tarea para marcarla hecha. ^Toca el texto^ para ver todo lo que puedes hacer con ella. | ^Toque no ponto^ de uma tarefa para marcá-la como feita. ^Toque no texto^ para ver tudo o que dá para fazer com ela. | ^Tippe auf den Punkt^ einer Aufgabe, um sie zu erledigen. ^Tippe auf den Text^, um alles zu sehen, was du mit ihr machen kannst. | ^Touche le point^ d'une tâche pour la marquer faite. ^Touche le texte^ pour voir tout ce que tu peux en faire. |
| `dayEmpty` | hoy | Nothing written today. | Nada escrito hoy. | Nada escrito hoje. | Heute steht noch nichts. | Rien d'écrit aujourd'hui. |
| `dayEmpty` | otro día | Nothing written on this day. | Nada escrito en este día. | Nada escrito neste dia. | An diesem Tag steht nichts. | Rien d'écrit ce jour-là. |
| `writeBelow` | | Write below to start. | Escribe abajo para empezar. | Escreva abaixo para começar. | Schreib unten, um anzufangen. | Écris en bas pour commencer. |
| `shareDay` | | Share this day | Compartir este día | Compartilhar este dia | Diesen Tag teilen | Partager ce jour |

El título de Hoy es `dayTitle` y el subtítulo `daySubtitle`. La oferta del recordatorio lleva `notNow` y
`yes`; el aviso de diario dañado, `ok`; la pista de la primera entrada, `gotIt`. Los `^` de `hintTap`
marcan el texto en negrita y no se pintan.

## 6. Mes

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `monthTasks` | | Tasks of the month | Tareas del mes | Tarefas do mês | Aufgaben des Monats | Tâches du mois |
| `calendarTitle` | | Calendar | Calendario | Calendário | Kalender | Calendrier |
| `monthExplain` | | Above, what happens each day. Below, what you want to do this month with no set date. | Arriba, lo que pasa cada día. Abajo, lo que quieres hacer este mes sin fecha fija. | Em cima, o que acontece a cada dia. Embaixo, o que você quer fazer este mês sem data fixa. | Oben, was an jedem Tag passiert. Unten, was du diesen Monat ohne festes Datum tun willst. | En haut, ce qui se passe chaque jour. En bas, ce que tu veux faire ce mois-ci sans date fixe. |
| `calendarHint` | | Tap a day to write on it | Toca un día para apuntar en él | Toque num dia para anotar nele | Tippe auf einen Tag, um dort zu notieren | Touche un jour pour y noter |
| `writingHere` | | Writing here | Aquí se apunta | Anotando aqui | Hier wird notiert | On note ici |
| `monthTasksEmpty` | | None yet. | Ninguna todavía. | Nenhuma ainda. | Noch keine. | Aucune pour l'instant. |
| `shareMonth` | | Share this month | Compartir este mes | Compartilhar este mês | Diesen Monat teilen | Partager ce mois |
| `futureWaiting` | n | 2 Future entries are waiting for this month / 1 Future entry is waiting for this month | 2 entradas de Futuro esperan a este mes / 1 entrada de Futuro espera a este mes | 2 entradas do Futuro esperam este mês / 1 entrada do Futuro espera este mês | 2 Einträge aus Zukunft warten auf diesen Monat / 1 Eintrag aus Zukunft wartet auf diesen Monat | 2 entrées de Futur attendent ce mois-ci / 1 entrée de Futur attend ce mois-ci |
| `futureWaitingBody` | | You wrote them down for this month. Move them to the calendar, leave them or discard them. | Las apuntaste para este mes. Pásalas al calendario, déjalas o descártalas. | Você as anotou para este mês. Passe-as para o calendário, deixe-as ou descarte-as. | Du hast sie für diesen Monat notiert. In den Kalender, lassen oder verwerfen. | Tu les as notées pour ce mois. Passe-les au calendrier, laisse-les ou écarte-les. |
| `reviewFuture`* | | Review Future | Repasar Futuro | Revisar Futuro | Zukunft durchsehen | Revoir Futur |

El título de Mes es `monthName` y el subtítulo `monthSubtitle`. El aviso del mes sin cerrar es
`unclosedMonth` con `unclosedBody` y `reviewMonth` (sección 5).

## 7. Futuro

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `futureSubtitle` | n | The next 6 months | Los próximos 6 meses | Os próximos 6 meses | Die nächsten 6 Monate | Les 6 prochains mois |
| `futureExplain` | | Write down what isn't due yet. When its month comes, you review it and decide: nothing drops into Today on its own. | Apunta lo que aún no toca. Cuando llegue su mes, lo repasas y decides: nada baja solo a Hoy. | Anote o que ainda não é para agora. Quando chegar o mês, você revisa e decide: nada desce sozinho para Hoje. | Notiere, was noch nicht dran ist. Kommt sein Monat, siehst du es durch und entscheidest: nichts rutscht von allein in Heute. | Note ce qui n'est pas encore pour maintenant. Quand son mois arrive, tu le revois et tu décides : rien ne descend seul dans Aujourd'hui. |
| `futureEmpty` | | Nothing yet. | Nada todavía. | Nada ainda. | Noch nichts. | Rien pour l'instant. |
| `onDay` | día | Day 14 | Día 14 | Dia 14 | Tag 14 | Jour 14 |

Título `tabFuture`; cada bloque, `monthTitle`; campo del día `dayField`; error `dayOutOfRange`;
`showMoreMonths`.

## 8. Índice

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `indexSubtitle` | | Your whole journal, in one place | Todo tu diario, en un sitio | Todo o seu diário, num só lugar | Dein ganzes Journal an einem Ort | Tout ton journal, au même endroit |
| `indexMonths` | | Months | Meses | Meses | Monate | Mois |
| `indexLists` | | Lists | Listas | Listas | Listen | Listes |
| `indexListsHint` | | Trips, ideas, books: what doesn't go in a day | Viajes, ideas, libros: lo que no va en un día | Viagens, ideias, livros: o que não cabe num dia | Reisen, Ideen, Bücher: was in keinen Tag gehört | Voyages, idées, livres : ce qui ne va pas dans un jour |
| `indexTrackers` | | Trackers | Seguimientos | Trackers | Tracker | Suivis |
| `indexTrackersHint` | | One row per habit, one box per day | Una fila por hábito, un cuadro por día | Uma linha por hábito, um quadrado por dia | Eine Zeile pro Gewohnheit, ein Kästchen pro Tag | Une ligne par habitude, une case par jour |
| `newTracker` | | New tracker | Nuevo seguimiento | Novo tracker | Neuer Tracker | Nouveau suivi |
| `listNameHint` | | Name of the list | Nombre de la lista | Nome da lista | Name der Liste | Nom de la liste |
| `trackerNameHint` | | Name of the tracker | Nombre del seguimiento | Nome do tracker | Name des Trackers | Nom du suivi |
| `listCreated` | | List created. Write its first line below. | Lista creada. Escribe abajo su primera línea. | Lista criada. Escreva a primeira linha abaixo. | Liste erstellt. Schreib unten die erste Zeile. | Liste créée. Écris sa première ligne en bas. |
| `archivedToggle` | n | Archived (2) | Archivadas (2) | Arquivadas (2) | Archiviert (2) | Archivées (2) |
| `indexEmpty` | | Months show up here as soon as you write in them. | Los meses aparecen aquí en cuanto escribes en ellos. | Os meses aparecem aqui assim que você escreve neles. | Monate erscheinen hier, sobald du in ihnen schreibst. | Les mois apparaissent ici dès que tu y écris. |

La fila de crear una lista es `newList` (sección 4). Cada grupo lleva su pista corta al lado del nombre.

## 9. Lista y seguimiento

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `listSubtitle` | | List | Lista | Lista | Liste | Liste |
| `listEmpty` | | Empty list. Write the first line below. | Lista vacía. Escribe abajo la primera línea. | Lista vazia. Escreva a primeira linha abaixo. | Leere Liste. Schreib unten die erste Zeile. | Liste vide. Écris la première ligne en bas. |
| `shareList` | | Share the list | Compartir la lista | Compartilhar a lista | Liste teilen | Partager la liste |
| `rename`* | | Rename | Cambiar el nombre | Mudar o nome | Umbenennen | Renommer |
| `archive`* | | Archive | Archivar | Arquivar | Archivieren | Archiver |
| `unarchive`* | | Unarchive | Sacar del archivo | Tirar do arquivo | Aus dem Archiv holen | Désarchiver |
| `deleteCollection`* | | Delete | Borrar | Apagar | Löschen | Supprimer |
| `archivedNote` | | Archived. | Archivada. | Arquivada. | Archiviert. | Archivée. |
| `trackerSubtitle` | mes | September tracker | Seguimiento de septiembre | Tracker de setembro | Tracker im September | Suivi de septembre |
| `trackerExplain` | | Tap a box to mark that day; again to clear it. Next month starts blank. | Toca un cuadro para marcar ese día; otra vez para quitarlo. El mes que viene empieza en blanco. | Toque num quadrado para marcar esse dia; de novo para tirar. O mês que vem começa em branco. | Tippe auf ein Kästchen, um den Tag zu markieren, noch einmal, um es zu löschen. Der nächste Monat beginnt leer. | Touche une case pour marquer ce jour ; encore une fois pour l'effacer. Le mois prochain commence vierge. |
| `trackerRowHint` | | New row | Nueva fila | Nova linha | Neue Zeile | Nouvelle ligne |
| `rowDelete` | | Delete row | Borrar fila | Apagar linha | Zeile löschen | Supprimer la ligne |

## 10. Revisar

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `rereadTitle` | mes? | Before deciding, read August again / Before deciding, read these days again | Antes de decidir, relee agosto / Antes de decidir, relee estos días | Antes de decidir, releia agosto / Antes de decidir, releia estes dias | Bevor du entscheidest, lies den August nach / Bevor du entscheidest, lies diese Tage nach | Avant de décider, relis août / Avant de décider, relis ces jours |
| `rereadLead` | n | 4 tasks were left open. Next you'll see them one at a time and decide what happens to each. / 1 task was left open. Next you'll see it and decide what happens to it. / No task was left open. | Quedaron 4 tareas abiertas. Después las verás de una en una y decides qué pasa con cada una. / Quedó 1 tarea abierta. Después la verás y decides qué pasa con ella. / No quedó ninguna tarea abierta. | Ficaram 4 tarefas abertas. Depois você as verá uma a uma e decide o que acontece com cada uma. / Ficou 1 tarefa aberta. Depois você a verá e decide o que acontece com ela. / Nenhuma tarefa ficou aberta. | 4 Aufgaben sind offen geblieben. Danach siehst du sie einzeln und entscheidest, was mit jeder passiert. / 1 Aufgabe ist offen geblieben. Danach siehst du sie und entscheidest, was mit ihr passiert. / Keine Aufgabe ist offen geblieben. | 4 tâches sont restées ouvertes. Ensuite tu les verras une par une et tu décideras ce que devient chacune. / 1 tâche est restée ouverte. Ensuite tu la verras et tu décideras ce qu'elle devient. / Aucune tâche n'est restée ouverte. |
| `rereadNote` | mes? | A note about August (optional) / A note about these days (optional) | Una nota sobre agosto (opcional) / Una nota sobre estos días (opcional) | Uma nota sobre agosto (opcional) / Uma nota sobre estes dias (opcional) | Eine Notiz zum August (optional) / Eine Notiz zu diesen Tagen (optional) | Une note sur août (facultatif) / Une note sur ces jours (facultatif) |
| `rereadNoteHint` | | What went well, what didn't... | Qué salió bien, qué no... | O que deu certo, o que não... | Was gut lief, was nicht... | Ce qui a marché, ce qui non... |
| `decideTasks`* | n | Decide the 4 tasks / Decide the task / Finish | Decidir las 4 tareas / Decidir la tarea / Terminar | Decidir as 4 tarefas / Decidir a tarefa / Terminar | 4 Aufgaben entscheiden / Aufgabe entscheiden / Fertig | Décider les 4 tâches / Décider la tâche / Terminer |
| `taskOf` | i, n | Task 2 of 4 | Tarea 2 de 4 | Tarefa 2 de 4 | Aufgabe 2 von 4 | Tâche 2 sur 4 |
| `entryOf` | i, n | Entry 1 of 3 | Entrada 1 de 3 | Entrada 1 de 3 | Eintrag 1 von 3 | Entrée 1 sur 3 |
| `doneStaysIn` | mes? | Stays in August, marked with an x. / Se queda en su sitio, marcada con una x. | Se queda en agosto, marcada con una x. / Se queda en su sitio, marcada con una x. | Fica em agosto, marcada com um x. / Se queda en su sitio, marcada con una x. | Bleibt im August, mit einem x markiert. / Se queda en su sitio, marcada con una x. | Reste en août, marquée d'un x. / Se queda en su sitio, marcada con una x. |
| `decideLater`* | | Decide later | Decidir luego | Decidir depois | Später entscheiden | Décider plus tard |
| `decideLaterHow` | | It stays open and the notice stays. | Sigue abierta y el aviso no se va. | Continua aberta e o aviso não sai. | Bleibt offen, und der Hinweis bleibt. | Elle reste ouverte et l'avis ne part pas. |
| `reviewFinished` | | Review finished | Repaso terminado | Revisão terminada | Durchsicht beendet | Revue terminée |
| `reviewEndTitle` | mes?, quedan | August, reviewed / August, almost / These days, reviewed | Agosto, repasado / Agosto, casi / Estos días, repasados | Agosto, revisado / Agosto, quase / Estes dias, revisados | August, durchgesehen / August, fast / Diese Tage, durchgesehen | Août, revu / Août, presque / Ces jours, revus |
| `reviewEndLead` | quedan | What you moved to today is already on your list, and what you moved to another month waits in Future. / You left 2 tasks for later. The notice stays on Today until you decide. | Lo que pasaste a hoy ya está en tu lista, y lo que llevaste a otro mes espera en Futuro. / Dejaste 2 tareas para luego. El aviso seguirá en Hoy hasta que decidas. | O que você passou para hoje já está na sua lista, e o que levou para outro mês espera no Futuro. / Você deixou 2 tarefas para depois. O aviso fica em Hoje até você decidir. | Was du auf heute verschoben hast, steht schon auf deiner Liste, und was in einen anderen Monat ging, wartet in Zukunft. / Du hast 2 Aufgaben für später gelassen. Der Hinweis bleibt in Heute, bis du entscheidest. | Ce que tu as passé à aujourd'hui est déjà dans ta liste, et ce que tu as reporté attend dans Futur. / Tu as laissé 2 tâches pour plus tard. L'avis reste dans Aujourd'hui jusqu'à ta décision. |
| `skip` | | Skip | Saltar | Pular | Überspringen | Passer |
| `fromDay` | fecha | From Monday 14 | Del lunes 14 | Da segunda-feira, 14 | Vom Montag, 14. | Du lundi 14 |
| `fromMonthTasks` | mes | August tasks | Tareas de agosto | Tarefas de agosto | Aufgaben im August | Tâches d'août |
| `fromCalendar` | fecha | Calendar, August 3 | Calendario, 3 de agosto | Calendário, 3 de agosto | Kalender, 3. August | Calendrier, 3 août |
| `fromFuture` | mes, día? | Future, September 14 / Future, September 2026 | Futuro, 14 de septiembre / Futuro, septiembre de 2026 | Futuro, 14 de setembro / Futuro, setembro de 2026 | Zukunft, 14. September / Zukunft, September 2026 | Futur, 14 septembre / Futur, septembre 2026 |
| `futureToCalendar` | | Move to the calendar | Pasar al calendario | Passar para o calendário | In den Kalender | Passer au calendrier |
| `goesTo` | lugar | Goes to September 14. | Va al 14 de septiembre. | Vai para 14 de setembro. | Geht nach: 14. September. | Va vers le 14 septembre. |
| `futureLeave`* | | Leave it | Dejarla | Deixar | Lassen | La laisser |
| `futureLeaveHow` | | It keeps waiting in Future. | Sigue esperando en Futuro. | Continua esperando no Futuro. | Wartet weiter in Zukunft. | Elle attend encore dans Futur. |
| `futureAllDecided` | | Everything Future kept for this month is decided. | Todo lo que Futuro guardaba para este mes está decidido. | Tudo o que o Futuro guardava para este mês está decidido. | Alles, was Zukunft für diesen Monat hatte, ist entschieden. | Tout ce que Futur gardait pour ce mois est décidé. |

Al releer, cada día lleva `dayTitle` y el mes `calendarTitle` y `monthTasks`. Las acciones de una tarea
son las de la hoja (sección 4) con su consecuencia; `decideLater` la deja para esta vez sin tocarla. El
fin lleva `backToToday`.

## 11. Buscar

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `searchHint` | | A word or a #tag | Una palabra o una #etiqueta | Uma palavra ou uma #etiqueta | Ein Wort oder ein #Tag | Un mot ou un #tag |
| `filterOpen` | | Open | Abiertas | Abertas | Offen | Ouvertes |
| `monthGroup` | mes | September 2026 | Septiembre 2026 | Setembro 2026 | September 2026 | Septembre 2026 |
| `futureGroup` | mes | Future, October 2026 | Futuro, octubre de 2026 | Futuro, outubro de 2026 | Zukunft, Oktober 2026 | Futur, octobre 2026 |
| `searchEmpty` | | Search for a word, or type # and a tag. | Busca una palabra, o escribe # y una etiqueta. | Busque uma palavra, ou escreva # e uma etiqueta. | Such nach einem Wort, oder tippe # und einen Tag. | Cherche un mot, ou écris # et un tag. |
| `searchNothing` | | Nothing with those words. | Nada con esas palabras. | Nada com essas palavras. | Nichts mit diesen Wörtern. | Rien avec ces mots. |

Los otros tres filtros son los nombres de los signifiers (sección 4); el grupo de un día,
`longDateWithYear`; el de una lista, su título.

## 12. Clave y guía

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `keyTitle` | | Key | Clave | Legenda | Legende | Légende |
| `keyWhat` | | What each symbol means | Qué significa cada símbolo | O que cada símbolo significa | Was jedes Symbol bedeutet | Ce que veut dire chaque symbole |
| `keyWrite` | | What you write down | Lo que apuntas | O que você anota | Was du notierst | Ce que tu notes |
| `keyHappened` | | What happened to a task | Qué pasó con una tarea | O que aconteceu com uma tarefa | Was mit einer Aufgabe geschah | Ce qu'est devenue une tâche |
| `keyMargin` | | In the margin | En el margen | Na margem | Am Rand | Dans la marge |
| `keyTask` | | Something to do. | Algo que hacer. | Algo para fazer. | Etwas zu tun. | Quelque chose à faire. |
| `keyEvent` | | Something that happens on a date. | Algo que pasa en una fecha. | Algo que acontece numa data. | Etwas, das an einem Datum passiert. | Quelque chose qui arrive à une date. |
| `keyNote` | | Something you want to remember. | Algo que quieres recordar. | Algo que você quer lembrar. | Etwas, das du dir merken willst. | Quelque chose dont tu veux te souvenir. |
| `keyDone` | | Stays where it was, with an x. | Se queda donde estaba, con una x. | Fica onde estava, com um x. | Bleibt, wo sie war, mit einem x. | Reste où elle était, avec un x. |
| `movedName` | | Moved | Pasada | Passada | Verschoben | Déplacée |
| `keyMoved` | | You took it to another day or a list. In the method: migrated. | La llevaste a otro día o a una lista. En el método: migrada. | Você a levou para outro dia ou uma lista. No método: migrada. | Du hast sie auf einen anderen Tag oder in eine Liste gelegt. In der Methode: migriert. | Tu l'as portée à un autre jour ou dans une liste. Dans la méthode : migrée. |
| `otherMonthName` | | Moved to another month | Llevada a otro mes | Levada para outro mês | In einen anderen Monat | Reportée à un autre mois |
| `keyOtherMonth` | | Waits in Future. In the method: scheduled. | Espera en Futuro. En el método: programada. | Espera no Futuro. No método: agendada. | Wartet in Zukunft. In der Methode: eingeplant. | Attend dans Futur. Dans la méthode : planifiée. |
| `keyDiscarded` | | It wasn't needed any more. In the method: irrelevant. | Ya no hacía falta. En el método: irrelevante. | Não era mais necessária. No método: irrelevante. | Wurde nicht mehr gebraucht. In der Methode: irrelevant. | Elle n'était plus utile. Dans la méthode : non pertinente. |
| `keyPriority` | | What comes before everything else. | Lo que va antes que lo demás. | O que vem antes do resto. | Was vor allem anderen kommt. | Ce qui passe avant le reste. |
| `keyInspiration` | | An idea worth keeping. | Una idea que vale la pena. | Uma ideia que vale a pena. | Eine Idee, die sich lohnt. | Une idée qui vaut le coup. |
| `keyExplore` | | Something to look into. | Algo que investigar. | Algo para investigar. | Etwas zum Nachforschen. | Quelque chose à creuser. |
| `keyPrefixes` | | When writing: start with ^o^ for an event, ^-^ for a note, and ^*^, ^!^ or ^?^ for the margin marks. | Al escribir: empieza con ^o^ para un evento, con ^-^ para una nota, y con ^*^, ^!^ o ^?^ para las marcas del margen. | Ao escrever: comece com ^o^ para um evento, com ^-^ para uma nota, e com ^*^, ^!^ ou ^?^ para as marcas da margem. | Beim Schreiben: beginne mit ^o^ für ein Ereignis, mit ^-^ für eine Notiz und mit ^*^, ^!^ oder ^?^ für die Randzeichen. | En écrivant : commence par ^o^ pour un événement, par ^-^ pour une note, et par ^*^, ^!^ ou ^?^ pour les marques de la marge. |

El nombre de cada fila es `bulletTask`, `bulletEvent`, `bulletNote`, `stateDone`, `movedName`,
`otherMonthName`, `stateDiscarded` y los tres `signifier*` (sección 4); `keyMoved` y `keyOtherMonth`
dicen además su nombre en el método. Los símbolos que citan las filas (`o`, `-`, `*`, `!`, `?`, `>`,
`<`) son los del método, iguales en los cinco idiomas. Los `^` de `keyPrefixes` marcan lo que va en
negrita.

**Guía del primer arranque** (`docs/pantallas.md` 13.1). Las líneas de ejemplo de los dibujos
(`guideInk`, `guideDinner`, `guideOpens`, `guideQuote`, `guideBank`) son de un diario inventado y se
traducen como cualquier texto. `skip`, `previous` y `next` son los de las secciones 2 y 10.

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `guideStart`* | | Start writing | Empezar a escribir | Começar a escrever | Losschreiben | Commencer à écrire |
| `guideStep` | i, n | Step 1 of 4 | Paso 1 de 4 | Passo 1 de 4 | Schritt 1 von 4 | Étape 1 sur 4 |
| `guideTitles[0]` | | Write it in one line | Apunta en una línea | Anote em uma linha | Schreib es in eine Zeile | Note-le en une ligne |
| `guideTexts[0]` | | Each line is a task, an event or a note. The symbol in front tells you which. | Cada línea es una tarea, un evento o una nota. El símbolo de delante te dice cuál es. | Cada linha é uma tarefa, um evento ou uma nota. O símbolo na frente diz qual é. | Jede Zeile ist eine Aufgabe, ein Ereignis oder eine Notiz. Das Symbol davor sagt dir, was. | Chaque ligne est une tâche, un événement ou une note. Le symbole devant te dit lequel. |
| `guideTitles[1]` | | Tap the dot when it's done | Toca el punto cuando esté hecha | Toque no ponto quando estiver feita | Tippe auf den Punkt, wenn sie erledigt ist | Touche le point quand c'est fait |
| `guideTexts[1]` | | The task stays where it was, marked with an x. If you got it wrong, another tap reopens it. | La tarea se queda donde estaba, marcada con una x. Si te equivocas, otro toque la reabre. | A tarefa fica onde estava, marcada com um x. Se errar, outro toque a reabre. | Die Aufgabe bleibt, wo sie war, mit einem x markiert. Hast du dich vertan, öffnet ein weiterer Tipp sie wieder. | La tâche reste où elle était, marquée d'un x. Si tu te trompes, un autre toucher la rouvre. |
| `guideTitles[2]` | | You move what's pending | Lo pendiente lo mueves tú | O pendente, você que move | Offenes verschiebst du selbst | Ce qui reste, c'est toi qui le déplaces |
| `guideTexts[2]` | | Nothing moves on its own. You move a task to tomorrow, take it to another month or discard it, and the page keeps the trace. | Nada cambia de sitio solo. Pasas una tarea a mañana, la llevas a otro mes o la descartas, y en la página queda el rastro. | Nada muda de lugar sozinho. Você passa uma tarefa para amanhã, leva para outro mês ou descarta, e a página guarda o rastro. | Nichts wandert von allein. Du verschiebst eine Aufgabe auf morgen, in einen anderen Monat oder verwirfst sie, und die Seite behält die Spur. | Rien ne bouge tout seul. Tu passes une tâche à demain, tu la reportes à un autre mois ou tu l'écartes, et la page en garde la trace. |
| `guideTitles[3]` | | Once a month, review | Una vez al mes, repasa | Uma vez por mês, revise | Einmal im Monat durchsehen | Une fois par mois, fais le point |
| `guideTexts[3]` | | Bobbin tells you what was left open last month and shows it to you one at a time, with each way out explained. | Bobbin te avisa de lo que quedó abierto el mes anterior y te lo enseña de una en una, con cada salida explicada. | O Bobbin avisa o que ficou aberto no mês anterior e mostra uma de cada vez, com cada saída explicada. | Bobbin zeigt dir, was im Vormonat offen blieb, eins nach dem anderen, und erklärt jeden Ausweg. | Bobbin te signale ce qui est resté ouvert le mois dernier et te le montre une à une, avec chaque issue expliquée. |
| `guideInk` | | Buy ink | Comprar tinta | Comprar tinta | Tinte kaufen | Acheter de l'encre |
| `guideDinner` | | Dinner with Ana | Cena con Ana | Jantar com a Ana | Essen mit Ana | Dîner avec Ana |
| `guideOpens` | | Opens at 10 | Abre a las 10 | Abre às 10 | Öffnet um 10 | Ouvre à 10 h |
| `guideQuote` | | Send the quote | Enviar el presupuesto | Enviar o orçamento | Angebot schicken | Envoyer le devis |
| `guideBank` | | Call the bank | Llamar al banco | Ligar para o banco | Bank anrufen | Appeler la banque |
| `guideCopy` | | Copy | Copia | Cópia | Kopie | Copie |
| `guideUnclosed` | n | 4 tasks not closed | 4 tareas sin cerrar | 4 tarefas sem fechar | 4 Aufgaben offen | 4 tâches non clôturées |
| `guideToToday` | | To today | A hoy | Para hoje | Auf heute | À aujourd'hui |
| `guideToMonth` | | Other month | A otro mes | Outro mês | Anderer Monat | Autre mois |

## 13. Ajustes

| Clave | Parámetros | en | es | pt | de | fr |
|---|---|---|---|---|---|---|
| `settingsTitle` | | Settings | Ajustes | Ajustes | Einstellungen | Réglages |
| `sectionHelp` | | Help | Ayuda | Ajuda | Hilfe | Aide |
| `guideAgain` | | See the guide again | Ver la guía otra vez | Ver o guia de novo | Anleitung erneut ansehen | Revoir le guide |
| `guideAgainSub` | | The four screens from the first start. | Las cuatro pantallas del primer arranque. | As quatro telas da primeira abertura. | Die vier Seiten vom ersten Start. | Les quatre écrans du premier lancement. |
| `keyRowSub` | | The method's key, with its usual names. | La clave del método, con sus nombres de siempre. | A legenda do método, com os nomes de sempre. | Die Legende der Methode, mit ihren üblichen Namen. | La légende de la méthode, avec ses noms habituels. |
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
  tiene no cambia nada, y "0 cambios" suena a fallo. El singular (decisión de #16, fila `cambio` de
  la regla del método 3) es "1 change", "1 cambio", "1 alteração", "1 Änderung", "1 modification".
- `importIsSibling` recibe el nombre de la app reconocida (`Purl`, `MoodTraker` o `Quilt`, 4.5) y
  vale para v1.0 y v1.1; en v1.2 la importación se desvía (sección 24).
- `wipeConfirmTitle` lleva dentro `wipeWord` del mismo idioma, y la comparación es con `fold`
  (`docs/pantallas.md` 15.4): en alemán vale "löschen" o "loschen".
- Exportar no tiene más textos: `exportFailed` con `ok`; cancelar no enseña nada.
- Decisión de #16, las mismas tres frases en singular en los otros cuatro idiomas, siguiendo la
  fila `entrada` de la regla del método 3: en "1 new entry", "1 newer than yours", "1 was already
  here"; pt "1 entrada nova", "1 mais recente que as suas", "1 já estava aqui"; de "1 neuer
  Eintrag", "1, der neuer ist als deine", "1 war schon da"; fr "1 nouvelle entrée", "1 plus récente
  que les tiennes", "1 était déjà là". El alemán mantiene la coma de la frase relativa también en
  singular.

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
| `a11yComplete` | | Complete | Completar | Concluir | Erledigen | Terminer |
| `a11yReopen` | | Reopen | Reabrir | Reabrir | Wieder öffnen | Rouvrir |
| `a11yMoveUp` | | Move up | Subir | Subir | Nach oben | Monter |
| `a11yMoveDown` | | Move down | Bajar | Descer | Nach unten | Descendre |
| `a11yOptions` | | See options | Ver opciones | Ver opções | Optionen ansehen | Voir les options |
| `a11yDayRow` | día, nombre, n | 3, Thursday, 2 entries | 3, jueves, 2 entradas | 3, quinta-feira, 2 entradas | 3., Donnerstag, 2 Einträge | 3, jeudi, 2 entrées |
| `a11yDayRow` | día, nombre, 0 | 3, Thursday, no entries | 3, jueves, sin entradas | 3, quinta-feira, sem entradas | 3., Donnerstag, keine Einträge | 3, jeudi, aucune entrée |
| `a11yTrackerCell` | fila, fecha, marcada | Two litres, September 23, marked / not marked | Dos litros, 23 de septiembre, marcada / sin marcar | Dois litros, 23 de setembro, marcada / sem marcar | Zwei Liter, 23. September, markiert / nicht markiert | Deux litres, 23 septembre, cochée / pas cochée |
| `a11yTrackerMarked` | días | Marked: 1, 2, 5 and 9 | Marcados: 1, 2, 5 y 9 | Marcados: 1, 2, 5 e 9 | Markiert: 1, 2, 5 und 9 | Cochés : 1, 2, 5 et 9 |
| `a11yTrackerMarked` | ninguno | No day marked | Ningún día marcado | Nenhum dia marcado | Kein Tag markiert | Aucun jour coché |

- `entryDescription` junta `glyphName` del estado (sección 4), los signifiers puestos por su nombre en
  el orden prioridad, inspiración, explorar, separados por coma, dos puntos y el texto. Los nombres de
  los signifiers van en minúscula salvo en alemán. Sin signifiers, el glifo, dos puntos y el texto:
  "Evento: cena con Ana". Una pasada o llevada a otro mes añade `. ` y la línea que se ve debajo
  (`movedTo`, `scheduledTo` o `cameFrom`, sección 4).
- El texto del usuario se lee tal cual, sin cambiarle la caja; "comprar pan" es solo el ejemplo.
- La acción principal de una fila es `a11yOptions`, que abre su hoja. Las acciones personalizadas son
  `a11yComplete` o `a11yReopen`, `actionGoToCopy`, `a11yMoveUp` y `a11yMoveDown`. Los iconos usan las
  `a11y*` de la sección 3; los que llevan su texto al lado (volver, compartir) se leen por ese texto.
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

Decisión de #16: de esta tabla, `Strings.kt` solo expone como funciones las tres palabras sueltas y
sin ambigüedad (`dateWordToday`, `dateWordTomorrow`, `dateWordDayAfterTomorrow`); los patrones de
varias formas por idioma ("on the 14th, the 14th", "March 14, 14 March"...) se quedan en esta tabla
para que `model/DateHint.kt` (#66) los lea directamente de aquí, porque reconocerlos es lógica de
párser, no una cadena que traducir.

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
