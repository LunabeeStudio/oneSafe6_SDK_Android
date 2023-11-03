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
 * Created by Lunabee Studio / Date - 10/30/2023 - for the oneSafe6 SDK.
 * Last modified 10/30/23, 9:45 AM
 */

package studio.lunabee.onesafe.importexport.provider

import android.provider.DocumentsContract
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.importexport.android.test.BuildConfig
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestUtils
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@HiltAndroidTest
class BackupsProviderTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject
    @InternalDir(InternalDir.Type.Backups)
    lateinit var backupsDir: File

    @Before
    fun setUp() {
        backupsDir.mkdirs()
    }

    @After
    fun tearsDown() {
        backupsDir.deleteRecursively()
    }

    @Test
    fun query_backups_test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val expectedFilename = "backup.${ImportExportConstant.ExtensionOs6Backup}"
        val backupFile = File(backupsDir, expectedFilename)
        val notBackupFile = File(backupsDir, "backup.not_backup")
        val expectedData = OSTestUtils.random.nextBytes(10)
        backupFile.writeBytes(expectedData)
        notBackupFile.writeBytes(OSTestUtils.random.nextBytes(1))

        val authority = BuildConfig.APPLICATION_ID + BuildConfig.BACKUPS_PROVIDER_AUTHORITY_SUFFIX
        val provider =
            context.contentResolver.acquireContentProviderClient(authority)?.localContentProvider as BackupsProvider

        val rootCursor = provider.queryRoots(null).apply {
            moveToFirst()
        }
        val rootDocumentIdIdx = rootCursor.getColumnIndex(DocumentsContract.Root.COLUMN_DOCUMENT_ID)
        val rootDocumentId = rootCursor.getString(rootDocumentIdIdx)
        val childrenCursor = provider.queryChildDocuments(parentDocumentId = rootDocumentId, projection = null, sortOrder = null).apply {
            moveToFirst()
        }

        repeat(childrenCursor.columnCount) {
            println("${childrenCursor.getColumnName(it)} - ${childrenCursor.getString(it)}")
        }

        val displayNameColumnIdx = childrenCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        val actualName = childrenCursor.getString(displayNameColumnIdx)
        val documentIdIdx = childrenCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val documentId = childrenCursor.getString(documentIdIdx)
        val fileDescriptor = provider.openDocument(documentId, "r", null).fileDescriptor
        val actualData: ByteArray = FileInputStream(fileDescriptor).use {
            it.readBytes()
        }

        assertEquals(1, childrenCursor.count)
        assertEquals(expectedFilename, actualName)
        assertContentEquals(expectedData, actualData)
    }
}
