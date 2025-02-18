package studio.lunabee.onesafe.organism.card

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSCardTitle
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.organism.card.component.OSCardAction
import studio.lunabee.onesafe.organism.card.scope.OSCardActionScope

@Composable
fun OSCustomCard(
    content: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    title: LbcTextSpec? = null,
    actionAlignment: Alignment = Alignment.Center,
    action: @Composable (OSCardActionScope.(padding: PaddingValues) -> Unit)? = null,
) {
    val titleSlot: @Composable ColumnScope.() -> Unit =
        {
            title?.let {
                OSCardTitle(title = title)
                OSRegularSpacer()
            }
        }
    OSCustomCard(
        content = content,
        modifier = modifier,
        titleSlot = titleSlot,
        actionAlignment = actionAlignment,
        action = action,
    )
}

@Composable
fun OSCustomCard(
    content: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    titleSlot: @Composable (ColumnScope.() -> Unit) = {},
    actionAlignment: Alignment = Alignment.Center,
    action: @Composable (OSCardActionScope.(padding: PaddingValues) -> Unit)? = null,
) {
    OSCard(
        modifier = modifier,
    ) {
        OSRegularSpacer()
        titleSlot()
        content()
        if (action != null) {
            OSCardAction(
                contentAlignment = actionAlignment,
                action = action,
            )
        } else {
            OSRegularSpacer()
        }
    }
}
