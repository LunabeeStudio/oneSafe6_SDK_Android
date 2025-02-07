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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 1:44 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId

class RoomSafeCryptoUpdate(
    @ColumnInfo(name = "id")
    val id: SafeId,
    @Embedded(prefix = "crypto_")
    val crypto: RoomSafeCrypto,
) {
    companion object {
        fun fromSafeCrypto(safeCrypto: SafeCrypto): RoomSafeCryptoUpdate = RoomSafeCryptoUpdate(
            id = safeCrypto.id,
            crypto = RoomSafeCrypto.fromSafeCrypto(safeCrypto),
        )
    }
}
