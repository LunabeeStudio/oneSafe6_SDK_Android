package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.model.OSLazyCardContent

context(LazyListScope)
fun osLazyCard(cardContents: List<OSLazyCardContent>) {
    cardContents.forEachIndexed { idx, cardContent ->
        val position = OSLazyCardContent.Position.fromIndex(idx, cardContents.lastIndex)
        when (cardContent) {
            is OSLazyCardContent.Item -> {
                item(
                    key = cardContent.key,
                    contentType = cardContent.contentType,
                ) {
                    OSLazyCard(position = position) { padding ->
                        cardContent.Content(
                            padding = padding,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
            is OSLazyCardContent.Paged -> {
                cardContent.pagedContent(positionInList = position)
            }
        }
    }
}

@Composable
fun OSLazyCard(
    position: OSLazyCardContent.Position,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.(padding: PaddingValues) -> Unit),
) {
    val padding = position.padding()
    val cardContent: @Composable ColumnScope.() -> Unit = { content(padding) }

    when (position) {
        OSLazyCardContent.Position.TOP -> OSTopCard(modifier = modifier.fillMaxWidth(), content = cardContent)
        OSLazyCardContent.Position.MIDDLE -> OSMiddleCard(modifier = modifier.fillMaxWidth(), content = cardContent)
        OSLazyCardContent.Position.BOTTOM -> OSBottomCard(modifier = modifier.fillMaxWidth(), content = cardContent)
        OSLazyCardContent.Position.SINGLE -> OSCard(modifier = modifier.fillMaxWidth(), content = cardContent)
    }
}
