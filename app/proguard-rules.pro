# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\saana\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit that file to add flags that are common to all your projects

-dontwarn java.awt.**
-dontwarn javax.swing.**

# Proguard rules for kotlin coroutines
-dontwarn kotlin.coroutines.jvm.internal.DebugProbesKt
-keep class kotlin.coroutines.jvm.internal.DebugProbesKt {
    <methods>;
}