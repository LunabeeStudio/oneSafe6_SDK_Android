/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/12/2024 - for the oneSafe6 SDK.
 * Last modified 4/12/24, 10:19 AM
 */

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.importexport.datasource.LocalBackupCacheDataSource
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.File
import java.io.InputStream
import java.time.Instant
import javax.inject.Inject

class LocalBackupFileCacheDataSource @Inject constructor(
    @param:InternalDir(InternalDir.Type.Cache) private val cacheDir: File,
    @param:FileDispatcher private val dispatcher: CoroutineDispatcher,
) : LocalBackupCacheDataSource {
    override suspend fun addBackup(inputStream: InputStream, date: Instant): File {
        val file = withContext(dispatcher) {
            val file = File
                .createTempFile("$Prefix${date.toEpochMilli()}", ".${ImportExportConstant.ExtensionOs6Backup}", cacheDir)
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        }
        return file
    }

    override suspend fun removeBackup(localBackup: LocalBackup): Boolean = withContext(dispatcher) {
        localBackup.file.delete()
    }

    companion object {
        private const val Prefix = "backup-"
    }
}
