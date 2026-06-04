# Product Requirements Document (PRD)
## Expense Tracker — KMP Application (Android,IOS, Desktop)

**Version:** 1.0.0  
**Status:** Draft  
**Last Updated:** 2026-06-04  
**Platform:** KMP (Android,Ios,Desktop)  
**Currency:** INR (₹)  
**Data Storage:** Fully Offline — Room Database (Local)

---

## 1. Product Overview

### 1.1 Product Summary

Expense Tracker is a fully offline, privacy-first Android application that empowers users to record, categorize, analyze, and manage their personal expenses — entirely on-device. No internet connection is required, no data is ever transmitted to any server, and users retain complete ownership of their financial data.

### 1.2 Problem Statement

Most expense tracking apps either require cloud accounts (privacy concern), are too complex for daily use, or lack meaningful analytics. Users need a simple, beautiful, and trustworthy tool that works without internet, respects their privacy, and provides clear financial insights.

### 1.3 Target Audience

- Individuals tracking personal daily expenses
- People mindful of data privacy
- Users who want offline-first financial tools
- Anyone managing household or personal budgets in India

### 1.4 Key Value Propositions

- **100% Offline** — No internet, no cloud, no accounts required
- **Privacy First** — All data stays on-device, always
- **Beautiful UI** — Stunning, modern design with dark/light mode
- **Smart Analytics** — Pie charts, category breakdowns, budget tracking
- **Flexible Structure** — Customizable categories and sub-categories

---

## 2. App Screens & Navigation

### 2.1 Navigation Structure

The app uses a **Bottom Navigation Bar** with 4 main tabs:

| Tab | Icon | Screen Name |
|-----|------|-------------|
| 1 | 📊 | Dashboard (Category View) |
| 2 | 📋 | All Expenses |
| 3 | 📈 | Analytics |
| 4 | ⚙️ | Settings & More |

A **Floating Action Button (FAB)** is globally accessible on tabs 1 and 2 to quickly add an expense.

---

## 3. Functional Requirements

### 3.1 Onboarding (First Launch Only)

**Trigger:** Shown only on first app launch. Never shown again after completion.

**Screens:**

| Slide | Title | Description |
|-------|-------|-------------|
| 1 | Welcome to Expense Tracker | Brief intro — offline, private, beautiful |
| 2 | Track Your Spending | Explain categories and expense logging |
| 3 | Smart Analytics | Explain pie charts and budget alerts |
| 4 | Your Data, Your Device | Emphasize privacy — no cloud, no accounts |

**Controls:** Skip button (top right), Next button, dot indicators, and a Get Started button on the last slide.

---

### 3.2 App Lock (Security)

**Options available to user (configurable in Settings):**

- **PIN Lock** — 4–6 digit numeric PIN set by the user
- **Biometric Lock** — Fingerprint / Face Unlock using Android BiometricPrompt API
- **Both** — Biometric with PIN fallback
- **None** — No lock (default)

**Behavior:**

- Lock screen appears when app is reopened after being backgrounded for more than 1 minute (configurable)
- Forgot PIN → user must clear app data (stated clearly in UI)
- Biometric failure falls back to PIN

---

### 3.3 Categories

#### 3.3.1 Default Categories (Pre-loaded)

| # | Category Name | Icon | Color |
|---|---------------|------|-------|
| 1 | Food & Dining | 🍔 | Orange |
| 2 | Rent & Housing | 🏠 | Blue |
| 3 | Travel | ✈️ | Teal |
| 4 | Medicines & Health | 💊 | Red |
| 5 | Entertainment | 🎬 | Purple |
| 6 | Shopping | 🛍️ | Pink |
| 7 | Education | 📚 | Indigo |
| 8 | Utilities | ⚡ | Yellow |
| 9 | Others | 📦 | Grey |

#### 3.3.2 Custom Categories

- User can **create** new categories with: Name + Icon (emoji picker) + Color
- User can **edit** existing custom categories
- User can **delete** custom categories (only if no expenses are linked; else warn user)
- Default categories cannot be deleted but can be renamed

