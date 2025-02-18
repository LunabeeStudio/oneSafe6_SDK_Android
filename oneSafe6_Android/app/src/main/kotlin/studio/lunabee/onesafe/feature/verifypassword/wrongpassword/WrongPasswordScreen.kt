package studio.lunabee.onesafe.feature.verifypassword.wrongpassword

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun WrongPasswordRoute(
    navigateBack: () -> Unit,
    navigateToChangePassword: () -> Unit,
    viewModel: WrongPasswordViewModel = hiltViewModel(),
) {
    val uiState: WrongPasswordUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    WrongPasswordScreen(
        navigateBack = navigateBack,
        onClickOnChangePassword = viewModel::startChangePasswordFlow,
    )

    if (uiState is WrongPasswordUiState.ShowBiometric) {
        val biometricPrompt = biometricPrompt(
            labels = DisplayBiometricLabels.Verify,
            getCipher = { (uiState as? WrongPasswordUiState.ShowBiometric)?.cipher },
            onSuccess = {
                viewModel.resetState()
                coroutineScope.launch {
                    navigateToChangePassword()
                }
            },
            onFailure = {
                viewModel.resetState()
                coroutineScope.launch {
                    navigateBack()
                }
            },
            onUserCancel = viewModel::resetState,
            onNegative = viewModel::resetState,
        )
        LaunchedEffect(Unit) {
            biometricPrompt()
        }
    }
}

@Composable
fun WrongPasswordScreen(
    navigateBack: () -> Unit,
    onClickOnChangePassword: () -> Unit,
) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.WrongPasswordScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_jamy_reflexion,
                offset = OSDimens.Card.DefaultImageCardOffset,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.wrongPassword_card_title),
                    description = LbcTextSpec.StringResource(OSString.wrongPassword_card_description),
                    action = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = OSDimens.SystemSpacing.Small),
                        ) {
                            OSFilledButton(
                                text = LbcTextSpec.StringResource(OSString.wrongPassword_card_retryButton),
                                onClick = navigateBack,
                                buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                                leadingIcon = {
                                    Icon(painter = painterResource(id = OSDrawable.ic_restore), contentDescription = null)
                                },
                            )

                            OSText(text = LbcTextSpec.StringResource(OSString.common_or))

                            OSFilledButton(
                                text = LbcTextSpec.StringResource(OSString.wrongPassword_card_changePasswordButton),
                                onClick = onClickOnChangePassword,
                                buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                                leadingIcon = {
                                    Icon(painter = painterResource(id = OSDrawable.ic_edit), contentDescription = null)
                                },
                            )
                        }
                    },
                )
            }
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

@OsDefaultPreview
@Composable
fun WrongPasswordScreenPreview() {
    OSPreviewBackgroundTheme {
        WrongPasswordScreen(
            navigateBack = {},
            onClickOnChangePassword = {},
        )
    }
}
