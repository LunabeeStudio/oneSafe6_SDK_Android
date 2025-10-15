/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 02/11/2023 17:07
 */

package studio.lunabee.messaging.domain.model

import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.error.BubblesDoubleRatchetError
import studio.lunabee.onesafe.error.OSError

sealed interface DecryptResult {
    val contactId: DoubleRatchetUUID
    val error: Error?

    data class NewMessage(
        override val contactId: DoubleRatchetUUID,
        val messageKey: DRMessageKey,
    ) : DecryptResult {
        override val error: Error? = null
    }

    data class ResetMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptResult {
        override val error: Error? = null
    }

    data class OwnMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptResult {
        override val error: Error = Error.OWN_MESSAGE
    }

    data class AlreadyDecrypted(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptResult {
        override val error: Error = Error.ALREADY_DECRYPTED
    }

    data class OutdatedMessage(
        override val contactId: DoubleRatchetUUID,
    ) : DecryptResult {
        override val error: Error = Error.OUTDATED_CONVERSATION
    }

    companion object {
        fun fromDecryptIncomingMessageData(
            decryptIncomingMessageData: DecryptIncomingMessageData,
        ): DecryptResult = when (decryptIncomingMessageData) {
            is DecryptIncomingMessageData.AlreadyDecryptedMessage -> AlreadyDecrypted(decryptIncomingMessageData.contactId)
            is DecryptIncomingMessageData.DecryptOwnMessage -> OwnMessage(decryptIncomingMessageData.contactId)
            is DecryptIncomingMessageData.OutdatedConversationMessage -> OutdatedMessage(decryptIncomingMessageData.contactId)
            is DecryptIncomingMessageData.NewMessage -> NewMessage(
                contactId = decryptIncomingMessageData.contactId,
                messageKey = decryptIncomingMessageData.messageKey,
            )
            is DecryptIncomingMessageData.ResetMessage -> ResetMessage(
                contactId = decryptIncomingMessageData.contactId,
            )
        }
    }

    enum class Error(val error: OSError) {
        ALREADY_DECRYPTED(BubblesDoubleRatchetError(BubblesDoubleRatchetError.Code.MessageKeyNotFound)),
        OWN_MESSAGE(BubblesDoubleRatchetError(BubblesDoubleRatchetError.Code.CantDecryptSentMessage)),
        OUTDATED_CONVERSATION(BubblesDoubleRatchetError(BubblesDoubleRatchetError.Code.OutdatedMessage)),
    }
}
