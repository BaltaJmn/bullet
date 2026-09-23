package com.baltajmn.bullet.i18n

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/** Test 24 (docs/tecnico.md 10): the examples of docs/textos.md, in the five languages. */
class StringsTest {
    private val saved = S.lang
    private val LANGS = listOf("en", "es", "pt", "de", "fr")
    private val sep23 = LocalDate(2026, 9, 23) // Wednesday
    private val jan1 = LocalDate(2027, 1, 1)
    private val sep2026 = YearMonth(2026, Month.SEPTEMBER)

    @AfterTest
    fun restore() {
        S.lang = saved
    }

    private fun each(check: () -> String, en: String, es: String, pt: String, de: String, fr: String) {
        listOf("en" to en, "es" to es, "pt" to pt, "de" to de, "fr" to fr).forEach { (code, expected) ->
            S.lang = code
            assertEquals(expected, check(), code)
        }
    }

    @Test
    fun fallsBackToEnglish() {
        assertEquals("es", normalizeLanguage("es-ES"))
        assertEquals("pt", normalizeLanguage("pt_BR"))
        assertEquals("en", normalizeLanguage("it"))
        assertEquals("en", normalizeLanguage(""))
    }

    @Test
    fun monthsAndWeekdaysExistInEveryLanguage() {
        for (l in LANGS) {
            S.lang = l
            assertEquals(12, S.monthNames().size, l)
            assertEquals(12, S.monthShort().size, l)
            assertEquals(7, S.weekdayNames().size, l)
            assertEquals(7, S.weekdayShort().size, l)
            assertEquals(7, S.weekdayInitial().size, l)
            S.monthNames().forEach { assertTrue(it.isNotEmpty(), "$l monthNames") }
            S.weekdayNames().forEach { assertTrue(it.isNotEmpty(), "$l weekdayNames") }
        }
    }

    @Test
    fun dates() {
        each({ S.dayTitle(sep23) }, "Wednesday 23", "Miércoles 23", "Quarta-feira, 23", "Mittwoch, 23.", "Mercredi 23")
        each(
            { S.shortDate(sep23) },
            "September 23", "23 de septiembre", "23 de setembro", "23. September", "23 septembre",
        )
        each(
            { S.longDateWithYear(LocalDate(2026, 9, 22)) },
            "Tuesday, September 22, 2026", "Martes, 22 de septiembre de 2026", "Terça-feira, 22 de setembro de 2026",
            "Dienstag, 22. September 2026", "Mardi 22 septembre 2026",
        )
        each(
            { S.dayMonthYear(jan1) },
            "January 1, 2027", "1 de enero de 2027", "1 de janeiro de 2027", "1. Januar 2027", "1er janvier 2027",
        )
        each(
            { S.abbrDate(LocalDate(2027, 9, 24)) },
            "Sep 24", "24 sept", "24 set", "24. Sept.", "24 sept.",
        )
        each(
            { S.abbrDateWithYear(LocalDate(2027, 10, 14)) },
            "Oct 14, 2027", "14 oct 2027", "14 out 2027", "14. Okt. 2027", "14 oct. 2027",
        )
        each({ S.widgetDate(sep23) }, "WED 23 SEP", "MIÉ 23 SEPT", "QUA 23 SET", "MI 23 SEPT", "MER 23 SEPT")
        each({ S.monthName(sep2026) }, "September", "Septiembre", "Setembro", "September", "Septembre")
        each({ S.monthYear(sep2026) }, "September 2026", "septiembre de 2026", "setembro de 2026", "September 2026", "septembre 2026")
        each({ S.monthTitle(sep2026) }, "September 2026", "Septiembre 2026", "Setembro 2026", "September 2026", "Septembre 2026")
        each({ S.clock(9, 5) }, "09:05", "09:05", "09:05", "09:05", "09:05")
    }

    @Test
    fun frenchDayOneIsWrittenAsFirst() {
        S.lang = "fr"
        assertEquals("1er septembre", S.shortDate(LocalDate(2026, 9, 1)))
        assertEquals("2 septembre", S.shortDate(LocalDate(2026, 9, 2)))
        assertEquals("Nouveau carnet depuis le 1er janvier 2027.", S.notebookDone(jan1))
    }

