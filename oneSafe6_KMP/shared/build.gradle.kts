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
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.skie)
    alias(libs.plugins.lbDokka)
}

android {
    namespace = "studio.lunabee.onesafe.di"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_LIB_SDK
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

group = "studio.lunabee.bubbles"
description = "DI entry point for ios app"
version = "0.0.1-SNAPSHOT"

val activateCrashlytics: Boolean = System.getenv("KOTLIN_FRAMEWORK_ACTIVATE_CRASHLYTICS").toBoolean()

kotlin {
    androidTarget()
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "oneSafeKmp"
            export(project(":oneSafe6_KMP:bubbles-domain"))
            export(project(":oneSafe6_KMP:bubbles-repository"))
            export(project(":oneSafe6_KMP:messaging-domain"))
            export(project(":oneSafe6_KMP:messaging-repository"))
            export(project(":oneSafe6_KMP:error"))
            export(project(":oneSafe6_KMP:crypto"))
            isStatic = true
            binaryOption("bundleId", "oneSafeKmp")
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.lunabee.bom))

            implementation(libs.doubleratchet)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.lbcore)
            implementation(libs.lblogger)

            api(project(":oneSafe6_KMP:bubbles-domain"))
            api(project(":oneSafe6_KMP:bubbles-repository"))
            api(project(":oneSafe6_KMP:crypto"))
            api(project(":oneSafe6_KMP:error"))
            api(project(":oneSafe6_KMP:messaging-domain"))
            api(project(":oneSafe6_KMP:messaging-repository"))
        }
        iosMain.dependencies {
            implementation(libs.koin.core)

            if (activateCrashlytics) {
                implementation(project(":oneSafe6_KMP:crashlytics"))
            } else {
                implementation(project(":oneSafe6_KMP:crashlytics-dummy"))
            }
        }

        androidMain.dependencies {
            implementation(libs.hilt.android)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

dependencies {
    add("kspAndroid", libs.dagger.hilt.android.compiler)
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}
