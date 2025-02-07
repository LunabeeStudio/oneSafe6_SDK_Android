package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.itemactions.OSSafeItemWithAction
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.molecule.OSShimmerSafeItem
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID
import kotlin.random.Random

@Composable
fun ItemDetailsChildrenRow(
    children: List<PlainItemData?>,
    onItemClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp,
    elementStyle: OSSafeItemStyle,
) {
    val halfSpacing = elementStyle.spacing / 2
    Box(
        modifier = modifier
            .testTag(UiConstants.TestTag.Item.ItemDetailsChildrenRow)
            .padding(horizontal = halfSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            children.forEach { child ->
                val itemWidth = elementStyle.elementSize + halfSpacing * 2
                if (child == null) {
                    OSShimmerSafeItem(
                        style = elementStyle,
                        modifier = Modifier
                            .requiredWidth(itemWidth)
                            .padding(halfSpacing),
                    )
                } else {
                    key(child.id) {
                        val itemName = child.itemNameProvider.name
                        OSSafeItemWithAction(
                            illustration = child.safeIllustration,
                            style = elementStyle,
                            label = itemName,
                            modifier = Modifier
                                .requiredWidth(itemWidth)
                                .clip(shape = MaterialTheme.shapes.medium),
                            clickLabel = LbcTextSpec.StringResource(
                                id = OSString.accessibility_home_itemClicked,
                                itemName.string,
                            ),
                            onClick = { onItemClick(child.id) },
                            paddingValues = PaddingValues(halfSpacing),
                            getActions = child.actions,
                        )
                    }
                }
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun ItemDetailsChildrenRowPreview() {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val halfSpacing = OSDimens.SystemSpacing.Regular / 2
    val cardContentWidth = remember(screenWidth) { screenWidth - OSDimens.SystemSpacing.Regular * 2 - halfSpacing * 2 }
    val elementWidth = OSSafeItemStyle.Regular.elementSize + halfSpacing * 2
    val childPerRow: Int = remember(cardContentWidth) { ((cardContentWidth + 24.dp) / (elementWidth + 24.dp)).toInt() }
    val allElementsWidth = elementWidth * childPerRow
    val spacing = (cardContentWidth - allElementsWidth) / (childPerRow - 1)

    OSTheme {
        ItemDetailsChildrenRow(
            children = listOf(
                PlainItemDataDefault(
                    id = UUID.randomUUID(),
                    itemNameProvider = DefaultNameProvider(loremIpsum(Random.nextInt(1, 4))),
                    icon = iconSample,
                    color = null,
                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                ),
                PlainItemDataDefault(
                    id = UUID.randomUUID(),
                    itemNameProvider = DefaultNameProvider(loremIpsum(Random.nextInt(1, 4))),
                    icon = iconSample,
                    color = randomColor,
                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                ),
                PlainItemDataDefault(
                    id = UUID.randomUUID(),
                    itemNameProvider = DefaultNameProvider(loremIpsum(Random.nextInt(1, 4))),
                    icon = null,
                    color = randomColor,
                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                ),
                PlainItemDataDefault(
                    id = UUID.randomUUID(),
                    itemNameProvider = DefaultNameProvider(loremIpsum(Random.nextInt(1, 4))),
                    icon = null,
                    color = randomColor,
                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                ),
                PlainItemDataDefault(
                    id = UUID.randomUUID(),
                    itemNameProvider = DefaultNameProvider(loremIpsum(Random.nextInt(1, 4))),
                    icon = null,
                    color = randomColor,
                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                ),
            ),
            onItemClick = {},
            spacing = spacing,
            elementStyle = OSSafeItemStyle.Regular,
        )
    }
}
