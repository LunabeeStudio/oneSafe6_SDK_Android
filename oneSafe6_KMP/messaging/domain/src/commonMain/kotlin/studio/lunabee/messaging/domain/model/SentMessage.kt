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
 * Created by Lunabee Studio / Date - 8/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/08/2023 10:07
 */

package studio.lunabee.messaging.domain.model

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID

/**
 * Whole message already sent to be use in case of re-sending a message
 */
class SentMessage(
    val id: DoubleRatchetUUID,
    val contactId: DoubleRatchetUUID,
    val encContent: ByteArray,
    val encCreatedAt: ByteArray,
    val order: Float,
    val safeId: DoubleRatchetUUID,
)
