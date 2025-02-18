package studio.lunabee.onesafe.feature.itemform.model.option.text

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.textfield.OSTrailingAction
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator.PasswordGeneratorBottomSheet
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField
import studio.lunabee.onesafe.ui.UiConstants

class GeneratePasswordOption(
    private val field: PasswordTextUiField,
    override val clickLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItem_form_accessibility_generatePassword),
) : UiFieldOption {
    private var isBottomSheetVisible: Boolean by mutableStateOf(false)
    override fun onClick() {
        isBottomSheetVisible = true
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ComposableLayout(modifier: Modifier) {
        OSTrailingAction(
            image = OSImageSpec.Drawable(
                drawable = OSDrawable.ic_key,
                tintColor = MaterialTheme.colorScheme.primary,
            ),
            onClick = ::onClick,
            contentDescription = LbcTextSpec.StringResource(OSString.safeItem_form_accessibility_generatePassword),
            testTag = UiConstants.TestTag.Item.GeneratePasswordAction,
            modifier = modifier,
        )

        BottomSheetHolder(
            isVisible = isBottomSheetVisible,
            onBottomSheetClosed = { isBottomSheetVisible = false },
            skipPartiallyExpanded = true,
        ) { closeBottomSheet, paddingValues ->
            PasswordGeneratorBottomSheet(
                paddingValues = paddingValues,
                onPasswordGenerated = {
                    field.onValueChanged(it.value)
                    closeBottomSheet()
                },
                onCancel = closeBottomSheet,
                isOverriding = field.getDisplayedValue().isNotEmpty(),
            )
        }
    }
}
