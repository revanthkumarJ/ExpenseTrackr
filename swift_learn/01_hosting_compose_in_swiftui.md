# 01 — Hosting the shared Compose app inside SwiftUI

**Component:** `ComposeView` (SwiftUI) + `MainViewController()` (Kotlin) + Koin startup.

This is the "glue" step: instead of rebuilding every screen in SwiftUI right now, we run the
**entire shared Compose Multiplatform app** on iOS and let SwiftUI host it. Same UI and business
logic as Android, one codebase. (We can still port individual screens to native SwiftUI later.)

---

## The three pieces

### 1. Kotlin: expose the app as a UIViewController

```kotlin
// shared/src/iosMain/.../MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider(LocalShareHandler provides IosShareHandler()) {
        App()
    }
}
```

- `ComposeUIViewController { ... }` wraps a `@Composable` into a UIKit `UIViewController` — the
  Compose ↔ iOS bridge (analogous to `ComponentActivity.setContent { }` on Android).
- `CompositionLocalProvider(...)` injects the iOS share-sheet implementation, exactly like Android's
  `MainActivity` provides its `ShareHandler`. `CompositionLocal` ≈ SwiftUI's `@Environment`.

### 2. Swift: wrap that UIViewController as a SwiftUI View

```swift
// iosApp/iosApp/ContentView.swift
import Shared   // the Kotlin framework (baseName = "Shared")

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .ignoresSafeArea(.keyboard)
    }
}
```

### 3. Swift: start Koin before the UI (once)

```swift
// iosApp/iosApp/iOSApp.swift
@main
struct iOSApp: App {
    init() { KoinIosKt.startKoinIos() }   // dependency injection, like Android's Application.onCreate
    var body: some Scene { WindowGroup { ContentView() } }
}
```

---

## Kotlin → Swift name mapping (important gotcha)

Kotlin top-level functions become **static methods on a class named `<FileName>Kt`**:

| Kotlin file | Kotlin function | Swift call |
|---|---|---|
| `MainViewController.kt` | `MainViewController()` | `MainViewControllerKt.MainViewController()` |
| `KoinIos.kt` | `startKoinIos()` | `KoinIosKt.startKoinIos()` |

⚠️ We deliberately named it `startKoinIos`, **not** `initKoin`: Kotlin/Native renames functions
starting with `init` when exporting to Objective-C/Swift (the `init` family is special there), which
would surprise us with a name like `doInitKoin()`. Avoiding the `init` prefix keeps the Swift name
predictable.

---

## Concept-by-concept

| SwiftUI / Swift | What it is | Compose / Kotlin analogue |
|---|---|---|
| `UIViewControllerRepresentable` | Protocol to use a UIKit controller as a SwiftUI `View` | `AndroidView { }` embedding a legacy View |
| `makeUIViewController` | Called once to build the controller | factory lambda |
| `updateUIViewController` | Called when SwiftUI state changes | recomposition callback (empty — Compose owns its state) |
| `import Shared` | Imports the Kotlin `Shared.framework` | `implementation(project(":shared"))` |
| `.ignoresSafeArea()` | Draw under notch/home-indicator | `WindowCompat.setDecorFitsSystemWindows(false)` / edge-to-edge |
| `App` / `Scene` / `WindowGroup` | SwiftUI app entry + window | `Application` + `Activity` + `setContent` |
| `init()` on a `struct App` | Runs once at app launch | `Application.onCreate()` |

## New Swift words

- **`UIViewControllerRepresentable`** — the bridge protocol; requires `makeUIViewController` +
  `updateUIViewController`.
- **`Context`** (the parameter) — SwiftUI-provided coordinator/environment info for the bridge; we
  don't need it here.
- **`init()`** — a Swift initializer; on the `@main` `App` struct it's the earliest place to run
  setup code (here, starting Koin).
- **`some Scene`** — like `some View`, an opaque return type; a `Scene` is a top-level UI container
  (a window). `WindowGroup` is the standard one.

---

## Why start Koin in Swift?

The shared app uses Koin for dependency injection (`koinViewModel()`, `koinInject()`). On Android,
`ExpenseTrackerApp.onCreate` calls `startKoin { ... }`. iOS has no `Application` class, so the
SwiftUI `App.init()` is the equivalent one-time hook. If we skip it, the first screen crashes
because no dependencies are registered.
