package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.CardSettingsNavAction
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.feature.settings.personalization.UiCameraSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OtherOptionsSettingCard(
    isScreenshotAllowed: Boolean,
    modifier: Modifier = Modifier,
    toggleAllowScreenshot: () -> Unit,
    onSetCameraClick: () -> Unit,
    selectedCameraSystem: CameraSystem,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_other_options),
        modifier = modifier,
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.settings_security_section_autolock_allowScreenshot_label),
                onValueChange = { toggleAllowScreenshot() },
                isChecked = isScreenshotAllowed,
            ),
            object : CardSettingsNavAction(
                icon = null,
                text = UiCameraSystem.valueOf(selectedCameraSystem.name).title,
            ) {
                override val onClick: () -> Unit = onSetCameraClick
            },
        ),
    )
}

@OsDefaultPreview
@Composable
fun OtherOptionsSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        OtherOptionsSettingCard(
            isScreenshotAllowed = false,
            toggleAllowScreenshot = {},
            onSetCameraClick = {},
            selectedCameraSystem = CameraSystem.External,
        )
    }
}
