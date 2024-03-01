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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

plugins {
    id("com.android.application")
    id("kotlin-android")
}

version = AndroidConfig.envVersionName
group = ProjectConfig.GROUP_ID

android {
    flavorDimensions += listOf(OSDimensions.Environment.value, OSDimensions.StoreChannel.value)
    productFlavors {
        val customBackupMimetype = "application/onesafe6"

        create(OSDimensions.Environment.Dev) {
            minSdk = AndroidConfig.MIN_APP_SDK

            dimension = OSDimensions.Environment.value
            applicationIdSuffix = ".dev"
            versionNameSuffix = "#${AndroidConfig.envVersionCode} dev"

            manifestPlaceholders["customBackupMimetype"] = customBackupMimetype

            buildConfigField("Boolean", "IS_DEV", "true")
            buildConfigField("Boolean", "ENABLE_FIREBASE", "true")
            buildConfigField("String", "CUSTOM_BACKUP_MIMETYPE", "\"$customBackupMimetype\"")
        }

        create(OSDimensions.Environment.Store) {
            minSdk = AndroidConfig.MIN_APP_SDK
            dimension = OSDimensions.Environment.value

            // TODO prod backup mimetype inverted with _dev
            //  Verify if a migration (or something else) is needed to update cloud backups
            val customBackupMimetypeDev = "${customBackupMimetype}_dev"
            manifestPlaceholders["customBackupMimetype"] = customBackupMimetypeDev

            buildConfigField("Boolean", "IS_DEV", "false")
            buildConfigField("String", "CUSTOM_BACKUP_MIMETYPE", "\"$customBackupMimetypeDev\"")
        }

        create(OSDimensions.StoreChannel.Beta) {
            dimension = OSDimensions.StoreChannel.value
            versionNameSuffix = "#${AndroidConfig.envVersionCode} beta"

            buildConfigField("Boolean", "ENABLE_FIREBASE", "true")
            buildConfigField("Boolean", "IS_BETA", "true")
        }

        create(OSDimensions.StoreChannel.Prod) {
            dimension = OSDimensions.StoreChannel.value

            buildConfigField("Boolean", "ENABLE_FIREBASE", "false")
            buildConfigField("Boolean", "IS_BETA", "false")
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JDK_VERSION
        targetCompatibility = ProjectConfig.JDK_VERSION
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        // devBeta -> day by day dev
        // storeBeta -> public beta release
        // storeProd -> public release
        if (variantBuilder.flavorName?.lowercase() in listOf(
                "${OSDimensions.Environment.Dev}${OSDimensions.StoreChannel.Prod}",
            )
        ) {
            variantBuilder.enable = false
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = ProjectConfig.JDK_VERSION.toString()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JDK_VERSION.toString()))
    }
}
