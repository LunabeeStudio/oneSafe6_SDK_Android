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
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "studio.lunabee.onesafe.migration"

    defaultConfig {
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create(OSDimensions.Environment.Dev)
        create(OSDimensions.Environment.Store)
    }

    packaging {
        resources {
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.sqlite.ktx)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)
    implementation(libs.lbextensions)
    implementation(libs.doubleratchet)
    implementation(libs.kotlinx.datetime)

    implementation(project(":domain-jvm"))
    implementation(project(":repository"))
    implementation(project(":crypto-android"))
    implementation(project(":common-jvm"))
    implementation(project(":common-protobuf"))
    implementation(libs.onesafe.error)
    implementation(project(":local-android"))
    implementation(project(":app:settings"))
    implementation(project(":import-export-domain"))
    implementation(project(":import-export-android"))
    implementation(project(":bubbles"))
    implementation(project(":local-android"))

    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(libs.bubbles.shared)
    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.room.testing)

    testImplementation(project(":dependency-injection:test-component"))
    testImplementation(libs.bubbles.shared)
    testImplementation(project(":common-test-robolectric"))
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.dagger.hilt.compiler)
}
