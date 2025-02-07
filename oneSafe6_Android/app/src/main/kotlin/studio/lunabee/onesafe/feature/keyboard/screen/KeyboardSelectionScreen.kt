package studio.lunabee.onesafe.feature.keyboard.screen

import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.utils.observeIsOSKImeEnabledAsStateWithLifecycle
import studio.lunabee.onesafe.common.utils.observeIsOSKImeSelectedAsState
import studio.lunabee.onesafe.common.utils.rememberOSKImeSettings
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationAction
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationActionAttributes
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStep
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStepLayout
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun KeyboardSelectionRoute(
    navigateBack: () -> Unit,
    onKeyboardSelected: () -> Unit,
) {
    val context = LocalContext.current
    val oskImeSettings = rememberOSKImeSettings()
    val isOSKeyboardEnabled by observeIsOSKImeEnabledAsStateWithLifecycle()
    val isOSKeyboardSelected by observeIsOSKImeSelectedAsState(foregroundOnly = true)

    LaunchedEffect(isOSKeyboardEnabled, isOSKeyboardSelected) {
        if (isOSKeyboardEnabled && isOSKeyboardSelected) {
            onKeyboardSelected()
        }
    }

    KeyboardSelectionScreen(
        navigateBack = navigateBack,
        isKeyboardEnabled = isOSKeyboardEnabled,
        onClickOnSelectKeyboard = {
            context.getSystemService(InputMethodManager::class.java).showInputMethodPicker()
        },
        onClickOnEnableKeyboard = oskImeSettings::requestEnableOSKIme,
    )
}

@Composable
fun KeyboardSelectionScreen(
    navigateBack: () -> Unit,
    onClickOnEnableKeyboard: () -> Unit,
    onClickOnSelectKeyboard: () -> Unit,
    isKeyboardEnabled: Boolean,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OneSafeKKeyboardSelectionScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    bottom = OSDimens.SystemSpacing.Huge,
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            PresentationStepLayout(
                presentationStep = PresentationStep(
                    title = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_keyboardSelection_title),
                    description = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_keyboardSelection_description).markdown(),
                    imageRes = OSDrawable.illustration_type_protection,
                    actions = if (!isKeyboardEnabled) {
                        listOf(
                            PresentationAction(
                                label = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_keyboardSelection_enableButton),
                                action = onClickOnEnableKeyboard,
                            ),
                        )
                    } else {
                        listOf(
                            PresentationAction(
                                label = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_keyboardSelection_enableButton),
                                action = {},
                                attributes = PresentationActionAttributes()
                                    .disabled()
                                    .leadingIcon(OSImageSpec.Drawable(OSDrawable.ic_check)),
                            ),
                            PresentationAction(
                                label = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_keyboardSelection_chooseButton),
                                action = onClickOnSelectKeyboard,
                            ),
                        )
                    },
                ),
            )
        }
        OSTopAppBar(
            title = LbcTextSpec.Raw(""),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
        )
    }
}

@Composable
@OsDefaultPreview
fun KeyboardSelectionScreenPreview() {
    OSPreviewBackgroundTheme {
        KeyboardSelectionScreen(
            navigateBack = {},
            onClickOnSelectKeyboard = {},
            onClickOnEnableKeyboard = {},
            isKeyboardEnabled = true,
        )
    }
}
