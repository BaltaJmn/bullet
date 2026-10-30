import AppIntents
import SwiftUI
import WidgetKit

/// The mirror of `Strings.kt` for the extension, which cannot reach Kotlin (docs/textos.md 19).
enum L {
    private static var code: String {
        let two = String((Locale.preferredLanguages.first ?? "en").prefix(2))
        return two == "in" ? "id" : two
    }

    static func t(_ en: String, _ es: String, _ pt: String, _ de: String, _ fr: String, _ it: String, _ nl: String,
                  _ pl: String, _ ru: String, _ tr: String, _ id: String, _ ja: String, _ ko: String) -> String {
        switch code {
        case "es": return es
        case "pt": return pt
        case "de": return de
        case "fr": return fr
        case "it": return it
        case "nl": return nl
        case "pl": return pl
        case "ru": return ru
        case "tr": return tr
        case "id": return id
        case "ja": return ja
        case "ko": return ko
        default: return en
        }
    }

    /// One form, "one|other" (French takes the singular for 0 too), or Polish and Russian "one|few|many".
    private static func word(_ n: Int, _ forms: String) -> String {
        let f = forms.components(separatedBy: "|")
        if f.count == 1 { return f[0] }
        if f.count == 2 { return (code == "fr" ? n < 2 : n == 1) ? f[0] : f[1] }
        let tens = n % 100, units = n % 10
        if n == 1 || (code == "ru" && units == 1 && tens != 11) { return f[0] }
        return (2...4).contains(units) && !(12...14).contains(tens) ? f[1] : f[2]
    }

    static func open(_ n: Int) -> String {
        word(n, t(
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
            "미완료"
        ))
    }

    static func done(_ n: Int) -> String {
        word(n, t(
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
            "완료"
        ))
    }

    static func events(_ n: Int) -> String {
        word(n, t(
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
            "일정"
        ))
    }

    static var review: String {
        t(
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
            "살펴볼 것"
        )
    }

    static var proTitle: String { "Bobbin Pro" }
    static var unlock: String {
        t(
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
            "탭해서 켜기"
        )
    }

    static var weekdayShort: [String] {
        t(
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
            "월, 화, 수, 목, 금, 토, 일"
        ).components(separatedBy: ", ")
    }

    static var monthShort: [String] {
        t(
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
            "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월"
        ).components(separatedBy: ", ")
    }

    static var weekdayNames: [String] {
        t(
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
            "월요일, 화요일, 수요일, 목요일, 금요일, 토요일, 일요일"
        ).components(separatedBy: ", ")
    }

    static var monthNames: [String] {
        t(
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
            "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월"
        ).components(separatedBy: ", ")
    }

    /// Monday is 0, as in the app; Calendar counts from Sunday.
    static func weekdayIndex(_ day: Date) -> Int { (BobbinStore.calendar.component(.weekday, from: day) + 5) % 7 }
    static func monthIndex(_ day: Date) -> Int { BobbinStore.calendar.component(.month, from: day) - 1 }
    static func dayNumber(_ day: Date) -> Int { BobbinStore.calendar.component(.day, from: day) }

    /// WED 23 SEP, 9月23日(水): the same shape as `S.widgetDate`, upper case as the language writes it.
    static func date(_ day: Date) -> String {
        let w = weekdayShort[weekdayIndex(day)]
        let m = monthShort[monthIndex(day)].replacingOccurrences(of: ".", with: "")
        let n = dayNumber(day)
        let text: String
        switch code {
        case "ja": text = "\(m)\(n)日(\(w))"
        case "ko": text = "\(m) \(n)일 (\(w))"
        default: text = "\(w) \(n) \(m)"
        }
        return caps(text)
    }

    /// Upper case as the language writes it: Turkish dots its capital i.
    static func caps(_ text: String) -> String { text.uppercased(with: Locale(identifier: code)) }
}

/// The method's glyphs with the coordinates of docs/pantallas.md 1.5, in a 24 point box.
enum Glyph {
    case task, done, event, migrated

