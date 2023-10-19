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
 * Created by Lunabee Studio / Date - 10/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/10/2023 13:59
 */

package studio.lunabee.onesafe.domain.model.safeitem

import java.io.InputStream
import java.util.UUID

sealed interface FileSavingData {
    data object AlreadySaved : FileSavingData
    data class ToRemove(val fileId: UUID) : FileSavingData
    data class ToSave(
        val fileId: UUID,
        val getStream: () -> InputStream?,
    ) : FileSavingData
}
