plugins {
    id("kotlin-android")
    id("com.android.application")
}

android {
    namespace = "com.lunabee.onesafe"

    compileSdk = AndroidConfig.CompileSdk

    defaultConfig {
        minSdk = AndroidConfig.MinAppSdk
        targetSdk = AndroidConfig.TargetSdk
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        maybeCreate("release").apply {
            storeFile = project.file("keystore")
            storePassword = "lunabee"
            keyAlias = "release"
            keyPassword = "lunabee"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JdkVersion
        targetCompatibility = ProjectConfig.JdkVersion
    }
}

dependencies {
    implementation(AndroidX.core)
}
