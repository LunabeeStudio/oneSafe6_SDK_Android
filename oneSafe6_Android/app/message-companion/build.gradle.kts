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
 * Created by Lunabee Studio / Date - 5/11/2023 - for the oneSafe6 SDK.
 * Last modified 5/11/23, 4:23 PM
 */

plugins {
    `android-library`
}

android {
    namespace = "studio.lunabee.onesafe.messagecompanion"

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = AndroidConfig.MIN_APP_SDK
        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"
        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
        missingDimensionStrategy(OSDimensions.Environment.value, OSDimensions.Environment.Store)
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.pickFirsts += "META-INF/LICENSE.md"
        resources.pickFirsts += "META-INF/LICENSE-notice.md"
        resources.pickFirsts += "META-INF/DEPENDENCIES"
        resources.pickFirsts += "META-INF/INDEX.LIST"
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
    }
}

dependencies {
    implementation(platform(libs.lunabee.bom))

    androidTestImplementation(libs.room.testing)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.android.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.lbcore)
    implementation(libs.lblogger)
    implementation(libs.protobuf.kotlinlite)

    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":remote"))
    androidTestImplementation(project(":repository"))
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.localAndroid)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    implementation(projects.domainJvm)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.error)
}

tasks.register("cleanProtobuf") {
    doLast {
        delete(file("build/generated/source/proto/"))
    }
}

tasks.getByName("clean") {
    dependsOn("cleanProtobuf")
}
