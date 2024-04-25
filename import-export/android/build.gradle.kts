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
}

android {
    namespace = "studio.lunabee.onesafe.importexport.android"

    val backupProviderAuthoritySuffix = ".importexport.backupprovider"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)

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
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.hilt.android)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.hilt.work)
    implementation(libs.work.runtime)
    implementation(libs.lblogger)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.auth.base) // required to get GoogleAuthUtil class

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    implementation(libs.lbccore)

    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    implementation(project(":import-export-core"))
    implementation(project(":import-export-proto"))
    api(project(":import-export-domain"))
    implementation(project(":import-export-repository"))
    implementation(project(":import-export-drive"))
    implementation(project(":domain"))
    implementation(project(":error"))
    implementation(project(":common"))
    implementation(project(":app:common-ui"))
    implementation(project(":app:core-ui"))
    implementation(project(":app:settings"))

    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(project(":dependency-injection"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":app:settings"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.lblogger)
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.rules)
}
