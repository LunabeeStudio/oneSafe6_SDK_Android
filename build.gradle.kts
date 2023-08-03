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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Android.tools.build.gradlePlugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(Google.dagger.hilt.android.gradlePlugin)
        classpath(libs.protobuf.plugin)
        classpath("org.jetbrains.kotlin:kotlin-serialization:_")
    }
}

val artifactoryUsername: String = project.findProperty("artifactory_consumer_username") as? String
    ?: "library-consumer-public"
val artifactoryPassword: String = project.findProperty("artifactory_consumer_api_key") as? String
    ?: "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"

allprojects {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://androidx.dev/storage/compose-compiler/repository/")
        }
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/florisboard-library-local")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/double-ratchet-kmm/")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("androidx.benchmark") apply false
    id("com.android.test") apply false
}
apply("Commons_Android/gradle/pr-code-analysis-project.gradle")
tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    exclude("**/build/**")
}
detekt {
    config.setFrom(files("$projectDir/Commons_Android/lunabee-detekt-config.yml", "$projectDir/onesafe-detekt-config.yml"))
}

apply("Commons_Android/gradle/lunabee-root.gradle.kts")

// TODO to improve if possible
//  Ideally, everything should be handle with external scripts on CI side
tasks.create("allTests") {
    group = "verification"
    description = "Run all gradle unit tests of application and every modules"
}

tasks.create("allConnectedTests") {
    group = "verification"
    description = "Run all Android connected test of application and every modules"
}

val allTestsTask: Task? = tasks.findByName("allTests")
val allConnectedTestsTask: Task? = tasks.findByName("allConnectedTests")
val excludedTestProjects: List<String> = listOf(
    "Commons_Android",
    "Commons_OS6",
    "common-test",
    "common-test-android",
    "benchmark-android",
    "macrobenchmark-android",
    "dependency-injection",
    "test-component",
    "mockos5",
    "checks",
)

subprojects {
    afterEvaluate {
        if (excludedTestProjects.contains(project.name) || !project.buildFile.exists()) {
            return@afterEvaluate
        }

        project.tasks.withType<Test> {
            useJUnitPlatform()
        }

        val isAndroidLibrary = extensions.findByType<com.android.build.gradle.LibraryExtension>() != null
        val isApp = extensions.findByType<com.android.build.gradle.AppExtension>() != null
        val isCryptoModule = project.name == "crypto-android"

        var ancestor = parent
        var parentName = ""
        while (ancestor != rootProject && ancestor != null) {
            parentName += "${ancestor.name}:"
            ancestor = ancestor.parent
        }

        val testTaskNames: List<String> = when {
            isApp -> listOf("$parentName$name:testProdReleaseUnitTest")
            isCryptoModule -> listOf(
                "$parentName$name:testJceReleaseUnitTest",
                "$parentName$name:testTinkReleaseUnitTest",
            )
            isAndroidLibrary -> listOf("$parentName$name:testReleaseUnitTest")
            else -> listOf("$parentName$name:test")
        }

        val androidTestTaskNames: List<String>? = when {
            isCryptoModule -> listOf(
                "$parentName$name:connectedJceDebugAndroidTest",
                "$parentName$name:connectedTinkDebugAndroidTest",
            )
            isAndroidLibrary -> listOf("$parentName$name:connectedDebugAndroidTest")
            isApp -> listOf("$parentName$name:connectedProdDebugAndroidTest")
            else -> null
        }

        println("Add ${testTaskNames.joinToString()} to allTests task")
        testTaskNames.forEach { allTestsTask?.dependsOn(it) }

        androidTestTaskNames?.let { tasks ->
            println("Add ${tasks.joinToString()} to allConnectedTests task")
            tasks.forEach { allConnectedTestsTask?.dependsOn(it) }
        }
    }
}
