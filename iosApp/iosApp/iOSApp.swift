import SwiftUI
import Shared

@main
struct iOSApp: App {
    // Start Koin ONCE, before any Compose UI is created, so the shared app's `koinViewModel()` /
    // `koinInject()` calls can resolve their dependencies. Mirrors Android's ExpenseTrackerApp.
    init() {
        KoinIosKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
