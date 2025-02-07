package studio.lunabee.onesafe.feature.itemactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSLazyCard
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.model.combinedClickableWithHaptic
import studio.lunabee.onesafe.molecule.ItemSubTitleMaxLine
import studio.lunabee.onesafe.molecule.OSItemRow
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSItemRowWithAction(
    osItemIllustration: OSItemIllustration,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    contentDescription: LbcTextSpec? = label,
    onClick: () -> Unit,
    subtitle: LbcTextSpec? = null,
    itemSubtitleMaxLine: Int = ItemSubTitleMaxLine,
    getActions: (suspend () -> List<SafeItemAction>)?,
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
                onClick = onClick,
                onClickLabel = null,
                onLongClickLabel = getActions?.let { stringResource(id = OSString.accessibility_home_longItemClicked, label.string) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        OSItemRow(
            osItemIllustration = osItemIllustration,
            label = label,
            modifier = modifier,
            paddingValues = paddingValues,
            contentDescription = contentDescription,
            subtitle = subtitle,
            itemSubtitleMaxLine = itemSubtitleMaxLine,
        )
        OSSafeItemActionDropdownMenu(
            isMenuExpended = isMenuExpended,
            onDismiss = { isMenuExpended = false },
            actions = actions,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSItemRowWithActionPreview() {
    OSPreviewBackgroundTheme {
        OSLazyCard(position = OSLazyCardContent.Position.SINGLE) { padding ->
            OSItemRowWithAction(
                osItemIllustration = OSItemIllustration.Emoji(LbcTextSpec.Raw("\uD83D\uDC4D"), Color.Blue),
                label = loremIpsumSpec(1),
                modifier = Modifier,
                paddingValues = padding,
                contentDescription = null,
                onClick = {},
                subtitle = loremIpsumSpec(3),
                getActions = { emptyList() },
            )
        }
    }
}
