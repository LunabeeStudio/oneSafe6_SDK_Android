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

plugins {
    id("com.android.application")
    id("kotlin-android")
}

version = AndroidConfig.envVersionName
group = ProjectConfig.GROUP_ID

android {
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            minSdk = AndroidConfig.MIN_DEV_APP_SDK

            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "#${AndroidConfig.envVersionCode} dev"

            buildConfigField("Boolean", "IS_DEV", "true")
            buildConfigField("String", "FIREBASE_APP_ID", "\"1:874555585125:android:7475d5fb91f686aad58c8c\"")
        }

        create("prod") {
            minSdk = AndroidConfig.MIN_APP_SDK
            dimension = "environment"

            buildConfigField("Boolean", "IS_DEV", "false")
            buildConfigField("String", "FIREBASE_APP_ID", "\"1:874555585125:android:3a9f446ab5ba07abd58c8c\"")
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = ProjectConfig.JDK_VERSION.toString()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JDK_VERSION.toString()))
    }
}
