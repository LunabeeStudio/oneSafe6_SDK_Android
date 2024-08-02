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

package studio.lunabee.onesafe.domain.repository

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import java.io.File
import java.util.UUID

// TODO suspend + IO dispatcher
interface IconRepository {
    fun getIcon(iconId: String): File
    fun getIcons(iconsId: List<String>): List<File>
    suspend fun addIcon(iconId: UUID, icon: ByteArray, safeId: SafeId): File
    suspend fun deleteIcon(iconId: UUID): Boolean

    @CrossSafeData
    fun getAllIcons(): List<File>
    suspend fun getIcons(safeId: SafeId): List<File>
    suspend fun copyAndDeleteIconFile(iconFile: File, iconId: UUID, safeId: SafeId)
    suspend fun deleteAll(safeId: SafeId)
}
