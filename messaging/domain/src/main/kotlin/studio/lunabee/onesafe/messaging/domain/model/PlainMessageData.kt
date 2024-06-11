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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 1:24 PM
 */

package studio.lunabee.onesafe.messaging.domain.model

import com.lunabee.lbcore.model.LBResult
import java.time.Instant
import java.util.UUID

sealed class PlainMessageData(
    val id: UUID,
    val sentAt: LBResult<Instant>,
    val direction: MessageDirection,
    val channel: LBResult<String>?,
    val isRead: Boolean,
) {
    open val hasCorruptedData: Boolean
        get() = listOf(sentAt, channel).any { it is LBResult.Failure }

    class Default(
        id: UUID,
        sentAt: LBResult<Instant>,
        val content: LBResult<String>,
        direction: MessageDirection,
        channel: LBResult<String>?,
        isRead: Boolean,
    ) : PlainMessageData(
        id = id,
        sentAt = sentAt,
        direction = direction,
        channel = channel,
        isRead = isRead,
    ) {
        override val hasCorruptedData: Boolean
            get() = super.hasCorruptedData || content is LBResult.Failure
    }

    class AcceptedInvitation(
        id: UUID,
        sentAt: LBResult<Instant>,
        channel: LBResult<String>?,
        isRead: Boolean,
    ) : PlainMessageData(
        id = id,
        sentAt = sentAt,
        direction = MessageDirection.RECEIVED,
        channel = channel,
        isRead = isRead,
    )
}
