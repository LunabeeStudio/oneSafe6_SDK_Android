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
 * Created by Lunabee Studio / Date - 11/2/2023 - for the oneSafe6 SDK.
 * Last modified 02/11/2023 16:08
 */

package studio.lunabee.messaging.domain.model

import kotlin.time.Instant
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.MessagingConstant

sealed interface DecryptIncomingMessageData {
    val contactId: DoubleRatchetUUID

    data class NewMessage(
        override val contactId: DoubleRatchetUUID,
        val osPlainMessage: SharedMessage?,
        val messageKey: DRMessageKey,
    ) : DecryptIncomingMessageData

    data class DecryptOwnMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptIncomingMessageData

    data class AlreadyDecryptedMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptIncomingMessageData

    data class ResetMessage(
        override val contactId: DoubleRatchetUUID,
        val receivedAt: Instant,
    ) : DecryptIncomingMessageData {
        // `sentAt` is at `clock.now()` to have the reset indicator as last received message
        val osPlainMessage: SharedMessage = SharedMessage(
            content = MessagingConstant.ResetConversationMessageData,
            recipientId = contactId,
            date = receivedAt,
        )
    }

    data class OutdatedConversationMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptIncomingMessageData
}
