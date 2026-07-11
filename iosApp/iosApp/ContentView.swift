import SwiftUI
import Shared

// MARK: - ComposeView
// Bridges the shared Compose Multiplatform UI (a UIKit `UIViewController`) into SwiftUI.
//
// `UIViewControllerRepresentable` is the SwiftUI protocol for wrapping any UIKit view controller
// so it can be used like a normal SwiftUI `View`. `MainViewControllerKt.MainViewController()` is
// the Kotlin function (in shared/iosMain) that returns `ComposeUIViewController { App() }` — i.e.
// the entire app, written once in Kotlin/Compose, reused on iOS.
//
// Compose ↔ SwiftUI mental model:
//   • `UIViewControllerRepresentable` ≈ Android's `AndroidView` (embedding one framework in another).
//   • `makeUIViewController` runs once to create the controller (≈ the factory lambda).
//   • `updateUIViewController` runs on SwiftUI state changes (nothing to push down here — Compose
//     manages its own state), so it's empty.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - ContentView
// The root SwiftUI view now simply hosts the shared Compose app full-screen.
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()          // Compose draws edge-to-edge and manages its own insets
            .ignoresSafeArea(.keyboard) // let Compose handle the on-screen keyboard
    }
}
