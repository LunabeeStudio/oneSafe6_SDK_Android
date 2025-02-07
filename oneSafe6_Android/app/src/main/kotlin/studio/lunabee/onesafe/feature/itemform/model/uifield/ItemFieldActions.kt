package studio.lunabee.onesafe.feature.itemform.model.uifield

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

sealed class ItemFieldActions(
    @DrawableRes val icon: Int,
    val text: LbcTextSpec,
    val type: Type = Type.Normal,
) {
    abstract val onClick: () -> Unit

    class UseAsIdentifier(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_crown,
        LbcTextSpec.StringResource(OSString.itemForm_action_useAsIdentifier),
    )

    class DeleteField(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_delete,
        LbcTextSpec.StringResource(OSString.common_delete),
        Type.Dangerous,
    )

    class RemoveIdentifier(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_crown,
        LbcTextSpec.StringResource(OSString.itemForm_action_dontUseAsIdentifier),
    )

    class RenameLabel(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_edit,
        LbcTextSpec.StringResource(OSString.itemForm_action_rename),
    )

    class RenameFile(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_edit,
        LbcTextSpec.StringResource(OSString.itemForm_action_renameFile),
    )

    class UseAsIcon(override val onClick: () -> Unit) : ItemFieldActions(
        OSDrawable.ic_add_file,
        LbcTextSpec.StringResource(OSString.itemForm_action_useAsItemIcon),
    )

    enum class Type {
        Normal, Dangerous
    }
}
