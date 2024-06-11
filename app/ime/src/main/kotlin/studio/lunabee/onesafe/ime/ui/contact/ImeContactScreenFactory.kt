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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/29/23, 6:19 PM
 */

package studio.lunabee.onesafe.ime.ui.contact

import androidx.compose.foundation.lazy.LazyListScope
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.bubbles.ui.composables.ContactItemCardContent
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

object ImeContactScreenFactory {
    fun addEmptyCard(
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item(
            key = EmptyCardKey,
        ) {
            EmptyContactCard()
        }
    }

    fun addManageContactsCard(
        lazyListScope: LazyListScope,
        onClick: () -> Unit,
    ) {
        lazyListScope.item(
            key = ManageContactsCardKey,
        ) {
            OSCard {
                OSClickableRow(
                    text = LbcTextSpec.StringResource(OSString.oneSafeK_contact_manageButton),
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_people)) },
                    onClick = onClick,
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(1, 1),
                )
            }
        }
    }

    context(LazyListScope)
    fun addContacts(
        contacts: List<UIBubblesContactInfo>,
        onClick: (contactId: UUID, isConversationReady: Boolean) -> Unit,
    ) {
        val cardContents = mutableListOf<OSLazyCardContent>()

        cardContents += contacts.map { contact ->
            ContactItemCardContent(
                contactInfo = contact,
                key = contact.id,
                onClick = {
                    onClick(
                        contact.id,
                        contact.isConversationReady,
                    )
                },
            )
        }

        osLazyCard(cardContents)
    }

    fun addInfoCard(lazyListScope: LazyListScope) {
        lazyListScope.item(
            key = InfoCardKey,
        ) {
            OSMessageCard(
                description = LbcTextSpec.StringResource(id = OSString.oneSafeK_contact_infoCard_description),
            )
        }
    }

    private const val InfoCardKey: String = "InfoCardKey"
    private const val EmptyCardKey: String = "EmptyCardKey"
    private const val ManageContactsCardKey: String = "ManageContactsCardKey"
}
