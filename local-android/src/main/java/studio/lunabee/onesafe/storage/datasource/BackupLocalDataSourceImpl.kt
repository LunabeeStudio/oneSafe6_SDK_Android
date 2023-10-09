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

package studio.lunabee.onesafe.storage.datasource

import android.os.FileObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.repository.datasource.BackupLocalDataSource
import studio.lunabee.onesafe.storage.utils.FileObserverCompat
import java.io.File
import java.io.IOException
import javax.inject.Inject

class BackupLocalDataSourceImpl @Inject constructor(
    @FileDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    @InternalDir(InternalDir.Type.Backups) backupsDir: File,
) : BackupLocalDataSource {

    val backupsDir: File = backupsDir
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }

    override fun addBackup(backupFile: File) {
        try {
            backupFile.copyTo(File(backupsDir, backupFile.name))
        } catch (e: IOException) {
            throw OSStorageError(OSStorageError.Code.UNKNOWN_FILE_ERROR, cause = e)
        }
    }

    override fun getBackups(): List<File> {
        return backupsDir.listFiles()?.toList() ?: emptyList()
    }

    override fun getBackupsFlow(): Flow<List<File>> = callbackFlow {
        trySend(getBackups())
        val fileObserver = FileObserverCompat.get(backupsDir, FileObserver.DELETE or FileObserver.CREATE) { _, _ ->
            trySend(getBackups())
        }
        fileObserver.startWatching()
        awaitClose {
            fileObserver.stopWatching()
        }
    }.flowOn(coroutineDispatcher)
}
