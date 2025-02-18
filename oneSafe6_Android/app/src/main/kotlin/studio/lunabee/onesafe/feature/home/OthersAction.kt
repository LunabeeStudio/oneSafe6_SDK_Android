package studio.lunabee.onesafe.feature.home

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

sealed class OthersAction(
    @DrawableRes val icon: Int,
    val text: LbcTextSpec,
    val clickLabel: LbcTextSpec? = null,
    val count: Int? = null,
) {
    abstract val onClick: () -> Unit

    class Bin(deletedItemCount: Int?, override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_delete,
        text = LbcTextSpec.StringResource(OSString.common_bin),
        count = deletedItemCount,
    )

    class Settings(override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_settings,
        text = LbcTextSpec.StringResource(OSString.home_settings_title),
    )

    class VerifyPassword(override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_security,
        text = LbcTextSpec.StringResource(OSString.home_verifyPassword_title),
    )

    class HelpTranslate(override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_language,
        text = LbcTextSpec.StringResource(OSString.home_translate_menu_title),
    )

    class Community(override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_discord,
        text = LbcTextSpec.StringResource(OSString.home_community_title),
        clickLabel = LbcTextSpec.StringResource(OSString.accessibility_home_community),
    )

    class Lock(override val onClick: () -> Unit) : OthersAction(
        icon = OSDrawable.ic_lock,
        text = LbcTextSpec.StringResource(OSString.settings_section_onesafe_lock),
        clickLabel = LbcTextSpec.StringResource(OSString.settings_section_onesafe_lock),
    )
}
