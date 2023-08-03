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
 * Last modified 6/23/23, 3:39 PM
 */

package studio.lunabee.onesafe.bubbles.ui.onesafek

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.extension.osShadowElevation
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import java.util.UUID

@Composable
fun SelectContactRoute(
    navigateBack: () -> Unit,
    navigateToWriteMessage: (UUID) -> Unit,
    @DrawableRes exitIcon: Int = R.drawable.ic_back,
    viewModel: SelectContactViewModel = hiltViewModel(),
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    SelectContactScreen(
        navigateBack,
        contacts = contacts,
        onClickOnContact = { contactId ->
            navigateToWriteMessage(contactId)
        },
        exitIcon = exitIcon,
    )
}

@Composable
private fun SelectContactScreen(
    navigateBack: () -> Unit,
    contacts: List<BubblesContactInfo>?,
    @DrawableRes exitIcon: Int = R.drawable.ic_back,
    onClickOnContact: (UUID) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val embeddedKeyboardHeight: Dp = LocalKeyboardUiHeight.current
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OneSafeKSelectContactScreen,
        applySystemBarPadding = false,
        modifier = if (embeddedKeyboardHeight != 0.dp) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        }.landscapeSystemBarsPadding(),
    ) {
        Column(
            Modifier
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .osShadowElevation(lazyListState.topAppBarElevation)
                    .statusBarsPadding(),
            ) {
                OSTopAppBar(
                    title = LbcTextSpec.StringResource(R.string.oneSafeK_selectContact_title),
                    options = listOf(
                        object : TopAppBarOptionNav(
                            image = OSImageSpec.Drawable(exitIcon),
                            contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
                            onClick = navigateBack,
                            state = OSActionState.Enabled,
                        ) {},
                    ),
                )
            }

            if (contacts != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = OSDimens.SystemSpacing.Hairline)
                        .padding(bottom = embeddedKeyboardHeight),
                    contentPadding = PaddingValues(
                        horizontal = OSDimens.SystemSpacing.Regular,
                        vertical = OSDimens.SystemSpacing.Regular,
                    ),
                    state = lazyListState,
                ) {
                    if (contacts.isEmpty()) {
                        SelectContactFactory.addEmptyCard(this)
                    } else {
                        SelectContactFactory.addContacts(contacts, onClickOnContact, this)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun SelectContactScreenPreview() {
    OSPreviewBackgroundTheme {
        SelectContactScreen(
            navigateBack = { },
            contacts = listOf(
                BubblesContactInfo(UUID.randomUUID(), DefaultNameProvider("Rémi")),
                BubblesContactInfo(UUID.randomUUID(), DefaultNameProvider("Florian")),
            ),
            onClickOnContact = {},
        )
    }
}

@Composable
@Preview
fun EmptySelectContactScreenPreview() {
    OSPreviewBackgroundTheme {
        SelectContactScreen(
            navigateBack = { },
            contacts = listOf(),
            onClickOnContact = {},
        )
    }
}
