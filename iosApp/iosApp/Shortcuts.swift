import AppIntents
import Shared

/// "Note in Bobbin" (docs/tecnico.md 12.5): Siri and Shortcuts write into today's Daily Log through the
/// same rapidParse as Hoy. Every text is a literal, because the AppIntents metadata is read at build
/// time; the translations live in <lang>.lproj/Localizable.strings and AppShortcuts.strings.
struct CaptureIntent: AppIntent, ForegroundContinuableIntent {
    static var title: LocalizedStringResource = "Note in Bobbin"
    static var description = IntentDescription("Adds an entry to today in your journal.")

    @Parameter(title: "Text", requestValueDialog: IntentDialog("What do you want to note?"))
    var text: String

    @MainActor
    func perform() async throws -> some IntentResult & ProvidesDialog {
        let result = BobbinBridge.shared.capture(text: text)
        if result == OutsideCapture.saved {
            return .result(dialog: "Noted in Bobbin.")
        }
        if result == OutsideCapture.empty {
            return .result(dialog: "There was nothing to note.")
        }
        // Without Pro nothing was written: the app opens on the paywall.
        throw needsToContinueInForegroundError("Noting from outside the app is part of Bobbin Pro. Open the app to see it.") {
            BobbinBridge.shared.open(url: "bobbin://pro")
        }
    }
}

struct BobbinShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: CaptureIntent(),
            phrases: [
                "Note in \(.applicationName)",
                "Add an entry to \(.applicationName)",
            ],
            shortTitle: "Note in Bobbin",
            systemImageName: "square.and.pencil"
        )
    }
}
