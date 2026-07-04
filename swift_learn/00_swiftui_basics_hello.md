# 00 · SwiftUI Basics — the "Hi 👋" screen

**Component:** `iosApp/iosApp/ContentView.swift` (our first native SwiftUI screen)
**Compose counterpart:** any simple `@Composable` with a `Column { Text(...) }`

This is the foundation. Everything else builds on these ideas.

---

## The code we wrote

```swift
import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("Hi 👋")
                .font(.largeTitle)
                .fontWeight(.bold)

            Text("ExpenseTrackr — SwiftUI")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
```

## The equivalent in Kotlin/Compose

```kotlin
@Composable
fun ContentView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hi 👋", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "ExpenseTrackr — SwiftUI",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun ContentViewPreview() { ContentView() }
```

---

## Concept-by-concept

### 1. `struct ContentView: View`  ≈  `@Composable fun ContentView()`
- In SwiftUI a screen/component is a **`struct` that conforms to the `View` protocol**. A
  `protocol` is like a Kotlin `interface`. "Conforms to" = "implements".
- In Compose, a component is a **function** annotated `@Composable`.
- Why a `struct` and not a `class`? Swift `struct`s are **value types** (copied, not referenced).
  SwiftUI relies on cheap, immutable value-type views that it re-creates constantly — conceptually
  like Compose re-running your function on recomposition.

### 2. `var body: some View`  ≈  the body of the `@Composable`
- `View` requires **one property** called `body` that returns the UI.
- `some View` means "**some specific type** of View that I won't spell out". It's an *opaque return
  type*. You don't write the exact type (it's a deeply nested generic); the compiler infers it.
  - Compose analogy: a `@Composable` function "returns" `Unit` but **emits** UI. SwiftUI's `body`
    literally returns a View value, but you treat it the same way — describe the UI declaratively.
- `var ... { ... }` here is a **computed property** (recomputed each time it's read), not a stored
  value. The `{ }` is the getter.

### 3. `VStack` / `HStack` / `ZStack`  ≈  `Column` / `Row` / `Box`

| SwiftUI | Compose | Lays out children… |
|---------|---------|--------------------|
| `VStack` | `Column` | vertically |
| `HStack` | `Row` | horizontally |
| `ZStack` | `Box` | stacked back-to-front (overlapping) |

- `VStack(spacing: 12)` ≈ `Column(verticalArrangement = Arrangement.spacedBy(12.dp))`.
- **Default alignment differs:** `VStack` **centers** children horizontally by default; Compose
  `Column` is **start-aligned** by default. To match Compose's centering you'd add
  `Column(horizontalAlignment = Alignment.CenterHorizontally)`. To match start-alignment in SwiftUI:
  `VStack(alignment: .leading)`.
- The children are written one after another inside the `{ }` — this is a **`@ViewBuilder`**
  (Swift's result-builder), the SwiftUI analog of the trailing `content: @Composable () -> Unit`
  lambda in `Column { ... }`. No commas between views.

### 4. Modifiers: `.font(...).fontWeight(...).padding()`  ≈  the `Modifier` chain / style params
- In SwiftUI you **chain methods on a view** to style/configure it. Each modifier returns a **new
  wrapped view**, so order matters (just like Compose `Modifier`).
- Mapping used here:
  - `.font(.largeTitle)` ≈ `style = MaterialTheme.typography.headlineLarge` (semantic text styles).
  - `.fontWeight(.bold)` ≈ `fontWeight = FontWeight.Bold`.
  - `.foregroundStyle(.secondary)` ≈ `color = MaterialTheme.colorScheme.onSurfaceVariant`
    (`.secondary` is a built-in adaptive "muted" color that also handles dark mode).
  - `.padding()` ≈ `Modifier.padding()`. With no argument it applies a **system default** inset on
    all sides (not a fixed dp). `.padding(16)` gives 16 points on all sides; `.padding(.horizontal, 16)`
    ≈ `Modifier.padding(horizontal = 16.dp)`.
- **Points vs dp:** SwiftUI uses **points** (no unit suffix — just `16`). Compose uses `.dp`. They're
  the same idea: density-independent units.

> ⚠️ Order gotcha (same as Compose): `.padding().background(...)` vs `.background(...).padding()`
> produce different results because each modifier wraps the previous view.

### 5. `Text("Hi 👋")`  ≈  `Text("Hi 👋")`
Nearly identical. SwiftUI `Text` is a value view; you style it with modifiers instead of parameters.

### 6. `#Preview { ContentView() }`  ≈  `@Preview @Composable`
- `#Preview` is a **macro** (the `#` marks a Swift macro) that renders the view live in Xcode's
  **canvas**, without launching the whole app — exactly like Compose `@Preview`.
- Open the canvas: `Cmd + Option + Return`. Resume/refresh it: `Cmd + Option + P`.
- You can have multiple `#Preview`s per file (different states/devices), like multiple `@Preview`s.

---

## Mental-model summary

| Idea | Compose | SwiftUI |
|------|---------|---------|
| A UI component | `@Composable fun` | `struct: View` with `var body` |
| Returns/emits UI | emits `Unit` | returns `some View` |
| Vertical / horizontal / overlap | `Column` / `Row` / `Box` | `VStack` / `HStack` / `ZStack` |
| Styling | `Modifier` chain + params | modifier methods chained on the view |
| Units | `.dp` | points (bare numbers) |
| Design tokens | `MaterialTheme.typography/colorScheme` | `.font(...)`, `.foregroundStyle(...)` |
| Live preview | `@Preview` | `#Preview` macro |
| Run target | Android Studio / emulator | Xcode / simulator (`Cmd+R`) |

## New Swift words you met here
- **`struct`** — value type (copied, immutable-friendly). Most SwiftUI views are structs.
- **`protocol`** — like a Kotlin `interface`; `: View` means "conforms to View".
- **opaque type `some View`** — "a concrete View whose exact type is inferred/hidden".
- **computed property** — `var body: some View { ... }` recomputes its value each read.
- **`@ViewBuilder`** — result builder that lets you list child views in `{ }` with no commas.
- **macro (`#Preview`)** — compile-time code generation marked with `#`.
