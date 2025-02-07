package studio.lunabee.onesafe.feature.itemdetails

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.textfield.OSTrailingAction
import studio.lunabee.onesafe.common.extensions.showCopyToast
import studio.lunabee.onesafe.common.extensions.toFontFamily
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntryTextField
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

@Composable
fun ItemDetailsTextInformationRow(
    field: InformationTabEntryTextField,
    navigateToFullScreen: (id: UUID) -> Unit,
    copyText: (label: String, value: String, isSecured: Boolean) -> Unit,
    textMaxLines: Int,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    // Don't use rememberSaveable in order to lose state when navigating.
    var currentValue: LbcTextSpec by remember(field.value) { mutableStateOf(field.value) }
    OSRow(
        text = currentValue,
        modifier = Modifier
            .rowClickable(
                field = field,
                currentValue = currentValue.string,
                isValueVisible = !field.isSecured || currentValue != field.value,
                getDecryptedValue = { field.getDecryptedRawValue() },
                onNoteFieldClick = { navigateToFullScreen(field.id) },
                copyText = copyText,
            )
            .padding(horizontal = OSDimens.SystemSpacing.Regular)
            .then(modifier),
        fontText = field.kind.font.toFontFamily(),
        label = field.label,
        secondaryText = null,
        contentDescription = buildAccessibilityString(
            label = field.label,
            value = currentValue,
            isCurrentValueVisible = !field.isSecured || currentValue != field.value,
        ),
        horizontalArrangement = Arrangement.End,
        textMaxLines = textMaxLines,
    ) {
        if (field.isSecured) {
            VisibilityTrailingAction(
                onClick = {
                    if (currentValue != field.value) {
                        currentValue = field.value
                    } else {
                        coroutineScope.launch {
                            field.getDecryptedDisplayValue()?.let { currentValue = LbcTextSpec.Raw(it) }
                        }
                    }
                },
                isSecuredVisible = currentValue != field.value,
                contentDescription = LbcTextSpec.StringResource(
                    if (currentValue == field.value) {
                        OSString.safeItemDetail_contentCard_informations_accessibility_securedValue_actionShow
                    } else {
                        OSString.safeItemDetail_contentCard_informations_accessibility_securedValue_actionHide
                    },
                ),
            )
        }
        if (field.kind == SafeItemFieldKind.Password) {
            OSTrailingAction(
                image = OSImageSpec.Drawable(OSDrawable.ic_fullscreen),
                onClick = { navigateToFullScreen(field.id) },
                contentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_contentCard_informations_accessibility_fullScreen),
            )
        }
    }
}

@Composable
private fun buildAccessibilityString(
    label: LbcTextSpec?,
    value: LbcTextSpec,
    isCurrentValueVisible: Boolean,
): String {
    val accessibilityFieldText = if (isCurrentValueVisible) {
        stringResource(id = OSString.safeItemDetail_contentCard_informations_accessibility_fieldText, value.string)
    } else {
        stringResource(id = OSString.safeItemDetail_contentCard_informations_accessibility_securedValue)
    }

    return listOfNotNull(
        label?.string?.let {
            stringResource(id = OSString.safeItemDetail_contentCard_informations_accessibility_fieldLabel, it)
        },
        accessibilityFieldText,
    ).joinToString()
}

@Suppress("LongParameterList")
private fun Modifier.rowClickable(
    field: InformationTabEntryTextField,
    currentValue: String,
    isValueVisible: Boolean,
    getDecryptedValue: suspend () -> String?,
    onNoteFieldClick: () -> Unit,
    copyText: (label: String, value: String, isSecured: Boolean) -> Unit,
): Modifier = composed {
    val context = LocalContext.current

    val onClickLabel = if (isValueVisible) {
        stringResource(
            OSString.safeItemDetail_contentCard_informations_accessibility_actionCopy,
            currentValue,
        )
    } else {
        stringResource(
            OSString.safeItemDetail_contentCard_informations_accessibility_actionCopySecured,
            currentValue,
        )
    }

    val fieldToastText = field.label.string
    val copyAndShowToast = { text: String, isSecured: Boolean ->
        copyText(fieldToastText, text, isSecured)
        context.showCopyToast(fieldToastText)
    }

    val onClick = when {
        field.kind == SafeItemFieldKind.Note -> onNoteFieldClick
        field.isSecured && !isValueVisible -> {
            val coroutineScope = rememberCoroutineScope()
            val onClick: () -> Unit = {
                coroutineScope.launch {
                    val text = getDecryptedValue()
                    if (text != null) {
                        copyAndShowToast(text, true)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(OSString.common_copy_error, fieldToastText),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
            onClick
        }
        else -> {
            val coroutineScope = rememberCoroutineScope()
            val onClick: () -> Unit = {
                coroutineScope.launch {
                    copyAndShowToast(getDecryptedValue().orEmpty(), false)
                }
            }
            onClick
        }
    }

    this.clickable(
        onClickLabel = onClickLabel,
        onClick = onClick,
    )
}
