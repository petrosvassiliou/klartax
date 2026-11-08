# SQLCipher rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Google Tink rules
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.errorprone.annotations.**

# Google Drive API v3 & HTTP Client
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class de.klartax.app.** {
    *** Companion;
}
-keepclasseswithmembers class de.klartax.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
