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
 * Created by Lunabee Studio / Date - 8/22/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 15:19
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.model.OSItemIllustration
import java.util.UUID

context(ContactFormNavScope)
@Composable
fun ContactFormRoute(
    type: ContactFormType,
    viewModel: ContactFormViewModel,
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val createResult by viewModel.createInvitationResult.collectAsStateWithLifecycle()
    val icon by remember {
        derivedStateOf {
            state.name.let { name ->
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

    var errorDialogState by rememberDialogState()
    errorDialogState?.DefaultAlertDialog()

    LaunchedEffect(createResult) {
        when (val result = createResult) {
            is LBResult.Success -> navigateToNextScreen(result.successData)
            is LBResult.Failure -> {
                errorDialogState = ErrorDialogState(result.throwable, listOf(DialogAction.commonOk(navigateBack)), navigateBack)
            }
            null -> {}
        }
    }

    ContactFormScreen(
        type = type,
        onBackClick = navigateBack,
        icon = icon,
        onInviteClick = {
            viewModel.saveContact(state.name, state.isUsingDeepLink)
        },
        onDeeplinkChange = viewModel::setIsUsingDeepLink,
        isDeeplinkChecked = state.isUsingDeepLink,
        onContactNameChange = viewModel::setName,
        contactName = state.name,
    )
}

interface ContactFormNavScope {
    val navigateBack: () -> Unit
    val navigateToNextScreen: (UUID) -> Unit
}
