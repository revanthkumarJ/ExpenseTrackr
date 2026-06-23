import SwiftUI

// MARK: - ContentView
// This is our first *native* SwiftUI screen. The project used to host Jetpack/Compose
// Multiplatform UI here (via `ComposeView` + `MainViewController()`); we've replaced that
// with hand-written SwiftUI so you can learn it screen-by-screen.
//
// SwiftUI mental model (vs Jetpack Compose):
//   • A `View` here  ≈ a `@Composable` function in Compose.
//   • `var body: some View` ≈ the body of a @Composable. `some View` = "an opaque View type",
//     similar to how a @Composable returns Unit but emits UI.
//   • Modifiers chained with `.` (e.g. `.padding()`) ≈ Compose's `Modifier` chain.
//   • `VStack` ≈ `Column`, `HStack` ≈ `Row`, `ZStack` ≈ `Box`.
//   • `Text("Hi")` ≈ `Text("Hi")` in Compose. Same idea, different framework.
struct ContentView: View {
    var body: some View {
        // VStack = vertical Column. `spacing` ≈ Arrangement.spacedBy.
        VStack(spacing: 12) {
            Text("Hi 👋")
                .font(.largeTitle)            // ≈ MaterialTheme.typography.headlineLarge
                .fontWeight(.bold)            // ≈ fontWeight = FontWeight.Bold

            Text("ExpenseTrackr — SwiftUI")
                .font(.subheadline)           // ≈ typography.bodyMedium
                .foregroundStyle(.secondary)  // ≈ color = onSurfaceVariant
        }
        .padding()                            // ≈ Modifier.padding()
    }
}

// MARK: - Preview
// #Preview renders this view live in Xcode's canvas WITHOUT running the whole app —
// the closest equivalent to Compose's @Preview. Open the canvas with Cmd+Option+Return.
#Preview {
    ContentView()
}
