# Keep line numbers in stack traces for crash reports.
-keepattributes SourceFile,LineNumberTable

# Retrofit / OkHttp / Gson
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*

# Keep our API response models (Gson uses reflection / @SerializedName)
-keep class com.example.vultrmanager.data.remote.model.** { *; }
