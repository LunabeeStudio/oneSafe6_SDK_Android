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

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "studio.lunabee.onesafe.test.android"

    compileSdk = AndroidConfig.CompileSdk

    defaultConfig {
        minSdk = AndroidConfig.MinLibSdk
        missingDimensionStrategy("crypto", AndroidConfig.CryptoBackendFlavorDefault)
    }

    sourceSets["main"].resources {
        srcDir("${rootDir.path}/oneSafe6_common/archive/test")
    }

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    implementation(libs.androidx.test.core.ktx)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.compose.ui.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.emoji2)
    implementation(libs.emoji2.bundled)
    implementation(libs.espresso.core)
    implementation(libs.hilt.android.testing)
    api(libs.hilt.work)
    api(libs.lbcandroidtest)
    implementation(libs.lbcore)
    implementation(libs.lbextensions.android)
    implementation(libs.lblogger)
    api(libs.mockk.android)
    implementation(libs.protobuf.kotlinlite)
    implementation(libs.room.ktx)
    implementation(libs.work.testing)

    api(project(":common-test"))
    api(project(":crypto-android"))
    implementation(projects.app.coreUi)
    implementation(projects.app.settings)
    implementation(projects.domainJvm)
    api(projects.localAndroid)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(ProjectConfig.JvmTarget)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JdkVersion.toString()))
    }
}
