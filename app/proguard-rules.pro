# Add project specific ProGuard rules here.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models intact for serialization / fromMap / toMap
-keep class com.sahed.money_tracker.data.model.** { *; }

# Keep ViewModel state models for reflection / Compose stability
-keep class com.sahed.money_tracker.viewmodel.**UiState { *; }
-keep class com.sahed.money_tracker.viewmodel.**Summary { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase & Play Services
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
