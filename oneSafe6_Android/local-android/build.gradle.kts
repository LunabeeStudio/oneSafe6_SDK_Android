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
        missingDimensionStrategy("crypto", AndroidConfig.CryptoBackendFlavorDefault)
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
            pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
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
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.lunabee.bom))

    ksp(libs.room.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.datastore)
    implementation(libs.doubleratchet)
    implementation(libs.hilt.android)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.lbcore)
    implementation(libs.lbextensions)
    implementation(libs.lblogger)
    implementation(libs.paging.runtime)
    implementation(libs.protobuf.kotlinlite)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.sqlcipher.android)

    implementation(project(":crypto-android"))
    implementation(project(":import-export-repository"))
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.importExportDrive)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.bubblesRepository)
    implementation(projects.oneSafe6KMP.error)
    implementation(projects.oneSafe6KMP.messagingDomain)
    implementation(projects.oneSafe6KMP.messagingRepository)
    implementation(project(":repository"))

    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    androidTestImplementation(libs.room.testing)

    testImplementation(project(":common-test-robolectric"))
    testImplementation(libs.compose.ui.test.junit4)
    kspTest(libs.dagger.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.dependencyInjection.testComponent)
    testImplementation(projects.oneSafe6KMP.shared)
    testImplementation(libs.room.testing)
}

tasks.register<CleanProtoTask>("cleanProtobuf")

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
