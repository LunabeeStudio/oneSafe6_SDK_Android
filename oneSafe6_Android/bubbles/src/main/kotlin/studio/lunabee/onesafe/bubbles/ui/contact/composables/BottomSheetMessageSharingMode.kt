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
 * Created by Lunabee Studio / Date - 6/13/2024 - for the oneSafe6 SDK.
 * Last modified 6/13/24, 3:44 PM
 */

package studio.lunabee.onesafe.bubbles.ui.contact.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.bubbles.ui.contact.model.MessageSharingModeUi
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ModeMessageShared(
    isVisible: Boolean,
    onVisibleChange: () -> Unit,
    sharingModeUi: MessageSharingModeUi,
    onSharingMessageModeChange: (MessageSharingModeUi) -> Unit,
) {
    OSCard {
        OSClickableRow(
            text = sharingModeUi.title,
            label = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode),
            onClick = onVisibleChange,
            contentPadding = PaddingValues(
                top = OSDimens.SystemSpacing.Regular,
                bottom = OSDimens.SystemSpacing.Regular,
                start = OSDimens.SystemSpacing.Regular,
            ),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = OSDrawable.ic_navigate_next),
                    modifier = Modifier.size(OSDimens.ActionButton.IconRegular),
                    contentDescription = null,
                    tint = LocalColorPalette.current.neutral30,
                )
            },
        )
    }

    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onVisibleChange,
    ) { closeBottomSheet, paddingValues ->
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier
                .selectableGroup()
                .wrapContentHeight()
                .padding(vertical = OSDimens.SystemSpacing.Small),
        ) {
            BottomSheetContent(closeBottomSheet, sharingModeUi, onSharingMessageModeChange)
        }
    }
}

@Composable
private fun BottomSheetContent(
    closeBottomSheet: () -> Unit,
    sharingModeUi: MessageSharingModeUi,
    onSharingMessageModeChange: (MessageSharingModeUi) -> Unit,
) {
    Column {
        OSText(
            text = LbcTextSpec.StringResource(OSString.bubbles_createContactScreen_messageSharingMode),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
        )
        MessageSharingModeUi.entries.forEach { mode ->
            OSOptionRow(
                text = mode.title,
                description = mode.description,
                onSelect = {
                    onSharingMessageModeChange(mode)
                    closeBottomSheet()
                },
                isSelected = mode == sharingModeUi,
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun ModeMessageSharedPreview() {
    OSTheme {
        ModeMessageShared(
            isVisible = true,
            onVisibleChange = {},
            sharingModeUi = MessageSharingModeUi.entries.first(),
            onSharingMessageModeChange = {},
        )
    }
}

@OsDefaultPreview
@Composable
fun BottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        BottomSheetContent(
            sharingModeUi = MessageSharingModeUi.CypherText,
            closeBottomSheet = {},
            onSharingMessageModeChange = {},
        )
    }
}
