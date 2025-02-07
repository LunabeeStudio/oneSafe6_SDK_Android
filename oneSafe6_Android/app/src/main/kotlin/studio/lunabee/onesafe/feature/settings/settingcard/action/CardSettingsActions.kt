package studio.lunabee.onesafe.feature.settings.settingcard.action

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.common.extensions.getLabel
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.chip.ComingInputChip
import studio.lunabee.onesafe.commonui.chip.FeatureBetaInputChip
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.commonui.settings.BetaCardSettingsAction
import studio.lunabee.onesafe.commonui.settings.CardSettingsNavAction
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

class CardSettingsActionImportData(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_download,
    text = LbcTextSpec.StringResource(OSString.settings_backupCard_importLabel),
)

class CardSettingsActionExportData(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_upload,
    text = LbcTextSpec.StringResource(OSString.settings_backupCard_exportLabel),
)

class CardSettingsActionSecurityOption(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_security,
    text = LbcTextSpec.StringResource(OSString.settings_section_security_option_label),
)

class CardSettingsActionAbout(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_lightbulb,
    text = LbcTextSpec.StringResource(OSString.settings_section_onesafe_aboutLabel),
)

class CardSettingsActionRateUs(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_star,
    text = LbcTextSpec.StringResource(OSString.settings_section_onesafe_rateUsLabel),
)

class CardSettingsActionFollowOnYoutube(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_youtube,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_youtubeLabel),
)

class CardSettingsActionFollowOnFacebook(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_facebook,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_facebookLabel),
)

class CardSettingsActionFollowOnTwitter(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_twitter,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_twitterLabel),
)

class CardSettingsActionFollowOnTiktok(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_tiktok,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_tiktokLabel),
)

class CardSettingsActionReadTermsOfUse(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_book,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_termsLabel),
)

class CardSettingsActionCredits(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_people,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_creditCard_label),
)

class CardSettingsActionLibraries(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_book,
    text = LbcTextSpec.StringResource(OSString.aboutScreen_librariesCard_label),
)

class CardSettingsActionPersonalizationOption(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_paint,
    text = LbcTextSpec.StringResource(OSString.settings_section_personalization_label),
)

class CardSettingsActionEnableAutofillAction(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.extension_autofillCard_actionEnableAutofill),
)

class CardSettingsActionStartOneSafeKOnBoardingLabel(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.oneSafeK_extension_startOnBoarding),
)

class CardSettingsActionClipboardCleanAction(delay: ClipboardClearDelay, override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.settings_security_section_clipboard_clear),
    onClickLabel = LbcTextSpec.StringResource(OSString.common_modify),
    secondaryText = delay.text,
)

class CardSettingsActionAutoLockInactivityAction(delay: AutoLockInactivityDelay, override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.settings_security_section_autolock_inactivity_title),
    onClickLabel = LbcTextSpec.StringResource(OSString.common_modify),
    secondaryText = delay.text,
)

class CardSettingsActionAutoLockAppChangeAction(delay: AutoLockBackgroundDelay, override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.settings_security_section_autolock_appChange_title),
    onClickLabel = LbcTextSpec.StringResource(OSString.common_modify),
    secondaryText = delay.text,
)

class CardSettingsActionChangePasswordOption(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_key,
    text = LbcTextSpec.StringResource(OSString.settings_section_changePassword_label),
)

class CardSettingsActionVerifyPasswordOption(interval: VerifyPasswordInterval, override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.settings_security_section_verifyPassword_row),
    secondaryText = interval.getLabel(),
)

class CardSettingsActionResendBubblesMessage(override val onClick: () -> Unit, secondaryText: LbcTextSpec?) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(OSString.bubblesSettings_resendMessage),
    secondaryText = secondaryText,
)

class CardSettingsActionBubblesOption(override val onClick: () -> Unit) : BetaCardSettingsAction(
    icon = OSDrawable.ic_people,
    text = LbcTextSpec.StringResource(OSString.settings_bubbles),
)

