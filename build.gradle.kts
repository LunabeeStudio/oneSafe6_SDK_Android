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

import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    alias(libs.plugins.lbDetekt)
}

lbDetekt {
    config.setFrom(files(lunabeeConfig, "$projectDir/onesafe-detekt-config.yml"))
}

setupTestTask()

fun Project.setupTestTask() {
    // TODO to improve if possible
    //  Ideally, everything should be handle with external scripts on CI side
    tasks.register("allTests") {
        group = "verification"
        description = "Run all gradle unit tests of application and every modules"
    }

    tasks.register("allConnectedTests") {
        group = "verification"
        description = "Run all Android connected test of application and every modules"
    }

    val allTestsTask: Task? = tasks.findByName("allTests")
    val allConnectedTestsTask: Task? = tasks.findByName("allConnectedTests")
    val excludedTestProjects: List<String> = listOf(
        "Commons_OS6",
        "common-test-android",
        "benchmark-android",
        "macrobenchmark-android",
        "dependency-injection",
        "test-component",
        "mockos5",
        "core-ui:checks",
        "crashlytics-dummy",
        "oneSafe6_KMP:common",
    )

    subprojects {
        afterEvaluate {
            var ancestor = parent
            var qualifiedName = name
            while (ancestor != rootProject && ancestor != null) {
                qualifiedName = "${ancestor.name}:$qualifiedName"
                ancestor = ancestor.parent
            }
            if (excludedTestProjects.any { qualifiedName.contains(it) } || !project.buildFile.exists()) {
                return@afterEvaluate
            }

            val isAndroidLibrary = extensions.findByType<com.android.build.gradle.LibraryExtension>() != null
            val isApp = extensions.findByType<com.android.build.gradle.AppExtension>() != null
            val isKmp = extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>() != null
            val hasEnvironmentFlavor = project.name == "import-export-android" ||
                project.name == "ime-android" ||
                project.name == "migration" ||
                project.name == "login" ||
                project.name == "help"
            val isCryptoModule = project.name == "crypto-android"

            val envFlavor = OSDimensions.Environment.Store.uppercaseFirstChar()
            val appVariant = "$envFlavor${OSDimensions.StoreChannel.Prod.uppercaseFirstChar()}"
            val testTaskNames: List<String> = when {
                isApp -> listOf("app:test${appVariant}ReleaseUnitTest")
                isKmp -> listOf("$qualifiedName:allTests")
                hasEnvironmentFlavor -> listOf("$qualifiedName:test${envFlavor}ReleaseUnitTest")
                isCryptoModule -> listOf(
                    "$qualifiedName:testJceReleaseUnitTest",
                    "$qualifiedName:testTinkReleaseUnitTest",
                )
                isAndroidLibrary -> listOf("$qualifiedName:testReleaseUnitTest")
                else -> listOf("$qualifiedName:test")
            }

            val androidTestTaskNames: List<String>? = when {
                isApp -> listOf("app:connected${appVariant}DebugAndroidTest")
                hasEnvironmentFlavor -> listOf("$qualifiedName:connected${envFlavor}DebugAndroidTest")
                isCryptoModule -> listOf(
                    "$qualifiedName:connectedJceDebugAndroidTest",
                    "$qualifiedName:connectedTinkDebugAndroidTest",
                )
                isAndroidLibrary -> listOf("$qualifiedName:connectedDebugAndroidTest")
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
}
