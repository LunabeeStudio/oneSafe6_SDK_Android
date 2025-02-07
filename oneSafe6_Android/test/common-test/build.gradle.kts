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
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(platform(libs.lunabee.bom))

    api(libs.junit4)
    api(libs.kotlin.test)
    api(libs.kotlinx.coroutines.test)
    api(libs.threeten.extra)
    implementation(libs.annotation)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lbcore)
    implementation(libs.mockk)

    implementation(projects.domainJvm)
    implementation(projects.importExportDomain)
    implementation(projects.oneSafe6KMP.error)
}
