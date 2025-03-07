/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 1/17/2025 - for the oneSafe6 SDK.
 * Last modified 1/17/25, 10:43 AM
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.lbDokka)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
}

dependencies {
    dokka(projects.oneSafe6KMP.bubblesDomain)
    dokka(projects.oneSafe6KMP.bubblesRepository)
    dokka(projects.oneSafe6KMP.common)
    dokka(projects.oneSafe6KMP.crypto)
    dokka(projects.oneSafe6KMP.domain)
    dokka(projects.oneSafe6KMP.error)
    dokka(projects.oneSafe6KMP.messagingDomain)
    dokka(projects.oneSafe6KMP.messagingRepository)
    dokka(projects.oneSafe6KMP.shared)
}

dokka {
    moduleName.set("oneSafe 6 multiplatform documentation")

    dokkaPublications.html {
        includes.from("Docs.md")
    }
}
