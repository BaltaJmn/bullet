import SwiftUI

// Sin BobbinBridge, sin lock overlay y sin onOpenURL todavia: llegan con Lock y Route
// (docs/tecnico.md 3, #39, #40-42). Esto es solo lo que hace falta para arrancar.
@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
