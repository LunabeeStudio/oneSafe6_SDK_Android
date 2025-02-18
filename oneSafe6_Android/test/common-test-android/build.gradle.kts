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

    compileSdk = AndroidConfig.COMPILE_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_LIB_SDK
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
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

    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    api(libs.hilt.work)
    api(libs.lbcandroidtest)
    api(libs.mockk.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.compose.ui.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.emoji2)
    implementation(libs.emoji2.bundled)
    implementation(libs.hilt.android.testing)
    implementation(libs.lbcore)
    implementation(libs.lbextensions.android)
    implementation(libs.lblogger)
    implementation(libs.protobuf.kotlinlite)
    implementation(libs.room.ktx)
    implementation(libs.work.testing)
    implementation(libs.espresso.core)

    api(project(":common-test"))
    api(project(":crypto-android"))
    api(projects.localAndroid)
    implementation(projects.app.settings)
    implementation(projects.domainJvm)
    implementation(projects.app.coreUi)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(ProjectConfig.JVM_TARGET)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JDK_VERSION.toString()))
    }
}
