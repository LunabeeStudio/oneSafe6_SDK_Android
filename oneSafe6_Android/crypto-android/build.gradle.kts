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

import com.google.protobuf.gradle.id

plugins {
    `android-library`
    `onesafe-publish`
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt)
}

description = "Android implementation of oneSafe cryptography"

android {
    namespace = "studio.lunabee.onesafe.cryptography.android"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    buildTypes {
        release {
            consumerProguardFile("consumer-rules.pro")
        }
    }

    packaging {
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/INDEX.LIST"
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
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

    protobuf {
        protoc {
            artifact = libs.protoc.get().toString()
        }

        // Add Kotlin protobuf plugin
        plugins {
            id("kotlin")
        }

        generateProtoTasks {
            all().forEach { task ->
                task.builtins {
                    create("java") {
                        option("lite")
                    }
                }

                // Use Kotlin
                task.plugins {
                    id("kotlin")
                }
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.maxParallelForks = 10
            }
        }
    }
}

val tinkImplementation: Configuration by configurations
val jceImplementation: Configuration by configurations

dependencies {
    implementation(platform(libs.lunabee.bom))

    androidTestImplementation(libs.biometric) // used to check if device has biometric
    androidTestImplementation(libs.mockk.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bouncycastle)
    implementation(libs.conscrypt.android) // Used for chachapoly in Jce flavor & as fallback for Rsa
    implementation(libs.datastore)
    implementation(libs.doubleratchet)
    implementation(libs.hilt.android)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.datetime)
    implementation(libs.lbextensions.android)
    implementation(libs.lblogger)
    implementation(libs.preference.ktx) { exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx") }
    implementation(libs.protobuf.kotlinlite)
    implementation(libs.tink.android) // Used for HKDF
    kspAndroidTest(libs.dagger.hilt.compiler)
    kspTest(libs.dagger.hilt.compiler)
    testImplementation(libs.biometric) // used to check if device has biometric
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.conscrypt.openjdk.uber)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.mockk.android)
    tinkImplementation(libs.tink.android)

    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.oneSafe6KMP.bubblesDomain)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.bubblesRepository)
    implementation(projects.oneSafe6KMP.crypto)
    implementation(projects.oneSafe6KMP.error)
    implementation(projects.oneSafe6KMP.messagingDomain)
    implementation(projects.oneSafe6KMP.shared)
    lintChecks(project(":crypto-android:checks"))
    testImplementation(project(":common-test-robolectric"))
    testImplementation(projects.dependencyInjection.testComponent)
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