    @Test
    fun frenchElidesDeBeforeAVowel() {
        S.lang = "fr"
        assertEquals("Tâches d'août", S.fromMonthTasks(YearMonth(2026, Month.AUGUST)))
        assertEquals("Tâches de septembre", S.fromMonthTasks(sep2026))
    }

    @Test
    fun taskAndEntryPluralsAt0And1And2() {
        each({ S.taskCount(0) }, "0 tasks", "0 tareas", "0 tarefas", "0 Aufgaben", "0 tâche")
        each({ S.taskCount(1) }, "1 task", "1 tarea", "1 tarefa", "1 Aufgabe", "1 tâche")
        each({ S.taskCount(2) }, "2 tasks", "2 tareas", "2 tarefas", "2 Aufgaben", "2 tâches")
        each({ S.entryCount(0) }, "0 entries", "0 entradas", "0 entradas", "0 Einträge", "0 entrée")
        each({ S.entryCount(1) }, "1 entry", "1 entrada", "1 entrada", "1 Eintrag", "1 entrée")
        each({ S.entryCount(2) }, "2 entries", "2 entradas", "2 entradas", "2 Einträge", "2 entrées")
    }

    @Test
    fun widgetWordPluralsAt0And1And2() {
        each({ S.widgetOpen(0) }, "open", "abiertas", "abertas", "offen", "ouverte")
        each({ S.widgetOpen(1) }, "open", "abierta", "aberta", "offen", "ouverte")
        each({ S.widgetOpen(2) }, "open", "abiertas", "abertas", "offen", "ouvertes")
        each({ S.widgetDone(1) }, "done", "hecha", "feita", "erledigt", "faite")
        each({ S.widgetEvents(2) }, "events", "eventos", "eventos", "Ereignisse", "événements")
    }

    @Test
    fun importSummaryJoinsTheParts() {
        each(
            { S.importSummary(12, 3, 40) },
            "The backup brings 12 new entries and 3 newer than yours; 40 were already here. Nothing gets deleted.",
            "La copia trae 12 entradas nuevas y 3 más recientes que las tuyas; 40 ya estaban. No se borra nada.",
            "A cópia traz 12 entradas novas e 3 mais recentes que as suas; 40 já estavam aqui. Nada é apagado.",
            "Die Sicherung bringt 12 neue Einträge und 3, die neuer sind als deine; 40 waren schon da. Es wird nichts gelöscht.",
            "La copie apporte 12 nouvelles entrées et 3 plus récentes que les tiennes ; 40 étaient déjà là. Rien n'est supprimé.",
        )
        each(
            { S.importSummary(0, 0, 40) },
            "The backup brings nothing new; 40 were already here. Nothing gets deleted.",
            "La copia no trae nada nuevo; 40 ya estaban. No se borra nada.",
            "A cópia não traz nada novo; 40 já estavam aqui. Nada é apagado.",
            "Die Sicherung bringt nichts Neues; 40 waren schon da. Es wird nichts gelöscht.",
            "La copie n'apporte rien de nouveau ; 40 étaient déjà là. Rien n'est supprimé.",
        )
        S.lang = "es"
        assertEquals("La copia trae 12 entradas nuevas y 3 más recientes que las tuyas. No se borra nada.", S.importSummary(12, 3, 0))
    }

    @Test
    fun importDoneAndSiblingDonePlurals() {
        each(
            { S.importDone(0) },
            "Journal up to date: there was nothing to change.",
            "Diario al día: no había nada que cambiar.",
            "Diário em dia: não havia nada para mudar.",
            "Journal aktuell: es gab nichts zu ändern.",
            "Journal à jour : il n'y avait rien à changer.",
        )
        each(
            { S.importDone(15) },
            "Journal up to date: 15 changes.",
            "Diario al día: 15 cambios.",
            "Diário em dia: 15 alterações.",
            "Journal aktuell: 15 Änderungen.",
            "Journal à jour : 15 modifications.",
        )
        each(
            { S.importSiblingDone(0) },
            "Nothing was imported.", "No se ha importado nada.", "Nada foi importado.", "Nichts wurde importiert.", "Rien n'a été importé.",
        )
        each(
            { S.importSiblingDone(1) },
            "1 entry imported.", "1 entrada importada.", "1 entrada importada.", "1 Eintrag importiert.", "1 entrée importée.",
        )
    }

