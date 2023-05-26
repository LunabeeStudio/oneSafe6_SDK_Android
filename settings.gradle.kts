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
 * Last modified 4/7/23, 12:30 AM
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {
    featureFlags {
        enable(de.fayard.refreshVersions.core.FeatureFlag.LIBS)
    }
}

rootProject.name = "oneSafe 6"

include(":Commons_Android")
project(":Commons_Android").projectDir = File("Commons_Android/gradle")
include(":Commons_OS6")
project(":Commons_OS6").projectDir = File("oneSafe6_common")
include("app")
include("app:core-ui")
include("app:core-ui:checks")
include("app:settings")
include("app:migration")
include("app:ime")
include(":domain")
include(":repository")
include(":crypto-android")
include(":local-android")
include(":common-test-android")
include(":common-test")
include(":common")
include(":error")
include(":remote")
include(":benchmark-android")
include(":macrobenchmark-android")
include(":proto")
include(":import-export")
include(":dependency-injection")
include(":dependency-injection:test-component")
include(":app:message-companion")
// include(":mockos5") // mockos5 apk is embedded for tests
