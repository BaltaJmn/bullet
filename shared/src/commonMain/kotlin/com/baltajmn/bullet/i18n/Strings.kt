package com.baltajmn.bullet.i18n

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/** The device locale's own first day of the week: what `firstDayOfWeek` falls back to without a setting (#24). */
expect fun systemFirstDayOfWeek(): DayOfWeek

/** The languages the app ships. Anything else falls back to English. */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr", "it", "nl", "pl", "ru", "tr", "id", "ja", "ko")

internal fun normalizeLanguage(code: String): String {
    // Android still reports Indonesian by its old ISO code.
    val two = code.take(2).lowercase().let { if (it == "in") "id" else it }
    return two.takeIf { it in SUPPORTED } ?: "en"
}

/** Upper case for a label, as [S.caps] writes it in the app's language. */
fun String.caps(): String = S.caps(this)

/** How many questions [S.question] holds (docs/tecnico.md 12.3). */
const val QUESTION_COUNT = 60

/**
 * Every user-facing string, in one table, copied from docs/textos.md. The section comments below
 * match that document's numbering (1 to 25) one for one.
 *
 * Not Compose Resources on purpose: part of these strings are drawn outside a @Composable (a
 * BroadcastReceiver, a Glance widget, a Canvas, a notification builder).
 *
 * ponytail: the language is read once at first access and the plain texts are resolved then, like
 * the siblings. Both systems restart the app when the language changes, so this only matters if
 * live switching is ever needed. The functions read [lang] on every call, which lets the tests go
 * through the thirteen.
 */
object S {

    internal var lang = normalizeLanguage(systemLanguage())

    private fun t(
        en: String, es: String, pt: String, de: String, fr: String,
        it: String, nl: String, pl: String, ru: String, tr: String, id: String, ja: String, ko: String,
    ): String = tIn(lang, en, es, pt, de, fr, it, nl, pl, ru, tr, id, ja, ko)

