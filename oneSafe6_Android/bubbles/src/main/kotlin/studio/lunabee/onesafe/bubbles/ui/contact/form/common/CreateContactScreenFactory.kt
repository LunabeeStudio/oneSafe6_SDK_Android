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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 09:50
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens

object CreateContactScreenFactory {

    fun header(
        iconProvider: OSItemIllustration,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                iconProvider.ImageComposable(
                    contentDescription = null,
                    style = OSSafeItemStyle.Large,
                )
            }
        }
    }

    fun nameTextField(
        lazyListScope: LazyListScope,
        contactName: String,
        onContactNameChange: (String) -> Unit,
    ) {
        lazyListScope.item {
            OSCard {
                OSTextField(
                    value = contactName,
                    label = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_textFieldLabel),
                    placeholder = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_textFieldLabel),
                    onValueChange = onContactNameChange,
                    modifier = Modifier.padding(
                        start = OSDimens.SystemSpacing.Regular,
                        end = OSDimens.SystemSpacing.Regular,
                        top = OSDimens.SystemSpacing.Regular - OSDimens.External.OutlinedTextFieldTopPadding,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                )
            }
        }
    }

    fun inviteButton(
        lazyListScope: LazyListScope,
        onClick: () -> Unit,
        label: LbcTextSpec,
        isEnabled: Boolean,
    ) {
        lazyListScope.item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OSFilledButton(
                    text = label,
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    state = if (isEnabled) OSActionState.Enabled else OSActionState.Disabled,
                )
            }
        }
    }
}
