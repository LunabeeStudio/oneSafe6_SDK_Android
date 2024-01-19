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
 * Created by Lunabee Studio / Date - 1/15/2024 - for the oneSafe6 SDK.
 * Last modified 1/15/24, 4:06 PM
 */

package studio.lunabee.onesafe.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import studio.lunabee.onesafe.domain.common.CtaState
import java.time.Instant

@Serializable
sealed interface LocalCtaState {
    @Serializable
    @SerialName("LocalCtaState.Hidden")
    data object Hidden : LocalCtaState

    @JvmInline
    @Serializable
    @SerialName("LocalCtaState.DismissedAt")
    value class DismissedAt(
        @SerialName("timestamp") val timestamp: Long,
    ) : LocalCtaState

    @JvmInline
    @Serializable
    @SerialName("LocalCtaState.VisibleSince")
    value class VisibleSince(
        @SerialName("timestamp") val timestamp: Long,
    ) : LocalCtaState

    fun toCtaState(): CtaState = when (this) {
        Hidden -> CtaState.Hidden
        is DismissedAt -> CtaState.DismissedAt(Instant.ofEpochMilli(timestamp))
        is VisibleSince -> CtaState.VisibleSince(Instant.ofEpochMilli(timestamp))
    }

    companion object {
        fun fromCtaState(ctaState: CtaState): LocalCtaState = when (ctaState) {
            CtaState.Hidden -> Hidden
            is CtaState.DismissedAt -> DismissedAt(ctaState.timestamp.toEpochMilli())
            is CtaState.VisibleSince -> VisibleSince(ctaState.timestamp.toEpochMilli())
        }
    }
}
