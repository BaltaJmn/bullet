import Foundation
import SwiftUI

/// Exactly the shape of `widget.json` (docs/tecnico.md 4.2): numbers, dates, booleans and one fixed
/// id. There is no model of the diary in Swift, and the file carries no text at all. A new field goes
/// here and in `WidgetState` in the same commit.
struct BobbinState: Decodable, Equatable {
    let date: String
    let open: Int
    let done: Int
    let events: Int
    let month: String
    let monthMask: String
    let reviewPending: Bool
    let isPro: Bool
    let cover: String
    let dayStartHour: Int
}

/// Reads the one file the extension is allowed to see, and nothing else. Widgets never write.
enum BobbinStore {
    static let appGroup = "group.com.baltajmn.bullet"
    static let defaultDayStart = 4

    static func read() -> BobbinState? {
        guard let url = FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroup)?
            .appendingPathComponent("widget.json"),
            let data = try? Data(contentsOf: url)
        else { return nil }
        return decode(data)
    }

    static func decode(_ data: Data) -> BobbinState? { try? JSONDecoder().decode(BobbinState.self, from: data) }

    /// The same rule as `widgetView` in Kotlin: the file may be from yesterday, when the app last ran.
    /// Yesterday's open tasks are now a review waiting.
    static func view(_ st: BobbinState, on day: Date) -> BobbinState {
        let today = key(day)
        if st.date == today { return st }
        let month = monthKey(day)
        return BobbinState(
            date: today, open: 0, done: 0, events: 0,
            month: month,
            monthMask: st.month == month ? st.monthMask : String(repeating: "0", count: monthDays(day)),
            reviewPending: st.reviewPending || st.open > 0,
            isPro: st.isPro, cover: st.cover, dayStartHour: st.dayStartHour
        )
    }

    /// The month widget's dots: the mask with Pro, nil without it, which is the locked widget. Kotlin's
    /// `shownMask` is the same rule.
    static func shownMask(_ st: BobbinState?) -> String? { st?.isPro == true ? st?.monthMask : nil }

    // MARK: - The logical day (docs/tecnico.md 6.1)

    static var calendar: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = .current
        return c
    }

    /// Before `dayStartHour` it is still the night of the day before.
    static func logicalDay(_ instant: Date, dayStartHour: Int) -> Date {
        let c = calendar
        let start = c.startOfDay(for: instant)
        return c.component(.hour, from: instant) < dayStartHour ? c.date(byAdding: .day, value: -1, to: start)! : start
    }

    /// The next `dayStartHour`:00 strictly after `instant`.
    static func nextDayStart(after instant: Date, dayStartHour: Int) -> Date {
        let c = calendar
        let todayAt = c.date(bySettingHour: dayStartHour, minute: 0, second: 0, of: instant)!
        return todayAt > instant ? todayAt : c.date(byAdding: .day, value: 1, to: todayAt)!
    }

    static func key(_ day: Date) -> String { format(day, "yyyy-MM-dd") }
    static func monthKey(_ day: Date) -> String { format(day, "yyyy-MM") }
    static func monthDays(_ day: Date) -> Int { calendar.range(of: .day, in: .month, for: day)!.count }

    private static func format(_ day: Date, _ pattern: String) -> String {
        let f = DateFormatter()
        f.calendar = calendar
        f.timeZone = .current
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = pattern
        return f.string(from: day)
    }

    // MARK: - The covers, the same eight as the app (docs/tecnico.md 5)

    static func coverColor(_ id: String) -> Color {
        switch id {
        case "rose": return Color(red: 0.941, green: 0.686, blue: 0.745)
        case "peach": return Color(red: 0.961, green: 0.765, blue: 0.608)
        case "butter": return Color(red: 0.929, green: 0.863, blue: 0.596)
        case "mint": return Color(red: 0.612, green: 0.827, blue: 0.780)
        case "sky": return Color(red: 0.635, green: 0.765, blue: 0.914)
        case "periwinkle": return Color(red: 0.706, green: 0.722, blue: 0.925)
        case "lilac": return Color(red: 0.851, green: 0.686, blue: 0.902)
        default: return Color(red: 0.714, green: 0.839, blue: 0.671)
        }
    }
}
