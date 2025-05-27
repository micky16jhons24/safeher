# === AndroidX Core & Lifecycle ===
# Reglas generales para AndroidX que suelen ser necesarias.
-keep class androidx.core.app.CoreComponentFactory { *; }
-keep class androidx.core.content.FileProvider { *; } # Si usas FileProvider

# Lifecycle (Muy importante para tu error)
-keep public class * extends androidx.lifecycle.ViewModel { *; }
-keep public interface androidx.lifecycle.LifecycleObserver { *; } # Interfaz en lugar de clase para implementaciones
-keep class androidx.lifecycle.ReportFragment { *; }
-keep class androidx.lifecycle.ReportFragment$* { *; } # Para clases internas como ActivityInitializationListener
-keep public class androidx.lifecycle.ProcessLifecycleOwner { *; }
-keep public class androidx.lifecycle.ProcessLifecycleInitializer { *; }
-keep public class androidx.lifecycle.DefaultLifecycleObserver { *; } # Importante para Java 8
-dontwarn androidx.lifecycle.**

# AndroidX Startup (También crucial para tu error)
-keep public interface androidx.startup.Initializer { *; } # Interfaz
-keep public class androidx.startup.InitializationProvider { *; }
-dontwarn androidx.startup.**

# === Room ===
# Si usas Room, estas reglas son importantes.
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <T extends androidx.room.RoomDatabase> T create(android.content.Context, java.lang.Class<T>, java.lang.String);
    public abstract <T> T getDao(java.lang.Class<T>); # Método común para obtener DAOs
}
# Mantén las clases @Entity, @Dao, @Database, TypeConverters
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; } # DAOs son interfaces en Java
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverters class * { *; }
-keepclassmembers class * { # Para mantener métodos anotados en DAOs, etc.
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
    @androidx.room.Transaction <methods>;
}

# === Google Play Services (Maps, Location, Places) ===
# Generalmente, las reglas de ProGuard para Play Services son manejadas por
# los archivos de ProGuard que vienen con las propias bibliotecas.
# Pero si tienes problemas específicos, puedes añadir:
-dontwarn com.google.android.gms.**
# -keep class com.google.android.gms.common.api.internal.TaskApiCall { *; } # Ejemplo si fuera necesario

# === OSMDroid (si usas y tienes problemas de ofuscación) ===
# -keep class org.osmdroid.** { *; }
# -dontwarn org.osmdroid.**

# === ViewBinding