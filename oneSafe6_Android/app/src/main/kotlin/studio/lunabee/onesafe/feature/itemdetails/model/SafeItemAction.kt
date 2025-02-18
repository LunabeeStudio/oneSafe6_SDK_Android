package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.molecule.OSActionButton
import studio.lunabee.onesafe.molecule.OSActionButtonStyle

sealed class SafeItemAction(@DrawableRes val icon: Int, val text: LbcTextSpec, val type: Type) {

    abstract val onClick: () -> Unit

    fun actionButton(
        contentPadding: PaddingValues,
    ): OSActionButton {
        return OSActionButton(
            text = text,
            onClick = onClick,
            contentPadding = contentPadding,
            style = when (type) {
                Type.Normal -> OSActionButtonStyle.Default
                Type.Dangerous -> OSActionButtonStyle.Destructive
            },
            startIcon = OSImageSpec.Drawable(drawable = icon),
        )
    }

    class AddToFavorites(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_like,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_addToFavorites),
        Type.Normal,
    )

    class RemoveFromFavorites(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_like_filled,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_removeFromFavorites),
        Type.Normal,
    )

    class Share(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_share,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_share),
        Type.Normal,
    )

    class SendViaBubbles(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_key,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_sendViaBubbles),
        Type.Normal,
    )

    class Move(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_drive_file_move,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_move),
        Type.Normal,
    )

    class Duplicate(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_content_copy,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_duplicate),
        Type.Normal,
    )

    class Restore(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_restore,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_restore),
        Type.Normal,
    )

    class Delete(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_delete,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_delete),
        Type.Dangerous,
    )

    class Remove(override val onClick: () -> Unit) : SafeItemAction(
        OSDrawable.ic_delete,
        LbcTextSpec.StringResource(OSString.safeItemDetail_actionCard_remove),
        Type.Dangerous,
    )

    enum class Type {
        Normal, Dangerous
    }
}
