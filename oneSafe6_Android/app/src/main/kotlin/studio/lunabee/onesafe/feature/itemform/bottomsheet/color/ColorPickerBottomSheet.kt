package studio.lunabee.onesafe.feature.itemform.bottomsheet.color

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.common.composable.ColorPickerBottomSheetContent
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    isVisible: Boolean,
    currentThemeColor: Color,
    onValidate: () -> Unit,
    setColorSelectedByUser: (Color?) -> Unit,
    onBottomSheetClosed: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
    ) { closeBottomSheet, paddingValues ->
        ColorPickerBottomSheetContent(
            paddingValues = paddingValues,
            currentSelectedColor = currentThemeColor,
            onColorPicked = setColorSelectedByUser,
            onValidate = {
                onValidate()
                closeBottomSheet()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = OSDimens.SystemSpacing.Regular)
                .testTag(tag = UiConstants.TestTag.BottomSheet.ColorPickerBottomSheet),
        )
    }
}
