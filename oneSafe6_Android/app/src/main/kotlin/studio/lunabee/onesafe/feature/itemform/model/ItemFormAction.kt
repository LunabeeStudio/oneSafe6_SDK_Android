package studio.lunabee.onesafe.feature.itemform.model

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

abstract class ItemFormAction(
    @DrawableRes val icon: Int,
    val text: LbcTextSpec,
) {
    abstract val onClick: () -> Unit

    class AddNewField(override val onClick: () -> Unit) : ItemFormAction(
        OSDrawable.ic_add,
        LbcTextSpec.StringResource(OSString.safeItemDetail_addField_buttonTitle),
    )

    class AddNewFile(override val onClick: () -> Unit) : ItemFormAction(
        OSDrawable.ic_add_file,
        LbcTextSpec.StringResource(OSString.safeItemDetail_addFile_buttonTitle),
    )

    class SaveForm(override val onClick: () -> Unit) : ItemFormAction(
        OSDrawable.ic_done,
        LbcTextSpec.StringResource(OSString.common_save),
    )
}

class ItemFormActionsHolder(
    val addNewField: ItemFormAction.AddNewField,
    val addNewFile: ItemFormAction.AddNewFile,
    val saveForm: ItemFormAction.SaveForm,
)
