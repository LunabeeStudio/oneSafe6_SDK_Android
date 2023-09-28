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
    `kotlin-library`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.lunabee.bom))
    implementation(libs.lbcore)
    implementation(libs.lblogger)

    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":error"))

    testImplementation(libs.mockk)
    testImplementation(project(":common-test"))
}
