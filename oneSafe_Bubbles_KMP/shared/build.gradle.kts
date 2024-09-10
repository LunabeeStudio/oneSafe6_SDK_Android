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
    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.library")
    `lunabee-publish`
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.skie)
}

android {
    namespace = "studio.lunabee.bubbles.di"
    compileSdk = ProjectConfig.COMPILE_SDK

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

group = "studio.lunabee.bubbles"
description = "DI entry point for ios app"
version = "0.0.1-SNAPSHOT"

kotlin {
    androidTarget()
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "bubbles"
            export(project(":oneSafe_Bubbles_KMP:bubbles-domain"))
            export(project(":oneSafe_Bubbles_KMP:bubbles-repository"))
            export(project(":oneSafe_Bubbles_KMP:messaging-domain"))
            export(project(":oneSafe_Bubbles_KMP:messaging-repository"))
            export(project(":oneSafe_Bubbles_KMP:error"))
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.doubleratchet)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.lunabee.bom))
            implementation(libs.lbcore)
            implementation(libs.lblogger)

            api(project(":oneSafe_Bubbles_KMP:bubbles-domain"))
            api(project(":oneSafe_Bubbles_KMP:bubbles-repository"))
            api(project(":oneSafe_Bubbles_KMP:messaging-domain"))
            api(project(":oneSafe_Bubbles_KMP:messaging-repository"))
            api(project(":oneSafe_Bubbles_KMP:error"))
        }
        iosMain.dependencies { implementation(libs.koin.core) }

        androidMain.dependencies {
            implementation(libs.dagger.hilt.android)
        }
    }
}

dependencies {
    add("kspAndroid", libs.dagger.hilt.android.compiler)
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}