#### 3.3.3 Sub-Categories

- No default sub-categories (user-defined only)
- Each sub-category **must belong to one parent category**
- Examples: Food → Tea, Breakfast, Biryani | Rent → PG, Flat
- User can **create, edit, and delete** sub-categories
- Deleting a sub-category with linked expenses: prompt user — detach or cancel

---

### 3.4 Add / Edit Expense

#### 3.4.1 Add Expense Screen (Bottom Sheet or Full Screen)

**Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Expense Name | Text input | Yes | Max 100 characters |
| Amount (₹) | Numeric input | Yes | Decimal supported, 2 decimal places |
| Category | Dropdown / Picker | Yes | Shows all categories |
| Sub-Category | Dropdown / Picker | No | Filtered by selected category; auto-selects category if sub-category chosen first |
| Date & Time | Date-Time Picker | Yes | Defaults to current date & time; fully editable |
| Notes | Text area | No | Optional notes, max 300 characters |

**Behavior:**

- If sub-category is selected first, parent category auto-populates and locks
- Amount field shows ₹ prefix
- Validation: Amount must be > 0, Category must be selected
- Save button disabled until required fields are filled

#### 3.4.2 Edit Expense

- All fields editable
- Shows original creation timestamp as reference
- Delete option available (with confirmation dialog)

---

### 3.5 Screen 1 — Dashboard (Category View)

**Purpose:** This month's expenses, organized by category.

**Default View:** Current month

**Header:**
- Month/filter label (e.g., "June 2026")
- Total spend amount (prominent)
- Filter button (top right)

**Content:**

- List of **Category Cards**, each showing:
    - Category icon + name
    - Total amount spent in that category
    - Progress bar relative to budget (if budget is set)
    - Percentage of total spend

**On Category Card Tap:**

→ Opens **Sub-Category View** for that category:
- Lists all sub-categories with their totals
- A row for "Uncategorized" (expenses with no sub-category)

**On Sub-Category Tap:**

→ Opens **Expense List View** filtered by that category + sub-category

**On "Uncategorized" row tap:**

→ Opens **Expense List View** showing expenses in that category with no sub-category

---

### 3.6 Screen 2 — All Expenses

**Purpose:** Flat list of all expenses, most recent first.

**Features:**

- Grouped by date (e.g., "Today", "Yesterday", "June 3")
- Each expense row shows: Name, Category chip, Sub-category chip (if any), Amount, Time
- Swipe left on expense → Delete (with undo snackbar)
- Tap on expense → Edit Expense screen
- Search bar at top (search by name, category, or amount)
- Filter button (top right)

---

### 3.7 Filter System (Shared across Screens 1 & 2)

**Available Filters:**

| Filter | Description |
|--------|-------------|
| This Week | Current week (Mon–Sun) |
| This Month | Current calendar month |
| Last Month | Previous calendar month |
| This Year | Current calendar year |
| Custom Range | User picks start and end date via date picker |

- Filter state persists per screen separately
- Chip/badge shown on screen indicating active filter
- "Clear Filter" option resets to "This Month" (default)

---

### 3.8 Screen 3 — Analytics

**Purpose:** Visual breakdown of spending.

#### 3.8.1 Analytics Header

- Filter selector (same options as Filter System above)
- Total spend for selected period (prominent)

#### 3.8.2 Category Pie Chart

- Interactive pie chart showing spend by category
- Tap on a slice → highlights that category and shows detail below
- Legend below chart with category name, amount, and percentage

#### 3.8.3 Sub-Category Breakdown (Per Category)

- Below the main pie chart, a **horizontal scrollable row of category tabs**
- Selecting a category tab shows a secondary pie chart for its sub-categories
- Sub-categories with no spend are excluded

#### 3.8.4 Summary Stats

- Highest spending category
- Average daily spend
- Most frequent expense name

---

### 3.9 Budget Management

#### 3.9.1 Overall Monthly Budget

- User sets a single monthly total budget (₹)
- Dashboard header shows: Spent / Total Budget with a progress bar
- Color coding: Green (< 70%), Yellow (70–90%), Red (> 90%)

