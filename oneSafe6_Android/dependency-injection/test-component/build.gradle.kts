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
    namespace = "studio.lunabee.onesafe.dependencyinjection.test"

    defaultConfig {
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    flavorDimensions += AndroidConfig.CryptoBackendFlavorDimension
    productFlavors {
        create(AndroidConfig.CryptoBackendFlavorJce) {
            dimension = AndroidConfig.CryptoBackendFlavorDimension
        }

        create(AndroidConfig.CryptoBackendFlavorTink) {
            dimension = AndroidConfig.CryptoBackendFlavorDimension
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/INDEX.LIST"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    ksp(libs.dagger.hilt.compiler)

    implementation(libs.androidx.test.core.ktx)
    implementation(libs.androidx.test.runner)
    implementation(libs.compose.ui.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.doubleratchet)
    implementation(libs.hilt.android)
    implementation(libs.hilt.android.testing)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.mock)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.lbcore)
    implementation(libs.lbloading.hilt)
    implementation(libs.lblogger)
    implementation(libs.room.ktx)
    implementation(libs.work.testing)

    implementation(project(":app:migration"))
    implementation(project(":bubbles-crypto-android"))
    api(project(":common-test"))
    implementation(project(":crypto-android"))
    implementation(project(":dependency-injection"))
    implementation(project(":import-export-core"))
    implementation(projects.app.commonUi)
    implementation(projects.app.settings)
    implementation(projects.commonJvm)
    implementation(projects.commonTestAndroid)
    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.localAndroid)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.bubblesRepository)
    implementation(projects.oneSafe6KMP.error)
    implementation(projects.oneSafe6KMP.messagingDomain)
    implementation(projects.oneSafe6KMP.messagingRepository)
    implementation(project(":remote"))
    implementation(project(":repository"))
}
