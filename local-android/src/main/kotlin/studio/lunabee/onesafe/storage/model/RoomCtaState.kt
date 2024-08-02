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
 * Last modified 6/19/24, 3:13 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import studio.lunabee.onesafe.domain.common.CtaState
import java.time.Instant

class RoomCtaState(
    @ColumnInfo(name = "state")
    val state: State,
    @ColumnInfo(name = "timestamp")
    val timestamp: Instant?,
) {
    fun toCtaState(): CtaState = when (state) {
        State.Hidden -> CtaState.Hidden
        State.DismissedAt -> CtaState.DismissedAt(timestamp!!)
        State.VisibleSince -> CtaState.VisibleSince(timestamp!!)
    }

    enum class State {
        Hidden, DismissedAt, VisibleSince
    }

    companion object {
        fun fromCtaState(ctaState: CtaState): RoomCtaState = when (ctaState) {
            is CtaState.DismissedAt -> RoomCtaState(State.DismissedAt, ctaState.timestamp)
            CtaState.Hidden -> RoomCtaState(State.Hidden, null)
            is CtaState.VisibleSince -> RoomCtaState(State.VisibleSince, ctaState.timestamp)
        }
    }
}
