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

import org.gradle.internal.management.VersionCatalogBuilderInternal

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Share versions with KMP project
    // https://stackoverflow.com/questions/73646181/gradle-version-catalogue-share-a-version-between-multiple-toml-files
    versionCatalogs {
        val kmpLibsBuilder: VersionCatalogBuilder = create("versions") {
            from(files("oneSafe6_KMP/gradle/libs.versions.toml")) // load versions
        }

        create("libs") {
            val kmpLibs = (kmpLibsBuilder as VersionCatalogBuilderInternal).build()
            kmpLibs.versionAliases.forEach { alias ->
                // inject version to this catalog
                val version = kmpLibs.getVersion(alias).version
                println("Inject version $version for alias $alias")
                version(alias) {
                    strictly(version.strictVersion)
                    require(version.requiredVersion)
                    prefer(version.preferredVersion)
                    version.rejectedVersions.forEach { reject(it) }
                }
            }
        }
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.5"
}

refreshVersions {
    // FIXME move refreshVersions config to Bubbles_KMP to avoid override
    //  https://github.com/Splitties/refreshVersions/issues/723
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
include("app:login")
include("app:help")
include(":domain-jvm")
include("repository")
include("crypto-android")
include("crypto-android:checks")
include("local-android")
include("common-jvm")
include("remote")
include("benchmark-android")
include("macrobenchmark-android")
include("dependency-injection")
include("dependency-injection:test-component")
include("app:message-companion")
include("app:common-ui")
include("app:common-ui:checks")

include("common-test")
project(":common-test").projectDir = File("test/common-test")
include("common-test-android")
project(":common-test-android").projectDir = File("test/common-test-android")
include("common-test-robolectric")
project(":common-test-robolectric").projectDir = File("test/common-test-robolectric")

include("import-export-core")
project(":import-export-core").projectDir = File("import-export/core")
include("import-export-proto")
project(":import-export-proto").projectDir = File("import-export/proto")
include(":import-export-domain")
project(":import-export-domain").projectDir = File("import-export/domain")
include(":import-export-drive")
project(":import-export-drive").projectDir = File("import-export/drive")
include(":import-export-repository")
project(":import-export-repository").projectDir = File("import-export/repository")
include(":import-export-android")
project(":import-export-android").projectDir = File("import-export/android")

include("bubbles")
include(":bubbles-crypto-android")
project(":bubbles-crypto-android").projectDir = File("bubbles/crypto-android")

include("messaging")

include(":ime-android")
project(":ime-android").projectDir = File("ime/android")
include(":ime-domain")
project(":ime-domain").projectDir = File("ime/domain")

includeBuild("oneSafe6_KMP")

// include(":mockos5") // mockos5 apk is embedded for tests
