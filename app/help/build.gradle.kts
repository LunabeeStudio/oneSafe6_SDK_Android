plugins {
    `android-library`
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "studio.lunabee.onesafe.help"

    buildFeatures {
        compose = true
    }
    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)

        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/INDEX.LIST"
    }

    flavorDimensions += "environment"
    productFlavors {
        create(OSDimensions.Environment.Dev)
        create(OSDimensions.Environment.Store)
    }
}

hilt {
    enableAggregatingTask = true
}

val devImplementation: Configuration by configurations

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.process.phoenix)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lblogger)
    implementation(libs.lbcore)
    implementation(libs.lbloading.compose)

    implementation(libs.lbccore)
    implementation(libs.doubleratchet)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":domain-jvm"))
    implementation(project(":import-export-android"))

    devImplementation(libs.work.runtime)
    devImplementation(libs.accompanist.permissions)
    devImplementation(libs.play.services.base)
    devImplementation(libs.android.material)
    devImplementation(libs.kotlin.reflect)
    devImplementation(libs.datastore.preferences)
    devImplementation(libs.room.ktx)
    devImplementation(project(":import-export-drive"))
    devImplementation(libs.bubbles.messaging.domain)
    devImplementation(libs.bubbles.domain)
    devImplementation(project(":local-android"))
    devImplementation(libs.onesafe.error)
    devImplementation(project(":app:settings"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":app:settings"))
    androidTestImplementation(project(":local-android"))
    androidTestImplementation(libs.bubbles.shared)
    androidTestImplementation(libs.datastore.preferences)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.espresso.core)
    kspAndroidTest(libs.androidx.hilt.compiler)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
