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
    namespace = "studio.lunabee.onesafe.importexport.drive"

    packaging {
        resources {
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }

    buildTypes {
        release {
            consumerProguardFile("consumer-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(platform(libs.lunabee.bom))

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.accompanist.permissions)
    implementation(libs.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.hilt.android)
    implementation(libs.kotlin.stdlib)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lblogger)
    implementation(libs.play.services.auth.base) // required to get GoogleAuthUtil class

    implementation(project(":import-export-repository"))
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.oneSafe6KMP.error)
}
