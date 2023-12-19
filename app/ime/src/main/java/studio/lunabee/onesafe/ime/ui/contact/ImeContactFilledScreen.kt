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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 2:46 PM
 */

package studio.lunabee.onesafe.ime.ui.contact

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.ime.ui.OSImeScreen
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

@Composable
fun ImeContactFilledScreen(
    uiState: ImeContactUiState.Data,
    navigateBack: () -> Unit,
    onClickOnContact: (contactId: UUID, isConversationReady: Boolean) -> Unit,
    @DrawableRes exitIcon: Int,
    navigateToBubblesHomeContact: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    OSImeScreen(
        testTag = UiConstants.TestTag.Screen.FilledContactScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = OSDimens.ItemTopBar.Height),
            contentPadding = PaddingValues(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.Regular,
            ),
            state = lazyListState,
        ) {
            ImeContactScreenFactory.addInfoCard(this)
            lazyVerticalOSRegularSpacer()
            ImeContactScreenFactory.addContacts(uiState.contacts, onClickOnContact)
            lazyVerticalOSRegularSpacer()
            ImeContactScreenFactory.addManageContactsCard(
                lazyListScope = this,
                onClick = { navigateToBubblesHomeContact() },
            )
        }

        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(R.string.oneSafeK_conversationScreen_title),
            options = listOf(
                TopAppBarOptionNav(
                    image = OSImageSpec.Drawable(exitIcon),
                    contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
                    onClick = navigateBack,
                    state = OSActionState.Enabled,
                    color = {
                        OSIconButtonDefaults.iconButtonColors(
                            containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            state = it,
                        )
                    },
                ),
            ),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}

@Composable
@OsDefaultPreview
private fun ImeContactFilledScreenPreview() {
    OSPreviewBackgroundTheme {
        ImeContactFilledScreen(
            uiState = ImeContactUiState.Data(
                ConversationState.entries.map {
                    UIBubblesContactInfo(UUID.randomUUID(), DefaultNameProvider(it.name), it)
                },
            ),
            navigateBack = { },
            onClickOnContact = { _, _ -> },
            exitIcon = R.drawable.ic_back,
        ) {}
    }
}
