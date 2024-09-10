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
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "studio.lunabee.messaging"
description = "Kotlin multiplatform implementation of oneSafe messaging"

kotlin {
    jvm()
    iosSimulatorArm64()
    iosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.doubleratchet)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.lunabee.bom))
            implementation(libs.lbcore)
            implementation(libs.lblogger)
            implementation(project(":oneSafe_Bubbles_KMP:messaging-domain"))
            implementation(project(":oneSafe_Bubbles_KMP:bubbles-domain"))
            implementation(project(":oneSafe_Bubbles_KMP:error"))
        }

        jvmMain.dependencies {
            implementation(libs.androidx.paging.common)
        }
    }
}
