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
    `onesafe-publish`
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "studio.lunabee.onesafe.importexport.android"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
        resources.pickFirsts.add("META-INF/DEPENDENCIES")
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
}

kapt {
    correctErrorTypes = true
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.hilt.android)
    kapt(libs.androidx.hilt.compiler)
    kapt(libs.dagger.hilt.compiler)

    implementation(libs.hilt.work)
    implementation(libs.work.runtime)
    implementation(libs.timber)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.accompanist.permissions)

    implementation(platform(libs.compose.beta.bom))
    implementation(libs.compose.material3)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    implementation(libs.lbccore)

    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    implementation(project(":import-export-core"))
    implementation(project(":import-export-proto"))
    implementation(project(":import-export-domain"))
    implementation(project(":domain"))
    implementation(project(":error"))
    implementation(project(":common"))
    implementation(project(":app:common-ui"))
    implementation(project(":app:core-ui"))
    implementation(project(":app:settings"))

    kaptAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(project(":dependency-injection"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":app:settings"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(libs.lblogger.timber)
}
