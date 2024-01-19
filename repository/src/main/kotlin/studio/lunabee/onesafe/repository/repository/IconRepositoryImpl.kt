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

package studio.lunabee.onesafe.repository.repository

import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import java.io.File
import java.util.UUID
import javax.inject.Inject

class IconRepositoryImpl @Inject constructor(
    private val iconLocalDataSource: IconLocalDataSource,
) : IconRepository {
    override fun getIcon(iconId: String): File = iconLocalDataSource.getIcon(iconId)

    override fun addIcon(iconId: UUID, icon: ByteArray): File = iconLocalDataSource.addIcon(iconId.toString(), icon)

    override fun deleteIcon(iconId: UUID): Boolean = iconLocalDataSource.deleteIcon(iconId.toString())

    override fun getIcons(): List<File> = iconLocalDataSource.getAllIcons()

    override fun copyAndDeleteIconFile(iconFile: File, iconId: UUID) {
        iconLocalDataSource.copyAndDeleteIconFile(newIconFile = iconFile, iconId = iconId)
    }

    override fun deleteAll() {
        iconLocalDataSource.removeAllIcons()
    }

    override fun getIcons(iconsId: List<String>): List<File> = iconLocalDataSource.getIcons(iconsId)
}
