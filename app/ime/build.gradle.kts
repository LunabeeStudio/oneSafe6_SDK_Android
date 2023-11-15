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
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "studio.lunabee.onesafe.ime"

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)

        val imeProcessName = ":ime"
        buildConfigField("String", "IME_PROCESS_NAME", "\"$imeProcessName\"")
        manifestPlaceholders["imeProcessName"] = imeProcessName
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev")
        create("prod")
    }

    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
        resources.pickFirsts.add("META-INF/DEPENDENCIES")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    api(libs.florisboard)

    implementation(libs.lbextensions)
    implementation(libs.lbcore)
    implementation(libs.lbccore)
    implementation(libs.protobuf.kotlinlite)

    implementation(platform(libs.compose.beta.bom))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.paging.compose)
    implementation(libs.activity.ktx)
    implementation(libs.biometric)
    implementation(libs.startup.runtime)

    implementation(project(":app:core-ui"))
    implementation(project(":app:common-ui"))
    implementation(project(":app:login"))
    implementation(project(":bubbles"))
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":messaging"))
    implementation(project(":messaging-domain"))
    implementation(project(":app:settings"))
    implementation(project(":app:migration"))
    implementation(project(":error"))

    androidTestImplementation(project(":common-test-android"))
    androidTestImplementation(libs.mockk.android)
}
