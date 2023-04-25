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
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

version = AndroidConfig.ONESAFE_SDK_VERSION
group = ProjectConfig.GROUP_ID

android {
    compileSdk = AndroidConfig.COMPILE_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_LIB_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(Google.dagger.hilt.android)
    kapt(Google.dagger.hilt.compiler)
    kaptAndroidTest(Google.dagger.hilt.compiler)

    testImplementation(Testing.junit.jupiter)
    testImplementation(Kotlin.test)

    androidTestImplementation(Kotlin.test)
    androidTestImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.coreKtx)
    androidTestImplementation(AndroidX.test.runner)
    androidTestImplementation(Google.dagger.hilt.android.testing)
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
