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
 * Created by Lunabee Studio / Date - 5/25/2023 - for the oneSafe6 SDK.
 * Last modified 5/25/23, 2:43 PM
 */

plugins {
    `android-library`
    `onesafe-publish`
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "studio.lunabee.onesafe.commonui"

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.lbcandroidtest)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.biometric)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.emoji2)
    implementation(libs.emoji2.bundled)
    implementation(libs.hilt.android)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lbextensions)
    implementation(libs.lbextensions.android)
    implementation(libs.lbloading.compose)
    implementation(libs.lblogger)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lottie)
    implementation(libs.navigation.compose)
    implementation(libs.palette.ktx)
    implementation(libs.process.phoenix)
    implementation(libs.work.runtime)

    implementation(projects.app.coreUi)
    implementation(projects.domainJvm)
    implementation(projects.oneSafe6KMP.error)
    lintPublish(project(":app:common-ui:checks"))
}
