/*
 * Copyright (c) 2025-2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 11/21/2025 - for the oneSafe6 SDK.
 * Last modified 11/21/25, 11:34 AM
 */

package studio.lunabee.onesafe.test

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.FileRepository
import java.io.File
import java.util.UUID

suspend fun FileRepository.addFile(
    fileId: UUID,
    data: ByteArray,
    safeId: SafeId,
): File {
    val file = createFile(fileId.toString(), safeId)
    file.writeBytes(data)
    return file
}
