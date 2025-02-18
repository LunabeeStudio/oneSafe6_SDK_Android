package studio.lunabee.onesafe.feature.move.movehost

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.commonui.extension.getNameForBreadcrumb
import studio.lunabee.onesafe.feature.move.MoveDestinationUiData
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSNavigationItem
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import java.util.UUID

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoveBreadcrumb(
    items: List<MoveDestinationUiData>,
    onClickOnDestination: (itemId: UUID?) -> Unit,
    onClickOnCancel: () -> Unit,
    onClickOnMove: () -> Unit,
    moveButtonState: OSActionState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        val lazyListState: LazyListState = rememberLazyListState(items.lastIndex.coerceAtLeast(0))

        if (items.isNotEmpty()) {
            LaunchedEffect(key1 = items.size) {
                lazyListState.scrollToItem(items.lastIndex)
            }
        }
        LazyRow(
            state = lazyListState,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(
                top = OSDimens.SystemSpacing.ExtraSmall,
                end = OSDimens.SystemSpacing.ExtraSmall,
                start = OSDimens.SystemSpacing.ExtraSmall,
            ),
            modifier = Modifier.clearAndSetSemantics {
                this.invisibleToUser()
            },
        ) {
            itemsIndexed(items) { idx, destinationItem ->
                if (idx != 0) {
                    Icon(
                        painter = painterResource(id = OSDrawable.ic_navigate_next),
                        modifier = Modifier.size(OSDimens.Breadcrumb.SeparatorSize),
                        contentDescription = null,
                        tint = LocalDesignSystem.current.navigationItemLabelColor(isActive = false),
                    )
                }

                // If not last item of breadcrumb, crop it to 12 char if more than 14 characters and add ellipsis
                val name = destinationItem.label.getNameForBreadcrumb(idx != items.lastIndex)

                OSNavigationItem(
                    text = name,
                    onClick = { onClickOnDestination(destinationItem.id) },
                    isActive = idx == items.lastIndex,
                )
            }
        }

        OSRegularDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Regular)
                .padding(top = OSDimens.SystemSpacing.Small),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OSDimens.SystemSpacing.Regular),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.ExtraLarge),
            ) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.common_cancel),
                    onClick = onClickOnCancel,
                    buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                )

                val accessibilityLabel = stringResource(
                    OSString.move_selectDestination_accessibility_moveHere,
                    items.lastOrNull()?.label?.string.orEmpty(),
                )
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.move_selectDestination_moveHereButton),
                    onClick = onClickOnMove,
                    state = moveButtonState,
                    modifier = Modifier
                        .testTag(UiConstants.TestTag.Item.MoveHereButton)
                        .then(
                            if (moveButtonState == OSActionState.Disabled) {
                                Modifier.clearAndSetSemantics {
                                    this.invisibleToUser()
                                }
                            } else {
                                Modifier.clearAndSetSemantics {
                                    this.text = AnnotatedString(accessibilityLabel)
                                }
                            },
                        ),
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SelectDestinationPathBarPreview() {
    OSTheme {
        MoveBreadcrumb(
            onClickOnDestination = {},
            onClickOnCancel = {},
            onClickOnMove = {},
            items = listOf(
                MoveDestinationUiData.home(),
                MoveDestinationUiData(UUID.randomUUID(), LbcTextSpec.Raw("Mac")),
                MoveDestinationUiData(UUID.randomUUID(), LbcTextSpec.Raw("Os")),
            ),
            moveButtonState = OSActionState.Enabled,
        )
    }
}
