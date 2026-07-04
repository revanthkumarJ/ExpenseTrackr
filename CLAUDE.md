# ExpenseTrackr — Project Guide for Claude

This file is auto-loaded every session. Read it first; it gives you the architecture and the
**mandatory localization workflow** so you don't need to re-scan all modules each time.

---

## 🌐 MANDATORY: Localization rule (read before writing ANY UI)

**The app is fully localized via Compose Multiplatform string resources. NEVER hardcode a
user-facing string in a screen.** Every new piece of UI text MUST go through string resources,
and MUST be added in all languages (machine translation is acceptable; mark for review).

### Where strings live
```
core/presentation/src/commonMain/composeResources/
├── values/strings.xml          ← English = source of truth & fallback
├── values-hi/strings.xml       ← Hindi (complete)
├── values-te/strings.xml       ← Telugu (complete)
└── values-<lang>/strings.xml   ← 22 more languages (common UI translated; rest falls back to English)
```
- `core/presentation/build.gradle.kts` sets only `publicResClass = true` (the package is the
  default `expensetrackr.core.presentation.generated.resources`). **Do NOT set `packageOfResClass`** —
  on Android the physical resources stay at the default path, so a custom package causes a runtime
  `MissingResourceException`.
- Every feature, `core:design-system`, and `shared` depend on `core:presentation`, so they can
  all use these resources.
- **Fallback:** a key missing in a `values-<lang>` file automatically uses the English value.
  Partial translations are safe.

### How to use a string in a Composable
```kotlin
import expensetrackr.core.presentation.generated.resources.*   // wildcard imports Res + all keys
import org.jetbrains.compose.resources.stringResource

Text(stringResource(Res.string.expenses_title))
Text(stringResource(Res.string.budget_warn_monthly, projected, budget))  // with args (%1$s, %2$s)
Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))    // content descriptions too
```

### The workflow when adding/changing UI text
1. Add the key to **`values/strings.xml`** (English). Use existing keys if one fits
   (e.g. `action_save`, `action_cancel`, `action_back`, `common_none`).
   - Format args use `%1$s`, `%2$d`; a literal `%` must be written `%%`.
   - Escape apostrophes as `\'` and `&` as `&amp;`.
2. Add the same key to **`values-hi`** and **`values-te`** (translate accurately) and, ideally,
   to the other `values-<lang>` files (machine translation OK). Skipped languages fall back to English.
3. Use it via `stringResource(...)`. Add `import ...resources.*` + the `stringResource` import once per file.
4. **Build to regenerate accessors:** `./gradlew :core:presentation:compileKotlinJvm` then the feature.

### ViewModels can't call `stringResource` — pattern for VM-produced text
`stringResource` is `@Composable`. When a string originates in a ViewModel:
- **Preferred:** keep structured data in state and format in the Composable. Example:
  `AddEditExpenseScreen` uses a `sealed interface BudgetWarning` (carries amounts) and the screen
  renders `stringResource(Res.string.budget_warn_monthly, ...)`.
- Or store a marker/identifier (e.g. category name) in state and resolve to a string in the UI
  (see `ManageCategoriesScreen` delete-error → `cannot_delete_message`).
- For one-off events (snackbars), capture the string in the Composable
  (`val msg = stringResource(...)`) and use it in the event handler (see `AllExpensesRoot`).
- Lists defined at top level (e.g. onboarding pages) must be built **inside** the Composable so
  `stringResource` is callable (see `OnboardingScreen`).

### In-app language selection (runtime locale override)
The user can pick a language; it does **not** rely on the system locale.
- Stored in `AppSettings.language` (BCP-47 tag, or `null` = system). Persisted in DataStore.
- Applied by `ProvideAppLocale(languageTag) { ... }` (expect/actual in `shared/`) which wraps the
  whole app in `App.kt`. Android overrides the composition `Configuration`/`Context` + `Locale.setDefault`;
  other platforms force re-composition (full switch may need a restart).
- The language list (tag + native name) is `core/presentation/.../AppLanguages.kt` (`appLanguages`).
- Pickers: **Settings → Language** (dialog) and the **first Onboarding page** (so the rest of
  onboarding renders in the chosen language). Both just write `AppSettings.language`.
- When adding a new language: add its `values-<lang>` folder AND an `AppLanguage(tag, nativeName)`
  entry to `appLanguages`.

### Supported languages (24)
Indian: `en hi te bn mr ta gu ur kn or ml pa as ne sa` · World: `zh es ar pt ru ja fr de in it`
- Only **2-letter ISO 639-1** qualifiers work. 3-letter-only languages (Maithili `mai`, Santali,
  Konkani, Dogri, Manipuri) are **not supported** by Compose resources — don't add `values-b+xxx`.
- RTL (`ur`, `ar`) mirroring is automatic.

### Intentionally English-only (don't worry about translating)
Long legal/marketing bodies in `feature/settings/.../SubScreens.kt` — About blurb, Privacy Policy
and Terms of Service paragraph bodies (`PolicySection(...)`), "Version 1.0.0", "Last updated…".
Month names come from `core/presentation/.../util/DateFormatter.kt` (English abbreviations) — not localized.

Full detail: **`docs/LOCALIZATION.md`**.

---

## 📘 MANDATORY: Swift learning notes (iOS work)

The user is **learning Swift/SwiftUI** by porting this app component-by-component from
Jetpack/Compose. Whenever you **build or change an iOS SwiftUI component**, you MUST also add or
update a teaching Markdown file under **`swift_learn/`**:

