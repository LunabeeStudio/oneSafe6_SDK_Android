/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/21/2024 - for the oneSafe6 SDK.
 * Last modified 8/21/24, 10:22 AM
 */

import de.fayard.refreshVersions.core.StabilityLevel

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.60.5"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

refreshVersions {
    // FIXME copy refreshVersion config from root project to avoid override
    //  https://github.com/Splitties/refreshVersions/issues/723

    @Suppress("UnstableApiUsage")
    this.rejectVersionIf {
        val excludeOldDriveVersioning = this.moduleId.group == "com.google.apis" &&
            this.moduleId.name == "google-api-services-drive" &&
            this.candidate.value.contains("^v3-rev\\d\\d?-".toRegex())

        // FIXME workaround https://github.com/Splitties/refreshVersions/issues/223
        val excludeLbPreviousSnapshot =
            this.candidate.value.startsWith(this.current.value) && this.candidate.stabilityLevel == StabilityLevel.Snapshot

        excludeOldDriveVersioning || excludeLbPreviousSnapshot
    }
    featureFlags {
        enable(de.fayard.refreshVersions.core.FeatureFlag.LIBS)
    }
}

rootProject.name = "oneSafe Bubbles KMP"
include(":oneSafe_Bubbles_KMP:bubbles-domain")
project(":oneSafe_Bubbles_KMP:bubbles-domain").projectDir = File("bubbles-domain")

include(":oneSafe_Bubbles_KMP:bubbles-repository")
project(":oneSafe_Bubbles_KMP:bubbles-repository").projectDir = File("bubbles-repository")

include(":oneSafe_Bubbles_KMP:messaging-domain")
project(":oneSafe_Bubbles_KMP:messaging-domain").projectDir = File("messaging-domain")

include(":oneSafe_Bubbles_KMP:messaging-repository")
project(":oneSafe_Bubbles_KMP:messaging-repository").projectDir = File("messaging-repository")

include(":oneSafe_Bubbles_KMP:shared")
project(":oneSafe_Bubbles_KMP:shared").projectDir = File("shared")

include(":oneSafe_Bubbles_KMP:error")
project(":oneSafe_Bubbles_KMP:error").projectDir = File("error")
