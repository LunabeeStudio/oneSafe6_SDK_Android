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
 * Message header, used data by double ratchet engine to retrieve the message key.
 * @property messageNumber Number of the message (sent only) in the conversation.
 * @property sequenceMessageNumber Number of the message sent in a raw before receiving a new message
 * @property publicKey The contact's public key used by the double ratchet.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
class ProtoMessageHeader(
    @ProtoNumber(1)
    val messageNumber: Int = 0,
    @ProtoNumber(2)
    val sequenceMessageNumber: Int = 0,
    @ProtoNumber(3)
    @ByteString
    val publicKey: ByteArray,
)
