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
 * Created by Lunabee Studio / Date - 7/30/2024 - for the oneSafe6 SDK.
 * Last modified 30/07/2024 08:45
 */

package studio.lunabee.messaging.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import org.junit.Test
import studio.lunabee.messaging.domain.model.proto.ProtoHandShakeMessage
import studio.lunabee.messaging.domain.model.proto.ProtoInvitationMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessageData
import studio.lunabee.messaging.domain.model.proto.ProtoMessageHeader
import studio.lunabee.messaging.domain.model.proto.ProtoTimestamp

class ProtoSchemaTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `print proto schemas`() {
        val schemas = ProtoBufSchemaGenerator.generateSchemaText(
            listOf(
                ProtoMessageData.serializer().descriptor,
                ProtoMessageHeader.serializer().descriptor,
                ProtoMessage.serializer().descriptor,
                ProtoHandShakeMessage.serializer().descriptor,
                ProtoInvitationMessage.serializer().descriptor,
                ProtoTimestamp.serializer().descriptor,
            ),
        )
        println(schemas)
    }
}
