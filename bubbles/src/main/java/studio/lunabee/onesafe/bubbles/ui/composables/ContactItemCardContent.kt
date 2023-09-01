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
 * Created by Lunabee Studio / Date - 5/24/2023 - for the oneSafe6 SDK.
 * Last modified 5/24/23, 12:03 PM
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.model.OSLazyCardContent

class ContactItemCardContent(
    private val contactInfo: UIBubblesContactInfo,
    private val onClick: () -> Unit,
    override val key: Any = contactInfo.id,
) : OSLazyCardContent.Item {
    override val contentType: Any = "ContactItem"

    @Composable
    override fun Content(padding: PaddingValues) {
        ContactListCell(
            uiBubblesContactInfo = contactInfo,
            onClick = onClick,
            paddingValues = padding,
        )
    }
}
