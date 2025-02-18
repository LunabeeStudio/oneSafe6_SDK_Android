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
 * Created by Lunabee Studio / Date - 8/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/08/2024 13:40
 */

package studio.lunabee.onesafe.feature.importbackup.bubbleswarning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.snackbar.DefaultSnackbarVisuals
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ImportBubblesWarningRoute(
    navScope: ImportBubblesWarningNavScope,
    viewModel: ImportBubblesWarningViewModel = hiltViewModel(),
) {
    val importSaveDataState: ImportSaveDataUiState by viewModel.importSaveDataState.collectAsStateWithLifecycle()

    val successMessage = stringResource(id = OSString.import_success_message)
    when (val state = importSaveDataState) {
        is ImportSaveDataUiState.Dialog -> state.dialogState.DefaultAlertDialog()
        is ImportSaveDataUiState.ExitWithError -> {
            val snackbarVisuals = state.error?.snackbarVisuals ?: DefaultSnackbarVisuals(
                message = stringResource(id = OSString.error_defaultMessage),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
                actionLabel = null,
            )
            LaunchedEffect(Unit) {
                navScope.showSnackBar.invoke(snackbarVisuals)
                navScope.navigateBackToFileSelection()
                viewModel.resetState()
            }
        }
        ImportSaveDataUiState.Success -> {
            LaunchedEffect(Unit) {
                navScope.showSnackBar.invoke(
                    DefaultSnackbarVisuals(
                        message = successMessage,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                        actionLabel = null,
                    ),
                )
                navScope.navigateBackToSettings()
            }
        }
        is ImportSaveDataUiState.ImportInProgress,
        is ImportSaveDataUiState.WaitingForUserChoice,
        -> {
            // Handle directly in screen
        }
    }

    val cardProgress = if (importSaveDataState is ImportSaveDataUiState.ImportInProgress) {
        OSCardProgressParam.UndeterminedProgress(
            progressDescription = LbcTextSpec.StringResource(OSString.import_progressCard_title),
        )
    } else {
        null
    }

    ImportBubblesWarningScreen(
        onConfirmClick = {
            if (viewModel.hasItemsToImports) {
                navScope.navigateToSaveData()
            } else {
                viewModel.saveBubblesData()
            }
        },
        cardProgress = cardProgress,
        navigateBack = navScope.navigateBack,
        confirmLabel = if (viewModel.hasItemsToImports) {
            LbcTextSpec.StringResource(OSString.import_bubblesWarning_confirmButton)
        } else {
            LbcTextSpec.StringResource(OSString.import_bubblesWarning_import)
        },
    )
}

@Composable
fun ImportBubblesWarningScreen(
    onConfirmClick: () -> Unit,
    navigateBack: () -> Unit,
    confirmLabel: LbcTextSpec,
    cardProgress: OSCardProgressParam?,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ImportSaveDataScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OSTopImageLoadingCard(
                title = LbcTextSpec.StringResource(OSString.import_bubblesWarning_title),
                description = LbcTextSpec.StringResource(OSString.import_bubblesWarning_description),
                cardProgress = cardProgress,
                cardImage = OSCardImageParam(
                    imageRes = OSDrawable.character_jamy_cool,
                    offset = OSDimens.Card.DefaultImageCardOffset,
                ),
                modifier = Modifier,
            )
            OSFilledButton(
                text = confirmLabel,
                onClick = onConfirmClick,
                modifier = Modifier.align(Alignment.End),
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )
        }
    }
}

@Preview
@Composable
private fun ImportBubblesWarningPreview() {
    OSTheme {
        ImportBubblesWarningScreen(
            onConfirmClick = {},
            navigateBack = {},
            confirmLabel = LbcTextSpec.StringResource(OSString.import_bubblesWarning_import),
            cardProgress = null,
        )
    }
}
