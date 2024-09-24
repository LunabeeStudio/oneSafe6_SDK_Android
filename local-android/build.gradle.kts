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
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

android {
    namespace = "studio.lunabee.onesafe.storage"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }

    sourceSets {
        getByName("test").assets.srcDir("$projectDir/schemas")
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.maxParallelForks = 10
            }
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.hilt.android)

    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.paging.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.sqlcipher.android)
    implementation(libs.kotlinx.datetime)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.datastore)
    implementation(libs.protobuf.kotlinlite)

    ksp(libs.room.compiler)

    implementation(libs.lblogger)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbextensions)
    implementation(libs.lbcore)

    implementation(project(":domain-jvm"))
    implementation(project(":repository"))
    implementation(libs.onesafe.error)
    implementation(project(":common-jvm"))
    implementation(project(":crypto-android"))

    implementation(libs.bubbles.domain)
    implementation(libs.bubbles.repository)
    implementation(libs.bubbles.messaging.repository)
    implementation(libs.bubbles.messaging.domain)

    implementation(project(":import-export-domain"))
    implementation(project(":import-export-repository"))
    implementation(project(":import-export-drive"))

    implementation(libs.doubleratchet)

    kspAndroidTest(libs.dagger.hilt.compiler)

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(libs.bubbles.shared)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)

    testImplementation(libs.hilt.android.testing)
    kspTest(libs.dagger.hilt.compiler)
    testImplementation(project(":dependency-injection:test-component"))
    testImplementation(project(":common-test-robolectric"))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.bubbles.shared)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
