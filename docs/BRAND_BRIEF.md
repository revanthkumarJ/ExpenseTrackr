# ExpenseTrackr — Project & Brand Brief

> **Purpose of this document:** a self-contained brief you can hand to an AI (or a designer)
> to generate **app icons, splash screens, store graphics, illustrations, and other visual
> assets**. It describes what the app is, who it's for, its personality, and its exact visual
> identity (colors, type, motifs), followed by concrete asset specs.

---

## 1. The app in one line

**ExpenseTrackr** — a private, offline-first personal expense tracker.
**Tagline:** *Track every rupee. Stay in control.*

## 2. Elevator pitch

ExpenseTrackr is a beautifully simple money tracker that lives entirely on your device — no
accounts, no servers, no ads, no data ever leaving the phone. You log expenses in seconds,
organize them into colorful categories and sub-categories, set monthly and per-category budgets,
track your salary, and see where your money goes through clean charts (a category donut, a
spending-trend bar chart, and spent-vs-saved bars). It's fast, calm, and respects your privacy.

## 3. Platforms & tech

- **Kotlin Multiplatform + Compose Multiplatform**: Android, iOS, and JVM/desktop from one codebase.
- Material 3 design language, light **and** dark themes, dynamic color on supported Android.
- Fully **localized in 24 languages** (Indian + world languages), RTL-aware.
- **Offline-first**, local database only. Version 1.0.0. Developer: **RevanthDev**.

## 4. Audience & positioning

- **Primary audience:** India-first, everyday individuals who want to understand and control
  personal spending. Currency is the Indian Rupee **₹** (single-currency, Indian digit grouping,
  e.g. `₹12,34,567.89`).
- **Positioning:** the *trustworthy, private, no-nonsense* alternative to cloud finance apps.
  Privacy and calm are the differentiators — not gamification, not social, not "hustle."

## 5. Brand personality

| Trait | Means (for visuals) |
|-------|---------------------|
| **Trustworthy & secure** | Deep indigo, lock motifs, solid geometry. Never chaotic. |
| **Calm & clean** | Generous whitespace, soft rounded cards, restrained accent use. |
| **Modern & friendly** | Material 3 rounded shapes, gentle gradients, tasteful emoji. |
| **Confident, not flashy** | Flat/soft-3D, no heavy skeuomorphism, no clutter, no ads vibe. |

**Tone words:** private, calm, clear, dependable, effortless.
**Avoid:** aggressive greens/"cash" clichés, dollar signs ($), coins-raining, casino/luck imagery,
busy dashboards, neon, corporate-bank stiffness.

## 6. Core features (context for illustration & store art)

- **Dashboard** — period total, budget progress bar, salary-remaining card, category cards.
- **Expenses** — quick add/edit with date/time, search, grouped-by-day list.
- **Analytics** — category **donut chart**, **spending-trend bar chart** (by day/week/month for the
  selected period), spent-vs-saved bars, stat tiles.
- **Budgets** — overall monthly budget + per-category budgets, allow/deny over-budget.
- **Categories & sub-categories** — colorful, emoji-led, user-manageable.
- **Salary tracking** — set monthly income, see savings rate.
- **App lock** — 6-digit PIN + optional biometric unlock.
- **Onboarding** — language picker + intro pager.

## 7. Visual identity

### 7.1 Signature symbol & motifs
- **Primary app symbol:** the money-bag emoji **💰** is used as the app's mark throughout
  (About screen, onboarding). A stylized **money bag** and/or a **downward analytics/trend
  element** and a **rupee ₹** are the go-to motifs.
- Supporting motifs: **shield/lock** (privacy), **donut/pie ring** (analytics), **rounded bars**
  (trend chart), **soft rounded-square** container shapes.
- **Emoji vocabulary** already in the product: 💰 (brand), 📂 track, 📊 analytics, 🔒 privacy,
  🌐 language, 🗂️ categories, 🏷️ sub-categories. Categories use emoji like 🍔 ✈️ etc.

### 7.2 Color palette (exact values)

**Brand core**
| Role | Light | Dark |
|------|-------|------|
| **Primary — Indigo** | `#3D52A0` | `#7091E6` |
| **Secondary — Teal** | `#00897B` | `#4DB6AC` |
| **Tertiary — Pink/Magenta** | `#B8467E` | `#FFB0CC` |

