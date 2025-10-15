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
        minSdk = AndroidConfig.MinAppSdk
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CryptoBackendFlavorDefault)
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
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    ksp(libs.dagger.hilt.compiler)

    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.doubleratchet)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lbloading.hilt)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.paging.compose)

    implementation(project(":bubbles"))
    implementation(projects.app.commonUi)
    implementation(projects.app.coreUi)
    implementation(projects.app.settings)
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.error)
    implementation(projects.oneSafe6KMP.messagingDomain)

    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(project(":crypto-android"))
    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.oneSafe6KMP.shared)
}
