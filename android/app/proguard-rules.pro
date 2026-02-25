# Flutter specific rules
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.**  { *; }
-keep class io.flutter.util.**  { *; }
-keep class io.flutter.view.**  { *; }
-keep class io.flutter.**  { *; }
-keep class io.flutter.plugins.**  { *; }

# Keep annotations
-keepattributes *Annotation*

# Media Kit rules
-keep class com.alexmercerind.** { *; }
-keep class com.mixaline.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Suppress warnings
-dontwarn com.google.android.play.core.**
-dontwarn kotlin.**
