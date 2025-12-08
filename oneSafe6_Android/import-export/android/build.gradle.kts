/*
 * Copyright (c) 2023-2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:45 AM
 */

plugins {
    `android-library`
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "studio.lunabee.onesafe.importexport.android"

    val backupProviderAuthoritySuffix = ".importexport.backupprovider"

    defaultConfig {
        minSdk = AndroidConfig.MinAppSdk
        missingDimensionStrategy("crypto", AndroidConfig.CryptoBackendFlavorDefault)

        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        buildConfigField("String", "BACKUPS_PROVIDER_AUTHORITY_SUFFIX", "\"$backupProviderAuthoritySuffix\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create(OSDimensions.Environment.Dev)
        create(OSDimensions.Environment.Store)
    }

    packaging {
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/INDEX.LIST"
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.accompanist.permissions)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lbextensions)
    implementation(libs.lblogger)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)
    implementation(libs.play.services.auth.base) // required to get GoogleAuthUtil class
    implementation(libs.work.runtime)

    implementation(project(":import-export-core"))
    implementation(project(":import-export-proto"))
    implementation(project(":import-export-repository"))
    implementation(projects.app.commonUi)
    implementation(projects.app.coreUi)
    implementation(projects.app.settings)
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    api(projects.importExportDomain)
    implementation(projects.importExportDrive)
    implementation(projects.oneSafe6KMP.error)

    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(project(":crypto-android"))
    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(project(":dependency-injection"))
    androidTestImplementation(libs.lblogger)
    androidTestImplementation(projects.app.settings)
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.work.testing)

    testImplementation(libs.androidx.test.rules)
    testImplementation(project(":common-test-robolectric"))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(project(":crypto-android"))
    kspTest(libs.dagger.hilt.compiler)
    testImplementation(project(":dependency-injection"))
    testImplementation(libs.doubleratchet)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.lblogger)
    testImplementation(projects.app.settings)
    testImplementation(projects.commonTestAndroid)
    testImplementation(projects.dependencyInjection.testComponent)
    testImplementation(projects.oneSafe6KMP.shared)
    testImplementation(libs.room.testing)
    testImplementation(libs.work.testing)
}
