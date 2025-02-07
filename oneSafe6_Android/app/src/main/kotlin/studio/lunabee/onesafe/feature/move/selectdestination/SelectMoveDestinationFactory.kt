package studio.lunabee.onesafe.feature.move.selectdestination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.molecule.OSLargeItemTitle
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens

object SelectMoveDestinationFactory {

    fun addHomeMessageCard(
        lazyGridScope: LazyGridScope,
        itemName: String,
    ) {
        lazyGridScope.item(
            key = KeyHomeMessageCard,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
        ) {
            OSMessageCard(
                description = LbcTextSpec.Annotated(
                    stringResource(
                        id = OSString.move_selectDestination_homeMessageCard,
                        itemName,
                    ).markdownToAnnotatedString(),
                ),
                modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
            )
        }
    }

    fun addHomeAccessibilityLabel(
        lazyGridScope: LazyGridScope,
        accessibilityAction: () -> Unit,
    ) {
        lazyGridScope.item(
            key = KeyHomeAccessibilityLabel,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
        ) {
            val clickLabel = stringResource(
                id = OSString.move_selectDestination_accessibility_moveHere,
                stringResource(id = OSString.common_home),
            )
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.common_home),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clearAndSetSemantics {
                        this.onClick(
                            label = clickLabel,
                            action = {
                                accessibilityAction()
                                true
                            },
                        )
                    }
                    .padding(OSDimens.SystemSpacing.Regular),
            )
        }
    }

    fun addItemDestinationTitle(
        lazyGridScope: LazyGridScope,
        currentDestination: MoveCurrentDestination,
        accessibilityAction: () -> Unit,
    ) {
        lazyGridScope.item(
            key = KeyDestinationTitle,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(bottom = OSDimens.SystemSpacing.Regular),
            ) {
                val clickLabel = stringResource(
                    id = OSString.move_selectDestination_accessibility_moveHere,
                    currentDestination.itemNameProvider.name.string,
                )
                OSLargeItemTitle(
                    title = currentDestination.itemNameProvider.name,
                    icon = currentDestination.itemIcon,
                    modifier = Modifier.clearAndSetSemantics {
                        this.onClick(
                            label = clickLabel,
                            action = {
                                accessibilityAction()
                                true
                            },
                        )
                    },
                )
            }
        }
    }

    fun addNoItemCard(
        lazyGridScope: LazyGridScope,
    ) {
        lazyGridScope.item(
            key = KeyNoSubItemCard,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
        ) {
            OSMessageCard(
                description = LbcTextSpec.StringResource(OSString.move_selectDestination_noSubItemLabel),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
        }
    }

    private const val KeyDestinationTitle: String = "KeyDestinationTitle"
    private const val KeyNoSubItemCard: String = "KeyNoSubItemCard"
    private const val KeyHomeMessageCard: String = "KeyHomeMessageCard"
    private const val KeyHomeAccessibilityLabel: String = "KeyHomeAccessibilityLabel"
}
