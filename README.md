<div align="center">

# 💰 ExpenseTrackr

### A privacy-first, offline personal finance tracker built with Kotlin Multiplatform & Compose Multiplatform

**One codebase → Android, iOS, and Desktop.**

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop-3D52A0)
![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Compose%20Multiplatform-4285F4)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVI-00897B)
![Languages](https://img.shields.io/badge/Localized-24%20languages-FF7043)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

---

## 🚦 Platform Status

| Platform | Status | Notes |
|----------|--------|-------|
| 🤖 **Android** | ✅ **Complete** | In **Google Play closed testing** — public release coming soon |
| 🍎 **iOS** | 🚧 **In progress** | Runs the shared Compose app; native SwiftUI port underway |
| 🖥️ **Desktop (JVM)** | 🚧 **In progress** | Compose desktop target wired; polishing in progress |

> 📱 **Android is feature-complete and going live on the Play Store shortly.** iOS and Desktop share the
> same business logic and are being finished next.

---

<div align="center">

<!-- 📌 APP BANNER GOES HERE -->
<!-- Replace the line below with your banner image, e.g. ![ExpenseTrackr Banner](docs/banner.png) -->

**_[ App banner will be placed here ]_**

</div>

---

## 📖 Overview

**ExpenseTrackr** helps you record, categorize, budget, and analyze your personal finances — with your
data staying **on your device**. There are no accounts and no cloud sync of your financial records: every
expense, income, budget, and category lives in a local database you fully own.

It's built as a **single Kotlin Multiplatform codebase** that renders a native UI on Android, iOS, and
Desktop via **Compose Multiplatform**, following a **clean, modular MVI architecture**.

### Why it stands out
- **True multiplatform** — domain, data, and UI shared across 3 platforms from one codebase (~10k lines of Kotlin, 16 Gradle modules).
- **Offline-first & private** — financial data never leaves the device; only anonymous crash/usage diagnostics are collected (opt-in-friendly, no ads, no data selling).
- **Production-grade engineering** — clean architecture, MVI, dependency injection, custom Gradle convention plugins, R8 minification, and a real Play Store release pipeline.
- **Fully localized** — 24 languages with an in-app language switcher (no app restart required).

---

## ✨ Features

### 💸 Track money in & out
- Log **expenses and income** with name, amount (₹), category, sub-category, date/time, and notes.
- **Income & salary tracking** kept separate from spend math, so budgets/analytics stay accurate.
- Add an expense in **3 taps or fewer** from anywhere.

### 🗂️ Flexible organization
- **9 pre-seeded categories** + unlimited custom categories (emoji icon + color).
- **Sub-categories** under any category (e.g. Food → Tea, Breakfast).
- Safe deletes: warns when a category/sub-category still has linked transactions.

### 📊 Insightful analytics
- **Interactive donut chart** — tap a slice to highlight the category and see its amount & share.
- **Spent-vs-Saved** breakdown and **spending-over-time** bar chart.
- Stat cards: top category, average daily spend, income, savings rate.

### 🎯 Budgeting
- **Overall monthly budget** + optional **per-category budgets**.
- Color-coded progress (green / yellow / red) and pre-overspend warnings.
- "Allow over budget" option for flexibility.

### 🔍 Powerful filtering
- This Week · This Month · Last Month · This Year · **Custom range**.
- Filter state persists per screen.

### 💾 Backup & Sync _(new)_
- Export everything to **CSV** — `expenses.csv` + `incomes.csv` — into an **ExpenseTrackr** folder in
  shared storage that **survives clearing app data / reinstalling**.
- **Restore/merge** from those files, de-duplicated by database ID. Categories are stored as text and
  recreated on import, so a backup is fully self-sufficient and readable in Excel/Sheets.

### 🔐 Security
- **App lock** with a 6-digit PIN and optional **biometric** (fingerprint/face) unlock with PIN fallback.
- PIN stored as a **SHA-256 hash**; biometrics handled by the OS (no biometric data stored).

### 🌐 Localization
- **24 languages** (Indian + world), including English, Hindi, Telugu, Tamil, Bengali, Marathi, Gujarati,
  Kannada, Malayalam, Punjabi, Urdu, Chinese, Spanish, Arabic, and more.
- **In-app language switcher** that overrides the system locale (RTL supported).

### 🎨 Experience
- **Material 3 (Material You)** design, light/dark/system themes, dynamic color on Android 12+.
- Smooth transitions, bounce-tap feedback, and thoughtful empty states.
- **Share App** action and a guided **onboarding** flow.

### 📈 Reliability
- **Firebase Crashlytics + Analytics** (Android) for anonymous crash reports and aggregate usage — used
  only to improve stability, never your financial data.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin (Multiplatform), Swift (iOS host) |
| **UI** | Compose Multiplatform, Material 3, Material Icons Extended |
| **Architecture** | Clean Architecture + MVI (State / Action / Event), unidirectional data flow |
| **DI** | Koin (core + compose-viewmodel) |
| **Navigation** | Compose Navigation with type-safe, `@Serializable` routes |
| **Persistence** | Room (KMP) + `androidx.sqlite` bundled driver; DataStore (preferences) |
| **Async** | Kotlinx Coroutines & Flow |
| **Serialization / Time** | kotlinx.serialization, kotlinx.datetime |
| **Logging** | Kermit |
| **Diagnostics (Android)** | Firebase Crashlytics & Analytics |
| **Build** | Gradle (Kotlin DSL), version catalog, **custom convention plugins** (`build-logic`), R8 minify + resource shrinking |

---

## 🏗️ Architecture

A **clean, modular MVI** design with a strict one-way dependency direction:

```
androidApp / iosApp / desktopApp
        │  (host + platform bridges)
shared (App.kt — Compose nav host)
        │
feature/* (presentation only — one screen module each)
        │
core:design-system → core:presentation → core:domain
core:data → core:database, core:domain
```

- **`core:domain`** — pure Kotlin: models, repository interfaces, `Result`/`DataError`, `DateFilter`, `AppSettings`.
- **`core:data`** — Room + DataStore implementations; `PinHasher` (expect/actual SHA-256), Koin modules.
- **`core:presentation`** — shared UI utilities, the **string-resource catalog**, `BiometricAuthenticator` & `ShareHandler` platform bridges.
- **`core:design-system`** — theme + reusable components (cards, charts helpers, PIN pad, motion).
- **`shared`** — root Compose `App()` + navigation; hosts the app on all platforms.
- **`feature/*`** — 8 feature modules, each a self-contained MVI screen set.

**Every screen follows the same MVI contract:** `XxxState` · `XxxAction` · `XxxEvent` · `XxxViewModel` (exposes `StateFlow` + event `Channel`, single `onAction`) · a stateless `XxxScreen` · a `XxxRoot` that wires DI + navigation.

**Multiplatform abstractions** are handled cleanly via `expect`/`actual` (DB builder, DataStore, PIN hashing, locale) and Compose `CompositionLocal` bridges (biometrics, sharing) with per-platform implementations.

### Feature modules

| Module | Screens |
|--------|---------|
| `dashboard` | Category dashboard, sub-category drilldown, filtered expenses |
| `expenses` | Expense list + search, add/edit with date-time picker & budget guard |
| `analytics` | Donut chart, spent-vs-saved, spending-over-time, stat cards |
| `budget` | Monthly + per-category budgets, allow-over-budget |
| `categories` | Manage categories & sub-categories |
| `settings` | Settings, About/Privacy/Terms, notifications, app-lock, language, **Backup & Sync**, **Share** |
| `applock` | PIN / biometric unlock |
| `onboarding` | Language picker + intro pager |

**At a glance:** 16 Gradle modules · 18 screens · 15 ViewModels · 24 languages.

---

## 📦 Data Model

- **Category** — `id`, name, icon (emoji), colorHex, isDefault, budgetAmount?, type (EXPENSE/INCOME), createdAt
- **SubCategory** — `id`, name, categoryId (FK), createdAt
- **Expense** — `id`, name, amount, categoryId (FK), subCategoryId? (FK), notes?, **type (EXPENSE/INCOME)**, expenseDate, createdAt
- **AppSettings** (DataStore) — theme, language, app-lock type & PIN hash, lock timeout, reminders, budgets, onboarding flag

> Income and expenses share one table, discriminated by `type`, keeping the schema simple while separating the two in all spend/budget/analytics math.

---

## 🔒 Privacy

- **Your money data stays on your device** — expenses, income, and budgets are never uploaded or shared.
- The only data leaving the device is **anonymous crash reports and aggregate usage analytics** (Firebase),
  used solely to improve reliability — never the content of your financial records. No ads, no data selling.
- Full policy: [`docs/privacy-policy.html`](docs/privacy-policy.html).

---

## ▶️ Build & Run

**Requirements:** JDK 11+, Android Studio (latest), Xcode (for iOS), and a `google-services.json` in
`androidApp/` (from your own Firebase project) for the Android build.

```bash
# Fast common-code check (compiles all shared/feature code)
./gradlew :shared:compileKotlinJvm

# Android debug APK
./gradlew :androidApp:assembleDebug

# Android release bundle (signed — see keystore.properties)
./gradlew :androidApp:bundleRelease

# iOS framework link check
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Desktop app
./gradlew :desktopApp:run
```

**iOS:** open `iosApp/iosApp.xcodeproj` in Xcode, pick a simulator, and Run. The shared `Shared`
framework builds via a Gradle build phase, Koin starts from `App.init()`, and the shared Compose UI loads.

---

## 🗺️ Roadmap

- ✅ Android feature-complete → Play Store closed testing → **public release (soon)**
- 🚧 iOS: native SwiftUI screen-by-screen port (currently hosting the shared Compose app)
- 🚧 Desktop (JVM) polish
- 🔜 Recurring transactions, home-screen widgets, richer charts

---

## 👤 Author

**Revanth Kumar Jilakara**
📧 jrevanth101@gmail.com · 🐙 [github.com/revanthkumarJ](https://github.com/revanthkumarJ)

---

## 📄 License

Released under the **MIT License**.

<div align="center">

_Built with ❤️ using Kotlin Multiplatform & Compose Multiplatform._

</div>
