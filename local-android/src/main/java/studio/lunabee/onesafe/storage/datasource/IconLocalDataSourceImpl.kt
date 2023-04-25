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
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import java.io.File
import java.util.UUID
import javax.inject.Inject

class IconLocalDataSourceImpl @Inject constructor(
    @ApplicationContext appContext: Context,
) : IconLocalDataSource {
    private val iconDir: File = File(appContext.filesDir, ICON_DIR)

    override fun getIcon(filename: String): File {
        return File(iconDir, filename)
    }

    override fun addIcon(filename: String, icon: ByteArray): File {
        if (!iconDir.exists()) {
            iconDir.mkdir()
        }

        val file = File(iconDir, filename)
        file.writeBytes(icon)
        return file
    }

    override fun removeIcon(filename: String): Boolean {
        val file = File(iconDir, filename)
        return file.delete()
    }

    override fun removeAllIcons(): Boolean {
        return iconDir.deleteRecursively()
    }

    override fun deleteIcon(filename: String): Boolean {
        return File(iconDir, filename).delete()
    }

    override fun getAllIcons(): List<File> {
        return iconDir.listFiles()?.toList().orEmpty()
    }

    override fun copyAndDeleteIconFile(newIconFile: File, iconId: UUID) {
        newIconFile.copyTo(target = File(iconDir, iconId.toString()))
        newIconFile.delete()
    }

    override fun getIcons(iconsId: List<String>): List<File> {
        return iconDir.listFiles()?.filter { iconsId.contains(it.name) }.orEmpty()
    }

    companion object {
        private const val ICON_DIR: String = "icons"
    }
}
