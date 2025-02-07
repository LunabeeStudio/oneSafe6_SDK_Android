package studio.lunabee.onesafe.feature.home.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.bubbles.ui.composables.ConversationCardContent
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.home.HomeLazyGridItems
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

class HomeConversationSectionData(
    private val conversations: List<BubblesConversationInfo>,
) {
    fun isEmpty(): Boolean = conversations.isEmpty()

    context(LazyGridScope)
    private fun item(onItemClick: (UUID) -> Unit) {
        item(
            key = KeyConversationRow,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
            contentType = ContentTypeConversationContainer,
        ) {
            val pagerState = rememberPagerState { conversations.size }

            val contentPadding = PaddingValues(
                start = if (pagerState.currentPage == 0) {
                    OSDimens.SystemSpacing.Regular
                } else {
                    OSDimens.SystemSpacing.ExtraLarge
                },
                end = if (pagerState.currentPage == conversations.lastIndex) {
                    OSDimens.SystemSpacing.Regular
                } else {
                    OSDimens.SystemSpacing.ExtraLarge
                },
            )

            HorizontalPager(
                state = pagerState,
                pageSpacing = OSDimens.SystemSpacing.Regular,
                contentPadding = contentPadding,
            ) { page ->
                OSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(UiConstants.TestTag.Item.ConversationCard),
                ) {
                    val conversation = conversations[page]
                    ConversationCardContent(conversation, { onItemClick(conversation.id.uuid) })
                        .Content(padding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular), modifier = Modifier)
                }
            }
        }
    }

    context(LazyGridScope)
    fun section(
        navigateToBubblesContacts: () -> Unit,
        navigateToBubblesConversation: (UUID) -> Unit,
    ) {
        if (conversations.isNotEmpty()) {
            val action: @Composable (RowScope.() -> Unit) = {
                OSTextButton(
                    text = LbcTextSpec.StringResource(id = OSString.common_seeAll),
                    onClick = navigateToBubblesContacts,
                    modifier = Modifier
                        .accessibilityInvisibleToUser(),
                )
            }

            HomeLazyGridItems.sectionHeader(
                text = LbcTextSpec.StringResource(OSString.home_section_bubbles_title),
                key = OSString.home_section_bubbles_title,
                action = action,
                accessibilityModifier = Modifier
                    .composed {
                        val clickLabel = stringResource(id = OSString.home_section_bubbles_title_accessibility)
                        semantics(mergeDescendants = true) {
                            accessibilityClick(label = clickLabel, action = navigateToBubblesContacts)
                            heading()
                        }
                    },
            )
            item(onItemClick = navigateToBubblesConversation)
            HomeLazyGridItems.sectionSpacer()
        }
    }
}

private const val KeyConversationRow: String = "KeyConversationRow"
private const val ContentTypeConversationContainer: String = "ContentTypeConversationContainer"

@OsDefaultPreview
@Composable
private fun HomeConversationSectionDataSectionPreview() {
    OSPreviewBackgroundTheme {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular),
            columns = GridCells.Adaptive(minSize = OSSafeItemStyle.Regular.elementSize + OSDimens.SystemSpacing.Regular * 2),
        ) {
            HomeConversationSectionData(
                conversations = listOf(
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = true,
                    ),
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = false,
                    ),
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = false,
                    ),
                ),
            ).section({}, {})
        }
    }
}
