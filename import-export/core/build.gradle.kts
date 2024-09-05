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
    `kotlin-library`
    `onesafe-publish`
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)
    implementation(libs.doubleratchet)
    implementation(libs.bubbles.domain)
    implementation(libs.bubbles.messaging.domain)
    implementation(libs.bubbles.repository)
    implementation(libs.kotlinx.datetime)
    implementation(project(":domain"))
    implementation(project(":error"))
    implementation(project(":common"))
    implementation(project(":import-export-proto"))
    implementation(project(":import-export-domain"))
}
