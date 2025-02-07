package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun ItemFormTopAppBar(
    title: LbcTextSpec,
    enabledSaveContentDescription: LbcTextSpec,
    disabledSaveContentDescription: LbcTextSpec,
    saveClickLabel: LbcTextSpec,
    saveButtonState: OSActionState,
    navigateBack: () -> Unit,
    validateForm: () -> Unit,
    addField: (() -> Unit)?,
    addClickLabel: LbcTextSpec?,
) {
    OSTopAppBar(
        title = title,
        options = listOfNotNull(
            topAppBarOptionNavBack(
                navigateBack = navigateBack,
                image = if (saveButtonState == OSActionState.Enabled) {
                    OSImageSpec.Drawable(OSDrawable.ic_close)
                } else {
                    OSImageSpec.Drawable(OSDrawable.ic_back)
                },
            ),
            addField?.let {
                TopAppBarOptionTrailing.secondaryIconAction(
                    image = OSImageSpec.Drawable(OSDrawable.ic_add),
                    contentDescription = addClickLabel,
                    onClick = addField,
                )
            },
            TopAppBarOptionTrailing.primaryIconAction(
                image = OSImageSpec.Drawable(OSDrawable.ic_done),
                contentDescription = if (saveButtonState == OSActionState.Enabled) {
                    enabledSaveContentDescription
                } else {
                    disabledSaveContentDescription
                },
                onClick = validateForm,
                state = saveButtonState,
                modifier = Modifier.clickable(onClickLabel = saveClickLabel.string, onClick = validateForm),
                tag = UiConstants.TestTag.Item.SaveAction,
            ),
        ),
    )
}