#### 3.9.2 Per-Category Budget

- User can set a monthly budget per category (optional)
- Shown on each category card as a progress bar
- Same color-coded thresholds as above

#### 3.9.3 Budget Settings Screen

- Accessible from Settings or directly from Dashboard
- Toggle to enable/disable overall budget
- List of categories with optional budget amount fields
- Reset all budgets option

---

### 3.10 Notifications (Local Only)

#### 3.10.1 Daily Expense Reminder

- Configurable daily reminder: "Don't forget to log today's expenses!"
- User sets preferred time (default: 9:00 PM)
- Toggle to enable/disable in Settings

#### 3.10.2 Budget Alert Notification

- Triggered when spending crosses 80% of either:
    - Overall monthly budget, OR
    - Any per-category budget
- Notification body: "You've used 80% of your [Category] budget for [Month]."
- Not repeated until next threshold (100%) is crossed
- Requires notification permission (Android 13+)

---

### 3.11 Screen 4 — Settings & More

**Sections:**

#### Preferences
- Dark Mode toggle (Light / Dark / System Default)
- App Lock settings (PIN / Biometric / None)
- Auto-lock timeout (1 min / 5 min / 10 min)
- Notification settings (Daily reminder toggle + time picker, Budget alerts toggle)

#### Budget
- Shortcut to Budget Management screen

#### Data Management
- Manage Categories (create, edit, delete)
- Manage Sub-Categories (create, edit, delete)

#### App Info
- About screen
- Privacy Policy
- Terms of Service
- Open Source Licenses
- App Version
- Rate the App (links to Play Store)
- Contact / Support (email link)

---

### 3.12 About / Privacy / Legal Screens

These are required for Play Store publication:

#### About
- App name, version, tagline
- Developer/company name
- Brief description

#### Privacy Policy
- Data collection: None (fully offline)
- No third-party SDKs that collect data
- No analytics, no crash reporting sent externally
- Statement that all data is stored locally on device

#### Terms of Service
- Standard ToS for a free app
- Disclaimer of liability for financial decisions

All screens accessible from Settings and from Play Store listing URL.

---

## 4. Non-Functional Requirements

### 4.1 Performance

- App cold start: < 2 seconds on mid-range device
- Expense list with 1,000+ entries: scroll at 60fps
- Database queries: < 100ms for all filtered reads
- Chart rendering: < 500ms

### 4.2 Offline

- 100% functionality without internet
- No network calls of any kind (except optional Play Store rate link)
- No Firebase, no analytics SDKs, no crash reporting

### 4.3 Storage

- Room database stored in internal app storage (not accessible without root)
- Database size estimate: < 5MB for 10,000 expenses

### 4.4 Security

- PIN stored as SHA-256 hash in EncryptedSharedPreferences
- Room database optionally encrypted with SQLCipher (if app lock is enabled)
- Biometric uses Android BiometricPrompt — no biometric data stored by the app

### 4.5 Compatibility

- Minimum SDK: Android 8.0 (API 26)
- Target SDK: Android 15 (API 35)
- Screen sizes: Phones (compact + medium); Tablets supported but not primary

### 4.6 Accessibility

- All interactive elements have content descriptions
- Minimum touch target: 48×48dp
- Text scales with system font size settings
- Color choices pass WCAG AA contrast ratio

---

## 5. UI / UX Requirements

### 5.1 Design Language

- **Design System:** Material Design 3 (Material You)
- **Typography:** Clean, readable sans-serif (Nunito or Inter)
- **Iconography:** Material Symbols Rounded
- **Animations:** Subtle, meaningful transitions (shared element transitions, fade-ins)
- **Elevation:** Card-based layout with soft shadows

### 5.2 Theming

| Mode | Trigger |
|------|---------|
| Light Mode | Manual toggle or system default |
| Dark Mode | Manual toggle or system default |
| System Default | Follows Android system theme |

- Theme preference saved in SharedPreferences
- Dynamic Color (Material You wallpaper-based theming) supported on Android 12+

