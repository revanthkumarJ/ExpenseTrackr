# ExpenseTrackr — iOS / SwiftUI Migration Plan & Handoff

> **Purpose of this file:** a self-contained context handoff. If the Claude Code conversation
> is cleared, paste/point at this file and work can resume exactly where it left off.
> Read `CLAUDE.md` first (project architecture + localization rules), then this.

---

## 1. The goal

Build the **iOS app's UI natively in SwiftUI**, screen by screen, as a **learning exercise** —
the user is learning Swift/SwiftUI by re-creating each Jetpack Compose screen component-by-component.

**Teaching style the user wants:**
- Go **component by component**, mapping each SwiftUI piece to its Jetpack Compose equivalent
  (e.g. `VStack` ≈ `Column`, `HStack` ≈ `Row`, `ZStack` ≈ `Box`, modifiers ≈ `Modifier` chain,
  `#Preview` ≈ `@Preview`, a `View` ≈ a `@Composable`).
- Explain Swift concepts as we go. Keep it incremental and verifiable (build/run after each step).

## 2. Architecture decision (already made)

**SwiftUI reuses the existing shared KMP business logic** — one source of truth. The user
explicitly chose this over a pure-Swift rewrite.

- Domain models, `Result`/`DateError`, repositories, `SalaryCalculator`, `DateFilter`, etc. live in
  `core:domain` / `core:data` and are exposed to Swift through the **`Shared`** framework
  (`import Shared` in Swift).
- **Important caveat / known constraint:** the feature **ViewModels** (`feature/*/presentation`)
  use AndroidX `androidx.lifecycle.ViewModel` + Koin `viewModelOf` + Compose. These are **not**
  cleanly consumable from Swift. So the intended iOS pattern is:
  - Swift `@Observable` (iOS 17+) / `ObservableObject` state holders **per screen**, which call the
    **Kotlin repositories** directly (clean Kotlin, Swift-friendly) and translate Kotlin `Flow`/
    `StateFlow` into Swift-observable state.
  - i.e. re-implement the thin VM/state layer in Swift, but reuse all repositories + domain logic.

## 3. Current state (what's DONE)

### iOS environment is unblocked ✅
The `Shared` framework now compiles & links for iOS. It had never fully built before (Android/JVM
hid native-only errors). Verified with:
```
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64    # BUILD SUCCESSFUL
```
Fixes applied:
| Problem | File | Fix |
|---|---|---|
| Room needs non-reflection ctor on Kotlin/Native | `core/database/.../ExpenseTrackerDatabase.kt` | Added `@ConstructedBy(ExpenseTrackerDatabaseConstructor::class)` + `expect object … : RoomDatabaseConstructor<…>` |
| Foreign-API opt-in | `core/database/.../DatabaseFactory.ios.kt`, `core/data/.../datasource/SettingsDataStore.ios.kt` | `@OptIn(ExperimentalForeignApi::class)` |
| `UByte.toString(radix)` / cinterop pointer indexing not on native | `core/data/.../util/PinHasher.ios.kt` | Rewrote with `UByteArray` + `usePinned` + `addressOf` |
| `String.format` is JVM-only | `feature/budget/.../BudgetScreen.kt` | New multiplatform `Double.toAmountString()` in `core/presentation/.../util/DateFormatter.kt` |

### First SwiftUI screen ✅
- `iosApp/iosApp/ContentView.swift` — replaced the old Compose host (`ComposeView` →
  `MainViewControllerKt.MainViewController()`) with **pure SwiftUI** showing "Hi 👋", annotated
  with Compose→SwiftUI mappings. `iOSApp.swift` (the `@main` entry) is unchanged.

### Salary/income feature (earlier work, Android-complete) ✅
A month-aware salary feature was added across the shared code + Android UI (see git log). Relevant
to iOS because the SwiftUI screens will eventually surface it too:
- `core:domain`: `SalaryEntry` + `SalaryCalculator` (`salaryForMonth`, prorated `salaryForRange`);
  `AppSettings.salaryHistory`.
