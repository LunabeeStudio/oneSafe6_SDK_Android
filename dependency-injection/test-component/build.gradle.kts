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
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "studio.lunabee.onesafe.dependencyinjection.test"

    flavorDimensions += AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
    productFlavors {
        create(AndroidConfig.CRYPTO_BACKEND_FLAVOR_JCE) {
            dimension = AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
        }

        create(AndroidConfig.CRYPTO_BACKEND_FLAVOR_TINK) {
            dimension = AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.hilt.android.testing)
    kapt(libs.dagger.hilt.compiler)

    implementation(libs.room.ktx)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.timber)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)

    implementation(platform(libs.compose.beta.bom))
    implementation(libs.androidx.test.runner)
    implementation(libs.compose.ui.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lblogger)
    implementation(libs.lbcore)

    implementation(libs.doubleratchet)

    implementation(project(":crypto-android"))
    implementation(project(":domain"))
    implementation(project(":import-export"))
    implementation(project(":import-export-domain"))
    implementation(project(":repository"))
    implementation(project(":local-android"))
    implementation(project(":remote"))
    implementation(project(":dependency-injection"))
    implementation(project(":app:settings"))
    implementation(project(":bubbles-domain"))
    implementation(project(":bubbles-repository"))
    implementation(project(":bubbles-crypto-android"))
    implementation(project(":messaging-domain"))
    implementation(project(":messaging-repository"))
    implementation(project(":messaging-crypto-android"))
    api(project(":common-test"))
}
