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
    namespace = "studio.lunabee.onesafe.bubbles.crypto"

    defaultConfig {
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
    }

    packaging {
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(platform(libs.lunabee.bom))

    androidTestImplementation(libs.kotlinx.coroutines.test)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bouncycastle)
    implementation(libs.doubleratchet)
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    androidTestImplementation(project(":common-test"))
    androidTestImplementation(projects.oneSafe6KMP.bubblesRepository)
    implementation(project(":crypto-android"))
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.error)
}
