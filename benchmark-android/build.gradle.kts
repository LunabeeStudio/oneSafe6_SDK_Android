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
    namespace = "studio.lunabee.onesafe.benchmark"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.benchmark.HiltBenchmarkRunner"
    }

    testBuildType = "release"
    buildTypes {
        debug {
            // Since isDebuggable can't be modified by gradle for library modules,
            // it must be done in a manifest - see src/AndroidManifest.xml
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "benchmark-proguard-rules.pro")
        }
        release {
            isDefault = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
        resources.pickFirsts.add("META-INF/DEPENDENCIES")
        resources.pickFirsts.add("META-INF/INDEX.LIST")
    }

    flavorDimensions += AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
    productFlavors {
        create(AndroidConfig.CRYPTO_BACKEND_FLAVOR_JCE) {
            dimension = AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
        }

        create(AndroidConfig.CRYPTO_BACKEND_FLAVOR_TINK) {
            dimension = AndroidConfig.CRYPTO_BACKEND_FLAVOR_DIMENSION
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.hilt.android)
    kspAndroidTest(libs.dagger.hilt.compiler)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation(libs.lblogger)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.datastore)
    androidTestImplementation(libs.datastore.preferences)
    androidTestImplementation(libs.ktor.client.android)
    androidTestImplementation(libs.room.ktx)
    androidTestImplementation(libs.room.testing)

    androidTestImplementation(platform(libs.lunabee.bom))
    androidTestImplementation(libs.lbcore)

    androidTestImplementation(project(":dependency-injection"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":common-test-android")) {
        exclude(group = "io.mockk")
    }
    androidTestImplementation(project(":domain"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":local-android"))
    androidTestImplementation(project(":app:core-ui"))
    androidTestImplementation(project(":app:settings"))
    androidTestImplementation(project(":bubbles-repository"))
    androidTestImplementation(project(":bubbles-crypto-android"))
    androidTestImplementation(project(":bubbles-domain"))
    androidTestImplementation(project(":messaging-domain"))
    androidTestImplementation(project(":messaging-repository"))
    androidTestImplementation(project(":messaging-crypto-android"))
    androidTestImplementation(project(":import-export-core"))
    androidTestImplementation(project(":import-export-domain"))
    androidTestImplementation(project(":import-export-repository"))
    androidTestImplementation(project(":import-export-drive"))
    androidTestImplementation(project(":remote"))
    androidTestImplementation(project(":repository"))
    androidTestImplementation(libs.doubleratchet)
}
