package studio.lunabee.onesafe.feature.itemform.bottomsheet.newfield

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.feature.dialog.FeatureComingDialogState
import studio.lunabee.onesafe.feature.itemform.model.NotImplementedItemFieldType
import studio.lunabee.onesafe.feature.itemform.model.StandardItemFieldType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewFieldBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onNewFieldRequested: (StandardItemFieldType) -> Unit,
) {
    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        ItemFormNewFieldBottomSheetContent(
            paddingValues = paddingValues,
            onNewFieldRequested = { fieldType ->
                when (fieldType) {
                    is StandardItemFieldType -> {
                        onNewFieldRequested(fieldType)
                        closeBottomSheet()
                    }
                    is NotImplementedItemFieldType -> {
                        dialogState = FeatureComingDialogState { dialogState = null }
                    }
                }
            },
        )
    }
}
