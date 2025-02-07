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
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
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
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    ksp(libs.dagger.hilt.compiler)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.datastore.preferences)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
    devImplementation(libs.accompanist.permissions)
    devImplementation(libs.android.material)
    devImplementation(libs.datastore.preferences)
    devImplementation(libs.kotlin.reflect)
    devImplementation(libs.play.services.base)
    devImplementation(libs.room.ktx)
    devImplementation(libs.work.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.doubleratchet)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lbloading.compose)
    implementation(libs.lblogger)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.process.phoenix)
    kspAndroidTest(libs.androidx.hilt.compiler)

    androidTestImplementation(projects.app.settings)
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.localAndroid)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    devImplementation(projects.app.settings)
    devImplementation(projects.importExportDrive)
    devImplementation(projects.localAndroid)
    devImplementation(projects.oneSafe6KMP.bubblesDomain)
    devImplementation(projects.oneSafe6KMP.error)
    devImplementation(projects.oneSafe6KMP.messagingDomain)
    implementation(projects.app.commonUi)
    implementation(projects.app.coreUi)
    implementation(projects.domainJvm)
    implementation(projects.importExportAndroid)
}
