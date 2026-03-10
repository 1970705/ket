# Wordland ProGuard Rules

# Keep Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Navigation Compose
-keep class androidx.navigation.compose.** { *; }
-keep interface androidx.navigation.compose.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.android.AndroidInjector
-keep class * implements dagger.android.AndroidInjector
-keep class * extends dagger.android.DaggerApplication

# Keep data classes and models
-keep class com.wordland.data.** { *; }
-keep class com.wordland.model.** { *; }
-keep class com.wordland.entity.** { *; }

# Keep ViewModels
-keep class com.wordland.ui.viewmodel.** { *; }

# Keep activities
-keep class com.wordland.ui.** { *; }
-keep class com.wordland.WordlandApplication { *; }

# Keep enums
-keepclassmembers enum com.wordland.** {
    **[] $VALUES;
    public *;
}

# Keep serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep inline classes
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
