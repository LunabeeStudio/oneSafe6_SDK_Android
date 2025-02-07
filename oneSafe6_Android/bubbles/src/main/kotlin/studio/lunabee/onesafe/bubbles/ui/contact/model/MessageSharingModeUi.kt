/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/13/2024 - for the oneSafe6 SDK.
 * Last modified 6/13/24, 3:26 PM
 */

package studio.lunabee.onesafe.bubbles.ui.contact.model

import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

enum class MessageSharingModeUi(
    val title: LbcTextSpec,
    val description: LbcTextSpec,
    val mode: MessageSharingMode,
) {
    Deeplinks(
        title = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_deeplink),
        description = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_deeplink_description),
        mode = MessageSharingMode.Deeplink,
    ),
    CypherText(
        title = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_encryptedText),
        description = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_encryptedText_description),
        mode = MessageSharingMode.CypherText,
    ),
    Archive(
        title = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_archiveFile),
        description = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode_archiveFile_description),
        mode = MessageSharingMode.Archive,
    ),
    ;

    companion object {
        fun fromMode(mode: MessageSharingMode): MessageSharingModeUi = when (mode) {
            MessageSharingMode.Deeplink -> Deeplinks
            MessageSharingMode.CypherText -> CypherText
            MessageSharingMode.Archive -> Archive
        }
    }
}
