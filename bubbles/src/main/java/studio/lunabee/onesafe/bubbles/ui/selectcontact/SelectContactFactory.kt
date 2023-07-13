/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/23/2023 - for the oneSafe6 SDK.
 * Last modified 6/12/23, 10:41 AM
 */

package studio.lunabee.onesafe.bubbles.ui.selectcontact

import androidx.compose.foundation.lazy.LazyListScope
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.bubbles.ui.composables.ContactItemCardContent
import studio.lunabee.onesafe.bubbles.ui.composables.EmptyContactCard
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.model.OSTextCaptionCardContent
import java.util.UUID

object SelectContactFactory {

    fun addEmptyCard(
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item(
            key = EmptyCardKey,
        ) {
            EmptyContactCard()
        }
    }

    fun addContacts(
        contacts: List<BubblesContactInfo>,
        onClick: (UUID) -> Unit,
        lazyListScope: LazyListScope,
    ) {
        val cardContents = mutableListOf<OSLazyCardContent>()

        cardContents += OSTextCaptionCardContent(
            text = LbcTextSpec.StringResource(R.string.oneSafeK_selectContact_description),
            key = ContactCaptionKey,
        )

        cardContents += contacts.map { contact ->
            ContactItemCardContent(
                contactInfo = contact,
                key = contact.id,
                onClick = { onClick(contact.id) },
            )
        }

        osLazyCard(lazyListScope, cardContents)
    }

    private const val EmptyCardKey: String = "EmptyCardKey"
    private const val ContactCaptionKey: String = "ContactCaptionKey"
}