    @ViewBuilder func view(_ ink: Color) -> some View {
        Canvas { context, size in
            let s = size.width / 24
            func p(_ x: CGFloat, _ y: CGFloat) -> CGPoint { CGPoint(x: x * s, y: y * s) }
            let stroke = StrokeStyle(lineWidth: 1.5 * s, lineCap: .round, lineJoin: .round)
            let dot = Path(ellipseIn: CGRect(x: 9.5 * s, y: 9.5 * s, width: 5 * s, height: 5 * s))
            switch self {
            case .task:
                context.fill(dot, with: .color(ink))
            case .done:
                context.fill(dot, with: .color(ink))
                var cross = Path()
                cross.move(to: p(8, 8)); cross.addLine(to: p(16, 16))
                cross.move(to: p(16, 8)); cross.addLine(to: p(8, 16))
                context.stroke(cross, with: .color(ink), style: stroke)
            case .event:
                context.stroke(Path(ellipseIn: CGRect(x: 8 * s, y: 8 * s, width: 8 * s, height: 8 * s)),
                               with: .color(ink), style: stroke)
            case .migrated:
                var angle = Path()
                angle.move(to: p(10, 8)); angle.addLine(to: p(14.5, 12)); angle.addLine(to: p(10, 16))
                context.stroke(angle, with: .color(ink), style: stroke)
            }
        }
        .frame(width: 24, height: 24)
        .accessibilityHidden(true)
    }
}

struct TodayEntry: TimelineEntry {
    let date: Date
    /// The logical day this entry shows, which is not `date` between midnight and the day's start.
    let day: Date
    let state: BobbinState?
}

/// Now and the next change of day, each seen through `view`, and then woken at that change: one reload a
/// day and no more (docs/tecnico.md 6.13). The app asks for the rest when it writes widget.json.
struct TodayProvider: TimelineProvider {
    func placeholder(in context: Context) -> TodayEntry { entry(at: .now, state: nil) }

    func getSnapshot(in context: Context, completion: @escaping (TodayEntry) -> Void) {
        completion(entry(at: .now, state: BobbinStore.read()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TodayEntry>) -> Void) {
        let state = BobbinStore.read()
        let hour = state?.dayStartHour ?? BobbinStore.defaultDayStart
        let next = BobbinStore.nextDayStart(after: .now, dayStartHour: hour)
        completion(Timeline(entries: [entry(at: .now, state: state), entry(at: next, state: state)], policy: .after(next)))
    }

    private func entry(at instant: Date, state: BobbinState?) -> TodayEntry {
        let day = BobbinStore.logicalDay(instant, dayStartHour: state?.dayStartHour ?? BobbinStore.defaultDayStart)
        return TodayEntry(date: instant, day: day, state: state.map { BobbinStore.view($0, on: day) })
    }
}

private struct CountRow: View {
    let glyph: Glyph
    let n: Int
    let label: String?

    var body: some View {
        HStack(spacing: 4) {
            glyph.view(.primary)
            Text("\(n)").font(.system(size: 17, weight: .medium))
            if let label { Text(label).font(.system(size: 13)).foregroundStyle(.secondary).lineLimit(1) }
        }
        .frame(height: 24)
    }
}

private struct Counts: View {
    let state: BobbinState?
    let labels: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            CountRow(glyph: .task, n: state?.open ?? 0, label: labels ? L.open(state?.open ?? 0) : nil)
            CountRow(glyph: .done, n: state?.done ?? 0, label: labels ? L.done(state?.done ?? 0) : nil)
            CountRow(glyph: .event, n: state?.events ?? 0, label: labels ? L.events(state?.events ?? 0) : nil)
            if state?.reviewPending == true {
                HStack(spacing: 4) {
                    Glyph.migrated.view(.primary)
                    Text(L.review).font(.system(size: 12)).foregroundStyle(.secondary)
                }
                .padding(.top, 4)
            }
        }
    }
}

/// docs/pantallas.md 18.1: never a word of the diary, only its numbers.
private struct TodayView: View {
    @Environment(\.widgetFamily) private var family
    let entry: TodayEntry

    private var cover: Color { BobbinStore.coverColor(entry.state?.cover ?? "sage") }

    var body: some View {
        content
            .widgetURL(URL(string: "bobbin://today?focus"))
            .containerBackground(for: .widget) { Color("WidgetBackground") }
    }

