package studio.lunabee.onesafe.feature.importbackup.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityCustomAction
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.accessibility.accessibilityLiveRegion
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSExtraSmallSpacer
import studio.lunabee.onesafe.atom.OSLinearProgress
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSTypography
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.LocalDateTime

@Composable
fun ImportAuthScreen(
    unlockArchive: (password: String) -> Unit,
    displayCredentialsError: Boolean,
    resetCredentialsError: () -> Unit,
    isCheckingPassword: Boolean,
    navigateBackToSettings: () -> Unit,
    importAuthArchiveKindLabels: ImportAuthArchiveKindLabels,
) {
    val scrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    var passwordTextFieldValue: String by remember { mutableStateOf(value = "") }
    var isPasswordTextFieldValueVisible: Boolean by remember { mutableStateOf(value = false) }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ImportAuthScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_jamy_hide,
                offset = null,
            ) {
                OSCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    OSRegularSpacer()

                    val cardContentModifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OSDimens.SystemSpacing.Regular)

                    // Title + message wrapped in a Column for accessibility purpose.
                    Column(
                        modifier = cardContentModifier
                            .accessibilityMergeDescendants(),
                    ) {
                        OSText(
                            text = LbcTextSpec.StringResource(OSString.import_decryptImportCard_title),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        OSRegularSpacer()

                        importAuthArchiveKindLabels.Description()

                        OSRegularSpacer()
                    }

                    // Password + hint wrapped in a Column for accessibility purpose
                    Column(
                        modifier = cardContentModifier
                            .accessibilityMergeDescendants(),
                    ) {
                        OSTextField(
                            value = passwordTextFieldValue,
                            label = LbcTextSpec.StringResource(id = OSString.import_decryptImportCard_passwordFieldLabelOsPlus),
                            placeholder = LbcTextSpec.StringResource(id = OSString.import_decryptImportCard_passwordFieldLabelOsPlus),
                            onValueChange = {
                                passwordTextFieldValue = it
                                if (displayCredentialsError) resetCredentialsError()
                            },
                            enabled = !isCheckingPassword,
                            trailingAction = {
                                VisibilityTrailingAction(
                                    isSecuredVisible = isPasswordTextFieldValueVisible,
                                    onClick = { isPasswordTextFieldValueVisible = !isPasswordTextFieldValueVisible },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .accessibilityInvisibleToUser(),
                                )
                            },
                            visualTransformation = if (isPasswordTextFieldValueVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            inputTextStyle = LocalTextStyle.current.copy(fontFamily = OSTypography.Legibility),
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions {
                                if (passwordTextFieldValue.isEmpty()) {
                                    defaultKeyboardAction(imeAction = ImeAction.Done) // hide keyboard
                                } else {
                                    unlockArchive(passwordTextFieldValue)
                                    defaultKeyboardAction(imeAction = ImeAction.Done) // hide keyboard
                                }
                            },
                            modifier = Modifier
                                .composed {
                                    val visibilityActionLabel: String = if (isPasswordTextFieldValueVisible) {
                                        stringResource(id = OSString.common_accessibility_hidePassword)
                                    } else {
                                        stringResource(id = OSString.common_accessibility_showPassword)
                                    }

                                    semantics {
                                        customActions = listOf(
                                            accessibilityCustomAction(
                                                label = visibilityActionLabel,
                                                action = { isPasswordTextFieldValueVisible = !isPasswordTextFieldValueVisible },
                                            ),
                                        )
                                    }
                                },
                        )

                        OSExtraSmallSpacer()

                        importAuthArchiveKindLabels.Hint()

                        AnimatedVisibility(visible = displayCredentialsError) {
                            Column(
                                modifier = Modifier
                                    .accessibilityLiveRegion(),
                            ) {
                                OSRegularSpacer()
                                OSText(
                                    text = LbcTextSpec.StringResource(
                                        id = OSString.import_decryptImportCard_errorMessage,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        AnimatedVisibility(visible = isCheckingPassword) {
                            OSLinearProgress(
                                progress = null,
                                modifier = cardContentModifier
                                    .padding(top = OSDimens.SystemSpacing.Regular)
                                    .accessibilityLiveRegion(),
                                progressDescription = LbcTextSpec.StringResource(OSString.import_authentication_progress),
                            )
                        }

                        OSRegularSpacer()
                    }
                }
            }

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.common_next),
                onClick = { unlockArchive(passwordTextFieldValue) },
                state = if (passwordTextFieldValue.isNotEmpty() && !isCheckingPassword) {
                    OSActionState.Enabled
                } else {
                    OSActionState.Disabled
                },
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBackToSettings, isEnabled = !isCheckingPassword)),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun ImportAuthScreenPreview() {
    OSTheme {
        ImportAuthScreen(
            unlockArchive = {},
            displayCredentialsError = true,
            resetCredentialsError = {},
            isCheckingPassword = false,
            navigateBackToSettings = {},
            importAuthArchiveKindLabels = ImportAuthArchiveKindLabels.Backup(
                archiveCreationDate = LocalDateTime.now(),
            ),
        )
    }
}
