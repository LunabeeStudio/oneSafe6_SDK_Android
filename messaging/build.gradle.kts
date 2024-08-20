/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/23/2023 - for the oneSafe6 SDK.
 * Last modified 5/23/23, 4:21 PM
 */

plugins {
    `android-library`
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "studio.lunabee.onesafe.messaging"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }
    packaging {
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/INDEX.LIST"
    }
}

dependencies {
    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lbccore)
    implementation(libs.lbloading.compose)

    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    kspAndroidTest(libs.dagger.hilt.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.paging.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.kotlinx.coroutines.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.doubleratchet)
    implementation(libs.kotlinx.datetime)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":app:settings"))
    implementation(project(":import-export-domain"))
    implementation(project(":error"))
    implementation(project(":domain"))
    implementation(project(":bubbles"))
    implementation(project(":common"))
    implementation(libs.bubbles.messaging.domain)
    implementation(libs.bubbles.domain)
    implementation(libs.bubbles.error)

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(libs.bubbles.shared)
}
