package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun BiometricSettingCard(
    modifier: Modifier = Modifier,
    isBiometricActivated: Boolean,
    toggleBiometric: (Boolean) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
    ) {
        SettingsCard(
            title = LbcTextSpec.StringResource(OSString.settings_security_section_fastId_title),
            footer = LbcTextSpec.StringResource(OSString.settings_security_section_fastId_useBiometric_info),
            modifier = modifier,
            actions = listOf(
                SwitchSettingAction(
                    label = LbcTextSpec.StringResource(OSString.settings_security_section_fastId_useBiometric_label),
                    onValueChange = toggleBiometric,
                    isChecked = isBiometricActivated,
                ),
            ),
        )
    }
}

@OsDefaultPreview
@Composable
fun BiometricSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        BiometricSettingCard(
            isBiometricActivated = false,
            toggleBiometric = {},
        )
    }
}
