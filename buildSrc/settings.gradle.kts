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

import org.gradle.internal.management.VersionCatalogBuilderInternal

pluginManagement {
    plugins {
        id("de.fayard.refreshVersions") version "0.60.5"
    }
}

dependencyResolutionManagement {
    // Share versions with KMP project
    // https://stackoverflow.com/questions/73646181/gradle-version-catalogue-share-a-version-between-multiple-toml-files
    versionCatalogs {
        val kmpLibsBuilder: VersionCatalogBuilder = create("versions") {
            from(files("../oneSafe6_KMP/gradle/libs.versions.toml")) // load versions
        }

        create("libs") {
            from(files("../gradle/libs.versions.toml"))
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
    id("de.fayard.refreshVersions")
}
