# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Media3 Transformer 1.8.x references API 31 media-metrics types. Keep the
# transformer boundary intact so R8 does not merge those references into code
# loaded on Android 9-11 (https://github.com/androidx/media/issues/2535).
-keep class androidx.media3.transformer.** { *; }
-dontwarn android.media.metrics.**

# These models are populated from Firebase Remote Config JSON through Gson.
# Keep them intact so R8 does not remove or rename their fields in release builds.
-keep class com.camera.gps.model.Ads.** { *; }
