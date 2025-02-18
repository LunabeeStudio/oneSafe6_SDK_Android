package studio.lunabee.onesafe.feature.settings.personalization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.ui.res.OSDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconChoiceBottomSheet(
    isVisible: Boolean,
    aliasIconDisplayed: AppIconUi,
    onBottomSheetClosed: () -> Unit,
    onConfirm: (aliasIconToSet: AppIcon) -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = false,
    ) { closeBottomSheet, paddingValues ->
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier.wrapContentHeight(),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.settings_personalization_iconAndName_bottomsheet_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = OSDimens.SystemSpacing.Regular),
                textAlign = TextAlign.Center,
            )
            aliasIconDisplayed.Composable(isSelected = false, modifier = Modifier.fillMaxWidth())
            OSRegularSpacer()
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.settings_personalization_iconAndName_bottomsheet_subtitle),
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
            OSRegularSpacer()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            ) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(id = OSString.common_confirm),
                    onClick = {
                        onConfirm(aliasIconDisplayed.icon)
                        closeBottomSheet()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                )
            }
        }
    }
}
