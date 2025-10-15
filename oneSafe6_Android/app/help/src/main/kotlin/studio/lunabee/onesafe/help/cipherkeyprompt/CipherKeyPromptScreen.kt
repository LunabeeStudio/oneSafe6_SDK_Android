package studio.lunabee.onesafe.help.cipherkeyprompt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.home.TextLogo
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
context(CipherKeyPromptNavigation)
fun CipherKeyPromptRoute(
    viewModel: CipherKeyPromptViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.openDatabaseResult == CipherKeyPromptUiState.OpenDatabaseState.Success) {
        exitToMain()
    }

    // TODO <cipher> add test which call tryFinishSetupDatabaseEncryption
    // Try to run FinishSetupDatabaseEncryption in case something went wrong during restart
    LaunchedEffect(Unit) {
        viewModel.tryFinishSetupDatabaseEncryption(context)
    }
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    dialogState?.DefaultAlertDialog()

    CipherKeyPromptScreen(
        uiState = uiState,
        setKey = viewModel::setKey,
        onConfirm = viewModel::confirm,
        onWhyClick = { navigateToWhyKeyMissing() },
        onLostKeyClick = { navigateToLostKey() },
    )
}

@Composable
private fun CipherKeyPromptScreen(
    uiState: CipherKeyPromptUiState,
    setKey: (String) -> Unit,
    onConfirm: () -> Unit,
    onWhyClick: () -> Unit,
    onLostKeyClick: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.CipherKeyPromptScreen,
        background = LocalDesignSystem.current.warningBackgroundGradient(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            TextLogo(
                modifier = Modifier
                    .padding(OSDimens.SystemSpacing.Regular)
                    .width(OSDimens.LayoutSize.LoginLogoTextWidth)
                    .align(Alignment.CenterHorizontally),
            )
            OSTopImageBox(
                imageRes = OSDrawable.character_hello,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.ExtraLarge,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
                offset = OSDimens.Card.OffsetHelloColored,
            ) {
                CipherKeyTextFieldCard(
                    keyValue = uiState.key,
                    onKeyChange = setKey,
                    focusRequester = remember { FocusRequester() },
                    errorText = (uiState.openDatabaseResult as? CipherKeyPromptUiState.OpenDatabaseState.Error)
                        ?.description,
                    onConfirm = onConfirm,
                    isLoading = uiState.openDatabaseResult == CipherKeyPromptUiState.OpenDatabaseState.Loading,
                    onWhyClick = onWhyClick,
                    onLostKeyClick = onLostKeyClick,
                )
            }
            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.common_confirm),
                onClick = onConfirm,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = OSDimens.SystemSpacing.Regular),
            )
            OSRegularSpacer()
        }
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_questions),
            onClick = onWhyClick,
            contentDescription = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_whyButton),
            colors = OSIconButtonDefaults.secondaryIconButtonColors(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(OSDimens.SystemSpacing.Regular),
        )
    }
}

@Composable
@OsDefaultPreview
private fun CipherKeyPromptScreenPreview() {
    OSTheme {
        CipherKeyPromptScreen(
            uiState = CipherKeyPromptUiState.default(),
            setKey = {},
            onConfirm = {},
            onWhyClick = {},
            onLostKeyClick = {},
        )
    }
}
