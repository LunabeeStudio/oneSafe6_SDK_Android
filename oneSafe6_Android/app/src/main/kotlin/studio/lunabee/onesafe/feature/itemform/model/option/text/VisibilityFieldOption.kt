package studio.lunabee.onesafe.feature.itemform.model.option.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField

class VisibilityFieldOption(
    private val field: PasswordTextUiField,
) : UiFieldOption {

    override fun onClick() {
        field.isValueHidden = !field.isValueHidden
    }

    override val clickLabel: LbcTextSpec? by derivedStateOf {
        LbcTextSpec.StringResource(
            if (field.isValueHidden) {
                OSString.common_accessibility_showPassword
            } else {
                OSString.common_accessibility_hidePassword
            },
        )
    }

    @Composable
    override fun ComposableLayout(modifier: Modifier) {
        VisibilityTrailingAction(
            isSecuredVisible = !field.isValueHidden,
            onClick = ::onClick,
            contentDescription = null,
            tintColor = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
    }
}
