// Test 36 (docs/tecnico.md 10): el espejo Swift de widget.json. La extension no tiene target de tests,
// asi que es un script; lo corre CI y se corre a mano al tocar BobbinStore.swift:
//
//   swiftc -parse-as-library -o /tmp/check-bobbinstore tools/check-bobbinstore.swift iosApp/BobbinWidget/BobbinStore.swift && /tmp/check-bobbinstore
//
import Foundation

func expect(_ got: String, _ want: String, _ what: String) {
    precondition(got == want, "\(what): got \(got), want \(want)")
    print("ok \(what): \(got)")
}

func instant(_ s: String) -> Date {
    let f = DateFormatter()
    f.locale = Locale(identifier: "en_US_POSIX")
    f.timeZone = .current
    f.dateFormat = "yyyy-MM-dd HH:mm"
    return f.date(from: s)!
}

@main
struct CheckBobbinStore {
    static func main() {
        // WIDGET_SAMPLE, read from the Kotlin file so the two sides can never drift apart silently.
        let here = URL(fileURLWithPath: #filePath).deletingLastPathComponent().deletingLastPathComponent()
        let kotlin = try! String(contentsOf: here.appendingPathComponent(
            "shared/src/commonTest/kotlin/com/baltajmn/bullet/WidgetSample.kt"), encoding: .utf8)
        let pieces = kotlin.components(separatedBy: "\"\"\"").enumerated().filter { $0.offset % 2 == 1 }.map { $0.element }
        let sample = pieces.joined()
        guard let st = BobbinStore.decode(sample.data(using: .utf8)!) else { fatalError("BobbinStore cannot decode WIDGET_SAMPLE: \(sample)") }

        expect(st.date, "2026-09-23", "date")
        expect("\(st.open) \(st.done) \(st.events)", "3 2 1", "counts")
        expect(st.month, "2026-09", "month")
        expect(st.monthMask, "110110011101111011101010000000", "monthMask")
        expect("\(st.reviewPending) \(st.isPro)", "true false", "flags")
        expect("\(st.cover) \(st.dayStartHour)", "sage 4", "cover and day start")

        // The logical day starts at dayStartHour, the same as Kotlin's logicalDate.
        expect(BobbinStore.key(BobbinStore.logicalDay(instant("2026-09-24 03:59"), dayStartHour: 4)), "2026-09-23", "03:59")
        expect(BobbinStore.key(BobbinStore.logicalDay(instant("2026-09-24 04:00"), dayStartHour: 4)), "2026-09-24", "04:00")
        expect(BobbinStore.key(BobbinStore.nextDayStart(after: instant("2026-09-24 03:00"), dayStartHour: 4)), "2026-09-24", "next start before")
        expect(BobbinStore.key(BobbinStore.nextDayStart(after: instant("2026-09-24 10:00"), dayStartHour: 4)), "2026-09-25", "next start after")

        // widgetView, with the dates of test 12.
        let same = BobbinStore.view(st, on: instant("2026-09-23 12:00"))
        expect("\(same == st)", "true", "same day untouched")
        let next = BobbinStore.view(st, on: instant("2026-09-24 12:00"))
        expect("\(next.open) \(next.done) \(next.events) \(next.reviewPending)", "0 0 0 true", "yesterday reads as zeros and a review")
        expect(next.monthMask, st.monthMask, "same month keeps the mask")
        let october = BobbinStore.view(st, on: instant("2026-10-01 12:00"))
        expect(october.month, "2026-10", "new month")
        expect(october.monthMask, String(repeating: "0", count: 31), "new month empty mask")

        // The month widget without Pro paints the locked state (#51).
        expect("\(BobbinStore.shownMask(st) == nil)", "true", "locked without Pro")
        let pro = BobbinState(date: st.date, open: st.open, done: st.done, events: st.events, month: st.month,
                              monthMask: st.monthMask, reviewPending: st.reviewPending, isPro: true,
                              cover: st.cover, dayStartHour: st.dayStartHour)
        expect(BobbinStore.shownMask(pro) ?? "nil", st.monthMask, "mask with Pro")
        print("all ok")
    }
}
