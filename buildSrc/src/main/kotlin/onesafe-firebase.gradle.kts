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

import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    id("com.google.firebase.crashlytics")
}

/**
 * Make the Firebase crashlytics plugin works without play services deps & plugin
 *
 * https://github.com/firebase/firebase-android-sdk/issues/1560#issuecomment-862686404
 */
afterEvaluate {
    val variants = listOf(
        "${OSDimensions.Environment.Store.uppercaseFirstChar()}${OSDimensions.StoreChannel.Prod.uppercaseFirstChar()}",
        "${OSDimensions.Environment.Store.uppercaseFirstChar()}${OSDimensions.StoreChannel.Beta.uppercaseFirstChar()}",
        "${OSDimensions.Environment.Dev.uppercaseFirstChar()}${OSDimensions.StoreChannel.Beta.uppercaseFirstChar()}",
    )

    variants.forEach { variant ->
        tasks.named<com.google.firebase.crashlytics.buildtools.gradle.tasks.UploadMappingFileTask>(
            "uploadCrashlyticsMappingFile${variant}Release",
        ) {
            val valuesPath = "src/${variant.replaceFirstChar { it.lowercase() }}/res"

            // Set the property usually set by play services plugin
            val firebasePropDir: DirectoryProperty = project.objects.directoryProperty().fileValue(file(valuesPath))
            googleServicesResourceRoot.value(firebasePropDir)

            // Set all tasks dependencies to make gradle ok
            dependsOn(
                "merge${variant}ReleaseAssets",
                "write${variant}ReleaseAppMetadata",
                "compile${variant}ReleaseArtProfile",
                "processApplicationManifest${variant}ReleaseForBundle",
                "compress${variant}ReleaseAssets",
                "l8DexDesugarLib${variant}Release",
                "optimize${variant}ReleaseResources",
                "lintVitalReport${variant}Release",
                "create${variant}ReleaseApkListingFileRedirect",
                "merge${variant}ReleaseJniLibFolders",
                "package${variant}Release",
                "merge${variant}ReleaseNativeLibs",
                "write${variant}ReleaseApplicationId",
                "extract${variant}ReleaseNativeSymbolTables",
                "create${variant}ReleaseVariantModel",
                "merge${variant}ReleaseNativeDebugMetadata",
            )
        }
    }
}
