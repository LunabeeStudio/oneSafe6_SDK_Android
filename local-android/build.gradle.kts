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
    kotlin("kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt)
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
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    val properties: Properties = Properties()
    File(rootDir.path + "/versions.properties").inputStream().use { properties.load(it) }

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
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.hilt.android)

    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.paging.runtime)
    implementation(libs.androidx.core.ktx)

    implementation(libs.datastore)
    implementation(libs.protobuf.kotlinlite)

    ksp(libs.room.compiler)

    implementation(libs.timber)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbextensions)

    implementation(project(":domain"))
    implementation(project(":repository"))
    implementation(project(":error"))
    implementation(project(":common"))

    implementation(project(":bubbles-domain"))
    implementation(project(":bubbles-repository"))

    implementation(project(":messaging-domain"))
    implementation(project(":messaging-repository"))

    implementation(libs.doubleratchet)

    kaptAndroidTest(libs.dagger.hilt.compiler)

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
