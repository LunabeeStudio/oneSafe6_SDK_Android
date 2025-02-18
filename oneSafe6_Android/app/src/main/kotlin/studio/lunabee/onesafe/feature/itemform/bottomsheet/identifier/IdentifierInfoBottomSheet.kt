package studio.lunabee.onesafe.feature.itemform.bottomsheet.identifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifierInfoBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { _, paddingValues ->
        val context = LocalContext.current
        val description = LbcTextSpec.StringResource(id = OSString.itemForm_identifierInfo_description).markdown()
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier
                .fillMaxWidth()
                .padding(OSDimens.SystemSpacing.Regular)
                .testTag(UiConstants.TestTag.BottomSheet.IdentifierInfoBottomSheet)
                .clearAndSetSemantics {
                    this.text = description.annotated(context)
                },
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.itemForm_identifierInfo_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )

            OSText(
                text = description,
            )
        }
    }
}
