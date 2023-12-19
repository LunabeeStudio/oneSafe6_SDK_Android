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

import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.domain.Constant
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private val log = LBLogger.get<SafeItem>()

data class SafeItem(
    val id: UUID,
    val encName: ByteArray?,
    val parentId: UUID?,
    val isFavorite: Boolean,
    val updatedAt: Instant,
    val position: Double,
    val iconId: UUID?,
    val encColor: ByteArray?,
    val deletedAt: Instant?,
    val deletedParentId: UUID?,
    val indexAlpha: Double,
) {
    init {
        check(!(deletedParentId != null && deletedAt == null))
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    /**
     * Get the number of day (round toward infinity) before the item will be removed. Coerced at least 1.
     *
     * @param now The current date
     */
    fun daysBeforeRemove(now: Instant = Instant.now()): Int? {
        return deletedAt?.let {
            val removeAt = deletedAt.plus(Constant.DefinitiveItemRemoveAfterDays.toLong() + 1, ChronoUnit.DAYS)
            now.until(removeAt, ChronoUnit.DAYS).toInt()
        }?.also {
            if (it < 1) {
                log.e("daysBeforeRemove must > 1 (actual $it)")
            }
        }?.coerceAtLeast(1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeItem

        if (id != other.id) return false
        if (encName != null) {
            if (other.encName == null) return false
            if (!encName.contentEquals(other.encName)) return false
        } else if (other.encName != null) return false
        if (parentId != other.parentId) return false
        if (isFavorite != other.isFavorite) return false
        if (updatedAt != other.updatedAt) return false
        if (position != other.position) return false
        if (iconId != other.iconId) return false
        if (encColor != null) {
            if (other.encColor == null) return false
            if (!encColor.contentEquals(other.encColor)) return false
        } else if (other.encColor != null) return false
        if (deletedAt != other.deletedAt) return false
        if (deletedParentId != other.deletedParentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (encName?.contentHashCode() ?: 0)
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (iconId?.hashCode() ?: 0)
        result = 31 * result + (encColor?.contentHashCode() ?: 0)
        result = 31 * result + (deletedAt?.hashCode() ?: 0)
        result = 31 * result + (deletedParentId?.hashCode() ?: 0)
        return result
    }
}