    @Test
    fun glyphNameCoversEveryStatus() {
        // OPEN/EVENT/NOTE fall back to the plain vals, which are frozen at S's own init (like the
        // siblings): compared against the val itself rather than a literal, since S.lang below does
        // not reach back and change them.
        assertEquals(S.bulletTask, S.glyphName(Bullet.TASK, TaskStatus.OPEN))
        assertEquals(S.bulletEvent, S.glyphName(Bullet.EVENT, TaskStatus.OPEN))
        assertEquals(S.bulletNote, S.glyphName(Bullet.NOTE, TaskStatus.OPEN))
        S.lang = "en"
        assertEquals("Done task", S.glyphName(Bullet.TASK, TaskStatus.DONE))
        assertEquals("Migrated task", S.glyphName(Bullet.TASK, TaskStatus.MIGRATED))
        assertEquals("Scheduled task", S.glyphName(Bullet.TASK, TaskStatus.SCHEDULED))
        assertEquals("Discarded task", S.glyphName(Bullet.TASK, TaskStatus.IRRELEVANT))
    }

    @Test
    fun entryDescriptionOrdersSignifiersAndCasesThem() {
        // signifierPriority/Inspiration/Explore are plain vals, frozen like bulletEvent above, so
        // the expected text is built from them directly rather than a literal in one language.
        assertEquals(
            "${S.bulletEvent}: dinner with Ana",
            S.entryDescription(Bullet.EVENT, TaskStatus.OPEN, emptySet(), "dinner with Ana"),
        )
        val done = S.glyphName(Bullet.TASK, TaskStatus.DONE) // fresh t() call: follows S.lang below
        // Priority before inspiration regardless of the set's own order (docs/textos.md 20).
        assertEquals(
            "$done, ${S.signifierPriority.lowercase()}, ${S.signifierInspiration.lowercase()}: buy bread",
            S.entryDescription(Bullet.TASK, TaskStatus.DONE, setOf(Signifier.INSPIRATION, Signifier.PRIORITY), "buy bread"),
        )
        // German keeps the noun's own case instead of lowercasing it (docs/textos.md 20).
        S.lang = "de"
        assertEquals(
            "${S.glyphName(Bullet.TASK, TaskStatus.DONE)}, ${S.signifierPriority}: Brot kaufen",
            S.entryDescription(Bullet.TASK, TaskStatus.DONE, setOf(Signifier.PRIORITY), "Brot kaufen"),
        )
    }

    @Test
    fun questionBankHasSixtyNonEmptyQuestionsPerLanguage() {
        for (l in LANGS) {
            S.lang = l
            assertEquals(QUESTION_COUNT, 60)
            for (i in 0 until QUESTION_COUNT) {
                assertTrue(S.question(i).isNotEmpty(), "$l question $i")
            }
        }
        S.lang = "en"
        assertEquals("What wouldn't you migrate if you had to rewrite it by hand?", S.question(0))
        assertEquals("How would you sum up these days in one line?", S.question(59))
        S.lang = "fr"
        assertEquals("Comment résumerais-tu ces jours en une ligne ?", S.question(59))
    }

    @Test
    fun coverAndPaperNamesFallBackToTheFreeOption() {
        S.lang = "es"
        assertEquals("Salvia", S.coverName("sage"))
        assertEquals("Salvia", S.coverName("unknown"))
        assertEquals("Lila", S.coverName("lilac"))
        assertEquals("punteado", S.paperName("dotted"))
        assertEquals("punteado", S.paperName("unknown"))
    }

