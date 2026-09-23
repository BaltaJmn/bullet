package com.baltajmn.bullet.i18n

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/** The languages the app ships. Anything else falls back to English. */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr")

internal fun normalizeLanguage(code: String): String =
    code.take(2).lowercase().takeIf { it in SUPPORTED } ?: "en"

/** How many questions [S.question] holds (docs/tecnico.md 12.3). */
const val QUESTION_COUNT = 60

/**
 * Every user-facing string, in one table, copied from docs/textos.md. The section comments below
 * match that document's numbering (1 to 24) one for one.
 *
 * Not Compose Resources on purpose: part of these strings are drawn outside a @Composable (a
 * BroadcastReceiver, a Glance widget, a Canvas, a notification builder).
 *
 * ponytail: the language is read once at first access and the plain texts are resolved then, like
 * the siblings. Both systems restart the app when the language changes, so this only matters if
 * live switching is ever needed. The functions read [lang] on every call, which lets the tests go
 * through the five.
 */
object S {

    internal var lang = normalizeLanguage(systemLanguage())

    private fun t(en: String, es: String, pt: String, de: String, fr: String): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        else -> en
    }

    /** Method rule 3 (docs/textos.md): fr treats 0 and 1 as singular, the other four only 1. */
    private fun isPlural(n: Int): Boolean = if (lang == "fr") n >= 2 else n != 1
    private fun isSingular(n: Int): Boolean = !isPlural(n)

    /** One word that changes with the count, picked by language and then by [isPlural]. */
    private fun word(
        n: Int,
        en: Pair<String, String>,
        es: Pair<String, String>,
        pt: Pair<String, String>,
        de: Pair<String, String>,
        fr: Pair<String, String>,
    ): String {
        val pair = when (lang) {
            "es" -> es
            "pt" -> pt
            "de" -> de
            "fr" -> fr
            else -> en
        }
        return if (isPlural(n)) pair.second else pair.first
    }

    private fun wordTask(n: Int) =
        word(n, "task" to "tasks", "tarea" to "tareas", "tarefa" to "tarefas", "Aufgabe" to "Aufgaben", "tâche" to "tâches")

    private fun wordEntry(n: Int) = word(
        n,
        "entry" to "entries",
        "entrada" to "entradas",
        "entrada" to "entradas",
        "Eintrag" to "Einträge",
        "entrée" to "entrées",
    )

    private fun wordChange(n: Int) = word(
        n,
        "change" to "changes",
        "cambio" to "cambios",
        "alteração" to "alterações",
        "Änderung" to "Änderungen",
        "modification" to "modifications",
    )

    private fun wordImage(n: Int) =
        word(n, "image" to "images", "imagen" to "imágenes", "imagem" to "imagens", "Bild" to "Bilder", "image" to "images")

    /** Generic count, not tied to one screen; used wherever a bare "n tasks" is needed. */
    fun taskCount(n: Int) = "$n ${wordTask(n)}"

    /** Generic count, not tied to one screen; used wherever a bare "n entries" is needed. */
    fun entryCount(n: Int) = "$n ${wordEntry(n)}"

    /** "1, 2, 5 y 9": commas and the language's conjunction before the last item. */
    fun joinAnd(items: List<String>): String {
        if (items.size <= 1) return items.firstOrNull().orEmpty()
        val and = t(" and ", " y ", " e ", " und ", " et ")
        return items.dropLast(1).joinToString(", ") + and + items.last()
    }

    private fun ofConnector() = t("of", "de", "de", "von", "sur")

    /** French elides "de" before a month name that starts with a vowel: "Tâches d'août". */
    private fun frDe(month: String) = if (month.firstOrNull()?.lowercaseChar() in setOf('a', 'e', 'i', 'o', 'u')) {
        "d'$month"
    } else {
        "de $month"
    }

    // --- 1. Fechas y horas ----------------------------------------------------------------

    fun monthNames(): List<String> = t(
        "January, February, March, April, May, June, July, August, September, October, November, December",
        "enero, febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre",
        "janeiro, fevereiro, março, abril, maio, junho, julho, agosto, setembro, outubro, novembro, dezembro",
        "Januar, Februar, März, April, Mai, Juni, Juli, August, September, Oktober, November, Dezember",
        "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre",
    ).split(", ")

    fun monthShort(): List<String> = t(
        "Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec",
        "ene, feb, mar, abr, may, jun, jul, ago, sept, oct, nov, dic",
        "jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez",
        "Jan., Feb., März, Apr., Mai, Juni, Juli, Aug., Sept., Okt., Nov., Dez.",
        "janv., févr., mars, avr., mai, juin, juil., août, sept., oct., nov., déc.",
    ).split(", ")

    fun weekdayNames(): List<String> = t(
        "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday",
        "lunes, martes, miércoles, jueves, viernes, sábado, domingo",
        "segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado, domingo",
        "Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag",
        "lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche",
    ).split(", ")

    fun weekdayShort(): List<String> = t(
        "Mon, Tue, Wed, Thu, Fri, Sat, Sun",
        "lun, mar, mié, jue, vie, sáb, dom",
        "seg, ter, qua, qui, sex, sáb, dom",
        "Mo, Di, Mi, Do, Fr, Sa, So",
        "lun, mar, mer, jeu, ven, sam, dim",
    ).split(", ")

    /** Monday to Sunday; Mes paints it starting from whichever day the week starts on. */
    fun weekdayInitial(): List<String> = t(
        "M, T, W, T, F, S, S",
        "L, M, X, J, V, S, D",
        "S, T, Q, Q, S, S, D",
        "M, D, M, D, F, S, S",
        "L, M, M, J, V, S, D",
    ).split(", ")

    /** French writes the first of the month as "1er" in any date that carries the month name. */
    private fun frDay(day: Int): String = if (lang == "fr" && day == 1) "1er" else "$day"

    fun dayTitle(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal].replaceFirstChar { it.uppercase() }
        return t("$w ${d.day}", "$w ${d.day}", "$w, ${d.day}", "$w, ${d.day}.", "$w ${d.day}")
    }

    fun monthName(month: YearMonth): String = monthNames()[month.month.ordinal].replaceFirstChar { it.uppercase() }

    fun monthYear(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        val y = month.year
        return t("$m $y", "$m de $y", "$m de $y", "$m $y", "$m $y")
    }

    fun monthTitle(month: YearMonth): String = "${monthName(month)} ${month.year}"

    fun shortDate(d: LocalDate): String {
        val m = monthNames()[d.month.ordinal]
        val day = frDay(d.day)
        return t("$m ${d.day}", "$day de $m", "$day de $m", "$day. $m", "$day $m")
    }

    private fun yearSuffix(y: Int) = t(", $y", " de $y", " de $y", " $y", " $y")

    fun longDate(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal].replaceFirstChar { it.uppercase() }
        return w + t(", ", ", ", ", ", ", ", " ") + shortDate(d)
    }

    fun dayMonthYear(d: LocalDate) = shortDate(d) + yearSuffix(d.year)

    fun longDateWithYear(d: LocalDate) = longDate(d) + yearSuffix(d.year)

    fun abbrDate(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        return t("$m ${d.day}", "${d.day} $m", "${d.day} $m", "${d.day}. $m", "${d.day} $m")
    }

    fun abbrDateWithYear(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        val y = d.year
        return t("$m ${d.day}, $y", "${d.day} $m $y", "${d.day} $m $y", "${d.day}. $m $y", "${d.day} $m $y")
    }

    /** Always 24 hours, in the five languages. */
    fun clock(hour: Int, minute: Int) =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    // --- 2. Comunes -------------------------------------------------------------------------

    val appName = "Bobbin"
    val ok = t("OK", "Vale", "OK", "OK", "OK")
    val cancel = t("Cancel", "Cancelar", "Cancelar", "Abbrechen", "Annuler")
    val yes = t("Yes", "Sí", "Sim", "Ja", "Oui")
    val notNow = t("Not now", "Ahora no", "Agora não", "Jetzt nicht", "Pas maintenant")
    val close = t("Close", "Cerrar", "Fechar", "Schließen", "Fermer")
    val back = t("Back", "Volver", "Voltar", "Zurück", "Retour")
    val undo = t("Undo", "Deshacer", "Desfazer", "Rückgängig", "Annuler")
    val working = t("One moment...", "Un momento...", "Um momento...", "Einen Moment...", "Un instant...")

    // --- 3. Navegacion y cabeceras -----------------------------------------------------------

    val tabToday = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val tabMonth = t("Month", "Mes", "Mês", "Monat", "Mois")
    val tabFuture = t("Future", "Futuro", "Futuro", "Zukunft", "Futur")
    val tabIndex = t("Index", "Índice", "Índice", "Index", "Index")
    val backToToday = t("Back to today", "Volver a hoy", "Voltar para hoje", "Zurück zu heute", "Revenir à aujourd'hui")
    val a11yBack = t("Back", "Volver", "Voltar", "Zurück", "Retour")
    val a11yPreviousDay = t("Previous day", "Día anterior", "Dia anterior", "Vorheriger Tag", "Jour précédent")
    val a11yNextDay = t("Next day", "Día siguiente", "Próximo dia", "Nächster Tag", "Jour suivant")
    val a11yPreviousMonth = t("Previous month", "Mes anterior", "Mês anterior", "Vorheriger Monat", "Mois précédent")
    val a11yNextMonth = t("Next month", "Mes siguiente", "Próximo mês", "Nächster Monat", "Mois suivant")
    val a11yClose = t("Close", "Cerrar", "Fechar", "Schließen", "Fermer")
    val a11yShare = t("Share this page", "Compartir esta página", "Compartilhar esta página", "Diese Seite teilen", "Partager cette page")
    val a11ySettings = t("Settings", "Ajustes", "Ajustes", "Einstellungen", "Réglages")
    val a11ySearch = t("Search the journal", "Buscar en el diario", "Buscar no diário", "Im Journal suchen", "Chercher dans le journal")
    val a11yKey = t("Symbol key", "Clave de símbolos", "Legenda dos símbolos", "Legende der Symbole", "Légende des symboles")
    val a11yMoreActions = t("More actions", "Más acciones", "Mais ações", "Weitere Aktionen", "Plus d'actions")
    val a11ySelected = t("selected", "elegida", "selecionada", "ausgewählt", "sélectionnée")

    // --- 4. La entrada, la captura y la hoja --------------------------------------------------

    val captureHint = t("Write here", "Escribe aquí", "Escreva aqui", "Hier schreiben", "Écris ici")
    fun counter(n: Int, max: Int) = "$n/$max"
    val bulletTask = t("Task", "Tarea", "Tarefa", "Aufgabe", "Tâche")
    val bulletEvent = t("Event", "Evento", "Evento", "Ereignis", "Événement")
    val bulletNote = t("Note", "Nota", "Nota", "Notiz", "Note")
    val stateDone = t("Done", "Hecha", "Feita", "Erledigt", "Faite")
    val stateMigrated = t("Migrated", "Migrada", "Migrada", "Migriert", "Migrée")
    val stateScheduled = t("Scheduled", "Programada", "Agendada", "Eingeplant", "Planifiée")
    val stateDiscarded = t("Discarded", "Descartada", "Descartada", "Verworfen", "Écartée")
    val signifierPriority = t("Priority", "Prioridad", "Prioridade", "Priorität", "Priorité")
    val signifierInspiration = t("Inspiration", "Inspiración", "Inspiração", "Inspiration", "Inspiration")
    val signifierExplore = t("Explore", "Explorar", "Explorar", "Erkunden", "Explorer")

    fun wentToDay(d: LocalDate) = abbrDate(d)
    fun wentToMonth(month: YearMonth) = monthTitle(month)

    /** [withYear] picks abbrDateWithYear over abbrDate when the destination isn't the current year. */
    fun wentToFuture(month: YearMonth, day: Int? = null, withYear: Boolean = false): String {
        val place = when {
            day != null && withYear -> abbrDateWithYear(LocalDate(month.year, month.month, day))
            day != null -> abbrDate(LocalDate(month.year, month.month, day))
            else -> monthYear(month)
        }
        return t("Future, $place", "Futuro, $place", "Futuro, $place", "Zukunft, $place", "Futur, $place")
    }

    /** Only shown from MIGRATION_SHOWN_FROM (2); the n == 1 phrase exists just in case. */
    fun migratedTimes(n: Int): String = if (n == 1) {
        t("Migrated once", "Migrada 1 vez", "Migrada 1 vez", "Einmal migriert", "Migrée 1 fois")
    } else {
        t("Migrated $n times", "Migrada $n veces", "Migrada $n vezes", "$n-mal migriert", "Migrée $n fois")
    }

    val actionMigrate = t("Migrate", "Migrar", "Migrar", "Migrieren", "Migrer")
    val actionSchedule = t("Schedule", "Programar", "Agendar", "Einplanen", "Planifier")
    val actionDiscard = t("Discard", "Descartar", "Descartar", "Verwerfen", "Écarter")
    val actionReopen = t("Reopen", "Reabrir", "Reabrir", "Wieder öffnen", "Rouvrir")
    val actionGoToCopy = t("Go to the copy", "Ir a la copia", "Ir para a cópia", "Zur Kopie", "Aller à la copie")
    val actionEdit = t("Edit", "Editar", "Editar", "Bearbeiten", "Modifier")
    val actionDelete = t("Delete", "Borrar", "Apagar", "Löschen", "Supprimer")
    val toToday = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val toTomorrow = t("Tomorrow", "Mañana", "Amanhã", "Morgen", "Demain")
    val toThisMonth = t("This month's tasks", "Tareas de este mes", "Tarefas deste mês", "Aufgaben dieses Monats", "Tâches de ce mois")
    val toDayOfMonth = t("A day this month", "Un día de este mes", "Um dia deste mês", "Ein Tag in diesem Monat", "Un jour de ce mois")
    val toCollection = t("To a collection", "A una colección", "Para uma coleção", "In eine Sammlung", "Vers une collection")
    val dayField = t("day", "día", "dia", "Tag", "jour")
    val dayFieldOptional = t("Day, optional", "Día, opcional", "Dia, opcional", "Tag, optional", "Jour, facultatif")
    val migrateAction = t("Migrate", "Migrar", "Migrar", "Migrieren", "Migrer")

    fun dayOutOfRange(month: YearMonth, day: Int): String {
        val name = monthName(month)
        return t(
            "$name has no day $day.",
            "$name no tiene día $day.",
            "$name não tem dia $day.",
            "Der $name hat keinen $day. Tag.",
            "$name n'a pas de jour $day.",
        )
    }

    val dayPast = t("That day has passed.", "Ese día ya pasó.", "Esse dia já passou.", "Dieser Tag ist vorbei.", "Ce jour est passé.")
    val newCollection = t("New collection", "Nueva colección", "Nova coleção", "Neue Sammlung", "Nouvelle collection")
    val showMoreMonths = t("Show more months", "Ver más meses", "Ver mais meses", "Mehr Monate zeigen", "Voir plus de mois")
    val entryDeleted = t("Entry deleted.", "Entrada borrada.", "Entrada apagada.", "Eintrag gelöscht.", "Entrée supprimée.")
    val collectionDeleted = t("Collection deleted.", "Colección borrada.", "Coleção apagada.", "Sammlung gelöscht.", "Collection supprimée.")
    val rowDeleted = t("Row deleted.", "Fila borrada.", "Linha apagada.", "Zeile gelöscht.", "Ligne supprimée.")

    /** docs/pantallas.md 1.5: the name of each glyph, for the screen reader. */
    fun glyphName(bullet: Bullet, status: TaskStatus): String = when (bullet) {
        Bullet.EVENT -> bulletEvent
        Bullet.NOTE -> bulletNote
        Bullet.TASK -> when (status) {
            TaskStatus.DONE -> t("Done task", "Tarea hecha", "Tarefa feita", "Erledigte Aufgabe", "Tâche faite")
            TaskStatus.MIGRATED -> t("Migrated task", "Tarea migrada", "Tarefa migrada", "Migrierte Aufgabe", "Tâche migrée")
            TaskStatus.SCHEDULED -> t("Scheduled task", "Tarea programada", "Tarefa agendada", "Eingeplante Aufgabe", "Tâche planifiée")
            TaskStatus.IRRELEVANT -> t("Discarded task", "Tarea descartada", "Tarefa descartada", "Verworfene Aufgabe", "Tâche écartée")
            TaskStatus.OPEN -> bulletTask
        }
    }

    /** docs/pantallas.md 1.5: the name of a signifier's glyph, for the screen reader. */
    fun glyphName(signifier: Signifier): String = when (signifier) {
        Signifier.PRIORITY -> signifierPriority
        Signifier.INSPIRATION -> signifierInspiration
        Signifier.EXPLORE -> signifierExplore
    }

    // --- 5. Hoy -------------------------------------------------------------------------------

    val prefixHint = t(
        "No prefix, task. - note, o event, * priority.",
        "Sin prefijo, tarea. - nota, o evento, * prioridad.",
        "Sem prefixo, tarefa. - nota, o evento, * prioridade.",
        "Ohne Präfix eine Aufgabe. - Notiz, o Ereignis, * Priorität.",
        "Sans préfixe, une tâche. - note, o événement, * priorité.",
    )
    val calendarToday = t("On the calendar", "En el calendario", "No calendário", "Im Kalender", "Au calendrier")
    val noticeCorrupt = t(
        "Couldn't read the journal. The files were set aside and nothing was deleted.",
        "No se ha podido leer el diario. Los ficheros se han guardado aparte y no se ha borrado nada.",
        "Não foi possível ler o diário. Os arquivos foram guardados à parte e nada foi apagado.",
        "Das Journal konnte nicht gelesen werden. Die Dateien wurden beiseitegelegt, gelöscht wurde nichts.",
        "Impossible de lire le journal. Les fichiers ont été mis de côté et rien n'a été supprimé.",
    )
    val noticeSaveFailed = t(
        "Couldn't save. I'll try again with your next change.",
        "No se ha podido guardar. Lo intento otra vez con tu próximo cambio.",
        "Não foi possível salvar. Vou tentar de novo na sua próxima alteração.",
        "Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut.",
        "Impossible d'enregistrer. Je réessaierai avec ta prochaine modification.",
    )

    fun offerReminder(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "Remind you to go over the day at $time?",
            "¿Te aviso para repasar el día a las $time?",
            "Quer que eu avise para repassar o dia às $time?",
            "Soll ich dich um $time erinnern, den Tag durchzugehen?",
            "Je te rappelle de relire ta journée à $time ?",
        )
    }

    fun unclosedMonth(month: YearMonth, n: Int): String {
        val name = monthName(month)
        val w = widgetOpen(n)
        return t(
            "$name not closed: $n $w",
            "$name sin cerrar: $n $w",
            "$name sem fechar: $n $w",
            "$name nicht abgeschlossen: $n $w",
            "$name pas clôturé : $n $w",
        )
    }

    fun earlierOpen(n: Int): String {
        val singular = isSingular(n)
        return when (lang) {
            "es" -> if (singular) "Queda $n abierta de días anteriores" else "Quedan $n abiertas de días anteriores"
            "pt" -> if (singular) "Resta $n aberta de dias anteriores" else "Restam $n abertas de dias anteriores"
            "de" -> "Noch $n offen von früheren Tagen"
            "fr" -> if (singular) {
                "Il reste $n tâche ouverte des jours précédents"
            } else {
                "Il reste $n tâches ouvertes des jours précédents"
            }
            else -> "$n still open from earlier days"
        }
    }

    // --- 6. Mes ---------------------------------------------------------------------------

    val monthTasks = t("Tasks of the month", "Tareas del mes", "Tarefas do mês", "Aufgaben des Monats", "Tâches du mois")
    val calendarTitle = t("Calendar", "Calendario", "Calendário", "Kalender", "Calendrier")

    fun futureWaiting(n: Int): String {
        val singular = isSingular(n)
        return when (lang) {
            "es" -> if (singular) "$n entrada del Future Log espera" else "$n entradas del Future Log esperan"
            "pt" -> if (singular) "$n entrada do Future Log espera" else "$n entradas do Future Log esperam"
            "de" -> if (singular) "$n Eintrag im Future Log wartet" else "$n Einträge im Future Log warten"
            "fr" -> if (singular) "$n entrée du Future Log attend" else "$n entrées du Future Log attendent"
            else -> if (singular) "$n Future Log entry waiting" else "$n Future Log entries waiting"
        }
    }

    // --- 8. Indice ------------------------------------------------------------------------

    val indexFilterHint = t("Filter by title", "Filtrar por título", "Filtrar por título", "Nach Titel filtern", "Filtrer par titre")
    val indexMonth = t("Month", "Mes", "Mês", "Monat", "Mois")
    val indexCollection = t("Collection", "Colección", "Coleção", "Sammlung", "Collection")
    val indexTracker = t("Tracker", "Seguimiento", "Tracker", "Tracker", "Suivi")
    val newTracker = t("New tracker", "Nuevo seguimiento", "Novo tracker", "Neuer Tracker", "Nouveau suivi")
    fun archivedToggle(n: Int) = t("Archived ($n)", "Archivadas ($n)", "Arquivadas ($n)", "Archiviert ($n)", "Archivées ($n)")
    val indexEmpty = t(
        "Months show up here as soon as you write in them.",
        "Los meses aparecen aquí en cuanto escribes en ellos.",
        "Os meses aparecem aqui assim que você escreve neles.",
        "Monate erscheinen hier, sobald du in ihnen schreibst.",
        "Les mois apparaissent ici dès que tu y écris.",
    )
    val indexNoMatch = t(
        "No title with those letters.",
        "Ningún título con esas letras.",
        "Nenhum título com essas letras.",
        "Kein Titel mit diesen Buchstaben.",
        "Aucun titre avec ces lettres.",
    )

    // --- 9. Coleccion y seguimiento ---------------------------------------------------------

    val archive = t("Archive", "Archivar", "Arquivar", "Archivieren", "Archiver")
    val unarchive = t("Unarchive", "Sacar del archivo", "Tirar do arquivo", "Aus dem Archiv holen", "Désarchiver")
    val deleteCollection = t("Delete collection", "Borrar colección", "Apagar coleção", "Sammlung löschen", "Supprimer la collection")
    val archivedNote = t("Archived.", "Archivada.", "Arquivada.", "Archiviert.", "Archivée.")
    val trackerRowHint = t("New row", "Nueva fila", "Nova linha", "Neue Zeile", "Nouvelle ligne")
    val rowDelete = t("Delete row", "Borrar fila", "Apagar linha", "Zeile löschen", "Supprimer la ligne")

    // --- 10. Revisar ------------------------------------------------------------------------

    fun reflectTitle(month: YearMonth) = t(
        "Read ${monthName(month)} again",
        "Releer ${monthNames()[month.month.ordinal]}",
        "Reler ${monthNames()[month.month.ordinal]}",
        "${monthName(month)} nachlesen",
        "Relire ${monthNames()[month.month.ordinal]}",
    )
    fun reflectTitle() = t("Read up to today", "Releer hasta hoy", "Reler até hoje", "Bis heute nachlesen", "Relire jusqu'à aujourd'hui")

    fun reflectHint(month: YearMonth) = t(
        "A note about this month, if you like",
        "Una nota sobre este mes, si quieres",
        "Uma nota sobre este mês, se quiser",
        "Eine Notiz zu diesem Monat, wenn du magst",
        "Une note sur ce mois, si tu veux",
    )
    fun reflectHint() = t(
        "A note about these days, if you like",
        "Una nota sobre estos días, si quieres",
        "Uma nota sobre estes dias, se quiser",
        "Eine Notiz zu diesen Tagen, wenn du magst",
        "Une note sur ces jours, si tu veux",
    )

    val skip = t("Skip", "Saltar", "Pular", "Überspringen", "Passer")
    val saveAndGo = t("Save and continue", "Guardar y seguir", "Salvar e seguir", "Speichern und weiter", "Valider et continuer")
    fun reviewPosition(i: Int, n: Int) = "$i ${ofConnector()} $n"

    fun fromDay(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        val phrase = t("$w ${d.day}", "$w ${d.day}", "$w, ${d.day}", "$w, ${d.day}.", "$w ${d.day}")
        return t("From $phrase", "Del $phrase", "Da $phrase", "Vom $phrase", "Du $phrase")
    }

    fun fromMonthTasks(month: YearMonth): String {
        val name = monthNames()[month.month.ordinal]
        return when (lang) {
            "es" -> "Tareas de $name"
            "pt" -> "Tarefas de $name"
            "de" -> "Aufgaben im $name"
            "fr" -> "Tâches ${frDe(name)}"
            else -> "$name tasks"
        }
    }

    fun fromCalendar(d: LocalDate): String {
        val date = shortDate(d)
        return t("Calendar, $date", "Calendario, $date", "Calendário, $date", "Kalender, $date", "Calendrier, $date")
    }

    fun fromFuture(month: YearMonth, day: Int? = null): String {
        val place = if (day != null) shortDate(LocalDate(month.year, month.month, day)) else monthYear(month)
        return t("Future, $place", "Futuro, $place", "Futuro, $place", "Zukunft, $place", "Futur, $place")
    }

    val reviewDone = t("Done", "Hecha", "Feita", "Erledigt", "Faite")
    val reviewMigrate = t("Migrate", "Migrar", "Migrar", "Migrieren", "Migrer")
    val reviewSchedule = t("Schedule", "Programar", "Agendar", "Einplanen", "Planifier")
    val reviewToCollection = t("To a collection", "A una colección", "Para uma coleção", "In eine Sammlung", "Vers une collection")
    val reviewDiscard = t("Discard", "Descartar", "Descartar", "Verwerfen", "Écarter")
    val reviewAllDecided = t("All decided.", "Todo decidido.", "Tudo decidido.", "Alles entschieden.", "Tout est décidé.")

    fun monthClosed(month: YearMonth): String {
        val name = monthName(month)
        return t("$name, closed.", "$name, cerrado.", "$name, fechado.", "$name, abgeschlossen.", "$name, clôturé.")
    }

    val futureToCalendar = t(
        "Move to the calendar",
        "Pasar al calendario",
        "Passar para o calendário",
        "In den Kalender",
        "Passer au calendrier",
    )
    val futureLeave = t("Leave it", "Dejarla", "Deixar", "Lassen", "La laisser")
    val futureDiscard = t("Discard", "Descartar", "Descartar", "Verwerfen", "Écarter")
    val futureAllDecided = t(
        "This month's Future Log is up to date.",
        "El Future Log de este mes está al día.",
        "O Future Log deste mês está em dia.",
        "Das Future Log dieses Monats ist auf dem Stand.",
        "Le Future Log de ce mois est à jour.",
    )

    // --- 11. Buscar -------------------------------------------------------------------------

    val searchHint = t("A word or a #tag", "Una palabra o una #etiqueta", "Uma palavra ou uma #etiqueta", "Ein Wort oder ein #Tag", "Un mot ou un #tag")
    val filterOpen = t("Open", "Abiertas", "Abertas", "Offen", "Ouvertes")
    fun monthGroup(month: YearMonth) = monthTitle(month)
    fun futureGroup(month: YearMonth) = wentToFuture(month)
    val searchEmpty = t(
        "Search for a word, or type # and a tag.",
        "Busca una palabra, o escribe # y una etiqueta.",
        "Busque uma palavra, ou escreva # e uma etiqueta.",
        "Such nach einem Wort, oder tippe # und einen Tag.",
        "Cherche un mot, ou écris # et un tag.",
    )
    val searchNothing = t(
        "Nothing with those words.",
        "Nada con esas palabras.",
        "Nada com essas palavras.",
        "Nichts mit diesen Wörtern.",
        "Rien avec ces mots.",
    )

    // --- 12. Clave --------------------------------------------------------------------------

    val keyTitle = t("Key", "Clave", "Legenda", "Legende", "Légende")
    val keyBullets = t("Bullets", "Bullets", "Bullets", "Bullets", "Bullets")
    val keyStates = t("States", "Estados", "Estados", "Zustände", "États")
    val keySignifiers = t("Signifiers", "Signifiers", "Signifiers", "Signifiers", "Signifiers")
    val keyGestures = t("Gestures", "Gestos", "Gestos", "Gesten", "Gestes")
    val keyTaskHow = t("No prefix", "Sin prefijo", "Sem prefixo", "Ohne Präfix", "Sans préfixe")
    val keyEventHow = t("Starts with o and a space", "Empieza con o y espacio", "Começa com o e espaço", "Beginnt mit o und Leerzeichen", "Commence par o et un espace")
    val keyNoteHow = t("Starts with - and a space", "Empieza con - y espacio", "Começa com - e espaço", "Beginnt mit - und Leerzeichen", "Commence par - et un espace")
    val keyDoneHow = t("Tap the dot", "Toca el punto", "Toque no ponto", "Tippe auf den Punkt", "Touche le point")
    val keyMigratedHow = t("Press and hold: Migrate", "Mantén pulsada: Migrar", "Toque e segure: Migrar", "Gedrückt halten: Migrieren", "Appui long : Migrer")
    val keyScheduledHow = t("Press and hold: Schedule", "Mantén pulsada: Programar", "Toque e segure: Agendar", "Gedrückt halten: Einplanen", "Appui long : Planifier")
    val keyDiscardedHow = t("Press and hold: Discard", "Mantén pulsada: Descartar", "Toque e segure: Descartar", "Gedrückt halten: Verwerfen", "Appui long : Écarter")
    val keyMigratedLink = t(
        "Tap the > to go where it went.",
        "Toca el > para ir a donde fue.",
        "Toque no > para ir aonde ela foi.",
        "Tippe auf das >, um zu ihrer Kopie zu gehen.",
        "Touche le > pour aller là où elle est partie.",
    )
    val keyPriorityHow = t("Starts with * and a space", "Empieza con * y espacio", "Começa com * e espaço", "Beginnt mit * und Leerzeichen", "Commence par * et un espace")
    val keyInspirationHow = t("Starts with ! and a space", "Empieza con ! y espacio", "Começa com ! e espaço", "Beginnt mit ! und Leerzeichen", "Commence par ! et un espace")
    val keyExploreHow = t("Starts with ? and a space", "Empieza con ? y espacio", "Começa com ? e espaço", "Beginnt mit ? und Leerzeichen", "Commence par ? et un espace")
    val keyGestureTap = t(
        "Tapping the symbol completes a task.",
        "Tocar el símbolo completa una tarea.",
        "Tocar no símbolo conclui uma tarefa.",
        "Ein Tipp auf das Symbol erledigt eine Aufgabe.",
        "Toucher le symbole termine une tâche.",
    )
    val keyGestureHold = t(
        "Holding an entry opens its actions.",
        "Mantener pulsada una entrada abre sus acciones.",
        "Tocar e segurar uma entrada abre suas ações.",
        "Langes Drücken auf einen Eintrag öffnet seine Aktionen.",
        "Un appui long sur une entrée ouvre ses actions.",
    )
    val keyGestureSwipe = t(
        "Swiping sideways on Today changes the day.",
        "Deslizar a los lados en Hoy cambia de día.",
        "Deslizar para os lados em Hoje muda o dia.",
        "Seitlich wischen in Heute wechselt den Tag.",
        "Glisser sur le côté dans Aujourd'hui change de jour.",
    )
    val keyGestureDrag = t(
        "Holding and dragging changes the order.",
        "Mantener pulsada y arrastrar cambia el orden.",
        "Segurar e arrastar muda a ordem.",
        "Gedrückt halten und ziehen ändert die Reihenfolge.",
        "Appuyer longuement et glisser change l'ordre.",
    )
    val keyTapText = t(
        "Tap the text to edit it.",
        "Toca el texto para editarlo.",
        "Toque no texto para editá-lo.",
        "Tippe auf den Text, um ihn zu bearbeiten.",
        "Touche le texte pour le modifier.",
    )

    // --- 13. Ajustes ------------------------------------------------------------------------

    val settingsTitle = t("Settings", "Ajustes", "Ajustes", "Einstellungen", "Réglages")
    val sectionDay = t("Day", "Día", "Dia", "Tag", "Jour")
    val dayStartRow = t("The day starts", "El día empieza", "O dia começa", "Der Tag beginnt", "La journée commence")
    fun dayStartAt(hour: Int): String {
        val time = clock(hour, 0)
        return t("At $time", "A las $time", "Às $time", "Um $time", "À $time")
    }
    val weekStartRow = t("The week starts", "La semana empieza", "A semana começa", "Die Woche beginnt", "La semaine commence")

    private fun ptWeekArticle(day: DayOfWeek) = if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) "No" else "Na"

    fun weekStartSystem(day: DayOfWeek): String {
        val name = weekdayNames()[day.ordinal]
        return t(
            "$name, like the system",
            "El $name, como el sistema",
            "${ptWeekArticle(day)} $name, como o sistema",
            "$name, wie im System",
            "Le $name, comme le système",
        )
    }

    fun weekStartDay(day: DayOfWeek): String {
        val name = weekdayNames()[day.ordinal]
        return t(name, "El $name", "${ptWeekArticle(day)} $name", name, "Le $name")
    }

    val weekStartFollowSystem = t("Like the system", "Como el sistema", "Como o sistema", "Wie im System", "Comme le système")
    val sectionReminder = t("Reminder", "Recordatorio", "Lembrete", "Erinnerung", "Rappel")
    val reminderRow = t(
        "Reminder to go over the day",
        "Recordatorio para repasar el día",
        "Lembrete para repassar o dia",
        "Erinnerung, den Tag durchzugehen",
        "Rappel pour relire ta journée",
    )
    val reminderOff = t("Off", "Apagado", "Desativado", "Aus", "Désactivé")
    fun reminderAt(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t("At $time", "A las $time", "Às $time", "Um $time", "À $time")
    }
    val reminderDenied = t(
        "Bobbin's notifications are turned off in the system.",
        "Las notificaciones de Bobbin están desactivadas en el sistema.",
        "As notificações do Bobbin estão desativadas no sistema.",
        "Die Benachrichtigungen von Bobbin sind im System deaktiviert.",
        "Les notifications de Bobbin sont désactivées dans le système.",
    )
    val openSystemSettings = t("Open settings", "Abrir ajustes", "Abrir ajustes", "Einstellungen öffnen", "Ouvrir les réglages")
    val sectionPrivacy = t("Privacy", "Privacidad", "Privacidade", "Datenschutz", "Confidentialité")
    val lockRow = t("Lock the journal", "Bloquear el diario", "Bloquear o diário", "Journal sperren", "Verrouiller le journal")
    val lockSubtitle = t(
        "Asks for your face, your fingerprint or your phone code",
        "Pide tu cara, tu huella o el código del teléfono",
        "Pede seu rosto, sua digital ou o código do telefone",
        "Fragt nach deinem Gesicht, deinem Fingerabdruck oder dem Code des Handys",
        "Demande ton visage, ton empreinte ou le code du téléphone",
    )
    val lockUnavailable = t(
        "Set a screen lock on your phone to use this.",
        "Pon un bloqueo de pantalla en el teléfono para usarlo.",
        "Configure um bloqueio de tela no telefone para usar isso.",
        "Richte eine Bildschirmsperre auf dem Handy ein, um das zu nutzen.",
        "Active un verrouillage d'écran sur ton téléphone pour l'utiliser.",
    )
    val sectionNotebook = t("Notebook", "Cuaderno", "Caderno", "Notizbuch", "Carnet")
    val previewTask = t("Buy ink", "Comprar tinta", "Comprar tinta", "Tinte kaufen", "Acheter de l'encre")
    val previewEvent = t("Dinner with Ana", "Cena con Ana", "Jantar com a Ana", "Abendessen mit Ana", "Dîner avec Ana")

    private val COVER_ORDER = listOf("rose", "peach", "butter", "sage", "mint", "sky", "periwinkle", "lilac")

    fun coverName(id: String): String {
        val names = t(
            "Rose, Peach, Butter, Sage, Mint, Sky, Periwinkle, Lilac",
            "Rosa, Melocotón, Mantequilla, Salvia, Menta, Cielo, Pervinca, Lila",
            "Rosa, Pêssego, Manteiga, Sálvia, Hortelã, Céu, Pervinca, Lilás",
            "Rosa, Pfirsich, Butter, Salbei, Minze, Himmel, Periwinkle, Flieder",
            "Rose, Pêche, Beurre, Sauge, Menthe, Ciel, Pervenche, Lilas",
        ).split(", ")
        return names[COVER_ORDER.indexOf(id).takeIf { it >= 0 } ?: COVER_ORDER.indexOf("sage")]
    }

    private val PAPER_ORDER = listOf("dotted", "lined", "grid", "blank")

    fun paperName(id: String): String {
        val names = t(
            "dotted, lined, grid, blank",
            "punteado, rayado, cuadrícula, liso",
            "pontilhado, pautado, quadriculado, liso",
            "gepunktet, liniert, kariert, blanko",
            "pointillé, ligné, quadrillé, uni",
        ).split(", ")
        return names[PAPER_ORDER.indexOf(id).takeIf { it >= 0 } ?: PAPER_ORDER.indexOf("dotted")]
    }

    val proLookHint = t(
        "Sage and dotted are free; the rest come with Bobbin Pro.",
        "Salvia y el punteado son gratis; lo demás, con Bobbin Pro.",
        "Sálvia e o pontilhado são grátis; o resto vem com o Bobbin Pro.",
        "Salbei und gepunktet sind kostenlos, der Rest kommt mit Bobbin Pro.",
        "Sauge et le pointillé sont gratuits ; le reste vient avec Bobbin Pro.",
    )
    val useThis = t("Use this style", "Usar este estilo", "Usar este estilo", "Diesen Stil nutzen", "Utiliser ce style")
    val sectionBackup = t("Backup", "Copia", "Cópia", "Sicherung", "Sauvegarde")
    val exportRow = t("Export backup", "Exportar copia", "Exportar cópia", "Kopie exportieren", "Exporter une copie")
    val exportSubtitle = t(
        "A zip with your journal in JSON and Markdown",
        "Un zip con tu diario en JSON y en Markdown",
        "Um zip com seu diário em JSON e em Markdown",
        "Ein Zip mit deinem Journal als JSON und Markdown",
        "Un zip avec ton journal en JSON et en Markdown",
    )
    val exportNothing = t(
        "There's nothing to back up yet.",
        "Aún no hay nada que copiar.",
        "Ainda não há nada para copiar.",
        "Es gibt noch nichts zu sichern.",
        "Il n'y a encore rien à copier.",
    )
    val importRow = t("Import backup", "Importar copia", "Importar cópia", "Kopie importieren", "Importer une copie")
    val importSubtitle = t(
        "Joins your journal, nothing gets deleted",
        "Se junta con tu diario, sin borrar nada",
        "Se junta ao seu diário, sem apagar nada",
        "Wird mit deinem Journal zusammengeführt, nichts wird gelöscht",
        "Se joint à ton journal, rien n'est supprimé",
    )
    val sectionPro = t("Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro")
    val proRow = t("Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro")
    val proSubtitle = t(
        "Covers, papers, trackers and widgets. One-time payment",
        "Portadas, papeles, seguimientos y widgets. Pago único",
        "Capas, papéis, trackers e widgets. Pagamento único",
        "Umschläge, Papiere, Tracker und Widgets. Einmalzahlung",
        "Couvertures, papiers, suivis et widgets. Paiement unique",
    )
    val proOwned = t("Purchased. Thank you.", "Comprado. Gracias.", "Comprado. Obrigado.", "Gekauft. Danke.", "Acheté. Merci.")
    val restoreRow = t("Restore purchase", "Restaurar compra", "Restaurar compra", "Kauf wiederherstellen", "Restaurer l'achat")
    val restoreDone = t("Purchase restored.", "Compra restaurada.", "Compra restaurada.", "Kauf wiederhergestellt.", "Achat restauré.")
    val restoreNothing = t(
        "There's no purchase to restore.",
        "No hay ninguna compra que restaurar.",
        "Não há nenhuma compra para restaurar.",
        "Es gibt keinen Kauf zum Wiederherstellen.",
        "Il n'y a aucun achat à restaurer.",
    )
    val sectionMoreApps = t("More apps", "Más apps", "Mais apps", "Weitere Apps", "Plus d'apps")
    val siblingPurl = t(
        "One line a day, read again every year",
        "Una línea al día que vuelves a leer cada año",
        "Uma linha por dia, relida a cada ano",
        "Eine Zeile am Tag, jedes Jahr wieder gelesen",
        "Une ligne par jour, relue chaque année",
    )
    val siblingQuilt = t(
        "Your habits, a year at a glance",
        "Tus hábitos, un año a la vista",
        "Seus hábitos, um ano à vista",
        "Deine Gewohnheiten, ein Jahr im Blick",
        "Tes habitudes, une année en un coup d'oeil",
    )
    val siblingMood = t(
        "How each day went, in colour",
        "Cómo te ha ido cada día, en color",
        "Como foi cada dia, em cores",
        "Wie jeder Tag war, in Farbe",
        "Comment chaque jour s'est passé, en couleur",
    )
    val sectionAbout = t("About", "Acerca de", "Sobre", "Über", "À propos")
    val privacyRow = t(
        "Privacy policy",
        "Política de privacidad",
        "Política de privacidade",
        "Datenschutzerklärung",
        "Politique de confidentialité",
    )
    fun version(v: String) = t("Version $v", "Versión $v", "Versão $v", "Version $v", "Version $v")
    val wipeRow = t("Delete all data", "Borrar todos los datos", "Apagar todos os dados", "Alle Daten löschen", "Supprimer toutes les données")
    val wipeSubtitle = t(
        "Leaves the journal empty on this phone. Bobbin Pro stays.",
        "Deja el diario vacío en este teléfono. Bobbin Pro se conserva.",
        "Deixa o diário vazio neste telefone. O Bobbin Pro continua.",
        "Leert das Journal auf diesem Handy. Bobbin Pro bleibt.",
        "Vide le journal sur ce téléphone. Bobbin Pro est conservé.",
    )

    // --- 14. Bobbin Pro ----------------------------------------------------------------------

    val proTitle = t("Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro", "Bobbin Pro")
    val proOnce = t(
        "One-time purchase, no subscription.",
        "Compra única, sin suscripción.",
        "Compra única, sem assinatura.",
        "Einmalkauf, kein Abo.",
        "Achat unique, sans abonnement.",
    )
    val proCovers = t("Seven more covers", "Siete portadas más", "Mais sete capas", "Sieben weitere Umschläge", "Sept couvertures de plus")
    val proPapers = t("Lined, grid and blank paper", "Rayado, cuadrícula y liso", "Pautado, quadriculado e liso", "Liniert, kariert und blanko", "Ligné, quadrillé et uni")
    val proTrackers = t("More than one tracker", "Más de un seguimiento", "Mais de um tracker", "Mehr als ein Tracker", "Plus d'un suivi")
    val proMonthWidget = t("The month widget", "El widget del mes", "O widget do mês", "Das Monats-Widget", "Le widget du mois")
    val proLockWidget = t(
        "The lock screen widget",
        "El widget de la pantalla de bloqueo",
        "O widget da tela de bloqueio",
        "Das Sperrbildschirm-Widget",
        "Le widget de l'écran verrouillé",
    )
    val proFree = t(
        "The whole method is free, and it will stay that way.",
        "El método entero es gratis, y lo seguirá siendo.",
        "O método inteiro é grátis, e vai continuar assim.",
        "Die ganze Methode ist kostenlos und bleibt es.",
        "Toute la méthode est gratuite, et le restera.",
    )
    fun buy(price: String) = t("Buy for $price", "Comprar por $price", "Comprar por $price", "Für $price kaufen", "Acheter pour $price")
    val restore = t("Restore", "Restaurar", "Restaurar", "Wiederherstellen", "Restaurer")
    val storeUnavailable = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora.",
        "A loja não está disponível agora.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
    )
    val buyFailed = t(
        "The purchase could not be completed.",
        "No se ha podido completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
    )

    // --- 15. Importar, exportar y borrar -------------------------------------------------------

    val importTitle = t("Import backup", "Importar copia", "Importar cópia", "Kopie importieren", "Importer une copie")

    private fun importAddedPhrase(n: Int): String = if (isSingular(n)) {
        t("1 new entry", "1 entrada nueva", "1 entrada nova", "1 neuer Eintrag", "1 nouvelle entrée")
    } else {
        t("$n new entries", "$n entradas nuevas", "$n entradas novas", "$n neue Einträge", "$n nouvelles entrées")
    }

    private fun importUpdatedPhrase(n: Int): String = if (isSingular(n)) {
        t(
            "1 newer than yours",
            "1 más reciente que las tuyas",
            "1 mais recente que as suas",
            "1, der neuer ist als deine",
            "1 plus récente que les tiennes",
        )
    } else {
        t(
            "$n newer than yours",
            "$n más recientes que las tuyas",
            "$n mais recentes que as suas",
            "$n, die neuer sind als deine",
            "$n plus récentes que les tiennes",
        )
    }

    private fun importSamePhrase(n: Int): String = if (isSingular(n)) {
        t("1 was already here", "1 ya estaba", "1 já estava aqui", "1 war schon da", "1 était déjà là")
    } else {
        t("$n were already here", "$n ya estaban", "$n já estavam aqui", "$n waren schon da", "$n étaient déjà là")
    }

    /**
     * [added] and [updated] are dropped from the sentence when they are 0; when [same] is 0 the
     * sentence ends right after them, before "Nothing gets deleted." (docs/textos.md 15).
     */
    fun importSummary(added: Int, updated: Int, same: Int): String {
        val parts = listOfNotNull(
            added.takeIf { it > 0 }?.let { importAddedPhrase(it) },
            updated.takeIf { it > 0 }?.let { importUpdatedPhrase(it) },
        )
        val brings = if (parts.isEmpty()) {
            t("brings nothing new", "no trae nada nuevo", "não traz nada novo", "bringt nichts Neues", "n'apporte rien de nouveau")
        } else {
            val and = t(" and ", " y ", " e ", " und ", " et ")
            t("brings ", "trae ", "traz ", "bringt ", "apporte ") + parts.joinToString(and)
        }
        val tail = if (same > 0) {
            val same2 = importSamePhrase(same)
            t("; $same2.", "; $same2.", "; $same2.", "; $same2.", " ; $same2.")
        } else {
            "."
        }
        return t(
            "The backup $brings$tail Nothing gets deleted.",
            "La copia $brings$tail No se borra nada.",
            "A cópia $brings$tail Nada é apagado.",
            "Die Sicherung $brings$tail Es wird nichts gelöscht.",
            "La copie $brings$tail Rien n'est supprimé.",
        )
    }

    val importAction = t("Import", "Importar", "Importar", "Importieren", "Importer")

    fun importDone(n: Int): String = if (n == 0) {
        t(
            "Journal up to date: there was nothing to change.",
            "Diario al día: no había nada que cambiar.",
            "Diário em dia: não havia nada para mudar.",
            "Journal aktuell: es gab nichts zu ändern.",
            "Journal à jour : il n'y avait rien à changer.",
        )
    } else {
        val c = wordChange(n)
        t(
            "Journal up to date: $n $c.",
            "Diario al día: $n $c.",
            "Diário em dia: $n $c.",
            "Journal aktuell: $n $c.",
            "Journal à jour : $n $c.",
        )
    }

    val importFailedTitle = t("Couldn't import", "No se ha podido importar", "Não foi possível importar", "Import fehlgeschlagen", "Échec de l'import")
    val importNotBackup = t(
        "That file is not a Bobbin backup.",
        "Ese fichero no es una copia de Bobbin.",
        "Esse arquivo não é uma cópia do Bobbin.",
        "Diese Datei ist keine Sicherung von Bobbin.",
        "Ce fichier n'est pas une copie de Bobbin.",
    )
    val importDamaged = t(
        "The backup is incomplete or damaged. Your journal wasn't touched.",
        "La copia está incompleta o dañada. Tu diario no se ha tocado.",
        "A cópia está incompleta ou danificada. Seu diário não foi alterado.",
        "Die Sicherung ist unvollständig oder beschädigt. Dein Journal wurde nicht verändert.",
        "La copie est incomplète ou endommagée. Ton journal n'a pas été touché.",
    )
    val importTooNew = t(
        "This backup is from a newer version of Bobbin. Update the app and try again.",
        "Esta copia es de una versión más nueva de Bobbin. Actualiza la app y vuelve a probar.",
        "Esta cópia é de uma versão mais nova do Bobbin. Atualize o app e tente de novo.",
        "Diese Sicherung stammt aus einer neueren Version von Bobbin. Aktualisiere die App und versuch es erneut.",
        "Cette copie vient d'une version plus récente de Bobbin. Mets à jour l'app et réessaie.",
    )
    val importEmpty = t(
        "The backup has no entries or collections.",
        "La copia no tiene entradas ni colecciones.",
        "A cópia não tem entradas nem coleções.",
        "Die Sicherung enthält keine Einträge und keine Sammlungen.",
        "La copie ne contient ni entrées ni collections.",
    )
    fun importIsSibling(app: String) = t(
        "This is a backup from $app. You'll be able to bring its entries in a future version.",
        "Es una copia de $app. Podrás traer sus entradas en una próxima versión.",
        "Isso é uma cópia do $app. Você vai poder trazer as entradas dele numa próxima versão.",
        "Das ist eine Sicherung von $app. Du kannst ihre Einträge in einer späteren Version übernehmen.",
        "C'est une copie de $app. Tu pourras importer ses entrées dans une prochaine version.",
    )
    val exportFailed = t(
        "Couldn't save the backup.",
        "No se ha podido guardar la copia.",
        "Não foi possível salvar a cópia.",
        "Die Sicherung konnte nicht gespeichert werden.",
        "Impossible d'enregistrer la copie.",
    )
    val wipeTitle = t("Delete the whole journal?", "¿Borrar todo el diario?", "Apagar todo o diário?", "Das ganze Journal löschen?", "Supprimer tout le journal ?")
    val wipeText = t(
        "All entries, collections and settings on this phone are deleted. Backups you exported are not touched, and Bobbin Pro stays.",
        "Se borran todas las entradas, colecciones y ajustes de este teléfono. Las copias que hayas exportado no se tocan, y Bobbin Pro se conserva.",
        "Todas as entradas, coleções e ajustes deste telefone são apagados. As cópias que você exportou não são alteradas, e o Bobbin Pro continua.",
        "Alle Einträge, Sammlungen und Einstellungen auf diesem Handy werden gelöscht. Exportierte Sicherungen bleiben unberührt, und Bobbin Pro bleibt.",
        "Toutes les entrées, collections et réglages de ce téléphone sont supprimés. Les copies que tu as exportées ne sont pas touchées, et Bobbin Pro est conservé.",
    )
    val wipeContinue = t("Continue", "Continuar", "Continuar", "Weiter", "Continuer")
    fun wipeConfirmTitle(word: String) = t(
        "Type the word $word to confirm",
        "Escribe la palabra $word para confirmar",
        "Digite a palavra $word para confirmar",
        "Gib das Wort $word ein, um zu bestätigen",
        "Écris le mot $word pour confirmer",
    )
    val wipeWord = t("delete", "borrar", "apagar", "löschen", "effacer")
    val wipeAction = t("Delete everything", "Borrar todo", "Apagar tudo", "Alles löschen", "Tout supprimer")

    // --- 16. Bloqueo y avisos de carga ---------------------------------------------------------

    val unlock = t("Unlock", "Desbloquear", "Desbloquear", "Entsperren", "Déverrouiller")
    val lockPromptTitle = t("Open Bobbin", "Abrir Bobbin", "Abrir Bobbin", "Bobbin öffnen", "Ouvrir Bobbin")
    val lockPromptSubtitle = t(
        "Your journal is locked",
        "Tu diario está bloqueado",
        "Seu diário está bloqueado",
        "Dein Journal ist gesperrt",
        "Ton journal est verrouillé",
    )
    val updateNeeded = t(
        "This journal was written with a newer version of Bobbin. Update the app to open it; nothing was touched.",
        "Este diario se escribió con una versión más nueva de Bobbin. Actualiza la app para abrirlo; no se ha tocado nada.",
        "Este diário foi escrito com uma versão mais nova do Bobbin. Atualize o app para abri-lo; nada foi alterado.",
        "Dieses Journal wurde mit einer neueren Version von Bobbin geschrieben. Aktualisiere die App, um es zu öffnen; nichts wurde verändert.",
        "Ce journal a été écrit avec une version plus récente de Bobbin. Mets à jour l'app pour l'ouvrir ; rien n'a été touché.",
    )
    val openStore = t("Open the store", "Abrir la tienda", "Abrir a loja", "Store öffnen", "Ouvrir la boutique")
    val migrationFailed = t(
        "Couldn't prepare the journal for this version. It is intact and nothing was touched.",
        "No se ha podido preparar el diario para esta versión. Sigue intacto y no se ha tocado nada.",
        "Não foi possível preparar o diário para esta versão. Ele continua intacto e nada foi alterado.",
        "Das Journal konnte nicht für diese Version vorbereitet werden. Es ist unversehrt, nichts wurde verändert.",
        "Impossible de préparer le journal pour cette version. Il est intact et rien n'a été touché.",
    )

    // --- 17. Compartir ----------------------------------------------------------------------

    val shareImage = t("Share as image", "Compartir como imagen", "Compartilhar como imagem", "Als Bild teilen", "Partager en image")
    val shareText = t("Share as text", "Compartir como texto", "Compartilhar como texto", "Als Text teilen", "Partager en texte")
    fun sharePages(n: Int) = "$n ${wordImage(n)}"
    fun pageOf(i: Int, n: Int) = "$i ${ofConnector()} $n"

    // --- 18. Notificacion --------------------------------------------------------------------

    val reminderTitle = t(
        "A moment to go over the day",
        "Un momento para repasar el día",
        "Um momento para repassar o dia",
        "Ein Moment, um den Tag durchzugehen",
        "Un moment pour relire ta journée",
    )
    val reminderBody = t(
        "Read today back and decide what comes next.",
        "Relee lo de hoy y decide qué sigue.",
        "Releia o dia de hoje e decida o que vem depois.",
        "Lies den heutigen Tag nach und entscheide, wie es weitergeht.",
        "Relis ta journée et décide de la suite.",
    )
    val reminderChannel = t(
        "Reminder to go over the day",
        "Recordatorio para repasar el día",
        "Lembrete para repassar o dia",
        "Erinnerung, den Tag durchzugehen",
        "Rappel pour relire ta journée",
    )

    // --- 19. Widgets -------------------------------------------------------------------------

    fun widgetDate(d: LocalDate): String {
        val w = weekdayShort()[d.dayOfWeek.ordinal]
        val m = monthShort()[d.month.ordinal].removeSuffix(".")
        return "$w ${d.day} $m".uppercase()
    }

    fun widgetWeekday(d: LocalDate): String = weekdayNames()[d.dayOfWeek.ordinal]
    fun widgetMonth(d: LocalDate): String = monthNames()[d.month.ordinal]
    fun widgetMonthName(month: YearMonth): String = monthName(month)

    fun widgetOpen(n: Int) =
        word(n, "open" to "open", "abierta" to "abiertas", "aberta" to "abertas", "offen" to "offen", "ouverte" to "ouvertes")

    fun widgetDone(n: Int) =
        word(n, "done" to "done", "hecha" to "hechas", "feita" to "feitas", "erledigt" to "erledigt", "faite" to "faites")

    fun widgetEvents(n: Int) = word(
        n,
        "event" to "events",
        "evento" to "eventos",
        "evento" to "eventos",
        "Ereignis" to "Ereignisse",
        "événement" to "événements",
    )

    val widgetReview = t("To review", "Por revisar", "Para revisar", "Durchsehen", "À revoir")
    val widgetUnlock = t("Tap to turn it on", "Toca para activarlo", "Toque para ativar", "Tippen zum Aktivieren", "Touche pour l'activer")
    val pickerTodayName = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val pickerTodayDescription = t(
        "Today's open, done and events, without any text",
        "Abiertas, hechas y eventos de hoy, sin ningún texto",
        "Abertas, feitas e eventos de hoje, sem nenhum texto",
        "Offene und erledigte Aufgaben und Ereignisse von heute, ohne Text",
        "Ouvertes, faites et événements du jour, sans aucun texte",
    )
    val pickerMonthName = t("The month", "El mes", "O mês", "Der Monat", "Le mois")
    val pickerMonthDescription = t(
        "One dot for each day with entries",
        "Un punto por cada día con entradas",
        "Um ponto para cada dia com entradas",
        "Ein Punkt für jeden Tag mit Einträgen",
        "Un point pour chaque jour avec des entrées",
    )
    val pickerLockName = t("Open tasks", "Tareas abiertas", "Tarefas abertas", "Offene Aufgaben", "Tâches ouvertes")
    val pickerLockDescription = t(
        "Today's open tasks on the lock screen",
        "Las tareas abiertas de hoy en la pantalla de bloqueo",
        "As tarefas abertas de hoje na tela de bloqueio",
        "Die offenen Aufgaben von heute auf dem Sperrbildschirm",
        "Les tâches ouvertes du jour sur l'écran verrouillé",
    )

    // --- 20. Accesibilidad -------------------------------------------------------------------

    /** Signifiers, if any, in priority/inspiration/explore order; lowercase except in German. */
    fun entryDescription(bullet: Bullet, status: TaskStatus, signifiers: Set<Signifier>, text: String): String {
        val glyph = glyphName(bullet, status)
        val order = listOf(Signifier.PRIORITY, Signifier.INSPIRATION, Signifier.EXPLORE)
        val names = order.filter { it in signifiers }.map { signifierCase(it) }
        val prefix = if (names.isEmpty()) glyph else "$glyph, ${names.joinToString(", ")}"
        return "$prefix: $text"
    }

    private fun signifierCase(s: Signifier): String {
        val name = when (s) {
            Signifier.PRIORITY -> signifierPriority
            Signifier.INSPIRATION -> signifierInspiration
            Signifier.EXPLORE -> signifierExplore
        }
        return if (lang == "de") name else name.lowercase()
    }

    fun a11yWentTo(destination: String) = t(
        "Moved to $destination",
        "Fue a $destination",
        "Foi para $destination",
        "Verschoben nach $destination",
        "Partie vers $destination",
    )
    val a11yComplete = t("Complete", "Completar", "Concluir", "Erledigen", "Terminer")
    val a11yReopen = t("Reopen", "Reabrir", "Reabrir", "Wieder öffnen", "Rouvrir")
    val a11yMoveUp = t("Move up", "Subir", "Subir", "Nach oben", "Monter")
    val a11yMoveDown = t("Move down", "Bajar", "Descer", "Nach unten", "Descendre")
    val a11yCapture = t("New entry", "Nueva entrada", "Nova entrada", "Neuer Eintrag", "Nouvelle entrée")

    fun a11yDayRow(day: Int, weekday: String, n: Int): String {
        val entries = if (n == 0) {
            t("no entries", "sin entradas", "sem entradas", "keine Einträge", "aucune entrée")
        } else {
            "$n ${wordEntry(n)}"
        }
        return t("$day, $weekday, $entries", "$day, $weekday, $entries", "$day, $weekday, $entries", "$day., $weekday, $entries", "$day, $weekday, $entries")
    }

    fun a11yTrackerCell(row: String, date: LocalDate, marked: Boolean): String {
        val when2 = shortDate(date)
        val state = if (marked) {
            t("marked", "marcada", "marcada", "markiert", "cochée")
        } else {
            t("not marked", "sin marcar", "sem marcar", "nicht markiert", "pas cochée")
        }
        return "$row, $when2, $state"
    }

    fun a11yTrackerMarked(days: List<Int>): String {
        if (days.isEmpty()) return t("No day marked", "Ningún día marcado", "Nenhum dia marcado", "Kein Tag markiert", "Aucun jour coché")
        val list = joinAnd(days.map { it.toString() })
        return t("Marked: $list", "Marcados: $list", "Marcados: $list", "Markiert: $list", "Cochés : $list")
    }

    // --- 23. v1.1 -----------------------------------------------------------------------------

    val continueCollection = t(
        "Continue in a new collection",
        "Continuar en una colección nueva",
        "Continuar em uma nova coleção",
        "In einer neuen Sammlung fortsetzen",
        "Continuer dans une nouvelle collection",
    )
    val continueTitle = t("Continue in...", "Continuar en...", "Continuar em...", "Fortsetzen in...", "Continuer dans...")
    val create = t("Create", "Crear", "Criar", "Erstellen", "Créer")
    fun threadFrom(title: String) = t(
        "Continued from $title", "Viene de $title", "Vem de $title", "Fortsetzung von $title", "Suite de $title",
    )
    fun threadNext(title: String) = t(
        "Continues in $title", "Sigue en $title", "Continua em $title", "Weiter in $title", "Continue dans $title",
    )
    val newNotebookRow = t("New notebook", "Nuevo cuaderno", "Novo caderno", "Neues Notizbuch", "Nouveau carnet")
    val newNotebookSub = t(
        "Go through collections and tasks to start another",
        "Repasa colecciones y tareas para empezar otro",
        "Repasse coleções e tarefas para começar outro",
        "Geh Sammlungen und Aufgaben durch, um ein neues zu beginnen",
        "Passe en revue collections et tâches pour en commencer un autre",
    )
    val notebookKeep = t("Keep", "Mantener", "Manter", "Behalten", "Garder")
    val notebookContinue = t("Continue in a new one", "Continuar en una nueva", "Continuar em uma nova", "In einer neuen fortsetzen", "Continuer dans une nouvelle")
    val notebookArchive = t("Archive", "Archivar", "Arquivar", "Archivieren", "Archiver")

    fun notebookDone(d: LocalDate): String {
        val date = dayMonthYear(d)
        return t(
            "New notebook from $date.",
            "Cuaderno nuevo desde el $date.",
            "Caderno novo desde $date.",
            "Neues Notizbuch seit dem $date.",
            "Nouveau carnet depuis le $date.",
        )
    }

    fun notebookUntil(d: LocalDate): String {
        val date = dayMonthYear(d)
        return t(
            "Notebook until $date",
            "Cuaderno hasta el $date",
            "Caderno até $date",
            "Notizbuch bis $date",
            "Carnet jusqu'au $date",
        )
    }

    /** Uses abbrDate: "14 mar"; in English, "Mar 14" (docs/textos.md 23). */
    fun scheduleFor(d: LocalDate): String {
        val date = abbrDate(d)
        return t("Schedule for $date", "Programar para el $date", "Agendar para $date", "Für $date einplanen", "Planifier pour le $date")
    }

    val tileLabel = t("Note in Bobbin", "Anotar en Bobbin", "Anotar no Bobbin", "In Bobbin notieren", "Noter dans Bobbin")
    val captureSaved = t("Noted in Bobbin.", "Anotado en Bobbin.", "Anotado no Bobbin.", "In Bobbin notiert.", "Noté dans Bobbin.")
    val captureEmpty = t(
        "There was nothing to note.",
        "No había nada que anotar.",
        "Não havia nada para anotar.",
        "Es gab nichts zu notieren.",
        "Il n'y avait rien à noter.",
    )
    val captureNeedsPro = t(
        "Noting from outside the app is part of Bobbin Pro. Open the app to see it.",
        "Anotar desde fuera de la app es de Bobbin Pro. Abre la app para verlo.",
        "Anotar de fora do app é do Bobbin Pro. Abra o app para ver.",
        "Von außerhalb der App notieren gehört zu Bobbin Pro. Öffne die App, um es zu sehen.",
        "Noter depuis l'extérieur de l'app fait partie de Bobbin Pro. Ouvre l'app pour le voir.",
    )
    val proBook = t("The book as a PDF", "El libro en PDF", "O livro em PDF", "Das Buch als PDF", "Le livre en PDF")

    fun proCapture(onIos: Boolean): String = if (onIos) {
        t(
            "Note with Siri and Shortcuts",
            "Anotar desde Siri y Atajos",
            "Anotar pela Siri e Atalhos",
            "Mit Siri und Kurzbefehlen notieren",
            "Noter avec Siri et Raccourcis",
        )
    } else {
        t(
            "Note from Quick Settings",
            "Anotar desde Ajustes rápidos",
            "Anotar pelas Configurações rápidas",
            "Aus den Schnelleinstellungen notieren",
            "Noter depuis les réglages rapides",
        )
    }

    val bookRow = t("Book as PDF", "Libro en PDF", "Livro em PDF", "Buch als PDF", "Livre en PDF")
    val bookSub = t(
        "The notebook laid out, to print or keep",
        "El cuaderno maquetado, para imprimir o guardar",
        "O caderno diagramado, para imprimir ou guardar",
        "Das Notizbuch gesetzt, zum Drucken oder Aufbewahren",
        "Le carnet mis en page, à imprimer ou à garder",
    )
    val bookAction = t(
        "Make the book as a PDF",
        "Hacer el libro en PDF",
        "Fazer o livro em PDF",
        "Das Buch als PDF erstellen",
        "Faire le livre en PDF",
    )

    fun bookRange(from: YearMonth, to: YearMonth): String {
        val a = monthYear(from)
        val b = monthYear(to)
        return t("$a to $b", "$a a $b", "$a a $b", "$a bis $b", "$a à $b")
    }

    fun bookMaking(n: Int, total: Int): String {
        val of = ofConnector()
        return t(
            "Laying out the book: page $n $of $total",
            "Maquetando el libro: página $n $of $total",
            "Diagramando o livro: página $n $of $total",
            "Buch wird gesetzt: Seite $n $of $total",
            "Mise en page du livre : page $n $of $total",
        )
    }

    val bookFailed = t(
        "Couldn't create the book.",
        "No se ha podido crear el libro.",
        "Não foi possível criar o livro.",
        "Das Buch konnte nicht erstellt werden.",
        "Impossible de créer le livre.",
    )

    // Natural language dates (v1.1, docs/tecnico.md 12.4): the single-word vocabulary that
    // model/DateHint.kt reads; the multi-word recognition patterns of docs/textos.md 23 stay in
    // that document for DateHint.kt (#66) to consult directly.
    fun dateWordToday() = t("today", "hoy", "hoje", "heute", "aujourd'hui")
    fun dateWordTomorrow() = t("tomorrow", "mañana", "amanhã", "morgen", "demain")
    fun dateWordDayAfterTomorrow() = t("day after tomorrow", "pasado mañana", "depois de amanhã", "übermorgen", "après-demain")

    // Question bank (docs/tecnico.md 12.3): S.question(i) with i in 0 until QUESTION_COUNT.

    private val QUESTIONS_EN = listOf(
        "What wouldn't you migrate if you had to rewrite it by hand?",
        "Which task has been waiting the longest?",
        "What did you do that you never wrote down?",
        "Which event from these days do you want to remember?",
        "Which note surprised you when you read it again?",
        "What would you cross out today without regret?",
        "What was worth the time it took?",
        "What did you say yes to that you'd say no to now?",
        "Which task turned out smaller than you thought?",
        "What will you do first tomorrow?",
        "What deserves a collection of its own?",
        "Which idea keeps coming back?",
        "Who shows up most in these pages?",
        "What did you finish that you started long ago?",
        "What would you do differently with the same days?",
        "What did you learn that you want to keep?",
        "Which task is really several tasks?",
        "What can wait?",
        "What went better than you expected?",
        "What are you glad you wrote down?",
        "What did you notice only when reading it again?",
        "What did you leave unfinished on purpose?",
        "Which place do you remember from these days?",
        "Which conversation would you write down in full?",
        "What small thing made a day better?",
        "Which task do you keep migrating, and why?",
        "What would you like more time for?",
        "What would you remove from your list if nobody asked?",
        "What did you do for someone else?",
        "What did someone do for you?",
        "What do you want to repeat?",
        "What was the best ordinary moment?",
        "Which question is still open?",
        "What did you read, watch or hear that deserves a note?",
        "What did you decide, and what came of it?",
        "What did you start without planning it?",
        "Which task would you hand to someone else?",
        "What did you put off that turned out fine?",
        "What is missing from these pages?",
        "What would you explore on a free afternoon?",
        "Which priority changed along the way?",
        "What did you fix, at home or elsewhere?",
        "Which day would you read again first?",
        "What did you buy that was worth it?",
        "What did you cook or eat that is worth remembering?",
        "Which habit showed up without you noticing?",
        "What would make the coming days simpler?",
        "What did you say no to, and how did it go?",
        "Which task could you do in five minutes?",
        "What surprised you about how you spent your time?",
        "What do you want to remember from these days in a year?",
        "What was harder than it looked?",
        "Where did you go for the first time?",
        "What did you make with your hands?",
        "Which plan changed, and what replaced it?",
        "Which piece of news marked these days?",
        "What can you set aside?",
        "What were you waiting for, and did it arrive?",
        "What would you write on the first line of the next page?",
        "How would you sum up these days in one line?",
    )

    private val QUESTIONS_ES = listOf(
        "¿Qué no migrarías si tuvieras que reescribirlo a mano?",
        "¿Qué tarea lleva más tiempo esperando?",
        "¿Qué hiciste que no llegaste a apuntar?",
        "¿Qué evento de estos días quieres recordar?",
        "¿Qué nota te ha sorprendido al releerla?",
        "¿Qué tacharías hoy sin pena?",
        "¿Qué mereció el tiempo que le dedicaste?",
        "¿A qué dijiste que sí y ahora dirías que no?",
        "¿Qué tarea resultó más pequeña de lo que pensabas?",
        "¿Qué es lo primero que harás mañana?",
        "¿Qué merece una colección propia?",
        "¿Qué idea vuelve una y otra vez?",
        "¿Quién aparece más en estas páginas?",
        "¿Qué terminaste que habías empezado hace tiempo?",
        "¿Qué harías distinto con los mismos días?",
        "¿Qué aprendiste que quieras conservar?",
        "¿Qué tarea son en realidad varias?",
        "¿Qué puede esperar?",
        "¿Qué salió mejor de lo que esperabas?",
        "¿Qué te alegra haber apuntado?",
        "¿Qué has visto solo al releerlo?",
        "¿Qué dejaste a medias a propósito?",
        "¿Qué lugar recuerdas de estos días?",
        "¿Qué conversación apuntarías entera?",
        "¿Qué cosa pequeña mejoró un día?",
        "¿Qué tarea sigues migrando, y por qué?",
        "¿Para qué te gustaría tener más tiempo?",
        "¿Qué quitarías de tu lista si nadie te lo pidiera?",
        "¿Qué hiciste por otra persona?",
        "¿Qué hizo alguien por ti?",
        "¿Qué quieres repetir?",
        "¿Cuál fue el mejor momento corriente?",
        "¿Qué pregunta sigue abierta?",
        "¿Qué leíste, viste u oíste que merezca una nota?",
        "¿Qué decidiste, y qué salió de ello?",
        "¿Qué empezaste sin planearlo?",
        "¿Qué tarea le darías a otra persona?",
        "¿Qué aplazaste que al final salió bien?",
        "¿Qué falta en estas páginas?",
        "¿Qué explorarías en una tarde libre?",
        "¿Qué prioridad cambió por el camino?",
        "¿Qué arreglaste, en casa o fuera?",
        "¿Qué día releerías primero?",
        "¿Qué compraste que mereció la pena?",
        "¿Qué cocinaste o comiste que valga la pena recordar?",
        "¿Qué costumbre apareció sin que te dieras cuenta?",
        "¿Qué haría más sencillos los próximos días?",
        "¿A qué dijiste que no, y cómo fue?",
        "¿Qué tarea podrías hacer en cinco minutos?",
        "¿Qué te sorprendió de cómo pasaste el tiempo?",
        "¿Qué quieres recordar de estos días dentro de un año?",
        "¿Qué fue más difícil de lo que parecía?",
        "¿Adónde fuiste por primera vez?",
        "¿Qué hiciste con las manos?",
        "¿Qué plan cambió, y qué lo sustituyó?",
        "¿Qué noticia marcó estos días?",
        "¿Qué puedes dejar de lado?",
        "¿Qué esperabas, y llegó?",
        "¿Qué escribirías en la primera línea de la página siguiente?",
        "¿Cómo resumirías estos días en una línea?",
    )

    private val QUESTIONS_PT = listOf(
        "O que você não migraria se tivesse que reescrever à mão?",
        "Qual tarefa está esperando há mais tempo?",
        "O que você fez e não chegou a anotar?",
        "Qual evento destes dias você quer lembrar?",
        "Qual nota te surpreendeu ao reler?",
        "O que você riscaria hoje sem pena?",
        "O que valeu o tempo que você dedicou?",
        "Para o que você disse sim e agora diria não?",
        "Qual tarefa foi menor do que você pensava?",
        "O que você vai fazer primeiro amanhã?",
        "O que merece uma coleção própria?",
        "Que ideia volta sempre?",
        "Quem aparece mais nestas páginas?",
        "O que você terminou que tinha começado há tempos?",
        "O que você faria diferente com os mesmos dias?",
        "O que você aprendeu e quer guardar?",
        "Qual tarefa é na verdade várias?",
        "O que pode esperar?",
        "O que saiu melhor do que você esperava?",
        "O que você gosta de ter anotado?",
        "O que você só percebeu ao reler?",
        "O que você deixou pela metade de propósito?",
        "Que lugar você lembra destes dias?",
        "Que conversa você anotaria inteira?",
        "Que coisa pequena melhorou um dia?",
        "Qual tarefa você continua migrando, e por quê?",
        "Para que você gostaria de ter mais tempo?",
        "O que você tiraria da sua lista se ninguém pedisse?",
        "O que você fez por outra pessoa?",
        "O que alguém fez por você?",
        "O que você quer repetir?",
        "Qual foi o melhor momento comum?",
        "Que pergunta continua aberta?",
        "O que você leu, viu ou ouviu que merece uma nota?",
        "O que você decidiu, e o que saiu disso?",
        "O que você começou sem planejar?",
        "Qual tarefa você passaria para outra pessoa?",
        "O que você adiou e no fim deu certo?",
        "O que falta nestas páginas?",
        "O que você exploraria numa tarde livre?",
        "Que prioridade mudou pelo caminho?",
        "O que você consertou, em casa ou fora?",
        "Que dia você releria primeiro?",
        "O que você comprou que valeu a pena?",
        "O que você cozinhou ou comeu que vale lembrar?",
        "Que costume apareceu sem você perceber?",
        "O que deixaria os próximos dias mais simples?",
        "Para o que você disse não, e como foi?",
        "Qual tarefa você poderia fazer em cinco minutos?",
        "O que te surpreendeu em como você passou o tempo?",
        "O que você quer lembrar destes dias daqui a um ano?",
        "O que foi mais difícil do que parecia?",
        "Aonde você foi pela primeira vez?",
        "O que você fez com as mãos?",
        "Que plano mudou, e o que o substituiu?",
        "Que notícia marcou estes dias?",
        "O que você pode deixar de lado?",
        "O que você esperava, e chegou?",
        "O que você escreveria na primeira linha da próxima página?",
        "Como você resumiria estes dias em uma linha?",
    )

    private val QUESTIONS_DE = listOf(
        "Was würdest du nicht migrieren, wenn du es von Hand neu schreiben müsstest?",
        "Welche Aufgabe wartet schon am längsten?",
        "Was hast du getan, ohne es aufzuschreiben?",
        "An welches Ereignis dieser Tage willst du dich erinnern?",
        "Welche Notiz hat dich beim Nachlesen überrascht?",
        "Was würdest du heute ohne Bedauern durchstreichen?",
        "Was war die Zeit wert, die es gekostet hat?",
        "Wozu hast du Ja gesagt, wozu du jetzt Nein sagen würdest?",
        "Welche Aufgabe war kleiner als gedacht?",
        "Was machst du morgen als Erstes?",
        "Was verdient eine eigene Sammlung?",
        "Welche Idee kommt immer wieder?",
        "Wer taucht auf diesen Seiten am häufigsten auf?",
        "Was hast du beendet, das du vor langer Zeit angefangen hattest?",
        "Was würdest du mit denselben Tagen anders machen?",
        "Was hast du gelernt, das du behalten willst?",
        "Welche Aufgabe sind eigentlich mehrere?",
        "Was kann warten?",
        "Was lief besser als erwartet?",
        "Worüber bist du froh, es aufgeschrieben zu haben?",
        "Was ist dir erst beim Nachlesen aufgefallen?",
        "Was hast du absichtlich unfertig gelassen?",
        "An welchen Ort dieser Tage erinnerst du dich?",
        "Welches Gespräch würdest du ganz aufschreiben?",
        "Welche Kleinigkeit hat einen Tag besser gemacht?",
        "Welche Aufgabe migrierst du immer wieder, und warum?",
        "Wofür hättest du gern mehr Zeit?",
        "Was würdest du von deiner Liste streichen, wenn niemand danach fragte?",
        "Was hast du für jemand anderen getan?",
        "Was hat jemand für dich getan?",
        "Was willst du wiederholen?",
        "Was war der schönste gewöhnliche Moment?",
        "Welche Frage ist noch offen?",
        "Was hast du gelesen, gesehen oder gehört, das eine Notiz verdient?",
        "Was hast du entschieden, und was ist daraus geworden?",
        "Was hast du ungeplant angefangen?",
        "Welche Aufgabe würdest du jemand anderem geben?",
        "Was hast du aufgeschoben, und es ging trotzdem gut?",
        "Was fehlt auf diesen Seiten?",
        "Was würdest du an einem freien Nachmittag erkunden?",
        "Welche Priorität hat sich unterwegs geändert?",
        "Was hast du repariert, zu Hause oder anderswo?",
        "Welchen Tag würdest du zuerst nachlesen?",
        "Was hast du gekauft, das sich gelohnt hat?",
        "Was hast du gekocht oder gegessen, das sich zu merken lohnt?",
        "Welche Gewohnheit ist unbemerkt aufgetaucht?",
        "Was würde die nächsten Tage einfacher machen?",
        "Wozu hast du Nein gesagt, und wie war es?",
        "Welche Aufgabe könntest du in fünf Minuten erledigen?",
        "Was hat dich daran überrascht, wie du deine Zeit verbracht hast?",
        "Woran willst du dich in einem Jahr von diesen Tagen erinnern?",
        "Was war schwieriger, als es aussah?",
        "Wo warst du zum ersten Mal?",
        "Was hast du mit den Händen gemacht?",
        "Welcher Plan hat sich geändert, und was kam stattdessen?",
        "Welche Nachricht hat diese Tage geprägt?",
        "Was kannst du beiseitelegen?",
        "Worauf hast du gewartet, und ist es gekommen?",
        "Was würdest du in die erste Zeile der nächsten Seite schreiben?",
        "Wie würdest du diese Tage in einer Zeile zusammenfassen?",
    )

    private val QUESTIONS_FR = listOf(
        "Que ne migrerais-tu pas s'il fallait le réécrire à la main ?",
        "Quelle tâche attend depuis le plus longtemps ?",
        "Qu'as-tu fait sans jamais le noter ?",
        "De quel événement de ces jours veux-tu te souvenir ?",
        "Quelle note te surprend en la relisant ?",
        "Que barrerais-tu aujourd'hui sans regret ?",
        "Qu'est-ce qui valait le temps que tu y as mis ?",
        "À quoi as-tu dit oui et dirais-tu non maintenant ?",
        "Quelle tâche s'est révélée plus petite que prévu ?",
        "Que feras-tu en premier demain ?",
        "Qu'est-ce qui mérite sa propre collection ?",
        "Quelle idée revient sans cesse ?",
        "Qui apparaît le plus dans ces pages ?",
        "Qu'as-tu terminé que tu avais commencé il y a longtemps ?",
        "Que ferais-tu autrement avec les mêmes jours ?",
        "Qu'as-tu appris que tu veux garder ?",
        "Quelle tâche en cache en fait plusieurs ?",
        "Qu'est-ce qui peut attendre ?",
        "Qu'est-ce qui s'est mieux passé que prévu ?",
        "Qu'est-ce que tu as bien fait de noter ?",
        "Qu'as-tu remarqué seulement en relisant ?",
        "Qu'as-tu laissé inachevé exprès ?",
        "De quel lieu te souviens-tu ces jours-ci ?",
        "Quelle conversation noterais-tu en entier ?",
        "Quelle petite chose a rendu une journée meilleure ?",
        "Quelle tâche continues-tu à migrer, et pourquoi ?",
        "Pour quoi aimerais-tu avoir plus de temps ?",
        "Que retirerais-tu de ta liste si personne ne le demandait ?",
        "Qu'as-tu fait pour quelqu'un d'autre ?",
        "Qu'est-ce que quelqu'un a fait pour toi ?",
        "Que veux-tu refaire ?",
        "Quel a été le meilleur moment ordinaire ?",
        "Quelle question reste ouverte ?",
        "Qu'as-tu lu, vu ou entendu qui mérite une note ?",
        "Qu'as-tu décidé, et qu'en est-il sorti ?",
        "Qu'as-tu commencé sans le prévoir ?",
        "Quelle tâche confierais-tu à quelqu'un d'autre ?",
        "Qu'as-tu repoussé qui s'est finalement bien passé ?",
        "Que manque-t-il dans ces pages ?",
        "Qu'explorerais-tu pendant un après-midi libre ?",
        "Quelle priorité a changé en route ?",
        "Qu'as-tu réparé, chez toi ou ailleurs ?",
        "Quel jour relirais-tu en premier ?",
        "Qu'as-tu acheté qui en valait la peine ?",
        "Qu'as-tu cuisiné ou mangé qui vaut la peine d'être retenu ?",
        "Quelle habitude est apparue sans que tu t'en rendes compte ?",
        "Qu'est-ce qui rendrait les prochains jours plus simples ?",
        "À quoi as-tu dit non, et comment ça s'est passé ?",
        "Quelle tâche pourrais-tu faire en cinq minutes ?",
        "Qu'est-ce qui te surprend dans ta façon de passer le temps ?",
        "De quoi veux-tu te souvenir de ces jours dans un an ?",
        "Qu'est-ce qui a été plus difficile que prévu ?",
        "Quel endroit as-tu découvert ?",
        "Qu'as-tu fait de tes mains ?",
        "Quel plan a changé, et qu'est-ce qui l'a remplacé ?",
        "Quelle nouvelle a marqué ces jours ?",
        "Que peux-tu mettre de côté ?",
        "Qu'attendais-tu, et est-ce arrivé ?",
        "Qu'écrirais-tu sur la première ligne de la page suivante ?",
        "Comment résumerais-tu ces jours en une ligne ?",
    )

    /** [i] in 0 until QUESTION_COUNT (docs/tecnico.md 12.3); order matches docs/textos.md 23. */
    fun question(i: Int): String {
        val list = when (lang) {
            "es" -> QUESTIONS_ES
            "pt" -> QUESTIONS_PT
            "de" -> QUESTIONS_DE
            "fr" -> QUESTIONS_FR
            else -> QUESTIONS_EN
        }
        return list[i]
    }

    // --- 24. v1.2 ------------------------------------------------------------------------------

    val syncRow = t("Sync", "Sincronizar", "Sincronizar", "Synchronisieren", "Synchroniser")
    val syncOff = t("Off", "Apagada", "Desativada", "Aus", "Désactivée")
    val syncICloud = t("With iCloud Drive", "Con iCloud Drive", "Com o iCloud Drive", "Mit iCloud Drive", "Avec iCloud Drive")
    fun syncFile(name: String) = t(
        "With $name, in the folder you chose",
        "Con $name, en la carpeta que elegiste",
        "Com $name, na pasta que você escolheu",
        "Mit $name im Ordner, den du gewählt hast",
        "Avec $name, dans le dossier que tu as choisi",
    )
    fun syncLast(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t("Last time, $time", "Última vez, $time", "Última vez, $time", "Zuletzt um $time", "Dernière fois, $time")
    }
    val syncConflictTitle = t(
        "Both sides have changed",
        "Los dos lados han cambiado",
        "Os dois lados mudaram",
        "Beide Seiten haben sich geändert",
        "Les deux côtés ont changé",
    )
    val syncConflictText = t(
        "This phone and the other one have different changes since last time. Choose which one to keep, or merge them after seeing a summary.",
        "Este teléfono y el otro tienen cambios distintos desde la última vez. Elige con cuál quedarte, o júntalos tras ver un resumen.",
        "Este telefone e o outro têm alterações diferentes desde a última vez. Escolha qual manter, ou junte os dois depois de ver um resumo.",
        "Dieses Handy und das andere haben seit dem letzten Mal unterschiedliche Änderungen. Wähle, welches du behältst, oder führe sie nach einer Übersicht zusammen.",
        "Ce téléphone et l'autre ont des modifications différentes depuis la dernière fois. Choisis lequel garder, ou fusionne-les après avoir vu un résumé.",
    )
    val syncKeepThis = t("Keep this phone", "Usar este teléfono", "Manter este telefone", "Dieses Handy behalten", "Garder ce téléphone")
    val syncKeepOther = t("Keep the other", "Usar el otro", "Manter o outro", "Das andere behalten", "Garder l'autre")
    val syncMerge = t("Merge both", "Juntar los dos", "Juntar os dois", "Beide zusammenführen", "Fusionner les deux")
    val proSync = t(
        "Sync through your own cloud",
        "Sincronizar con tu propia nube",
        "Sincronizar pela sua própria nuvem",
        "Synchronisieren über deine eigene Cloud",
        "Synchroniser via ton propre cloud",
    )
    val proYearSummary = t("The year summary", "El resumen del año", "O resumo do ano", "Der Jahresrückblick", "Le bilan de l'année")
    val yearSummaryRow = t("Year summary", "Resumen del año", "Resumo do ano", "Jahresrückblick", "Bilan de l'année")
    fun yearSummaryTitle(year: Int) = t("Your $year", "Tu $year", "Seu $year", "Dein $year", "Ton $year")
    val summaryInspirations = t("Inspirations", "Inspiraciones", "Inspirações", "Inspirationen", "Inspirations")
    val summaryPriorities = t("Priorities done", "Prioridades hechas", "Prioridades feitas", "Erledigte Prioritäten", "Priorités faites")
    val summaryMostMigrated = t("Travelled the most", "Las que más viajaron", "As que mais viajaram", "Am weitesten gereist", "Les plus voyageuses")
    val summaryEmpty = t(
        "This year you marked no inspirations or priorities. Next year starts blank.",
        "Este año no marcaste inspiraciones ni prioridades. El que viene está en blanco.",
        "Este ano você não marcou inspirações nem prioridades. O próximo está em branco.",
        "Dieses Jahr hast du keine Inspirationen oder Prioritäten markiert. Das nächste ist noch leer.",
        "Cette année, tu n'as marqué ni inspirations ni priorités. La prochaine est vierge.",
    )
    val iconAndTheme = t("Icon and theme", "Icono y tema", "Ícone e tema", "Symbol und Thema", "Icône et thème")
    val iconNone = t("No icon", "Sin icono", "Sem ícone", "Kein Symbol", "Sans icône")
    val themeHint = t("Theme", "Tema", "Tema", "Thema", "Thème")
    val themeNone = t("No theme", "Sin tema", "Sem tema", "Ohne Thema", "Sans thème")

    private val ICON_ORDER = listOf("star", "heart", "book", "home", "work", "travel", "money", "health", "food", "music", "idea", "people")

    fun iconName(id: String): String {
        val names = t(
            "Star, Heart, Book, Home, Work, Travel, Money, Health, Food, Music, Idea, People",
            "Estrella, Corazón, Libro, Casa, Trabajo, Viaje, Dinero, Salud, Comida, Música, Idea, Personas",
            "Estrela, Coração, Livro, Casa, Trabalho, Viagem, Dinheiro, Saúde, Comida, Música, Ideia, Pessoas",
            "Stern, Herz, Buch, Zuhause, Arbeit, Reise, Geld, Gesundheit, Essen, Musik, Idee, Menschen",
            "Étoile, Coeur, Livre, Maison, Travail, Voyage, Argent, Santé, Cuisine, Musique, Idée, Personnes",
        ).split(", ")
        return names[ICON_ORDER.indexOf(id).takeIf { it >= 0 } ?: 0]
    }

    /** Written in normal case here; the Eyebrow style uppercases it when painting. */
    fun siblingFrom(app: String, d: LocalDate): String = "$app, ${dayMonthYear(d)}"

    val importToDay = t("Import to that day", "Importar a ese día", "Importar para esse dia", "An diesem Tag importieren", "Importer à ce jour")
    val importElsewhere = t(
        "To another day or collection",
        "A otro día o colección",
        "Para outro dia ou coleção",
        "An einen anderen Tag oder in eine Sammlung",
        "Vers un autre jour ou une collection",
    )
    val importSkip = t("Skip", "Saltar", "Pular", "Überspringen", "Passer")

    fun importSiblingDone(n: Int): String = when {
        n == 0 -> t("Nothing was imported.", "No se ha importado nada.", "Nada foi importado.", "Nichts wurde importiert.", "Rien n'a été importé.")
        isSingular(n) -> t("1 entry imported.", "1 entrada importada.", "1 entrada importada.", "1 Eintrag importiert.", "1 entrée importée.")
        else -> t("$n entries imported.", "$n entradas importadas.", "$n entradas importadas.", "$n Einträge importiert.", "$n entrées importées.")
    }

    val importNotSibling = t(
        "That file is not a backup from Bobbin, Purl, MoodTraker or Quilt.",
        "Ese fichero no es una copia de Bobbin, Purl, MoodTraker ni Quilt.",
        "Esse arquivo não é uma cópia do Bobbin, Purl, MoodTraker nem Quilt.",
        "Diese Datei ist keine Sicherung von Bobbin, Purl, MoodTraker oder Quilt.",
        "Ce fichier n'est pas une copie de Bobbin, Purl, MoodTraker ou Quilt.",
    )
}