    @ViewBuilder private var content: some View {
        switch family {
        case .systemMedium:
            HStack(alignment: .top, spacing: 16) {
                VStack(alignment: .leading, spacing: 0) {
                    Text(L.weekdayNames[L.weekdayIndex(entry.day)]).font(.system(size: 13)).foregroundStyle(.secondary)
                    HStack(spacing: 6) {
                        Text("\(L.dayNumber(entry.day))").font(.system(size: 34, weight: .medium))
                        Circle().fill(cover).frame(width: 8, height: 8)
                    }
                    Text(L.monthNames[L.monthIndex(entry.day)]).font(.system(size: 13)).foregroundStyle(.secondary)
                }
                Spacer(minLength: 0)
                Counts(state: entry.state, labels: true)
            }
        default:
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Circle().fill(cover).frame(width: 8, height: 8)
                    Text(L.date(entry.day)).font(.system(size: 12, weight: .medium)).foregroundStyle(.secondary)
                }
                Counts(state: entry.state, labels: true)
                Spacer(minLength: 0)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct BobbinTodayWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BobbinTodayWidget", provider: TodayProvider()) { entry in
            TodayView(entry: entry)
        }
        // Literals, not L: the gallery reads these at build time; the translations are in Localizable.strings.
        .configurationDisplayName("Today")
        .description("Today's open, done and events, without any text")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

/// docs/pantallas.md 18.3, Pro and iOS only: monochrome, as the system paints it, numbers and glyphs only.
private struct LockView: View {
    @Environment(\.widgetFamily) private var family
    let entry: TodayEntry

    private var pro: Bool { entry.state?.isPro == true }

    var body: some View {
        content
            .widgetURL(URL(string: pro ? "bobbin://today" : "bobbin://pro"))
            .containerBackground(for: .widget) { AccessoryWidgetBackground().opacity(family == .accessoryCircular ? 1 : 0) }
    }

    @ViewBuilder private var content: some View {
        let open = entry.state?.open ?? 0
        let events = entry.state?.events ?? 0
        switch (family, pro) {
        case (.accessoryCircular, true):
            VStack(spacing: -4) {
                Glyph.task.view(.primary)
                Text("\(open)").font(.system(size: 20, weight: .medium))
            }
        case (.accessoryCircular, false):
            Image(systemName: "lock")
        case (_, true):
            VStack(alignment: .leading, spacing: 0) {
                HStack(spacing: 2) { Glyph.task.view(.primary); Text("\(open) \(L.open(open))") }
                HStack(spacing: 2) { Glyph.event.view(.primary); Text("\(events) \(L.events(events))") }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        case (_, false):
            VStack(alignment: .leading, spacing: 0) {
                Text(L.proTitle).font(.headline)
                Text(L.unlock)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct BobbinLockWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BobbinLockWidget", provider: TodayProvider()) { entry in
            LockView(entry: entry)
        }
        .configurationDisplayName("Open tasks")
        .description("Today's open tasks on the lock screen")
        .supportedFamilies([.accessoryCircular, .accessoryRectangular])
    }
}

/// "Escribir en Bobbin" (docs/tecnico.md 12.14): a door to Hoy with the keyboard up, from the Control
/// Center, the lock screen or the Action button. Free: it opens the app, it writes nothing by itself.
/// The texts are literals, translated in <lang>.lproj, as AppIntents need.
@available(iOS 18.0, *)
struct OpenToWriteIntent: AppIntent {
    static let title: LocalizedStringResource = "Write in Bobbin"
    static let description = IntentDescription("Opens Today with the keyboard up")

    func perform() async throws -> some IntentResult & OpensIntent {
        .result(opensIntent: OpenURLIntent(URL(string: "bobbin://today?focus")!))
    }
}

@available(iOS 18.0, *)
struct BobbinWriteControl: ControlWidget {
    var body: some ControlWidgetConfiguration {
        StaticControlConfiguration(kind: "com.baltajmn.bullet.write") {
            ControlWidgetButton(action: OpenToWriteIntent()) {
                Label("Write in Bobbin", systemImage: "square.and.pencil")
            }
        }
        .displayName("Write in Bobbin")
        .description("Opens Today with the keyboard up")
    }
}

@main
struct BobbinWidgetBundle: WidgetBundle {
    var body: some Widget {
        BobbinTodayWidget()
        BobbinMonthWidget()
        BobbinLockWidget()
        if #available(iOS 18.0, *) {
            BobbinWriteControl()
        }
    }
}
