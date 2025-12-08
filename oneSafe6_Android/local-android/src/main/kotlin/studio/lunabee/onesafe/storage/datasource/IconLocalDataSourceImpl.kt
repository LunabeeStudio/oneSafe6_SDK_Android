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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.SafeFileDao
import studio.lunabee.onesafe.storage.model.RoomSafeFile
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import java.io.File
import java.util.UUID
import javax.inject.Inject

class IconLocalDataSourceImpl @Inject constructor(
    @ApplicationContext appContext: Context,
    private val dao: SafeFileDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : IconLocalDataSource {
    private val iconDir: File = File(appContext.filesDir, IconDir)

    override fun getIcon(filename: String): File = File(iconDir, filename)

    override suspend fun addIcon(filename: String, icon: ByteArray, safeId: SafeId): File {
        if (!iconDir.exists()) {
            iconDir.mkdir()
        }

        return transactionProvider.runAsTransaction {
            val file = saveIconRef(filename, safeId)
            file.writeBytes(icon)
            file
        }
    }

    override suspend fun deleteIcon(filename: String): Boolean {
        val file = File(iconDir, filename)
        return transactionProvider.runAsTransaction {
            dao.removeFile(file)
            file.delete()
        }
    }

    @CrossSafeData
    override fun getAllIcons(): List<File> = iconDir.listFiles()?.toList().orEmpty()

    override suspend fun getIcons(safeId: SafeId): Set<File> = dao.getAllFiles(safeId, iconDir.path).toSet()

    override suspend fun copyAndDeleteIconFile(newIconFile: File, iconId: UUID, safeId: SafeId) {
        val target = File(iconDir, iconId.toString())
        transactionProvider.runAsTransaction {
            dao.upsertFile(RoomSafeFile(target, safeId))
            newIconFile.copyTo(target = target)
            newIconFile.delete()
        }
    }

    override suspend fun deleteAll(safeId: SafeId) {
        transactionProvider.runAsTransaction {
            dao.getAllFiles(safeId, iconDir.path).forEach { it.delete() }
            dao.deleteAll(safeId, iconDir.path)
        }
    }

    override suspend fun saveIconRef(filename: String, safeId: SafeId): File {
        val file = File(iconDir, filename)
        dao.upsertFile(RoomSafeFile(file, safeId))
        return file
    }

    override fun getIcons(iconsId: List<String>): Set<File> = iconDir
        .listFiles()
        ?.filter { iconsId.contains(it.name) }
        .orEmpty()
        .toSet()

    companion object {
        private const val IconDir: String = "icons"
    }
}
