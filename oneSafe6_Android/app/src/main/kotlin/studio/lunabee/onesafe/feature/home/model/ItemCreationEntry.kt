package studio.lunabee.onesafe.feature.home.model

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.chip.ComingInputChip
import studio.lunabee.onesafe.commonui.chip.NewInputChip
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSActionButton
import studio.lunabee.onesafe.molecule.OSActionButtonStyle

sealed class ItemCreationEntry(
    @get:DrawableRes val iconRes: Int,
    val text: LbcTextSpec,
    val id: String,
    val state: ItemCreationSortState,
    val clickLabel: LbcTextSpec,
    val contentType: String,
) {

    enum class ItemCreationSortState(
        val isEnabled: Boolean,
        val chip: @Composable (() -> Unit)?,
        val contentDescription: LbcTextSpec?,
    ) {
        Normal(
            isEnabled = true,
            chip = null,
            contentDescription = null,
        ),
        ComingSoon(
            isEnabled = false,
            chip = { ComingInputChip() },
            contentDescription = LbcTextSpec.StringResource(OSString.common_coming),
        ),
        New(
            isEnabled = true,
            chip = { NewInputChip() },
            contentDescription = LbcTextSpec.StringResource(OSString.common_new),
        ),
    }

    fun actionButton(
        contentPadding: PaddingValues,
        onClick: () -> Unit,
    ): OSActionButton {
        return OSActionButton(
            text = text,
            onClick = onClick,
            contentPadding = contentPadding,
            style = OSActionButtonStyle.Default,
            state = if (state.isEnabled) OSActionState.Enabled else OSActionState.DisabledWithAction,
            startIcon = OSImageSpec.Drawable(iconRes),
            chip = state.chip,
            contentDescription = state.contentDescription,
            clickLabel = clickLabel,
        )
    }
}

sealed class ItemCreationEntryFile(
    @DrawableRes iconRes: Int,
    text: LbcTextSpec,
    state: ItemCreationSortState,
    id: String,
) : ItemCreationEntry(
    iconRes,
    text,
    id,
    state,
    text,
    "ItemCreationSortFile",
) {
    data object Gallery : ItemCreationEntryFile(
        iconRes = OSDrawable.ic_add_file,
        text = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_menu_photoLibrary),
        id = "Gallery",
        state = ItemCreationSortState.Normal,
    )

    data object FileExplorer : ItemCreationEntryFile(
        iconRes = OSDrawable.ic_file,
        text = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_menu_files),
        id = "FileExplorer",
        state = ItemCreationSortState.Normal,
    )

    data object Camera : ItemCreationEntryFile(
        iconRes = OSDrawable.ic_from_camera,
        text = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_menu_camera),
        id = "Camera",
        state = ItemCreationSortState.Normal,
    )
}

sealed class ItemCreationEntryWithTemplate(
    val template: Template,
    @DrawableRes iconRes: Int,
    text: LbcTextSpec,
    state: ItemCreationSortState,
) : ItemCreationEntry(
    iconRes = iconRes,
    text = text,
    id = template.name,
    state = state,
    clickLabel = LbcTextSpec.StringResource(id = OSString.createItem_template_accessibility_button, text),
    contentType = "ItemCreationSortTemplate",
) {
    data object CreditCard : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_credit_card,
        text = LbcTextSpec.StringResource(OSString.createItem_template_creditCardElement),
        template = Template.CreditCard,
        state = ItemCreationSortState.Normal,
    )

    data class WebsiteFromClipboard(val clipboardContent: String) : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_link,
        text = LbcTextSpec.StringResource(OSString.createItem_template_fromClipboard, clipboardContent),
        template = Template.Website,
        state = ItemCreationSortState.Normal,
    )

    data object Website : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_web,
        text = LbcTextSpec.StringResource(OSString.createItem_template_websiteElement),
        template = Template.Website,
        state = ItemCreationSortState.Normal,
    )

    data object Application : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_apps,
        text = LbcTextSpec.StringResource(OSString.createItem_template_applicationElement),
        template = Template.Application,
        state = ItemCreationSortState.Normal,
    )

    data object Folder : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_folder,
        text = LbcTextSpec.StringResource(OSString.createItem_template_folderElement),
        template = Template.Folder,
        state = ItemCreationSortState.Normal,
    )

    data object Note : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_note,
        text = LbcTextSpec.StringResource(OSString.createItem_template_noteElement),
        template = Template.Note,
        state = ItemCreationSortState.Normal,
    )

    data object Custom : ItemCreationEntryWithTemplate(
        iconRes = OSDrawable.ic_edit,
        text = LbcTextSpec.StringResource(OSString.createItem_template_customElement),
        template = Template.Custom,
        state = ItemCreationSortState.Normal,
    )

    enum class Template {
        CreditCard,
        Website,
        Application,
        Folder,
        Note,
        Custom,
    }
}
