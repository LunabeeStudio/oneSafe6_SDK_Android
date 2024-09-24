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
 * Created by Lunabee Studio / Date - 6/6/2024 - for the oneSafe6 SDK.
 * Last modified 6/6/24, 3:31 PM
 */

package studio.lunabee.onesafe.domain.model.safe

import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.jvm.toUUID
import java.util.UUID

@JvmInline
value class SafeId(val id: UUID) {
    constructor(id: String) : this(UUID.fromString(id))
    constructor(id: ByteArray) : this(id.toUUID())

    override fun toString(): String {
        return id.toString()
    }

    fun toByteArray(): ByteArray {
        return id.toByteArray()
    }
}
