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

package studio.lunabee.onesafe.repository.datasource

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import java.io.File
import java.util.UUID

interface IconLocalDataSource {
    fun getIcon(filename: String): File

    suspend fun addIcon(filename: String, icon: ByteArray, safeId: SafeId): File

    suspend fun deleteIcon(filename: String): Boolean

    @CrossSafeData
    fun getAllIcons(): List<File>

    suspend fun getIcons(safeId: SafeId): Set<File>

    suspend fun copyAndDeleteIconFile(newIconFile: File, iconId: UUID, safeId: SafeId)

    fun getIcons(iconsId: List<String>): Set<File>

    suspend fun deleteAll(safeId: SafeId)

    suspend fun saveIconRef(filename: String, safeId: SafeId): File
}
