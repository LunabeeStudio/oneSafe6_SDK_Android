package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemform.model.ItemFormAction
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun ItemFormActionList(
    itemFormActionList: List<ItemFormAction>,
) {
    val context = LocalContext.current
    OSCard(
        modifier = Modifier
            .padding(OSDimens.SystemSpacing.Regular)
            .semantics {
                heading()
                text = AnnotatedString(context.getString(OSString.itemForm_actionCard_accessibility))
            },
    ) {
        itemFormActionList.forEachIndexed { index, itemFormAction ->
            OSClickableRow(
                text = itemFormAction.text,
                onClick = itemFormAction.onClick,
                leadingIcon = {
                    OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = itemFormAction.icon))
                },
                contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                    index = index,
                    elementsCount = itemFormActionList.size,
                ),
            )
        }
    }
}