class CardSettingsActionAutofillOption(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_key,
    text = LbcTextSpec.StringResource(OSString.extension_autofillCard_title),
)

class CardSettingsActionOverEncryptionOption(override val onClick: () -> Unit, isEnabled: Boolean) : ChipCardSettingsAction(
    icon = null,
    text = if (isEnabled) {
        LbcTextSpec.StringResource(OSString.settings_security_section_overEncryption_status_enabled)
    } else {
        LbcTextSpec.StringResource(OSString.settings_security_section_overEncryption_status_disabled)
    },
    state = OSActionState.Enabled,
    chipContentDescription = LbcTextSpec.StringResource(OSString.common_new),
    chip = { FeatureBetaInputChip() },
)

class CardSettingsActionSynchronizeOption(override val onClick: () -> Unit) : ChipCardSettingsAction(
    icon = OSDrawable.ic_synchronize,
    text = LbcTextSpec.StringResource(OSString.settings_backupCard_synchronization),
    state = OSActionState.DisabledWithAction,
    chipContentDescription = LbcTextSpec.StringResource(id = OSString.common_coming),
    chip = { ComingInputChip() },
)

class CardSettingsActionCreateNewSafeAction(override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = OSDrawable.ic_add,
    text = LbcTextSpec.StringResource(OSString.settings_multiSafe_newSafe),
)

class CardAutoDestructionSettingsAction(
    isAutoDestructionEnabled: Boolean,
    override val onClick: () -> Unit,
) : CardSettingsNavAction(
    icon = null,
    text = if (isAutoDestructionEnabled) {
        LbcTextSpec.StringResource(OSString.settings_security_section_autodestruction_enabled)
    } else {
        LbcTextSpec.StringResource(OSString.settings_security_section_autodestruction)
    },
)

class PanicWidgetSettingsAction(
    override val onClick: () -> Unit,
    val isWidgetEnabled: Boolean,
    val isPanicModeEnabled: Boolean,
) : CardSettingsNavAction(
    icon = null,
    text = when {
        isWidgetEnabled && isPanicModeEnabled -> LbcTextSpec.StringResource(OSString.settings_panicDestruction_enabled)
        isWidgetEnabled -> LbcTextSpec.StringResource(OSString.settings_panicDestruction_installedAndDisabled)
        else -> LbcTextSpec.StringResource(OSString.settings_panicDestruction_disabled)
    },
)

abstract class ChipCardSettingsAction(
    @DrawableRes icon: Int?,
    text: LbcTextSpec,
    override val state: OSActionState,
    val chipContentDescription: LbcTextSpec,
    val chip: @Composable () -> Unit,
) : CardSettingsNavAction(icon, text) {

    @Composable
    override fun Label(modifier: Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Texts(state = state, modifier = Modifier.weight(1f))
            chip()
        }
    }

    @Composable
    override fun Composable() {
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
        ) {
            super.Composable()
            val context = LocalContext.current
            // Use sibling box to handle interaction so the whole row is clickable, including the chip
            // https://issuetracker.google.com/issues/289087869#comment7
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() }
                    .semantics {
                        text = LbcTextSpec
                            .Raw(
                                "%s.%s",
                                this@ChipCardSettingsAction.text,
                                chipContentDescription,
                            )
                            .annotated(context)
                        role = Role.Button
                        if (state == OSActionState.Disabled) this.disabled()
                        accessibilityClick(label = null) {
                            onClick()
                        }
                    },
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun SimpleSettingsActionComposablePreview() {
    OSPreviewOnSurfaceTheme {
        Column {
            CardSettingsActionClipboardCleanAction(ClipboardClearDelay.THIRTY_SECONDS) {}.Composable()
            CardSettingsActionSynchronizeOption {}.Composable()
            CardSettingsActionOverEncryptionOption({}, false).Composable()
        }
    }
}
