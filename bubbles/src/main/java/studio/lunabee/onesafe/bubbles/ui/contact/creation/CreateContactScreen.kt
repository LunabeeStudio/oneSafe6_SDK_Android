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
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 13/07/2023 10:45
 */

package studio.lunabee.onesafe.bubbles.ui.contact.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.composables.DeeplinkSwitchRow
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import timber.log.Timber
import java.util.UUID

@Composable
fun CreateContactRoute(
    type: ContactCreationType,
    navigateBack: () -> Unit,
    navigateToNextScreen: (UUID) -> Unit,
    viewModel: CreateContactViewModel,
) {
    val contactName = rememberSaveable { mutableStateOf("") }
    val isDeeplinkChecked = rememberSaveable { mutableStateOf(true) }
    val createResult by viewModel.createInvitationResult.collectAsStateWithLifecycle()
    val icon by remember {
        derivedStateOf {
            contactName.value.let { name ->
                OSNameProvider.fromName(
                    name = name,
                    hasIcon = false,
                ).let {
                    if (it is EmojiNameProvider) {
                        OSItemIllustration.Emoji(it.placeholderName, null)
                    } else {
                        OSItemIllustration.Text(it.placeholderName, null)
                    }
                }
            }
        }
    }

    val result = createResult
    if (result != null) {
        LaunchedEffect(result) {
            when (result) {
                is LBResult.Success -> navigateToNextScreen(result.successData)
                is LBResult.Failure -> {
                    // TODO What append if error?
                    Timber.e(result.throwable)
                }
            }
        }
    }

    CreateContactScreen(
        type = type,
        onBackClick = navigateBack,
        icon = icon,
        onInviteClick = {
            viewModel.createContact(contactName.value, isDeeplinkChecked.value)
        },
        onDeeplinkChange = { isDeeplinkChecked.value = it },
        isDeeplinkChecked = isDeeplinkChecked.value,
        onContactNameChange = { contactName.value = it },
        contactName = contactName.value,
    )
}

@Composable
fun CreateContactScreen(
    type: ContactCreationType,
    onBackClick: () -> Unit,
    icon: OSItemIllustration,
    onInviteClick: () -> Unit,
    onDeeplinkChange: (Boolean) -> Unit,
    isDeeplinkChecked: Boolean,
    contactName: String,
    onContactNameChange: (String) -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.CreateContactScreen,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding(),
        ) {
            OSTopAppBar(
                title = type.title,
                options = listOf(TopAppBarOptionNavBack(onBackClick)),
            )
            LazyColumn(
                contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
                modifier = Modifier.fillMaxSize(),
            ) {
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.header(iconProvider = icon, lazyListScope = this)
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.nameTextField(
                    contactName = contactName,
                    onContactNameChange = onContactNameChange,
                    lazyListScope = this,
                )
                lazyVerticalOSRegularSpacer()
                item {
                    DeeplinkSwitchRow(onValueChange = onDeeplinkChange, isChecked = isDeeplinkChecked)
                }
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.inviteButton(
                    lazyListScope = this,
                    onClick = onInviteClick,
                    label = type.buttonString,
                    isEnabled = contactName.isNotEmpty(),
                )
            }
        }
    }
}
