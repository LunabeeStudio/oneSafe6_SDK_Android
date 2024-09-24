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
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Message content, should be encrypted via double ratchet crypto.
 * @property content string content of the message.
 * @property sentAt Timestamp of the message.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
class ProtoMessageData(
    @ProtoNumber(1)
    val content: String,
    @ProtoNumber(3)
    val sentAt: ProtoTimestamp,
)
