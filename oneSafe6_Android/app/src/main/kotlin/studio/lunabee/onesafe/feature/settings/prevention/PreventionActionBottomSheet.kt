package studio.lunabee.onesafe.feature.settings.prevention

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSClickableRowText
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventionActionBottomSheet(
    actions: List<UiPreventionAction>,
    onBottomSheetClosed: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = actions.isNotEmpty(),
        onBottomSheetClosed = onBottomSheetClosed,
        bottomOverlayBrush = LocalDesignSystem.current.navBarOverlayBackgroundGradientBrush,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        val context = LocalContext.current
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier.wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier,
            ) {
                actions.forEachIndexed { index, action ->
                    OSClickableRow(
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = action.title.string(context)
                            },
                        onClick = {
                            closeBottomSheet()
                            action.onClick()
                        },
                        label = { modifier ->
                            OSClickableRowText(text = action.title, modifier = modifier)
                        },
                        contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                            index = index,
                            elementsCount = actions.size,
                        ),
                    )
                }
            }
        }
    }
}
