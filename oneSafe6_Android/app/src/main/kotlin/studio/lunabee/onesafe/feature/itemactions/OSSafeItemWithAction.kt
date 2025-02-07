package studio.lunabee.onesafe.feature.itemactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSHapticEffect
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.model.combinedClickableWithHaptic
import studio.lunabee.onesafe.molecule.OSSafeItem

@Composable
fun OSSafeItemWithAction(
    illustration: OSItemIllustration,
    style: OSSafeItemStyle,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    contentDescription: LbcTextSpec? = label,
    labelMinLines: Int = 1,
    getActions: (suspend () -> List<SafeItemAction>)?,
    clickLabel: LbcTextSpec,
    onClick: () -> Unit,
    paddingValues: PaddingValues,
) {
    var isMenuExpended: Boolean by remember { mutableStateOf(false) }
    var actions: List<SafeItemAction> by remember { mutableStateOf(listOf()) }
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .combinedClickableWithHaptic(
                onLongClick = getActions?.let {
                    {
                        coroutineScope.launch {
                            actions = getActions()
                            isMenuExpended = true
                        }
                    }
                },
                osClickHapticEffect = OSHapticEffect.Primary,
                onClick = onClick,
                onClickLabel = clickLabel.string,
                onLongClickLabel = getActions?.let { stringResource(id = OSString.accessibility_home_longItemClicked, label.string) },
            )
            .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        OSSafeItem(
            illustration = illustration,
            style = style,
            label = label,
            contentDescription = contentDescription,
            labelMinLines = labelMinLines,
        )
        OSSafeItemActionDropdownMenu(
            isMenuExpended = isMenuExpended,
            onDismiss = { isMenuExpended = false },
            actions = actions,
        )
    }
}