    @Test
    fun joinAndAndTrackerMarked() {
        S.lang = "en"
        assertEquals("9", S.joinAnd(listOf("9")))
        assertEquals("1 and 9", S.joinAnd(listOf("1", "9")))
        assertEquals("1, 2, 5 and 9", S.joinAnd(listOf("1", "2", "5", "9")))
        assertEquals("No day marked", S.a11yTrackerMarked(emptyList()))
        assertEquals("Marked: 1, 2, 5 and 9", S.a11yTrackerMarked(listOf(1, 2, 5, 9)))
        S.lang = "fr"
        assertEquals("Cochés : 1, 2, 5 et 9", S.a11yTrackerMarked(listOf(1, 2, 5, 9)))
    }

    @Test
    fun weekStartUsesThePortugueseGenderedArticle() {
        S.lang = "pt"
        assertEquals("Na segunda-feira, como o sistema", S.weekStartSystem(DayOfWeek.MONDAY))
        assertEquals("No domingo", S.weekStartDay(DayOfWeek.SUNDAY))
        assertEquals("No sábado", S.weekStartDay(DayOfWeek.SATURDAY))
    }

    @Test
    fun a11yDayRowHandlesZero() {
        each(
            { S.a11yDayRow(3, "Thursday", 2) },
            "3, Thursday, 2 entries", "3, Thursday, 2 entradas", "3, Thursday, 2 entradas", "3., Thursday, 2 Einträge", "3, Thursday, 2 entrées",
        )
        each(
            { S.a11yDayRow(3, "Thursday", 0) },
            "3, Thursday, no entries", "3, Thursday, sin entradas", "3, Thursday, sem entradas", "3., Thursday, keine Einträge", "3, Thursday, aucune entrée",
        )
    }

    @Test
    fun everyFixedTextIsNonEmptyInEveryLanguage() {
        val fixed: List<() -> String> = listOf(
            { S.appName }, { S.ok }, { S.cancel }, { S.yes }, { S.notNow }, { S.close }, { S.back }, { S.undo }, { S.working },
            { S.tabToday }, { S.tabMonth }, { S.tabFuture }, { S.tabIndex }, { S.a11ySelected },
            { S.captureHint }, { S.bulletTask }, { S.bulletEvent }, { S.bulletNote }, { S.stateDone }, { S.stateMigrated },
            { S.stateScheduled }, { S.stateDiscarded }, { S.signifierPriority }, { S.signifierInspiration }, { S.signifierExplore },
            { S.prefixHint }, { S.calendarToday }, { S.noticeCorrupt }, { S.noticeSaveFailed },
            { S.monthTasks }, { S.calendarTitle },
            { S.indexFilterHint }, { S.indexEmpty }, { S.indexNoMatch },
            { S.archive }, { S.unarchive }, { S.deleteCollection },
            { S.skip }, { S.saveAndGo }, { S.reviewAllDecided }, { S.futureAllDecided },
            { S.searchHint }, { S.filterOpen }, { S.searchEmpty }, { S.searchNothing },
            { S.keyTitle }, { S.keyGestureTap }, { S.keyTapText },
            { S.settingsTitle }, { S.sectionDay }, { S.sectionPro }, { S.siblingPurl }, { S.siblingQuilt }, { S.siblingMood },
            { S.proTitle }, { S.proFree }, { S.storeUnavailable },
            { S.importTitle }, { S.importFailedTitle }, { S.wipeTitle }, { S.wipeText }, { S.wipeWord },
            { S.unlock }, { S.lockPromptTitle }, { S.updateNeeded },
            { S.shareImage }, { S.shareText },
            { S.reminderTitle }, { S.reminderBody }, { S.reminderChannel },
            { S.widgetReview }, { S.widgetUnlock }, { S.pickerTodayName },
            { S.a11yComplete }, { S.a11yReopen }, { S.a11yCapture },
            { S.continueCollection }, { S.newNotebookRow }, { S.tileLabel }, { S.captureSaved }, { S.captureEmpty }, { S.bookFailed },
            { S.syncRow }, { S.syncOff }, { S.iconAndTheme }, { S.importNotSibling },
        )
        for (l in LANGS) {
            S.lang = l
            fixed.forEach { assertTrue(it().isNotEmpty(), l) }
        }
    }
}
