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
 * Created by Lunabee Studio / Date - 7/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/07/2023 09:54
 */

package studio.lunabee.onesafe.bubbles.ui.decryptmessage

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

context(DecryptMessageNavScope)
@Composable
fun DecryptMessageRoute(
    viewModel: DecryptMessageViewModel = hiltViewModel(),
) {
    val messageValue = rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiResultState.collectAsStateWithLifecycle()
    when (val safeUiState = uiState) {
        is DecryptMessageUiState.NavigateToConversation -> {
            LaunchedEffect(Unit) { navigateDecryptMessageToConversation(safeUiState.decryptResult) }
        }
        is DecryptMessageUiState.NavigateToCreateContact -> {
            LaunchedEffect(Unit) { navigateToCreateContactPopToHome(safeUiState.messageString) }
        }
        else -> {}
    }
    DecryptMessageScreen(
        onBackClick = navigateBack,
        error = (uiState as? DecryptMessageUiState.Error)?.error,
        onMessageChange = { messageValue.value = it },
        message = messageValue.value,
        onDecryptClick = { viewModel.handleMessage(messageValue.value) },
    )
}

@Composable
fun DecryptMessageScreen(
    onBackClick: () -> Unit,
    error: OSError?,
    message: String,
    onMessageChange: (String) -> Unit,
    onDecryptClick: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.DecryptMessageScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(R.string.bubbles_decrypMessageScreen_title),
                options = listOf(topAppBarOptionNavBack(onBackClick)),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            ) {
                OSRegularSpacer()
                OSTextField(
                    value = message,
                    label = null,
                    placeholder = LbcTextSpec.StringResource(R.string.bubbles_decrypMessageScreen_placeholder),
                    onValueChange = onMessageChange,
                    errorLabel = error.description(),
                    isError = error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (isSystemInDarkTheme()) {
                            LocalColorPalette.current.Neutral70
                        } else {
                            LocalColorPalette.current.Neutral30
                        },
                        unfocusedLabelColor = if (isSystemInDarkTheme()) {
                            LocalColorPalette.current.Neutral10
                        } else {
                            LocalColorPalette.current.Neutral80
                        },
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    trailingAction = {
                        if (message.isNotEmpty()) {
                            OSIconButton(
                                image = OSImageSpec.Drawable(R.drawable.ic_close),
                                onClick = { onMessageChange("") },
                                state = OSActionState.Enabled,
                                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                                colors = OSIconButtonDefaults.secondaryIconButtonColors(state = OSActionState.Enabled),
                                contentDescription = LbcTextSpec.StringResource(R.string.bubbles_decrypMessageScreen_accessibility_erase),
                                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
                            )
                        }
                    },
                )
                OSRegularSpacer()
                OSFilledButton(
                    text = LbcTextSpec.StringResource(R.string.bubbles_decrypMessageScreen_decryptButton),
                    onClick = onDecryptClick,
                    modifier = Modifier.align(Alignment.End),
                    state = if (message.isNotEmpty()) OSActionState.Enabled else OSActionState.Disabled,
                )
                OSSmallSpacer()
            }
        }
    }
}

interface DecryptMessageNavScope {
    val navigateBack: () -> Unit
    val navigateDecryptMessageToConversation: (DecryptResult) -> Unit
    val navigateToCreateContactPopToHome: (String) -> Unit
}
