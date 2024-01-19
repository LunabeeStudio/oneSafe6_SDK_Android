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
 * Created by Lunabee Studio / Date - 8/2/2023 - for the oneSafe6 SDK.
 * Last modified 8/2/23, 3:13 PM
 */

package studio.lunabee.onesafe.messaging.domain.model

import java.util.UUID

class EncConversation(
    val id: UUID,
    val encPersonalPublicKey: ByteArray, // DRPublicKey
    val encPersonalPrivateKey: ByteArray, // DRPrivateKey
    val encMessageNumber: ByteArray, // Int
    val encSequenceNumber: ByteArray, // Int
    val encRootKey: ByteArray? = null, // DRRootKey?
    val encSendingChainKey: ByteArray? = null, // DRChainKey?
    val encReceiveChainKey: ByteArray? = null, // DRChainKey?
    val encLastContactPublicKey: ByteArray? = null, // DRPublicKey?
    val encReceivedLastMessageNumber: ByteArray? = null, // Int?
)
