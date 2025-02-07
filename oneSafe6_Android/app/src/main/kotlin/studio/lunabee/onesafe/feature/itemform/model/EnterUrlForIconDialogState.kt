package studio.lunabee.onesafe.feature.itemform.model

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class EnterUrlForIconDialogState(
    fetchIconFromUrl: (String) -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    private val urlValue: MutableStateFlow<String> = MutableStateFlow("")
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_icon_import_fromUrl_dialog_title)
    override val message: LbcTextSpec? = null
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(LbcTextSpec.StringResource(id = OSString.common_confirm), onClick = { fetchIconFromUrl(urlValue.value) }),
    )
    override val customContent: (@Composable () -> Unit) = {
        val url by urlValue.collectAsStateWithLifecycle()
        OSTextField(
            value = url,
            label = LbcTextSpec.StringResource(OSString.safeItemDetail_icon_import_fromUrl_dialog_label),
            placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_icon_import_fromUrl_dialog_label),
            onValueChange = { urlValue.value = it },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                fetchIconFromUrl(url)
            },
        )
    }
}
