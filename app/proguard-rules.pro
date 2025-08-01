# Becomap Demo App - ProGuard Rules
# These rules are applied when building the demo app with obfuscation enabled

# ================================================================================================
# DEMO APP CLASSES - CAN BE OBFUSCATED
# ================================================================================================

# The demo app classes can be safely obfuscated since they're not used by other apps
# MainActivity, SearchActivity, adapters, etc. can all be obfuscated

# ================================================================================================
# BECOMAP SDK INTEGRATION
# ================================================================================================

# The SDK's consumer-rules.pro will automatically protect SDK APIs
# No additional rules needed for SDK classes - they're handled by consumer-rules.pro

# ================================================================================================
# ANDROID FRAMEWORK COMPATIBILITY
# ================================================================================================

# Keep Activity classes and their lifecycle methods
-keep public class * extends android.app.Activity {
    public void onCreate(android.os.Bundle);
    public void onDestroy();
    public void onPause();
    public void onResume();
    public void onStart();
    public void onStop();
}

# Keep Application class if any
-keep public class * extends android.app.Application {
    public void onCreate();
}

# Keep custom views and their constructors
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ================================================================================================
# SERIALIZATION SUPPORT
# ================================================================================================

# Keep Serializable implementation (for Intent data passing)
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================================================================================
# ANDROIDX AND MATERIAL DESIGN
# ================================================================================================

# Keep RecyclerView adapters and ViewHolders
-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    public *;
}

-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(...);
}

# Keep Material Design components
-keep class com.google.android.material.** { *; }

# ================================================================================================
# DEBUGGING SUPPORT
# ================================================================================================

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*

# Keep inner classes
-keepattributes InnerClasses,EnclosingMethod

# Keep generic signatures
-keepattributes Signature

# ================================================================================================
# OPTIMIZATION SETTINGS
# ================================================================================================

# Enable optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 3

# Allow access modification for better optimization
-allowaccessmodification

# ================================================================================================
# DEBUG LOGGING REMOVAL
# ================================================================================================

# Remove Android Log calls in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# ================================================================================================
# WARNINGS SUPPRESSION
# ================================================================================================

# Suppress warnings for missing classes
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# ================================================================================================
# NOTES
# ================================================================================================

# This configuration:
# 1. Allows demo app code to be obfuscated (since it's not a library)
# 2. Relies on SDK's consumer-rules.pro to protect SDK APIs
# 3. Keeps Android framework compatibility
# 4. Provides debugging support
# 5. Removes debug logging in release builds
# 6. Enables optimization for better performance