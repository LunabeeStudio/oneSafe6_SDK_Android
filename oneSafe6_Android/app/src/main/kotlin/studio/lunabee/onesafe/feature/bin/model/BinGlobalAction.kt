package studio.lunabee.onesafe.feature.bin.model

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

sealed class BinGlobalAction(@DrawableRes val icon: Int, val text: LbcTextSpec) {
    abstract val onClick: () -> Unit

    class RemoveAll(override val onClick: () -> Unit) : BinGlobalAction(
        OSDrawable.ic_delete,
        LbcTextSpec.StringResource(OSString.bin_topBar_menu_removeAll),
    )

    class RestoreAll(override val onClick: () -> Unit) : BinGlobalAction(
        OSDrawable.ic_restore,
        LbcTextSpec.StringResource(OSString.bin_topBar_menu_restoreAll),
    )
}
