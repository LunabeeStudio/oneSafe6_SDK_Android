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
    namespace = "studio.lunabee.onesafe.migration"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev")
        create("prod")
    }

    packaging {
        resources {
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    implementation(project(":domain"))
    implementation(project(":repository"))
    implementation(project(":crypto-android"))
    implementation(project(":common"))
    implementation(project(":error"))
    implementation(project(":local-android"))
    implementation(project(":app:settings"))
    implementation(project(":import-export-domain"))
    implementation(project(":import-export-android"))

    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(libs.junit4)
}