    /** [t] in [code] rather than the device language: what a parser told the language reads (12.4). */
    private fun tIn(
        code: String,
        en: String, es: String, pt: String, de: String, fr: String,
        it: String, nl: String, pl: String, ru: String, tr: String, id: String, ja: String, ko: String,
    ): String = when (code) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        "it" -> it
        "nl" -> nl
        "pl" -> pl
        "ru" -> ru
        "tr" -> tr
        "id" -> id
        "ja" -> ja
        "ko" -> ko
        else -> en
    }

    /** Upper case as each language writes it: Turkish dots its capital i. */
    fun caps(text: String): String = if (lang == "tr") text.replace('i', 'İ').uppercase() else text.uppercase()

    /**
     * Method rule 3 (docs/textos.md): fr treats 0 and 1 as singular, the others only 1. Polish and
     * Russian pick among three forms with [word].
     */
    private fun isPlural(n: Int): Boolean = if (lang == "fr") n >= 2 else n != 1
    private fun isSingular(n: Int): Boolean = !isPlural(n)

    /**
     * The form of a word that goes with [n]. [forms] holds one form (no plural), "one|other" by
     * [isPlural], or "one|few|many" for Polish and Russian ([slavic]).
     */
    private fun word(n: Int, forms: String): String {
        val f = forms.split("|")
        return when (f.size) {
            1 -> f[0]
            2 -> if (isPlural(n)) f[1] else f[0]
            else -> slavic(n, f[0], f[1], f[2])
        }
    }

    /** Polish and Russian: "1 задача", "2 задачи", "5 задач"; Russian takes [one] again for 21. */
    private fun slavic(n: Int, one: String, few: String, many: String): String {
        val tens = n % 100
        val units = n % 10
        return when {
            n == 1 || (lang == "ru" && units == 1 && tens != 11) -> one
            units in 2..4 && tens !in 12..14 -> few
            else -> many
        }
    }

    private fun wordTask(n: Int) = word(n, t(
        "task|tasks",
        "tarea|tareas",
        "tarefa|tarefas",
        "Aufgabe|Aufgaben",
        "tâche|tâches",
        "attività|attività",
        "taak|taken",
        "zadanie|zadania|zadań",
        "задача|задачи|задач",
        "görev",
        "tugas",
        "タスク",
        "할 일",
    ))

    private fun wordEntry(n: Int) = word(n, t(
        "entry|entries",
        "entrada|entradas",
        "entrada|entradas",
        "Eintrag|Einträge",
        "entrée|entrées",
        "voce|voci",
        "item|items",
        "wpis|wpisy|wpisów",
        "запись|записи|записей",
        "kayıt",
        "entri",
        "項目",
        "항목",
    ))

    private fun wordChange(n: Int) = word(
        n,
        t(
            "change|changes",
            "cambio|cambios",
            "alteração|alterações",
            "Änderung|Änderungen",
            "modification|modifications",
            "modifica|modifiche",
            "wijziging|wijzigingen",
            "zmiana|zmiany|zmian",
            "изменение|изменения|изменений",
            "değişiklik",
            "perubahan",
            "変更",
            "변경",
        ),
    )

    private fun wordImage(n: Int) = word(n, t(
        "image|images",
        "imagen|imágenes",
        "imagem|imagens",
        "Bild|Bilder",
        "image|images",
        "immagine|immagini",
        "afbeelding|afbeeldingen",
        "obraz|obrazy|obrazów",
        "изображение|изображения|изображений",
        "görsel",
        "gambar",
        "画像",
        "이미지",
    ))

    /** Generic count, not tied to one screen; used wherever a bare "n tasks" is needed. */
    fun taskCount(n: Int) = t(
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "$n ${wordTask(n)}",
        "${n}件の${wordTask(n)}",
        "${wordTask(n)} ${n}개",
    )

    /** Generic count, not tied to one screen; used wherever a bare "n entries" is needed. */
    fun entryCount(n: Int) = t(
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "$n ${wordEntry(n)}",
        "${n}件の${wordEntry(n)}",
        "${wordEntry(n)} ${n}개",
    )

    /** "1, 2, 5 y 9": commas and the language's conjunction before the last item. */
    fun joinAnd(items: List<String>): String {
        if (items.size <= 1) return items.firstOrNull().orEmpty()
        val and = t(" and ", " y ", " e ", " und ", " et ", " e ", " en ", " i ", " и ", " ve ", " dan ", "と", ", ")
        return items.dropLast(1).joinToString(listComma()) + and + items.last()
    }

    /** The comma between the items of a list; Japanese writes its own and no space. */
    private fun listComma() = t(", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", ", "、", ", ")

    /** The full stop that closes a sentence the code ends; Japanese writes its own. */
    private fun fullStop() = t(".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "。", ".")

    /** A weekday with a whole date: after it in Turkish, Japanese and Korean. */
    private fun weekdayWithDate(w: String, date: String) = t(
        "$w $date", "$w $date", "$w $date", "$w $date", "$w $date",
        "$w $date", "$w $date", "$w, $date", "$w, $date", "$date $w", "$w, $date", "$date $w", "$date $w",
    )

    private fun daysBetween(from: LocalDate, to: LocalDate): Int = (to.toEpochDays() - from.toEpochDays()).toInt()

    /** French elides "de" before a month name that starts with a vowel: "Tâches d'août". */
    private fun frDe(month: String) = if (month.firstOrNull()?.lowercaseChar() in setOf('a', 'e', 'i', 'o', 'u')) {
        "d'$month"
    } else {
        "de $month"
    }

    // --- 1. Fechas y horas ----------------------------------------------------------------

    fun monthNames(code: String = lang): List<String> = tIn(
        code,
        "January, February, March, April, May, June, July, August, September, October, November, December",
        "enero, febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre",
        "janeiro, fevereiro, março, abril, maio, junho, julho, agosto, setembro, outubro, novembro, dezembro",
        "Januar, Februar, März, April, Mai, Juni, Juli, August, September, Oktober, November, Dezember",
        "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre",
        "gennaio, febbraio, marzo, aprile, maggio, giugno, luglio, agosto, settembre, ottobre, novembre, dicembre",
        "januari, februari, maart, april, mei, juni, juli, augustus, september, oktober, november, december",
        "styczeń, luty, marzec, kwiecień, maj, czerwiec, lipiec, sierpień, wrzesień, październik, listopad, grudzień",
        "январь, февраль, март, апрель, май, июнь, июль, август, сентябрь, октябрь, ноябрь, декабрь",
        "Ocak, Şubat, Mart, Nisan, Mayıs, Haziran, Temmuz, Ağustos, Eylül, Ekim, Kasım, Aralık",
        "Januari, Februari, Maret, April, Mei, Juni, Juli, Agustus, September, Oktober, November, Desember",
        "1月, 2月, 3月, 4月, 5月, 6月, 7月, 8月, 9月, 10月, 11月, 12月",
        "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월",
    ).split(", ")

    /** The month inside a date: Polish and Russian decline it ("14 września", "14 сентября"). */
    fun monthNamesInDate(code: String = lang): List<String> = when (code) {
        "pl" -> "stycznia, lutego, marca, kwietnia, maja, czerwca, lipca, sierpnia, września, października, listopada, grudnia"
        "ru" -> "января, февраля, марта, апреля, мая, июня, июля, августа, сентября, октября, ноября, декабря"
        else -> return monthNames(code)
    }.split(", ")

    fun monthShort(): List<String> = t(
        "Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec",
        "ene, feb, mar, abr, may, jun, jul, ago, sept, oct, nov, dic",
        "jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez",
        "Jan., Feb., März, Apr., Mai, Juni, Juli, Aug., Sept., Okt., Nov., Dez.",
        "janv., févr., mars, avr., mai, juin, juil., août, sept., oct., nov., déc.",
        "gen, feb, mar, apr, mag, giu, lug, ago, set, ott, nov, dic",
        "jan, feb, mrt, apr, mei, jun, jul, aug, sep, okt, nov, dec",
        "sty, lut, mar, kwi, maj, cze, lip, sie, wrz, paź, lis, gru",
        "янв., февр., мар., апр., мая, июн., июл., авг., сент., окт., нояб., дек.",
        "Oca, Şub, Mar, Nis, May, Haz, Tem, Ağu, Eyl, Eki, Kas, Ara",
        "Jan, Feb, Mar, Apr, Mei, Jun, Jul, Agu, Sep, Okt, Nov, Des",
        "1月, 2月, 3月, 4月, 5月, 6月, 7月, 8月, 9月, 10月, 11月, 12月",
        "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월",
    ).split(", ")

    fun weekdayNames(code: String = lang): List<String> = tIn(
        code,
        "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday",
        "lunes, martes, miércoles, jueves, viernes, sábado, domingo",
        "segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado, domingo",
        "Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag",
        "lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche",
        "lunedì, martedì, mercoledì, giovedì, venerdì, sabato, domenica",
        "maandag, dinsdag, woensdag, donderdag, vrijdag, zaterdag, zondag",
        "poniedziałek, wtorek, środa, czwartek, piątek, sobota, niedziela",
        "понедельник, вторник, среда, четверг, пятница, суббота, воскресенье",
        "Pazartesi, Salı, Çarşamba, Perşembe, Cuma, Cumartesi, Pazar",
        "Senin, Selasa, Rabu, Kamis, Jumat, Sabtu, Minggu",
        "月曜日, 火曜日, 水曜日, 木曜日, 金曜日, 土曜日, 日曜日",
        "월요일, 화요일, 수요일, 목요일, 금요일, 토요일, 일요일",
    ).split(", ")

    fun weekdayShort(): List<String> = t(
        "Mon, Tue, Wed, Thu, Fri, Sat, Sun",
        "lun, mar, mié, jue, vie, sáb, dom",
        "seg, ter, qua, qui, sex, sáb, dom",
        "Mo, Di, Mi, Do, Fr, Sa, So",
        "lun, mar, mer, jeu, ven, sam, dim",
        "lun, mar, mer, gio, ven, sab, dom",
        "ma, di, wo, do, vr, za, zo",
        "pon, wt, śr, czw, pt, sob, niedz",
        "пн, вт, ср, чт, пт, сб, вс",
        "Pzt, Sal, Çar, Per, Cum, Cmt, Paz",
        "Sen, Sel, Rab, Kam, Jum, Sab, Min",
        "月, 火, 水, 木, 金, 土, 日",
        "월, 화, 수, 목, 금, 토, 일",
    ).split(", ")

    /** Monday to Sunday; Mes paints it starting from whichever day the week starts on. */
    fun weekdayInitial(): List<String> = t(
        "M, T, W, T, F, S, S",
        "L, M, X, J, V, S, D",
        "S, T, Q, Q, S, S, D",
        "M, D, M, D, F, S, S",
        "L, M, M, J, V, S, D",
        "L, M, M, G, V, S, D",
        "M, D, W, D, V, Z, Z",
        "P, W, Ś, C, P, S, N",
        "П, В, С, Ч, П, С, В",
        "P, S, Ç, P, C, C, P",
        "S, S, R, K, J, S, M",
        "月, 火, 水, 木, 金, 土, 日",
        "월, 화, 수, 목, 금, 토, 일",
    ).split(", ")

    /** French writes the first of the month as "1er" in any date that carries the month name. */
    private fun frDay(day: Int): String = if (lang == "fr" && day == 1) "1er" else "$day"

    fun dayTitle(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal].replaceFirstChar { it.uppercase() }
        val n = d.day
        return t(
            "$w $n", "$w $n", "$w, $n", "$w, $n.", "$w $n",
            "$w $n", "$w $n", "$w, $n", "$w, $n", "$n $w", "$w, $n", "${n}日 $w", "${n}일 $w",
        )
    }

    fun monthName(month: YearMonth): String = monthNames()[month.month.ordinal].replaceFirstChar { it.uppercase() }

    private fun monthAndYear(m: String, y: Int) = t(
        "$m $y", "$m de $y", "$m de $y", "$m $y", "$m $y",
        "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "${y}年$m", "${y}년 $m",
    )

    fun monthYear(month: YearMonth): String = monthAndYear(monthNames()[month.month.ordinal], month.year)

    fun monthTitle(month: YearMonth): String {
        val m = monthName(month)
        val y = month.year
        return t(
            "$m $y", "$m $y", "$m $y", "$m $y", "$m $y",
            "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "${y}年$m", "${y}년 $m",
        )
    }

    fun shortDate(d: LocalDate): String {
        val m = monthNamesInDate()[d.month.ordinal]
        val day = frDay(d.day)
        val n = d.day
        return t(
            "$m $n", "$day de $m", "$day de $m", "$day. $m", "$day $m",
            "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$m${n}日", "$m ${n}일",
        )
    }

    /** [date] with the year, where each language puts it: "September 14, 2026", "2026年9月14日". */
    private fun withYear(date: String, y: Int) = t(
        "$date, $y", "$date de $y", "$date de $y", "$date $y", "$date $y",
        "$date $y", "$date $y", "$date $y", "$date $y", "$date $y", "$date $y", "${y}年$date", "${y}년 $date",
    )

    /** The weekday in front of [date], or behind it in Turkish, Japanese and Korean. */
    private fun withWeekday(d: LocalDate, date: String): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        val up = w.replaceFirstChar { it.uppercase() }
        return t(
            "$up, $date", "$up, $date", "$up, $date", "$up, $date", "$up $date",
            "$up $date", "$up $date", "$up, $date", "$up, $date", "$date $w", "$up, $date", "$date $w", "$date $w",
        )
    }

    fun longDate(d: LocalDate): String = withWeekday(d, shortDate(d))

    fun dayMonthYear(d: LocalDate) = withYear(shortDate(d), d.year)

    fun longDateWithYear(d: LocalDate) = withWeekday(d, dayMonthYear(d))

    fun abbrDate(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        val n = d.day
        return t(
            "$m $n", "$n $m", "$n $m", "$n. $m", "$n $m",
            "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$m${n}日", "$m ${n}일",
        )
    }

    fun abbrDateWithYear(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        val n = d.day
        val y = d.year
        return t(
            "$m $n, $y", "$n $m $y", "$n $m $y", "$n. $m $y", "$n $m $y",
            "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "${y}年$m${n}日", "${y}년 $m ${n}일",
        )
    }

    /** Always 24 hours, in every language. */
    fun clock(hour: Int, minute: Int) =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    // --- 2. Comunes -------------------------------------------------------------------------

    val appName = "Bobbin"
    val ok = t("OK", "Vale", "OK", "OK", "OK", "OK", "OK", "OK", "ОК", "Tamam", "OK", "OK", "확인")
    val cancel = t("Cancel", "Cancelar", "Cancelar", "Abbrechen", "Annuler", "Annulla", "Annuleren", "Anuluj", "Отмена", "İptal", "Batal", "キャンセル", "취소")
    val yes = t("Yes", "Sí", "Sim", "Ja", "Oui", "Sì", "Ja", "Tak", "Да", "Evet", "Ya", "はい", "예")
    val notNow = t("Not now", "Ahora no", "Agora não", "Jetzt nicht", "Pas maintenant", "Non ora", "Niet nu", "Nie teraz", "Не сейчас", "Şimdi değil", "Nanti saja", "あとで", "나중에")
    val close = t("Close", "Cerrar", "Fechar", "Schließen", "Fermer", "Chiudi", "Sluiten", "Zamknij", "Закрыть", "Kapat", "Tutup", "閉じる", "닫기")
    val back = t("Back", "Volver", "Voltar", "Zurück", "Retour", "Indietro", "Terug", "Wstecz", "Назад", "Geri", "Kembali", "戻る", "뒤로")
    val undo = t("Undo", "Deshacer", "Desfazer", "Rückgängig", "Annuler", "Annulla", "Ongedaan maken", "Cofnij", "Отменить", "Geri al", "Urungkan", "元に戻す", "되돌리기")
    val working = t(
        "One moment...",
        "Un momento...",
        "Um momento...",
        "Einen Moment...",
        "Un instant...",
        "Un attimo...",
        "Een moment...",
        "Chwileczkę...",
        "Минутку...",
        "Bir saniye...",
        "Sebentar...",
        "少々お待ちください...",
        "잠시만요...",
    )
    val add = t("Add", "Añadir", "Adicionar", "Hinzufügen", "Ajouter", "Aggiungi", "Toevoegen", "Dodaj", "Добавить", "Ekle", "Tambah", "追加", "추가")
    val save = t("Save", "Guardar", "Salvar", "Speichern", "Enregistrer", "Salva", "Opslaan", "Zapisz", "Сохранить", "Kaydet", "Simpan", "保存", "저장")
    val gotIt = t("Got it", "Entendido", "Entendi", "Verstanden", "Compris", "Capito", "Begrepen", "Rozumiem", "Понятно", "Anladım", "Mengerti", "わかりました", "알겠어요")
    val previous = t("Back", "Atrás", "Voltar", "Zurück", "Retour", "Indietro", "Terug", "Wstecz", "Назад", "Geri", "Kembali", "戻る", "이전")
    val next = t("Next", "Siguiente", "Próximo", "Weiter", "Suivant", "Avanti", "Volgende", "Dalej", "Далее", "İleri", "Berikutnya", "次へ", "다음")

    // --- 3. Navegacion y cabeceras -----------------------------------------------------------

    val tabToday = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari", "今日", "오늘")
    val tabMonth = t("Month", "Mes", "Mês", "Monat", "Mois", "Mese", "Maand", "Miesiąc", "Месяц", "Ay", "Bulan", "月", "월간")
    val tabFuture = t("Future", "Futuro", "Futuro", "Zukunft", "Futur", "Futuro", "Toekomst", "Przyszłość", "Будущее", "Gelecek", "Mendatang", "未来", "미래")
    val tabIndex = t("Index", "Índice", "Índice", "Index", "Index", "Indice", "Index", "Indeks", "Индекс", "Dizin", "Indeks", "目次", "목차")
    val backToToday = t(
        "Back to today",
        "Volver a hoy",
        "Voltar para hoje",
        "Zurück zu heute",
        "Revenir à aujourd'hui",
        "Torna a oggi",
        "Terug naar vandaag",
        "Wróć do dziś",
        "Вернуться на сегодня",
        "Bugüne dön",
        "Kembali ke hari ini",
        "今日に戻る",
        "오늘로 돌아가기",
    )
    val a11yPreviousDay = t(
        "Previous day",
        "Día anterior",
        "Dia anterior",
        "Vorheriger Tag",
        "Jour précédent",
        "Giorno precedente",
        "Vorige dag",
        "Poprzedni dzień",
        "Предыдущий день",
        "Önceki gün",
        "Hari sebelumnya",
        "前の日",
        "전날",
    )
    val a11yNextDay = t(
        "Next day",
        "Día siguiente",
        "Próximo dia",
        "Nächster Tag",
        "Jour suivant",
        "Giorno successivo",
        "Volgende dag",
        "Następny dzień",
        "Следующий день",
        "Sonraki gün",
        "Hari berikutnya",
        "次の日",
        "다음 날",
    )
    val a11yPreviousMonth = t(
        "Previous month",
        "Mes anterior",
        "Mês anterior",
        "Vorheriger Monat",
        "Mois précédent",
        "Mese precedente",
        "Vorige maand",
        "Poprzedni miesiąc",
        "Предыдущий месяц",
        "Önceki ay",
        "Bulan sebelumnya",
        "前の月",
        "이전 달",
    )
    val a11yNextMonth = t(
        "Next month",
        "Mes siguiente",
        "Próximo mês",
        "Nächster Monat",
        "Mois suivant",
        "Mese successivo",
        "Volgende maand",
        "Następny miesiąc",
        "Следующий месяц",
        "Sonraki ay",
        "Bulan berikutnya",
        "次の月",
        "다음 달",
    )
    val a11yClose = t("Close", "Cerrar", "Fechar", "Schließen", "Fermer", "Chiudi", "Sluiten", "Zamknij", "Закрыть", "Kapat", "Tutup", "閉じる", "닫기")
    val a11ySettings = t(
        "Settings",
        "Ajustes",
        "Ajustes",
        "Einstellungen",
        "Réglages",
        "Impostazioni",
        "Instellingen",
        "Ustawienia",
        "Настройки",
        "Ayarlar",
        "Pengaturan",
        "設定",
        "설정",
    )
    val a11ySearch = t(
        "Search the journal",
        "Buscar en el diario",
        "Buscar no diário",
        "Im Journal suchen",
        "Chercher dans le journal",
        "Cerca nel diario",
        "Zoeken in het dagboek",
        "Szukaj w dzienniku",
        "Поиск по дневнику",
        "Günlükte ara",
        "Cari di jurnal",
        "ジャーナルを検索",
        "다이어리에서 검색",
    )
    val a11yMoreActions = t(
        "More actions",
        "Más acciones",
        "Mais ações",
        "Weitere Aktionen",
        "Plus d'actions",
        "Altre azioni",
        "Meer acties",
        "Więcej działań",
        "Другие действия",
        "Diğer işlemler",
        "Tindakan lainnya",
        "その他の操作",
        "더 많은 동작",
    )
    val a11ySelected = t("selected", "elegida", "selecionada", "ausgewählt", "sélectionnée", "selezionata", "geselecteerd", "wybrane", "выбрано", "seçili", "dipilih", "選択中", "선택됨")

    /** Under the day's title: "Hoy, septiembre de 2026", or just the month away from the three near days. */
    fun daySubtitle(day: LocalDate, today: LocalDate): String {
        val month = monthYear(YearMonth(day.year, day.month))
        val near = when (daysBetween(today, day)) {
            0 -> t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
            -1 -> t("Yesterday", "Ayer", "Ontem", "Gestern", "Hier", "Ieri", "Gisteren", "Wczoraj", "Вчера", "Dün", "Kemarin", "昨日", "어제")
            1 -> t("Tomorrow", "Mañana", "Amanhã", "Morgen", "Demain", "Domani", "Morgen", "Jutro", "Завтра", "Yarın", "Besok", "明日", "내일")
            else -> return month.replaceFirstChar { it.uppercase() }
        }
        return near + listComma() + month
    }

    fun monthSubtitle(month: YearMonth, past: Boolean): String = if (!past) {
        yearAlone(month.year)
    } else {
        yearAlone(month.year) + t(
            ", a past month",
            ", un mes pasado",
            ", um mês passado",
            ", ein vergangener Monat",
            ", un mois passé",
            ", un mese passato",
            ", een afgelopen maand",
            ", miniony miesiąc",
            ", прошедший месяц",
            ", geçmiş bir ay",
            ", bulan yang sudah lewat",
            "、過去の月",
            ", 지나간 달",
        )
    }

    /** A year standing alone: Japanese and Korean write it with its own character. */
    private fun yearAlone(y: Int) = t("$y", "$y", "$y", "$y", "$y", "$y", "$y", "$y", "$y", "$y", "$y", "${y}年", "${y}년")

    fun backToMonth(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        return t(
            "Back to $m",
            "Volver a $m",
            "Voltar para $m",
            "Zurück zum $m",
            "Revenir à $m",
            "Torna ${if (m.startsWith("a")) "ad" else "a"} $m",
            "Terug naar $m",
            "Wróć do: $m",
            "Назад: $m",
            "$m ayına dön",
            "Kembali ke $m",
            "${m}に戻る",
            "${m} 페이지로 돌아가기",
        )
    }

    // --- 4. La entrada, la captura y la hoja --------------------------------------------------

    fun counter(n: Int, max: Int) = "$n/$max"
    val bulletTask = t("Task", "Tarea", "Tarefa", "Aufgabe", "Tâche", "Attività", "Taak", "Zadanie", "Задача", "Görev", "Tugas", "タスク", "할 일")
    val bulletEvent = t("Event", "Evento", "Evento", "Ereignis", "Événement", "Evento", "Gebeurtenis", "Wydarzenie", "Событие", "Etkinlik", "Acara", "イベント", "일정")
    val bulletNote = t("Note", "Nota", "Nota", "Notiz", "Note", "Nota", "Notitie", "Notatka", "Заметка", "Not", "Catatan", "メモ", "메모")
    val stateDone = t("Done", "Hecha", "Feita", "Erledigt", "Faite", "Fatta", "Voltooid", "Zrobione", "Выполнена", "Yapıldı", "Selesai", "完了", "완료")
    val stateMigrated = t("Migrated", "Migrada", "Migrada", "Migriert", "Migrée", "Migrata", "Gemigreerd", "Zmigrowane", "Мигрирована", "Aktarıldı", "Dimigrasikan", "移行済み", "이월")
    val stateScheduled = t(
        "Scheduled",
        "Programada",
        "Agendada",
        "Eingeplant",
        "Planifiée",
        "Programmata",
        "Ingepland",
        "Zaplanowane",
        "Запланирована",
        "Planlandı",
        "Dijadwalkan",
        "スケジュール済み",
        "예정",
    )
    val stateDiscarded = t(
        "Discarded",
        "Descartada",
        "Descartada",
        "Verworfen",
        "Écartée",
        "Scartata",
        "Geschrapt",
        "Odrzucone",
        "Вычеркнута",
        "Bırakıldı",
        "Dibuang",
        "破棄済み",
        "버림",
    )
    val signifierPriority = t(
        "Priority",
        "Prioridad",
        "Prioridade",
        "Priorität",
        "Priorité",
        "Priorità",
        "Prioriteit",
        "Priorytet",
        "Приоритет",
        "Öncelik",
        "Prioritas",
        "優先",
        "우선순위",
    )
    val signifierInspiration = t(
        "Inspiration",
        "Inspiración",
        "Inspiração",
        "Inspiration",
        "Inspiration",
        "Ispirazione",
        "Inspiratie",
        "Inspiracja",
        "Вдохновение",
        "İlham",
        "Inspirasi",
        "ひらめき",
        "영감",
    )
    val signifierExplore = t("Explore", "Explorar", "Explorar", "Erkunden", "Explorer", "Esplora", "Verkennen", "Do zbadania", "Исследование", "Keşfet", "Jelajahi", "調べもの", "알아보기")

    fun wentToDay(d: LocalDate) = abbrDate(d)
    fun wentToMonth(month: YearMonth) = monthTitle(month)

    /** [withYear] picks abbrDateWithYear over abbrDate when the destination isn't the current year. */
    fun wentToFuture(month: YearMonth, day: Int? = null, withYear: Boolean = false): String {
        val place = when {
            day != null && withYear -> abbrDateWithYear(LocalDate(month.year, month.month, day))
            day != null -> abbrDate(LocalDate(month.year, month.month, day))
            else -> monthYear(month)
        }
        return t(
            "Future, $place",
            "Futuro, $place",
            "Futuro, $place",
            "Zukunft, $place",
            "Futur, $place",
            "Futuro, $place",
            "Toekomst, $place",
            "Przyszłość, $place",
            "Будущее, $place",
            "Gelecek, $place",
            "Mendatang, $place",
            "未来、${place}",
            "미래, ${place}",
        )
    }

    /** From MIGRATION_SHOWN_FROM (2) in a review (docs/pantallas.md 11.2): how many times this task was already moved. */
    fun timesMoved(n: Int): String = if (n == 1) {
        t(
            "You've moved it once",
            "Ya la has pasado una vez",
            "Você já a passou uma vez",
            "Schon einmal verschoben",
            "Déjà déplacée une fois",
            "L'hai già spostata una volta",
            "Je hebt deze taak al één keer verplaatst",
            "Przeniesione już raz",
            "Уже перенесена один раз",
            "Bunu bir kez taşıdın",
            "Sudah kamu pindahkan sekali",
            "すでに1回移しています",
            "이미 한 번 옮겼어요",
        )
    } else {
        t(
            "You've moved it $n times",
            "Ya la has pasado $n veces",
            "Você já a passou $n vezes",
            "Schon $n-mal verschoben",
            "Déjà déplacée $n fois",
            "L'hai già spostata $n volte",
            "Je hebt deze taak al $n keer verplaatst",
            "Przeniesione już $n razy",
            "Уже перенесена $n ${word(n, "раз|раза|раз")}",
            "Bunu $n kez taşıdın",
            "Sudah kamu pindahkan $n kali",
            "すでに${n}回移しています",
            "이미 ${n}번 옮겼어요",
        )
    }

    val actionDone = t("Done", "Hecha", "Feita", "Erledigt", "Faite", "Fatta", "Voltooid", "Zrobione", "Выполнена", "Yapıldı", "Selesai", "完了", "완료")
    val actionDiscard = t("Discard", "Descartar", "Descartar", "Verwerfen", "Écarter", "Scarta", "Schrappen", "Odrzuć", "Вычеркнуть", "Bırak", "Buang", "破棄", "버리기")
    val actionReopen = t(
        "Reopen",
        "Reabrir",
        "Reabrir",
        "Wieder öffnen",
        "Rouvrir",
        "Riapri",
        "Heropenen",
        "Otwórz ponownie",
        "Открыть снова",
        "Yeniden aç",
        "Buka lagi",
        "未完了に戻す",
        "다시 열기",
    )
    val actionRecover = t(
        "Bring back",
        "Recuperar",
        "Recuperar",
        "Zurückholen",
        "Récupérer",
        "Recupera",
        "Terughalen",
        "Przywróć",
        "Вернуть",
        "Geri getir",
        "Kembalikan",
        "復元",
        "되살리기",
    )
    val actionGoToCopy = t(
        "Go to the copy",
        "Ir a la copia",
        "Ir para a cópia",
        "Zur Kopie",
        "Aller à la copie",
        "Vai alla copia",
        "Naar de kopie",
        "Przejdź do kopii",
        "Перейти к копии",
        "Kopyaya git",
        "Ke salinan",
        "コピーへ移動",
        "사본으로 가기",
    )
    val actionEdit = t(
        "Edit the text",
        "Editar el texto",
        "Editar o texto",
        "Text bearbeiten",
        "Modifier le texte",
        "Modifica il testo",
        "Tekst bewerken",
        "Edytuj tekst",
        "Изменить текст",
        "Metni düzenle",
        "Ubah teks",
        "テキストを編集",
        "텍스트 고치기",
    )
    val actionDelete = t("Delete", "Borrar", "Apagar", "Löschen", "Supprimer", "Elimina", "Verwijderen", "Usuń", "Удалить", "Sil", "Hapus", "削除", "삭제")
    val moveToday = t(
        "Move to today",
        "Pasar a hoy",
        "Passar para hoje",
        "Auf heute verschieben",
        "Passer à aujourd'hui",
        "Sposta a oggi",
        "Naar vandaag verplaatsen",
        "Przenieś na dziś",
        "Перенести на сегодня",
        "Bugüne taşı",
        "Pindahkan ke hari ini",
        "今日に移す",
        "오늘로 옮기기",
    )
    val moveTomorrow = t(
        "Move to tomorrow",
        "Pasar a mañana",
        "Passar para amanhã",
        "Auf morgen verschieben",
        "Passer à demain",
        "Sposta a domani",
        "Naar morgen verplaatsen",
        "Przenieś na jutro",
        "Перенести на завтра",
        "Yarına taşı",
        "Pindahkan ke besok",
        "明日に移す",
        "내일로 옮기기",
    )
    val moveNextDay = t(
        "Move to the next day",
        "Pasar al día siguiente",
        "Passar para o dia seguinte",
        "Auf den nächsten Tag",
        "Passer au jour suivant",
        "Sposta al giorno dopo",
        "Naar de volgende dag verplaatsen",
        "Przenieś na następny dzień",
        "Перенести на следующий день",
        "Sonraki güne taşı",
        "Pindahkan ke hari berikutnya",
        "次の日に移す",
        "다음 날로 옮기기",
    )
    val moveOtherMonth = t(
        "Move to another month",
        "Llevar a otro mes",
        "Levar para outro mês",
        "In einen anderen Monat",
        "Reporter à un autre mois",
        "Sposta in un altro mese",
        "Naar een andere maand verplaatsen",
        "Przenieś na inny miesiąc",
        "Отложить на другой месяц",
        "Başka aya taşı",
        "Pindahkan ke bulan lain",
        "別の月に移す",
        "다른 달로 옮기기",
    )
    val moveElsewhere = t(
        "Move somewhere else",
        "Pasar a otro sitio",
        "Passar para outro lugar",
        "Woandershin verschieben",
        "Déplacer ailleurs",
        "Sposta altrove",
        "Ergens anders naartoe verplaatsen",
        "Przenieś gdzie indziej",
        "Перенести в другое место",
        "Başka yere taşı",
        "Pindahkan ke tempat lain",
        "ほかの場所に移す",
        "다른 곳으로 옮기기",
    )
    val priorityOn = t(
        "Mark as priority",
        "Marcar como prioridad",
        "Marcar como prioridade",
        "Als Priorität markieren",
        "Marquer comme priorité",
        "Segna come priorità",
        "Als prioriteit markeren",
        "Oznacz jako priorytet",
        "Отметить как приоритет",
        "Öncelik olarak işaretle",
        "Tandai sebagai prioritas",
        "優先にする",
        "우선순위로 표시",
    )
    val priorityOff = t(
        "Remove priority",
        "Quitar prioridad",
        "Tirar prioridade",
        "Priorität entfernen",
        "Retirer la priorité",
        "Togli la priorità",
        "Prioriteit verwijderen",
        "Usuń priorytet",
        "Убрать приоритет",
        "Önceliği kaldır",
        "Hilangkan prioritas",
        "優先を外す",
        "우선순위 해제",
    )
    val otherMarks = t(
        "Other margin marks",
        "Otras marcas del margen",
        "Outras marcas da margem",
        "Weitere Randzeichen",
        "Autres marques de la marge",
        "Altri segni a margine",
        "Andere tekens in de kantlijn",
        "Inne znaki na marginesie",
        "Другие пометки на полях",
        "Diğer kenar işaretleri",
        "Tanda margin lainnya",
        "余白のほかの印",
        "다른 여백 표시",
    )
    val backToOptions = t(
        "Back to the options",
        "Volver a las opciones",
        "Voltar às opções",
        "Zurück zu den Optionen",
        "Retour aux options",
        "Torna alle opzioni",
        "Terug naar de opties",
        "Wróć do opcji",
        "Назад к вариантам",
        "Seçeneklere dön",
        "Kembali ke pilihan",
        "選択肢に戻る",
        "옵션으로 돌아가기",
    )

    // What each action does, under its name (docs/pantallas.md 5.6): the consequence, never an instruction.
    val doneHow = t(
        "Stays where it is, marked with an x.",
        "Se queda en su sitio, marcada con una x.",
        "Fica onde está, marcada com um x.",
        "Bleibt, wo sie ist, mit einem x markiert.",
        "Reste à sa place, marquée d'un x.",
        "Resta dov'è, segnata con una x.",
        "Blijft op zijn plek, gemarkeerd met een x.",
        "Zostaje na swoim miejscu, oznaczone x.",
        "Остаётся на своём месте, отмечена знаком x.",
        "Olduğu yerde kalır, x ile işaretlenir.",
        "Tetap di tempatnya, ditandai dengan x.",
        "元の場所に残り、x印が付きます。",
        "제자리에 남고 x 표시가 붙어요.",
    )
    val otherMonthHow = t(
        "You pick the month and it waits in Future.",
        "Eliges el mes y espera en Futuro.",
        "Você escolhe o mês e ela espera no Futuro.",
        "Du wählst den Monat, dort wartet sie in Zukunft.",
        "Tu choisis le mois et elle attend dans Futur.",
        "Scegli il mese e aspetta in Futuro.",
        "Je kiest de maand en de taak wacht in Toekomst.",
        "Wybierasz miesiąc, a zadanie czeka w Przyszłości.",
        "Выбираешь месяц, и она ждёт в Будущем.",
        "Ayı sen seçersin, Gelecek'te bekler.",
        "Kamu memilih bulannya, lalu tugasnya menunggu di Mendatang.",
        "月を選ぶと、未来で待ちます。",
        "달을 고르면 미래에서 기다려요.",
    )
    val discardHow = t(
        "No longer needed. It stays crossed out.",
        "Ya no hace falta. Se queda tachada.",
        "Não é mais necessária. Fica riscada.",
        "Nicht mehr nötig. Bleibt durchgestrichen.",
        "Plus nécessaire. Elle reste barrée.",
        "Non serve più. Resta barrata.",
        "Niet meer nodig. Blijft doorgestreept.",
        "Nie jest już potrzebne. Zostaje przekreślone.",
        "Больше не нужна. Остаётся зачёркнутой.",
        "Artık gerekmiyor. Üstü çizili kalır.",
        "Tidak diperlukan lagi. Tetap tercoret.",
        "もう必要ありません。取り消し線が引かれて残ります。",
        "더는 필요 없어요. 취소선이 그어진 채 남아요.",
    )
    val reopenHow = t(
        "It's open again.",
        "Vuelve a estar pendiente.",
        "Volta a ficar pendente.",
        "Ist wieder offen.",
        "Elle est de nouveau à faire.",
        "È di nuovo aperta.",
        "Staat weer open.",
        "Znów jest otwarte.",
        "Снова открыта.",
        "Yeniden açık olur.",
        "Terbuka lagi.",
        "未完了に戻ります。",
        "다시 미완료가 돼요.",
    )
    val priorityOnHow = t(
        "An asterisk in the margin.",
        "Un asterisco en el margen.",
        "Um asterisco na margem.",
        "Ein Sternchen am Rand.",
        "Un astérisque dans la marge.",
        "Un asterisco a margine.",
        "Een sterretje in de kantlijn.",
        "Gwiazdka na marginesie.",
        "Звёздочка на полях.",
        "Kenarda bir yıldız olur.",
        "Tanda bintang di margin.",
        "余白にアスタリスクが付きます。",
        "여백에 별표가 붙어요.",
    )
    val priorityOffHow = t(
        "The asterisk leaves the margin.",
        "Se va el asterisco del margen.",
        "O asterisco sai da margem.",
        "Das Sternchen verschwindet vom Rand.",
        "L'astérisque quitte la marge.",
        "L'asterisco sparisce dal margine.",
        "Het sterretje verdwijnt uit de kantlijn.",
        "Gwiazdka znika z marginesu.",
        "Звёздочка уходит с полей.",
        "Yıldız kenardan kalkar.",
        "Tanda bintang hilang dari margin.",
        "余白のアスタリスクが消えます。",
        "여백의 별표가 사라져요.",
    )
    val otherMarksHow = t(
        "Inspiration or explore.",
        "Inspiración o explorar.",
        "Inspiração ou explorar.",
        "Inspiration oder Erkunden.",
        "Inspiration ou explorer.",
        "Ispirazione o esplora.",
        "Inspiratie of verkennen.",
        "Inspiracja albo do zbadania.",
        "Вдохновение или исследование.",
        "İlham ya da keşfet.",
        "Inspirasi atau jelajahi.",
        "ひらめきか調べもの。",
        "영감 또는 알아보기.",
    )
    val elsewhereHow = t(
        "A day this month, the month's tasks or a list.",
        "Un día de este mes, las tareas del mes o una lista.",
        "Um dia deste mês, as tarefas do mês ou uma lista.",
        "Ein Tag in diesem Monat, die Aufgaben des Monats oder eine Liste.",
        "Un jour de ce mois, les tâches du mois ou une liste.",
        "Un giorno di questo mese, le attività del mese o una lista.",
        "Een dag van deze maand, de taken van de maand of een lijst.",
        "Dzień tego miesiąca, zadania miesiąca albo lista.",
        "День этого месяца, задачи месяца или список.",
        "Bu aydan bir gün, ayın görevleri ya da bir liste.",
        "Satu hari di bulan ini, tugas bulan ini, atau satu daftar.",
        "この月のある日、月のタスク、またはリスト。",
        "이번 달의 하루, 이달의 할 일 또는 목록.",
    )
    val deleteHow = t(
        "It leaves the journal. You can undo it for a few seconds.",
        "Desaparece del diario. Puedes deshacerlo unos segundos.",
        "Sai do diário. Dá para desfazer por alguns segundos.",
        "Verschwindet aus dem Journal. Ein paar Sekunden lang rückgängig zu machen.",
        "Elle quitte le journal. Tu peux annuler pendant quelques secondes.",
        "Sparisce dal diario. Puoi annullare per qualche secondo.",
        "Verdwijnt uit het dagboek. Je kunt het een paar seconden ongedaan maken.",
        "Znika z dziennika. Możesz to cofnąć przez kilka sekund.",
        "Исчезает из дневника. Отменить можно в течение нескольких секунд.",
        "Günlükten kalkar. Birkaç saniye boyunca geri alabilirsin.",
        "Hilang dari jurnal. Bisa diurungkan selama beberapa detik.",
        "ジャーナルから消えます。数秒間は元に戻せます。",
        "다이어리에서 사라져요. 몇 초 동안은 되돌릴 수 있어요.",
    )

    /** "En agosto queda una >." with the review's [month], "Aquí queda una >." without one. [mark] is the method's own sign. */
    fun leavesMark(mark: String, month: YearMonth? = null): String {
        val m = month?.let { monthNames()[it.month.ordinal] }
        return if (m == null) {
            t(
                "A $mark stays here.",
                "Aquí queda una $mark.",
                "Aqui fica um $mark.",
                "Hier bleibt ein $mark.",
                "Ici reste un $mark.",
                "Qui resta un $mark.",
                "Hier blijft een $mark staan.",
                "Tu zostaje znak $mark.",
                "Здесь остаётся знак $mark.",
                "Burada bir $mark kalır.",
                "Di sini tersisa tanda $mark.",
                "ここに ${mark} が残ります。",
                "${mark} 표시가 여기에 남아요.",
            )
        } else {
            t(
                "A $mark stays in $m.",
                "En $m queda una $mark.",
                "Em $m fica um $mark.",
                "Im $m bleibt ein $mark.",
                "En $m reste un $mark.",
                "In $m resta un $mark.",
                "In $m blijft een $mark staan.",
                "${m.replaceFirstChar { it.uppercase() }}: zostaje znak $mark.",
                "Знак $mark остаётся в месяце: $m.",
                "$m ayında bir $mark kalır.",
                "Di bulan $m tersisa tanda $mark.",
                "${m}に ${mark} が残ります。",
                "${m}에 ${mark} 표시가 남아요.",
            )
        }
    }

    fun waitsIn(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        return t(
            "Waits in Future, in $m.",
            "Espera en Futuro, en $m.",
            "Espera no Futuro, em $m.",
            "Wartet in Zukunft, im $m.",
            "Attend dans Futur, en $m.",
            "Aspetta in Futuro, in $m.",
            "Wacht in Toekomst, in $m.",
            "Czeka w Przyszłości: $m.",
            "Ждёт в Будущем: $m.",
            "Gelecek'te, $m ayında bekler.",
            "Menunggu di Mendatang, bulan $m.",
            "未来の${m}で待っています。",
            "미래의 ${m}에서 기다려요.",
        )
    }

    /**
     * Where an entry is, in the middle of a sentence (docs/textos.md 4): "hoy, martes 29", "el 12 de
     * septiembre", "la lista Viaje". [today] decides hoy, mañana and ayer; [list] is a collection's title.
     */
    fun placeLabel(place: Place, today: LocalDate, list: String?): String = when (place) {
        is Place.Daily -> {
            val d = place.date
            val phrase = dayPhrase(d, sameMonth = d.year == today.year && d.month == today.month)
            when (daysBetween(today, d)) {
                0 -> t(
                    "today, $phrase",
                    "hoy, $phrase",
                    "hoje, $phrase",
                    "heute, $phrase",
                    "aujourd'hui, $phrase",
                    "oggi, $phrase",
                    "vandaag, $phrase",
                    "dziś, $phrase",
                    "сегодня, $phrase",
                    "bugün, $phrase",
                    "hari ini, $phrase",
                    "今日(${phrase})",
                    "오늘, ${phrase}",
                )
                1 -> t(
                    "tomorrow, $phrase",
                    "mañana, $phrase",
                    "amanhã, $phrase",
                    "morgen, $phrase",
                    "demain, $phrase",
                    "domani, $phrase",
                    "morgen, $phrase",
                    "jutro, $phrase",
                    "завтра, $phrase",
                    "yarın, $phrase",
                    "besok, $phrase",
                    "明日(${phrase})",
                    "내일, ${phrase}",
                )
                -1 -> t(
                    "yesterday, $phrase",
                    "ayer, $phrase",
                    "ontem, $phrase",
                    "gestern, $phrase",
                    "hier, $phrase",
                    "ieri, $phrase",
                    "gisteren, $phrase",
                    "wczoraj, $phrase",
                    "вчера, $phrase",
                    "dün, $phrase",
                    "kemarin, $phrase",
                    "昨日(${phrase})",
                    "어제, ${phrase}",
                )
                else -> t(phrase, "el $phrase", phrase, phrase, phrase, "$phrase", phrase, phrase, "$phrase", phrase, "$phrase", phrase, phrase)
            }
        }
        is Place.Monthly -> if (place.day != null) {
            monthDayLabel(place.month, place.day, withYear = place.month.year != today.year)
        } else {
            val m = monthNames()[place.month.month.ordinal]
            t(
                "$m's tasks",
                "las tareas de $m",
                "as tarefas de $m",
                "Aufgaben im $m",
                "les tâches ${frDe(m)}",
                "attività di $m",
                "de taken van $m",
                "zadania na $m",
                "задачи месяца ($m)",
                "$m ayının görevleri",
                "tugas $m",
                "${m}のタスク",
                "${m} 할 일",
            )
        }
        is Place.Future -> {
            val where = if (place.day != null) {
                monthDayLabel(place.month, place.day, withYear = place.month.year != today.year)
            } else if (place.month.year != today.year) {
                monthYear(place.month)
            } else {
                monthNames()[place.month.month.ordinal]
            }
            t(
                "$where, in Future",
                "$where, en Futuro",
                "$where, no Futuro",
                "$where, in Zukunft",
                "$where, dans Futur",
                "$where, in Futuro",
                "$where, in Toekomst",
                "$where, w Przyszłości",
                "Будущее, $where",
                "$where, Gelecek'te",
                "$where, di Mendatang",
                "未来の${where}",
                "${where}, 미래",
            )
        }
        is Place.InCollection -> t(
            "the list $list",
            "la lista $list",
            "a lista $list",
            "Liste $list",
            "la liste $list",
            "lista $list",
            "de lijst $list",
            "lista $list",
            "список $list",
            "$list listesi",
            "daftar $list",
            "リスト「${list}」",
            "${list} 목록",
        )
    }

    /** "el 12 de septiembre", "September 12", "le 12 septembre": a day of a month, with its article where the language puts one. */
    private fun monthDayLabel(month: YearMonth, day: Int, withYear: Boolean): String {
        val short = shortDate(LocalDate(month.year, month.month, day))
        val date = if (withYear) withYear(short, month.year) else short
        return t(
            date,
            "el $date",
            date,
            date,
            "le $date",
            "${if (date.startsWith("8 ") || date.startsWith("11 ")) "l'" else "il "}$date",
            date,
            date,
            date,
            date,
            "tanggal $date",
            date,
            date,
        )
    }

    /** "martes 29", "Saturday, August 2": the weekday in lower case where the language writes it so. */
    private fun dayPhrase(d: LocalDate, sameMonth: Boolean): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        if (sameMonth) return t(
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}.",
            "$w ${frDay(d.day)}",
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}",
            "${d.day} $w",
            "$w, ${d.day}",
            "${d.day}日 ${w}",
            "${d.day}일 ${w}",
        )
        return t(
            "$w, ${shortDate(d)}",
            "$w ${shortDate(d)}",
            "$w, ${shortDate(d)}",
            "$w, ${shortDate(d)}",
            "$w ${shortDate(d)}",
            "$w ${shortDate(d)}",
            "$w ${shortDate(d)}",
            "$w, ${shortDate(d)}",
            "$w, ${shortDate(d)}",
            "${shortDate(d)} $w",
            "$w, ${shortDate(d)}",
            "${shortDate(d)} ${w}",
            "${shortDate(d)} ${w}",
        )
    }

    /** Spanish joins "a" and "de" with "el": "al sábado", "del 12 de septiembre". */
    private fun esA(label: String) = if (label.startsWith("el ")) "al ${label.drop(3)}" else "a $label"
    private fun esDe(label: String) = if (label.startsWith("el ")) "del ${label.drop(3)}" else "de $label"

    fun movedTo(label: String) = t(
        "Moved to $label",
        "Pasada ${esA(label)}",
        "Passada para $label",
        "Verschoben: $label",
        "Déplacée vers $label",
        "Spostata: $label",
        "Verplaatst naar $label",
        "Przeniesione: $label",
        "Перенесено: $label",
        "Taşındı: $label",
        "Dipindahkan ke $label",
        "移動先: ${label}",
        "옮긴 곳: ${label}",
    )
    fun scheduledTo(label: String) = t(
        "Moved to $label",
        "Llevada ${esA(label)}",
        "Levada para $label",
        "Verschoben: $label",
        "Reportée vers $label",
        "Spostata: $label",
        "Verplaatst naar $label",
        "Przeniesione: $label",
        "Отложено: $label",
        "Taşındı: $label",
        "Dipindahkan ke $label",
        "移動先: ${label}",
        "옮긴 곳: ${label}",
    )
    fun cameFrom(label: String) = t(
        "Came from $label",
        "Viene ${esDe(label)}",
        "Origem: $label",
        "Kommt von: $label",
        "Origine : $label",
        "Origine: $label",
        "Komt van $label",
        "Pochodzi z: $label",
        "Источник: $label",
        "Geldiği yer: $label",
        "Berasal dari $label",
        "移動元: ${label}",
        "옮겨 온 곳: ${label}",
    )
    fun copiesTo(label: String) = sentence(t(
        "A copy goes to $label",
        "Se copia ${esA(label)}",
        "Uma cópia vai para $label",
        "Kopie: $label",
        "Une copie va vers $label",
        "Copia in: $label",
        "Een kopie gaat naar $label",
        "Kopia trafia do: $label",
        "Копия появится: $label",
        "Kopyası şuraya gider: $label",
        "Disalin ke $label",
        "${label}にコピーされます。",
        "사본이 생길 곳: ${label}",
    ))
    fun copyIsAt(label: String) = sentence(t(
        "Copy: $label",
        "Está en $label",
        "Cópia: $label",
        "Kopie: $label",
        "Copie : $label",
        "Copia: $label",
        "Kopie: $label",
        "Kopia: $label",
        "Копия: $label",
        "Kopya: $label",
        "Salinan ada di $label",
        "コピーは${label}にあります。",
        "사본: ${label}",
    ))

    /** A label that already ends a sentence ("24." in German) gets no second full stop. */
    fun sentence(text: String) = if (text.endsWith(".") || text.endsWith("。")) text else text + fullStop()

    // The composer (docs/pantallas.md 5.2): what is being written and where it lands.
    fun composeHint(bullet: Bullet): String = when (bullet) {
        Bullet.TASK -> t(
            "New task...",
            "Nueva tarea...",
            "Nova tarefa...",
            "Neue Aufgabe...",
            "Nouvelle tâche...",
            "Nuova attività...",
            "Nieuwe taak...",
            "Nowe zadanie...",
            "Новая задача...",
            "Yeni görev...",
            "Tugas baru...",
            "新しいタスク...",
            "새 할 일...",
        )
        Bullet.EVENT -> t(
            "New event...",
            "Nuevo evento...",
            "Novo evento...",
            "Neues Ereignis...",
            "Nouvel événement...",
            "Nuovo evento...",
            "Nieuwe gebeurtenis...",
            "Nowe wydarzenie...",
            "Новое событие...",
            "Yeni etkinlik...",
            "Acara baru...",
            "新しいイベント...",
            "새 일정...",
        )
        Bullet.NOTE -> t(
            "New note...",
            "Nueva nota...",
            "Nova nota...",
            "Neue Notiz...",
            "Nouvelle note...",
            "Nuova nota...",
            "Nieuwe notitie...",
            "Nowa notatka...",
            "Новая заметка...",
            "Yeni not...",
            "Catatan baru...",
            "新しいメモ...",
            "새 메모...",
        )
    }

    /** The word before the composer's destination, which goes in bold: "En **hoy**", "For **today**". */
    val composeFor = t("For", "En", "Para", "Für", "Pour", "Per", "Voor", "Na:", "Куда:", "Şuraya:", "Untuk", "追加先", "적는 곳:")

    /** The composer's destination (docs/pantallas.md 5.2). [list] is set for a collection, and is its title. */
    fun targetLabel(place: Place, today: LocalDate, list: String?): String = when (place) {
        is Place.Daily -> when (daysBetween(today, place.date)) {
            0 -> t("today", "hoy", "hoje", "heute", "aujourd'hui", "oggi", "vandaag", "dziś", "сегодня", "bugün", "hari ini", "今日", "오늘")
            1 -> t("tomorrow", "mañana", "amanhã", "morgen", "demain", "domani", "morgen", "jutro", "завтра", "yarın", "besok", "明日", "내일")
            else -> dayPhrase(place.date, sameMonth = true).let { t(it, "el $it", it, it, it, it, it, it, it, it, it, it, it) }
        }
        is Place.Monthly -> if (place.day != null) {
            monthDayLabel(place.month, place.day, withYear = false)
        } else {
            val m = monthNames()[place.month.month.ordinal]
            t(
                "$m's tasks",
                "tareas de $m",
                "tarefas de $m",
                "Aufgaben im $m",
                "tâches ${frDe(m)}",
                "le attività di $m",
                "de taken van $m",
                "zadania na $m",
                "задачи месяца ($m)",
                "$m ayının görevleri",
                "tugas $m",
                "${m}のタスク",
                "${m} 할 일",
            )
        }
        is Place.Future -> if (place.day != null) {
            monthDayLabel(place.month, place.day, withYear = place.month.year != today.year)
        } else if (place.month.year != today.year) {
            monthYear(place.month)
        } else {
            monthNames()[place.month.month.ordinal]
        }
        is Place.InCollection -> list.orEmpty()
    }

    fun added(bullet: Bullet, label: String): String = when (bullet) {
        Bullet.TASK -> sentence(t(
            "Task added for $label",
            "Tarea añadida en $label",
            "Tarefa adicionada para $label",
            "Aufgabe hinzugefügt: $label",
            "Tâche ajoutée pour $label",
            "Attività aggiunta: $label",
            "Taak toegevoegd voor $label",
            "Dodano zadanie: $label",
            "Задача добавлена: $label",
            "Görev eklendi: $label",
            "Tugas ditambahkan untuk $label",
            "${label}にタスクを追加しました。",
            "할 일을 추가했어요: ${label}",
        ))
        Bullet.EVENT -> sentence(t(
            "Event added for $label",
            "Evento añadido en $label",
            "Evento adicionado para $label",
            "Ereignis hinzugefügt: $label",
            "Événement ajouté pour $label",
            "Evento aggiunto: $label",
            "Gebeurtenis toegevoegd voor $label",
            "Dodano wydarzenie: $label",
            "Событие добавлено: $label",
            "Etkinlik eklendi: $label",
            "Acara ditambahkan untuk $label",
            "${label}にイベントを追加しました。",
            "일정을 추가했어요: ${label}",
        ))
        Bullet.NOTE -> sentence(t(
            "Note added for $label",
            "Nota añadida en $label",
            "Nota adicionada para $label",
            "Notiz hinzugefügt: $label",
            "Note ajoutée pour $label",
            "Nota aggiunta: $label",
            "Notitie toegevoegd voor $label",
            "Dodano notatkę: $label",
            "Заметка добавлена: $label",
            "Not eklendi: $label",
            "Catatan ditambahkan untuk $label",
            "${label}にメモを追加しました。",
            "메모를 추가했어요: ${label}",
        ))
    }

    // The line that confirms an action, with undo (docs/pantallas.md 5.8).
    val toastDone = t("Done.", "Hecha.", "Feita.", "Erledigt.", "Faite.", "Fatta.", "Voltooid.", "Zrobione.", "Выполнена.", "Yapıldı.", "Selesai.", "完了しました。", "완료했어요.")
    val toastDiscarded = t(
        "Discarded.",
        "Descartada.",
        "Descartada.",
        "Verworfen.",
        "Écartée.",
        "Scartata.",
        "Geschrapt.",
        "Odrzucone.",
        "Вычеркнута.",
        "Bırakıldı.",
        "Dibuang.",
        "破棄しました。",
        "버렸어요.",
    )
    val toastPriorityOn = t(
        "Marked as priority.",
        "Marcada como prioridad.",
        "Marcada como prioridade.",
        "Als Priorität markiert.",
        "Marquée comme priorité.",
        "Segnata come priorità.",
        "Als prioriteit gemarkeerd.",
        "Oznaczone jako priorytet.",
        "Отмечена как приоритет.",
        "Öncelik olarak işaretlendi.",
        "Ditandai sebagai prioritas.",
        "優先にしました。",
        "우선순위로 표시했어요.",
    )
    val toastPriorityOff = t(
        "No longer a priority.",
        "Sin prioridad.",
        "Sem prioridade.",
        "Keine Priorität mehr.",
        "Plus prioritaire.",
        "Non è più una priorità.",
        "Geen prioriteit meer.",
        "To już nie priorytet.",
        "Больше не приоритет.",
        "Artık öncelikli değil.",
        "Bukan prioritas lagi.",
        "優先を外しました。",
        "우선순위를 해제했어요.",
    )
    val toastSaved = t(
        "Saved.",
        "Guardado.",
        "Salvo.",
        "Gespeichert.",
        "Enregistré.",
        "Salvato.",
        "Opgeslagen.",
        "Zapisano.",
        "Сохранено.",
        "Kaydedildi.",
        "Tersimpan.",
        "保存しました。",
        "저장했어요.",
    )

    fun toastScheduled(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        return t(
            "Moved to $m. It waits in Future.",
            "Llevada a $m. Espera en Futuro.",
            "Levada para $m. Espera no Futuro.",
            "Verschoben: $m. Wartet in Zukunft.",
            "Reportée en $m. Elle attend dans Futur.",
            "Spostata in $m. Aspetta in Futuro.",
            "Verplaatst naar $m. De taak wacht in Toekomst.",
            "Przeniesione: $m. Czeka w Przyszłości.",
            "Отложено: $m. Ждёт в Будущем.",
            "Taşındı: $m. Gelecek'te bekler.",
            "Dipindahkan ke bulan $m. Menunggu di Mendatang.",
            "${m}に移しました。未来で待っています。",
            "옮긴 곳: ${m}. 미래에서 기다려요.",
        )
    }

    // The sheet's header (docs/pantallas.md 5.6): what the entry is, in plain words.
    fun entryKind(bullet: Bullet, status: TaskStatus, priority: Boolean): String {
        val kind = when (bullet) {
            Bullet.EVENT -> bulletEvent
            Bullet.NOTE -> bulletNote
            Bullet.TASK -> when (status) {
                TaskStatus.OPEN -> t(
                    "Open task",
                    "Tarea pendiente",
                    "Tarefa pendente",
                    "Offene Aufgabe",
                    "Tâche à faire",
                    "Attività aperta",
                    "Open taak",
                    "Otwarte zadanie",
                    "Открытая задача",
                    "Açık görev",
                    "Tugas terbuka",
                    "未完了のタスク",
                    "미완료 할 일",
                )
                TaskStatus.DONE -> t(
                    "Done task",
                    "Tarea hecha",
                    "Tarefa feita",
                    "Erledigte Aufgabe",
                    "Tâche faite",
                    "Attività fatta",
                    "Voltooide taak",
                    "Zrobione zadanie",
                    "Выполненная задача",
                    "Yapılmış görev",
                    "Tugas selesai",
                    "完了したタスク",
                    "완료한 할 일",
                )
                TaskStatus.MIGRATED -> t(
                    "Moved task",
                    "Tarea pasada",
                    "Tarefa passada",
                    "Verschobene Aufgabe",
                    "Tâche déplacée",
                    "Attività spostata",
                    "Verplaatste taak",
                    "Przeniesione zadanie",
                    "Перенесённая задача",
                    "Taşınmış görev",
                    "Tugas dipindahkan",
                    "移したタスク",
                    "옮긴 할 일",
                )
                TaskStatus.SCHEDULED -> t(
                    "Task moved to another month",
                    "Tarea llevada a otro mes",
                    "Tarefa levada para outro mês",
                    "In einen anderen Monat verschoben",
                    "Tâche reportée à un autre mois",
                    "Attività spostata in un altro mese",
                    "Taak naar een andere maand verplaatst",
                    "Zadanie przeniesione na inny miesiąc",
                    "Задача, отложенная на другой месяц",
                    "Başka aya taşınmış görev",
                    "Tugas dipindahkan ke bulan lain",
                    "別の月に移したタスク",
                    "다른 달로 옮긴 할 일",
                )
                TaskStatus.IRRELEVANT -> t(
                    "Discarded task",
                    "Tarea descartada",
                    "Tarefa descartada",
                    "Verworfene Aufgabe",
                    "Tâche écartée",
                    "Attività scartata",
                    "Geschrapte taak",
                    "Odrzucone zadanie",
                    "Вычеркнутая задача",
                    "Bırakılmış görev",
                    "Tugas dibuang",
                    "破棄したタスク",
                    "버린 할 일",
                )
            }
        }
        return if (priority) kind + t(
            ", priority",
            ", prioridad",
            ", prioridade",
            ", Priorität",
            ", priorité",
            ", priorità",
            ", prioriteit",
            ", priorytet",
            ", приоритет",
            ", öncelikli",
            ", prioritas",
            "、優先",
            ", 우선순위",
        ) else kind
    }

    val toToday = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
    val toTomorrow = t("Tomorrow", "Mañana", "Amanhã", "Morgen", "Demain", "Domani", "Morgen", "Jutro", "Завтра", "Yarın", "Besok", "明日", "내일")
    val toThisMonth = t(
        "This month's tasks",
        "Tareas de este mes",
        "Tarefas deste mês",
        "Aufgaben dieses Monats",
        "Tâches de ce mois",
        "Attività di questo mese",
        "Taken van deze maand",
        "Zadania tego miesiąca",
        "Задачи этого месяца",
        "Bu ayın görevleri",
        "Tugas bulan ini",
        "今月のタスク",
        "이번 달 할 일",
    )
    val toDayOfMonth = t(
        "A day this month",
        "Un día de este mes",
        "Um dia deste mês",
        "Ein Tag in diesem Monat",
        "Un jour de ce mois",
        "Un giorno di questo mese",
        "Een dag van deze maand",
        "Dzień tego miesiąca",
        "День этого месяца",
        "Bu aydan bir gün",
        "Satu hari di bulan ini",
        "今月のある日",
        "이번 달의 하루",
    )
    val toCollection = t(
        "To a list",
        "A una lista",
        "Para uma lista",
        "In eine Liste",
        "Vers une liste",
        "A una lista",
        "Naar een lijst",
        "Do listy",
        "В список",
        "Bir listeye",
        "Ke daftar",
        "リストへ",
        "목록으로",
    )
    val dayField = t("day", "día", "dia", "Tag", "jour", "giorno", "dag", "dzień", "день", "gün", "tanggal", "日", "일")
    val moveAction = t("Move", "Pasar", "Passar", "Verschieben", "Déplacer", "Sposta", "Verplaatsen", "Przenieś", "Перенести", "Taşı", "Pindahkan", "移す", "옮기기")

    fun dayOutOfRange(month: YearMonth, day: Int): String {
        val name = monthName(month)
        return t(
            "$name has no day $day.",
            "$name no tiene día $day.",
            "$name não tem dia $day.",
            "Der $name hat keinen $day. Tag.",
            "$name n'a pas de jour $day.",
            "$name non ha il giorno $day.",
            "$name heeft geen dag $day.",
            "$name nie ma dnia $day.",
            "$name: нет дня $day.",
            "$name ayının $day. günü yok.",
            "$name tidak punya tanggal $day.",
            "${name}に${day}日はありません。",
            "${name}에는 ${day}일이 없어요.",
        )
    }

    val dayPast = t(
        "That day has passed.",
        "Ese día ya pasó.",
        "Esse dia já passou.",
        "Dieser Tag ist vorbei.",
        "Ce jour est passé.",
        "Quel giorno è già passato.",
        "Die dag is voorbij.",
        "Ten dzień już minął.",
        "Этот день уже прошёл.",
        "O gün geçti.",
        "Hari itu sudah lewat.",
        "その日はもう過ぎています。",
        "그날은 이미 지났어요.",
    )
    val newList = t(
        "New list",
        "Nueva lista",
        "Nova lista",
        "Neue Liste",
        "Nouvelle liste",
        "Nuova lista",
        "Nieuwe lijst",
        "Nowa lista",
        "Новый список",
        "Yeni liste",
        "Daftar baru",
        "新しいリスト",
        "새 목록",
    )
    val showMoreMonths = t(
        "Show more months",
        "Ver más meses",
        "Ver mais meses",
        "Mehr Monate zeigen",
        "Voir plus de mois",
        "Mostra altri mesi",
        "Meer maanden tonen",
        "Pokaż więcej miesięcy",
        "Показать больше месяцев",
        "Daha fazla ay göster",
        "Lihat bulan lainnya",
        "さらに月を表示",
        "더 많은 달 보기",
    )
    val entryDeleted = t(
        "Entry deleted.",
        "Entrada borrada.",
        "Entrada apagada.",
        "Eintrag gelöscht.",
        "Entrée supprimée.",
        "Voce eliminata.",
        "Item verwijderd.",
        "Wpis usunięty.",
        "Запись удалена.",
        "Kayıt silindi.",
        "Entri dihapus.",
        "項目を削除しました。",
        "항목을 삭제했어요.",
    )
    val collectionDeleted = t(
        "List deleted.",
        "Lista borrada.",
        "Lista apagada.",
        "Liste gelöscht.",
        "Liste supprimée.",
        "Lista eliminata.",
        "Lijst verwijderd.",
        "Lista usunięta.",
        "Список удалён.",
        "Liste silindi.",
        "Daftar dihapus.",
        "リストを削除しました。",
        "목록을 삭제했어요.",
    )
    val rowDeleted = t(
        "Row deleted.",
        "Fila borrada.",
        "Linha apagada.",
        "Zeile gelöscht.",
        "Ligne supprimée.",
        "Riga eliminata.",
        "Rij verwijderd.",
        "Wiersz usunięty.",
        "Строка удалена.",
        "Satır silindi.",
        "Baris dihapus.",
        "行を削除しました。",
        "줄을 삭제했어요.",
    )

    /** docs/pantallas.md 1.5: the name of each glyph, for the screen reader. */
    fun glyphName(bullet: Bullet, status: TaskStatus): String = when (bullet) {
        Bullet.EVENT -> bulletEvent
        Bullet.NOTE -> bulletNote
        Bullet.TASK -> when (status) {
            TaskStatus.DONE -> t(
                "Done task",
                "Tarea hecha",
                "Tarefa feita",
                "Erledigte Aufgabe",
                "Tâche faite",
                "Attività fatta",
                "Voltooide taak",
                "Zadanie zrobione",
                "Выполненная задача",
                "Yapılmış görev",
                "Tugas selesai",
                "完了したタスク",
                "완료한 할 일",
            )
            TaskStatus.MIGRATED -> t(
                "Migrated task",
                "Tarea migrada",
                "Tarefa migrada",
                "Migrierte Aufgabe",
                "Tâche migrée",
                "Attività migrata",
                "Gemigreerde taak",
                "Zadanie zmigrowane",
                "Мигрированная задача",
                "Aktarılmış görev",
                "Tugas dimigrasikan",
                "移行済みのタスク",
                "이월한 할 일",
            )
            TaskStatus.SCHEDULED -> t(
                "Scheduled task",
                "Tarea programada",
                "Tarefa agendada",
                "Eingeplante Aufgabe",
                "Tâche planifiée",
                "Attività programmata",
                "Ingeplande taak",
                "Zadanie zaplanowane",
                "Запланированная задача",
                "Planlanmış görev",
                "Tugas dijadwalkan",
                "スケジュール済みのタスク",
                "예정한 할 일",
            )
            TaskStatus.IRRELEVANT -> t(
                "Discarded task",
                "Tarea descartada",
                "Tarefa descartada",
                "Verworfene Aufgabe",
                "Tâche écartée",
                "Attività scartata",
                "Geschrapte taak",
                "Zadanie odrzucone",
                "Вычеркнутая задача",
                "Bırakılmış görev",
                "Tugas dibuang",
                "破棄したタスク",
                "버린 할 일",
            )
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

    val calendarToday = t(
        "On the calendar",
        "En el calendario",
        "No calendário",
        "Im Kalender",
        "Au calendrier",
        "Nel calendario",
        "In de kalender",
        "W kalendarzu",
        "В календаре",
        "Takvimde",
        "Di kalender",
        "カレンダーにあるもの",
        "캘린더에서",
    )
    val noticeCorrupt = t(
        "Couldn't read the journal. The files were set aside and nothing was deleted.",
        "No se ha podido leer el diario. Los ficheros se han guardado aparte y no se ha borrado nada.",
        "Não foi possível ler o diário. Os arquivos foram guardados à parte e nada foi apagado.",
        "Das Journal konnte nicht gelesen werden. Die Dateien wurden beiseitegelegt, gelöscht wurde nichts.",
        "Impossible de lire le journal. Les fichiers ont été mis de côté et rien n'a été supprimé.",
        "Impossibile leggere il diario. I file sono stati messi da parte e non è stato eliminato nulla.",
        "Het dagboek kon niet worden gelezen. De bestanden zijn apart bewaard en er is niets verwijderd.",
        "Nie udało się odczytać dziennika. Pliki odłożono na bok i nic nie zostało usunięte.",
        "Не удалось прочитать дневник. Файлы сохранены отдельно, ничего не удалено.",
        "Günlük okunamadı. Dosyalar bir kenara alındı, hiçbir şey silinmedi.",
        "Jurnal tidak bisa dibaca. Berkasnya disimpan terpisah dan tidak ada yang dihapus.",
        "ジャーナルを読み込めませんでした。ファイルは別に保存してあり、何も削除されていません。",
        "다이어리를 읽지 못했어요. 파일은 따로 보관했고 아무것도 삭제하지 않았어요.",
    )
    val noticeSaveFailed = t(
        "Couldn't save. I'll try again with your next change.",
        "No se ha podido guardar. Lo intento otra vez con tu próximo cambio.",
        "Não foi possível salvar. Vou tentar de novo na sua próxima alteração.",
        "Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut.",
        "Impossible d'enregistrer. Je réessaierai avec ta prochaine modification.",
        "Impossibile salvare. Riprovo con la tua prossima modifica.",
        "Opslaan is mislukt. Ik probeer het opnieuw bij je volgende wijziging.",
        "Nie udało się zapisać. Spróbuję ponownie przy następnej zmianie.",
        "Не удалось сохранить. Попробую снова при следующем изменении.",
        "Kaydedilemedi. Bir sonraki değişikliğinde yeniden deneyeceğim.",
        "Tidak bisa menyimpan. Aku coba lagi pada perubahanmu berikutnya.",
        "保存できませんでした。次の変更のときに、もう一度試します。",
        "저장하지 못했어요. 다음 변경 때 다시 시도할게요.",
    )

    fun offerReminder(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "Remind you to go over the day at $time?",
            "¿Te aviso para repasar el día a las $time?",
            "Quer que eu avise para repassar o dia às $time?",
            "Soll ich dich um $time erinnern, den Tag durchzugehen?",
            "Je te rappelle de relire ta journée à $time ?",
            "Vuoi che ti ricordi alle $time di rivedere la giornata?",
            "Zal ik je om $time herinneren om de dag door te nemen?",
            "Przypomnieć ci o $time, żeby przejrzeć dzień?",
            "Напомнить тебе в $time просмотреть день?",
            "Günü gözden geçirmen için saat $time olduğunda hatırlatayım mı?",
            "Ingatkan kamu untuk meninjau harimu pukul $time?",
            "${time}に1日の見直しをお知らせしますか？",
            "${time}에 하루를 살펴보도록 알려 드릴까요?",
        )
    }

    /** The title of the unclosed month's card (docs/pantallas.md 6.3): "Agosto tiene 4 tareas sin cerrar". */
    fun unclosedMonth(month: YearMonth, n: Int): String {
        val name = monthName(month)
        val w = wordTask(n)
        return t(
            "$name still has $n open $w",
            "$name tiene $n $w sin cerrar",
            "$name tem $n $w sem fechar",
            "Im $name ${if (isSingular(n)) "ist" else "sind"} $n $w offen",
            "$name a $n $w non ${if (isSingular(n)) "clôturée" else "clôturées"}",
            "$name ha ancora $n $w ${word(n, "aperta|aperte")}",
            "$name heeft nog $n open $w",
            "$name ma jeszcze $n ${word(n, "otwarte|otwarte|otwartych")} $w",
            "$name: $n ${word(n, "задача не закрыта|задачи не закрыты|задач не закрыто")}",
            "$name ayında $n $w hâlâ açık",
            "$name masih punya $n $w terbuka",
            "${name}は未完了の${w}が${n}件",
            "${name}에 미완료 할 일이 ${n}개 남아 있어요",
        )
    }
    val unclosedBody = t(
        "Decide what happens to each one: done, to today, to another month or discarded. Nothing moves unless you say so.",
        "Decide qué pasa con cada una: hecha, a hoy, a otro mes o descartada. Nada se mueve si no lo dices tú.",
        "Decida o que acontece com cada uma: feita, para hoje, para outro mês ou descartada. Nada se move se você não disser.",
        "Entscheide, was mit jeder passiert: erledigt, auf heute, in einen anderen Monat oder verworfen. Nichts bewegt sich, wenn du es nicht sagst.",
        "Décide ce que devient chacune : faite, à aujourd'hui, à un autre mois ou écartée. Rien ne bouge si tu ne le dis pas.",
        "Decidi cosa succede a ciascuna: fatta, a oggi, in un altro mese o scartata. Niente si sposta se non lo dici tu.",
        "Bepaal wat er met elke taak gebeurt: voltooid, naar vandaag, naar een andere maand of geschrapt. Er wordt niets verplaatst tenzij jij het zegt.",
        "Zdecyduj, co dzieje się z każdym zadaniem: zrobione, na dziś, na inny miesiąc albo odrzucone. Nic się nie przenosi, dopóki tego nie powiesz.",
        "Реши, что будет с каждой: выполнена, перенесена на сегодня, отложена на другой месяц или вычеркнута. Ничего не сдвинется, пока ты не скажешь.",
        "Her biri için karar ver: yapıldı, bugüne, başka aya ya da bırakıldı. Sen söylemedikçe hiçbir şey taşınmaz.",
        "Putuskan apa yang terjadi pada masing-masing: selesai, ke hari ini, ke bulan lain, atau dibuang. Tidak ada yang berpindah kecuali kamu memintanya.",
        "それぞれどうするか決めてください: 完了、今日に移す、別の月に移す、破棄。選ぶまで、何も動きません。",
        "각각 어떻게 할지 정해 주세요: 완료, 오늘로, 다른 달로, 아니면 버리기. 직접 정하지 않으면 아무것도 움직이지 않아요.",
    )

    /** Also the review's own label (docs/pantallas.md 11.1). */
    fun reviewMonth(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        return t(
            "Review $m",
            "Repasar $m",
            "Revisar $m",
            "$m durchsehen",
            "Revoir $m",
            "Rivedi $m",
            "${m.replaceFirstChar { it.uppercase() }} doornemen",
            "Przejrzyj $m",
            "Просмотреть $m",
            "Gözden geçir: $m",
            "Tinjau $m",
            "${m}を見直す",
            "${m} 살펴보기",
        )
    }

    fun earlierOpen(n: Int): String {
        return if (isSingular(n)) {
            t(
                "$n task still open from earlier days",
                "Queda $n tarea abierta de días anteriores",
                "Resta $n tarefa aberta de dias anteriores",
                "Noch $n Aufgabe offen von früheren Tagen",
                "Il reste $n tâche ouverte des jours précédents",
                "Resta $n attività aperta dei giorni precedenti",
                "Nog $n taak open van eerdere dagen",
                "Z wcześniejszych dni zostało $n otwarte zadanie",
                "Осталась $n открытая задача с прошлых дней",
                "Önceki günlerden $n görev hâlâ açık",
                "$n tugas masih terbuka dari hari-hari sebelumnya",
                "以前の日から未完了のタスクが${n}件残っています",
                "지난 날들의 미완료 할 일이 ${n}개 남아 있어요",
            )
        } else {
            t(
                "$n tasks still open from earlier days",
                "Quedan $n tareas abiertas de días anteriores",
                "Restam $n tarefas abertas de dias anteriores",
                "Noch $n Aufgaben offen von früheren Tagen",
                "Il reste $n tâches ouvertes des jours précédents",
                "Restano $n attività aperte dei giorni precedenti",
                "Nog $n taken open van eerdere dagen",
                "Z wcześniejszych dni ${word(n, "zostało|zostały|zostało")} $n ${word(n, "otwarte zadanie|otwarte zadania|otwartych zadań")}",
                "${word(n, "Осталась|Остались|Осталось")} $n ${word(n, "открытая задача|открытые задачи|открытых задач")} с прошлых дней",
                "Önceki günlerden $n görev hâlâ açık",
                "$n tugas masih terbuka dari hari-hari sebelumnya",
                "以前の日から未完了のタスクが${n}件残っています",
                "지난 날들의 미완료 할 일이 ${n}개 남아 있어요",
            )
        }
    }
    val earlierBody = t(
        "They're from days of this month that have passed. Decide what happens to each one.",
        "Son de días de este mes que ya pasaron. Decide qué pasa con cada una.",
        "São de dias deste mês que já passaram. Decida o que acontece com cada uma.",
        "Sie sind von vergangenen Tagen dieses Monats. Entscheide, was mit jeder passiert.",
        "Elles viennent de jours passés de ce mois. Décide ce que devient chacune.",
        "Sono di giorni di questo mese già passati. Decidi cosa succede a ciascuna.",
        "Ze komen uit dagen van deze maand die al voorbij zijn. Bepaal wat er met elke taak gebeurt.",
        "To zadania z minionych dni tego miesiąca. Zdecyduj, co dzieje się z każdym z nich.",
        "Они остались с прошедших дней этого месяца. Реши, что будет с каждой.",
        "Bu ayın geçmiş günlerinden kalanlar. Her birine ne olacağına karar ver.",
        "Semuanya dari hari-hari bulan ini yang sudah lewat. Putuskan apa yang terjadi pada masing-masing.",
        "今月のすでに過ぎた日のものです。それぞれどうするか決めてください。",
        "이번 달에서 이미 지난 날들의 할 일이에요. 각각 어떻게 할지 정해 주세요.",
    )
    val reviewEarlier = t(
        "Review those days",
        "Repasar esos días",
        "Revisar esses dias",
        "Diese Tage durchsehen",
        "Revoir ces jours",
        "Rivedi quei giorni",
        "Die dagen doornemen",
        "Przejrzyj te dni",
        "Просмотреть эти дни",
        "Günleri gözden geçir",
        "Tinjau hari-hari itu",
        "それらの日を見直す",
        "지난 날들 살펴보기",
    )
    val reviewDay = t(
        "Review the day",
        "Repasar el día",
        "Revisar o dia",
        "Den Tag durchsehen",
        "Revoir la journée",
        "Rivedi la giornata",
        "De dag doornemen",
        "Przejrzyj dzień",
        "Просмотреть день",
        "Günü gözden geçir",
        "Tinjau hari",
        "この日を見直す",
        "하루 살펴보기",
    )

    /** Once, on Hoy, until "Entendido" or the first use (docs/pantallas.md 6.3). ^...^ goes in bold. */
    val hintTap = t(
        "^Tap the dot^ of a task to mark it done. ^Tap the text^ to see everything you can do with it.",
        "^Toca el punto^ de una tarea para marcarla hecha. ^Toca el texto^ para ver todo lo que puedes hacer con ella.",
        "^Toque no ponto^ de uma tarefa para marcá-la como feita. ^Toque no texto^ para ver tudo o que dá para fazer com ela.",
        "^Tippe auf den Punkt^ einer Aufgabe, um sie zu erledigen. ^Tippe auf den Text^, um alles zu sehen, was du mit ihr machen kannst.",
        "^Touche le point^ d'une tâche pour la marquer faite. ^Touche le texte^ pour voir tout ce que tu peux en faire.",
        "^Tocca il punto^ di un'attività per segnarla come fatta. ^Tocca il testo^ per vedere tutto quello che puoi farci.",
        "^Tik op de stip^ van een taak om die als voltooid te markeren. ^Tik op de tekst^ om alles te zien wat je ermee kunt doen.",
        "^Dotknij kropki^ przy zadaniu, aby oznaczyć je jako zrobione. ^Dotknij tekstu^, aby zobaczyć wszystko, co możesz z nim zrobić.",
        "^Нажми на точку^ у задачи, чтобы отметить её выполненной. ^Нажми на текст^, чтобы увидеть всё, что с ней можно сделать.",
        "Bir görevin ^noktasına dokun^ ve yapıldı olarak işaretle. Onunla yapabileceğin her şeyi görmek için ^metnine dokun^.",
        "^Ketuk titik^ pada tugas untuk menandainya selesai. ^Ketuk teks^ untuk melihat semua yang bisa kamu lakukan dengannya.",
        "タスクの^点をタップ^すると、完了にできます。^テキストをタップ^すると、できる操作がすべて表示されます。",
        "할 일의 ^점을 탭하면^ 완료로 표시돼요. ^내용을 탭하면^ 그 할 일로 할 수 있는 모든 것을 볼 수 있어요.",
    )

    /** Hoy with nothing on it (docs/pantallas.md 6.2), followed by [writeBelow]. */
    fun dayEmpty(day: LocalDate, today: LocalDate): String = when (daysBetween(today, day)) {
        0 -> t(
            "Nothing written today.",
            "Nada escrito hoy.",
            "Nada escrito hoje.",
            "Heute steht noch nichts.",
            "Rien d'écrit aujourd'hui.",
            "Niente scritto oggi.",
            "Vandaag niets opgeschreven.",
            "Dziś nic nie zapisano.",
            "Сегодня пока ничего не записано.",
            "Bugün hiçbir şey yazılmadı.",
            "Belum ada yang ditulis hari ini.",
            "今日はまだ何も書かれていません。",
            "오늘 쓴 것이 없어요.",
        )
        1 -> t(
            "Nothing written for tomorrow.",
            "Nada escrito para mañana.",
            "Nada escrito para amanhã.",
            "Für morgen steht nichts.",
            "Rien d'écrit pour demain.",
            "Niente scritto per domani.",
            "Niets opgeschreven voor morgen.",
            "Na jutro nic nie zapisano.",
            "На завтра пока ничего не записано.",
            "Yarın için hiçbir şey yazılmadı.",
            "Belum ada yang ditulis untuk besok.",
            "明日の分はまだ何も書かれていません。",
            "내일 쓴 것이 없어요.",
        )
        else -> t(
            "Nothing written on this day.",
            "Nada escrito en este día.",
            "Nada escrito neste dia.",
            "An diesem Tag steht nichts.",
            "Rien d'écrit ce jour-là.",
            "Niente scritto in questo giorno.",
            "Niets opgeschreven op deze dag.",
            "Nic nie zapisano w tym dniu.",
            "В этот день ничего не записано.",
            "Bu gün için hiçbir şey yazılmadı.",
            "Belum ada yang ditulis di tanggal ini.",
            "この日には何も書かれていません。",
            "이 날 쓴 것이 없어요.",
        )
    }
    val writeBelow = t(
        "Write below to start.",
        "Escribe abajo para empezar.",
        "Escreva abaixo para começar.",
        "Schreib unten, um anzufangen.",
        "Écris en bas pour commencer.",
        "Scrivi qui sotto per iniziare.",
        "Schrijf hieronder om te beginnen.",
        "Napisz poniżej, aby zacząć.",
        "Пиши внизу, чтобы начать.",
        "Başlamak için aşağıya yaz.",
        "Tulis di bawah untuk mulai.",
        "始めるには、下に書き込んでください。",
        "시작하려면 아래에 써 보세요.",
    )
    val shareDay = t(
        "Share this day",
        "Compartir este día",
        "Compartilhar este dia",
        "Diesen Tag teilen",
        "Partager ce jour",
        "Condividi questo giorno",
        "Deze dag delen",
        "Udostępnij ten dzień",
        "Поделиться этим днём",
        "Bu günü paylaş",
        "Bagikan hari ini",
        "この日を共有",
        "이 날 공유",
    )

    // --- 6. Mes ---------------------------------------------------------------------------

    val monthTasks = t(
        "Tasks of the month",
        "Tareas del mes",
        "Tarefas do mês",
        "Aufgaben des Monats",
        "Tâches du mois",
        "Attività del mese",
        "Taken van de maand",
        "Zadania miesiąca",
        "Задачи месяца",
        "Ayın görevleri",
        "Tugas bulan ini",
        "月のタスク",
        "이달의 할 일",
    )
    val calendarTitle = t(
        "Calendar",
        "Calendario",
        "Calendário",
        "Kalender",
        "Calendrier",
        "Calendario",
        "Kalender",
        "Kalendarz",
        "Календарь",
        "Takvim",
        "Kalender",
        "カレンダー",
        "캘린더",
    )

    val monthExplain = t(
        "Above, what happens each day. Below, what you want to do this month with no set date.",
        "Arriba, lo que pasa cada día. Abajo, lo que quieres hacer este mes sin fecha fija.",
        "Em cima, o que acontece a cada dia. Embaixo, o que você quer fazer este mês sem data fixa.",
        "Oben, was an jedem Tag passiert. Unten, was du diesen Monat ohne festes Datum tun willst.",
        "En haut, ce qui se passe chaque jour. En bas, ce que tu veux faire ce mois-ci sans date fixe.",
        "Sopra, quello che succede ogni giorno. Sotto, quello che vuoi fare questo mese senza una data fissa.",
        "Boven: wat er elke dag gebeurt. Onder: wat je deze maand wilt doen zonder vaste datum.",
        "Na górze to, co dzieje się każdego dnia. Poniżej to, co chcesz zrobić w tym miesiącu bez ustalonej daty.",
        "Сверху то, что происходит каждый день. Снизу то, что хочешь сделать в этом месяце без точной даты.",
        "Yukarıda her günün olayları. Aşağıda bu ay kesin tarihi olmadan yapmak istediklerin.",
        "Di atas, yang terjadi setiap hari. Di bawah, yang ingin kamu lakukan bulan ini tanpa tanggal pasti.",
        "上は、毎日の出来事。下は、日付を決めずにこの月にやりたいこと。",
        "위에는 날마다 있는 일, 아래에는 이번 달에 하고 싶지만 날짜를 정하지 않은 일이에요.",
    )
    val calendarHint = t(
        "Tap a day to write on it",
        "Toca un día para apuntar en él",
        "Toque num dia para anotar nele",
        "Tippe auf einen Tag, um dort zu notieren",
        "Touche un jour pour y noter",
        "Tocca un giorno per scriverci",
        "Tik op een dag om erin te schrijven",
        "Dotknij dnia, aby coś w nim zapisać",
        "Нажми на день, чтобы записать в него",
        "Yazmak için bir güne dokun",
        "Ketuk satu hari untuk mencatat di situ",
        "日をタップして書き込む",
        "날짜를 탭하면 그 날에 적어요",
    )

    /** On the picked day of Mes and the picked block of Futuro: where the composer writes. */
    val writingHere = t(
        "Writing here",
        "Aquí se apunta",
        "Anotando aqui",
        "Hier wird notiert",
        "On note ici",
        "Qui si scrive",
        "Je schrijft hier",
        "Tu zapisujesz",
        "Пишешь сюда",
        "Burada yazılıyor",
        "Mencatat di sini",
        "ここに書き込みます",
        "여기에 적는 중",
    )
    val monthTasksEmpty = t(
        "None yet.",
        "Ninguna todavía.",
        "Nenhuma ainda.",
        "Noch keine.",
        "Aucune pour l'instant.",
        "Ancora nessuna.",
        "Nog geen.",
        "Na razie żadnych.",
        "Пока ни одной.",
        "Henüz yok.",
        "Belum ada.",
        "まだありません。",
        "아직 없어요.",
    )
    val shareMonth = t(
        "Share this month",
        "Compartir este mes",
        "Compartilhar este mês",
        "Diesen Monat teilen",
        "Partager ce mois",
        "Condividi questo mese",
        "Deze maand delen",
        "Udostępnij ten miesiąc",
        "Поделиться этим месяцем",
        "Bu ayı paylaş",
        "Bagikan bulan ini",
        "この月を共有",
        "이 달 공유",
    )

    fun futureWaiting(n: Int): String {
        return if (isSingular(n)) {
            t(
                "$n Future entry is waiting for this month",
                "$n entrada de Futuro espera a este mes",
                "$n entrada do Futuro espera este mês",
                "$n Eintrag aus Zukunft wartet auf diesen Monat",
                "$n entrée de Futur attend ce mois-ci",
                "$n voce di Futuro aspetta questo mese",
                "$n item uit Toekomst wacht op deze maand",
                "$n wpis z Przyszłości czeka na ten miesiąc",
                "$n запись из Будущего ждёт этого месяца",
                "Gelecek'te bu ayı bekleyen $n kayıt var",
                "$n entri dari Mendatang menunggu bulan ini",
                "未来の項目${n}件が、この月を待っています",
                "미래에서 이번 달을 기다리는 항목이 ${n}개 있어요",
            )
        } else {
            t(
                "$n Future entries are waiting for this month",
                "$n entradas de Futuro esperan a este mes",
                "$n entradas do Futuro esperam este mês",
                "$n Einträge aus Zukunft warten auf diesen Monat",
                "$n entrées de Futur attendent ce mois-ci",
                "$n voci di Futuro aspettano questo mese",
                "$n items uit Toekomst wachten op deze maand",
                "$n ${word(n, "wpis z Przyszłości czeka|wpisy z Przyszłości czekają|wpisów z Przyszłości czeka")} na ten miesiąc",
                "$n ${word(n, "запись из Будущего ждёт|записи из Будущего ждут|записей из Будущего ждут")} этого месяца",
                "Gelecek'te bu ayı bekleyen $n kayıt var",
                "$n entri dari Mendatang menunggu bulan ini",
                "未来の項目${n}件が、この月を待っています",
                "미래에서 이번 달을 기다리는 항목이 ${n}개 있어요",
            )
        }
    }
    val futureWaitingBody = t(
        "You wrote them down for this month. Move them to the calendar, leave them or discard them.",
        "Las apuntaste para este mes. Pásalas al calendario, déjalas o descártalas.",
        "Você as anotou para este mês. Passe-as para o calendário, deixe-as ou descarte-as.",
        "Du hast sie für diesen Monat notiert. In den Kalender, lassen oder verwerfen.",
        "Tu les as notées pour ce mois. Passe-les au calendrier, laisse-les ou écarte-les.",
        "Le hai scritte per questo mese. Spostale nel calendario, lasciale o scartale.",
        "Je hebt ze voor deze maand opgeschreven. Verplaats ze naar de kalender, laat ze staan of schrap ze.",
        "Zapisano je na ten miesiąc. Przenieś je do kalendarza, zostaw albo odrzuć.",
        "Они записаны на этот месяц. Перенеси их в календарь, оставь или вычеркни.",
        "Bunları bu ay için yazdın. Takvime taşı, kalsın ya da bırak.",
        "Kamu mencatatnya untuk bulan ini. Pindahkan ke kalender, biarkan, atau buang.",
        "この月のために書いた項目です。カレンダーに移すか、そのままにするか、破棄するか選べます。",
        "이번 달로 적어 둔 항목이에요. 캘린더로 옮기거나, 그대로 두거나, 버릴 수 있어요.",
    )
    val reviewFuture = t(
        "Review Future",
        "Repasar Futuro",
        "Revisar Futuro",
        "Zukunft durchsehen",
        "Revoir Futur",
        "Rivedi Futuro",
        "Toekomst doornemen",
        "Przejrzyj Przyszłość",
        "Просмотреть Будущее",
        "Gözden geçir: Gelecek",
        "Tinjau Mendatang",
        "未来を見直す",
        "미래 살펴보기",
    )

    // --- 7. Futuro --------------------------------------------------------------------------

    fun futureSubtitle(n: Int) = t(
        "The next $n months",
        "Los próximos $n meses",
        "Os próximos $n meses",
        "Die nächsten $n Monate",
        "Les $n prochains mois",
        "I prossimi $n mesi",
        "De komende $n maanden",
        "${word(n, "Następny|Następne|Następne")} $n ${word(n, "miesiąc|miesiące|miesięcy")}",
        "Ближайшие $n ${word(n, "месяц|месяца|месяцев")}",
        "Sonraki $n ay",
        "$n bulan berikutnya",
        "これからの${n}か月",
        "앞으로 ${n}개월",
    )
    val futureExplain = t(
        "Write down what isn't due yet. When its month comes, you review it and decide: nothing drops into Today on its own.",
        "Apunta lo que aún no toca. Cuando llegue su mes, lo repasas y decides: nada baja solo a Hoy.",
        "Anote o que ainda não é para agora. Quando chegar o mês, você revisa e decide: nada desce sozinho para Hoje.",
        "Notiere, was noch nicht dran ist. Kommt sein Monat, siehst du es durch und entscheidest: nichts rutscht von allein in Heute.",
        "Note ce qui n'est pas encore pour maintenant. Quand son mois arrive, tu le revois et tu décides : rien ne descend seul dans Aujourd'hui.",
        "Scrivi ciò che non è ancora il momento di fare. Quando arriva il suo mese, lo rivedi e decidi: niente scende da solo in Oggi.",
        "Schrijf op wat nog niet aan de beurt is. Als de maand aanbreekt, neem je het door en beslis je: niets zakt vanzelf naar Vandaag.",
        "Zapisz to, na co jeszcze nie czas. Gdy nadejdzie właściwy miesiąc, przeglądasz to i decydujesz: nic samo nie trafia do zakładki Dziś.",
        "Записывай то, чему ещё не время. Когда наступит нужный месяц, ты просматриваешь запись и решаешь: само в \"Сегодня\" ничего не попадает.",
        "Zamanı henüz gelmemiş olanı yaz. Ayı geldiğinde onu gözden geçirip karar verirsin: hiçbir şey kendiliğinden Bugün'e düşmez.",
        "Catat apa yang belum waktunya. Saat bulannya tiba, kamu meninjaunya lalu memutuskan: tidak ada yang turun sendiri ke tab Hari.",
        "まだ先のことを書き留めます。その月が来たら見直して決めます。何も勝手に「今日」へ移ることはありません。",
        "아직 때가 안 된 일을 적어 두세요. 그 달이 오면 직접 살펴보고 정해요. 오늘로 저절로 내려오는 것은 없어요.",
    )
    val futureEmpty = t(
        "Nothing yet.",
        "Nada todavía.",
        "Nada ainda.",
        "Noch nichts.",
        "Rien pour l'instant.",
        "Ancora niente.",
        "Nog niets.",
        "Na razie nic.",
        "Пока ничего.",
        "Henüz bir şey yok.",
        "Belum ada apa-apa.",
        "まだ何もありません。",
        "아직 없어요.",
    )
    fun onDay(day: Int) = t(
        "Day $day",
        "Día $day",
        "Dia $day",
        "Tag $day",
        "Jour $day",
        "Giorno $day",
        "Dag $day",
        "Dzień $day",
        "День $day",
        "$day. gün",
        "Tanggal $day",
        "${day}日",
        "${day}일",
    )

    // --- 8. Indice ------------------------------------------------------------------------

    val indexSubtitle = t(
        "Your whole journal, in one place",
        "Todo tu diario, en un sitio",
        "Todo o seu diário, num só lugar",
        "Dein ganzes Journal an einem Ort",
        "Tout ton journal, au même endroit",
        "Tutto il tuo diario, in un posto solo",
        "Je hele dagboek op één plek",
        "Cały twój dziennik w jednym miejscu",
        "Весь твой дневник в одном месте",
        "Tüm günlüğün tek yerde",
        "Seluruh jurnalmu, di satu tempat",
        "ジャーナルのすべてを、ひとつの場所に",
        "다이어리 전체를 한곳에",
    )
    val indexMonths = t("Months", "Meses", "Meses", "Monate", "Mois", "Mesi", "Maanden", "Miesiące", "Месяцы", "Aylar", "Bulan", "月別", "월별")
    val indexLists = t("Lists", "Listas", "Listas", "Listen", "Listes", "Liste", "Lijsten", "Listy", "Списки", "Listeler", "Daftar", "リスト", "목록")
    val indexListsHint = t(
        "Trips, ideas, books: what doesn't go in a day",
        "Viajes, ideas, libros: lo que no va en un día",
        "Viagens, ideias, livros: o que não cabe num dia",
        "Reisen, Ideen, Bücher: was in keinen Tag gehört",
        "Voyages, idées, livres : ce qui ne va pas dans un jour",
        "Viaggi, idee, libri: ciò che non sta in un giorno",
        "Reizen, ideeën, boeken: wat niet in één dag past",
        "Podróże, pomysły, książki: to, co nie mieści się w jednym dniu",
        "Поездки, идеи, книги: то, что не помещается в один день",
        "Geziler, fikirler, kitaplar: bir güne sığmayanlar",
        "Perjalanan, ide, buku: yang tidak muat dalam satu hari",
        "旅行、アイデア、本: 1日に入りきらないもの",
        "여행, 아이디어, 책: 하루에 담기지 않는 것",
    )
    val indexTrackers = t("Trackers", "Seguimientos", "Trackers", "Tracker", "Suivis", "Tracker", "Trackers", "Trackery", "Трекеры", "Çizelgeler", "Pelacak", "トラッカー", "트래커")
    val indexTrackersHint = t(
        "One row per habit, one box per day",
        "Una fila por hábito, un cuadro por día",
        "Uma linha por hábito, um quadrado por dia",
        "Eine Zeile pro Gewohnheit, ein Kästchen pro Tag",
        "Une ligne par habitude, une case par jour",
        "Una riga per abitudine, una casella per giorno",
        "Eén rij per gewoonte, één vakje per dag",
        "Jeden wiersz na nawyk, jedno pole na dzień",
        "Одна строка на привычку, одна клетка на день",
        "Alışkanlık başına bir satır, gün başına bir kutu",
        "Satu baris per kebiasaan, satu kotak per hari",
        "習慣ごとに1行、日ごとに1マス",
        "습관마다 한 줄, 하루마다 한 칸",
    )
    val newTracker = t(
        "New tracker",
        "Nuevo seguimiento",
        "Novo tracker",
        "Neuer Tracker",
        "Nouveau suivi",
        "Nuovo tracker",
        "Nieuwe tracker",
        "Nowy tracker",
        "Новый трекер",
        "Yeni çizelge",
        "Pelacak baru",
        "新しいトラッカー",
        "새 트래커",
    )
    val listNameHint = t(
        "Name of the list",
        "Nombre de la lista",
        "Nome da lista",
        "Name der Liste",
        "Nom de la liste",
        "Nome della lista",
        "Naam van de lijst",
        "Nazwa listy",
        "Название списка",
        "Listenin adı",
        "Nama daftar",
        "リストの名前",
        "목록 이름",
    )
    val trackerNameHint = t(
        "Name of the tracker",
        "Nombre del seguimiento",
        "Nome do tracker",
        "Name des Trackers",
        "Nom du suivi",
        "Nome del tracker",
        "Naam van de tracker",
        "Nazwa trackera",
        "Название трекера",
        "Çizelgenin adı",
        "Nama pelacak",
        "トラッカーの名前",
        "트래커 이름",
    )
    val listCreated = t(
        "List created. Write its first line below.",
        "Lista creada. Escribe abajo su primera línea.",
        "Lista criada. Escreva a primeira linha abaixo.",
        "Liste erstellt. Schreib unten die erste Zeile.",
        "Liste créée. Écris sa première ligne en bas.",
        "Lista creata. Scrivi qui sotto la prima riga.",
        "Lijst gemaakt. Schrijf hieronder de eerste regel.",
        "Lista utworzona. Napisz poniżej jej pierwszą linijkę.",
        "Список создан. Напиши внизу первую строку.",
        "Liste oluşturuldu. İlk satırını aşağıya yaz.",
        "Daftar dibuat. Tulis baris pertamanya di bawah.",
        "リストを作りました。下に最初の1行を書いてください。",
        "목록을 만들었어요. 아래에 첫 줄을 써 보세요.",
    )
    fun archivedToggle(n: Int) = t(
        "Archived ($n)",
        "Archivadas ($n)",
        "Arquivadas ($n)",
        "Archiviert ($n)",
        "Archivées ($n)",
        "Archiviate ($n)",
        "Gearchiveerd ($n)",
        "Zarchiwizowane ($n)",
        "В архиве ($n)",
        "Arşivlenenler ($n)",
        "Diarsipkan ($n)",
        "アーカイブ済み(${n})",
        "보관됨 (${n})",
    )
    val indexEmpty = t(
        "Months show up here as soon as you write in them.",
        "Los meses aparecen aquí en cuanto escribes en ellos.",
        "Os meses aparecem aqui assim que você escreve neles.",
        "Monate erscheinen hier, sobald du in ihnen schreibst.",
        "Les mois apparaissent ici dès que tu y écris.",
        "I mesi compaiono qui appena ci scrivi.",
        "Maanden verschijnen hier zodra je erin schrijft.",
        "Miesiące pojawią się tutaj, gdy tylko zaczniesz w nich pisać.",
        "Месяцы появятся здесь, как только ты начнёшь в них писать.",
        "Aylar, içlerine yazar yazmaz burada görünür.",
        "Bulan muncul di sini begitu kamu menulis di dalamnya.",
        "月は、書き込むとすぐここに表示されます。",
        "어떤 달에 쓰기 시작하면 바로 여기에 나타나요.",
    )

    // --- 9. Coleccion y seguimiento ---------------------------------------------------------

    val listSubtitle = t("List", "Lista", "Lista", "Liste", "Liste", "Lista", "Lijst", "Lista", "Список", "Liste", "Daftar", "リスト", "목록")
    val listEmpty = t(
        "Empty list. Write the first line below.",
        "Lista vacía. Escribe abajo la primera línea.",
        "Lista vazia. Escreva a primeira linha abaixo.",
        "Leere Liste. Schreib unten die erste Zeile.",
        "Liste vide. Écris la première ligne en bas.",
        "Lista vuota. Scrivi qui sotto la prima riga.",
        "Lege lijst. Schrijf hieronder de eerste regel.",
        "Pusta lista. Napisz poniżej pierwszą linijkę.",
        "Список пуст. Напиши внизу первую строку.",
        "Boş liste. İlk satırı aşağıya yaz.",
        "Daftar kosong. Tulis baris pertama di bawah.",
        "空のリストです。下に最初の1行を書いてください。",
        "빈 목록이에요. 아래에 첫 줄을 써 보세요.",
    )
    val shareList = t(
        "Share the list",
        "Compartir la lista",
        "Compartilhar a lista",
        "Liste teilen",
        "Partager la liste",
        "Condividi la lista",
        "Lijst delen",
        "Udostępnij listę",
        "Поделиться списком",
        "Listeyi paylaş",
        "Bagikan daftar",
        "リストを共有",
        "목록 공유",
    )
    val rename = t(
        "Rename",
        "Cambiar el nombre",
        "Mudar o nome",
        "Umbenennen",
        "Renommer",
        "Rinomina",
        "Naam wijzigen",
        "Zmień nazwę",
        "Переименовать",
        "Adı değiştir",
        "Ubah nama",
        "名前を変更",
        "이름 변경",
    )
    val archive = t("Archive", "Archivar", "Arquivar", "Archivieren", "Archiver", "Archivia", "Archiveren", "Archiwizuj", "Архивировать", "Arşivle", "Arsipkan", "アーカイブ", "보관")
    val unarchive = t(
        "Unarchive",
        "Sacar del archivo",
        "Tirar do arquivo",
        "Aus dem Archiv holen",
        "Désarchiver",
        "Togli dall'archivio",
        "Uit archief halen",
        "Przywróć z archiwum",
        "Вернуть из архива",
        "Arşivden çıkar",
        "Keluarkan dari arsip",
        "アーカイブから戻す",
        "보관 해제",
    )
    val deleteCollection = t("Delete", "Borrar", "Apagar", "Löschen", "Supprimer", "Elimina", "Verwijderen", "Usuń", "Удалить", "Sil", "Hapus", "削除", "삭제")
    val archivedNote = t(
        "Archived.",
        "Archivada.",
        "Arquivada.",
        "Archiviert.",
        "Archivée.",
        "Archiviata.",
        "Gearchiveerd.",
        "Zarchiwizowano.",
        "В архиве.",
        "Arşivlendi.",
        "Diarsipkan.",
        "アーカイブ済み。",
        "보관했어요.",
    )

    fun trackerSubtitle(month: YearMonth): String {
        val m = monthNames()[month.month.ordinal]
        return t(
            "${m.replaceFirstChar { it.uppercase() }} tracker",
            "Seguimiento de $m",
            "Tracker de $m",
            "Tracker im $m",
            "Suivi ${frDe(m)}",
            "Tracker di $m",
            "Tracker van $m",
            "Tracker: $m",
            "Трекер: $m",
            "$m çizelgesi",
            "Pelacak $m",
            "${m}のトラッカー",
            "${m} 트래커",
        )
    }
    val trackerExplain = t(
        "Tap a box to mark that day; again to clear it. Next month starts blank.",
        "Toca un cuadro para marcar ese día; otra vez para quitarlo. El mes que viene empieza en blanco.",
        "Toque num quadrado para marcar esse dia; de novo para tirar. O mês que vem começa em branco.",
        "Tippe auf ein Kästchen, um den Tag zu markieren, noch einmal, um es zu löschen. Der nächste Monat beginnt leer.",
        "Touche une case pour marquer ce jour ; encore une fois pour l'effacer. Le mois prochain commence vierge.",
        "Tocca una casella per segnare quel giorno; ancora una volta per toglierla. Il mese prossimo comincia vuoto.",
        "Tik op een vakje om die dag te markeren; nog een keer om de markering te wissen. De volgende maand begint leeg.",
        "Dotknij pola, aby oznaczyć ten dzień; dotknij jeszcze raz, aby zdjąć znak. Następny miesiąc zaczyna się od pustej strony.",
        "Нажми на клетку, чтобы отметить день; ещё раз, чтобы снять отметку. Следующий месяц начинается с чистого листа.",
        "O günü işaretlemek için bir kutuya dokun; kaldırmak için yeniden dokun. Sonraki ay boş başlar.",
        "Ketuk kotak untuk menandai hari itu, ketuk lagi untuk menghapus tandanya. Bulan depan dimulai kosong.",
        "マスをタップするとその日に印が付き、もう一度タップすると消えます。来月は白紙から始まります。",
        "칸을 탭하면 그 날이 표시되고, 한 번 더 탭하면 지워져요. 다음 달은 빈 칸으로 시작해요.",
    )
    val trackerRowHint = t(
        "New row",
        "Nueva fila",
        "Nova linha",
        "Neue Zeile",
        "Nouvelle ligne",
        "Nuova riga",
        "Nieuwe rij",
        "Nowy wiersz",
        "Новая строка",
        "Yeni satır",
        "Baris baru",
        "新しい行",
        "새 줄",
    )
    val rowDelete = t(
        "Delete row",
        "Borrar fila",
        "Apagar linha",
        "Zeile löschen",
        "Supprimer la ligne",
        "Elimina riga",
        "Rij verwijderen",
        "Usuń wiersz",
        "Удалить строку",
        "Satırı sil",
        "Hapus baris",
        "行を削除",
        "줄 삭제",
    )

    // --- 10. Revisar ------------------------------------------------------------------------

    /** Step 1's title (docs/pantallas.md 11.1): a month by its name, the other scopes as "these days". */
    fun rereadTitle(month: YearMonth?): String {
        val m = month?.let { monthNames()[it.month.ordinal] }
        return if (m != null) {
            t(
                "Before deciding, read $m again",
                "Antes de decidir, relee $m",
                "Antes de decidir, releia $m",
                "Bevor du entscheidest, lies den $m nach",
                "Avant de décider, relis $m",
                "Prima di decidere, rileggi $m",
                "Lees $m terug voordat je beslist",
                "Zanim zdecydujesz, przeczytaj ponownie $m",
                "Перед решением перечитай: $m",
                "Karar vermeden önce $m ayını tekrar oku",
                "Sebelum memutuskan, baca ulang bulan $m",
                "決める前に、${m}を読み返す",
                "정하기 전에 ${m} 다시 읽기",
            )
        } else {
            t(
                "Before deciding, read these days again",
                "Antes de decidir, relee estos días",
                "Antes de decidir, releia estes dias",
                "Bevor du entscheidest, lies diese Tage nach",
                "Avant de décider, relis ces jours",
                "Prima di decidere, rileggi questi giorni",
                "Lees deze dagen terug voordat je beslist",
                "Zanim zdecydujesz, przeczytaj ponownie te dni",
                "Перед решением перечитай эти дни",
                "Karar vermeden önce bu günleri tekrar oku",
                "Sebelum memutuskan, baca ulang hari-hari ini",
                "決める前に、これらの日を読み返す",
                "정하기 전에 이 날들 다시 읽기",
            )
        }
    }

    fun rereadLead(n: Int): String = when {
        n == 0 -> t(
            "No task was left open.",
            "No quedó ninguna tarea abierta.",
            "Nenhuma tarefa ficou aberta.",
            "Keine Aufgabe ist offen geblieben.",
            "Aucune tâche n'est restée ouverte.",
            "Nessuna attività è rimasta aperta.",
            "Er is geen taak open gebleven.",
            "Żadne zadanie nie pozostało otwarte.",
            "Открытых задач не осталось.",
            "Açık kalan görev yok.",
            "Tidak ada tugas yang masih terbuka.",
            "未完了のタスクは残っていません。",
            "미완료로 남은 할 일이 없어요.",
        )
        isSingular(n) -> t(
            "$n task was left open. Next you'll see it and decide what happens to it.",
            "Quedó $n tarea abierta. Después la verás y decides qué pasa con ella.",
            "Ficou $n tarefa aberta. Depois você a verá e decide o que acontece com ela.",
            "$n Aufgabe ist offen geblieben. Danach siehst du sie und entscheidest, was mit ihr passiert.",
            "$n tâche est restée ouverte. Ensuite tu la verras et tu décideras ce qu'elle devient.",
            "È rimasta aperta $n attività. Poi la vedi e decidi cosa farne.",
            "Er is $n taak open gebleven. Daarna zie je de taak en beslis je wat ermee gebeurt.",
            "Zostało $n otwarte zadanie. Potem je zobaczysz i zdecydujesz, co się z nim stanie.",
            "Осталась $n открытая задача. Дальше ты увидишь её и решишь, что с ней будет.",
            "$n görev açık kaldı. Ardından onu görüp ne olacağına karar vereceksin.",
            "$n tugas masih terbuka. Setelah ini kamu akan melihatnya dan memutuskan apa yang terjadi padanya.",
            "未完了のタスクが${n}件残っています。このあと、それを見てどうするか決めます。",
            "미완료로 남은 할 일이 ${n}개 있어요. 이어서 그 할 일을 보고 어떻게 할지 정해요.",
        )
        else -> t(
            "$n tasks were left open. Next you'll see them one at a time and decide what happens to each.",
            "Quedaron $n tareas abiertas. Después las verás de una en una y decides qué pasa con cada una.",
            "Ficaram $n tarefas abertas. Depois você as verá uma a uma e decide o que acontece com cada uma.",
            "$n Aufgaben sind offen geblieben. Danach siehst du sie einzeln und entscheidest, was mit jeder passiert.",
            "$n tâches sont restées ouvertes. Ensuite tu les verras une par une et tu décideras ce que devient chacune.",
            "Sono rimaste aperte $n attività. Poi le vedi una alla volta e decidi cosa fare di ciascuna.",
            "Er zijn $n taken open gebleven. Daarna zie je ze een voor een en beslis je wat er met elke taak gebeurt.",
            "${word(n, "Zostało|Zostały|Zostało")} $n ${word(n, "otwarte zadanie|otwarte zadania|otwartych zadań")}. Potem zobaczysz je po kolei, jedno po drugim, i zdecydujesz, co stanie się z każdym.",
            "${word(n, "Осталась|Остались|Осталось")} $n ${word(n, "открытая задача|открытые задачи|открытых задач")}. Дальше ты увидишь их по одной и решишь, что будет с каждой.",
            "$n görev açık kaldı. Ardından onları tek tek görüp her birine ne olacağına karar vereceksin.",
            "$n tugas masih terbuka. Setelah ini kamu akan melihatnya satu per satu dan memutuskan apa yang terjadi pada masing-masing.",
            "未完了のタスクが${n}件残っています。このあと1件ずつ見て、それぞれどうするか決めます。",
            "미완료로 남은 할 일이 ${n}개 있어요. 이어서 하나씩 보면서 각각 어떻게 할지 정해요.",
        )
    }

    fun rereadNote(month: YearMonth?): String {
        val m = month?.let { monthNames()[it.month.ordinal] }
        return if (m != null) {
            t(
                "A note about $m (optional)",
                "Una nota sobre $m (opcional)",
                "Uma nota sobre $m (opcional)",
                "Eine Notiz zum $m (optional)",
                "Une note sur $m (facultatif)",
                "Una nota su $m (facoltativa)",
                "Een notitie over $m (optioneel)",
                "Notatka o miesiącu: $m (opcjonalnie)",
                "Заметка про $m (необязательно)",
                "$m ayı hakkında bir not (isteğe bağlı)",
                "Catatan tentang bulan $m (opsional)",
                "${m}についてのメモ(任意)",
                "${m}에 대한 메모 (선택)",
            )
        } else {
            t(
                "A note about these days (optional)",
                "Una nota sobre estos días (opcional)",
                "Uma nota sobre estes dias (opcional)",
                "Eine Notiz zu diesen Tagen (optional)",
                "Une note sur ces jours (facultatif)",
                "Una nota su questi giorni (facoltativa)",
                "Een notitie over deze dagen (optioneel)",
                "Notatka o tych dniach (opcjonalnie)",
                "Заметка про эти дни (необязательно)",
                "Bu günler hakkında bir not (isteğe bağlı)",
                "Catatan tentang hari-hari ini (opsional)",
                "これらの日についてのメモ(任意)",
                "이 날들에 대한 메모 (선택)",
            )
        }
    }

    fun decideTasks(n: Int): String = when {
        n == 0 -> t("Finish", "Terminar", "Terminar", "Fertig", "Terminer", "Concludi", "Afronden", "Zakończ", "Завершить", "Bitir", "Selesai", "見直しを終える", "끝내기")
        n == 1 -> t(
            "Decide the task",
            "Decidir la tarea",
            "Decidir a tarefa",
            "Aufgabe entscheiden",
            "Décider la tâche",
            "Decidi l'attività",
            "Beslis over de taak",
            "Zdecyduj o zadaniu",
            "Решить по задаче",
            "Görev için karar ver",
            "Putuskan tugas ini",
            "1件のタスクを決める",
            "할 일 정하기",
        )
        else -> t(
            "Decide the $n tasks",
            "Decidir las $n tareas",
            "Decidir as $n tarefas",
            "$n Aufgaben entscheiden",
            "Décider les $n tâches",
            "Decidi le $n attività",
            "Beslis over $n taken",
            "Zdecyduj: $n ${wordTask(n)}",
            "Решить по $n ${word(n, "задаче|задачам|задачам")}",
            "Karar ver: $n görev",
            "Putuskan $n tugas",
            "${n}件のタスクを決める",
            "할 일 ${n}개 정하기",
        )
    }

    fun taskOf(i: Int, n: Int) = t(
        "Task $i of $n",
        "Tarea $i de $n",
        "Tarefa $i de $n",
        "Aufgabe $i von $n",
        "Tâche $i sur $n",
        "Attività $i di $n",
        "Taak $i van $n",
        "Zadanie $i z $n",
        "Задача $i из $n",
        "Görev $i / $n",
        "Tugas $i dari $n",
        "タスク ${i}/${n}",
        "할 일 ${i}/${n}",
    )
    fun entryOf(i: Int, n: Int) = t(
        "Entry $i of $n",
        "Entrada $i de $n",
        "Entrada $i de $n",
        "Eintrag $i von $n",
        "Entrée $i sur $n",
        "Voce $i di $n",
        "Item $i van $n",
        "Wpis $i z $n",
        "Запись $i из $n",
        "Kayıt $i / $n",
        "Entri $i dari $n",
        "項目 ${i}/${n}",
        "항목 ${i}/${n}",
    )
    fun collectionOf(i: Int, n: Int) = t(
        "Collection $i of $n",
        "Colección $i de $n",
        "Coleção $i de $n",
        "Sammlung $i von $n",
        "Collection $i sur $n",
        "Raccolta $i di $n",
        "Collectie $i van $n",
        "Kolekcja $i z $n",
        "Коллекция $i из $n",
        "Koleksiyon $i / $n",
        "Koleksi $i dari $n",
        "コレクション ${i}/${n}",
        "목록 ${i}/${n}",
    )

    /** The review's own "Hecha": in a month review the task stays in that month (docs/pantallas.md 11.2). */
    fun doneStaysIn(month: YearMonth?): String {
        val m = month?.let { monthNames()[it.month.ordinal] } ?: return doneHow
        return t(
            "Stays in $m, marked with an x.",
            "Se queda en $m, marcada con una x.",
            "Fica em $m, marcada com um x.",
            "Bleibt im $m, mit einem x markiert.",
            "Reste en $m, marquée d'un x.",
            "Resta in $m, segnata con una x.",
            "Blijft in $m, gemarkeerd met een x.",
            "Zostaje w miesiącu: $m, oznaczone x.",
            "Остаётся на месте ($m), отмечена знаком x.",
            "$m ayında kalır, x ile işaretlenir.",
            "Tetap di bulan $m, ditandai dengan x.",
            "${m}に残り、x印が付きます。",
            "${m}에 그대로 남고 x 표시가 붙어요.",
        )
    }
    val decideLater = t(
        "Decide later",
        "Decidir luego",
        "Decidir depois",
        "Später entscheiden",
        "Décider plus tard",
        "Decidi dopo",
        "Later beslissen",
        "Zdecyduj później",
        "Решить позже",
        "Sonra karar ver",
        "Putuskan nanti",
        "あとで決める",
        "나중에 정하기",
    )
    val decideLaterHow = t(
        "It stays open and the notice stays.",
        "Sigue abierta y el aviso no se va.",
        "Continua aberta e o aviso não sai.",
        "Bleibt offen, und der Hinweis bleibt.",
        "Elle reste ouverte et l'avis ne part pas.",
        "Resta aperta e l'avviso non sparisce.",
        "Blijft open en de melding blijft staan.",
        "Zostaje otwarte, a komunikat nie znika.",
        "Остаётся открытой, уведомление не исчезает.",
        "Açık kalır, uyarı da kalır.",
        "Tetap terbuka dan pemberitahuannya tetap ada.",
        "未完了のまま残り、お知らせも消えません。",
        "미완료로 남고, 안내도 그대로 있어요.",
    )
    val reviewFinished = t(
        "Review finished",
        "Repaso terminado",
        "Revisão terminada",
        "Durchsicht beendet",
        "Revue terminée",
        "Revisione terminata",
        "Doornemen afgerond",
        "Przegląd zakończony",
        "Просмотр завершён",
        "Gözden geçirme bitti",
        "Tinjauan selesai",
        "見直し完了",
        "살펴보기 완료",
    )

    fun reviewEndTitle(month: YearMonth?, left: Int): String {
        val almost = left > 0
        if (month == null) {
            return if (almost) {
                t(
                    "These days, almost",
                    "Estos días, casi",
                    "Estes dias, quase",
                    "Diese Tage, fast",
                    "Ces jours, presque",
                    "Questi giorni, quasi",
                    "Deze dagen, bijna",
                    "Te dni, prawie przejrzane",
                    "Эти дни почти просмотрены",
                    "Bu günler, neredeyse tamam",
                    "Hari-hari ini, hampir selesai",
                    "これらの日、あと少し",
                    "이 날들, 거의 다 됐어요",
                )
            } else {
                t(
                    "These days, reviewed",
                    "Estos días, repasados",
                    "Estes dias, revisados",
                    "Diese Tage, durchgesehen",
                    "Ces jours, revus",
                    "Questi giorni, rivisti",
                    "Deze dagen, doorgenomen",
                    "Te dni, przejrzane",
                    "Эти дни просмотрены",
                    "Bu günler, gözden geçirildi",
                    "Hari-hari ini, selesai ditinjau",
                    "これらの日、見直し済み",
                    "이 날들, 살펴봤어요",
                )
            }
        }
        val m = monthName(month)
        return if (almost) {
            t(
                "$m, almost",
                "$m, casi",
                "$m, quase",
                "$m, fast",
                "$m, presque",
                "$m, quasi",
                "$m, bijna",
                "$m, prawie przejrzany",
                "$m почти просмотрен",
                "$m, neredeyse tamam",
                "$m, hampir selesai",
                "${m}、あと少し",
                "${m}, 거의 다 됐어요",
            )
        } else {
            t(
                "$m, reviewed",
                "$m, repasado",
                "$m, revisado",
                "$m, durchgesehen",
                "$m, revu",
                "$m, rivisto",
                "$m, doorgenomen",
                "$m, przejrzany",
                "$m просмотрен",
                "$m, gözden geçirildi",
                "$m, selesai ditinjau",
                "${m}、見直し済み",
                "${m}, 살펴봤어요",
            )
        }
    }

    fun reviewEndLead(left: Int): String = if (left > 0) {
        val w = wordTask(left)
        t(
            "You left $left $w for later. The notice stays on Today until you decide.",
            "Dejaste $left $w para luego. El aviso seguirá en Hoy hasta que decidas.",
            "Você deixou $left $w para depois. O aviso fica em Hoje até você decidir.",
            "Du hast $left $w für später gelassen. Der Hinweis bleibt in Heute, bis du entscheidest.",
            "Tu as laissé $left $w pour plus tard. L'avis reste dans Aujourd'hui jusqu'à ta décision.",
            "Hai lasciato $left $w per dopo. L'avviso resta su Oggi finché non decidi.",
            "Je hebt $left $w voor later laten liggen. De melding blijft in Vandaag staan tot je beslist.",
            "Na później zostawiono $left $w. Komunikat zostaje w zakładce Dziś, dopóki nie zdecydujesz.",
            "На потом оставлено: $left $w. Уведомление останется на экране \"Сегодня\", пока ты не решишь.",
            "Sonraya $left $w bıraktın. Sen karar verene kadar uyarı Bugün'de durur.",
            "Kamu menyisakan $left $w untuk nanti. Pemberitahuannya tetap ada di tab Hari sampai kamu memutuskan.",
            "${left}件の${w}をあとに残しました。決めるまで、お知らせは「今日」に表示されたままです。",
            "나중에 정하려고 남겨 둔 할 일이 ${left}개 있어요. 정할 때까지 안내가 오늘 화면에 남아 있어요.",
        )
    } else {
        t(
            "What you moved to today is already on your list, and what you moved to another month waits in Future.",
            "Lo que pasaste a hoy ya está en tu lista, y lo que llevaste a otro mes espera en Futuro.",
            "O que você passou para hoje já está na sua lista, e o que levou para outro mês espera no Futuro.",
            "Was du auf heute verschoben hast, steht schon auf deiner Liste, und was in einen anderen Monat ging, wartet in Zukunft.",
            "Ce que tu as passé à aujourd'hui est déjà dans ta liste, et ce que tu as reporté attend dans Futur.",
            "Quello che hai spostato a oggi è già nella tua lista, e quello che hai spostato in un altro mese aspetta in Futuro.",
            "Wat je naar vandaag hebt verplaatst staat al op je lijst, en wat je naar een andere maand hebt verplaatst wacht in Toekomst.",
            "To, co przeniesiono na dziś, jest już na twojej liście, a to, co przeniesiono na inny miesiąc, czeka w Przyszłości.",
            "Перенесённое на сегодня уже в твоём списке, а отложенное на другой месяц ждёт в Будущем.",
            "Bugüne taşıdıkların zaten listende, başka aya taşıdıkların ise Gelecek'te bekliyor.",
            "Yang kamu pindahkan ke hari ini sudah ada di tab Hari, dan yang kamu pindahkan ke bulan lain menunggu di Mendatang.",
            "今日に移したものはすでに今日に入っていて、別の月に移したものは未来で待っています。",
            "오늘로 옮긴 것은 이미 오늘 목록에 있고, 다른 달로 옮긴 것은 미래에서 기다려요.",
        )
    }

    val skip = t("Skip", "Saltar", "Pular", "Überspringen", "Passer", "Salta", "Overslaan", "Pomiń", "Пропустить", "Atla", "Lewati", "スキップ", "건너뛰기")

    fun fromDay(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        val phrase = t(
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}.",
            "$w ${d.day}",
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}",
            "${d.day} $w",
            "$w, ${d.day}",
            "${d.day}日 ${w}",
            "${d.day}일 ${w}",
        )
        return t(
            "From $phrase",
            "Del $phrase",
            "Da $phrase",
            "Vom $phrase",
            "Du $phrase",
            "Da $phrase",
            "Van $phrase",
            "Z dnia: $phrase",
            "День: $phrase",
            "$phrase gününden",
            "Dari $phrase",
            "${phrase}から",
            "${phrase}에서",
        )
    }

    fun fromMonthTasks(month: YearMonth): String {
        val name = monthNames()[month.month.ordinal]
        return t(
            "$name tasks",
            "Tareas de $name",
            "Tarefas de $name",
            "Aufgaben im $name",
            "Tâches ${frDe(name)}",
            "Attività di $name",
            "Taken van $name",
            "Zadania na $name",
            "Задачи месяца: $name",
            "$name ayının görevleri",
            "Tugas $name",
            "${name}のタスク",
            "${name} 할 일",
        )
    }

    fun fromCalendar(d: LocalDate): String {
        val date = shortDate(d)
        return t(
            "Calendar, $date",
            "Calendario, $date",
            "Calendário, $date",
            "Kalender, $date",
            "Calendrier, $date",
            "Calendario, $date",
            "Kalender, $date",
            "Kalendarz, $date",
            "Календарь, $date",
            "Takvim, $date",
            "Kalender, $date",
            "カレンダー、${date}",
            "캘린더, ${date}",
        )
    }

    fun fromFuture(month: YearMonth, day: Int? = null): String {
        val place = if (day != null) shortDate(LocalDate(month.year, month.month, day)) else monthYear(month)
        return t(
            "Future, $place",
            "Futuro, $place",
            "Futuro, $place",
            "Zukunft, $place",
            "Futur, $place",
            "Futuro, $place",
            "Toekomst, $place",
            "Przyszłość, $place",
            "Будущее, $place",
            "Gelecek, $place",
            "Mendatang, $place",
            "未来、${place}",
            "미래, ${place}",
        )
    }

    val futureToCalendar = t(
        "Move to the calendar",
        "Pasar al calendario",
        "Passar para o calendário",
        "In den Kalender",
        "Passer au calendrier",
        "Sposta nel calendario",
        "Naar de kalender verplaatsen",
        "Przenieś do kalendarza",
        "Перенести в календарь",
        "Takvime taşı",
        "Pindahkan ke kalender",
        "カレンダーに移す",
        "캘린더로 옮기기",
    )
    fun goesTo(label: String) = sentence(t(
        "Goes to $label",
        "Va ${esA(label)}",
        "Vai para $label",
        "Geht nach: $label",
        "Va vers $label",
        "Va a: $label",
        "Gaat naar $label",
        "Trafia do: $label",
        "Переносится: $label",
        "Şuraya gider: $label",
        "Masuk ke $label",
        "${label}に移ります。",
        "가는 곳: ${label}",
    ))
    val futureLeave = t("Leave it", "Dejarla", "Deixar", "Lassen", "La laisser", "Lasciala", "Laten staan", "Zostaw", "Оставить", "Kalsın", "Biarkan", "そのままにする", "그대로 두기")
    val futureLeaveHow = t(
        "It keeps waiting in Future.",
        "Sigue esperando en Futuro.",
        "Continua esperando no Futuro.",
        "Wartet weiter in Zukunft.",
        "Elle attend encore dans Futur.",
        "Continua ad aspettare in Futuro.",
        "Blijft in Toekomst wachten.",
        "Nadal czeka w Przyszłości.",
        "Продолжает ждать в Будущем.",
        "Gelecek'te beklemeye devam eder.",
        "Tetap menunggu di Mendatang.",
        "未来で待ち続けます。",
        "미래에서 계속 기다려요.",
    )
    val futureAllDecided = t(
        "Everything Future kept for this month is decided.",
        "Todo lo que Futuro guardaba para este mes está decidido.",
        "Tudo o que o Futuro guardava para este mês está decidido.",
        "Alles, was Zukunft für diesen Monat hatte, ist entschieden.",
        "Tout ce que Futur gardait pour ce mois est décidé.",
        "Tutto ciò che Futuro teneva per questo mese è deciso.",
        "Alles wat Toekomst voor deze maand bewaarde, is beslist.",
        "Wszystko, co czekało w Przyszłości na ten miesiąc, jest rozstrzygnięte.",
        "Всё, что Будущее хранило для этого месяца, решено.",
        "Bu ay için Gelecek'te bekleyen her şeye karar verildi.",
        "Semua yang disimpan Mendatang untuk bulan ini sudah diputuskan.",
        "この月のために未来にあったものは、すべて決まりました。",
        "이번 달을 위해 미래에 있던 항목을 모두 정했어요.",
    )

    // --- 11. Buscar -------------------------------------------------------------------------

    val searchHint = t(
        "A word or a #tag",
        "Una palabra o una #etiqueta",
        "Uma palavra ou uma #etiqueta",
        "Ein Wort oder ein #Tag",
        "Un mot ou un #tag",
        "Una parola o un #tag",
        "Een woord of een #tag",
        "Słowo lub #tag",
        "Слово или #тег",
        "Bir sözcük ya da bir #etiket",
        "Satu kata atau #tagar",
        "ことば、または #タグ",
        "단어 또는 #태그",
    )
    val filterOpen = t("Open", "Abiertas", "Abertas", "Offen", "Ouvertes", "Aperte", "Open", "Otwarte", "Открытые", "Açık", "Terbuka", "未完了", "미완료")
    fun monthGroup(month: YearMonth) = monthTitle(month)
    fun futureGroup(month: YearMonth) = wentToFuture(month)
    val searchEmpty = t(
        "Search for a word, or type # and a tag.",
        "Busca una palabra, o escribe # y una etiqueta.",
        "Busque uma palavra, ou escreva # e uma etiqueta.",
        "Such nach einem Wort, oder tippe # und einen Tag.",
        "Cherche un mot, ou écris # et un tag.",
        "Cerca una parola, oppure scrivi # e un tag.",
        "Zoek een woord, of typ # en een tag.",
        "Wyszukaj słowo albo wpisz # i tag.",
        "Ищи слово или напиши # и тег.",
        "Bir sözcük ara ya da # ve bir etiket yaz.",
        "Cari sebuah kata, atau ketik # dan tagar.",
        "ことばを入力するか、# とタグを入力してください。",
        "단어를 검색하거나 # 뒤에 태그를 입력해 보세요.",
    )
    val searchNothing = t(
        "Nothing with those words.",
        "Nada con esas palabras.",
        "Nada com essas palavras.",
        "Nichts mit diesen Wörtern.",
        "Rien avec ces mots.",
        "Niente con queste parole.",
        "Niets met die woorden.",
        "Nic z tymi słowami.",
        "Ничего по этим словам.",
        "Bu sözcüklerle eşleşen bir şey yok.",
        "Tidak ada yang memuat kata-kata itu.",
        "そのことばに合うものはありません。",
        "그 단어가 들어간 것이 없어요.",
    )

    // --- 12. Clave --------------------------------------------------------------------------

    val keyTitle = t("Key", "Clave", "Legenda", "Legende", "Légende", "Legenda", "Legenda", "Legenda", "Легенда", "Anahtar", "Keterangan", "凡例", "범례")
    val keyWhat = t(
        "What each symbol means",
        "Qué significa cada símbolo",
        "O que cada símbolo significa",
        "Was jedes Symbol bedeutet",
        "Ce que veut dire chaque symbole",
        "Cosa significa ogni simbolo",
        "Wat elk symbool betekent",
        "Co oznacza każdy symbol",
        "Что означает каждый символ",
        "Her sembolün anlamı",
        "Arti setiap simbol",
        "それぞれの記号の意味",
        "각 기호의 뜻",
    )
    val keyWrite = t(
        "What you write down",
        "Lo que apuntas",
        "O que você anota",
        "Was du notierst",
        "Ce que tu notes",
        "Quello che annoti",
        "Wat je opschrijft",
        "To, co zapisujesz",
        "Что ты записываешь",
        "Yazdıkların",
        "Yang kamu catat",
        "書き留めるもの",
        "적는 것",
    )
    val keyHappened = t(
        "What happened to a task",
        "Qué pasó con una tarea",
        "O que aconteceu com uma tarefa",
        "Was mit einer Aufgabe geschah",
        "Ce qu'est devenue une tâche",
        "Cosa è successo a un'attività",
        "Wat er met een taak is gebeurd",
        "Co się stało z zadaniem",
        "Что случилось с задачей",
        "Bir göreve ne olduğu",
        "Apa yang terjadi pada tugas",
        "タスクのその後",
        "할 일에 일어난 일",
    )
    val keyMargin = t(
        "In the margin",
        "En el margen",
        "Na margem",
        "Am Rand",
        "Dans la marge",
        "A margine",
        "In de kantlijn",
        "Na marginesie",
        "На полях",
        "Kenarda",
        "Di margin",
        "余白",
        "여백",
    )
    val keyTask = t(
        "Something to do.",
        "Algo que hacer.",
        "Algo para fazer.",
        "Etwas zu tun.",
        "Quelque chose à faire.",
        "Qualcosa da fare.",
        "Iets om te doen.",
        "Coś do zrobienia.",
        "Что-то, что нужно сделать.",
        "Yapılacak bir şey.",
        "Sesuatu yang perlu dikerjakan.",
        "やること。",
        "해야 할 일.",
    )
    val keyEvent = t(
        "Something that happens on a date.",
        "Algo que pasa en una fecha.",
        "Algo que acontece numa data.",
        "Etwas, das an einem Datum passiert.",
        "Quelque chose qui arrive à une date.",
        "Qualcosa che succede in una data.",
        "Iets dat op een datum gebeurt.",
        "Coś, co dzieje się w określonym dniu.",
        "Что-то, что происходит в определённую дату.",
        "Bir tarihte olan bir şey.",
        "Sesuatu yang terjadi pada suatu tanggal.",
        "日付のある出来事。",
        "날짜에 일어나는 일.",
    )
    val keyNote = t(
        "Something you want to remember.",
        "Algo que quieres recordar.",
        "Algo que você quer lembrar.",
        "Etwas, das du dir merken willst.",
        "Quelque chose dont tu veux te souvenir.",
        "Qualcosa che vuoi ricordare.",
        "Iets dat je wilt onthouden.",
        "Coś, co chcesz zapamiętać.",
        "Что-то, что хочешь запомнить.",
        "Hatırlamak istediğin bir şey.",
        "Sesuatu yang ingin kamu ingat.",
        "覚えておきたいこと。",
        "기억하고 싶은 것.",
    )
    val keyDone = t(
        "Stays where it was, with an x.",
        "Se queda donde estaba, con una x.",
        "Fica onde estava, com um x.",
        "Bleibt, wo sie war, mit einem x.",
        "Reste où elle était, avec un x.",
        "Resta dov'era, con una x.",
        "Blijft op zijn plek, met een x.",
        "Zostaje tam, gdzie było, z x.",
        "Остаётся на месте, со знаком x.",
        "Olduğu yerde kalır, bir x ile.",
        "Tetap di tempatnya, dengan x.",
        "元の場所に残り、xが付きます。",
        "있던 자리에 x 표시와 함께 남아요.",
    )
    val movedName = t("Moved", "Pasada", "Passada", "Verschoben", "Déplacée", "Spostata", "Verplaatst", "Przeniesione", "Перенесена", "Taşındı", "Dipindahkan", "移動済み", "옮김")
    val keyMoved = t(
        "You took it to another day or a list. In the method: migrated.",
        "La llevaste a otro día o a una lista. En el método: migrada.",
        "Você a levou para outro dia ou uma lista. No método: migrada.",
        "Du hast sie auf einen anderen Tag oder in eine Liste gelegt. In der Methode: migriert.",
        "Tu l'as portée à un autre jour ou dans une liste. Dans la méthode : migrée.",
        "L'hai portata in un altro giorno o in una lista. Nel metodo: migrata.",
        "Je hebt de taak naar een andere dag of naar een lijst verplaatst. In de methode: gemigreerd.",
        "Przeniesione na inny dzień albo do listy. W metodzie: zmigrowane.",
        "Перенесена на другой день или в список. В методе: мигрирована.",
        "Onu başka bir güne ya da bir listeye taşıdın. Yöntemdeki adı: aktarıldı.",
        "Kamu membawanya ke hari lain atau ke daftar. Dalam metode: dimigrasikan.",
        "別の日やリストに移したもの。メソッドでは「移行済み」。",
        "다른 날이나 목록으로 옮겼어요. 원래 방식에서는 이월이라고 불러요.",
    )
    val otherMonthName = t(
        "Moved to another month",
        "Llevada a otro mes",
        "Levada para outro mês",
        "In einen anderen Monat",
        "Reportée à un autre mois",
        "Spostata in un altro mese",
        "Naar een andere maand verplaatst",
        "Przeniesione na inny miesiąc",
        "Отложена на другой месяц",
        "Başka aya taşındı",
        "Dipindahkan ke bulan lain",
        "別の月へ移動済み",
        "다른 달로 옮김",
    )
    val keyOtherMonth = t(
        "Waits in Future. In the method: scheduled.",
        "Espera en Futuro. En el método: programada.",
        "Espera no Futuro. No método: agendada.",
        "Wartet in Zukunft. In der Methode: eingeplant.",
        "Attend dans Futur. Dans la méthode : planifiée.",
        "Aspetta in Futuro. Nel metodo: programmata.",
        "Wacht in Toekomst. In de methode: ingepland.",
        "Czeka w Przyszłości. W metodzie: zaplanowane.",
        "Ждёт в Будущем. В методе: запланирована.",
        "Gelecek'te bekler. Yöntemdeki adı: planlandı.",
        "Menunggu di Mendatang. Dalam metode: dijadwalkan.",
        "未来で待っています。メソッドでは「スケジュール済み」。",
        "미래에서 기다려요. 원래 방식에서는 예정이라고 불러요.",
    )
    val keyDiscarded = t(
        "It wasn't needed any more. In the method: irrelevant.",
        "Ya no hacía falta. En el método: irrelevante.",
        "Não era mais necessária. No método: irrelevante.",
        "Wurde nicht mehr gebraucht. In der Methode: irrelevant.",
        "Elle n'était plus utile. Dans la méthode : non pertinente.",
        "Non serviva più. Nel metodo: irrilevante.",
        "Was niet meer nodig. In de methode: irrelevant.",
        "Nie było już potrzebne. W metodzie: nieistotne.",
        "Больше не требовалась. В методе: неактуальна.",
        "Artık gerekmiyordu. Yöntemdeki adı: ilgisiz.",
        "Sudah tidak diperlukan. Dalam metode: tidak relevan.",
        "もう必要なくなったもの。メソッドでは「不要」。",
        "더는 필요 없었어요. 원래 방식에서는 관련 없음이라고 불러요.",
    )
    val keyPriority = t(
        "What comes before everything else.",
        "Lo que va antes que lo demás.",
        "O que vem antes do resto.",
        "Was vor allem anderen kommt.",
        "Ce qui passe avant le reste.",
        "Ciò che viene prima di tutto il resto.",
        "Wat vóór al het andere komt.",
        "To, co idzie przed całą resztą.",
        "То, что идёт раньше всего остального.",
        "Her şeyden önce gelen.",
        "Yang didahulukan sebelum yang lain.",
        "何よりも先にすること。",
        "다른 모든 것보다 먼저인 것.",
    )
    val keyInspiration = t(
        "An idea worth keeping.",
        "Una idea que vale la pena.",
        "Uma ideia que vale a pena.",
        "Eine Idee, die sich lohnt.",
        "Une idée qui vaut le coup.",
        "Un'idea da conservare.",
        "Een idee dat het bewaren waard is.",
        "Pomysł wart zachowania.",
        "Идея, которую стоит сохранить.",
        "Saklamaya değer bir fikir.",
        "Ide yang layak disimpan.",
        "残しておきたいアイデア。",
        "간직할 만한 생각.",
    )
    val keyExplore = t(
        "Something to look into.",
        "Algo que investigar.",
        "Algo para investigar.",
        "Etwas zum Nachforschen.",
        "Quelque chose à creuser.",
        "Qualcosa da approfondire.",
        "Iets om uit te zoeken.",
        "Coś do zbadania.",
        "Что-то, что нужно исследовать.",
        "Araştırılacak bir şey.",
        "Sesuatu yang perlu ditelusuri.",
        "調べてみたいこと。",
        "알아볼 만한 것.",
    )

    /** The prefixes of docs/tecnico.md 6.2, as written; ^...^ goes in bold. */
    val keyPrefixes = t(
        "When writing: start with ^o^ for an event, ^-^ for a note, and ^*^, ^!^ or ^?^ for the margin marks.",
        "Al escribir: empieza con ^o^ para un evento, con ^-^ para una nota, y con ^*^, ^!^ o ^?^ para las marcas del margen.",
        "Ao escrever: comece com ^o^ para um evento, com ^-^ para uma nota, e com ^*^, ^!^ ou ^?^ para as marcas da margem.",
        "Beim Schreiben: beginne mit ^o^ für ein Ereignis, mit ^-^ für eine Notiz und mit ^*^, ^!^ oder ^?^ für die Randzeichen.",
        "En écrivant : commence par ^o^ pour un événement, par ^-^ pour une note, et par ^*^, ^!^ ou ^?^ pour les marques de la marge.",
        "Quando scrivi: inizia con ^o^ per un evento, con ^-^ per una nota, e con ^*^, ^!^ o ^?^ per i segni a margine.",
        "Bij het schrijven: begin met ^o^ voor een gebeurtenis, ^-^ voor een notitie, en ^*^, ^!^ of ^?^ voor de tekens in de kantlijn.",
        "Podczas pisania: zacznij od ^o^ dla wydarzenia, od ^-^ dla notatki oraz od ^*^, ^!^ albo ^?^ dla znaków na marginesie.",
        "При записи: начинай с ^o^ для события, с ^-^ для заметки и с ^*^, ^!^ или ^?^ для пометок на полях.",
        "Yazarken: etkinlik için ^o^, not için ^-^, kenar işaretleri için ^*^, ^!^ ya da ^?^ ile başla.",
        "Saat menulis: mulai dengan ^o^ untuk acara, ^-^ untuk catatan, dan ^*^, ^!^ atau ^?^ untuk tanda margin.",
        "書くとき: 行の先頭に ^o^ を付けるとイベント、^-^ を付けるとメモ、^*^、^!^、^?^ を付けると余白の印になります。",
        "줄을 이렇게 시작하면 종류가 바뀌어요. 일정 ^o^, 메모 ^-^, 여백 표시 ^*^ ^!^ ^?^.",
    )

    // The guide of the first start (docs/pantallas.md 13.1): four steps, each with its moving picture.
    val guideStart = t(
        "Start writing",
        "Empezar a escribir",
        "Começar a escrever",
        "Losschreiben",
        "Commencer à écrire",
        "Inizia a scrivere",
        "Begin met schrijven",
        "Zacznij pisać",
        "Начать писать",
        "Yazmaya başla",
        "Mulai menulis",
        "書き始める",
        "적기 시작하기",
    )
    fun guideStep(i: Int, n: Int) = t(
        "Step $i of $n",
        "Paso $i de $n",
        "Passo $i de $n",
        "Schritt $i von $n",
        "Étape $i sur $n",
        "Passo $i di $n",
        "Stap $i van $n",
        "Krok $i z $n",
        "Шаг $i из $n",
        "Adım $i / $n",
        "Langkah $i dari $n",
        "ステップ ${i}/${n}",
        "${n}단계 중 ${i}단계",
    )
    val guideTitles = listOf(
        t(
            "Write it in one line",
            "Apunta en una línea",
            "Anote em uma linha",
            "Schreib es in eine Zeile",
            "Note-le en une ligne",
            "Scrivi in una riga",
            "Schrijf het op in één regel",
            "Zapisz to w jednej linijce",
            "Запиши в одну строку",
            "Tek satırda yaz",
            "Tulis dalam satu baris",
            "1行で書く",
            "한 줄로 적어요",
        ),
        t(
            "Tap the dot when it's done",
            "Toca el punto cuando esté hecha",
            "Toque no ponto quando estiver feita",
            "Tippe auf den Punkt, wenn sie erledigt ist",
            "Touche le point quand c'est fait",
            "Tocca il punto quando è fatta",
            "Tik op de stip als de taak voltooid is",
            "Dotknij kropki, gdy jest zrobione",
            "Нажми на точку, когда задача выполнена",
            "Yapılınca noktaya dokun",
            "Ketuk titiknya kalau sudah selesai",
            "終わったら点をタップ",
            "끝나면 점을 탭해요",
        ),
        t(
            "You move what's pending",
            "Lo pendiente lo mueves tú",
            "O pendente, você que move",
            "Offenes verschiebst du selbst",
            "Ce qui reste, c'est toi qui le déplaces",
            "Ciò che resta lo sposti tu",
            "Wat openstaat, verplaats jij zelf",
            "To, co czeka, przenosisz ty",
            "Незавершённое переносишь ты",
            "Bekleyeni sen taşırsın",
            "Yang tertunda, kamu yang memindahkan",
            "残ったものは自分で移す",
            "남은 일은 직접 옮겨요",
        ),
        t(
            "Once a month, review",
            "Una vez al mes, repasa",
            "Uma vez por mês, revise",
            "Einmal im Monat durchsehen",
            "Une fois par mois, fais le point",
            "Una volta al mese, rivedi",
            "Eén keer per maand doornemen",
            "Raz w miesiącu przejrzyj",
            "Раз в месяц просматривай",
            "Ayda bir gözden geçir",
            "Sebulan sekali, tinjau",
            "月に1回、見直す",
            "한 달에 한 번, 살펴봐요",
        ),
    )
    val guideTexts = listOf(
        t(
            "Each line is a task, an event or a note. The symbol in front tells you which.",
            "Cada línea es una tarea, un evento o una nota. El símbolo de delante te dice cuál es.",
            "Cada linha é uma tarefa, um evento ou uma nota. O símbolo na frente diz qual é.",
            "Jede Zeile ist eine Aufgabe, ein Ereignis oder eine Notiz. Das Symbol davor sagt dir, was.",
            "Chaque ligne est une tâche, un événement ou une note. Le symbole devant te dit lequel.",
            "Ogni riga è un'attività, un evento o una nota. Il simbolo davanti ti dice quale.",
            "Elke regel is een taak, een gebeurtenis of een notitie. Het symbool ervoor zegt welke.",
            "Każda linijka to zadanie, wydarzenie albo notatka. Symbol z przodu mówi ci, co to jest.",
            "Каждая строка: задача, событие или заметка. Символ перед ней подскажет, какая именно.",
            "Her satır bir görev, bir etkinlik ya da bir nottur. Önündeki sembol hangisi olduğunu söyler.",
            "Setiap baris adalah tugas, acara, atau catatan. Simbol di depannya memberi tahu yang mana.",
            "1行が、タスク、イベント、メモのどれか1つです。行の頭の記号で見分けます。",
            "한 줄이 할 일, 일정, 메모 중 하나예요. 앞에 붙은 기호가 무엇인지 알려 줘요.",
        ),
        t(
            "The task stays where it was, marked with an x. If you got it wrong, another tap reopens it.",
            "La tarea se queda donde estaba, marcada con una x. Si te equivocas, otro toque la reabre.",
            "A tarefa fica onde estava, marcada com um x. Se errar, outro toque a reabre.",
            "Die Aufgabe bleibt, wo sie war, mit einem x markiert. Hast du dich vertan, öffnet ein weiterer Tipp sie wieder.",
            "La tâche reste où elle était, marquée d'un x. Si tu te trompes, un autre toucher la rouvre.",
            "L'attività resta dov'era, segnata con una x. Se sbagli, un altro tocco la riapre.",
            "De taak blijft op zijn plek, gemarkeerd met een x. Als je je vergist, opent nog een tik de taak weer.",
            "Zadanie zostaje na swoim miejscu, oznaczone x. Jeśli się pomylisz, kolejne dotknięcie otworzy je z powrotem.",
            "Задача остаётся на месте, отмеченная знаком x. При ошибке ещё одно нажатие откроет её снова.",
            "Görev olduğu yerde kalır, x ile işaretlenir. Yanlış yaptıysan bir dokunuş daha onu yeniden açar.",
            "Tugas tetap di tempatnya, ditandai dengan x. Kalau keliru, ketuk sekali lagi untuk membukanya kembali.",
            "タスクは元の場所に残り、xが付きます。間違えたら、もう一度タップすると未完了に戻ります。",
            "할 일은 있던 자리에 x 표시와 함께 남아요. 잘못 눌렀다면 한 번 더 탭하면 다시 열려요.",
        ),
        t(
            "Nothing moves on its own. You move a task to tomorrow, take it to another month or discard it, and the page keeps the trace.",
            "Nada cambia de sitio solo. Pasas una tarea a mañana, la llevas a otro mes o la descartas, y en la página queda el rastro.",
            "Nada muda de lugar sozinho. Você passa uma tarefa para amanhã, leva para outro mês ou descarta, e a página guarda o rastro.",
            "Nichts wandert von allein. Du verschiebst eine Aufgabe auf morgen, in einen anderen Monat oder verwirfst sie, und die Seite behält die Spur.",
            "Rien ne bouge tout seul. Tu passes une tâche à demain, tu la reportes à un autre mois ou tu l'écartes, et la page en garde la trace.",
            "Niente cambia posto da solo. Sposti un'attività a domani, la porti in un altro mese o la scarti, e sulla pagina resta la traccia.",
            "Niets verandert vanzelf van plek. Je verplaatst een taak naar morgen of naar een andere maand, of je schrapt de taak, en op de pagina blijft het spoor staan.",
            "Nic nie przenosi się samo. Przenosisz zadanie na jutro, przenosisz je na inny miesiąc albo odrzucasz, a na stronie zostaje ślad.",
            "Ничего не перемещается само. Ты переносишь задачу на завтра, откладываешь на другой месяц или вычёркиваешь, а на странице остаётся след.",
            "Hiçbir şey kendiliğinden yer değiştirmez. Bir görevi yarına ya da başka aya taşırsın veya bırakırsın; sayfada izi kalır.",
            "Tidak ada yang berpindah sendiri. Kamu memindahkan tugas ke besok, membawanya ke bulan lain, atau membuangnya, dan halaman menyimpan jejaknya.",
            "何も勝手には動きません。タスクを明日に移す、別の月に移す、破棄する。そのあとが、ページに跡として残ります。",
            "저절로 움직이는 것은 없어요. 할 일을 내일로 옮기거나, 다른 달로 옮기거나, 버리면 페이지에 흔적이 남아요.",
        ),
        t(
            "Bobbin tells you what was left open last month and shows it to you one at a time, with each way out explained.",
            "Bobbin te avisa de lo que quedó abierto el mes anterior y te lo enseña de una en una, con cada salida explicada.",
            "O Bobbin avisa o que ficou aberto no mês anterior e mostra uma de cada vez, com cada saída explicada.",
            "Bobbin zeigt dir, was im Vormonat offen blieb, eins nach dem anderen, und erklärt jeden Ausweg.",
            "Bobbin te signale ce qui est resté ouvert le mois dernier et te le montre une à une, avec chaque issue expliquée.",
            "Bobbin ti avvisa di cosa è rimasto aperto il mese scorso e te lo mostra un'attività alla volta, con ogni via d'uscita spiegata.",
            "Bobbin meldt wat vorige maand open bleef en laat het je een voor een zien, met uitleg bij elke uitweg.",
            "Bobbin informuje cię, które zadania pozostały otwarte w poprzednim miesiącu, i pokazuje je po kolei, z objaśnieniem każdego wyjścia.",
            "Bobbin сообщает, что осталось открытым в прошлом месяце, и показывает по одной задаче, объясняя каждый вариант.",
            "Bobbin geçen ay açık kalanları sana bildirir ve her çıkış yolunu açıklayarak onları tek tek gösterir.",
            "Bobbin memberi tahu apa yang masih terbuka dari bulan lalu dan menampilkannya satu per satu, dengan setiap pilihan dijelaskan.",
            "先月に未完了だったものをBobbinが知らせ、1件ずつ表示して、それぞれの選択肢を説明します。",
            "Bobbin이 지난달에 미완료로 남은 것을 알려 주고, 하나씩 보여 주면서 각 선택지를 설명해요.",
        ),
    )

    // The guide's pictures: a made-up page, never the user's diary.
    val guideInk = t(
        "Buy ink",
        "Comprar tinta",
        "Comprar tinta",
        "Tinte kaufen",
        "Acheter de l'encre",
        "Comprare inchiostro",
        "Inkt kopen",
        "Kupić atrament",
        "Купить чернила",
        "Mürekkep al",
        "Beli tinta",
        "インクを買う",
        "잉크 사기",
    )
    val guideDinner = t(
        "Dinner with Ana",
        "Cena con Ana",
        "Jantar com a Ana",
        "Essen mit Ana",
        "Dîner avec Ana",
        "Cena con Ana",
        "Diner met Ana",
        "Kolacja z Aną",
        "Ужин с Аной",
        "Ana ile akşam yemeği",
        "Makan malam dengan Ana",
        "アナと夕食",
        "아나와 저녁 식사",
    )
    val guideOpens = t(
        "Opens at 10",
        "Abre a las 10",
        "Abre às 10",
        "Öffnet um 10",
        "Ouvre à 10 h",
        "Apre alle 10",
        "Opent om 10 uur",
        "Otwiera się o 10",
        "Открывается в 10",
        "Saat 10'da açılıyor",
        "Buka jam 10",
        "10時に開店",
        "10시에 열어요",
    )
    val guideQuote = t(
        "Send the quote",
        "Enviar el presupuesto",
        "Enviar o orçamento",
        "Angebot schicken",
        "Envoyer le devis",
        "Inviare il preventivo",
        "Offerte sturen",
        "Wysłać wycenę",
        "Отправить смету",
        "Teklifi gönder",
        "Kirim penawaran",
        "見積もりを送る",
        "견적서 보내기",
    )
    val guideBank = t(
        "Call the bank",
        "Llamar al banco",
        "Ligar para o banco",
        "Bank anrufen",
        "Appeler la banque",
        "Chiamare la banca",
        "Bank bellen",
        "Zadzwonić do banku",
        "Позвонить в банк",
        "Bankayı ara",
        "Telepon bank",
        "銀行に電話する",
        "은행에 전화하기",
    )
    val guideCopy = t("Copy", "Copia", "Cópia", "Kopie", "Copie", "Copia", "Kopie", "Kopia", "Копия", "Kopya", "Salinan", "コピー", "사본")
    fun guideUnclosed(n: Int) = t(
        "$n tasks not closed",
        "$n tareas sin cerrar",
        "$n tarefas sem fechar",
        "$n Aufgaben offen",
        "$n tâches non clôturées",
        "$n attività non chiuse",
        "$n taken nog open",
        "$n ${word(n, "otwarte|otwarte|otwartych")} ${wordTask(n)}",
        "$n ${word(n, "задача не закрыта|задачи не закрыты|задач не закрыто")}",
        "$n görev kapanmadı",
        "$n tugas terbuka",
        "未完了のタスク${n}件",
        "미완료 할 일 ${n}개",
    )
    val guideToToday = t("To today", "A hoy", "Para hoje", "Auf heute", "À aujourd'hui", "A oggi", "Naar vandaag", "Na dziś", "На сегодня", "Bugüne", "Ke hari ini", "今日へ", "오늘로")
    val guideToMonth = t(
        "Other month",
        "A otro mes",
        "Outro mês",
        "Anderer Monat",
        "Autre mois",
        "Altro mese",
        "Andere maand",
        "Inny miesiąc",
        "В другой месяц",
        "Başka ay",
        "Bulan lain",
        "別の月へ",
        "다른 달로",
    )

    // --- 13. Ajustes ------------------------------------------------------------------------

    val sectionHelp = t("Help", "Ayuda", "Ajuda", "Hilfe", "Aide", "Aiuto", "Help", "Pomoc", "Помощь", "Yardım", "Bantuan", "ヘルプ", "도움말")
    val guideAgain = t(
        "See the guide again",
        "Ver la guía otra vez",
        "Ver o guia de novo",
        "Anleitung erneut ansehen",
        "Revoir le guide",
        "Rivedi la guida",
        "Gids opnieuw bekijken",
        "Zobacz przewodnik jeszcze raz",
        "Посмотреть руководство ещё раз",
        "Rehberi yeniden gör",
        "Lihat panduan lagi",
        "ガイドをもう一度見る",
        "가이드 다시 보기",
    )
    val guideAgainSub = t(
        "The four screens from the first start.",
        "Las cuatro pantallas del primer arranque.",
        "As quatro telas da primeira abertura.",
        "Die vier Seiten vom ersten Start.",
        "Les quatre écrans du premier lancement.",
        "Le quattro schermate del primo avvio.",
        "De vier schermen van de eerste start.",
        "Cztery ekrany z pierwszego uruchomienia.",
        "Четыре экрана с первого запуска.",
        "İlk açılıştaki dört ekran.",
        "Empat layar dari saat pertama dibuka.",
        "初回起動時の4つの画面。",
        "첫 실행 때 본 네 화면이에요.",
    )
    val keyRowSub = t(
        "The method's key, with its usual names.",
        "La clave del método, con sus nombres de siempre.",
        "A legenda do método, com os nomes de sempre.",
        "Die Legende der Methode, mit ihren üblichen Namen.",
        "La légende de la méthode, avec ses noms habituels.",
        "La legenda del metodo, con i nomi di sempre.",
        "De legenda van de methode, met de gebruikelijke namen.",
        "Legenda metody, z jej zwykłymi nazwami.",
        "Легенда метода с привычными названиями.",
        "Yöntemin anahtarı, bilinen adlarıyla.",
        "Keterangan metode, dengan nama-nama yang biasa dipakai.",
        "メソッドの凡例。ふだん使われる名前で説明します。",
        "방식의 범례와 원래 쓰는 이름이에요.",
    )
    val settingsTitle = t(
        "Settings",
        "Ajustes",
        "Ajustes",
        "Einstellungen",
        "Réglages",
        "Impostazioni",
        "Instellingen",
        "Ustawienia",
        "Настройки",
        "Ayarlar",
        "Pengaturan",
        "設定",
        "설정",
    )
    val sectionDay = t("Day", "Día", "Dia", "Tag", "Jour", "Giorno", "Dag", "Dzień", "День", "Gün", "Hari", "1日", "하루")
    val dayStartRow = t(
        "The day starts",
        "El día empieza",
        "O dia começa",
        "Der Tag beginnt",
        "La journée commence",
        "Il giorno inizia",
        "De dag begint",
        "Dzień zaczyna się",
        "День начинается",
        "Günün başlangıcı",
        "Hari dimulai",
        "1日の始まり",
        "하루의 시작",
    )
    fun dayStartAt(hour: Int): String {
        val time = clock(hour, 0)
        return t("At $time", "A las $time", "Às $time", "Um $time", "À $time", "Alle $time", "Om $time", "O $time", "В $time", "Saat $time", "Pukul $time", "${time}から", "${time}")
    }
    val weekStartRow = t(
        "The week starts",
        "La semana empieza",
        "A semana começa",
        "Die Woche beginnt",
        "La semaine commence",
        "La settimana inizia",
        "De week begint",
        "Tydzień zaczyna się",
        "Неделя начинается",
        "Haftanın başlangıcı",
        "Pekan dimulai",
        "週の始まり",
        "한 주의 시작",
    )

    private fun ptWeekArticle(day: DayOfWeek) = if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) "No" else "Na"

    fun weekStartSystem(day: DayOfWeek): String {
        val name = weekdayNames()[day.ordinal]
        return t(
            "$name, like the system",
            "El $name, como el sistema",
            "${ptWeekArticle(day)} $name, como o sistema",
            "$name, wie im System",
            "Le $name, comme le système",
            "${name.replaceFirstChar { it.uppercase() }}, come il sistema",
            "${name.replaceFirstChar { it.uppercase() }}, zoals het systeem",
            "Jak w systemie: $name",
            "Как в системе: $name",
            "$name, sistemdeki gibi",
            "$name, seperti sistem",
            "${name}(システムと同じ)",
            "${name}, 시스템 설정대로",
        )
    }

    fun weekStartDay(day: DayOfWeek): String {
        val name = weekdayNames()[day.ordinal]
        return t(
            name,
            "El $name",
            "${ptWeekArticle(day)} $name",
            name,
            "Le $name",
            "${name.replaceFirstChar { it.uppercase() }}",
            "${name.replaceFirstChar { it.uppercase() }}",
            name.replaceFirstChar { it.uppercase() },
            name.replaceFirstChar { it.uppercase() },
            name,
            "$name",
            name,
            name,
        )
    }

    val weekStartFollowSystem = t(
        "Like the system",
        "Como el sistema",
        "Como o sistema",
        "Wie im System",
        "Comme le système",
        "Come il sistema",
        "Zoals het systeem",
        "Jak w systemie",
        "Как в системе",
        "Sistemdeki gibi",
        "Seperti sistem",
        "システムと同じ",
        "시스템 설정대로",
    )
    val sectionReminder = t(
        "Reminder",
        "Recordatorio",
        "Lembrete",
        "Erinnerung",
        "Rappel",
        "Promemoria",
        "Herinnering",
        "Przypomnienie",
        "Напоминание",
        "Hatırlatıcı",
        "Pengingat",
        "リマインダー",
        "알림",
    )
    val reminderRow = t(
        "Reminder to go over the day",
        "Recordatorio para repasar el día",
        "Lembrete para repassar o dia",
        "Erinnerung, den Tag durchzugehen",
        "Rappel pour relire ta journée",
        "Promemoria per rivedere la giornata",
        "Herinnering om de dag door te nemen",
        "Przypomnienie, by przejrzeć dzień",
        "Напоминание просмотреть день",
        "Günü gözden geçirme hatırlatıcısı",
        "Pengingat untuk meninjau hari",
        "1日を見直すリマインダー",
        "하루 살펴보기 알림",
    )
    val reminderOff = t("Off", "Apagado", "Desativado", "Aus", "Désactivé", "Disattivato", "Uit", "Wyłączone", "Выключено", "Kapalı", "Mati", "オフ", "꺼짐")
    fun reminderAt(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t("At $time", "A las $time", "Às $time", "Um $time", "À $time", "Alle $time", "Om $time", "O $time", "В $time", "Saat $time", "Pukul $time", "${time}に通知", "${time}")
    }
    val reminderDenied = t(
        "Bobbin's notifications are turned off in the system.",
        "Las notificaciones de Bobbin están desactivadas en el sistema.",
        "As notificações do Bobbin estão desativadas no sistema.",
        "Die Benachrichtigungen von Bobbin sind im System deaktiviert.",
        "Les notifications de Bobbin sont désactivées dans le système.",
        "Le notifiche di Bobbin sono disattivate nel sistema.",
        "De meldingen van Bobbin staan uit in het systeem.",
        "Powiadomienia Bobbin są wyłączone w systemie.",
        "Уведомления Bobbin отключены в системе.",
        "Bobbin'in bildirimleri sistemde kapalı.",
        "Notifikasi Bobbin dimatikan di sistem.",
        "Bobbinの通知がシステムでオフになっています。",
        "시스템에서 Bobbin 알림이 꺼져 있어요.",
    )
    val openSystemSettings = t(
        "Open settings",
        "Abrir ajustes",
        "Abrir ajustes",
        "Einstellungen öffnen",
        "Ouvrir les réglages",
        "Apri impostazioni",
        "Instellingen openen",
        "Otwórz ustawienia",
        "Открыть настройки",
        "Ayarları aç",
        "Buka pengaturan",
        "設定を開く",
        "설정 열기",
    )
    val sectionPrivacy = t(
        "Privacy",
        "Privacidad",
        "Privacidade",
        "Datenschutz",
        "Confidentialité",
        "Privacy",
        "Privacy",
        "Prywatność",
        "Конфиденциальность",
        "Gizlilik",
        "Privasi",
        "プライバシー",
        "개인정보",
    )
    val lockRow = t(
        "Lock the journal",
        "Bloquear el diario",
        "Bloquear o diário",
        "Journal sperren",
        "Verrouiller le journal",
        "Blocca il diario",
        "Dagboek vergrendelen",
        "Zablokuj dziennik",
        "Блокировка дневника",
        "Günlüğü kilitle",
        "Kunci jurnal",
        "ジャーナルをロック",
        "다이어리 잠금",
    )
    val lockSubtitle = t(
        "Asks for your face, your fingerprint or your phone code",
        "Pide tu cara, tu huella o el código del teléfono",
        "Pede seu rosto, sua digital ou o código do telefone",
        "Fragt nach deinem Gesicht, deinem Fingerabdruck oder dem Code des Handys",
        "Demande ton visage, ton empreinte ou le code du téléphone",
        "Chiede il volto, l'impronta o il codice del telefono",
        "Vraagt om je gezicht, je vingerafdruk of de toegangscode van je telefoon",
        "Prosi o twarz, odcisk palca lub kod telefonu",
        "Запросит лицо, отпечаток пальца или код телефона",
        "Yüzünü, parmak izini ya da telefon kodunu ister",
        "Meminta wajah, sidik jari, atau kode ponselmu",
        "顔、指紋、または端末のパスコードで開きます",
        "얼굴, 지문 또는 휴대폰 암호를 요청해요",
    )
    val lockUnavailable = t(
        "Set a screen lock on your phone to use this.",
        "Pon un bloqueo de pantalla en el teléfono para usarlo.",
        "Configure um bloqueio de tela no telefone para usar isso.",
        "Richte eine Bildschirmsperre auf dem Handy ein, um das zu nutzen.",
        "Active un verrouillage d'écran sur ton téléphone pour l'utiliser.",
        "Imposta un blocco schermo sul telefono per usarlo.",
        "Stel een schermvergrendeling in op je telefoon om dit te gebruiken.",
        "Ustaw blokadę ekranu w telefonie, aby z tego korzystać.",
        "Чтобы пользоваться этим, включи блокировку экрана на телефоне.",
        "Bunu kullanmak için telefonuna bir ekran kilidi koy.",
        "Atur kunci layar di ponselmu untuk memakainya.",
        "使うには、端末で画面ロックを設定してください。",
        "사용하려면 휴대폰에 화면 잠금을 설정하세요.",
    )
    val sectionNotebook = t("Notebook", "Cuaderno", "Caderno", "Notizbuch", "Carnet", "Quaderno", "Notitieboek", "Notatnik", "Блокнот", "Defter", "Buku catatan", "ノート", "노트")
    val previewTask = t(
        "Buy ink",
        "Comprar tinta",
        "Comprar tinta",
        "Tinte kaufen",
        "Acheter de l'encre",
        "Comprare inchiostro",
        "Inkt kopen",
        "Kupić atrament",
        "Купить чернила",
        "Mürekkep al",
        "Beli tinta",
        "インクを買う",
        "잉크 사기",
    )
    val previewEvent = t(
        "Dinner with Ana",
        "Cena con Ana",
        "Jantar com a Ana",
        "Abendessen mit Ana",
        "Dîner avec Ana",
        "Cena con Ana",
        "Diner met Ana",
        "Kolacja z Aną",
        "Ужин с Аной",
        "Ana ile akşam yemeği",
        "Makan malam dengan Ana",
        "アナと夕食",
        "아나와 저녁 식사",
    )

    private val COVER_ORDER = listOf("rose", "peach", "butter", "sage", "mint", "sky", "periwinkle", "lilac")

    fun coverName(id: String): String {
        val names = t(
            "Rose, Peach, Butter, Sage, Mint, Sky, Periwinkle, Lilac",
            "Rosa, Melocotón, Mantequilla, Salvia, Menta, Cielo, Pervinca, Lila",
            "Rosa, Pêssego, Manteiga, Sálvia, Hortelã, Céu, Pervinca, Lilás",
            "Rosa, Pfirsich, Butter, Salbei, Minze, Himmel, Periwinkle, Flieder",
            "Rose, Pêche, Beurre, Sauge, Menthe, Ciel, Pervenche, Lilas",
            "Rosa, Pesca, Burro, Salvia, Menta, Cielo, Pervinca, Lilla",
            "Roze, Perzik, Boter, Salie, Munt, Hemel, Vinca, Lila",
            "Róż, Brzoskwinia, Masło, Szałwia, Mięta, Niebo, Barwinek, Lila",
            "Роза, Персик, Сливки, Шалфей, Мята, Небо, Барвинок, Сирень",
            "Gül, Şeftali, Tereyağı, Adaçayı, Nane, Gök mavisi, Cezayir menekşesi, Leylak",
            "Mawar, Persik, Mentega, Hijau sage, Mint, Langit, Periwinkle, Lilac",
            "ローズ, ピーチ, バター, セージ, ミント, スカイ, ペリウィンクル, ライラック",
            "로즈, 피치, 버터, 세이지, 민트, 하늘, 페리윙클, 라일락",
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
            "a puntini, a righe, a quadretti, bianca",
            "gestippeld, gelinieerd, geruit, blanco",
            "w kropki, w linie, w kratkę, gładki",
            "в точку, в линейку, в клетку, без разметки",
            "noktalı, çizgili, kareli, düz",
            "titik-titik, bergaris, kotak-kotak, polos",
            "ドット, 罫線, 方眼, 無地",
            "도트, 줄, 모눈, 무지",
        ).split(", ")
        return names[PAPER_ORDER.indexOf(id).takeIf { it >= 0 } ?: PAPER_ORDER.indexOf("dotted")]
    }

    val proLookHint = t(
        "Sage and dotted are free; the rest come with Bobbin Pro.",
        "Salvia y el punteado son gratis; lo demás, con Bobbin Pro.",
        "Sálvia e o pontilhado são grátis; o resto vem com o Bobbin Pro.",
        "Salbei und gepunktet sind kostenlos, der Rest kommt mit Bobbin Pro.",
        "Sauge et le pointillé sont gratuits ; le reste vient avec Bobbin Pro.",
        "Salvia e la carta a puntini sono gratis; il resto arriva con Bobbin Pro.",
        "Salie en gestippeld zijn gratis; de rest hoort bij Bobbin Pro.",
        "Szałwia i kropki są za darmo; reszta jest w Bobbin Pro.",
        "Шалфей и бумага в точку бесплатны, остальное входит в Bobbin Pro.",
        "Adaçayı ve noktalı ücretsiz; gerisi Bobbin Pro ile gelir.",
        "Hijau sage dan titik-titik gratis; sisanya dengan Bobbin Pro.",
        "セージとドットは無料です。ほかはBobbin Proで使えます。",
        "세이지와 도트는 무료이고, 나머지는 Bobbin Pro에 포함돼요.",
    )
    val useThis = t(
        "Use this style",
        "Usar este estilo",
        "Usar este estilo",
        "Diesen Stil nutzen",
        "Utiliser ce style",
        "Usa questo stile",
        "Deze stijl gebruiken",
        "Użyj tego stylu",
        "Применить этот стиль",
        "Bu stili kullan",
        "Pakai gaya ini",
        "このスタイルを使う",
        "이 스타일 사용",
    )
    val sectionBackup = t("Backup", "Copia", "Cópia", "Sicherung", "Sauvegarde", "Copia", "Back-up", "Kopia zapasowa", "Резервная копия", "Yedek", "Cadangan", "バックアップ", "백업")
    val exportRow = t(
        "Export backup",
        "Exportar copia",
        "Exportar cópia",
        "Kopie exportieren",
        "Exporter une copie",
        "Esporta copia",
        "Back-up exporteren",
        "Eksportuj kopię",
        "Экспорт копии",
        "Yedeği dışa aktar",
        "Ekspor cadangan",
        "バックアップを書き出す",
        "백업 내보내기",
    )
    val exportSubtitle = t(
        "A zip with your journal in JSON and Markdown",
        "Un zip con tu diario en JSON y en Markdown",
        "Um zip com seu diário em JSON e em Markdown",
        "Ein Zip mit deinem Journal als JSON und Markdown",
        "Un zip avec ton journal en JSON et en Markdown",
        "Uno zip con il tuo diario in JSON e in Markdown",
        "Een zip met je dagboek in JSON en Markdown",
        "Plik zip z twoim dziennikiem w JSON i Markdown",
        "Zip с твоим дневником в JSON и Markdown",
        "Günlüğün JSON ve Markdown olarak bir zip içinde",
        "Berkas zip berisi jurnalmu dalam JSON dan Markdown",
        "ジャーナルをJSONとMarkdownで入れたzip",
        "다이어리를 JSON과 Markdown으로 담은 zip",
    )
    val exportNothing = t(
        "There's nothing to back up yet.",
        "Aún no hay nada que copiar.",
        "Ainda não há nada para copiar.",
        "Es gibt noch nichts zu sichern.",
        "Il n'y a encore rien à copier.",
        "Non c'è ancora niente da copiare.",
        "Er is nog niets om te back-uppen.",
        "Na razie nie ma czego kopiować.",
        "Пока нечего копировать.",
        "Henüz yedeklenecek bir şey yok.",
        "Belum ada yang bisa dicadangkan.",
        "まだバックアップするものがありません。",
        "아직 백업할 것이 없어요.",
    )
    val importRow = t(
        "Import backup",
        "Importar copia",
        "Importar cópia",
        "Kopie importieren",
        "Importer une copie",
        "Importa copia",
        "Back-up importeren",
        "Importuj kopię",
        "Импорт копии",
        "Yedeği içe aktar",
        "Impor cadangan",
        "バックアップを読み込む",
        "백업 가져오기",
    )
    val importSubtitle = t(
        "Joins your journal, nothing gets deleted",
        "Se junta con tu diario, sin borrar nada",
        "Se junta ao seu diário, sem apagar nada",
        "Wird mit deinem Journal zusammengeführt, nichts wird gelöscht",
        "Se joint à ton journal, rien n'est supprimé",
        "Si unisce al tuo diario, senza eliminare nulla",
        "Wordt samengevoegd met je dagboek, er wordt niets verwijderd",
        "Łączy się z twoim dziennikiem, nic nie jest usuwane",
        "Объединяется с твоим дневником, ничего не удаляя",
        "Günlüğünle birleşir, hiçbir şey silinmez",
        "Digabung dengan jurnalmu, tidak ada yang dihapus",
        "今のジャーナルと合わせます。何も削除されません",
        "다이어리와 합쳐지고, 아무것도 지워지지 않아요",
    )
    val sectionPro = t(
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
    )
    val proRow = t(
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
    )
    val proSubtitle = t(
        "Covers, papers, trackers and widgets. One-time payment",
        "Portadas, papeles, seguimientos y widgets. Pago único",
        "Capas, papéis, trackers e widgets. Pagamento único",
        "Umschläge, Papiere, Tracker und Widgets. Einmalzahlung",
        "Couvertures, papiers, suivis et widgets. Paiement unique",
        "Copertine, tipi di carta, tracker e widget. Pagamento unico",
        "Omslagen, papier, trackers en widgets. Eenmalige betaling",
        "Okładki, papiery, trackery i widżety. Jednorazowa płatność",
        "Обложки, бумага, трекеры и виджеты. Разовая оплата",
        "Kapaklar, kağıtlar, çizelgeler ve widget'lar. Tek seferlik ödeme",
        "Sampul, kertas, pelacak, dan widget. Sekali bayar",
        "カバー、用紙、トラッカー、ウィジェット。買い切り",
        "표지, 속지, 트래커, 위젯. 한 번 결제",
    )
    val proOwned = t(
        "Purchased. Thank you.",
        "Comprado. Gracias.",
        "Comprado. Obrigado.",
        "Gekauft. Danke.",
        "Acheté. Merci.",
        "Acquistato. Grazie.",
        "Gekocht. Bedankt.",
        "Kupione. Dziękujemy.",
        "Куплено. Спасибо.",
        "Satın alındı. Teşekkürler.",
        "Sudah dibeli. Terima kasih.",
        "購入済みです。ありがとうございます。",
        "구매했어요. 고마워요.",
    )
    val restoreRow = t(
        "Restore purchase",
        "Restaurar compra",
        "Restaurar compra",
        "Kauf wiederherstellen",
        "Restaurer l'achat",
        "Ripristina acquisto",
        "Aankoop herstellen",
        "Przywróć zakup",
        "Восстановить покупку",
        "Satın alımı geri yükle",
        "Pulihkan pembelian",
        "購入を復元",
        "구매 복원",
    )
    val restoreDone = t(
        "Purchase restored.",
        "Compra restaurada.",
        "Compra restaurada.",
        "Kauf wiederhergestellt.",
        "Achat restauré.",
        "Acquisto ripristinato.",
        "Aankoop hersteld.",
        "Zakup przywrócony.",
        "Покупка восстановлена.",
        "Satın alım geri yüklendi.",
        "Pembelian dipulihkan.",
        "購入を復元しました。",
        "구매를 복원했어요.",
    )
    val restoreNothing = t(
        "There's no purchase to restore.",
        "No hay ninguna compra que restaurar.",
        "Não há nenhuma compra para restaurar.",
        "Es gibt keinen Kauf zum Wiederherstellen.",
        "Il n'y a aucun achat à restaurer.",
        "Non c'è nessun acquisto da ripristinare.",
        "Er is geen aankoop om te herstellen.",
        "Nie ma zakupu do przywrócenia.",
        "Нет покупки, которую можно восстановить.",
        "Geri yüklenecek bir satın alım yok.",
        "Tidak ada pembelian yang bisa dipulihkan.",
        "復元できる購入はありません。",
        "복원할 구매가 없어요.",
    )
    val sectionMoreApps = t(
        "More apps",
        "Más apps",
        "Mais apps",
        "Weitere Apps",
        "Plus d'apps",
        "Altre app",
        "Meer apps",
        "Więcej aplikacji",
        "Другие приложения",
        "Diğer uygulamalar",
        "Aplikasi lainnya",
        "ほかのアプリ",
        "다른 앱",
    )
    val siblingPurl = t(
        "One line a day, read again every year",
        "Una línea al día que vuelves a leer cada año",
        "Uma linha por dia, relida a cada ano",
        "Eine Zeile am Tag, jedes Jahr wieder gelesen",
        "Une ligne par jour, relue chaque année",
        "Una riga al giorno, riletta ogni anno",
        "Eén regel per dag, elk jaar opnieuw gelezen",
        "Jedna linijka dziennie, czytana ponownie co roku",
        "Одна строка в день, которую перечитываешь каждый год",
        "Günde bir satır, her yıl tekrar okunur",
        "Satu baris sehari, dibaca ulang setiap tahun",
        "1日1行を、毎年読み返す",
        "하루 한 줄, 해마다 다시 읽어요",
    )
    val siblingQuilt = t(
        "Your habits, a year at a glance",
        "Tus hábitos, un año a la vista",
        "Seus hábitos, um ano à vista",
        "Deine Gewohnheiten, ein Jahr im Blick",
        "Tes habitudes, une année en un coup d'oeil",
        "Le tue abitudini, un anno a colpo d'occhio",
        "Je gewoontes, een jaar in één oogopslag",
        "Twoje nawyki, cały rok w jednym spojrzeniu",
        "Твои привычки: весь год перед глазами",
        "Alışkanlıkların, tek bakışta bir yıl",
        "Kebiasaanmu, satu tahun sekilas pandang",
        "習慣を、1年ひと目で",
        "습관을 한 해 한눈에",
    )
    val siblingMood = t(
        "How each day went, in colour",
        "Cómo te ha ido cada día, en color",
        "Como foi cada dia, em cores",
        "Wie jeder Tag war, in Farbe",
        "Comment chaque jour s'est passé, en couleur",
        "Com'è andato ogni giorno, a colori",
        "Hoe elke dag ging, in kleur",
        "Jak minął każdy dzień, w kolorze",
        "Как прошёл каждый день, в цвете",
        "Her günün nasıl geçtiği, renkle",
        "Bagaimana setiap harimu berlalu, dalam warna",
        "その日がどうだったかを、色で",
        "하루하루가 어땠는지, 색으로",
    )
    val sectionAbout = t("About", "Acerca de", "Sobre", "Über", "À propos", "Informazioni", "Over", "O aplikacji", "О приложении", "Hakkında", "Tentang", "このアプリについて", "정보")
    val privacyRow = t(
        "Privacy policy",
        "Política de privacidad",
        "Política de privacidade",
        "Datenschutzerklärung",
        "Politique de confidentialité",
        "Informativa sulla privacy",
        "Privacybeleid",
        "Polityka prywatności",
        "Политика конфиденциальности",
        "Gizlilik politikası",
        "Kebijakan privasi",
        "プライバシーポリシー",
        "개인정보 처리방침",
    )
    fun version(v: String) = t(
        "Version $v",
        "Versión $v",
        "Versão $v",
        "Version $v",
        "Version $v",
        "Versione $v",
        "Versie $v",
        "Wersja $v",
        "Версия $v",
        "Sürüm $v",
        "Versi $v",
        "バージョン ${v}",
        "버전 ${v}",
    )
    val wipeRow = t(
        "Delete all data",
        "Borrar todos los datos",
        "Apagar todos os dados",
        "Alle Daten löschen",
        "Supprimer toutes les données",
        "Elimina tutti i dati",
        "Alle gegevens verwijderen",
        "Usuń wszystkie dane",
        "Удалить все данные",
        "Tüm verileri sil",
        "Hapus semua data",
        "すべてのデータを削除",
        "모든 데이터 삭제",
    )
    val wipeSubtitle = t(
        "Leaves the journal empty on this phone. Bobbin Pro stays.",
        "Deja el diario vacío en este teléfono. Bobbin Pro se conserva.",
        "Deixa o diário vazio neste telefone. O Bobbin Pro continua.",
        "Leert das Journal auf diesem Handy. Bobbin Pro bleibt.",
        "Vide le journal sur ce téléphone. Bobbin Pro est conservé.",
        "Lascia il diario vuoto su questo telefono. Bobbin Pro resta.",
        "Maakt het dagboek op deze telefoon leeg. Bobbin Pro blijft behouden.",
        "Zostawia dziennik pusty na tym telefonie. Bobbin Pro zostaje.",
        "Оставляет дневник пустым на этом телефоне. Bobbin Pro сохраняется.",
        "Günlüğü bu telefonda boş bırakır. Bobbin Pro kalır.",
        "Mengosongkan jurnal di ponsel ini. Bobbin Pro tetap ada.",
        "この端末のジャーナルを空にします。Bobbin Proは残ります。",
        "이 휴대폰의 다이어리를 비워요. Bobbin Pro는 그대로예요.",
    )

    // --- 14. Bobbin Pro ----------------------------------------------------------------------

    val proTitle = t(
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
        "Bobbin Pro",
    )
    val proOnce = t(
        "One-time purchase, no subscription.",
        "Compra única, sin suscripción.",
        "Compra única, sem assinatura.",
        "Einmalkauf, kein Abo.",
        "Achat unique, sans abonnement.",
        "Acquisto una tantum, senza abbonamento.",
        "Eenmalige aankoop, geen abonnement.",
        "Jednorazowy zakup, bez subskrypcji.",
        "Разовая покупка, без подписки.",
        "Tek seferlik satın alım, abonelik yok.",
        "Sekali beli, tanpa langganan.",
        "買い切りで、サブスクリプションはありません。",
        "한 번 결제, 구독 없음.",
    )
    val proCovers = t(
        "Seven more covers",
        "Siete portadas más",
        "Mais sete capas",
        "Sieben weitere Umschläge",
        "Sept couvertures de plus",
        "Sette copertine in più",
        "Zeven extra omslagen",
        "Siedem dodatkowych okładek",
        "Ещё семь обложек",
        "Yedi kapak daha",
        "Tujuh sampul lagi",
        "カバーがさらに7種類",
        "표지 일곱 개 더",
    )
    val proPapers = t(
        "Lined, grid and blank paper",
        "Rayado, cuadrícula y liso",
        "Pautado, quadriculado e liso",
        "Liniert, kariert und blanko",
        "Ligné, quadrillé et uni",
        "Carta a righe, a quadretti e bianca",
        "Gelinieerd, geruit en blanco papier",
        "Papier w linie, w kratkę i gładki",
        "Бумага в линейку, в клетку и без разметки",
        "Çizgili, kareli ve düz kağıt",
        "Kertas bergaris, kotak-kotak, dan polos",
        "罫線、方眼、無地の用紙",
        "줄, 모눈, 무지 속지",
    )
    val proTrackers = t(
        "More than one tracker",
        "Más de un seguimiento",
        "Mais de um tracker",
        "Mehr als ein Tracker",
        "Plus d'un suivi",
        "Più di un tracker",
        "Meer dan één tracker",
        "Więcej niż jeden tracker",
        "Больше одного трекера",
        "Birden fazla çizelge",
        "Lebih dari satu pelacak",
        "複数のトラッカー",
        "트래커 두 개 이상",
    )
    val proMonthWidget = t(
        "The month widget",
        "El widget del mes",
        "O widget do mês",
        "Das Monats-Widget",
        "Le widget du mois",
        "Il widget del mese",
        "De maandwidget",
        "Widżet miesiąca",
        "Виджет месяца",
        "Ay widget'ı",
        "Widget bulan",
        "月のウィジェット",
        "월간 위젯",
    )
    val proLockWidget = t(
        "The lock screen widget",
        "El widget de la pantalla de bloqueo",
        "O widget da tela de bloqueio",
        "Das Sperrbildschirm-Widget",
        "Le widget de l'écran verrouillé",
        "Il widget della schermata di blocco",
        "De widget voor het vergrendelscherm",
        "Widżet ekranu blokady",
        "Виджет на экране блокировки",
        "Kilit ekranı widget'ı",
        "Widget layar kunci",
        "ロック画面のウィジェット",
        "잠금 화면 위젯",
    )
    val proFree = t(
        "The whole method is free, and it will stay that way.",
        "El método entero es gratis, y lo seguirá siendo.",
        "O método inteiro é grátis, e vai continuar assim.",
        "Die ganze Methode ist kostenlos und bleibt es.",
        "Toute la méthode est gratuite, et le restera.",
        "Tutto il metodo è gratis, e lo resterà.",
        "De hele methode is gratis en dat blijft zo.",
        "Cała metoda jest za darmo i tak zostanie.",
        "Весь метод бесплатен, и так и останется.",
        "Yöntemin tamamı ücretsiz ve öyle kalacak.",
        "Seluruh metodenya gratis, dan akan tetap begitu.",
        "メソッドのすべてが無料で、これからもそうです。",
        "방식 전체는 무료이고, 앞으로도 그래요.",
    )
    fun buy(price: String) = t(
        "Buy for $price",
        "Comprar por $price",
        "Comprar por $price",
        "Für $price kaufen",
        "Acheter pour $price",
        "Acquista a $price",
        "Kopen voor $price",
        "Kup za $price",
        "Купить за $price",
        "Satın al: $price",
        "Beli $price",
        "${price}で購入",
        "${price}에 구매",
    )
    val restore = t(
        "Restore",
        "Restaurar",
        "Restaurar",
        "Wiederherstellen",
        "Restaurer",
        "Ripristina",
        "Herstellen",
        "Przywróć",
        "Восстановить",
        "Geri yükle",
        "Pulihkan",
        "復元",
        "복원",
    )
    val storeUnavailable = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora.",
        "A loja não está disponível agora.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
        "Lo store non è disponibile al momento.",
        "De store is nu niet beschikbaar.",
        "Sklep jest teraz niedostępny.",
        "Магазин сейчас недоступен.",
        "Mağaza şu anda kullanılamıyor.",
        "Toko tidak tersedia saat ini.",
        "ストアは現在利用できません。",
        "지금은 스토어를 사용할 수 없어요.",
    )
    val buyFailed = t(
        "The purchase could not be completed.",
        "No se ha podido completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
        "Non è stato possibile completare l'acquisto.",
        "De aankoop kon niet worden voltooid.",
        "Nie udało się dokończyć zakupu.",
        "Не удалось завершить покупку.",
        "Satın alma tamamlanamadı.",
        "Pembelian tidak bisa diselesaikan.",
        "購入を完了できませんでした。",
        "구매를 완료하지 못했어요.",
    )

    // --- 15. Importar, exportar y borrar -------------------------------------------------------

    val importTitle = t(
        "Import backup",
        "Importar copia",
        "Importar cópia",
        "Kopie importieren",
        "Importer une copie",
        "Importa copia",
        "Back-up importeren",
        "Importuj kopię",
        "Импорт копии",
        "Yedeği içe aktar",
        "Impor cadangan",
        "バックアップを読み込む",
        "백업 가져오기",
    )

    private fun importAddedPhrase(n: Int): String = if (isSingular(n)) {
        t(
            "1 new entry",
            "1 entrada nueva",
            "1 entrada nova",
            "1 neuer Eintrag",
            "1 nouvelle entrée",
            "1 voce nuova",
            "1 nieuw item",
            "1 nowy wpis",
            "1 новая запись",
            "1 yeni kayıt",
            "1 entri baru",
            "新しい項目1件",
            "새 항목 1개",
        )
    } else {
        t(
            "$n new entries",
            "$n entradas nuevas",
            "$n entradas novas",
            "$n neue Einträge",
            "$n nouvelles entrées",
            "$n voci nuove",
            "$n nieuwe items",
            "$n ${word(n, "nowy wpis|nowe wpisy|nowych wpisów")}",
            "$n ${word(n, "новая запись|новые записи|новых записей")}",
            "$n yeni kayıt",
            "$n entri baru",
            "新しい項目${n}件",
            "새 항목 ${n}개",
        )
    }

    private fun importUpdatedPhrase(n: Int): String = if (isSingular(n)) {
        t(
            "1 newer than yours",
            "1 más reciente que las tuyas",
            "1 mais recente que as suas",
            "1, der neuer ist als deine",
            "1 plus récente que les tiennes",
            "1 più recente delle tue",
            "1 die nieuwer is dan die van jou",
            "1 nowszy niż twój",
            "1 запись новее твоей",
            "seninkilerden daha yeni 1 kayıt",
            "1 lebih baru dari milikmu",
            "今の項目より新しいもの1件",
            "내 것보다 최신인 항목 1개",
        )
    } else {
        t(
            "$n newer than yours",
            "$n más recientes que las tuyas",
            "$n mais recentes que as suas",
            "$n, die neuer sind als deine",
            "$n plus récentes que les tiennes",
            "$n più recenti delle tue",
            "$n die nieuwer zijn dan die van jou",
            "$n ${word(n, "nowszy niż twój|nowsze niż twoje|nowszych niż twoje")}",
            "$n ${word(n, "запись новее твоей|записи новее твоих|записей новее твоих")}",
            "seninkilerden daha yeni $n kayıt",
            "$n lebih baru dari milikmu",
            "今の項目より新しいもの${n}件",
            "내 것보다 최신인 항목 ${n}개",
        )
    }

    private fun importSamePhrase(n: Int): String = if (isSingular(n)) {
        t(
            "1 was already here",
            "1 ya estaba",
            "1 já estava aqui",
            "1 war schon da",
            "1 était déjà là",
            "1 c'era già",
            "1 was er al",
            "1 już tu był",
            "1 запись уже была",
            "1 kayıt zaten buradaydı",
            "1 sudah ada",
            "1件はすでにありました",
            "이미 있던 항목 1개",
        )
    } else {
        t(
            "$n were already here",
            "$n ya estaban",
            "$n já estavam aqui",
            "$n waren schon da",
            "$n étaient déjà là",
            "$n c'erano già",
            "$n waren er al",
            "$n ${word(n, "już tu był|już tu były|już tu było")}",
            "$n ${word(n, "запись уже была|записи уже были|записей уже было")}",
            "$n kayıt zaten buradaydı",
            "$n sudah ada",
            "${n}件はすでにありました",
            "이미 있던 항목 ${n}개",
        )
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
            t(
                "brings nothing new",
                "no trae nada nuevo",
                "não traz nada novo",
                "bringt nichts Neues",
                "n'apporte rien de nouveau",
                "non porta nulla di nuovo",
                "bevat niets nieuws",
                "nie zawiera nic nowego",
                "нет ничего нового",
                "yeni bir şey getirmiyor",
                "tidak membawa hal baru",
                "新しいものはありません",
                "백업에 새로 가져올 항목이 없어요",
            )
        } else {
            val and = t(" and ", " y ", " e ", " und ", " et ", " e ", " en ", " i ", " и ", " ve ", " dan ", "、", ", ")
            t(
                "brings ",
                "trae ",
                "traz ",
                "bringt ",
                "apporte ",
                "porta ",
                "bevat ",
                "zawiera ",
                "есть ",
                "şunları getiriyor: ",
                "membawa ",
                "",
                "백업에서 가져올 것: ",
            ) + parts.joinToString(and)
        }
        val tail = if (same > 0) {
            val same2 = importSamePhrase(same)
            t(
                "; $same2.",
                "; $same2.",
                "; $same2.",
                "; $same2.",
                " ; $same2.",
                "; $same2.",
                "; $same2.",
                "; $same2.",
                "; $same2.",
                "; $same2.",
                "; $same2.",
                "。${same2}。",
                ". ${same2}.",
            )
        } else {
            fullStop()
        }
        return t(
            "The backup $brings$tail Nothing gets deleted.",
            "La copia $brings$tail No se borra nada.",
            "A cópia $brings$tail Nada é apagado.",
            "Die Sicherung $brings$tail Es wird nichts gelöscht.",
            "La copie $brings$tail Rien n'est supprimé.",
            "La copia $brings$tail Non viene eliminato nulla.",
            "De back-up $brings$tail Er wordt niets verwijderd.",
            "Kopia $brings$tail Nic nie zostanie usunięte.",
            "В копии $brings$tail Ничего не удаляется.",
            "Yedek $brings$tail Hiçbir şey silinmez.",
            "Cadangan ini $brings$tail Tidak ada yang dihapus.",
            "バックアップの内容: ${brings}${tail}何も削除されません。",
            "${brings}${tail} 아무것도 삭제되지 않아요.",
        )
    }

    val importAction = t("Import", "Importar", "Importar", "Importieren", "Importer", "Importa", "Importeren", "Importuj", "Импортировать", "İçe aktar", "Impor", "読み込む", "가져오기")

    fun importDone(n: Int): String = if (n == 0) {
        t(
            "Journal up to date: there was nothing to change.",
            "Diario al día: no había nada que cambiar.",
            "Diário em dia: não havia nada para mudar.",
            "Journal aktuell: es gab nichts zu ändern.",
            "Journal à jour : il n'y avait rien à changer.",
            "Diario aggiornato: non c'era niente da cambiare.",
            "Dagboek bijgewerkt: er was niets te wijzigen.",
            "Dziennik aktualny: nie było nic do zmiany.",
            "Дневник актуален: менять было нечего.",
            "Günlük güncel: değiştirilecek bir şey yoktu.",
            "Jurnal sudah mutakhir: tidak ada yang perlu diubah.",
            "ジャーナルは最新です。変更するものはありませんでした。",
            "다이어리가 최신 상태예요. 바꿀 것이 없었어요.",
        )
    } else {
        val c = wordChange(n)
        t(
            "Journal up to date: $n $c.",
            "Diario al día: $n $c.",
            "Diário em dia: $n $c.",
            "Journal aktuell: $n $c.",
            "Journal à jour : $n $c.",
            "Diario aggiornato: $n $c.",
            "Dagboek bijgewerkt: $n $c.",
            "Dziennik aktualny: $n $c.",
            "Дневник актуален: $n $c.",
            "Günlük güncel: $n $c.",
            "Jurnal sudah mutakhir: $n $c.",
            "ジャーナルは最新です: ${n}件の${c}。",
            "다이어리가 최신 상태예요. ${c} ${n}건.",
        )
    }

    val importFailedTitle = t(
        "Couldn't import",
        "No se ha podido importar",
        "Não foi possível importar",
        "Import fehlgeschlagen",
        "Échec de l'import",
        "Importazione non riuscita",
        "Importeren mislukt",
        "Nie udało się zaimportować",
        "Не удалось импортировать",
        "İçe aktarılamadı",
        "Impor gagal",
        "読み込めませんでした",
        "가져오지 못했어요",
    )
    val importDamaged = t(
        "The backup is incomplete or damaged. Your journal wasn't touched.",
        "La copia está incompleta o dañada. Tu diario no se ha tocado.",
        "A cópia está incompleta ou danificada. Seu diário não foi alterado.",
        "Die Sicherung ist unvollständig oder beschädigt. Dein Journal wurde nicht verändert.",
        "La copie est incomplète ou endommagée. Ton journal n'a pas été touché.",
        "La copia è incompleta o danneggiata. Il tuo diario non è stato toccato.",
        "De back-up is onvolledig of beschadigd. Je dagboek is niet aangeraakt.",
        "Kopia jest niepełna lub uszkodzona. Twój dziennik pozostał nietknięty.",
        "Копия неполная или повреждена. Твой дневник не тронут.",
        "Yedek eksik ya da bozuk. Günlüğüne dokunulmadı.",
        "Cadangan tidak lengkap atau rusak. Jurnalmu tidak diubah.",
        "バックアップが不完全か壊れています。今のジャーナルには手を加えていません。",
        "백업이 불완전하거나 손상됐어요. 다이어리는 그대로예요.",
    )
    val importTooNew = t(
        "This backup is from a newer version of Bobbin. Update the app and try again.",
        "Esta copia es de una versión más nueva de Bobbin. Actualiza la app y vuelve a probar.",
        "Esta cópia é de uma versão mais nova do Bobbin. Atualize o app e tente de novo.",
        "Diese Sicherung stammt aus einer neueren Version von Bobbin. Aktualisiere die App und versuch es erneut.",
        "Cette copie vient d'une version plus récente de Bobbin. Mets à jour l'app et réessaie.",
        "Questa copia viene da una versione più recente di Bobbin. Aggiorna l'app e riprova.",
        "Deze back-up komt van een nieuwere versie van Bobbin. Werk de app bij en probeer het opnieuw.",
        "Ta kopia pochodzi z nowszej wersji Bobbin. Zaktualizuj aplikację i spróbuj ponownie.",
        "Эта копия из более новой версии Bobbin. Обнови приложение и попробуй снова.",
        "Bu yedek Bobbin'in daha yeni bir sürümünden. Uygulamayı güncelleyip tekrar dene.",
        "Cadangan ini dari versi Bobbin yang lebih baru. Perbarui aplikasinya lalu coba lagi.",
        "このバックアップは新しいバージョンのBobbinで作られました。アプリを更新してもう一度お試しください。",
        "더 새로운 버전의 Bobbin에서 만든 백업이에요. 앱을 업데이트한 뒤 다시 시도해 주세요.",
    )
    val importEmpty = t(
        "The backup has no entries or collections.",
        "La copia no tiene entradas ni colecciones.",
        "A cópia não tem entradas nem coleções.",
        "Die Sicherung enthält keine Einträge und keine Sammlungen.",
        "La copie ne contient ni entrées ni collections.",
        "La copia non contiene voci né raccolte.",
        "De back-up bevat geen items of collecties.",
        "Kopia nie zawiera wpisów ani kolekcji.",
        "В копии нет ни записей, ни коллекций.",
        "Yedekte ne kayıt ne de koleksiyon var.",
        "Cadangan ini tidak berisi entri atau koleksi.",
        "バックアップに項目もコレクションもありません。",
        "백업에 항목도 목록도 없어요.",
    )
    val exportFailed = t(
        "Couldn't save the backup.",
        "No se ha podido guardar la copia.",
        "Não foi possível salvar a cópia.",
        "Die Sicherung konnte nicht gespeichert werden.",
        "Impossible d'enregistrer la copie.",
        "Impossibile salvare la copia.",
        "De back-up kon niet worden opgeslagen.",
        "Nie udało się zapisać kopii.",
        "Не удалось сохранить копию.",
        "Yedek kaydedilemedi.",
        "Gagal menyimpan cadangan.",
        "バックアップを保存できませんでした。",
        "백업을 저장하지 못했어요.",
    )
    val wipeTitle = t(
        "Delete the whole journal?",
        "¿Borrar todo el diario?",
        "Apagar todo o diário?",
        "Das ganze Journal löschen?",
        "Supprimer tout le journal ?",
        "Eliminare tutto il diario?",
        "Het hele dagboek verwijderen?",
        "Usunąć cały dziennik?",
        "Удалить весь дневник?",
        "Tüm günlük silinsin mi?",
        "Hapus seluruh jurnal?",
        "ジャーナルをすべて削除しますか？",
        "다이어리 전체를 삭제할까요?",
    )
    val wipeText = t(
        "All entries, collections and settings on this phone are deleted. Backups you exported are not touched, and Bobbin Pro stays.",
        "Se borran todas las entradas, colecciones y ajustes de este teléfono. Las copias que hayas exportado no se tocan, y Bobbin Pro se conserva.",
        "Todas as entradas, coleções e ajustes deste telefone são apagados. As cópias que você exportou não são alteradas, e o Bobbin Pro continua.",
        "Alle Einträge, Sammlungen und Einstellungen auf diesem Handy werden gelöscht. Exportierte Sicherungen bleiben unberührt, und Bobbin Pro bleibt.",
        "Toutes les entrées, collections et réglages de ce téléphone sont supprimés. Les copies que tu as exportées ne sont pas touchées, et Bobbin Pro est conservé.",
        "Tutte le voci, le raccolte e le impostazioni su questo telefono vengono eliminate. Le copie che hai esportato non vengono toccate, e Bobbin Pro resta.",
        "Alle items, collecties en instellingen op deze telefoon worden verwijderd. Back-ups die je hebt geëxporteerd worden niet aangeraakt, en Bobbin Pro blijft behouden.",
        "Wszystkie wpisy, kolekcje i ustawienia na tym telefonie zostaną usunięte. Wyeksportowane kopie nie zostaną naruszone, a Bobbin Pro zostaje.",
        "Все записи, коллекции и настройки на этом телефоне будут удалены. Экспортированные копии не затрагиваются, а Bobbin Pro сохраняется.",
        "Bu telefondaki tüm kayıtlar, koleksiyonlar ve ayarlar silinir. Dışa aktardığın yedeklere dokunulmaz ve Bobbin Pro kalır.",
        "Semua entri, koleksi, dan pengaturan di ponsel ini dihapus. Cadangan yang sudah kamu ekspor tidak disentuh, dan Bobbin Pro tetap ada.",
        "この端末のすべての項目、コレクション、設定が削除されます。書き出したバックアップには影響せず、Bobbin Proは残ります。",
        "이 휴대폰의 모든 항목, 목록, 설정이 삭제돼요. 내보낸 백업은 그대로 있고, Bobbin Pro도 유지돼요.",
    )
    val wipeContinue = t("Continue", "Continuar", "Continuar", "Weiter", "Continuer", "Continua", "Doorgaan", "Kontynuuj", "Продолжить", "Devam", "Lanjutkan", "続ける", "계속")
    fun wipeConfirmTitle(word: String) = t(
        "Type the word $word to confirm",
        "Escribe la palabra $word para confirmar",
        "Digite a palavra $word para confirmar",
        "Gib das Wort $word ein, um zu bestätigen",
        "Écris le mot $word pour confirmer",
        "Scrivi la parola $word per confermare",
        "Typ het woord $word om te bevestigen",
        "Wpisz słowo $word, aby potwierdzić",
        "Чтобы подтвердить, введи слово $word",
        "Onaylamak için $word sözcüğünü yaz",
        "Ketik kata $word untuk mengonfirmasi",
        "確認のため「${word}」と入力してください",
        "확인하려면 이 단어를 입력하세요: ${word}",
    )
    val wipeWord = t("delete", "borrar", "apagar", "löschen", "effacer", "elimina", "verwijderen", "usuń", "удалить", "sil", "hapus", "削除", "삭제")
    val wipeAction = t(
        "Delete everything",
        "Borrar todo",
        "Apagar tudo",
        "Alles löschen",
        "Tout supprimer",
        "Elimina tutto",
        "Alles verwijderen",
        "Usuń wszystko",
        "Удалить всё",
        "Hepsini sil",
        "Hapus semuanya",
        "すべて削除",
        "모두 삭제",
    )

    // --- 16. Bloqueo y avisos de carga ---------------------------------------------------------

    val unlock = t(
        "Unlock",
        "Desbloquear",
        "Desbloquear",
        "Entsperren",
        "Déverrouiller",
        "Sblocca",
        "Ontgrendelen",
        "Odblokuj",
        "Разблокировать",
        "Kilidi aç",
        "Buka kunci",
        "ロックを解除",
        "잠금 해제",
    )
    val lockPromptTitle = t(
        "Open Bobbin",
        "Abrir Bobbin",
        "Abrir Bobbin",
        "Bobbin öffnen",
        "Ouvrir Bobbin",
        "Apri Bobbin",
        "Bobbin openen",
        "Otwórz Bobbin",
        "Открыть Bobbin",
        "Bobbin'i aç",
        "Buka Bobbin",
        "Bobbinを開く",
        "Bobbin 열기",
    )
    val lockPromptSubtitle = t(
        "Your journal is locked",
        "Tu diario está bloqueado",
        "Seu diário está bloqueado",
        "Dein Journal ist gesperrt",
        "Ton journal est verrouillé",
        "Il tuo diario è bloccato",
        "Je dagboek is vergrendeld",
        "Twój dziennik jest zablokowany",
        "Твой дневник заблокирован",
        "Günlüğün kilitli",
        "Jurnalmu terkunci",
        "ジャーナルはロックされています",
        "다이어리가 잠겨 있어요",
    )
    val updateNeeded = t(
        "This journal was written with a newer version of Bobbin. Update the app to open it; nothing was touched.",
        "Este diario se escribió con una versión más nueva de Bobbin. Actualiza la app para abrirlo; no se ha tocado nada.",
        "Este diário foi escrito com uma versão mais nova do Bobbin. Atualize o app para abri-lo; nada foi alterado.",
        "Dieses Journal wurde mit einer neueren Version von Bobbin geschrieben. Aktualisiere die App, um es zu öffnen; nichts wurde verändert.",
        "Ce journal a été écrit avec une version plus récente de Bobbin. Mets à jour l'app pour l'ouvrir ; rien n'a été touché.",
        "Questo diario è stato scritto con una versione più recente di Bobbin. Aggiorna l'app per aprirlo; non è stato toccato nulla.",
        "Dit dagboek is geschreven met een nieuwere versie van Bobbin. Werk de app bij om het te openen; er is niets aangeraakt.",
        "Ten dziennik został zapisany nowszą wersją Bobbin. Zaktualizuj aplikację, aby go otworzyć; nic nie zostało naruszone.",
        "Этот дневник записан в более новой версии Bobbin. Обнови приложение, чтобы открыть его; ничего не тронуто.",
        "Bu günlük Bobbin'in daha yeni bir sürümüyle yazılmış. Açmak için uygulamayı güncelle; hiçbir şeye dokunulmadı.",
        "Jurnal ini ditulis dengan versi Bobbin yang lebih baru. Perbarui aplikasinya untuk membukanya; tidak ada yang diubah.",
        "このジャーナルは新しいバージョンのBobbinで書かれています。開くにはアプリを更新してください。何も変更されていません。",
        "더 새로운 버전의 Bobbin으로 쓴 다이어리예요. 열려면 앱을 업데이트하세요. 아무것도 건드리지 않았어요.",
    )
    val openStore = t(
        "Open the store",
        "Abrir la tienda",
        "Abrir a loja",
        "Store öffnen",
        "Ouvrir la boutique",
        "Apri lo store",
        "Store openen",
        "Otwórz sklep",
        "Открыть магазин",
        "Mağazayı aç",
        "Buka toko aplikasi",
        "ストアを開く",
        "스토어 열기",
    )
    val migrationFailed = t(
        "Couldn't prepare the journal for this version. It is intact and nothing was touched.",
        "No se ha podido preparar el diario para esta versión. Sigue intacto y no se ha tocado nada.",
        "Não foi possível preparar o diário para esta versão. Ele continua intacto e nada foi alterado.",
        "Das Journal konnte nicht für diese Version vorbereitet werden. Es ist unversehrt, nichts wurde verändert.",
        "Impossible de préparer le journal pour cette version. Il est intact et rien n'a été touché.",
        "Impossibile preparare il diario per questa versione. È intatto e non è stato toccato nulla.",
        "Het dagboek kon niet worden voorbereid op deze versie. Het is intact en er is niets aangeraakt.",
        "Nie udało się przygotować dziennika na tę wersję. Jest nienaruszony i nic nie zostało zmienione.",
        "Не удалось подготовить дневник к этой версии. Он цел, ничего не тронуто.",
        "Günlük bu sürüm için hazırlanamadı. Olduğu gibi duruyor ve hiçbir şeye dokunulmadı.",
        "Jurnal tidak bisa disiapkan untuk versi ini. Jurnalnya utuh dan tidak ada yang diubah.",
        "このバージョン用にジャーナルを準備できませんでした。ジャーナルは無事で、何も変更されていません。",
        "이 버전에 맞게 다이어리를 준비하지 못했어요. 다이어리는 온전하고 아무것도 건드리지 않았어요.",
    )

    // --- 17. Compartir ----------------------------------------------------------------------

    val shareImage = t(
        "Share as image",
        "Compartir como imagen",
        "Compartilhar como imagem",
        "Als Bild teilen",
        "Partager en image",
        "Condividi come immagine",
        "Delen als afbeelding",
        "Udostępnij jako obraz",
        "Поделиться как изображением",
        "Görsel olarak paylaş",
        "Bagikan sebagai gambar",
        "画像として共有",
        "이미지로 공유",
    )
    val shareText = t(
        "Share as text",
        "Compartir como texto",
        "Compartilhar como texto",
        "Als Text teilen",
        "Partager en texte",
        "Condividi come testo",
        "Delen als tekst",
        "Udostępnij jako tekst",
        "Поделиться как текстом",
        "Metin olarak paylaş",
        "Bagikan sebagai teks",
        "テキストとして共有",
        "텍스트로 공유",
    )
    fun sharePages(n: Int) = t(
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "$n ${wordImage(n)}",
        "${n}枚の${wordImage(n)}",
        "${wordImage(n)} ${n}장",
    )
    fun pageOf(i: Int, n: Int) = t(
        "$i of $n",
        "$i de $n",
        "$i de $n",
        "$i von $n",
        "$i sur $n",
        "$i di $n",
        "$i van $n",
        "$i z $n",
        "$i из $n",
        "$i / $n",
        "$i dari $n",
        "${i}/${n}",
        "${i}/${n}",
    )

    // --- 18. Notificacion --------------------------------------------------------------------

    val reminderTitle = t(
        "A moment to go over the day",
        "Un momento para repasar el día",
        "Um momento para repassar o dia",
        "Ein Moment, um den Tag durchzugehen",
        "Un moment pour relire ta journée",
        "Un momento per rivedere la giornata",
        "Een moment om de dag door te nemen",
        "Chwila na przejrzenie dnia",
        "Время просмотреть день",
        "Günü gözden geçirmek için bir an",
        "Sejenak untuk meninjau hari",
        "1日を見直す時間です",
        "하루를 살펴볼 시간이에요",
    )
    val reminderBody = t(
        "Read today back and decide what comes next.",
        "Relee lo de hoy y decide qué sigue.",
        "Releia o dia de hoje e decida o que vem depois.",
        "Lies den heutigen Tag nach und entscheide, wie es weitergeht.",
        "Relis ta journée et décide de la suite.",
        "Rileggi quello di oggi e decidi cosa viene dopo.",
        "Lees vandaag terug en bepaal wat er daarna komt.",
        "Przeczytaj ponownie dzisiejszy dzień i zdecyduj, co dalej.",
        "Перечитай сегодняшний день и реши, что дальше.",
        "Bugünü tekrar oku ve sıradakine karar ver.",
        "Baca ulang hari ini dan putuskan apa selanjutnya.",
        "今日書いたものを読み返して、次にどうするか決めてください。",
        "오늘을 다시 읽고 다음에 할 일을 정해 보세요.",
    )
    val reminderChannel = t(
        "Reminder to go over the day",
        "Recordatorio para repasar el día",
        "Lembrete para repassar o dia",
        "Erinnerung, den Tag durchzugehen",
        "Rappel pour relire ta journée",
        "Promemoria per rivedere la giornata",
        "Herinnering om de dag door te nemen",
        "Przypomnienie, by przejrzeć dzień",
        "Напоминание просмотреть день",
        "Günü gözden geçirme hatırlatıcısı",
        "Pengingat untuk meninjau hari",
        "1日を見直すリマインダー",
        "하루 살펴보기 알림",
    )

    // --- 19. Widgets -------------------------------------------------------------------------

    fun widgetDate(d: LocalDate): String {
        val w = weekdayShort()[d.dayOfWeek.ordinal]
        val m = monthShort()[d.month.ordinal].removeSuffix(".")
        val n = d.day
        return caps(
            t(
                "$w $n $m", "$w $n $m", "$w $n $m", "$w $n $m", "$w $n $m",
                "$w $n $m", "$w $n $m", "$w $n $m", "$w $n $m", "$w $n $m", "$w $n $m", "$m${n}日($w)", "$m ${n}일 ($w)",
            ),
        )
    }

    fun widgetWeekday(d: LocalDate): String = weekdayNames()[d.dayOfWeek.ordinal]
    fun widgetMonth(d: LocalDate): String = monthNames()[d.month.ordinal]
    fun widgetMonthName(month: YearMonth): String = monthName(month)

    fun widgetOpen(n: Int) = word(n, t(
        "open",
        "abierta|abiertas",
        "aberta|abertas",
        "offen",
        "ouverte|ouvertes",
        "aperta|aperte",
        "open",
        "otwarte|otwarte|otwartych",
        "открыта|открыты|открыто",
        "açık",
        "terbuka",
        "未完了",
        "미완료",
    ))

    fun widgetDone(n: Int) = word(n, t(
        "done",
        "hecha|hechas",
        "feita|feitas",
        "erledigt",
        "faite|faites",
        "fatta|fatte",
        "voltooid",
        "zrobione|zrobione|zrobionych",
        "выполнена|выполнены|выполнено",
        "yapıldı",
        "selesai",
        "完了",
        "완료",
    ))

    fun widgetEvents(n: Int) = word(n, t(
        "event|events",
        "evento|eventos",
        "evento|eventos",
        "Ereignis|Ereignisse",
        "événement|événements",
        "evento|eventi",
        "gebeurtenis|gebeurtenissen",
        "wydarzenie|wydarzenia|wydarzeń",
        "событие|события|событий",
        "etkinlik",
        "acara",
        "イベント",
        "일정",
    ))

    val widgetReview = t(
        "To review",
        "Por revisar",
        "Para revisar",
        "Durchsehen",
        "À revoir",
        "Da rivedere",
        "Door te nemen",
        "Do przejrzenia",
        "Нужно просмотреть",
        "Gözden geçirilecek",
        "Perlu ditinjau",
        "見直し待ち",
        "살펴볼 것",
    )
    val widgetUnlock = t(
        "Tap to turn it on",
        "Toca para activarlo",
        "Toque para ativar",
        "Tippen zum Aktivieren",
        "Touche pour l'activer",
        "Tocca per attivarlo",
        "Tik om in te schakelen",
        "Dotknij, aby włączyć",
        "Нажми, чтобы включить",
        "Açmak için dokun",
        "Ketuk untuk mengaktifkan",
        "タップしてオンにする",
        "탭해서 켜기",
    )
    val pickerTodayName = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
    val pickerTodayDescription = t(
        "Today's open, done and events, without any text",
        "Abiertas, hechas y eventos de hoy, sin ningún texto",
        "Abertas, feitas e eventos de hoje, sem nenhum texto",
        "Offene und erledigte Aufgaben und Ereignisse von heute, ohne Text",
        "Ouvertes, faites et événements du jour, sans aucun texte",
        "Aperte, fatte ed eventi di oggi, senza alcun testo",
        "Open en voltooide taken en gebeurtenissen van vandaag, zonder tekst",
        "Dzisiejsze otwarte i zrobione zadania oraz wydarzenia, bez tekstu",
        "Открытые и выполненные задачи и события на сегодня, без текста",
        "Bugünün açık görevleri, yapılanlar ve etkinlikler, hiç metin olmadan",
        "Tugas terbuka, selesai, dan acara hari ini, tanpa teks apa pun",
        "今日の未完了、完了、イベント。テキストは表示しません",
        "오늘의 미완료, 완료, 일정을 글 없이 보여 줘요",
    )
    val pickerMonthName = t("The month", "El mes", "O mês", "Der Monat", "Le mois", "Il mese", "De maand", "Miesiąc", "Месяц", "Ay", "Bulan", "月", "월간")
    val pickerMonthDescription = t(
        "One dot for each day with entries",
        "Un punto por cada día con entradas",
        "Um ponto para cada dia com entradas",
        "Ein Punkt für jeden Tag mit Einträgen",
        "Un point pour chaque jour avec des entrées",
        "Un punto per ogni giorno con voci",
        "Een stip voor elke dag met items",
        "Jedna kropka na każdy dzień z wpisami",
        "Одна точка на каждый день с записями",
        "Kaydı olan her gün için bir nokta",
        "Satu titik untuk setiap hari yang punya entri",
        "項目がある日に点がひとつ",
        "항목이 있는 날마다 점 하나",
    )
    val pickerLockName = t(
        "Open tasks",
        "Tareas abiertas",
        "Tarefas abertas",
        "Offene Aufgaben",
        "Tâches ouvertes",
        "Attività aperte",
        "Open taken",
        "Otwarte zadania",
        "Открытые задачи",
        "Açık görevler",
        "Tugas terbuka",
        "未完了のタスク",
        "미완료 할 일",
    )
    val pickerLockDescription = t(
        "Today's open tasks on the lock screen",
        "Las tareas abiertas de hoy en la pantalla de bloqueo",
        "As tarefas abertas de hoje na tela de bloqueio",
        "Die offenen Aufgaben von heute auf dem Sperrbildschirm",
        "Les tâches ouvertes du jour sur l'écran verrouillé",
        "Le attività aperte di oggi sulla schermata di blocco",
        "De open taken van vandaag op het vergrendelscherm",
        "Dzisiejsze otwarte zadania na ekranie blokady",
        "Открытые задачи на сегодня на экране блокировки",
        "Bugünün açık görevleri kilit ekranında",
        "Tugas terbuka hari ini di layar kunci",
        "今日の未完了のタスクをロック画面に表示",
        "잠금 화면에 오늘의 미완료 할 일",
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
        return when (lang) {
            "de" -> name
            "tr" -> name.replace('İ', 'i').replace('I', 'ı').lowercase()
            else -> name.lowercase()
        }
    }

    val a11yComplete = t(
        "Complete",
        "Completar",
        "Concluir",
        "Erledigen",
        "Terminer",
        "Completa",
        "Voltooien",
        "Oznacz jako zrobione",
        "Выполнить",
        "Tamamla",
        "Selesaikan",
        "完了にする",
        "완료하기",
    )
    val a11yReopen = t(
        "Reopen",
        "Reabrir",
        "Reabrir",
        "Wieder öffnen",
        "Rouvrir",
        "Riapri",
        "Heropenen",
        "Otwórz ponownie",
        "Открыть снова",
        "Yeniden aç",
        "Buka lagi",
        "未完了に戻す",
        "다시 열기",
    )
    val a11yOptions = t(
        "See options",
        "Ver opciones",
        "Ver opções",
        "Optionen ansehen",
        "Voir les options",
        "Vedi opzioni",
        "Opties bekijken",
        "Zobacz opcje",
        "Показать варианты",
        "Seçenekleri gör",
        "Lihat pilihan",
        "操作を表示",
        "옵션 보기",
    )
    val a11yMoveUp = t("Move up", "Subir", "Subir", "Nach oben", "Monter", "Sposta su", "Omhoog", "W górę", "Переместить выше", "Yukarı taşı", "Pindah ke atas", "上へ移動", "위로 옮기기")
    val a11yMoveDown = t(
        "Move down",
        "Bajar",
        "Descer",
        "Nach unten",
        "Descendre",
        "Sposta giù",
        "Omlaag",
        "W dół",
        "Переместить ниже",
        "Aşağı taşı",
        "Pindah ke bawah",
        "下へ移動",
        "아래로 옮기기",
    )

    fun a11yDayRow(day: Int, weekday: String, n: Int): String {
        val entries = if (n == 0) {
            t(
                "no entries",
                "sin entradas",
                "sem entradas",
                "keine Einträge",
                "aucune entrée",
                "nessuna voce",
                "geen items",
                "brak wpisów",
                "нет записей",
                "kayıt yok",
                "tanpa entri",
                "項目なし",
                "항목 없음",
            )
        } else {
            entryCount(n)
        }
        return t(
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day., $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "$day, $weekday, $entries",
            "${day}日、${weekday}、${entries}",
            "${day}일, ${weekday}, ${entries}",
        )
    }

    fun a11yTrackerCell(row: String, date: LocalDate, marked: Boolean): String {
        val when2 = shortDate(date)
        val state = if (marked) {
            t("marked", "marcada", "marcada", "markiert", "cochée", "segnata", "gemarkeerd", "oznaczone", "отмечено", "işaretli", "ditandai", "印あり", "표시됨")
        } else {
            t(
                "not marked",
                "sin marcar",
                "sem marcar",
                "nicht markiert",
                "pas cochée",
                "non segnata",
                "niet gemarkeerd",
                "nieoznaczone",
                "не отмечено",
                "işaretli değil",
                "belum ditandai",
                "印なし",
                "표시 안 됨",
            )
        }
        return "$row, $when2, $state"
    }

    fun a11yTrackerMarked(days: List<Int>): String {
        if (days.isEmpty()) return t(
            "No day marked",
            "Ningún día marcado",
            "Nenhum dia marcado",
            "Kein Tag markiert",
            "Aucun jour coché",
            "Nessun giorno segnato",
            "Geen dag gemarkeerd",
            "Żaden dzień nie jest oznaczony",
            "Ни один день не отмечен",
            "Hiçbir gün işaretli değil",
            "Tidak ada hari yang ditandai",
            "印の付いた日はありません",
            "표시한 날이 없어요",
        )
        val list = joinAnd(days.map { it.toString() })
        return t(
            "Marked: $list",
            "Marcados: $list",
            "Marcados: $list",
            "Markiert: $list",
            "Cochés : $list",
            "Segnati: $list",
            "Gemarkeerd: $list",
            "Oznaczone: $list",
            "Отмечены: $list",
            "İşaretli: $list",
            "Ditandai: $list",
            "印の付いた日: ${list}",
            "표시한 날: ${list}",
        )
    }

    // --- 23. v1.1 -----------------------------------------------------------------------------

    val continueCollection = t(
        "Continue in a new collection",
        "Continuar en una colección nueva",
        "Continuar em uma nova coleção",
        "In einer neuen Sammlung fortsetzen",
        "Continuer dans une nouvelle collection",
        "Continua in una nuova raccolta",
        "Voortzetten in een nieuwe collectie",
        "Kontynuuj w nowej kolekcji",
        "Продолжить в новой коллекции",
        "Yeni bir koleksiyonda devam et",
        "Lanjutkan di koleksi baru",
        "新しいコレクションで続ける",
        "새 목록에서 이어 쓰기",
    )
    val continueTitle = t(
        "Continue in...",
        "Continuar en...",
        "Continuar em...",
        "Fortsetzen in...",
        "Continuer dans...",
        "Continua in...",
        "Voortzetten in...",
        "Kontynuuj w...",
        "Продолжить в...",
        "Şurada devam et...",
        "Lanjutkan di...",
        "続ける先...",
        "이어 쓸 곳...",
    )
    val create = t("Create", "Crear", "Criar", "Erstellen", "Créer", "Crea", "Maken", "Utwórz", "Создать", "Oluştur", "Buat", "作成", "만들기")
    fun threadFrom(title: String) = t(
        "Continued from $title",
        "Viene de $title",
        "Vem de $title",
        "Fortsetzung von $title",
        "Suite de $title",
        "Continuazione di $title",
        "Voortgezet van $title",
        "Ciąg dalszy z: $title",
        "Предыдущая часть: $title",
        "Önceki: $title",
        "Lanjutan dari $title",
        "「${title}」から続く",
        "이어 온 곳: ${title}",
    )
    fun threadNext(title: String) = t(
        "Continues in $title",
        "Sigue en $title",
        "Continua em $title",
        "Weiter in $title",
        "Continue dans $title",
        "Prosegue in $title",
        "Wordt voortgezet in $title",
        "Ciąg dalszy w: $title",
        "Следующая часть: $title",
        "Devamı: $title",
        "Berlanjut di $title",
        "「${title}」に続く",
        "이어지는 곳: ${title}",
    )
    val newNotebookRow = t(
        "New notebook",
        "Nuevo cuaderno",
        "Novo caderno",
        "Neues Notizbuch",
        "Nouveau carnet",
        "Nuovo quaderno",
        "Nieuw notitieboek",
        "Nowy notatnik",
        "Новый блокнот",
        "Yeni defter",
        "Buku catatan baru",
        "新しいノート",
        "새 노트",
    )
    val newNotebookSub = t(
        "Go through collections and tasks to start another",
        "Repasa colecciones y tareas para empezar otro",
        "Repasse coleções e tarefas para começar outro",
        "Geh Sammlungen und Aufgaben durch, um ein neues zu beginnen",
        "Passe en revue collections et tâches pour en commencer un autre",
        "Rivedi raccolte e attività per iniziarne un altro",
        "Neem collecties en taken door om een nieuw notitieboek te beginnen",
        "Przejrzyj kolekcje i zadania, aby zacząć kolejny",
        "Просмотри коллекции и задачи, чтобы начать новый",
        "Bir defter daha başlatmak için koleksiyonları ve görevleri gözden geçir",
        "Tinjau koleksi dan tugas untuk memulai yang baru",
        "コレクションとタスクを順に見直して、新しいノートを始めます",
        "목록과 할 일을 하나씩 살펴보며 새로 시작해요",
    )
    val notebookKeep = t("Keep", "Mantener", "Manter", "Behalten", "Garder", "Mantieni", "Houden", "Zachowaj", "Оставить", "Koru", "Pertahankan", "残す", "유지")
    val notebookContinue = t(
        "Continue in a new one",
        "Continuar en una nueva",
        "Continuar em uma nova",
        "In einer neuen fortsetzen",
        "Continuer dans une nouvelle",
        "Continua in una nuova",
        "Voortzetten in een nieuwe",
        "Kontynuuj w nowej",
        "Продолжить в новой",
        "Yenisinde devam et",
        "Lanjutkan di yang baru",
        "新しいノートで続ける",
        "새 노트로 이어 가기",
    )
    val notebookArchive = t(
        "Archive",
        "Archivar",
        "Arquivar",
        "Archivieren",
        "Archiver",
        "Archivia",
        "Archiveren",
        "Archiwizuj",
        "Архивировать",
        "Arşivle",
        "Arsipkan",
        "アーカイブ",
        "보관",
    )

    fun notebookDone(d: LocalDate): String {
        val date = dayMonthYear(d)
        return t(
            "New notebook from $date.",
            "Cuaderno nuevo desde el $date.",
            "Caderno novo desde $date.",
            "Neues Notizbuch seit dem $date.",
            "Nouveau carnet depuis le $date.",
            "Nuovo quaderno ${if (date.startsWith("8 ") || date.startsWith("11 ")) "dall'" else "dal "}$date.",
            "Nieuw notitieboek sinds $date.",
            "Nowy notatnik od $date.",
            "Новый блокнот с $date.",
            "Yeni defter, $date tarihinden itibaren.",
            "Buku catatan baru sejak $date.",
            "${date}から新しいノートです。",
            "${date}부터 새 노트예요.",
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
            "Quaderno fino ${if (date.startsWith("8 ") || date.startsWith("11 ")) "all'" else "al "}$date",
            "Notitieboek tot $date",
            "Notatnik do $date",
            "Блокнот до $date",
            "$date tarihine kadar defter",
            "Buku catatan sampai $date",
            "${date}までのノート",
            "${date}까지의 노트",
        )
    }

    /** Uses abbrDate: "14 mar"; in English, "Mar 14" (docs/textos.md 23). */
    fun scheduleFor(d: LocalDate): String {
        val date = abbrDate(d)
        return t(
            "Schedule for $date",
            "Programar para el $date",
            "Agendar para $date",
            "Für $date einplanen",
            "Planifier pour le $date",
            "Programma per ${if (date.startsWith("8 ") || date.startsWith("11 ")) "l'" else "il "}$date",
            "Inplannen voor $date",
            "Zaplanuj na $date",
            "Запланировать на $date",
            "$date için planla",
            "Jadwalkan untuk $date",
            "${date}にスケジュール",
            "예정일: ${date}",
        )
    }

    val tileLabel = t(
        "Note in Bobbin",
        "Anotar en Bobbin",
        "Anotar no Bobbin",
        "In Bobbin notieren",
        "Noter dans Bobbin",
        "Annota in Bobbin",
        "Noteren in Bobbin",
        "Zapisz w Bobbin",
        "Записать в Bobbin",
        "Bobbin'e not al",
        "Catat di Bobbin",
        "Bobbinにメモ",
        "Bobbin에 적기",
    )
    val captureSaved = t(
        "Noted in Bobbin.",
        "Anotado en Bobbin.",
        "Anotado no Bobbin.",
        "In Bobbin notiert.",
        "Noté dans Bobbin.",
        "Annotato in Bobbin.",
        "Genoteerd in Bobbin.",
        "Zapisano w Bobbin.",
        "Записано в Bobbin.",
        "Bobbin'e not alındı.",
        "Tercatat di Bobbin.",
        "Bobbinにメモしました。",
        "Bobbin에 적었어요.",
    )
    val captureEmpty = t(
        "There was nothing to note.",
        "No había nada que anotar.",
        "Não havia nada para anotar.",
        "Es gab nichts zu notieren.",
        "Il n'y avait rien à noter.",
        "Non c'era niente da annotare.",
        "Er was niets om te noteren.",
        "Nie było nic do zapisania.",
        "Записывать было нечего.",
        "Not alınacak bir şey yoktu.",
        "Tidak ada yang perlu dicatat.",
        "メモするものがありませんでした。",
        "적을 내용이 없었어요.",
    )
    val captureNeedsPro = t(
        "Noting from outside the app is part of Bobbin Pro. Open the app to see it.",
        "Anotar desde fuera de la app es de Bobbin Pro. Abre la app para verlo.",
        "Anotar de fora do app é do Bobbin Pro. Abra o app para ver.",
        "Von außerhalb der App notieren gehört zu Bobbin Pro. Öffne die App, um es zu sehen.",
        "Noter depuis l'extérieur de l'app fait partie de Bobbin Pro. Ouvre l'app pour le voir.",
        "Annotare dall'esterno dell'app fa parte di Bobbin Pro. Apri l'app per vederlo.",
        "Noteren buiten de app hoort bij Bobbin Pro. Open de app om het te zien.",
        "Zapisywanie spoza aplikacji jest częścią Bobbin Pro. Otwórz aplikację, aby to zobaczyć.",
        "Запись вне приложения входит в Bobbin Pro. Открой приложение, чтобы посмотреть.",
        "Uygulamanın dışından not almak bir Bobbin Pro özelliğidir. Görmek için uygulamayı aç.",
        "Mencatat dari luar aplikasi adalah bagian dari Bobbin Pro. Buka aplikasinya untuk melihatnya.",
        "アプリの外からメモするのはBobbin Proの機能です。アプリを開いて確認してください。",
        "앱 밖에서 적는 기능은 Bobbin Pro에 포함돼요. 앱을 열어서 확인해 보세요.",
    )
    val proBook = t(
        "The book as a PDF",
        "El libro en PDF",
        "O livro em PDF",
        "Das Buch als PDF",
        "Le livre en PDF",
        "Il libro in PDF",
        "Het boek als PDF",
        "Książka w PDF",
        "Книга в PDF",
        "PDF olarak kitap",
        "Buku sebagai PDF",
        "PDFの本",
        "PDF 책",
    )

    fun proCapture(onIos: Boolean): String = if (onIos) {
        t(
            "Note with Siri and Shortcuts",
            "Anotar desde Siri y Atajos",
            "Anotar pela Siri e Atalhos",
            "Mit Siri und Kurzbefehlen notieren",
            "Noter avec Siri et Raccourcis",
            "Annota con Siri e Comandi rapidi",
            "Noteren met Siri en Opdrachten",
            "Zapisywanie przez Siri i Skróty",
            "Записывать через Siri и Команды",
            "Siri ve Kısayollar ile not al",
            "Catat lewat Siri dan Pintasan",
            "Siriとショートカットでメモ",
            "Siri와 단축어로 적기",
        )
    } else {
        t(
            "Note from Quick Settings",
            "Anotar desde Ajustes rápidos",
            "Anotar pelas Configurações rápidas",
            "Aus den Schnelleinstellungen notieren",
            "Noter depuis les réglages rapides",
            "Annota dalle Impostazioni rapide",
            "Noteren via Snelle instellingen",
            "Zapisywanie z Szybkich ustawień",
            "Записывать из быстрых настроек",
            "Hızlı Ayarlar'dan not al",
            "Catat dari Setelan Cepat",
            "クイック設定からメモ",
            "빠른 설정에서 적기",
        )
    }

    val bookRow = t(
        "Book as PDF",
        "Libro en PDF",
        "Livro em PDF",
        "Buch als PDF",
        "Livre en PDF",
        "Libro in PDF",
        "Boek als PDF",
        "Książka w PDF",
        "Книга в PDF",
        "PDF olarak kitap",
        "Buku PDF",
        "PDFの本",
        "PDF 책",
    )
    val bookSub = t(
        "The notebook laid out, to print or keep",
        "El cuaderno maquetado, para imprimir o guardar",
        "O caderno diagramado, para imprimir ou guardar",
        "Das Notizbuch gesetzt, zum Drucken oder Aufbewahren",
        "Le carnet mis en page, à imprimer ou à garder",
        "Il quaderno impaginato, da stampare o conservare",
        "Het notitieboek opgemaakt, om af te drukken of te bewaren",
        "Notatnik w gotowym układzie, do wydrukowania lub zachowania",
        "Блокнот в готовой вёрстке, чтобы распечатать или сохранить",
        "Basılacak ya da saklanacak şekilde dizilmiş defter",
        "Buku catatan yang sudah ditata, untuk dicetak atau disimpan",
        "レイアウトしたノート。印刷や保存に",
        "인쇄하거나 보관할 수 있게 편집한 노트",
    )
    val bookAction = t(
        "Make the book as a PDF",
        "Hacer el libro en PDF",
        "Fazer o livro em PDF",
        "Das Buch als PDF erstellen",
        "Faire le livre en PDF",
        "Crea il libro in PDF",
        "Het boek als PDF maken",
        "Utwórz książkę w PDF",
        "Создать книгу в PDF",
        "Kitabı PDF olarak oluştur",
        "Buat buku sebagai PDF",
        "本をPDFで作成",
        "PDF 책 만들기",
    )

    fun bookRange(from: YearMonth, to: YearMonth): String {
        val a = monthYear(from)
        val b = monthYear(to)
        return t(
            "$a to $b",
            "$a a $b",
            "$a a $b",
            "$a bis $b",
            "$a à $b",
            "Da $a ${if (b.startsWith("a")) "ad" else "a"} $b",
            "$a tot $b",
            "$a - $b",
            "$a - $b",
            "$a - $b",
            "$a sampai $b",
            "${a}から${b}まで",
            "${a}부터 ${b}까지",
        )
    }

    fun bookMaking(n: Int, total: Int): String {
        return t(
            "Laying out the book: page $n of $total",
            "Maquetando el libro: página $n de $total",
            "Diagramando o livro: página $n de $total",
            "Buch wird gesetzt: Seite $n von $total",
            "Mise en page du livre : page $n sur $total",
            "Impaginazione del libro: pagina $n di $total",
            "Boek opmaken: pagina $n van $total",
            "Układanie książki: strona $n z $total",
            "Вёрстка книги: страница $n из $total",
            "Kitap diziliyor: sayfa $n / $total",
            "Menata buku: halaman $n dari $total",
            "本をレイアウト中: ${n}/${total}ページ",
            "책을 편집하는 중: ${total}쪽 중 ${n}쪽",
        )
    }

    val bookFailed = t(
        "Couldn't create the book.",
        "No se ha podido crear el libro.",
        "Não foi possível criar o livro.",
        "Das Buch konnte nicht erstellt werden.",
        "Impossible de créer le livre.",
        "Impossibile creare il libro.",
        "Het boek kon niet worden gemaakt.",
        "Nie udało się utworzyć książki.",
        "Не удалось создать книгу.",
        "Kitap oluşturulamadı.",
        "Gagal membuat buku.",
        "本を作成できませんでした。",
        "책을 만들지 못했어요.",
    )

    // Natural language dates (v1.1, docs/tecnico.md 12.4): the single-word vocabulary that
    // model/DateHint.kt reads; the multi-word recognition patterns of docs/textos.md 23 stay in
    // that document for DateHint.kt (#66) to consult directly.
    fun dateWordToday(code: String = lang) = tIn(code, "today", "hoy", "hoje", "heute", "aujourd'hui", "oggi", "vandaag", "dzisiaj", "сегодня", "bugün", "hari ini", "今日", "오늘")
    fun dateWordTomorrow(code: String = lang) = tIn(code, "tomorrow", "mañana", "amanhã", "morgen", "demain", "domani", "morgen", "jutro", "завтра", "yarın", "besok", "明日", "내일")
    fun dateWordDayAfterTomorrow(code: String = lang) = tIn(
        code,
        "day after tomorrow",
        "pasado mañana",
        "depois de amanhã",
        "übermorgen",
        "après-demain",
        "dopodomani",
        "overmorgen",
        "pojutrze",
        "послезавтра",
        "öbür gün",
        "lusa",
        "明後日",
        "모레",
    )

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

    private val QUESTIONS_IT = listOf(
        "Che cosa non migreresti se dovessi riscriverlo a mano?",
        "Quale attività aspetta da più tempo?",
        "Che cosa hai fatto che non hai mai annotato?",
        "Quale evento di questi giorni vuoi ricordare?",
        "Quale nota ti ha sorpreso rileggendola?",
        "Che cosa depenneresti oggi senza rimpianti?",
        "Che cosa è valso il tempo che ci hai dedicato?",
        "A che cosa hai detto di sì e ora diresti di no?",
        "Quale attività si è rivelata più piccola di quanto pensavi?",
        "Qual è la prima cosa che farai domani?",
        "Che cosa merita una raccolta tutta sua?",
        "Quale idea torna di continuo?",
        "Chi compare di più in queste pagine?",
        "Che cosa hai finito che avevi iniziato tempo fa?",
        "Che cosa faresti in modo diverso con gli stessi giorni?",
        "Che cosa hai imparato che vuoi conservare?",
        "Quale attività è in realtà più di una?",
        "Che cosa può aspettare?",
        "Che cosa è andato meglio di come ti aspettavi?",
        "Che cosa ti fa piacere aver annotato?",
        "Che cosa hai notato solo rileggendo?",
        "Che cosa hai lasciato a metà di proposito?",
        "Quale luogo ricordi di questi giorni?",
        "Quale conversazione annoteresti per intero?",
        "Quale piccola cosa ha reso migliore un giorno?",
        "Quale attività continui a migrare, e perché?",
        "Per che cosa vorresti avere più tempo?",
        "Che cosa toglieresti dalla tua lista se nessuno te lo chiedesse?",
        "Che cosa hai fatto per un'altra persona?",
        "Che cosa ha fatto qualcuno per te?",
        "Che cosa vuoi ripetere?",
        "Qual è stato il momento ordinario più bello?",
        "Quale domanda è ancora aperta?",
        "Che cosa hai letto, visto o sentito che merita una nota?",
        "Che cosa hai deciso, e che cosa ne è venuto fuori?",
        "Che cosa hai iniziato senza pianificarlo?",
        "Quale attività daresti a un'altra persona?",
        "Che cosa hai rimandato e alla fine è andato bene?",
        "Che cosa manca in queste pagine?",
        "Che cosa esploreresti in un pomeriggio libero?",
        "Quale priorità è cambiata lungo il cammino?",
        "Che cosa hai aggiustato, in casa o altrove?",
        "Quale giorno rileggeresti per primo?",
        "Che cosa hai comprato che ne valeva la pena?",
        "Che cosa hai cucinato o mangiato che vale la pena ricordare?",
        "Quale abitudine è comparsa senza che te ne accorgessi?",
        "Che cosa renderebbe più semplici i prossimi giorni?",
        "A che cosa hai detto di no, e com'è andata?",
        "Quale attività potresti fare in cinque minuti?",
        "Che cosa ti ha sorpreso di come hai passato il tempo?",
        "Che cosa vuoi ricordare di questi giorni tra un anno?",
        "Che cosa è stato più difficile di quanto sembrava?",
        "Quale posto hai visitato per la prima volta?",
        "Che cosa hai fatto con le mani?",
        "Quale piano è cambiato, e che cosa lo ha sostituito?",
        "Quale notizia ha segnato questi giorni?",
        "Che cosa puoi mettere da parte?",
        "Che cosa aspettavi, ed è arrivato?",
        "Che cosa scriveresti sulla prima riga della pagina successiva?",
        "Come riassumeresti questi giorni in una riga?",
    )

    private val QUESTIONS_NL = listOf(
        "Wat zou je niet verplaatsen als je het met de hand opnieuw moest schrijven?",
        "Welke taak wacht al het langst?",
        "Wat heb je gedaan zonder het ooit op te schrijven?",
        "Welke gebeurtenis van deze dagen wil je onthouden?",
        "Welke notitie verraste je bij het teruglezen?",
        "Wat zou je vandaag zonder spijt schrappen?",
        "Wat was de tijd waard die je eraan besteedde?",
        "Waar zei je ja tegen, en zou je nu nee zeggen?",
        "Welke taak bleek kleiner dan je dacht?",
        "Wat ga je morgen als eerste doen?",
        "Wat verdient een eigen collectie?",
        "Welk idee komt steeds terug?",
        "Wie komt het vaakst voor op deze pagina's?",
        "Wat heb je afgerond dat je al lang geleden begon?",
        "Wat zou je anders doen met dezelfde dagen?",
        "Wat heb je geleerd dat je wilt bewaren?",
        "Welke taak is eigenlijk meerdere taken?",
        "Wat kan wachten?",
        "Wat ging beter dan je verwachtte?",
        "Wat ben je blij te hebben opgeschreven?",
        "Wat viel je pas op bij het teruglezen?",
        "Wat heb je met opzet onafgemaakt gelaten?",
        "Welke plek herinner je je van deze dagen?",
        "Welk gesprek zou je helemaal opschrijven?",
        "Welk klein ding maakte een dag beter?",
        "Welke taak blijf je verplaatsen, en waarom?",
        "Waarvoor zou je meer tijd willen hebben?",
        "Wat zou je van je lijst halen als niemand erom vroeg?",
        "Wat heb je voor iemand anders gedaan?",
        "Wat heeft iemand voor jou gedaan?",
        "Wat wil je herhalen?",
        "Wat was het mooiste gewone moment?",
        "Welke vraag staat nog open?",
        "Wat heb je gelezen, gezien of gehoord dat een notitie verdient?",
        "Wat heb je besloten, en wat is ervan gekomen?",
        "Wat ben je begonnen zonder het te plannen?",
        "Welke taak zou je aan iemand anders geven?",
        "Wat heb je uitgesteld dat uiteindelijk goed afliep?",
        "Wat ontbreekt er op deze pagina's?",
        "Wat zou je verkennen op een vrije middag?",
        "Welke prioriteit is onderweg veranderd?",
        "Wat heb je gerepareerd, thuis of elders?",
        "Welke dag zou je als eerste teruglezen?",
        "Wat heb je gekocht dat het waard was?",
        "Wat heb je gekookt of gegeten dat het onthouden waard is?",
        "Welke gewoonte is er ongemerkt bijgekomen?",
        "Wat zou de komende dagen eenvoudiger maken?",
        "Waar zei je nee tegen, en hoe ging dat?",
        "Welke taak zou je in vijf minuten kunnen doen?",
        "Wat verraste je aan hoe je je tijd hebt doorgebracht?",
        "Wat wil je over een jaar nog van deze dagen onthouden?",
        "Wat was moeilijker dan het leek?",
        "Waar ben je voor het eerst geweest?",
        "Wat heb je met je handen gemaakt?",
        "Welk plan veranderde, en wat kwam ervoor in de plaats?",
        "Welk nieuws markeerde deze dagen?",
        "Wat kun je opzijzetten?",
        "Waar wachtte je op, en is het gekomen?",
        "Wat zou je op de eerste regel van de volgende pagina schrijven?",
        "Hoe zou je deze dagen in één regel samenvatten?",
    )

    private val QUESTIONS_PL = listOf(
        "Czego nie warto przenosić, gdyby trzeba było przepisywać to ręcznie?",
        "Które zadanie czeka najdłużej?",
        "Co zostało zrobione, ale nie zapisane?",
        "Które wydarzenie z tych dni chcesz zapamiętać?",
        "Która notatka zaskoczyła cię po ponownym przeczytaniu?",
        "Co dałoby się dziś skreślić bez żalu?",
        "Co było warte czasu, jaki na to poszło?",
        "Na co była zgoda, a dziś byłaby odmowa?",
        "Które zadanie okazało się mniejsze, niż się wydawało?",
        "Co zrobisz jutro jako pierwsze?",
        "Co zasługuje na własną kolekcję?",
        "Jaki pomysł wciąż wraca?",
        "Kto pojawia się na tych stronach najczęściej?",
        "Co udało się dokończyć z tego, co zaczęte dawno temu?",
        "Co dałoby się zrobić inaczej w tych samych dniach?",
        "Czego udało się nauczyć, co chcesz zachować?",
        "Które zadanie to w rzeczywistości kilka zadań?",
        "Co może poczekać?",
        "Co poszło lepiej, niż oczekiwano?",
        "Co dobrze, że zapisano?",
        "Co rzuciło się w oczy dopiero przy ponownym czytaniu?",
        "Co zostało celowo niedokończone?",
        "Jakie miejsce pamiętasz z tych dni?",
        "Którą rozmowę warto zapisać w całości?",
        "Jaka drobnostka poprawiła jakiś dzień?",
        "Które zadanie wciąż przenosisz i dlaczego?",
        "Na co przydałoby się więcej czasu?",
        "Co dałoby się usunąć z listy, gdyby nikt o to nie prosił?",
        "Co zostało zrobione dla kogoś innego?",
        "Co ktoś zrobił dla ciebie?",
        "Co chcesz powtórzyć?",
        "Jaka była najlepsza zwyczajna chwila?",
        "Które pytanie wciąż jest otwarte?",
        "Co przeczytane, obejrzane albo usłyszane zasługuje na notatkę?",
        "Co zostało postanowione i co z tego wyszło?",
        "Co zaczęło się bez planowania?",
        "Które zadanie można by oddać komuś innemu?",
        "Co zostało odłożone, a i tak wyszło dobrze?",
        "Czego brakuje na tych stronach?",
        "Co chcesz zbadać w wolne popołudnie?",
        "Który priorytet zmienił się po drodze?",
        "Co zostało naprawione, w domu lub gdzie indziej?",
        "Który dzień warto przeczytać ponownie jako pierwszy?",
        "Jaki zakup był tego wart?",
        "Co ugotowane albo zjedzone warto zapamiętać?",
        "Jaki nawyk pojawił się niezauważenie?",
        "Co ułatwiłoby najbliższe dni?",
        "Co dostało odmowę i jak to poszło?",
        "Które zadanie da się zrobić w pięć minut?",
        "Co cię zaskoczyło w tym, na co poszedł twój czas?",
        "Co chcesz pamiętać z tych dni za rok?",
        "Co było trudniejsze, niż wyglądało?",
        "Które miejsce odwiedzono po raz pierwszy?",
        "Co powstało twoimi rękami?",
        "Który plan się zmienił i co go zastąpiło?",
        "Jaka wiadomość naznaczyła te dni?",
        "Co możesz odłożyć na bok?",
        "Na co czekano i czy to nadeszło?",
        "Co mogłoby stać w pierwszej linijce następnej strony?",
        "Jak podsumować te dni jedną linijką?",
    )

    private val QUESTIONS_RU = listOf(
        "Что не стоило бы переносить, если бы пришлось переписывать это от руки?",
        "Какая задача ждёт дольше всех?",
        "Что было сделано, но так и не записано?",
        "Какое событие этих дней хочется запомнить?",
        "Какая заметка удивила тебя при перечитывании?",
        "Что сегодня можно вычеркнуть без сожаления?",
        "Что стоило потраченного времени?",
        "На что было сказано \"да\", а сейчас прозвучало бы \"нет\"?",
        "Какая задача оказалась меньше, чем казалось?",
        "Что ты сделаешь завтра в первую очередь?",
        "Что заслуживает отдельной коллекции?",
        "Какая идея возвращается снова и снова?",
        "Кто чаще всего появляется на этих страницах?",
        "Что удалось закончить из давно начатого?",
        "Что стоило бы сделать иначе в те же дни?",
        "Что из узнанного хочется сохранить?",
        "Какая задача на самом деле состоит из нескольких?",
        "Что может подождать?",
        "Что получилось лучше, чем ожидалось?",
        "О какой записи приятно, что она есть?",
        "Что бросилось в глаза только при перечитывании?",
        "Что намеренно оставлено недоделанным?",
        "Какое место запомнилось за эти дни?",
        "Какой разговор стоило бы записать целиком?",
        "Какая мелочь сделала день лучше?",
        "Какую задачу ты всё время переносишь, и почему?",
        "Для чего хотелось бы иметь больше времени?",
        "Что можно убрать из списка, если никто этого не требует?",
        "Что было сделано для другого человека?",
        "Что для тебя сделал кто-то другой?",
        "Что хочется повторить?",
        "Каким был лучший обычный момент?",
        "Какой вопрос всё ещё открыт?",
        "Что из прочитанного, увиденного или услышанного заслуживает заметки?",
        "Что было решено и что из этого вышло?",
        "Что началось без всякого плана?",
        "Какую задачу можно передать кому-то другому?",
        "Что откладывалось, но в итоге обошлось хорошо?",
        "Чего не хватает на этих страницах?",
        "Что хотелось бы исследовать свободным днём?",
        "Какой приоритет изменился по пути?",
        "Что удалось починить, дома или где-то ещё?",
        "Какой день хочется перечитать первым?",
        "Какая покупка оказалась стоящей?",
        "Какое приготовленное или съеденное блюдо стоит запомнить?",
        "Какая привычка появилась незаметно?",
        "Что упростило бы ближайшие дни?",
        "На что было сказано \"нет\" и как всё прошло?",
        "Какую задачу можно сделать за пять минут?",
        "Что удивило в том, на что ушло время?",
        "Что хочется помнить об этих днях через год?",
        "Что оказалось труднее, чем выглядело?",
        "Где довелось побывать впервые?",
        "Что сделано своими руками?",
        "Какой план изменился и что пришло ему на смену?",
        "Какая новость определила эти дни?",
        "Что можно отложить в сторону?",
        "Что ожидалось, и пришло ли оно?",
        "Что написать в первой строке следующей страницы?",
        "Как описать эти дни одной строкой?",
    )

    private val QUESTIONS_TR = listOf(
        "Elle yeniden yazman gerekseydi neyi taşımazdın?",
        "Hangi görev en uzun süredir bekliyor?",
        "Ne yaptın ama bir yere yazmadın?",
        "Bu günlerden hangi etkinliği hatırlamak istiyorsun?",
        "Hangi not tekrar okuyunca seni şaşırttı?",
        "Bugün neyin üstünü pişmanlık duymadan çizerdin?",
        "Hangisi harcadığı zamana değdi?",
        "Neye evet dedin de şimdi hayır derdin?",
        "Hangi görev düşündüğünden daha küçük çıktı?",
        "Yarın ilk olarak ne yapacaksın?",
        "Ne, kendi koleksiyonunu hak ediyor?",
        "Hangi fikir sürekli geri geliyor?",
        "Bu sayfalarda en çok kim geçiyor?",
        "Uzun zaman önce başlayıp neyi bitirdin?",
        "Aynı günlerle neyi farklı yapardın?",
        "Öğrendiklerinden hangisini saklamak istiyorsun?",
        "Hangi görev aslında birkaç görev?",
        "Ne bekleyebilir?",
        "Ne, beklediğinden daha iyi gitti?",
        "Neyi yazdığına sevindin?",
        "Neyi ancak tekrar okurken fark ettin?",
        "Neyi bilerek yarım bıraktın?",
        "Bu günlerden hangi yeri hatırlıyorsun?",
        "Hangi sohbeti baştan sona yazardın?",
        "Hangi küçük şey bir günü daha iyi yaptı?",
        "Hangi görevi sürekli taşıyorsun ve neden?",
        "Ne için daha çok zaman isterdin?",
        "Kimse senden istemeseydi listenden neyi çıkarırdın?",
        "Başkası için ne yaptın?",
        "Biri senin için ne yaptı?",
        "Neyi tekrar etmek istiyorsun?",
        "En güzel sıradan an hangisiydi?",
        "Hangi soru hâlâ açık?",
        "Not almayı hak eden ne okudun, izledin ya da duydun?",
        "Neye karar verdin ve sonunda ne oldu?",
        "Planlamadan neye başladın?",
        "Hangi görevi başkasına verirdin?",
        "Neyi erteledin de sonunda iyi çıktı?",
        "Bu sayfalarda ne eksik?",
        "Boş bir öğleden sonrada neyi keşfederdin?",
        "Hangi öncelik yolda değişti?",
        "Evde ya da dışarıda neyi onardın?",
        "Önce hangi günü tekrar okurdun?",
        "Neyi satın aldın da değdi?",
        "Ne pişirdin ya da yedin de hatırlamaya değer?",
        "Hangi alışkanlık sen fark etmeden ortaya çıktı?",
        "Önümüzdeki günleri ne daha kolay yapardı?",
        "Neye hayır dedin ve nasıl geçti?",
        "Hangi görevi beş dakikada yapabilirsin?",
        "Zamanı nasıl geçirdiğinle ilgili seni ne şaşırttı?",
        "Bu günlerden bir yıl sonra neyi hatırlamak istersin?",
        "Ne göründüğünden daha zordu?",
        "İlk kez nereye gittin?",
        "Elinle ne yaptın?",
        "Hangi plan değişti ve yerine ne geldi?",
        "Hangi haber bu günlere damga vurdu?",
        "Neyi bir kenara bırakabilirsin?",
        "Neyi bekliyordun ve geldi mi?",
        "Bir sonraki sayfanın ilk satırına ne yazardın?",
        "Bu günleri tek satırda nasıl özetlerdin?",
    )

    private val QUESTIONS_ID = listOf(
        "Apa yang tidak akan kamu pindahkan kalau harus menulis ulang semuanya dengan tangan?",
        "Tugas mana yang paling lama menunggu?",
        "Apa yang kamu lakukan tapi tidak sempat kamu catat?",
        "Acara mana dari hari-hari ini yang ingin kamu ingat?",
        "Catatan mana yang mengejutkanmu saat kamu membacanya lagi?",
        "Apa yang akan kamu coret hari ini tanpa menyesal?",
        "Apa yang sepadan dengan waktu yang kamu habiskan?",
        "Apa yang dulu kamu setujui, tapi sekarang akan kamu tolak?",
        "Tugas mana yang ternyata lebih kecil dari dugaanmu?",
        "Apa yang akan kamu kerjakan pertama kali besok?",
        "Apa yang pantas punya koleksi sendiri?",
        "Ide mana yang terus kembali?",
        "Siapa yang paling sering muncul di halaman-halaman ini?",
        "Apa yang sudah lama kamu mulai dan akhirnya kamu selesaikan?",
        "Apa yang akan kamu lakukan secara berbeda dengan hari-hari yang sama?",
        "Apa yang kamu pelajari dan ingin kamu simpan?",
        "Tugas mana yang sebenarnya terdiri dari beberapa tugas?",
        "Apa yang bisa menunggu?",
        "Apa yang berjalan lebih baik dari harapanmu?",
        "Apa yang membuatmu senang sudah kamu catat?",
        "Apa yang baru kamu sadari saat membacanya lagi?",
        "Apa yang sengaja kamu biarkan belum selesai?",
        "Tempat mana yang kamu ingat dari hari-hari ini?",
        "Percakapan mana yang akan kamu tulis utuh?",
        "Hal kecil apa yang membuat satu hari jadi lebih baik?",
        "Tugas mana yang terus kamu pindahkan, dan kenapa?",
        "Untuk apa kamu ingin punya lebih banyak waktu?",
        "Apa yang akan kamu singkirkan dari daftarmu kalau tidak ada yang memintanya?",
        "Apa yang kamu lakukan untuk orang lain?",
        "Apa yang dilakukan seseorang untukmu?",
        "Apa yang ingin kamu ulangi?",
        "Apa momen biasa yang terbaik?",
        "Pertanyaan mana yang masih terbuka?",
        "Apa yang kamu baca, tonton, atau dengar yang layak dicatat?",
        "Apa yang kamu putuskan, dan apa hasilnya?",
        "Apa yang kamu mulai tanpa direncanakan?",
        "Tugas mana yang akan kamu serahkan kepada orang lain?",
        "Apa yang kamu tunda dan ternyata baik-baik saja?",
        "Apa yang kurang di halaman-halaman ini?",
        "Apa yang akan kamu jelajahi di sore yang luang?",
        "Prioritas mana yang berubah di tengah jalan?",
        "Apa yang kamu perbaiki, di rumah atau di tempat lain?",
        "Hari mana yang akan kamu baca ulang lebih dulu?",
        "Apa yang kamu beli dan ternyata sepadan?",
        "Apa yang kamu masak atau makan yang layak diingat?",
        "Kebiasaan mana yang muncul tanpa kamu sadari?",
        "Apa yang akan membuat hari-hari mendatang lebih sederhana?",
        "Apa yang kamu tolak, dan bagaimana hasilnya?",
        "Tugas mana yang bisa kamu selesaikan dalam lima menit?",
        "Apa yang mengejutkanmu dari cara kamu menghabiskan waktu?",
        "Apa yang ingin kamu ingat dari hari-hari ini setahun lagi?",
        "Apa yang lebih sulit dari kelihatannya?",
        "Ke mana kamu pergi untuk pertama kalinya?",
        "Apa yang kamu buat dengan tanganmu?",
        "Rencana mana yang berubah, dan apa penggantinya?",
        "Kabar mana yang menandai hari-hari ini?",
        "Apa yang bisa kamu sisihkan?",
        "Apa yang kamu tunggu, dan apakah sudah datang?",
        "Apa yang akan kamu tulis di baris pertama halaman berikutnya?",
        "Bagaimana kamu merangkum hari-hari ini dalam satu baris?",
    )

    private val QUESTIONS_JA = listOf(
        "もし手で書き直すなら、移行しないものは何ですか？",
        "いちばん長く待っているタスクはどれですか？",
        "やったのに、書き留めなかったことは何ですか？",
        "この数日のイベントで、覚えておきたいのはどれですか？",
        "読み返して驚いたメモはどれですか？",
        "今日、未練なく取り消し線を引けるものは何ですか？",
        "かけた時間に見合ったものは何でしたか？",
        "引き受けたけれど、今なら断るのは何ですか？",
        "思ったより小さかったタスクはどれですか？",
        "明日、最初にすることは何ですか？",
        "専用のコレクションを作る価値があるものは何ですか？",
        "何度も戻ってくるアイデアはどれですか？",
        "このページによく登場するのは誰ですか？",
        "ずっと前に始めて、やっと終えたことは何ですか？",
        "同じ日々をもう一度過ごすなら、何を変えますか？",
        "学んだことで、残しておきたいものは何ですか？",
        "実はいくつものタスクが入っているタスクはどれですか？",
        "待てるものは何ですか？",
        "思ったよりうまくいったことは何ですか？",
        "書き留めておいてよかったと思うことは何ですか？",
        "読み返して初めて気づいたことは何ですか？",
        "わざと途中でやめたことは何ですか？",
        "この数日で思い出す場所はどこですか？",
        "まるごと書き留めたい会話はどれですか？",
        "ある日を良くした小さなことは何ですか？",
        "どのタスクを移行し続けていて、それはなぜですか？",
        "もっと時間をかけたいことは何ですか？",
        "誰にも頼まれていないなら、やることから外すのは何ですか？",
        "ほかの人のために何をしましたか？",
        "誰かがしてくれたことは何ですか？",
        "繰り返したいことは何ですか？",
        "いちばんよかった、ふつうの瞬間はどれですか？",
        "まだ答えが出ていない問いは何ですか？",
        "読んだり、見たり、聞いたりして、メモする価値があったものは何ですか？",
        "何を決めて、その結果どうなりましたか？",
        "予定していなかったのに始めたことは何ですか？",
        "ほかの人に渡したいタスクはどれですか？",
        "先延ばしにしたけれど、結局うまくいったことは何ですか？",
        "このページに足りないものは何ですか？",
        "空いている午後に、何を調べてみたいですか？",
        "途中で変わった優先事項はどれですか？",
        "家やほかの場所で、直したものは何ですか？",
        "最初に読み返したい日はどの日ですか？",
        "買ってよかったものは何ですか？",
        "作ったり食べたりして、覚えておきたいものは何ですか？",
        "気づかないうちに身についた習慣はどれですか？",
        "これからの日々を楽にするものは何ですか？",
        "何を断って、どうなりましたか？",
        "5分でできるタスクはどれですか？",
        "時間の使い方で、意外だったことは何ですか？",
        "1年後に、この数日の何を覚えていたいですか？",
        "見た目より難しかったことは何ですか？",
        "初めて行ったのはどこですか？",
        "手を使って作ったものは何ですか？",
        "どの計画が変わって、代わりに何になりましたか？",
        "この数日を印象づけたニュースは何ですか？",
        "脇に置けるものは何ですか？",
        "何を待っていて、それは届きましたか？",
        "次のページの1行目に、何を書きますか？",
        "この数日を1行でまとめるなら、どう書きますか？",
    )

    private val QUESTIONS_KO = listOf(
        "손으로 다시 써야 한다면 옮기지 않을 것은 무엇인가요?",
        "어떤 할 일이 가장 오래 기다리고 있나요?",
        "했지만 적지 않은 일은 무엇인가요?",
        "이 날들의 일정 중 기억하고 싶은 것은 무엇인가요?",
        "다시 읽었을 때 놀라웠던 메모는 무엇인가요?",
        "오늘 미련 없이 줄을 그어 지울 것은 무엇인가요?",
        "들인 시간만큼 값어치가 있었던 것은 무엇인가요?",
        "하겠다고 했지만 지금이라면 안 하겠다고 할 일은 무엇인가요?",
        "생각보다 작았던 할 일은 무엇인가요?",
        "내일 가장 먼저 할 일은 무엇인가요?",
        "따로 목록을 만들 만한 것은 무엇인가요?",
        "계속 떠오르는 생각은 무엇인가요?",
        "이 페이지들에 가장 자주 나오는 사람은 누구인가요?",
        "오래전에 시작해서 마침내 끝낸 일은 무엇인가요?",
        "같은 날들을 다시 산다면 무엇을 다르게 할까요?",
        "배워서 간직하고 싶은 것은 무엇인가요?",
        "사실은 여러 개인 할 일은 무엇인가요?",
        "기다려도 되는 것은 무엇인가요?",
        "기대보다 잘된 일은 무엇인가요?",
        "적어 두길 잘했다고 생각하는 것은 무엇인가요?",
        "다시 읽고서야 알아챈 것은 무엇인가요?",
        "일부러 끝내지 않고 남겨 둔 것은 무엇인가요?",
        "이 날들에서 기억나는 장소는 어디인가요?",
        "통째로 적어 두고 싶은 대화는 무엇인가요?",
        "하루를 더 낫게 만든 작은 일은 무엇인가요?",
        "계속 옮기고 있는 할 일은 무엇이고, 왜일까요?",
        "무엇을 위한 시간이 더 있으면 좋겠나요?",
        "아무도 시키지 않는다면 목록에서 뺄 것은 무엇인가요?",
        "다른 사람을 위해 한 일은 무엇인가요?",
        "누군가 나를 위해 해 준 일은 무엇인가요?",
        "다시 하고 싶은 것은 무엇인가요?",
        "가장 좋았던 평범한 순간은 언제인가요?",
        "아직 열려 있는 질문은 무엇인가요?",
        "읽고, 보고, 들은 것 중 메모할 만한 것은 무엇인가요?",
        "무엇을 결정했고, 그 결과는 어땠나요?",
        "계획 없이 시작한 일은 무엇인가요?",
        "다른 사람에게 맡기고 싶은 할 일은 무엇인가요?",
        "미뤘는데 결국 괜찮았던 일은 무엇인가요?",
        "이 페이지들에 빠진 것은 무엇인가요?",
        "한가한 오후라면 무엇을 알아볼까요?",
        "가는 길에 바뀐 우선순위는 무엇인가요?",
        "집이나 다른 곳에서 고친 것은 무엇인가요?",
        "어느 날을 가장 먼저 다시 읽고 싶나요?",
        "산 것 중 잘 샀다고 느낀 것은 무엇인가요?",
        "기억해 둘 만한 요리나 음식은 무엇인가요?",
        "나도 모르게 생긴 습관은 무엇인가요?",
        "앞으로의 날들을 더 단순하게 만들어 줄 것은 무엇인가요?",
        "무엇을 거절했고, 어땠나요?",
        "5분이면 끝낼 수 있는 할 일은 무엇인가요?",
        "시간을 쓴 방식에서 놀라웠던 점은 무엇인가요?",
        "1년 뒤에도 기억하고 싶은 이 날들의 일은 무엇인가요?",
        "보기보다 어려웠던 것은 무엇인가요?",
        "처음 가 본 곳은 어디인가요?",
        "손으로 만든 것은 무엇인가요?",
        "바뀐 계획은 무엇이고, 무엇이 그것을 대신했나요?",
        "이 날들에 가장 크게 남은 소식은 무엇인가요?",
        "내려놓아도 되는 것은 무엇인가요?",
        "기다리던 것은 무엇이고, 왔나요?",
        "다음 페이지의 첫 줄에 무엇을 쓰고 싶나요?",
        "이 날들을 한 줄로 정리한다면 뭐라고 쓸까요?",
    )

    /** [i] in 0 until QUESTION_COUNT (docs/tecnico.md 12.3); order matches docs/textos.md 23. */
    fun question(i: Int): String {
        val list = when (lang) {
            "es" -> QUESTIONS_ES
            "pt" -> QUESTIONS_PT
            "de" -> QUESTIONS_DE
            "fr" -> QUESTIONS_FR
            "it" -> QUESTIONS_IT
            "nl" -> QUESTIONS_NL
            "pl" -> QUESTIONS_PL
            "ru" -> QUESTIONS_RU
            "tr" -> QUESTIONS_TR
            "id" -> QUESTIONS_ID
            "ja" -> QUESTIONS_JA
            "ko" -> QUESTIONS_KO
            else -> QUESTIONS_EN
        }
        return list[i]
    }

    // --- 24. v1.2 ------------------------------------------------------------------------------

    val syncRow = t(
        "Sync",
        "Sincronizar",
        "Sincronizar",
        "Synchronisieren",
        "Synchroniser",
        "Sincronizza",
        "Synchroniseren",
        "Synchronizacja",
        "Синхронизация",
        "Eşitleme",
        "Sinkronisasi",
        "同期",
        "동기화",
    )
    val syncOff = t("Off", "Apagada", "Desativada", "Aus", "Désactivée", "Disattivata", "Uit", "Wyłączona", "Выключена", "Kapalı", "Mati", "オフ", "꺼짐")
    val syncICloud = t(
        "With iCloud Drive",
        "Con iCloud Drive",
        "Com o iCloud Drive",
        "Mit iCloud Drive",
        "Avec iCloud Drive",
        "Con iCloud Drive",
        "Met iCloud Drive",
        "Przez iCloud Drive",
        "Через iCloud Drive",
        "iCloud Drive ile",
        "Dengan iCloud Drive",
        "iCloud Driveを使用",
        "iCloud Drive 사용",
    )
    fun syncFile(name: String) = t(
        "With $name, in the folder you chose",
        "Con $name, en la carpeta que elegiste",
        "Com $name, na pasta que você escolheu",
        "Mit $name im Ordner, den du gewählt hast",
        "Avec $name, dans le dossier que tu as choisi",
        "Con $name, nella cartella che hai scelto",
        "Met $name, in de map die je hebt gekozen",
        "Przez plik $name w wybranym przez ciebie folderze",
        "Через $name в выбранной папке",
        "$name ile, seçtiğin klasörde",
        "Dengan $name, di folder yang kamu pilih",
        "選んだフォルダの${name}を使用",
        "선택한 폴더의 ${name} 사용",
    )
    fun syncLast(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "Last time, $time",
            "Última vez, $time",
            "Última vez, $time",
            "Zuletzt um $time",
            "Dernière fois, $time",
            "Ultima volta alle $time",
            "Laatste keer om $time",
            "Ostatnio o $time",
            "Последний раз: $time",
            "Son eşitleme: $time",
            "Terakhir pukul $time",
            "前回: ${time}",
            "마지막 동기화: ${time}",
        )
    }
    val syncConflictTitle = t(
        "Both sides have changed",
        "Los dos lados han cambiado",
        "Os dois lados mudaram",
        "Beide Seiten haben sich geändert",
        "Les deux côtés ont changé",
        "Entrambi i lati sono cambiati",
        "Beide kanten zijn gewijzigd",
        "Obie strony się zmieniły",
        "Изменились обе стороны",
        "İki taraf da değişti",
        "Kedua sisi berubah",
        "両方で変更があります",
        "양쪽 모두 바뀌었어요",
    )
    val syncConflictText = t(
        "This phone and the other one have different changes since last time. Choose which one to keep, or merge them after seeing a summary.",
        "Este teléfono y el otro tienen cambios distintos desde la última vez. Elige con cuál quedarte, o júntalos tras ver un resumen.",
        "Este telefone e o outro têm alterações diferentes desde a última vez. Escolha qual manter, ou junte os dois depois de ver um resumo.",
        "Dieses Handy und das andere haben seit dem letzten Mal unterschiedliche Änderungen. Wähle, welches du behältst, oder führe sie nach einer Übersicht zusammen.",
        "Ce téléphone et l'autre ont des modifications différentes depuis la dernière fois. Choisis lequel garder, ou fusionne-les après avoir vu un résumé.",
        "Questo telefono e l'altro hanno modifiche diverse dall'ultima volta. Scegli quale tenere, oppure uniscili dopo aver visto un riepilogo.",
        "Deze telefoon en de andere hebben sinds de vorige keer verschillende wijzigingen. Kies welke je houdt, of voeg ze samen nadat je een overzicht hebt gezien.",
        "Ten telefon i drugi mają różne zmiany od ostatniego razu. Wybierz, który zachować, albo połącz je po obejrzeniu podsumowania.",
        "На этом телефоне и на другом разные изменения с прошлого раза. Выбери, какой оставить, или объедини оба после краткой сводки.",
        "Bu telefonla diğerinde, son seferden beri farklı değişiklikler var. Hangisini tutacağını seç ya da bir özete baktıktan sonra ikisini birleştir.",
        "Ponsel ini dan yang satunya punya perubahan yang berbeda sejak terakhir kali. Pilih mana yang dipertahankan, atau gabungkan setelah melihat ringkasan.",
        "この端末ともう一方の端末で、前回からそれぞれ違う変更があります。どちらかを残すか、概要を見てから両方を合わせるか、選べます。",
        "마지막 동기화 이후 이 휴대폰과 다른 쪽에 서로 다른 변경이 생겼어요. 어느 쪽을 유지할지 고르거나, 요약을 본 뒤 합칠 수 있어요.",
    )
    val syncKeepThis = t(
        "Keep this phone",
        "Usar este teléfono",
        "Manter este telefone",
        "Dieses Handy behalten",
        "Garder ce téléphone",
        "Tieni questo telefono",
        "Deze telefoon houden",
        "Zachowaj ten telefon",
        "Оставить этот телефон",
        "Bu telefonu koru",
        "Pakai ponsel ini",
        "この端末を残す",
        "이 휴대폰 유지",
    )
    val syncKeepOther = t(
        "Keep the other",
        "Usar el otro",
        "Manter o outro",
        "Das andere behalten",
        "Garder l'autre",
        "Tieni l'altro",
        "De andere houden",
        "Zachowaj drugi",
        "Оставить другой",
        "Diğerini koru",
        "Pakai yang satunya",
        "もう一方を残す",
        "다른 쪽 유지",
    )
    val syncMerge = t(
        "Merge both",
        "Juntar los dos",
        "Juntar os dois",
        "Beide zusammenführen",
        "Fusionner les deux",
        "Unisci entrambi",
        "Beide samenvoegen",
        "Połącz oba",
        "Объединить оба",
        "İkisini birleştir",
        "Gabungkan keduanya",
        "両方を合わせる",
        "둘 다 합치기",
    )
    val syncCreate = t(
        "Create a new bobbin-sync.json",
        "Crear un bobbin-sync.json nuevo",
        "Criar um bobbin-sync.json novo",
        "Neue bobbin-sync.json erstellen",
        "Créer un nouveau bobbin-sync.json",
        "Crea un nuovo bobbin-sync.json",
        "Een nieuw bobbin-sync.json maken",
        "Utwórz nowy plik bobbin-sync.json",
        "Создать новый bobbin-sync.json",
        "Yeni bir bobbin-sync.json oluştur",
        "Buat bobbin-sync.json baru",
        "新しいbobbin-sync.jsonを作成",
        "새 bobbin-sync.json 만들기",
    )
    val syncOpen = t(
        "Use the one from another device",
        "Usar el de otro dispositivo",
        "Usar o de outro dispositivo",
        "Die von einem anderen Gerät verwenden",
        "Utiliser celui d'un autre appareil",
        "Usa quello di un altro dispositivo",
        "Die van een ander apparaat gebruiken",
        "Użyj pliku z innego urządzenia",
        "Использовать файл с другого устройства",
        "Başka bir cihazdakini kullan",
        "Pakai yang dari perangkat lain",
        "ほかの端末のものを使う",
        "다른 기기의 파일 사용",
    )
    val syncNoICloud = t(
        "To sync, sign in to iCloud and turn on iCloud Drive in Settings.",
        "Para sincronizar, inicia sesión en iCloud y activa iCloud Drive en Ajustes.",
        "Para sincronizar, entre no iCloud e ative o iCloud Drive nos Ajustes.",
        "Melde dich zum Synchronisieren in den Einstellungen bei iCloud an und aktiviere iCloud Drive.",
        "Pour synchroniser, connecte-toi à iCloud et active iCloud Drive dans Réglages.",
        "Per sincronizzare, accedi a iCloud e attiva iCloud Drive in Impostazioni.",
        "Meld je aan bij iCloud en zet iCloud Drive aan in Instellingen om te synchroniseren.",
        "Aby synchronizować, zaloguj się do iCloud i włącz iCloud Drive w Ustawieniach.",
        "Чтобы синхронизировать, войди в iCloud и включи iCloud Drive в Настройках.",
        "Eşitlemek için Ayarlar'da iCloud'a giriş yap ve iCloud Drive'ı aç.",
        "Untuk menyinkronkan, masuk ke iCloud dan aktifkan iCloud Drive di Pengaturan.",
        "同期するには、設定でiCloudにサインインし、iCloud Driveをオンにしてください。",
        "동기화하려면 설정에서 iCloud에 로그인하고 iCloud Drive를 켜세요.",
    )
    val proSync = t(
        "Sync through your own cloud",
        "Sincronizar con tu propia nube",
        "Sincronizar pela sua própria nuvem",
        "Synchronisieren über deine eigene Cloud",
        "Synchroniser via ton propre cloud",
        "Sincronizza tramite il tuo cloud",
        "Synchroniseren via je eigen cloud",
        "Synchronizacja przez własną chmurę",
        "Синхронизация через собственное облако",
        "Kendi bulutunla eşitle",
        "Sinkronisasi lewat cloud milikmu sendiri",
        "自分のクラウドで同期",
        "내 클라우드로 동기화",
    )
    val proYearSummary = t(
        "The year summary",
        "El resumen del año",
        "O resumo do ano",
        "Der Jahresrückblick",
        "Le bilan de l'année",
        "Il riepilogo dell'anno",
        "Het jaaroverzicht",
        "Podsumowanie roku",
        "Итоги года",
        "Yıl özeti",
        "Ringkasan tahun",
        "1年のまとめ",
        "한 해 요약",
    )
    val yearSummaryRow = t(
        "Year summary",
        "Resumen del año",
        "Resumo do ano",
        "Jahresrückblick",
        "Bilan de l'année",
        "Riepilogo dell'anno",
        "Jaaroverzicht",
        "Podsumowanie roku",
        "Итоги года",
        "Yıl özeti",
        "Ringkasan tahun",
        "1年のまとめ",
        "한 해 요약",
    )
    val yearSummarySub = t(
        "Inspirations, priorities done and what travelled the most",
        "Inspiraciones, prioridades hechas y lo que más viajó",
        "Inspirações, prioridades feitas e o que mais viajou",
        "Inspirationen, erledigte Prioritäten und was am weitesten reiste",
        "Inspirations, priorités faites et ce qui a le plus voyagé",
        "Ispirazioni, priorità fatte e ciò che ha viaggiato di più",
        "Inspiraties, voltooide prioriteiten en wat het meest heeft gereisd",
        "Inspiracje, zrobione priorytety i to, co najwięcej wędrowało",
        "Вдохновение, выполненные приоритеты и то, что переносилось чаще всего",
        "İlhamlar, yapılan öncelikler ve en çok yolculuk eden görevler",
        "Inspirasi, prioritas yang selesai, dan yang paling banyak berpindah",
        "ひらめき、完了した優先、いちばん旅したもの",
        "영감, 완료한 우선순위, 가장 많이 옮겨 다닌 것",
    )
    fun yearSummaryTitle(year: Int) = t(
        "Your $year",
        "Tu $year",
        "Seu $year",
        "Dein $year",
        "Ton $year",
        "Il tuo $year",
        "Jouw $year",
        "Twój rok $year",
        "Твой $year год",
        "$year yılın",
        "Tahunmu $year",
        "${year}年",
        "나의 ${year}년",
    )
    val summaryInspirations = t(
        "Inspirations",
        "Inspiraciones",
        "Inspirações",
        "Inspirationen",
        "Inspirations",
        "Ispirazioni",
        "Inspiraties",
        "Inspiracje",
        "Вдохновение",
        "İlhamlar",
        "Inspirasi",
        "ひらめき",
        "영감",
    )
    val summaryPriorities = t(
        "Priorities done",
        "Prioridades hechas",
        "Prioridades feitas",
        "Erledigte Prioritäten",
        "Priorités faites",
        "Priorità fatte",
        "Voltooide prioriteiten",
        "Zrobione priorytety",
        "Выполненные приоритеты",
        "Yapılan öncelikler",
        "Prioritas yang selesai",
        "完了した優先",
        "완료한 우선순위",
    )
    val summaryMostMigrated = t(
        "Travelled the most",
        "Las que más viajaron",
        "As que mais viajaram",
        "Am weitesten gereist",
        "Les plus voyageuses",
        "Quelle che hanno viaggiato di più",
        "Het meest gereisd",
        "Najwięcej wędrowały",
        "Чаще всего переносились",
        "En çok yolculuk edenler",
        "Paling banyak berpindah",
        "いちばん旅したもの",
        "가장 많이 옮겨 다닌 것",
    )
    val a11yPreviousYear = t(
        "Previous year",
        "Año anterior",
        "Ano anterior",
        "Vorheriges Jahr",
        "Année précédente",
        "Anno precedente",
        "Vorig jaar",
        "Poprzedni rok",
        "Предыдущий год",
        "Önceki yıl",
        "Tahun sebelumnya",
        "前の年",
        "이전 해",
    )
    val a11yNextYear = t(
        "Next year",
        "Año siguiente",
        "Próximo ano",
        "Nächstes Jahr",
        "Année suivante",
        "Anno successivo",
        "Volgend jaar",
        "Następny rok",
        "Следующий год",
        "Sonraki yıl",
        "Tahun berikutnya",
        "次の年",
        "다음 해",
    )
    val summaryEmpty = t(
        "This year you marked no inspirations or priorities. Next year starts blank.",
        "Este año no marcaste inspiraciones ni prioridades. El que viene está en blanco.",
        "Este ano você não marcou inspirações nem prioridades. O próximo está em branco.",
        "Dieses Jahr hast du keine Inspirationen oder Prioritäten markiert. Das nächste ist noch leer.",
        "Cette année, tu n'as marqué ni inspirations ni priorités. La prochaine est vierge.",
        "Quest'anno non hai segnato né ispirazioni né priorità. Il prossimo è in bianco.",
        "Dit jaar heb je geen inspiraties of prioriteiten gemarkeerd. Het volgende jaar begint leeg.",
        "W tym roku nie oznaczono inspiracji ani priorytetów. Następny rok jest jeszcze pusty.",
        "В этом году ничего не отмечено как вдохновение или приоритет. Следующий год пока пуст.",
        "Bu yıl hiç ilham ya da öncelik işaretlemedin. Sonraki yıl boş başlıyor.",
        "Tahun ini kamu tidak menandai inspirasi atau prioritas. Tahun depan dimulai kosong.",
        "この年は、ひらめきも優先も付けませんでした。次の年は白紙から始まります。",
        "올해는 영감도 우선순위도 표시하지 않았어요. 내년은 빈 페이지로 시작해요.",
    )
    val iconAndTheme = t(
        "Icon and theme",
        "Icono y tema",
        "Ícone e tema",
        "Symbol und Thema",
        "Icône et thème",
        "Icona e tema",
        "Pictogram en thema",
        "Ikona i motyw",
        "Значок и тема",
        "Simge ve tema",
        "Ikon dan tema",
        "アイコンとテーマ",
        "아이콘과 테마",
    )
    val iconNone = t(
        "No icon",
        "Sin icono",
        "Sem ícone",
        "Kein Symbol",
        "Sans icône",
        "Nessuna icona",
        "Geen pictogram",
        "Bez ikony",
        "Без значка",
        "Simge yok",
        "Tanpa ikon",
        "アイコンなし",
        "아이콘 없음",
    )
    val themeHint = t("Theme", "Tema", "Tema", "Thema", "Thème", "Tema", "Thema", "Motyw", "Тема", "Tema", "Tema", "テーマ", "테마")
    val themeNone = t(
        "No theme",
        "Sin tema",
        "Sem tema",
        "Ohne Thema",
        "Sans thème",
        "Nessun tema",
        "Geen thema",
        "Bez motywu",
        "Без темы",
        "Tema yok",
        "Tanpa tema",
        "テーマなし",
        "테마 없음",
    )

    private val ICON_ORDER = listOf("star", "heart", "book", "home", "work", "travel", "money", "health", "food", "music", "idea", "people")

    fun iconName(id: String): String {
        val names = t(
            "Star, Heart, Book, Home, Work, Travel, Money, Health, Food, Music, Idea, People",
            "Estrella, Corazón, Libro, Casa, Trabajo, Viaje, Dinero, Salud, Comida, Música, Idea, Personas",
            "Estrela, Coração, Livro, Casa, Trabalho, Viagem, Dinheiro, Saúde, Comida, Música, Ideia, Pessoas",
            "Stern, Herz, Buch, Zuhause, Arbeit, Reise, Geld, Gesundheit, Essen, Musik, Idee, Menschen",
            "Étoile, Coeur, Livre, Maison, Travail, Voyage, Argent, Santé, Cuisine, Musique, Idée, Personnes",
            "Stella, Cuore, Libro, Casa, Lavoro, Viaggio, Denaro, Salute, Cibo, Musica, Idea, Persone",
            "Ster, Hart, Boek, Huis, Werk, Reis, Geld, Gezondheid, Eten, Muziek, Idee, Mensen",
            "Gwiazda, Serce, Książka, Dom, Praca, Podróż, Pieniądze, Zdrowie, Jedzenie, Muzyka, Pomysł, Ludzie",
            "Звезда, Сердце, Книга, Дом, Работа, Поездка, Деньги, Здоровье, Еда, Музыка, Идея, Люди",
            "Yıldız, Kalp, Kitap, Ev, İş, Seyahat, Para, Sağlık, Yemek, Müzik, Fikir, İnsanlar",
            "Bintang, Hati, Buku, Rumah, Kerja, Perjalanan, Uang, Kesehatan, Makanan, Musik, Ide, Orang",
            "星, ハート, 本, 家, 仕事, 旅行, お金, 健康, 食べ物, 音楽, アイデア, 人",
            "별, 하트, 책, 집, 일, 여행, 돈, 건강, 음식, 음악, 아이디어, 사람",
        ).split(", ")
        return names[ICON_ORDER.indexOf(id).takeIf { it >= 0 } ?: 0]
    }

    /** Written in normal case here; the Eyebrow style uppercases it when painting. */
    fun siblingFrom(app: String, d: LocalDate): String = "$app, ${dayMonthYear(d)}"

    val importToDay = t(
        "Import to that day",
        "Importar a ese día",
        "Importar para esse dia",
        "An diesem Tag importieren",
        "Importer à ce jour",
        "Importa in quel giorno",
        "Op die dag importeren",
        "Importuj do tego dnia",
        "Импортировать на этот день",
        "O güne içe aktar",
        "Impor ke hari itu",
        "その日に読み込む",
        "그날로 가져오기",
    )
    val importElsewhere = t(
        "To another day or collection",
        "A otro día o colección",
        "Para outro dia ou coleção",
        "An einen anderen Tag oder in eine Sammlung",
        "Vers un autre jour ou une collection",
        "In un altro giorno o in una raccolta",
        "Naar een andere dag of collectie",
        "Do innego dnia lub kolekcji",
        "На другой день или в коллекцию",
        "Başka bir güne ya da koleksiyona",
        "Ke hari lain atau koleksi",
        "別の日またはコレクションへ",
        "다른 날이나 목록으로",
    )
    val importSkip = t("Skip", "Saltar", "Pular", "Überspringen", "Passer", "Salta", "Overslaan", "Pomiń", "Пропустить", "Atla", "Lewati", "スキップ", "건너뛰기")

    fun importSiblingDone(n: Int): String = when {
        n == 0 -> t(
            "Nothing was imported.",
            "No se ha importado nada.",
            "Nada foi importado.",
            "Nichts wurde importiert.",
            "Rien n'a été importé.",
            "Non è stato importato nulla.",
            "Er is niets geïmporteerd.",
            "Nic nie zaimportowano.",
            "Ничего не импортировано.",
            "Hiçbir şey içe aktarılmadı.",
            "Tidak ada yang diimpor.",
            "何も読み込まれませんでした。",
            "아무것도 가져오지 않았어요.",
        )
        isSingular(n) -> t(
            "1 entry imported.",
            "1 entrada importada.",
            "1 entrada importada.",
            "1 Eintrag importiert.",
            "1 entrée importée.",
            "1 voce importata.",
            "1 item geïmporteerd.",
            "Zaimportowano 1 wpis.",
            "Импортирована 1 запись.",
            "1 kayıt içe aktarıldı.",
            "1 entri diimpor.",
            "1件の項目を読み込みました。",
            "항목 1개를 가져왔어요.",
        )
        else -> t(
            "$n entries imported.",
            "$n entradas importadas.",
            "$n entradas importadas.",
            "$n Einträge importiert.",
            "$n entrées importées.",
            "$n voci importate.",
            "$n items geïmporteerd.",
            "Zaimportowano $n ${wordEntry(n)}.",
            "${word(n, "Импортирована|Импортированы|Импортировано")} $n ${word(n, "запись|записи|записей")}.",
            "$n kayıt içe aktarıldı.",
            "$n entri diimpor.",
            "${n}件の項目を読み込みました。",
            "항목 ${n}개를 가져왔어요.",
        )
    }

    val importNotSibling = t(
        "That file is not a backup from Bobbin, Purl, MoodTraker or Quilt.",
        "Ese fichero no es una copia de Bobbin, Purl, MoodTraker ni Quilt.",
        "Esse arquivo não é uma cópia do Bobbin, Purl, MoodTraker nem Quilt.",
        "Diese Datei ist keine Sicherung von Bobbin, Purl, MoodTraker oder Quilt.",
        "Ce fichier n'est pas une copie de Bobbin, Purl, MoodTraker ou Quilt.",
        "Quel file non è una copia di Bobbin, Purl, MoodTraker o Quilt.",
        "Dat bestand is geen back-up van Bobbin, Purl, MoodTraker of Quilt.",
        "Ten plik nie jest kopią z Bobbin, Purl, MoodTraker ani Quilt.",
        "Этот файл не копия Bobbin, Purl, MoodTraker или Quilt.",
        "Bu dosya Bobbin, Purl, MoodTraker ya da Quilt yedeği değil.",
        "Berkas itu bukan cadangan dari Bobbin, Purl, MoodTraker, atau Quilt.",
        "このファイルは、Bobbin、Purl、MoodTraker、Quiltのバックアップではありません。",
        "이 파일은 Bobbin, Purl, MoodTraker, Quilt의 백업이 아니에요.",
    )

    // --- 25. v1.3 -----------------------------------------------------------------------------

    // Sending a list (docs/pantallas.md 23.11, #75).
    val sendList = t(
        "Send the list",
        "Enviar la lista",
        "Enviar a lista",
        "Liste senden",
        "Envoyer la liste",
        "Invia la lista",
        "Lijst versturen",
        "Wyślij listę",
        "Отправить список",
        "Listeyi gönder",
        "Kirim daftar",
        "リストを送る",
        "목록 보내기",
    )
    val sendAsFile = t(
        "Send as a file",
        "Enviar como archivo",
        "Enviar como arquivo",
        "Als Datei senden",
        "Envoyer comme fichier",
        "Invia come file",
        "Als bestand versturen",
        "Wyślij jako plik",
        "Отправить файлом",
        "Dosya olarak gönder",
        "Kirim sebagai berkas",
        "ファイルとして送る",
        "파일로 보내기",
    )
    val sendAsFileSub = t(
        "For AirDrop, Quick Share or email. With Bobbin, it opens and merges with theirs.",
        "Para AirDrop, Quick Share o correo. Con Bobbin se abre y se junta con la suya.",
        "Para AirDrop, Quick Share ou e-mail. Com o Bobbin, abre e se junta com a da outra pessoa.",
        "Für AirDrop, Quick Share oder E-Mail. Mit Bobbin öffnet sie sich und wird mit der anderen zusammengeführt.",
        "Pour AirDrop, Quick Share ou e-mail. Avec Bobbin, elle s'ouvre et se fusionne avec l'autre.",
        "Per AirDrop, Quick Share o email. Con Bobbin si apre e si unisce alla sua.",
        "Voor AirDrop, Quick Share of e-mail. Met Bobbin opent het bestand en wordt de lijst samengevoegd met die van de ander.",
        "Przez AirDrop, Quick Share lub e-mail. W Bobbin otwiera się i łączy z ich listą.",
        "Для AirDrop, Quick Share или почты. В Bobbin он откроется и объединится с версией списка у получателя.",
        "AirDrop, Quick Share ya da e-posta için. Bobbin ile açılır ve karşıdakinin listesiyle birleşir.",
        "Untuk AirDrop, Quick Share, atau email. Dengan Bobbin, daftarnya terbuka dan digabung dengan milik penerima.",
        "AirDrop、Quick Share、メールで送れます。Bobbinで開くと、相手のリストと合わさります。",
        "AirDrop, Quick Share, 이메일용이에요. Bobbin에서 열면 받는 사람의 목록과 합쳐져요.",
    )
    val sendBlank = t("Blank", "En blanco", "Em branco", "Leer", "Vierge", "In bianco", "Blanco", "Pusta", "Пустой", "Boş", "Kosong", "白紙", "빈 목록")
    val sendBlankSub = t(
        "Just the text, everything open, as a new list.",
        "Solo los textos, todo abierto, como una lista nueva.",
        "Só os textos, tudo aberto, como uma lista nova.",
        "Nur die Texte, alles offen, als neue Liste.",
        "Seulement les textes, tout ouvert, comme une nouvelle liste.",
        "Solo i testi, tutto aperto, come una lista nuova.",
        "Alleen de teksten, alles open, als nieuwe lijst.",
        "Tylko teksty, wszystko otwarte, jako nowa lista.",
        "Только тексты, всё открыто, как новый список.",
        "Yalnızca metinler, hepsi açık, yeni bir liste olarak.",
        "Hanya teksnya, semua terbuka, sebagai daftar baru.",
        "テキストだけを、すべて未完了の新しいリストとして。",
        "글만, 모두 미완료로, 새 목록으로 보내요.",
    )
    val sendTrackerBlank = t(
        "A tracker is sent with its rows and without marks.",
        "Un seguimiento se envía con sus filas y sin marcas.",
        "Um tracker é enviado com suas linhas e sem marcas.",
        "Ein Tracker wird mit seinen Zeilen und ohne Markierungen gesendet.",
        "Un suivi s'envoie avec ses lignes et sans marques.",
        "Un tracker viene inviato con le sue righe e senza segni.",
        "Een tracker wordt verstuurd met zijn rijen en zonder markeringen.",
        "Tracker jest wysyłany z wierszami, ale bez oznaczeń.",
        "Трекер отправляется со строками, но без отметок.",
        "Bir çizelge, satırlarıyla ve işaretsiz gönderilir.",
        "Pelacak dikirim dengan baris-barisnya dan tanpa tanda.",
        "トラッカーは、行だけを印なしで送ります。",
        "트래커는 줄은 그대로, 표시는 없이 보내져요.",
    )

    /** Written in normal case here; the Eyebrow style uppercases it when painting. */
    val receivedEyebrow = t(
        "List received",
        "Lista recibida",
        "Lista recebida",
        "Empfangene Liste",
        "Liste reçue",
        "Lista ricevuta",
        "Lijst ontvangen",
        "Otrzymana lista",
        "Получен список",
        "Gelen liste",
        "Daftar diterima",
        "受け取ったリスト",
        "받은 목록",
    )
    fun receivedCount(n: Int): String = entryCount(n)
    fun receivedRows(n: Int): String {
        val w = word(n, t(
            "row|rows",
            "fila|filas",
            "linha|linhas",
            "Zeile|Zeilen",
            "ligne|lignes",
            "riga|righe",
            "rij|rijen",
            "wiersz|wiersze|wierszy",
            "строка|строки|строк",
            "satır",
            "baris",
            "行",
            "줄",
        ))
        return t("$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "$n $w", "${n}${w}", "${n}${w}")
    }
    fun receivedMore(n: Int) = t(
        "and $n more",
        "y $n más",
        "e mais $n",
        "und $n weitere",
        "et $n de plus",
        "e altre $n",
        "en nog $n",
        "i jeszcze $n",
        "и ещё $n",
        "ve $n tane daha",
        "dan $n lainnya",
        "ほか${n}件",
        "외 ${n}개",
    )
    val addToIndex = t(
        "Add to Index",
        "Añadir al Índice",
        "Adicionar ao Índice",
        "Zum Index hinzufügen",
        "Ajouter à l'Index",
        "Aggiungi all'Indice",
        "Aan Index toevoegen",
        "Dodaj do Indeksu",
        "Добавить в Индекс",
        "Dizin'e ekle",
        "Tambah ke Indeks",
        "目次に追加",
        "목차에 추가",
    )
    fun alreadyHave(title: String) = t(
        "You already have $title",
        "Ya tienes $title",
        "Você já tem $title",
        "Du hast $title schon",
        "Tu as déjà $title",
        "Hai già $title",
        "Je hebt $title al",
        "Masz już listę $title",
        "У тебя уже есть $title",
        "Sende zaten var: $title",
        "Kamu sudah punya $title",
        "「${title}」はすでにあります",
        "이미 있는 목록: ${title}",
    )

    /** [added] or [updated] is dropped when it is 0; both at 0 never gets here, that is [mergeNothing]. */
    fun receivedSummary(added: Int, updated: Int): String {
        val parts = listOfNotNull(
            added.takeIf { it > 0 }?.let { importAddedPhrase(it) },
            updated.takeIf { it > 0 }?.let { importUpdatedPhrase(it) },
        ).joinToString(t(" and ", " y ", " e ", " und ", " et ", " e ", " en ", " i ", " и ", " ve ", " dan ", "、", ", "))
        return t(
            "It brings $parts. Nothing gets deleted.",
            "Trae $parts. No se borra nada.",
            "Traz $parts. Nada é apagado.",
            "Sie bringt $parts. Es wird nichts gelöscht.",
            "Elle apporte $parts. Rien n'est supprimé.",
            "Porta $parts. Non viene eliminato nulla.",
            "De lijst bevat $parts. Er wordt niets verwijderd.",
            "Zawiera $parts. Nic nie zostanie usunięte.",
            "В нём есть $parts. Ничего не удаляется.",
            "Şunları getiriyor: $parts. Hiçbir şey silinmez.",
            "Daftar ini membawa $parts. Tidak ada yang dihapus.",
            "${parts}が含まれています。何も削除されません。",
            "가져올 것: ${parts}. 아무것도 삭제되지 않아요.",
        )
    }
    val mergeList = t("Merge", "Juntar", "Juntar", "Zusammenführen", "Fusionner", "Unisci", "Samenvoegen", "Połącz", "Объединить", "Birleştir", "Gabungkan", "合わせる", "합치기")
    val mergeNothing = t(
        "You already have this list exactly as it is.",
        "Ya tienes esta lista igual.",
        "Você já tem esta lista igual.",
        "Du hast diese Liste schon genau so.",
        "Tu as déjà cette liste à l'identique.",
        "Hai già questa lista così com'è.",
        "Je hebt deze lijst al precies zo.",
        "Masz już tę listę w dokładnie takiej postaci.",
        "У тебя уже есть точно такой же список.",
        "Bu liste sende zaten aynen var.",
        "Kamu sudah punya daftar ini persis seperti ini.",
        "このリストは、まったく同じものがすでにあります。",
        "이 목록은 이미 똑같이 있어요.",
    )
    val listAdded = t(
        "List added to the Index",
        "Lista añadida al Índice",
        "Lista adicionada ao Índice",
        "Liste zum Index hinzugefügt",
        "Liste ajoutée à l'Index",
        "Lista aggiunta all'Indice",
        "Lijst aan de Index toegevoegd",
        "Lista dodana do Indeksu",
        "Список добавлен в Индекс",
        "Liste Dizin'e eklendi",
        "Daftar ditambahkan ke Indeks",
        "リストを目次に追加しました",
        "목록을 목차에 추가했어요",
    )
    val listMerged = t(
        "Lists merged",
        "Listas juntadas",
        "Listas juntadas",
        "Listen zusammengeführt",
        "Listes fusionnées",
        "Liste unite",
        "Lijsten samengevoegd",
        "Listy połączone",
        "Списки объединены",
        "Listeler birleştirildi",
        "Daftar digabungkan",
        "リストを合わせました",
        "목록을 합쳤어요",
    )
    val listDamaged = t(
        "This list is incomplete or damaged. Your journal wasn't touched.",
        "Esta lista está incompleta o dañada. Tu diario no se ha tocado.",
        "Esta lista está incompleta ou danificada. Seu diário não foi alterado.",
        "Diese Liste ist unvollständig oder beschädigt. Dein Journal wurde nicht verändert.",
        "Cette liste est incomplète ou endommagée. Ton journal n'a pas été touché.",
        "Questa lista è incompleta o danneggiata. Il tuo diario non è stato toccato.",
        "Deze lijst is onvolledig of beschadigd. Je dagboek is niet aangeraakt.",
        "Ta lista jest niepełna lub uszkodzona. Twój dziennik pozostał nietknięty.",
        "Этот список неполный или повреждён. Твой дневник не тронут.",
        "Bu liste eksik ya da bozuk. Günlüğüne dokunulmadı.",
        "Daftar ini tidak lengkap atau rusak. Jurnalmu tidak diubah.",
        "このリストは不完全か壊れています。ジャーナルには手を加えていません。",
        "이 목록이 불완전하거나 손상됐어요. 다이어리는 그대로예요.",
    )
    val sentListEmpty = t(
        "This list has nothing in it.",
        "Esta lista no tiene nada.",
        "Esta lista não tem nada.",
        "Diese Liste ist leer.",
        "Cette liste ne contient rien.",
        "Questa lista è vuota.",
        "Deze lijst is leeg.",
        "Ta lista jest pusta.",
        "В этом списке ничего нет.",
        "Bu listede hiçbir şey yok.",
        "Daftar ini kosong.",
        "このリストは空です。",
        "이 목록은 비어 있어요.",
    )
    val listTooNew = t(
        "This list comes from a newer version of Bobbin. Update the app to open it.",
        "Esta lista es de una versión más nueva de Bobbin. Actualiza la app para abrirla.",
        "Esta lista é de uma versão mais nova do Bobbin. Atualize o app para abri-la.",
        "Diese Liste stammt aus einer neueren Version von Bobbin. Aktualisiere die App, um sie zu öffnen.",
        "Cette liste vient d'une version plus récente de Bobbin. Mets l'app à jour pour l'ouvrir.",
        "Questa lista viene da una versione più recente di Bobbin. Aggiorna l'app per aprirla.",
        "Deze lijst komt van een nieuwere versie van Bobbin. Werk de app bij om de lijst te openen.",
        "Ta lista pochodzi z nowszej wersji Bobbin. Zaktualizuj aplikację, aby ją otworzyć.",
        "Этот список из более новой версии Bobbin. Обнови приложение, чтобы открыть его.",
        "Bu liste Bobbin'in daha yeni bir sürümünden. Açmak için uygulamayı güncelle.",
        "Daftar ini dari versi Bobbin yang lebih baru. Perbarui aplikasinya untuk membukanya.",
        "このリストは新しいバージョンのBobbinのものです。開くにはアプリを更新してください。",
        "더 새로운 버전의 Bobbin에서 온 목록이에요. 열려면 앱을 업데이트하세요.",
    )

    // The week (docs/pantallas.md 23.13, #78).
    val viewDay = t("Day", "Día", "Dia", "Tag", "Jour", "Giorno", "Dag", "Dzień", "День", "Gün", "Hari", "日", "일간")
    val viewWeek = t("Week", "Semana", "Semana", "Woche", "Semaine", "Settimana", "Week", "Tydzień", "Неделя", "Hafta", "Pekan", "週", "주간")
    fun weekTitle(start: LocalDate): String {
        val d = shortDate(start)
        return t(
            "Week of ${dayFirst(start)}",
            "Semana del $d",
            "Semana de $d",
            "Woche vom $d",
            "Semaine du $d",
            "Settimana ${if (d.startsWith("8 ") || d.startsWith("11 ")) "dell'" else "del "}$d",
            "Week van $d",
            "Tydzień od $d",
            "Неделя с $d",
            "$d haftası",
            "Pekan $d",
            "${d}の週",
            "${d}의 한 주",
        )
    }
    fun weekRange(from: LocalDate, to: LocalDate): String {
        val a = shortDate(from)
        val b = shortDate(to)
        return t(
            "${dayFirst(from)} to ${dayFirst(to)}",
            "Del $a al $b",
            "De $a a $b",
            "$a bis $b",
            "Du $a au $b",
            "${if (a.startsWith("8 ") || a.startsWith("11 ")) "Dall'" else "Dal "}$a ${if (b.startsWith("8 ") || b.startsWith("11 ")) "all'" else "al "}$b",
            "$a tot $b",
            "Od $a do $b",
            "С $a по $b",
            "$a - $b",
            "$a sampai $b",
            "${a}から${b}",
            "${a}부터 ${b}까지",
        )
    }

    /** "26 October": the English title of a week reads day first, like the other four. */
    private fun dayFirst(d: LocalDate) = "${d.day} ${monthNames("en")[d.month.ordinal]}"

    /** Written in normal case here; the Eyebrow style uppercases it when painting. */
    val weekToday = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
    fun weekDayLabel(d: LocalDate, withMonth: Boolean): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        return if (withMonth) weekdayWithDate(w, shortDate(d)) else t(
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}.",
            "$w ${d.day}",
            "$w ${d.day}",
            "$w ${d.day}",
            "$w, ${d.day}",
            "$w, ${d.day}",
            "${d.day} $w",
            "$w, ${d.day}",
            "${d.day}日 ${w}",
            "${d.day}일 ${w}",
        )
    }
    val backToThisWeek = t(
        "Back to this week",
        "Volver a esta semana",
        "Voltar a esta semana",
        "Zurück zu dieser Woche",
        "Retour à cette semaine",
        "Questa settimana",
        "Terug naar deze week",
        "Wróć do tego tygodnia",
        "К этой неделе",
        "Bu haftaya dön",
        "Kembali ke pekan ini",
        "今週に戻る",
        "이번 주로 돌아가기",
    )
    val inCalendar = t(
        "In the calendar",
        "En el calendario",
        "No calendário",
        "Im Kalender",
        "Dans le calendrier",
        "Nel calendario",
        "In de kalender",
        "W kalendarzu",
        "В календаре",
        "Takvimde",
        "Di kalender",
        "カレンダーにあるもの",
        "캘린더에",
    )
    val inFuture = t(
        "In Future",
        "En Futuro",
        "No Futuro",
        "In Zukunft",
        "Dans Futur",
        "In Futuro",
        "In Toekomst",
        "W Przyszłości",
        "В Будущем",
        "Gelecek'te",
        "Di Mendatang",
        "未来にあるもの",
        "미래에",
    )
    val a11yPreviousWeek = t(
        "Previous week",
        "Semana anterior",
        "Semana anterior",
        "Vorherige Woche",
        "Semaine précédente",
        "Settimana precedente",
        "Vorige week",
        "Poprzedni tydzień",
        "Предыдущая неделя",
        "Önceki hafta",
        "Pekan sebelumnya",
        "前の週",
        "이전 주",
    )
    val a11yNextWeek = t(
        "Next week",
        "Semana siguiente",
        "Próxima semana",
        "Nächste Woche",
        "Semaine suivante",
        "Settimana successiva",
        "Volgende week",
        "Następny tydzień",
        "Следующая неделя",
        "Sonraki hafta",
        "Pekan berikutnya",
        "次の週",
        "다음 주",
    )

    // A hardware keyboard (docs/pantallas.md 23.15, #80).
    val keysTitle = t(
        "With a keyboard",
        "Con teclado",
        "Com teclado",
        "Mit Tastatur",
        "Avec un clavier",
        "Con la tastiera",
        "Met een toetsenbord",
        "Z klawiaturą",
        "С клавиатурой",
        "Klavye ile",
        "Dengan keyboard",
        "キーボード操作",
        "키보드로",
    )
    val keyNew = t(
        "Write in today",
        "Escribir en hoy",
        "Escrever no dia de hoje",
        "Für heute schreiben",
        "Écrire pour aujourd'hui",
        "Scrivi per oggi",
        "Schrijven in vandaag",
        "Pisz na dziś",
        "Писать на сегодня",
        "Bugüne yaz",
        "Tulis untuk hari ini",
        "今日に書く",
        "오늘 페이지에 쓰기",
    )
    val keySearch = t("Search", "Buscar", "Buscar", "Suchen", "Rechercher", "Cerca", "Zoeken", "Szukaj", "Поиск", "Ara", "Cari", "検索", "검색")
    val keyTabs = t(
        "Go to Today, Month, Future or Index",
        "Ir a Hoy, Mes, Futuro o Índice",
        "Ir para Hoje, Mês, Futuro ou Índice",
        "Zu Heute, Monat, Zukunft oder Index",
        "Aller à Aujourd'hui, Mois, Futur ou Index",
        "Vai a Oggi, Mese, Futuro o Indice",
        "Naar Vandaag, Maand, Toekomst of Index",
        "Przejdź do: Dziś, Miesiąc, Przyszłość lub Indeks",
        "Перейти: Сегодня, Месяц, Будущее или Индекс",
        "Bugün, Ay, Gelecek ya da Dizin'e git",
        "Buka Hari, Bulan, Mendatang, atau Indeks",
        "今日、月、未来、目次へ移動",
        "오늘, 월간, 미래, 목차로 이동",
    )
    val keyPrevNext = t(
        "Previous and next",
        "Anterior y siguiente",
        "Anterior e próximo",
        "Zurück und weiter",
        "Précédent et suivant",
        "Precedente e successivo",
        "Vorige en volgende",
        "Poprzedni i następny",
        "Предыдущий и следующий",
        "Önceki ve sonraki",
        "Sebelumnya dan berikutnya",
        "前と次",
        "이전과 다음",
    )
    val keyBack = t("Back", "Atrás", "Voltar", "Zurück", "Retour", "Indietro", "Terug", "Wstecz", "Назад", "Geri", "Kembali", "戻る", "뒤로")

    // Sharing a tracker and the story shape (docs/pantallas.md 23.12, #77).
    val shareTracker = t(
        "Share this tracker",
        "Compartir este seguimiento",
        "Compartilhar este tracker",
        "Diesen Tracker teilen",
        "Partager ce suivi",
        "Condividi questo tracker",
        "Deze tracker delen",
        "Udostępnij ten tracker",
        "Поделиться этим трекером",
        "Bu çizelgeyi paylaş",
        "Bagikan pelacak ini",
        "このトラッカーを共有",
        "이 트래커 공유",
    )
    val formatPost = t("Post", "Publicación", "Post", "Beitrag", "Publication", "Post", "Post", "Post", "Пост", "Gönderi", "Postingan", "投稿", "게시물")
    val formatStory = t("Story", "Historia", "Story", "Story", "Story", "Storia", "Story", "Relacja", "История", "Hikaye", "Cerita", "ストーリー", "스토리")

    // Writing from outside, second part (docs/pantallas.md 23.14, #79).
    val captureWidgetLabel = t("Write", "Escribir", "Escrever", "Schreiben", "Écrire", "Scrivi", "Schrijven", "Napisz", "Записать", "Yaz", "Tulis", "書く", "쓰기")
    val proWriteOutside = t(
        "Write without opening the app",
        "Escribir sin abrir la app",
        "Escrever sem abrir o app",
        "Schreiben, ohne die App zu öffnen",
        "Écrire sans ouvrir l'app",
        "Scrivi senza aprire l'app",
        "Schrijven zonder de app te openen",
        "Pisanie bez otwierania aplikacji",
        "Писать, не открывая приложение",
        "Uygulamayı açmadan yaz",
        "Menulis tanpa membuka aplikasi",
        "アプリを開かずに書く",
        "앱을 열지 않고 쓰기",
    )
}
