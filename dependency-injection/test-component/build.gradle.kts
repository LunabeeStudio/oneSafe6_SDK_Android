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
    id("kotlinx-serialization")
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

dependencies {
    implementation(Google.dagger.hilt.android.testing)

    implementation(AndroidX.room.ktx)
    implementation(AndroidX.test.coreKtx)
    implementation(JakeWharton.timber)
    implementation(Ktor.client.android)
    implementation(Ktor.client.logging)
    implementation(AndroidX.dataStore)
    implementation(AndroidX.dataStore.preferences)

    implementation(platform(libs.compose.beta.bom))
    implementation(AndroidX.test.runner)
    implementation(AndroidX.compose.ui.test)
    implementation(AndroidX.compose.ui.testJunit4)
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
    implementation(Ktor.client.contentNegotiation)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lblogger)
    implementation(libs.lbcore)

    implementation(project(":crypto-android"))
    implementation(project(":domain"))
    implementation(project(":import-export"))
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
    api(project(":common-test"))
}
