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
}

android {
    namespace = "studio.lunabee.onesafe.importexport"

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":import-export:proto"))
    implementation(project(":domain"))
    implementation(project(":error"))
    implementation(project(":common"))

    implementation(platform(libs.lunabee.bom))
    api(libs.lbcore)
    implementation(libs.lblogger)

    coreLibraryDesugaring(Android.tools.desugarJdkLibs)

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(project(":dependency-injection:test-component"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(libs.lblogger.timber)
}
