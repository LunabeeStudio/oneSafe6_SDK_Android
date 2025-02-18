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
 * Created by Lunabee Studio / Date - 7/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/07/2023 10:14
 */

package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.BubblesDoubleRatchetError

fun BubblesDoubleRatchetError.localizedTitle(): LbcTextSpec? = when (this.code) {
    BubblesDoubleRatchetError.Code.ConversationNotSetup,
    BubblesDoubleRatchetError.Code.ConversationNotFound,
    BubblesDoubleRatchetError.Code.MessageKeyNotFound,
    BubblesDoubleRatchetError.Code.RequiredChainKeyMissing,
    BubblesDoubleRatchetError.Code.CantDecryptSentMessage,
    BubblesDoubleRatchetError.Code.OutdatedMessage,
    -> null
}

fun BubblesDoubleRatchetError.localizedDescription(): LbcTextSpec? = when (this.code) {
    BubblesDoubleRatchetError.Code.MessageKeyNotFound -> LbcTextSpec.StringResource(OSString.bubbles_error_alreadyDecrypted)
    BubblesDoubleRatchetError.Code.CantDecryptSentMessage -> LbcTextSpec.StringResource(OSString.bubbles_error_decryptOwnMessage)
    BubblesDoubleRatchetError.Code.ConversationNotFound,
    BubblesDoubleRatchetError.Code.RequiredChainKeyMissing,
    BubblesDoubleRatchetError.Code.ConversationNotSetup,
    BubblesDoubleRatchetError.Code.OutdatedMessage,
    -> null
}
