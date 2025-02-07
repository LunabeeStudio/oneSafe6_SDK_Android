package studio.lunabee.onesafe.feature.autofill.creation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemform.screen.ItemFormRoute

@Composable
fun AfItemCreationRoute(
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val saveMessage = stringResource(id = OSString.autofill_itemSaved_message)
    ItemFormRoute(
        navigateBack = navigateBack,
        navigateToItemDetails = {
            Toast.makeText(context, saveMessage, Toast.LENGTH_LONG).show()
            navigateBack()
        },
        viewModel = hiltViewModel<AfItemCreationViewModel>(),
        screenTitle = LbcTextSpec.StringResource(OSString.safeItemDetail_newItem_title),
    )
}
