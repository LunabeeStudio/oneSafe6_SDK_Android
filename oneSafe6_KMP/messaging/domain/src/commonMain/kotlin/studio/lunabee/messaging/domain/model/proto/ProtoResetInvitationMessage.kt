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
 * Created by Lunabee Studio / Date - 8/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/08/2024 14:48
 */

package studio.lunabee.messaging.domain.model.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Message sent after a conversation reset, should be encrypted with bubbles shared crypto.
 * @property doubleRatchetPublicKey The initial contact's public key used by the double ratchet.
 * @property conversationId Shared conversation id between the two parties, used to identify the conversation.
 * @property recipientId Contact id of the recipient of the message in the message sender database.
 * @property conversationResetDate Timestamp of the conversation reset.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ProtoResetInvitationMessage(
    @ByteString
    @ProtoNumber(1)
    val doubleRatchetPublicKey: ByteArray,
    @ProtoNumber(2)
    val conversationId: String,
    @ProtoNumber(3)
    val recipientId: String,
    @ProtoNumber(4)
    val conversationResetDate: ProtoTimestamp,
)
