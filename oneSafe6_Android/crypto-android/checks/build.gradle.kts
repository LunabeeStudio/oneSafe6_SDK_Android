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
 * Created by Lunabee Studio / Date - 5/4/2023 - for the oneSafe6 SDK.
 * Last modified 5/4/23, 2:54 PM
 */

plugins {
    id("java-library")
    id("kotlin")
}

description = "Lint rules for oneSafe crypto"

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.lint.api)
    testImplementation(libs.lint.tests)
    testImplementation(projects.domainJvm)
}

java {
    sourceCompatibility = ProjectConfig.JDK_VERSION
    targetCompatibility = ProjectConfig.JDK_VERSION
}
