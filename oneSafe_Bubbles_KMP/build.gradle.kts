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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 1:27 PM
 */

import java.net.URI

plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.androidLibrary).apply(false)
}

val artifactoryUsername: String = project.findProperty("artifactory_consumer_username") as? String
    ?: "library-consumer-public"
val artifactoryPassword: String = project.findProperty("artifactory_consumer_api_key") as? String
    ?: "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/double-ratchet-kmm/")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                snapshotsOnly()
            }
        }
        maven {
            url = URI("https://artifactory.lunabee.studio/artifactory/libs-release-local")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = URI("https://artifactory.lunabee.studio/artifactory/libs-snapshot-local")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory.asFile)
}

tasks.register("assembleAll") {
    group = "build"

    val assembleProject = project.subprojects.filter { it.name != "oneSafe_Bubbles_KMP" }.map {
        "${it.path}:assemble"
    }
    dependsOn(assembleProject)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

detekt {
    parallel = true
    source.from(files(rootProject.rootDir))
    buildUponDefaultConfig = true
    config.from(files("${rootProject.rootDir}/lunabee-detekt-config.yml"))
    autoCorrect = true
    ignoreFailures = true
}

tasks.detekt.configure {
    outputs.upToDateWhen { false }
    exclude("**/build/**")
    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.asFile.get().path}/reports/detekt/detekt-report.xml"))

        html.required.set(true)
        html.outputLocation.set(file("${layout.buildDirectory.asFile.get().path}/reports/detekt/detekt-report.html"))
    }
}
