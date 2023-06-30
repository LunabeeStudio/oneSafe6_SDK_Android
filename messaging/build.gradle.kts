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
 * Created by Lunabee Studio / Date - 5/23/2023 - for the oneSafe6 SDK.
 * Last modified 5/23/23, 4:21 PM
 */

plugins {
    `android-library`
}

android {
    namespace = "studio.lunabee.onesafe.messaging"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "_"
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lbccore)

    implementation(platform(libs.compose.beta.bom))
    implementation(AndroidX.compose.ui)
    debugImplementation(AndroidX.compose.ui.tooling)
    implementation(AndroidX.compose.ui.toolingPreview)
    implementation(AndroidX.compose.material3)
    implementation(AndroidX.paging.compose)
    implementation(AndroidX.hilt.navigationCompose)
    implementation(AndroidX.lifecycle.runtime.compose)

    implementation(KotlinX.coroutines.android)
    coreLibraryDesugaring(Android.tools.desugarJdkLibs)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":error"))
    implementation(project(":domain"))
    implementation(project(":bubbles"))
    implementation(project(":messaging-domain"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
}
