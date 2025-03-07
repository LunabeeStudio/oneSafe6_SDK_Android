plugins {
    id("kotlin-android")
    id("com.android.application")
}

android {
    namespace = "com.lunabee.onesafe"

    compileSdk = AndroidConfig.COMPILE_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        targetSdk = AndroidConfig.TARGET_SDK
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
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

dependencies {
    implementation(AndroidX.core)
}
