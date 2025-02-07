package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

sealed class FileFieldAction(
    @DrawableRes val icon: Int,
    val text: LbcTextSpec,
) {

    abstract val onClick: () -> Unit

    class Share(override val onClick: () -> Unit) : FileFieldAction(
        icon = OSDrawable.ic_share,
        text = LbcTextSpec.StringResource(OSString.common_share),
    )

    class Download(override val onClick: () -> Unit) : FileFieldAction(
        icon = OSDrawable.ic_save,
        text = LbcTextSpec.StringResource(OSString.common_save),
    )
}
