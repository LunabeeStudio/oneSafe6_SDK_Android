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
 * Created by Lunabee Studio / Date - 10/27/2023 - for the oneSafe6 SDK.
 * Last modified 10/27/23, 2:48 PM
 */

package studio.lunabee.onesafe.importexport.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.provider.BackupsProvider

private val logger = LBLogger.get<BackupFileManagerHelper>()

object BackupFileManagerHelper {

    /**
     * Open the default file picker to pick a backup
     */
    fun launchFilePicker(pickFileLauncher: ManagedActivityResultLauncher<String, Uri?>) {
        pickFileLauncher.launch(ImportExportAndroidConstants.MimeTypeOs6lsb)
    }

    /**
     * Helper to open the device file manager using the [BackupsProvider]
     * Inspired from Yuzu app 🪦
     *
     * @see <a href="https://t.ly/MnrR9" />Yuzu HomeSettingsFragment.kt</a>
     */
    fun openInternalFileManager(context: Context): Boolean {
        // First, try to open the user data folder directly
        var result = false
        try {
            context.startActivity(getFileManagerIntentOnDocumentProvider(Intent.ACTION_VIEW, context.packageName))
            result = true
        } catch (_: ActivityNotFoundException) {
            logger.v("fallback 1")
        }

        if (!result) {
            try {
                context.startActivity(getFileManagerIntentOnDocumentProvider("android.provider.action.BROWSE", context.packageName))
                result = true
            } catch (_: ActivityNotFoundException) {
                logger.v("fallback 2")
            }
        }

        // Just try to open the file manager, try the package name used on "normal" phones
        if (!result) {
            try {
                context.startActivity(getFileManagerIntent("com.google.android.documentsui"))
                result = true
            } catch (_: ActivityNotFoundException) {
                logger.v("fallback 3")
            }
        }

        if (!result) {
            try {
                // Next, try the AOSP package name
                context.startActivity(getFileManagerIntent("com.android.documentsui"))
                result = true
            } catch (_: ActivityNotFoundException) {
                logger.v("fallback 4")
            }
        }

        return result
    }

    private fun getFileManagerIntentOnDocumentProvider(action: String, appId: String): Intent {
        val authority = BackupsProvider.authority(appId)
        val intent = Intent(action)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = DocumentsContract.buildRootUri(authority, BackupsProvider.ROOT_ID)
        intent.addFlags(
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        return intent
    }

    private fun getFileManagerIntent(packageName: String): Intent {
        // Fragile, but some phones don't expose the system file manager in any better way
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClassName(packageName, "com.android.documentsui.files.FilesActivity")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }
}
