package studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.extensions.label
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.domain.model.password.GeneratedPassword
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import studio.lunabee.onesafe.domain.model.password.PasswordStrength
import studio.lunabee.onesafe.model.OSGeneratedPasswordOption
import studio.lunabee.onesafe.molecule.OSGeneratedPassword
import studio.lunabee.onesafe.molecule.OSSlider
import studio.lunabee.onesafe.molecule.OSSwitchOption
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import kotlin.math.roundToInt

@Composable
fun PasswordGeneratorBottomSheet(
    paddingValues: PaddingValues,
    isOverriding: Boolean,
    onPasswordGenerated: (GeneratedPassword) -> Unit,
    onCancel: () -> Unit,
    viewModel: PasswordGeneratorViewModel = hiltViewModel(),
) {
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }
    val password by viewModel.password.collectAsStateWithLifecycle()
    val passwordGeneratorUiState by viewModel.passwordGeneratorUiState.collectAsStateWithLifecycle()
    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    when (val uiState = passwordGeneratorUiState) {
        is PasswordGeneratorUiState.Data -> {
            PasswordGeneratorLayout(
                paddingValues = paddingValues,
                password = password,
                generateNewPassword = { viewModel.generatePassword(it) },
                onConfirm = {
                    if (isOverriding) {
                        dialogState = OverridePasswordDialogState(
                            onConfirm = {
                                onPasswordGenerated(password)
                            },
                            dismiss = {
                                dialogState = null
                            },
                        )
                    } else {
                        onPasswordGenerated(password)
                    }
                },
                onCancel = onCancel,
                passwordGeneratorUiState = uiState,
            )

            LaunchedEffect(isFirstComposition) {
                if (isFirstComposition) {
                    viewModel.generatePassword(uiState)
                    isFirstComposition = false
                }
            }
        }
        PasswordGeneratorUiState.Initializing -> {
            /* no-op */
        }
    }
}

@Composable
fun PasswordGeneratorLayout(
    paddingValues: PaddingValues,
    password: GeneratedPassword,
    generateNewPassword: (passwordGeneratorUiState: PasswordGeneratorUiState.Data) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    passwordGeneratorUiState: PasswordGeneratorUiState.Data,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .testTag(UiConstants.TestTag.BottomSheet.PasswordGeneratorBottomSheet)
            .padding(horizontal = OSDimens.SystemSpacing.ExtraLarge, vertical = OSDimens.SystemSpacing.Regular)
            .padding(bottom = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.passwordGenerator_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier.padding(vertical = OSDimens.SystemSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
        ) {
            OSGeneratedPassword(
                password = password.value,
                options = listOf(
                    OSGeneratedPasswordOption.Primary(
                        icon = OSImageSpec.Drawable(OSDrawable.ic_actualise),
                        contentDescription = LbcTextSpec.StringResource(OSString.passwordGenerator_accessibility_reGenerate_button),
                        onClick = { generateNewPassword(passwordGeneratorUiState) },
                    ),
                ),
            )

            password.strength.label()?.let { strengthLabel ->
                OSText(
                    text = strengthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(UiConstants.TestTag.Item.PasswordStrengthText),
                )
            }
        }

        OSSlider(
            value = passwordGeneratorUiState.length.toFloat(),
            onValueChange = {
                if (it.roundToInt() in PasswordConfig.MinLength..PasswordConfig.MaxLength) {
                    passwordGeneratorUiState.length = it.roundToInt()
                    generateNewPassword(passwordGeneratorUiState)
                }
            },
            stepsNumber = passwordGeneratorUiState.passwordLengthStepNumber,
            valueRange = passwordGeneratorUiState.passwordLengthValueRange,
            label = LbcTextSpec.StringResource(
                OSString.passwordGenerator_criteria_length,
                passwordGeneratorUiState.passwordLength.roundToInt(),
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OSDimens.SystemSpacing.ExtraLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OSSwitchOption(
                checked = passwordGeneratorUiState.includeUpperCase,
                onCheckedChange = {
                    passwordGeneratorUiState.includeUpperCase = it
                    generateNewPassword(passwordGeneratorUiState)
                },
                label = LbcTextSpec.StringResource(OSString.passwordGenerator_criteria_upperCase),
                enabled = passwordGeneratorUiState.upperCaseEnabled,
                accessibilityLabel = LbcTextSpec.StringResource(OSString.passwordGenerator_accessibility_criteria_uppercase),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
            )

            OSSwitchOption(
                checked = passwordGeneratorUiState.includeNumber,
                onCheckedChange = {
                    passwordGeneratorUiState.includeNumber = it
                    generateNewPassword(passwordGeneratorUiState)
                },
                label = LbcTextSpec.StringResource(OSString.passwordGenerator_criteria_number),
                enabled = passwordGeneratorUiState.numberEnabled,
                accessibilityLabel = LbcTextSpec.StringResource(OSString.passwordGenerator_accessibility_criteria_number),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OSDimens.SystemSpacing.Regular),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OSSwitchOption(
                checked = passwordGeneratorUiState.includeLowerCase,
                onCheckedChange = {
                    passwordGeneratorUiState.includeLowerCase = it
                    generateNewPassword(passwordGeneratorUiState)
                },
                label = LbcTextSpec.StringResource(OSString.passwordGenerator_criteria_lowerCase),
                enabled = passwordGeneratorUiState.lowerCaseEnabled,
                accessibilityLabel = LbcTextSpec.StringResource(OSString.passwordGenerator_accessibility_criteria_lowercase),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
            )

            OSSwitchOption(
                checked = passwordGeneratorUiState.includeSymbol,
                onCheckedChange = {
                    passwordGeneratorUiState.includeSymbol = it
                    generateNewPassword(passwordGeneratorUiState)
                },
                label = LbcTextSpec.StringResource(OSString.passwordGenerator_criteria_symbol),
                enabled = passwordGeneratorUiState.symbolEnabled,
                accessibilityLabel = LbcTextSpec.StringResource(OSString.passwordGenerator_accessibility_criteria_symbol),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
            )
        }

        OSRegularDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OSDimens.SystemSpacing.Small),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.ExtraLarge),
            ) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.common_cancel),
                    onClick = onCancel,
                    buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                )

                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.common_confirm),
                    onClick = onConfirm,
                )
            }
        }
    }
}

@OsDefaultPreview
@Composable
fun PasswordGeneratorPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordGeneratorLayout(
            paddingValues = PaddingValues(0.dp),
            password = GeneratedPassword("Az1&dks!sic729j", PasswordStrength.Strong),
            generateNewPassword = {},
            onConfirm = {},
            onCancel = {},
            passwordGeneratorUiState = PasswordGeneratorUiState.Data(PasswordConfig.default()),
        )
    }
}