**Primary/secondary containers (soft tints for backgrounds & shapes)**
- Primary container: light `#DFE3FF` / dark `#273573` · on-primary-container `#0C1565` / `#DFE3FF`
- Secondary container: light `#B6F2EA` / dark `#005048`
- Tertiary container: light `#FFD9E5` / dark `#8E2A5E`

**Surfaces & backgrounds**
- Background: light `#F6F7FB` / dark `#111318`
- Surface: light `#FFFFFF` / dark `#1A1C22`
- Error: light `#D32F2F` / dark `#EF9A9A`

**Status colors (budgets / trends)**
- Green (under budget / saved): `#388E3C` · Yellow (nearing): `#F57F17` · Red (over): `#C62828`

**Category accent palette** (vivid, used for category tiles/charts)
`#FF6D00` `#1565C0` `#00796B` `#C62828` `#6A1B9A` `#AD1457` `#283593` `#F9A825` `#616161`

**Gradients used in-app:** primary-container → secondary-container (soft indigo→teal) for hero
circles/tiles. Keep gradients subtle and low-contrast.

### 7.3 Icon / splash color direction
- **Hero recipe:** deep **Indigo `#3D52A0`** as the anchor, a **Teal `#00897B`** accent, on a
  clean light `#F6F7FB` or white ground (light) — and the brighter indigo `#7091E6` mark on the
  dark `#111318` ground (dark). Optional soft indigo→teal gradient.
- App icon should read clearly at small sizes: **one bold, simple mark** (money bag / ₹ / trend
  bar), not a scene. Rounded-square friendly; keep it centered with safe margins.

### 7.4 Shape & type
- **Shapes:** Material 3 rounded — small radius ~8dp, medium ~12dp, large ~16–20dp; app uses
  soft rounded-square containers and pill chips. Corners are always rounded, never sharp.
- **Type:** Material 3 default type scale (clean geometric sans, e.g. Inter/Roboto-like).
  Numbers are prominent (amounts in headline/title weight). No decorative or script fonts.

## 8. Assets to generate (specs)

> Use the palette above. Provide **light and dark** variants where noted. Keep the mark simple and
> legible at 24px. No text baked into the app icon.

**App icon**
- **iOS:** 1024×1024 px, square, no transparency, no rounded corners (system rounds it).
- **Android adaptive:** foreground 108×108dp (safe zone 66dp centered) + background layer;
  export 432×432 px foreground/background PNGs. Also a 512×512 Play Store icon.
- **Monochrome/notification icon:** single-color, transparent, simple silhouette of the mark.

**Splash screen**
- **Android 12+ splash** = centered icon on a solid brand background. Provide: centered logo mark
  (vector/PNG, ~288×288 within a 432 canvas) + background colors light `#F6F7FB` / dark `#111318`
  (or brand indigo `#3D52A0`). Keep it a single centered mark — the system animates it.
- **Generic/full splash (optional):** portrait 1080×1920 & 1284×2778, mark centered, tagline
  optional below, brand gradient or solid background, plenty of breathing room.

**Store / marketing**
- Play **feature graphic** 1024×500 (mark + tagline on brand background).
- App Store / Play **screenshots frames** and promo banners in-palette (indigo/teal, rounded cards).
- Optional **onboarding illustrations** for the 4 intro pages: welcome 💰, tracking 📂,
  analytics 📊, privacy 🔒 — flat, friendly, brand-colored.

## 9. Do / Don't (quick rules for the generator)

**Do:** deep indigo + teal, soft rounded shapes, generous space, one clear mark, light & dark
variants, ₹ rupee if a currency glyph is shown, calm and trustworthy mood.

**Don't:** use `$`, green "cash"/coins-raining clichés, neon, heavy gradients, drop-shadow-heavy
skeuomorphism, busy scenes, sharp corners, stocky "corporate finance" photography, or any imagery
implying data is uploaded to the cloud (the app is proudly **offline & private**).

---

### Ready-to-paste prompt seed

> Design a modern, friendly **app icon for "ExpenseTrackr"**, a private offline personal expense
> tracker. Single bold mark combining a **money bag** with a subtle **rupee ₹** / upward-calm
> **bar-chart** motif. Palette: deep indigo **#3D52A0** with a teal **#00897B** accent, optional
> soft indigo→teal gradient, on a clean light **#F6F7FB** ground; also provide a dark variant using
> indigo **#7091E6** on **#111318**. Rounded-square friendly, flat/soft-3D, legible at small sizes,
> centered with safe margins, no text. Mood: private, calm, trustworthy.
