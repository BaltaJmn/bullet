import SwiftUI
import WidgetKit

/// docs/pantallas.md 18.2, Pro: one dot per day of the month, filled where the day has entries. Like the
/// Monthly Log and not a calendar: no weekdays. Only `monthMask` reaches it, never a word of the diary.
private struct MonthView: View {
    let entry: TodayEntry

    private var mask: String? { BobbinStore.shownMask(entry.state) }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                Text(L.monthNames[L.monthIndex(entry.day)].uppercased())
                    .font(.system(size: 12, weight: .medium)).foregroundStyle(.secondary).lineLimit(1)
                Spacer(minLength: 0)
                Glyph.task.view(.primary)
                Text("\(entry.state?.open ?? 0)").font(.system(size: 17, weight: .medium))
            }
            ZStack {
                MonthDots(
                    mask: mask,
                    days: BobbinStore.monthDays(entry.day),
                    today: L.dayNumber(entry.day),
                    cover: BobbinStore.coverColor(entry.state?.cover ?? "sage")
                )
                .opacity(mask == nil ? 0.4 : 1)
                if mask == nil {
                    VStack(spacing: 2) {
                        Text(L.proTitle).font(.system(size: 15, weight: .medium))
                        Text(L.unlock).font(.system(size: 12)).foregroundStyle(.secondary)
                    }
                }
            }
        }
        // Without Pro the whole widget is the door to the paywall.
        .widgetURL(URL(string: mask == nil ? "bobbin://pro" : "bobbin://today"))
        .containerBackground(for: .widget) { Color("WidgetBackground") }
    }
}

/// Two rows, 1 to 16 and 17 to the last day; the step is the width over 16. A nil mask is all empty.
private struct MonthDots: View {
    let mask: String?
    let days: Int
    let today: Int
    let cover: Color

    var body: some View {
        Canvas { context, size in
            let step = size.width / 16
            let r: CGFloat = 3
            let gap = min(step * 1.5, size.height / 2)
            let marks = Array(mask ?? "")
            for d in 1...days {
                let i = d - 1
                let x = step * CGFloat(i % 16) + step / 2
                let y = size.height / 2 + (i < 16 ? -gap / 2 : gap / 2)
                if i < marks.count && marks[i] == "1" {
                    context.fill(Path(ellipseIn: CGRect(x: x - r, y: y - r, width: 2 * r, height: 2 * r)), with: .color(cover))
                } else {
                    context.stroke(Path(ellipseIn: CGRect(x: x - r + 0.5, y: y - r + 0.5, width: 2 * r - 1, height: 2 * r - 1)),
                                   with: .color(Color("WidgetOutline")), lineWidth: 1)
                }
                if mask != nil && d == today {
                    let ring = r + 2.75
                    context.stroke(Path(ellipseIn: CGRect(x: x - ring, y: y - ring, width: 2 * ring, height: 2 * ring)),
                                   with: .color(.primary), lineWidth: 1.5)
                }
            }
        }
        .accessibilityHidden(true)
    }
}

struct BobbinMonthWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BobbinMonthWidget", provider: TodayProvider()) { entry in
            MonthView(entry: entry)
        }
        // Literals, not L: the gallery reads these at build time; the translations are in Localizable.strings.
        .configurationDisplayName("The month")
        .description("One dot for each day with entries")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
