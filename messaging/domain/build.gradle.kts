/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/23/2023 - for the oneSafe6 SDK.
 * Last modified 5/23/23, 4:21 PM
 */

import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import java.util.Properties

plugins {
    `kotlin-library`
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    api(libs.protobuf.kotlinlite)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    implementation(project(":domain"))
    implementation(project(":bubbles-domain"))
    implementation(project(":common"))
    implementation(project(":error"))

    implementation(libs.doubleratchet)
    testImplementation(libs.mockk)
    testImplementation(project(":common-test"))
}
// TODO proto in domain
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
                getByName("java") {
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

sourceSets {
    main {
        proto {
            srcDir("${rootDir.path}/oneSafe6_common/onesafe_k/proto")
        }
    }
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
