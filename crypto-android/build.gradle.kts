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
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

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
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.kotlin.stdlib)
    implementation(libs.hilt.android)

    implementation(libs.datastore)
    implementation(libs.protobuf.kotlinlite)

    tinkImplementation(libs.tink.android)

    implementation(libs.bouncycastle)
    implementation(libs.tink.android) // Used for HKDF
    implementation(libs.conscrypt.android) // Used for chachapoly in Jce flavor & as fallback for Rsa
    implementation(libs.doubleratchet)

    implementation(libs.preference.ktx) {
        exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
    }

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbextensions.android)
    implementation(libs.lblogger)
    implementation(libs.kotlin.reflect)

    implementation(project(":domain-jvm"))
    implementation(libs.bubbles.domain)
    implementation(libs.bubbles.messaging.domain)
    implementation(libs.bubbles.shared)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bubbles.repository)
    implementation(libs.onesafe.error)
    implementation(libs.onesafe.crypto)
    implementation(project(":common-jvm"))
    implementation(project(":import-export-domain"))

    kspAndroidTest(libs.dagger.hilt.compiler)

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(libs.bubbles.domain)
    androidTestImplementation(libs.bubbles.shared)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.biometric) // used to check if device has biometric

    testImplementation(libs.hilt.android.testing)
    kspTest(libs.dagger.hilt.compiler)
    testImplementation(project(":dependency-injection:test-component"))
    testImplementation(project(":common-test-robolectric"))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.mockk.android)
    testImplementation(libs.biometric) // used to check if device has biometric
    testImplementation(libs.conscrypt.openjdk.uber)

    lintChecks(project(":crypto-android:checks"))
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
