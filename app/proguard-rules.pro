# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Obfuscation avancee
-repackageclasses 'a'
-allowaccessmodification
-overloadaggressively

# Renommage des classes
-classobfuscationdictionary dict.txt
-packageobfuscationdictionary dict.txt
-obfuscationdictionary dict.txt

# Ne pas optimiser les classes de securite
-keep class com.secure.taxapp.services.** { *; }
-keep class com.secure.taxapp.utils.** { *; }

# Garder les services Android
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.telecom.CallScreeningService

# Supprimer les logs en production
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
