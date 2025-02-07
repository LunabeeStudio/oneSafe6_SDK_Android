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
 * Created by Lunabee Studio / Date - 8/6/2024 - for the oneSafe6 SDK.
 * Last modified 06/08/2024 16:54
 */

package studio.lunabee.onesafe.bubbles.ui.model

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.ConversationState
import java.time.Instant

data class BubbleContactInfo(
    val id: DoubleRatchetUUID,
    val conversationState: LBResult<ConversationState>,
    val isConversationReady: Boolean,
    val plainName: LBResult<String>,
    val updatedAt: Instant,
)