- **One concept per file**, named after the component: `navigation_bottom_bar.md`, `expense_card.md`,
  `settings_card.md`, etc. (Reuse/extend an existing file if the concept already has one.)
- Each file teaches **(a)** the Kotlin/Compose → Swift/SwiftUI conversion for that piece (side by
  side) and **(b)** how the Swift/SwiftUI part actually works (the language + framework concepts).
- Add a row to the index table in `swift_learn/README.md`.
- Do this **without being asked** — it's a standing requirement for all iOS/Swift changes.
- Follow the style of `swift_learn/00_swiftui_basics_hello.md` (code both ways, concept-by-concept
  breakdown, a "new Swift words" recap).

Big-picture iOS plan/status: **`docs/IOS_SWIFTUI_PLAN.md`** (read before any iOS/Swift work).

## Architecture

Kotlin Multiplatform + Compose Multiplatform (Android + iOS + JVM/desktop). Clean, modular MVI.

### Dependency direction
```
androidApp / shared (App.kt = nav host)
        ↓ depends on
feature/* (presentation only)
        ↓
core:design-system → core:presentation → core:domain
core:data → core:database, core:domain
```
- **`core:domain`** — pure Kotlin models, repository interfaces, `Result`/`DataError` util,
  `DateFilter`, `AppSettings`, `Category`, `Expense`, etc. (no Compose). JVM target pinned to 11.
- **`core:data`** — Room (`core:database`) + DataStore implementations of the domain repositories;
  `DateFilterHelper`, `PinHasher` (expect/actual SHA-256), Koin `CoreDataModule`.
- **`core:presentation`** — shared presentation utilities **and the string resource catalog**;
  `ObserveAsEvents`, `UiText`, `DateFormatter` (`toCurrencyString`, `toDisplayDate`…),
  `BiometricAuthenticator` interface + `LocalBiometricAuthenticator`.
- **`core:design-system`** — theme (`Color`, `Theme`, `Shape`, `Type`), and reusable components in
  `component/`: `Components.kt` (`ExpenseItemCard`, `CategoryCard`, `EmptyState`, `DateFilterRow`,
  `GradientIconTile`, `AnimatedProgressBar`), `Motion.kt` (`Modifier.bounceClick`), `PinPad.kt`
  (`PinEntryScreen` — shared by app-lock unlock & PIN setup).
- **`shared/App.kt`** — root + tab `NavHost`s, screen transitions, bottom navigation.
- **`androidApp`** — `MainActivity` (FragmentActivity; provides `AndroidBiometricAuthenticator`).

### Feature modules (each `feature/<name>/presentation`, commonMain, one main screen file)
| Module | Main file | Screens / contents |
|--------|-----------|--------------------|
| `dashboard` | `DashboardScreen.kt` | Dashboard, SubCategory drilldown, Filtered expenses (3 VMs) |
| `expenses` | `ExpensesScreen.kt`, `AddEditExpenseScreen.kt` | List + search; add/edit with date/time picker & budget guard |
| `analytics` | `AnalyticsScreen.kt` | Donut chart, stat cards, breakdown |
| `budget` | `BudgetScreen.kt` | Monthly + per-category budgets, allow-over-budget switch |
| `categories` | `CategoriesScreen.kt` | Manage categories + sub-categories (2 VMs) |
| `settings` | `SettingsScreen.kt`, `SubScreens.kt` | Settings list; About/Privacy/Terms, Notifications, App-lock setup, PIN setup flow |
| `applock` | `AppLockScreen.kt` | PIN/biometric unlock |
| `onboarding` | `OnboardingScreen.kt` | Pager intro |

### MVI pattern (every screen follows this)
- `data class XxxState`, `sealed interface XxxAction`, `sealed interface XxxEvent`.
- `XxxViewModel : ViewModel()` exposes `state: StateFlow` + `events: Channel.receiveAsFlow()`;
  one `onAction(action)` entry point. Registered with Koin `viewModelOf(::XxxViewModel)`.
- `XxxRoot(...)` composable: `koinViewModel()`, `ObserveAsEvents(vm.events)`, `collectAsState()`,
  delegates navigation via lambdas passed from `App.kt`.
- `XxxScreen(state, onAction)` is the stateless UI.

### Notable conventions / gotchas
- Currency is `₹` via `Double.toCurrencyString()` in `core:presentation` (single-currency today).
- `DateFilter` is `ThisWeek/ThisMonth/LastMonth/ThisYear/CustomRange`; the **Custom** chip + range
  picker live in `DateFilterRow` (design-system). Keep `DateFilterHelper.toDateRange` exhaustive.
- App lock: PIN always 6 digits; biometric is additive (stored as `BOTH`) with PIN fallback.
- Budgets are monthly → Dashboard only shows budget UI when `filter == ThisMonth`.

## Build & verify
```bash
./gradlew :shared:compileKotlinJvm                 # fast common-code check (all features)
./gradlew :androidApp:assembleDebug                # full Android APK
./gradlew :core:presentation:compileKotlinJvm      # regenerate string-resource accessors
```
> The iOS `Shared` framework now builds & links — verify iOS with
> `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.

## Docs
- `docs/LOCALIZATION.md` — localization deep dive (this is the source of truth for i18n).
- `docs/FEATURE_IDEAS.md` — growth/feature roadmap & status.
- `docs/IOS_SWIFTUI_PLAN.md` — **iOS/SwiftUI migration plan & context handoff.** Read this before
  any iOS/Swift work; it tracks the screen-by-screen SwiftUI rebuild (reusing shared KMP logic).
