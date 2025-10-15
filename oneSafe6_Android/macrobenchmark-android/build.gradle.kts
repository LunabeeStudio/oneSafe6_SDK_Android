plugins {
    id("com.android.test")
    id("kotlin-android")
}

android {
    namespace = "studio.lunabee.onesafe.macrobenchmark"
    compileSdk = AndroidConfig.CompileSdk

    defaultConfig {
        minSdk = AndroidConfig.MinAppSdk
        targetSdk = AndroidConfig.TargetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "dev")
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    flavorDimensions += AndroidConfig.CryptoBackendFlavorDimension
    productFlavors {
        create(AndroidConfig.CryptoBackendFlavorJce) {
            dimension = AndroidConfig.CryptoBackendFlavorDimension
        }

        create(AndroidConfig.CryptoBackendFlavorTink) {
            dimension = AndroidConfig.CryptoBackendFlavorDimension
        }
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
            pickFirsts += "META-INF/com.google.dagger_dagger.version"
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JdkVersion
        targetCompatibility = ProjectConfig.JdkVersion
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.lunabee.bom))

    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.espresso.core)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.lblogger)

    implementation(projects.app.commonUi)
    implementation(projects.app.coreUi)
    implementation(projects.commonTestAndroid)
    implementation(projects.domainJvm)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(ProjectConfig.JvmTarget)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JdkVersion.toString()))
    }
}
