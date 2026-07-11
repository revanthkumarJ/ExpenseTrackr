# R8 / ProGuard rules for the release build.
# Most libraries (Room, Compose, Firebase, Koin DSL) ship their own consumer rules.
# The rules below protect kotlinx.serialization, which powers our type-safe Navigation routes.

# --- kotlinx.serialization (official recommended rules) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the Companion of @Serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep serializer() on companions of @Serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep INSTANCE.serializer() of @Serializable objects (our nav routes are objects/data classes).
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# --- Extra safety net for our navigation route classes ---
# Keep every @Serializable class in our packages plus its generated $$serializer,
# so type-safe Navigation reflection always resolves.
-keep @kotlinx.serialization.Serializable class com.revanthdev.expensetrackr.** { *; }
-keep,includedescriptorclasses class com.revanthdev.expensetrackr.**$$serializer { *; }

# --- Do not obfuscate/strip our own code ---
# R8 shrinks the large library dependencies (where most of the size win is) but leaves our
# own classes intact. This prevents startup crashes from serialization / DI / navigation
# reflection touching classes R8 would otherwise rename or remove.
-keep class com.revanthdev.expensetrackr.** { *; }
-keepnames class com.revanthdev.expensetrackr.** { *; }

# --- Koin (dependency injection) ---
-keep class org.koin.** { *; }
-keepnames class org.koin.** { *; }

# Keep generic signatures & annotations that reflection-based libraries rely on.
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod

# --- Firebase (Crashlytics + Analytics) ---
# Firebase discovers its modules via ComponentRegistrar classes that are referenced BY NAME
# (as strings) in the merged manifest metadata. If R8 strips or renames them, startup fails
# with "FirebaseCrashlytics component is not present". Keep the component framework and all
# registrar implementations so discovery works in the minified build.
-keep class com.google.firebase.components.** { *; }
-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.provider.FirebaseInitProvider { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
