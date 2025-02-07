package studio.lunabee.onesafe.feature.settings.bubbles

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionStartOneSafeKOnBoardingLabel
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun ExtensionOneSafeKOnBoardingCard(
    onClickOnStartOnBoarding: () -> Unit,
) {
    SettingsCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(UiConstants.TestTag.Item.OneSafeKStartOnBoardingCard),
        title = LbcTextSpec.StringResource(OSString.oneSafeK_extension_title),
        actions = listOf(CardSettingsActionStartOneSafeKOnBoardingLabel(onClickOnStartOnBoarding)),
        footer = LbcTextSpec.StringResource(OSString.oneSafeK_extension_startOnBoarding_footer),
    )
}