- `core:data`: salary history persisted in DataStore (compact `monthIndex:amount;…` string).
- Android UI: salary editor in **Settings** (amount + "apply to all months" vs "from this month
  onward" toggle), Dashboard "salary remaining" card, Analytics income/saved cards + spent-vs-saved
  bar chart. Dashboard category list also sorted by amount desc.

## 4. Environment facts

- macOS, **Xcode 26.5** installed at `/Applications/Xcode.app`. Simulators available (e.g. iPhone 17).
- **Run the iOS app from Xcode**, not Android Studio (Android Studio has no SwiftUI canvas/Swift
  tooling). Edit Kotlin in Android Studio, Swift in Xcode — same repo on disk.
- Open: `open iosApp/iosApp.xcodeproj` → pick simulator → `Cmd+R`. Xcode auto-runs the Gradle
  build phase `./gradlew :shared:embedAndSignAppleFrameworkForXcode` (configured in `project.pbxproj`).
- Framework config: `shared/build.gradle.kts` → `baseName = "Shared"`, `isStatic = true`.
- Signing: simulator usually needs no Team; for a device set Team in *Signing & Capabilities*.
  Config lives in `iosApp/Configuration/Config.xcconfig` (`TEAM_ID` currently empty).

## 5. The plan from here (NEXT STEPS, in order)

> **Immediate next action:** confirm the user has run the app and seen "Hi 👋" on the simulator,
> then start step 1.

1. **Interop primer + Flow bridge.** Show how Swift consumes the `Shared` framework. The one new
   concept is bridging Kotlin `Flow`/`StateFlow` → SwiftUI. Provide a small Swift helper to collect
   a `Flow` into Swift `@Observable` state. Verify by reading one real value (e.g. the category list
   via `CategoryRepository`) into the "Hi" screen.
2. **Initialize Koin from Swift** at app start (`iOSApp.swift`) so Swift can resolve repositories.
   (Check how Koin is started today — likely an `initKoin`/`KoinHelper` in `shared`; may need to add
   a Swift-callable entry point.)
3. **First real screen — Dashboard.** Rebuild `DashboardScreen.kt` in SwiftUI component-by-component
   against a Swift state holder that uses the existing repositories. Compare each piece to the
   Compose original.
4. Continue screen-by-screen: Expenses (list + add/edit), Analytics, Budget, Categories, Settings,
   App-lock, Onboarding — mirroring `feature/*` modules.

Cross-cutting to handle as they come up: navigation (SwiftUI `NavigationStack` ≈ Compose `NavHost`),
theming (Compose `core:design-system` colors/typography → SwiftUI), localization (Compose string
resources are Kotlin-side; decide whether SwiftUI reuses them via the framework or uses native
`Localizable.strings`), currency `₹` formatting (`Double.toCurrencyString()` exists in Kotlin).

## 6. Reference: Compose feature modules (what to port)

| Module | Main file | Screens |
|---|---|---|
| dashboard | `DashboardScreen.kt` | Dashboard, SubCategory drilldown, Filtered expenses |
| expenses | `ExpensesScreen.kt`, `AddEditExpenseScreen.kt` | List + search; add/edit |
| analytics | `AnalyticsScreen.kt` | Donut chart, stat cards, savings bar chart |
| budget | `BudgetScreen.kt` | Monthly + per-category budgets |
| categories | `CategoriesScreen.kt` | Manage categories + sub-categories |
| settings | `SettingsScreen.kt`, `SubScreens.kt` | Settings, salary editor, app-lock, about/privacy/terms |
| applock | `AppLockScreen.kt` | PIN / biometric unlock |
| onboarding | `OnboardingScreen.kt` | Pager intro |

## 7. Build & verify commands

```bash
# iOS shared framework (what Xcode builds) — primary iOS verification:
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Still keep these green for Android/desktop:
./gradlew :shared:compileKotlinJvm                 # all features, common code
./gradlew :androidApp:assembleDebug                # Android APK
./gradlew :core:presentation:compileKotlinJvm      # regenerate string-resource accessors
```

## 8. Key paths

- iOS app (Swift): `iosApp/iosApp/` (`iOSApp.swift`, `ContentView.swift`, `Info.plist`, assets)
- Xcode project: `iosApp/iosApp.xcodeproj`
- Shared framework module: `shared/` (`build.gradle.kts`, `src/iosMain`, `App.kt` = Compose nav host)
- Shared Kotlin to consume from Swift: `core/domain`, `core/data`
