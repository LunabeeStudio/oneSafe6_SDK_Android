package studio.lunabee.onesafe.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.molecule.OSSwitch
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverSettingsBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onCreate: (prefill: Boolean, tutorial: Boolean) -> Unit,
) {
    var isPrefillEnabled by remember { mutableStateOf(true) }
    var isTutorialEnabled by remember { mutableStateOf(true) }
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier.wrapContentHeight(),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
            )
            OptionSwitch(
                title = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_prefill_title),
                description = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_prefill_description),
                icon = OSImageSpec.Drawable(OSDrawable.ic_magic),
                isEnabled = isPrefillEnabled,
                toggle = { isPrefillEnabled = it },
                index = 0,
            )
            OptionSwitch(
                title = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_items_title),
                description = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_items_description),
                icon = OSImageSpec.Drawable(OSDrawable.ic_school_hat),
                isEnabled = isTutorialEnabled,
                toggle = { isTutorialEnabled = it },
                index = 1,
            )
            OSRegularDivider()
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OSDimens.SystemSpacing.Regular),
            ) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.common_cancel),
                    onClick = closeBottomSheet,
                    buttonColors = OSFilledButtonDefaults.secondaryButtonColors(state = OSActionState.Enabled),
                )
                OSRegularSpacer()
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.home_discoverBottomSheet_create),
                    onClick = {
                        onCreate(isPrefillEnabled, isTutorialEnabled)
                        closeBottomSheet()
                    },
                    state = if (isPrefillEnabled || isTutorialEnabled) OSActionState.Enabled else OSActionState.Disabled,
                )
            }
        }
    }
}

@Composable
private fun OptionSwitch(
    title: LbcTextSpec,
    description: LbcTextSpec,
    icon: OSImageSpec,
    isEnabled: Boolean,
    toggle: (Boolean) -> Unit,
    index: Int,
) {
    val context = LocalContext.current
    OSSwitch(
        checked = isEnabled,
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .toggleable(
                value = isEnabled,
                onValueChange = toggle,
                role = Role.Switch,
                enabled = true,
            )
            .clearAndSetSemantics {
                text = description.annotated(context)
            }
            .padding(
                LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                    index = index,
                    elementsCount = 2,
                ),
            ),
        enabled = true,
    ) {
        OSRow(
            text = title,
            secondaryText = description,
            textMaxLines = Int.MAX_VALUE,
            startContent = {
                OSImage(
                    image = icon,
                    modifier = Modifier.size(OSDimens.SystemButton.ExtraSmall),
                )
            },
            modifier = Modifier.weight(1f),
        )
    }
}
