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
    id("com.google.firebase.crashlytics")
}

/**
 * Make the Firebase crashlytics plugin works without play services deps & plugin
 *
 * https://github.com/firebase/firebase-android-sdk/issues/1560#issuecomment-862686404
 */
afterEvaluate {
    tasks.named<com.google.firebase.crashlytics.buildtools.gradle.tasks.UploadMappingFileTask>(
        "uploadCrashlyticsMappingFileProdRelease",
    ) {
        // Set the property usually set by play services plugin
        val firebasePropDir: DirectoryProperty = project.objects.directoryProperty().fileValue(file("firebase/prod"))
        googleServicesResourceRoot.value(firebasePropDir)

        // Set all tasks dependencies to make gradle ok
        dependsOn(
            "mergeProdReleaseAssets",
            "writeProdReleaseAppMetadata",
            "compileProdReleaseArtProfile",
            "processApplicationManifestProdReleaseForBundle",
            "compressProdReleaseAssets",
            "l8DexDesugarLibProdRelease",
            "optimizeProdReleaseResources",
            "lintVitalReportProdRelease",
            "createProdReleaseApkListingFileRedirect",
            "mergeProdReleaseJniLibFolders",
            "packageProdRelease",
            "mergeProdReleaseNativeLibs",
            "writeProdReleaseApplicationId",
            "extractProdReleaseNativeSymbolTables",
            "createProdReleaseVariantModel",
            "mergeProdReleaseNativeDebugMetadata",
        )
    }
    tasks.named<com.google.firebase.crashlytics.buildtools.gradle.tasks.UploadMappingFileTask>(
        "uploadCrashlyticsMappingFileDevRelease",
    ) {
        // Set the property usually set by play services plugin
        val firebasePropDir: DirectoryProperty = project.objects.directoryProperty().fileValue(file("firebase/dev"))
        googleServicesResourceRoot.value(firebasePropDir)

        // Set all tasks dependencies to make gradle ok
        dependsOn(
            "mergeDevReleaseAssets",
            "writeDevReleaseAppMetadata",
            "compileDevReleaseArtProfile",
            "processApplicationManifestDevReleaseForBundle",
            "compressDevReleaseAssets",
            "l8DexDesugarLibDevRelease",
            "optimizeDevReleaseResources",
            "lintVitalReportDevRelease",
            "createDevReleaseApkListingFileRedirect",
            "mergeDevReleaseJniLibFolders",
            "packageDevRelease",
            "mergeDevReleaseNativeLibs",
            "writeDevReleaseApplicationId",
            "extractDevReleaseNativeSymbolTables",
            "createDevReleaseVariantModel",
            "mergeDevReleaseNativeDebugMetadata",
        )
    }
}
