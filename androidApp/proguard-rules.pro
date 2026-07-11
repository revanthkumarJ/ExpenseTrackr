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
