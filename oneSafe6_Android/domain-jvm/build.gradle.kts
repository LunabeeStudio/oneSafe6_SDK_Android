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
    alias(libs.plugins.kotlin.serialization)
}

description = "Business layer of oneSafe"

dependencies {
    implementation(platform(libs.lunabee.bom))

    api(libs.paging.common.ktx)
    implementation(libs.doubleratchet)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lbcore)
    implementation(libs.lbextensions)
    implementation(libs.lblogger)
    implementation(libs.nbvcxz)
    testImplementation(libs.mockk)

    api(projects.oneSafe6KMP.domain)
    implementation(projects.commonJvm)
    implementation(projects.oneSafe6KMP.bubblesDomain)
    implementation(projects.oneSafe6KMP.error)
    testImplementation(project(":common-test"))
}
