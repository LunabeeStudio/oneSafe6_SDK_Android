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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("VariableNaming")
pluginManagement {
    val artifactory_consumer_username: String? by settings
    val artifactory_consumer_api_key: String? by settings

    val artifactoryUsername: String = artifactory_consumer_username
        ?: "library-consumer-public"
    val artifactoryPassword: String = artifactory_consumer_api_key
        ?: "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://artifactory.lunabee.studio/artifactory/lunabee-gradle-plugin/")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
        mavenLocal()
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.6"
    id("studio.lunabee.plugins.cache") version "1.0.0"
}

rootProject.name = "oneSafe6"

include(":Commons_OS6")
project(":Commons_OS6").projectDir = File("oneSafe6_common")
include(":docs")
include("app")
project(":app").projectDir = File("oneSafe6_Android/app")
include("app:core-ui")
project(":app:core-ui").projectDir = File("oneSafe6_Android/app/core-ui")
include("app:core-ui:checks")
project(":app:core-ui:checks").projectDir = File("oneSafe6_Android/app/core-ui/checks")
include("app:settings")
project(":app:settings").projectDir = File("oneSafe6_Android/app/settings")
include("app:migration")
project(":app:migration").projectDir = File("oneSafe6_Android/app/migration")
include("app:login")
project(":app:login").projectDir = File("oneSafe6_Android/app/login")
include("app:help")
project(":app:help").projectDir = File("oneSafe6_Android/app/help")
include(":domain-jvm")
project(":domain-jvm").projectDir = File("oneSafe6_Android/domain-jvm")
include("repository")
project(":repository").projectDir = File("oneSafe6_Android/repository")
include("crypto-android")
project(":crypto-android").projectDir = File("oneSafe6_Android/crypto-android")
include("crypto-android:checks")
project(":crypto-android:checks").projectDir = File("oneSafe6_Android/crypto-android/checks")
include("local-android")
project(":local-android").projectDir = File("oneSafe6_Android/local-android")
include("common-jvm")
project(":common-jvm").projectDir = File("oneSafe6_Android/common-jvm")
include("remote")
project(":remote").projectDir = File("oneSafe6_Android/remote")
include("benchmark-android")
project(":benchmark-android").projectDir = File("oneSafe6_Android/benchmark-android")
include("macrobenchmark-android")
project(":macrobenchmark-android").projectDir = File("oneSafe6_Android/macrobenchmark-android")
include("dependency-injection")
project(":dependency-injection").projectDir = File("oneSafe6_Android/dependency-injection")
include("dependency-injection:test-component")
project(":dependency-injection:test-component").projectDir = File("oneSafe6_Android/dependency-injection/test-component")
include("app:message-companion")
project(":app:message-companion").projectDir = File("oneSafe6_Android/app/message-companion")
include("app:common-ui")
project(":app:common-ui").projectDir = File("oneSafe6_Android/app/common-ui")
include("app:common-ui:checks")
project(":app:common-ui:checks").projectDir = File("oneSafe6_Android/app/common-ui/checks")
include("common-protobuf")
project(":common-protobuf").projectDir = File("oneSafe6_Android/common-protobuf")
include(":widget-android")
project(":widget-android").projectDir = File("oneSafe6_Android/widget-android")

include("common-test")
project(":common-test").projectDir = File("oneSafe6_Android/test/common-test")
include("common-test-android")
project(":common-test-android").projectDir = File("oneSafe6_Android/test/common-test-android")
include("common-test-robolectric")
project(":common-test-robolectric").projectDir = File("oneSafe6_Android/test/common-test-robolectric")

include("import-export-core")
project(":import-export-core").projectDir = File("oneSafe6_Android/import-export/core")
include("import-export-proto")
project(":import-export-proto").projectDir = File("oneSafe6_Android/import-export/proto")
include(":import-export-domain")
project(":import-export-domain").projectDir = File("oneSafe6_Android/import-export/domain")
include(":import-export-drive")
project(":import-export-drive").projectDir = File("oneSafe6_Android/import-export/drive")
include(":import-export-repository")
project(":import-export-repository").projectDir = File("oneSafe6_Android/import-export/repository")
include(":import-export-android")
project(":import-export-android").projectDir = File("oneSafe6_Android/import-export/android")

include("bubbles")
project(":bubbles").projectDir = File("oneSafe6_Android/bubbles")
include(":bubbles-crypto-android")
project(":bubbles-crypto-android").projectDir = File("oneSafe6_Android/bubbles/crypto-android")

include("messaging")
project(":messaging").projectDir = File("oneSafe6_Android/messaging")

include(":ime-android")
project(":ime-android").projectDir = File("oneSafe6_Android/ime/android")
include(":ime-domain")
project(":ime-domain").projectDir = File("oneSafe6_Android/ime/domain")

// mockos5 apk is embedded for tests
// include(":mockos5")
// project(":mockos5").projectDir = File("oneSafe6_Android/mockos5")

// KMP
include(":oneSafe6_KMP:bubbles-domain")
project(":oneSafe6_KMP:bubbles-domain").projectDir = File("oneSafe6_KMP/bubbles/domain")

include(":oneSafe6_KMP:bubbles-repository")
project(":oneSafe6_KMP:bubbles-repository").projectDir = File("oneSafe6_KMP/bubbles/repository")

include(":oneSafe6_KMP:messaging-domain")
project(":oneSafe6_KMP:messaging-domain").projectDir = File("oneSafe6_KMP/messaging/domain")

include(":oneSafe6_KMP:messaging-repository")
project(":oneSafe6_KMP:messaging-repository").projectDir = File("oneSafe6_KMP/messaging/repository")

include(":oneSafe6_KMP:shared")
project(":oneSafe6_KMP:shared").projectDir = File("oneSafe6_KMP/shared")

include(":oneSafe6_KMP:error")
project(":oneSafe6_KMP:error").projectDir = File("oneSafe6_KMP/error")

include(":oneSafe6_KMP:domain")
project(":oneSafe6_KMP:domain").projectDir = File("oneSafe6_KMP/domain")

include(":oneSafe6_KMP:crypto")
project(":oneSafe6_KMP:crypto").projectDir = File("oneSafe6_KMP/crypto")

include(":oneSafe6_KMP:common")
project(":oneSafe6_KMP:common").projectDir = File("oneSafe6_KMP/common")

include(":oneSafe6_KMP:crashlytics")
project(":oneSafe6_KMP:crashlytics").projectDir = File("oneSafe6_KMP/crashlytics")

include(":oneSafe6_KMP:crashlytics-dummy")
project(":oneSafe6_KMP:crashlytics-dummy").projectDir = File("oneSafe6_KMP/crashlyticsDummy")