### 5.3 Color Palette (Base)

| Role | Light | Dark |
|------|-------|------|
| Primary | Deep Indigo #3D52A0 | Soft Indigo #7091E6 |
| Secondary | Teal #00897B | Light Teal #4DB6AC |
| Background | #F8F9FA | #121212 |
| Surface | #FFFFFF | #1E1E1E |
| Error | #D32F2F | #EF9A9A |

### 5.4 Key UX Principles

- **Thumb-friendly:** Primary actions reachable with one thumb (FAB, bottom nav)
- **Minimal taps:** Add expense in 3 taps or fewer from any screen
- **Forgiving:** Delete with undo, confirmations for destructive actions
- **Informative empty states:** Helpful illustrations and CTAs when no data exists

---

## 6. Data Model (High Level)

### 6.1 Entities

**Category**
- id (PK)
- name
- icon (emoji string)
- colorHex
- isDefault (Boolean)
- budgetAmount (nullable ₹)
- createdAt

**SubCategory**
- id (PK)
- name
- categoryId (FK → Category)
- createdAt

**Expense**
- id (PK)
- name
- amount (Double)
- categoryId (FK → Category)
- subCategoryId (nullable FK → SubCategory)
- notes (nullable)
- expenseDate (DateTime — user-selected)
- createdAt (DateTime — system-generated)

**AppSettings**
- id (PK, always 1)
- isDarkMode (nullable — null = system)
- appLockType (NONE / PIN / BIOMETRIC / BOTH)
- pinHash (nullable)
- lockTimeoutMinutes
- dailyReminderEnabled
- dailyReminderTime
- budgetAlertEnabled
- overallMonthlyBudget (nullable)
- isOnboardingDone

---

## 7. Screens Summary Table

| # | Screen | Accessible From |
|---|--------|----------------|
| 1 | Splash Screen | App launch |
| 2 | Onboarding (4 slides) | First launch only |
| 3 | App Lock / PIN Entry | Every launch (if enabled) |
| 4 | Dashboard (Category View) | Bottom Nav Tab 1 |
| 5 | Sub-Category View | Tap category on Dashboard |
| 6 | Expense List (filtered) | Tap sub-category |
| 7 | All Expenses | Bottom Nav Tab 2 |
| 8 | Add Expense | FAB (global) |
| 9 | Edit / Delete Expense | Tap expense row |
| 10 | Analytics | Bottom Nav Tab 3 |
| 11 | Settings & More | Bottom Nav Tab 4 |
| 12 | Manage Categories | Settings |
| 13 | Manage Sub-Categories | Settings |
| 14 | Budget Management | Settings / Dashboard |
| 15 | Notification Settings | Settings |
| 16 | App Lock Setup | Settings |
| 17 | About | Settings |
| 18 | Privacy Policy | Settings / Play Store |
| 19 | Terms of Service | Settings |

---

## 8. Out of Scope (v1.0)

The following features are explicitly excluded from v1.0:

- Data export (CSV / PDF)
- Cloud backup or sync
- Multiple currencies
- Recurring / scheduled expenses
- Home screen widgets
- Income tracking
- Loan or debt tracking
- Multiple user profiles
- Web or iOS version

---

## 9. Success Metrics (Post-Launch)

| Metric | Target |
|--------|--------|
| Play Store Rating | ≥ 4.5 stars |
| Day-7 Retention | ≥ 40% |
| Crash-free rate | ≥ 99.5% |
| ANR rate | < 0.1% |
| Average session length | ≥ 2 minutes |

---

## 10. Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| User loses PIN, cannot access data | Clear warning in UI that PIN recovery requires app data reset |
| Room DB corruption on low-end devices | WAL mode enabled; periodic integrity checks |
| Notification permission denied (Android 13+) | Graceful fallback; prompt user with rationale |
| App killed by aggressive battery optimization | Document workaround in onboarding/FAQ |

---

*End of PRD v1.0 — Next document: Architecture & Technology Stack (ARCHITECTURE.md)*