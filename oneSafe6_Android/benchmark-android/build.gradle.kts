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
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    testBuildType = "release"
    buildTypes {
        debug {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "benchmark-proguard-rules.pro")
        }
        release {
            isDefault = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
    androidTestImplementation(platform(libs.lunabee.bom))

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation(libs.datastore)
    androidTestImplementation(libs.datastore.preferences)
    androidTestImplementation(libs.doubleratchet)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.ktor.client.android)
    androidTestImplementation(libs.lbcore)
    androidTestImplementation(libs.lblogger)
    androidTestImplementation(libs.room.ktx)
    androidTestImplementation(libs.room.testing)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.hilt.android)
    kspAndroidTest(libs.dagger.hilt.compiler)

    androidTestImplementation(project(":bubbles-crypto-android"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":dependency-injection"))
    androidTestImplementation(project(":import-export-core"))
    androidTestImplementation(project(":import-export-repository"))
    androidTestImplementation(project(":remote"))
    androidTestImplementation(project(":repository"))
    androidTestImplementation(projects.app.coreUi)
    androidTestImplementation(projects.app.settings)
    androidTestImplementation(projects.commonTestAndroid) { exclude(group = "io.mockk") }
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.domainJvm)
    androidTestImplementation(projects.importExportDomain)
    androidTestImplementation(projects.importExportDrive)
    androidTestImplementation(projects.localAndroid)
    androidTestImplementation(projects.oneSafe6KMP.bubblesDomain)
    androidTestImplementation(projects.oneSafe6KMP.bubblesRepository)
    androidTestImplementation(projects.oneSafe6KMP.messagingDomain)
    androidTestImplementation(projects.oneSafe6KMP.messagingRepository)
    androidTestImplementation(projects.oneSafe6KMP.shared)
}
