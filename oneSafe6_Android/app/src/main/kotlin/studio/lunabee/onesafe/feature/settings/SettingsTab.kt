package studio.lunabee.onesafe.feature.settings

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

enum class SettingsTab(val title: LbcTextSpec) {
    ActualSafe(LbcTextSpec.StringResource(OSString.settings_tab_safe)),
    OneSafe(LbcTextSpec.StringResource(OSString.settings_tab_app)),
}
