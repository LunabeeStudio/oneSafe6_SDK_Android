/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/16/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 4:58 PM
 */

package studio.lunabee.onesafe.migration.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import studio.lunabee.onesafe.domain.common.CtaState
import java.time.Instant

@Serializable
internal class LegacyLocalCtaStateMap(
    val data: Map<String, LegacyLocalCtaState>,
) {
    operator fun get(key: String): LegacyLocalCtaState {
        return data.getOrDefault(key, LegacyLocalCtaState.Hidden)
    }
}

@Serializable
internal sealed interface LegacyLocalCtaState {
    @Serializable
    @SerialName("LocalCtaState.Hidden")
    data object Hidden : LegacyLocalCtaState

    @JvmInline
    @Serializable
    @SerialName("LocalCtaState.DismissedAt")
    value class DismissedAt(
        @SerialName("timestamp") val timestamp: Long,
    ) : LegacyLocalCtaState

    @JvmInline
    @Serializable
    @SerialName("LocalCtaState.VisibleSince")
    value class VisibleSince(
        @SerialName("timestamp") val timestamp: Long,
    ) : LegacyLocalCtaState

    fun toCtaState(): CtaState = when (this) {
        Hidden -> CtaState.Hidden
        is DismissedAt -> CtaState.DismissedAt(Instant.ofEpochMilli(timestamp))
        is VisibleSince -> CtaState.VisibleSince(Instant.ofEpochMilli(timestamp))
    }
}
