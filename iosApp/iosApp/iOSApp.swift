import SwiftUI
import UserNotifications
import Shared

/// Swift owns the notification delegate, so a tap on the reminder crosses to Kotlin as the same link
/// a widget would use (docs/tecnico.md 6.12).
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        BobbinBridge.shared.open(url: "bobbin://review")
        completionHandler()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.colorScheme) private var colorScheme

    var body: some Scene {
        WindowGroup {
            // The system takes the task switcher picture before Compose could repaint, so the cover
            // is painted here, in the window Swift owns: the paper and nothing else (docs/tecnico.md 6.15).
            ZStack {
                ContentView()
                if scenePhase != .active && BobbinBridge.shared.isLockOn() {
                    Color(colorScheme == .dark ? UIColor(red: 0.090, green: 0.082, blue: 0.059, alpha: 1)
                                               : UIColor(red: 0.984, green: 0.973, blue: 0.953, alpha: 1))
                        .ignoresSafeArea()
                }
            }
            .onOpenURL { BobbinBridge.shared.open(url: $0.absoluteString) }
        }
    }
}
