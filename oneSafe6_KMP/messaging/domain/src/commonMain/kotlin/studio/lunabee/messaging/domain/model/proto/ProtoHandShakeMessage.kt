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
 */

package studio.lunabee.messaging.domain.model.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Message sent as invitation response, should be sent plained to the other user since the crypto is not ready yet
 * @property body the message content, encrypted via double ratchet, encrypted `ProtoMessageData`.
 * @property header the message header, used to retrieve the double ratchet message key.
 * @property conversationId Shared conversation id between the two parties, used to identify the conversation.
 * @property recipientId Contact id of the recipient of the message in the message sender database.
 * @property oneSafePublicKey sender's bubbles public key for shared crypto.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
class ProtoHandShakeMessage(
    @ByteString
    @ProtoNumber(1)
    val body: ByteArray,
    @ProtoNumber(2)
    val header: ProtoMessageHeader,
    @ProtoNumber(3)
    val conversationId: String,
    @ByteString
    @ProtoNumber(4)
    val oneSafePublicKey: ByteArray,
    @ProtoNumber(5)
    val recipientId: String,
)
