# ExpenseTrackr — Feature Ideas & Growth Roadmap

Ideas to grow Play Store installs and ratings, derived from a scan of the current
`feature/` modules. Ratings drive store ranking, and ranking drives installs — so
several "retention/rating" items below are really install levers in disguise.

> Status legend: ✅ Done · 🚧 In progress · ⬜ Not started

---

## Current scope (baseline)

Expense-only tracking (name, amount, category/sub-category, notes, date), categories +
budgets, dashboard with date filters, analytics (donut + top category + avg/day),
monthly & per-category budgets with over-budget blocking, PIN/biometric app lock,
daily reminder setting, onboarding, fully offline/local. Currency hardcoded to ₹.

### Known gaps that became the ideas below
- No income / net balance (expense-only)
- No recurring / subscription expenses
- Single hardcoded currency (₹)
- **No backup / export** (data-loss risk for a local-only app)
- ~~Expenses can't be backdated (`expenseDate` always "now")~~ ✅ fixed
- No widget / quick-add
- No accounts / wallets, no payment methods
- No receipt attachments, no tags
- No localization beyond English

---

## 🔴 Critical — prevent uninstalls & 1-star reviews

These protect the rating, which is the ranking lever.

| # | Idea | Why it matters | Status |
|---|------|----------------|--------|
| 1 | **Backup & restore** (export/import JSON or encrypted file; optional Google Drive) | A local-only app with no backup = guaranteed data loss on phone switch → furious 1-star reviews. The single biggest review-killer for offline finance apps. | ⬜ |
| 2 | **Backdate / pick expense date & time** | Users constantly log expenses a day late ("forgot yesterday's lunch"). Forcing "today" is an instant frustration and top complaint. | ✅ |
| 3 | **CSV / PDF export of reports** | People hand data to a spouse, accountant, or Excel. Frequently demanded; common search term. | ⬜ |

---

## 🟠 High — broaden the audience (more eligible installers)

| # | Idea | Why it matters | Status |
|---|------|----------------|--------|
| 4 | **Multi-currency + currency picker** | Hardcoded ₹ caps the app to India. Even a symbol/locale selector (no FX) widens the market hugely; real FX for travelers is a bonus. | ⬜ |
| 5 | **Income tracking + net balance** | "Am I saving?" is half the story expense-only can't tell. Turns a logger into a *money manager* — a much bigger search category. | ⬜ |
| 6 | **Recurring / subscription expenses** | Auto-create rent, Netflix, EMIs. Strong ASO angle ("subscription tracker") and a retention driver. | ⬜ |
| 7 | **Home-screen widget + quick-add** | Logging friction is the #1 reason trackers get abandoned. 2-tap add keeps the habit alive → higher DAU → higher ranking. | ⬜ |

---

## 🟡 Retention & habit (keeps DAU high → ranking → installs)

| # | Idea | Why it matters | Status |
|---|------|----------------|--------|
| 8 | **Streaks & gentle gamification** | "7-day logging streak." Habit loops keep finance apps sticky; daily-reminder plumbing already exists. | ⬜ |
| 9 | **Smart insights / month-over-month trends** | "You spent 32% more on Food than last month." Feels intelligent and is screenshot-worthy (organic sharing). | ⬜ |
| 10 | **Spending calendar / heatmap** | Month grid colored by daily spend. Very visual, great store screenshot, matches how users think about a month. | ⬜ |
| 11 | **Budget alert notifications** | `budgetAlertEnabled` exists in settings but drives nothing yet. "You've used 80% of Food budget" is a proven re-engagement nudge. | ⬜ |

---

## 🟢 Differentiators & ASO ammo

| # | Idea | Why it matters | Status |
|---|------|----------------|--------|
| 12 | **Accounts/wallets + payment methods** (Cash / Card / UPI / Bank) | Reconcile against real balances; "UPI" resonates strongly in India. | ⬜ |
| 13 | **Receipt photo attachments** | Snap a bill, attach to an expense. Tangible feature for screenshots and tax time. | ⬜ |
| 14 | **Tags** (cross-cutting labels like #trip #reimbursable) | Power-user retention beyond fixed categories. | ⬜ |
| 15 | **Lean into "100% private & offline"** | The genuine moat vs. cloud apps that sell data. Make it the #1 store screenshot/description line. | ⬜ |
| 16 | **Localization** (Hindi + 3–4 major languages) | Each language unlocks a new store-listing audience and locale ASO. High install-per-effort ratio. | 🚧 Infra + 24 languages wired; common UI translated, per-screen extraction ongoing. See [LOCALIZATION.md](LOCALIZATION.md) |

---

## Recommended order (max install ROI)

1. **Backup / restore** (#1) — protect the rating
2. ~~**Backdate expenses** (#2)~~ ✅ done
3. **Recurring expenses** (#6) — retention + ASO
4. **Income + balance** (#5) — expand what the app *is*
5. **Widget / quick-add** (#7) — daily habit

> #1 and #2 protect the rating (the ranking lever). #5–#7 expand what the app does and
> how often it's opened.

---

## Two cheap ASO multipliers (do regardless of features)

- **Screenshots**: the donut chart, a spending heatmap, and budget bars are screenshot
  gold — a strong first two screenshots can lift store conversion 20–30%.
- **Keywords**: adding income / subscriptions / multi-currency lets the listing legitimately
  rank for "budget planner", "subscription tracker", "money manager" — far higher-volume
  searches than "expense tracker" alone.
