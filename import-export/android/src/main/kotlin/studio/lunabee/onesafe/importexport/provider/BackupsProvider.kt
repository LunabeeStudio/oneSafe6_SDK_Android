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
 * Last modified 10/27/23, 12:05 PM
 */

// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

// SPDX-License-Identifier: MPL-2.0
// Copyright © 2023 Skyline Team and Contributors (https://github.com/skyline-emu/)
// https://github.com/yuzu-emu/yuzu-android/blob/master/src/android/app/src/main/java/org/yuzu/yuzu_emu/features/DocumentProvider.kt

package studio.lunabee.onesafe.importexport.provider

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.domain.qualifier.InternalBackupMimetype
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.importexport.android.BuildConfig
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import java.io.FileNotFoundException

/**
 * [android.content.ContentProvider] exposing oneSafe internals backup
 * Widely inspired from Yuzu app implementation
 *
 * @see <a href="https://t.ly/ELgCP">Yuzu DocumentProvider.kt</a>
 */
class BackupsProvider : DocumentsProvider() {

    // ContentProvider injection https://developer.android.com/codelabs/android-hilt#10
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface BackupsProviderProviderEntryPoint {
        @InternalDir(InternalDir.Type.Backups)
        fun backupDir(): File

        @InternalBackupMimetype
        fun internalBackupMimetype(): String
    }

    private fun getBackupDir(appContext: Context): File {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BackupsProviderProviderEntryPoint::class.java,
        )
        return hiltEntryPoint.backupDir().also { it.mkdirs() }
    }

    private fun getInternalBackupMimetype(appContext: Context): String {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BackupsProviderProviderEntryPoint::class.java,
        )
        return hiltEntryPoint.internalBackupMimetype()
    }

    private val baseDirectory: File by lazy {
        getBackupDir(context!!).canonicalFile
    }

    private val internalBackupMimetype: String by lazy {
        getInternalBackupMimetype(context!!)
    }

    companion object {
        private val DEFAULT_ROOT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
        )

        private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE,
        )

        fun authority(appId: String): String = appId + BuildConfig.BACKUPS_PROVIDER_AUTHORITY_SUFFIX
        const val ROOT_ID: String = "root"
    }

    override fun onCreate(): Boolean {
        return true
    }

    /**
     * @return The [File] that corresponds to the document ID supplied by [getDocumentId]
     */
    private fun getFile(documentId: String): File {
        if (documentId.startsWith(ROOT_ID)) {
            val file = baseDirectory.resolve(documentId.drop(ROOT_ID.length + 1))
            if (!file.exists()) {
                throw FileNotFoundException(
                    "${file.absolutePath} ($documentId) not found",
                )
            }
            return file
        } else {
            throw FileNotFoundException("'$documentId' is not in any known root")
        }
    }

    /**
     * @return A unique ID for the provided [File]
     */
    private fun getDocumentId(file: File): String {
        return "$ROOT_ID/${file.toRelativeString(baseDirectory)}"
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)

        cursor.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID)
            add(DocumentsContract.Root.COLUMN_SUMMARY, context!!.getString(R.string.settings_autoBackupScreen_saveAccess_localSaves))
            add(
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE or
                    DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD,
            )
            add(DocumentsContract.Root.COLUMN_TITLE, context!!.getString(R.string.application_name))
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocumentId(baseDirectory))
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, "*/*")
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, baseDirectory.freeSpace)
            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_onesafe_logo)
        }

        return cursor
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        return includeFile(cursor, documentId, null)
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        return documentId?.startsWith(parentDocumentId!!) ?: false
    }

    private fun includeFile(cursor: MatrixCursor, documentId: String?, file: File?): MatrixCursor {
        val localDocumentId = documentId ?: file?.let { getDocumentId(it) }
        val localFile = file ?: getFile(documentId!!)

        cursor.newRow().apply {
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, localDocumentId)
            add(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                if (localFile == baseDirectory) {
                    context!!.getString(R.string.application_name)
                } else {
                    localFile.name
                },
            )
            add(DocumentsContract.Document.COLUMN_SIZE, localFile.length())
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, getTypeForFile(localFile))
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, localFile.lastModified())
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_onesafe_logo)
        }

        return cursor
    }

    private fun getTypeForFile(file: File): Any {
        return if (file.isDirectory) {
            DocumentsContract.Document.MIME_TYPE_DIR
        } else {
            internalBackupMimetype
        }
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        var cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)

        val parent = getFile(parentDocumentId!!)
        parent.listFiles { _, name ->
            name.endsWith(ImportExportConstant.ExtensionOs6Backup)
        }?.forEach { file ->
            cursor = includeFile(cursor, null, file)
        }

        return cursor
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor {
        val file = documentId?.let { getFile(it) }
        val accessMode = ParcelFileDescriptor.parseMode(mode)
        if (accessMode != ParcelFileDescriptor.MODE_READ_ONLY) {
            throw UnsupportedOperationException("Unsupported mode $mode")
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }
}
