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
 * Created by Lunabee Studio / Date - 5/12/2023 - for the oneSafe6 SDK.
 * Last modified 5/12/23, 9:25 AM
 */

plugins {
    `android-library`
}

android {
    namespace = "studio.lunabee.onesafe.ime"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
    }

    buildFeatures {
        compose = true
    }

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "_"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(Android.tools.desugarJdkLibs)

    api(libs.florisboard)

    implementation(libs.lbextensions)
    implementation(libs.lbcore)
    implementation(libs.lbccore)
    implementation(libs.protobuf.kotlinlite)

    implementation(platform(libs.compose.beta.bom))
    implementation(AndroidX.hilt.navigationCompose)
    implementation(AndroidX.compose.ui)
    debugImplementation(AndroidX.compose.ui.tooling)
    implementation(AndroidX.compose.ui.toolingPreview)
    implementation(AndroidX.compose.material3)
    implementation(AndroidX.navigation.compose)
    implementation(AndroidX.lifecycle.runtime.compose)
    implementation(JakeWharton.timber)
    implementation(COIL.compose)
    implementation(AndroidX.paging.compose)
    implementation(AndroidX.Activity.ktx)
    implementation(AndroidX.biometric)
    implementation(AndroidX.startup.runtime)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":bubbles"))
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":messaging"))
    implementation(project(":messaging-domain"))
    implementation(project(":app:settings"))
    implementation(project(":app:migration"))
    implementation(project(":error"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(Testing.MockK.android)
}
