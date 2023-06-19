# BouncyCastle PBKDF2
-keepnames class org.bouncycastle.jcajce.provider.symmetric.PBEPBKDF2 { *; }
-keep class org.bouncycastle.jcajce.provider.symmetric.PBEPBKDF2$PBKDF2withSHA512 { *; }
-keep class org.bouncycastle.jcajce.provider.symmetric.PBEPBKDF2$Mappings { *; }

# FIXME cf https://github.com/tuskyapp/Tusky/pull/3350 -> okhttp 4.10.1 (transitive from COIL)
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE