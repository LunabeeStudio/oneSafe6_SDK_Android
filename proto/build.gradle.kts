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
    `kotlin-library`
    id("com.google.protobuf")
    `onesafe-publish`
}

dependencies {
    api(libs.protobuf.kotlinlite)
    testImplementation(project(":common-test"))
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
            srcDir("${rootDir.path}/oneSafe6_common/proto")
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
