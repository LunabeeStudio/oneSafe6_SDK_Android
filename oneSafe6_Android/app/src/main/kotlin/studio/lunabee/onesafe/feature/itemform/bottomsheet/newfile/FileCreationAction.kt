package studio.lunabee.onesafe.feature.itemform.bottomsheet.newfile

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

sealed class FileCreationAction(
    @DrawableRes val iconRes: Int,
    val title: LbcTextSpec,
) {

    abstract val onClick: () -> Unit

    class FromGallery(
        override val onClick: () -> Unit,
    ) : FileCreationAction(
        iconRes = OSDrawable.ic_add_file,
        title = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_bottomsheet_menu_gallery),
    )

    class FromCamera(
        override val onClick: () -> Unit,
    ) : FileCreationAction(
        iconRes = OSDrawable.ic_add_photo,
        title = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_bottomsheet_menu_camera),
    )

    class FromFileExplorer(
        override val onClick: () -> Unit,
    ) : FileCreationAction(
        iconRes = OSDrawable.ic_file,
        title = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_bottomsheet_menu_files),
    )
}
