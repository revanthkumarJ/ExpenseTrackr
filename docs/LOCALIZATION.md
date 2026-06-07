# Localization

ExpenseTrackr uses **Compose Multiplatform string resources**. All app strings live in one
central module so every feature, the design system, and the app shell can share them.

## Where strings live

```
core/presentation/src/commonMain/composeResources/
├── values/strings.xml          ← English (source of truth / fallback)
├── values-hi/strings.xml       ← Hindi
├── values-te/strings.xml       ← Telugu
└── values-<lang>/strings.xml   ← one folder per language
```

The generated accessor class is exposed publicly (`core/presentation/build.gradle.kts`):

```kotlin
compose.resources {
    publicResClass = true
    // Do NOT set packageOfResClass — Android packages resources at the default path
    // (expensetrackr.core.presentation.generated.resources); a custom package causes a
    // runtime MissingResourceException. Use the default generated package below.
}
```

So any module can use a string:

```kotlin
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.nav_dashboard
import org.jetbrains.compose.resources.stringResource

Text(stringResource(Res.string.nav_dashboard))
// With arguments:  stringResource(Res.string.budget_warn_monthly, projected, budget)
```

> **Fallback:** any key missing from a `values-<lang>` file automatically falls back to the
> English value in `values/`. So partial translations are safe — the app is fully usable in
> every locale, showing English only for not-yet-translated keys.

## Supported languages (24)

**Indian (14):** English `en`, Hindi `hi`, Bengali `bn`, Marathi `mr`, Telugu `te`,
Tamil `ta`, Gujarati `gu`, Urdu `ur` (RTL), Kannada `kn`, Odia `or`, Malayalam `ml`,
Punjabi `pa`, Assamese `as`, Nepali `ne`, Sanskrit `sa`

**World (10):** Chinese `zh`, Spanish `es`, Arabic `ar` (RTL), Portuguese `pt`,
Russian `ru`, Japanese `ja`, French `fr`, German `de`, Indonesian `in`, Italian `it`

> **Known limitation:** Compose resources only accepts standard 2-letter (ISO 639-1)
> language qualifiers. Languages with 3-letter-only codes — **Maithili (mai), Santali (sat),
> Konkani (kok), Dogri (doi), Manipuri/Meitei (mni)** — cannot use `values-<code>` folders and
> would need a custom string mechanism. They are intentionally omitted for now.

## Coverage status

**All feature screens are fully extracted to string resources** (~110 keys in
`values/strings.xml`). Every user-facing literal across `feature/` goes through `stringResource`.

| Language | Status |
|----------|--------|
| English (`values/`) | ✅ Complete (source of truth) |
| Hindi (`hi`), Telugu (`te`) | ✅ Complete (all ~110 keys translated) |
| Other 22 languages | ◑ Common UI translated (~17 keys); remaining keys **fall back to English** |

To finish a language: copy any missing keys from `values/strings.xml` into its
`values-<lang>/strings.xml` and translate the values. No code changes needed.

### Intentionally English-only
Long legal/marketing bodies in `feature/settings/.../SubScreens.kt` (About blurb, Privacy &
Terms paragraph bodies, "Version 1.0.0", "Last updated…"), and month-name abbreviations from
`DateFormatter.kt`.

## How to add a new string
1. Add `<string name="my_key">English text</string>` to `values/strings.xml`.
2. Use it: `stringResource(Res.string.my_key)`.
3. (Optional) add `my_key` to any `values-<lang>/strings.xml` to translate it.

## How to add a new language
1. Create `values-<lang>/strings.xml` (2-letter ISO 639-1 code).
2. Add the keys you want translated; the rest fall back to English.
3. Build — Compose generates the accessors automatically.

## Notes
- Machine-translated strings should be reviewed by a native speaker before release,
  especially money/budget wording.
- RTL (Urdu, Arabic) layout mirroring is handled automatically by Compose based on locale.
