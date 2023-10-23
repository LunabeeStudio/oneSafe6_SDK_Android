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
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "studio.lunabee.onesafe.messaging"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
        resources.pickFirsts.add("META-INF/DEPENDENCIES")
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lbccore)

    implementation(libs.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    kaptAndroidTest(libs.dagger.hilt.compiler)

    implementation(platform(libs.compose.beta.bom))
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.paging.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.kotlinx.coroutines.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.doubleratchet)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":app:settings"))
    implementation(project(":error"))
    implementation(project(":domain"))
    implementation(project(":bubbles"))
    implementation(project(":messaging-domain"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
}
