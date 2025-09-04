# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-printseeds seeds.txt

## https://gitlab.com/mvysny/slf4j-handroid/-/blob/master/README.md#L83
-keep class com.google.firebase.crashlytics.FirebaseCrashlytics {
    static com.google.firebase.crashlytics.FirebaseCrashlytics getInstance();
    void log(java.lang.String);
    void recordException(java.lang.Throwable);
}

# Protobuf https://github.com/protocolbuffers/protobuf/issues/6463#issuecomment-632884075
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Unused by Bouquet lib (pdf)
-dontwarn com.gemalto.jp2.JP2Decoder

# Keep OSError's constructor to allow instanciation of OSError with reflection from ErrorCode::get
-keepclassmembers class * extends studio.lunabee.onesafe.error.OSError { <init>(...); }

# Ignore SLF4J no impl warning
# https://youtrack.jetbrains.com/issue/KTOR-5528/Missing-class-warning-when-using-R8-with-ktor-client-in-android-application
-dontwarn org.slf4j.impl.StaticLoggerBinder

# About librairies
-dontwarn com.mikepenz.aboutlibraries.ui.compose.m3.LibraryColors
-dontwarn com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
-dontwarn com.mikepenz.aboutlibraries.ui.compose.m3.LibraryPadding