package studio.lunabee.onesafe.molecule.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSTabs(
    data: List<TabsData>,
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    onTabSelected: ((Int) -> Unit)?,
) {
    val isEnabled = onTabSelected != null

    Box(
        modifier = modifier
            .wrapContentSize(),
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .wrapContentWidth()
                .testTag(UiConstants.TestTag.OSTabs),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = @Composable { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .animateContentSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = OSDimens.TabBar.IndicatorRadius,
                                topEnd = OSDimens.TabBar.IndicatorRadius,
                            ),
                        ),
                    color = if (isEnabled) {
                        // FIXME https://issuetracker.google.com/issues/244483995 to handle Color.Unspecified
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalDesignSystem.current.tabPrimaryDisabledColor
                    },
                )
            },
            divider = { },
        ) {
            data.forEachIndexed { idx, item ->
                Tab(
                    selected = idx == selectedTabIndex,
                    onClick = { onTabSelected?.invoke(idx) },
                    modifier = Modifier
                        .testTag(UiConstants.TestTag.tab(idx)),
                    unselectedContentColor = LocalColorPalette.current.Neutral60,
                    enabled = isEnabled,
                ) {
                    item.Composable(index = idx, selectedTabIndex = selectedTabIndex, isEnabled = isEnabled)
                }
            }
        }

        OSRegularDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
@OsDefaultPreview
private fun OSTabRowPreview() {
    OSPreviewOnSurfaceTheme {
        OSTabs(
            data = listOf(
                TabsData(
                    title = LbcTextSpec.Raw("1"),
                    contentDescription = null,
                ),
                TabsData(
                    title = LbcTextSpec.Raw("22"),
                    contentDescription = null,
                ),
                TabsData(
                    title = LbcTextSpec.Raw("333"),
                    contentDescription = null,
                ),
            ),
            selectedTabIndex = 1,
        ) {}
    }
}

@Composable
@OsDefaultPreview
private fun OSTabRowDisabledPreview() {
    OSPreviewOnSurfaceTheme {
        OSTabs(
            data = listOf(
                TabsData(
                    title = LbcTextSpec.Raw("1"),
                    contentDescription = null,
                ),
                TabsData(
                    title = LbcTextSpec.Raw("22"),
                    contentDescription = null,
                ),
                TabsData(
                    title = LbcTextSpec.Raw("333"),
                    contentDescription = null,
                ),
            ),
            selectedTabIndex = 1,
            onTabSelected = null,
        )
    }
}
