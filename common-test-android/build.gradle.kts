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
    `android-library`
}

android {
    namespace = "studio.lunabee.onesafe.test"

    compileSdk = AndroidConfig.COMPILE_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_LIB_SDK
        manifestPlaceholders["testTargetSdk"] = AndroidConfig.TARGET_SDK
    }

    sourceSets["main"].resources {
        srcDir("${rootDir.path}/oneSafe6_common/archive/test")
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(libs.hilt.android.testing)

    implementation(platform(libs.compose.beta.bom))
    implementation(libs.androidx.test.runner)
    implementation(libs.compose.ui.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.timber)
    implementation(libs.work.testing)
    api(libs.hilt.work)
    implementation(libs.mockk.android)

    implementation(libs.lunabee.bom)
    implementation(libs.lbcore)

    implementation(project(":domain"))
    implementation(project(":app:settings"))
    implementation(project(":app:migration"))
    api(project(":common-test"))
    api(libs.lbcandroidtest)
}
