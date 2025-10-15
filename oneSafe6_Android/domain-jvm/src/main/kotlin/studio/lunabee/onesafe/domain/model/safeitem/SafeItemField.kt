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

import java.time.Instant
import java.util.UUID

data class SafeItemField(
    val id: UUID,
    val encName: ByteArray?,
    val position: Double,
    val itemId: UUID,
    val encPlaceholder: ByteArray?,
    val encValue: ByteArray?,
    val showPrediction: Boolean,
    val encKind: ByteArray?,
    val updatedAt: Instant,
    val isItemIdentifier: Boolean,
    val encFormattingMask: ByteArray?,
    val encSecureDisplayMask: ByteArray?,
    val encThumbnailFileName: ByteArray?,
    val isSecured: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeItemField

        if (id != other.id) return false
        if (encName != null) {
            if (other.encName == null) return false
            if (!encName.contentEquals(other.encName)) return false
        } else if (other.encName != null) {
            return false
        }
        if (position != other.position) return false
        if (itemId != other.itemId) return false
        if (encPlaceholder != null) {
            if (other.encPlaceholder == null) return false
            if (!encPlaceholder.contentEquals(other.encPlaceholder)) return false
        } else if (other.encPlaceholder != null) {
            return false
        }
        if (encValue != null) {
            if (other.encValue == null) return false
            if (!encValue.contentEquals(other.encValue)) return false
        } else if (other.encValue != null) {
            return false
        }
        if (showPrediction != other.showPrediction) return false
        if (encKind != null) {
            if (other.encKind == null) return false
            if (!encKind.contentEquals(other.encKind)) return false
        } else if (other.encKind != null) {
            return false
        }
        if (updatedAt != other.updatedAt) return false
        if (isItemIdentifier != other.isItemIdentifier) return false
        if (encFormattingMask != null) {
            if (other.encFormattingMask == null) return false
            if (!encFormattingMask.contentEquals(other.encFormattingMask)) return false
        } else if (other.encFormattingMask != null) {
            return false
        }
        if (encSecureDisplayMask != null) {
            if (other.encSecureDisplayMask == null) return false
            if (!encSecureDisplayMask.contentEquals(other.encSecureDisplayMask)) return false
        } else if (other.encSecureDisplayMask != null) {
            return false
        }
        if (encThumbnailFileName != null) {
            if (other.encThumbnailFileName == null) return false
            if (!encThumbnailFileName.contentEquals(other.encThumbnailFileName)) return false
        } else if (other.encThumbnailFileName != null) {
            return false
        }
        if (isSecured != other.isSecured) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (encName?.contentHashCode() ?: 0)
        result = 31 * result + position.hashCode()
        result = 31 * result + itemId.hashCode()
        result = 31 * result + (encPlaceholder?.contentHashCode() ?: 0)
        result = 31 * result + (encValue?.contentHashCode() ?: 0)
        result = 31 * result + showPrediction.hashCode()
        result = 31 * result + (encKind?.contentHashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + isItemIdentifier.hashCode()
        result = 31 * result + (encFormattingMask?.contentHashCode() ?: 0)
        result = 31 * result + (encSecureDisplayMask?.contentHashCode() ?: 0)
        result = 31 * result + (encThumbnailFileName?.contentHashCode() ?: 0)
        result = 31 * result + isSecured.hashCode()
        return result
    }

    companion object {
        fun equalsForThumbnails(olds: List<SafeItemField>, news: List<SafeItemField>): Boolean {
            if (olds.size != news.size) return false
            return olds
                .asSequence()
                .zip(news.asSequence()) { old, new -> equalsForThumbnail(old, new) }
                .all { it }
        }

        @Suppress("ReturnCount")
        private fun equalsForThumbnail(old: SafeItemField, new: SafeItemField): Boolean {
            if (old.id != new.id) return false
            if (old.encName != null) {
                if (new.encName == null) return false
                if (!old.encName.contentEquals(new.encName)) return false
            } else if (new.encName != null) {
                return false
            }
            if (old.position != new.position) return false
            if (old.itemId != new.itemId) return false
            if (old.encValue != null) {
                if (new.encValue == null) return false
                if (!old.encValue.contentEquals(new.encValue)) return false
            } else if (new.encValue != null) {
                return false
            }
            if (old.encKind != null) {
                if (new.encKind == null) return false
                if (!old.encKind.contentEquals(new.encKind)) return false
            } else if (new.encKind != null) {
                return false
            }
            if (old.encFormattingMask != null) {
                if (new.encFormattingMask == null) return false
                if (!old.encFormattingMask.contentEquals(new.encFormattingMask)) return false
            } else if (new.encFormattingMask != null) {
                return false
            }
            if (old.encSecureDisplayMask != null) {
                if (new.encSecureDisplayMask == null) return false
                if (!old.encSecureDisplayMask.contentEquals(new.encSecureDisplayMask)) return false
            } else if (new.encSecureDisplayMask != null) {
                return false
            }
            if (old.isSecured != new.isSecured) return false

            return true
        }
    }
}
