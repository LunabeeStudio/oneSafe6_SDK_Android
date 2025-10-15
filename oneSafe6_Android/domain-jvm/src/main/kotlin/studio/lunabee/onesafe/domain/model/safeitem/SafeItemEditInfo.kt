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

package studio.lunabee.onesafe.domain.model.safeitem

import java.util.UUID

data class SafeItemEditInfo(
    val id: UUID,
    val encName: ByteArray?,
    val iconId: UUID?,
    val encSafeItemFields: List<SafeItemField>,
    val encColor: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeItemEditInfo

        if (id != other.id) return false
        if (encName != null) {
            if (other.encName == null) return false
            if (!encName.contentEquals(other.encName)) return false
        } else if (other.encName != null) {
            return false
        }
        if (iconId != other.iconId) return false
        if (encSafeItemFields != other.encSafeItemFields) return false
        if (encColor != null) {
            if (other.encColor == null) return false
            if (!encColor.contentEquals(other.encColor)) return false
        } else if (other.encColor != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (encName?.contentHashCode() ?: 0)
        result = 31 * result + (iconId?.hashCode() ?: 0)
        result = 31 * result + encSafeItemFields.hashCode()
        result = 31 * result + (encColor?.contentHashCode() ?: 0)
        return result
    }
}
