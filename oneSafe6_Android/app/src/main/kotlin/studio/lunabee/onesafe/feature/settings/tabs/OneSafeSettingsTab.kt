package studio.lunabee.onesafe.feature.settings.tabs

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.IconSettingsAction
import studio.lunabee.onesafe.feature.settings.settingcard.impl.OneSafeExtensionSettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.OverEncryptionSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.SettingOneSafeCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.SettingsCreateNewSafeCard
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.ui.UiConstants.TestTag.ScrollableContent.SettingsGlobalLazyColumn
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun OneSafeSettingsTab(
    isOverEncryptionEnabled: Boolean?,
    navigateToAutoFillScreen: () -> Unit,
    navigateToAbout: () -> Unit,
    onClickOnRateUs: () -> Unit,
    createNewSafe: () -> Unit,
    onOverEncryptionClick: (Boolean) -> Unit,
    currentAliasSelected: AppIcon,
    onIconAliasClick: (iconAlias: AppIcon) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        modifier = Modifier
            .testTag(SettingsGlobalLazyColumn),
    ) {
        item(
            key = OSString.settings_multiSafe_newSafe,
        ) {
            SettingsCreateNewSafeCard(
                createNewSafe = createNewSafe,
            )
        }

        item(
            key = OSString.settings_personalization_iconAndName_title,
        ) {
            SettingsCard(
                title = LbcTextSpec.StringResource(OSString.settings_personalization_iconAndName_title),
                actions = listOf(IconSettingsAction(currentAliasSelected = currentAliasSelected, onIconAliasClick = onIconAliasClick)),
            )
        }

        isOverEncryptionEnabled?.let { isOverEncryptionEnabled ->
            item(
                key = OSString.settings_security_section_overEncryption_title,
            ) {
                OverEncryptionSettingCard(
                    isEnabled = isOverEncryptionEnabled,
                    onClickOnStatus = { onOverEncryptionClick(isOverEncryptionEnabled) },
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            item(
                key = OSString.settings_section_vault_extensions,
            ) {
                OneSafeExtensionSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    navigateToAutofillSettings = navigateToAutoFillScreen,
                )
            }
        }

        item(
            key = OSString.application_name,
        ) {
            SettingOneSafeCard(
                modifier = Modifier.fillMaxWidth(),
                onClickOnAbout = navigateToAbout,
                onClickOnRateUs = onClickOnRateUs,
            )
        }
    }
}
