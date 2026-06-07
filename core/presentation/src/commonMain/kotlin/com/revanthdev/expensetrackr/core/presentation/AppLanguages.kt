package com.revanthdev.expensetrackr.core.presentation

/**
 * A selectable UI language. [tag] is the BCP-47 / resource-qualifier code stored in settings
 * (null is represented separately as "system default"); [nativeName] is shown in the picker
 * in that language's own script so users recognise it regardless of the current locale.
 */
data class AppLanguage(val tag: String, val nativeName: String)

/** All languages the app ships translations (or fallbacks) for. Order: English, then Indian, then world. */
val appLanguages: List<AppLanguage> = listOf(
    AppLanguage("en", "English"),
    AppLanguage("hi", "हिन्दी"),
    AppLanguage("te", "తెలుగు"),
    AppLanguage("bn", "বাংলা"),
    AppLanguage("mr", "मराठी"),
    AppLanguage("ta", "தமிழ்"),
    AppLanguage("gu", "ગુજરાતી"),
    AppLanguage("ur", "اردو"),
    AppLanguage("kn", "ಕನ್ನಡ"),
    AppLanguage("or", "ଓଡ଼ିଆ"),
    AppLanguage("ml", "മലയാളം"),
    AppLanguage("pa", "ਪੰਜਾਬੀ"),
    AppLanguage("as", "অসমীয়া"),
    AppLanguage("ne", "नेपाली"),
    AppLanguage("sa", "संस्कृतम्"),
    AppLanguage("zh", "中文"),
    AppLanguage("es", "Español"),
    AppLanguage("ar", "العربية"),
    AppLanguage("pt", "Português"),
    AppLanguage("ru", "Русский"),
    AppLanguage("ja", "日本語"),
    AppLanguage("fr", "Français"),
    AppLanguage("de", "Deutsch"),
    AppLanguage("in", "Bahasa Indonesia"),
    AppLanguage("it", "Italiano"),
)
