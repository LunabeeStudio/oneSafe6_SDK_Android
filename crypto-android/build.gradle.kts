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
import java.util.Properties

plugins {
    `android-library`
    `onesafe-publish`
    id("com.google.protobuf")
}

description = "Android implementation of oneSafe cryptography"

android {
    namespace = "studio.lunabee.onesafe.cryptography"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
    }

    buildTypes {
        release {
            consumerProguardFile("consumer-rules.pro")
        }
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
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

    val properties: Properties = Properties()
    File(rootDir.path + "/versions.properties").inputStream().use { properties.load(it) }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${properties.getProperty("version.com.google.protobuf..protobuf-kotlin-lite")}"
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
}

val tinkImplementation: Configuration by configurations
val jceImplementation: Configuration by configurations

dependencies {
    coreLibraryDesugaring(Android.tools.desugarJdkLibs)

    implementation(libs.kotlin.stdlib)

    implementation(AndroidX.dataStore)
    implementation(libs.protobuf.kotlinlite)

    tinkImplementation(libs.tink.android)

    implementation(libs.bcprov.jdk18on)
    implementation(libs.tink.android) // Used for HKDF
    implementation(libs.conscrypt.android) // Used for chachapoly in Jce flavor & as fallback for Rsa

    implementation(AndroidX.preference.ktx.toString()) {
        exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
    }

    implementation(libs.kotlin.logging.jvm)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbextensions.android)
    implementation(libs.lblogger.timber)
    implementation(libs.kotlin.reflect)

    implementation(project(":domain"))
    implementation(project(":bubbles-domain"))
    implementation(project(":error"))
    implementation(project(":common"))
    implementation(project(":import-export"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(Testing.MockK.android)
    androidTestImplementation(AndroidX.biometric) // used to check if device has biometric

    testImplementation(project(":common-test-android"))
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
