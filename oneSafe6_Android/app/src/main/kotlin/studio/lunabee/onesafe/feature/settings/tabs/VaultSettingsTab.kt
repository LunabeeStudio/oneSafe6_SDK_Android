package studio.lunabee.onesafe.feature.settings.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.settings.prevention.PreventionSettingsWarningData
import studio.lunabee.onesafe.feature.settings.prevention.UiPreventionSettingsWarning
import studio.lunabee.onesafe.feature.settings.settingcard.impl.SettingLockCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.SettingSecurityCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.SettingsBackupCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.VaultExtensionSettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.WidgetPanicModeSettingsCard
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes
import studio.lunabee.onesafe.organism.card.OSMessageCardStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.TestTag.ScrollableContent.SettingsSafeLazyColumn
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun VaultSettingsTab(
    importData: () -> Unit,
    exportData: () -> Unit,
    navigateToSecuritySettings: () -> Unit,
    navigateToBubblesSettings: () -> Unit,
    navigateToPersonalizationSettings: () -> Unit,
    startChangePasswordFlow: () -> Unit,
    navigateToAutoBackupSettings: () -> Unit,
    onSafeDeletion: () -> Unit,
    onCloseIndependentVaultsMessage: () -> Unit,
    showSafeCta: Boolean,
    navigateToPanicWidgetSettings: () -> Unit,
    isWidgetEnabled: Boolean,
    isPanicModeEnabled: Boolean,
    preventionSettingsWarningData: PreventionSettingsWarningData?,
) {
    LazyColumn(
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        modifier = Modifier
            .testTag(SettingsSafeLazyColumn),
    ) {
        preventionSettingsWarningData?.let { (preventionWarning, onPreventionCardClicked, onClosePreventWarningMessage) ->
            item(
                key = UiPreventionSettingsWarning::class.simpleName,
            ) {
                OSMessageCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .testTag(UiConstants.TestTag.Item.FeedbackWarningPrevention),
                    title = preventionWarning.title,
                    description = preventionWarning.description,
                    attributes = OSMessageCardAttributes()
                        .dismissible(
                            icon = OSImageSpec.Drawable(OSDrawable.ic_close),
                            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                            onDismiss = onClosePreventWarningMessage,
                        )
                        .style(OSMessageCardStyle.Feedback)
                        .clickable(onPreventionCardClicked),
                )
            }
        }
        if (showSafeCta) {
            item(
                key = OSString.settings_multiSafe_card_title,
            ) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.settings_multiSafe_card_title),
                    description = LbcTextSpec.StringResource(OSString.settings_multiSafe_card_message),
                    attributes = OSMessageCardAttributes()
                        .dismissible(
                            icon = OSImageSpec.Drawable(OSDrawable.ic_close),
                            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                            onDismiss = onCloseIndependentVaultsMessage,
                        ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                )
            }
        }

        item(
            key = OSString.settings_section_security_title,
        ) {
            SettingSecurityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                onClickSecurityOption = navigateToSecuritySettings,
                onClickOnPersonalizationOption = navigateToPersonalizationSettings,
                onClickChangePasswordOption = {
                    startChangePasswordFlow()
                },
            )
        }

        item(
            key = OSString.settings_panicDestruction_section_title,
        ) {
            WidgetPanicModeSettingsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                onClick = navigateToPanicWidgetSettings,
                isWidgetEnabled = isWidgetEnabled,
                isPanicModeEnabled = isPanicModeEnabled,
            )
        }

        item(
            key = OSString.settings_backupCard_title,
        ) {
            SettingsBackupCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                onClickOnImport = importData,
                onClickOnExport = exportData,
                onClickOnAutoBackup = navigateToAutoBackupSettings,
            )
        }

        item(
            key = OSString.settings_section_onesafe_extensions,
        ) {
            VaultExtensionSettingsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                navigateToBubblesSettings = navigateToBubblesSettings,
            )
        }

        item(
            key = OSString.settings_multiSafe_deleteSafe,
        ) {
            SettingLockCard(
                modifier = Modifier.animateItem(),
                onSafeDeletion = onSafeDeletion,
            )
        }
    }
}
