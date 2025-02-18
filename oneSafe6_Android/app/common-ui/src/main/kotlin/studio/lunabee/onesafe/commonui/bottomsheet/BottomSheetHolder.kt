package studio.lunabee.onesafe.commonui.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bottomsheet.OSModalBottomSheet
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import kotlin.math.ceil
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun rememberBottomSheetDimens(): BottomSheetDimens {
    val innerScreenHeight = LocalConfiguration.current.screenHeightDp.dp
    val windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical).asPaddingValues()

    // FIXME workaround https://issuetracker.google.com/issues/323792322
    val topPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    return remember(windowInsets, topPadding) {
        val bottomSystemHeight: Dp = windowInsets.calculateBottomPadding()
        val availableSheetHeight: Dp = innerScreenHeight + bottomSystemHeight - OSDimens.SystemSpacing.Small
        val topSystemHeight: Dp = windowInsets.calculateTopPadding()
        val screenHeight = innerScreenHeight + bottomSystemHeight + topSystemHeight
        BottomSheetDimens(
            availableSheetHeight = availableSheetHeight,
            screenHeight = screenHeight,
            padding = PaddingValues(top = topPadding, bottom = bottomSystemHeight),
        )
    }
}

/**
 * Dimension holder for [BottomSheetHolder]
 *
 * @property availableSheetHeight max height of the bottom sheet content
 * @property screenHeight fullscreen height
 * @property padding Vertical padding values to apply to the content
 */
private class BottomSheetDimens(
    val availableSheetHeight: Dp,
    val screenHeight: Dp,
    val padding: PaddingValues,
)

/**
 * @param bottomOverlayBrush Brush applied behind the bottom system bar (i.e the navigation bar) when the content is taller than the screen
 * @param skipPartiallyExpanded Passthrough to [rememberModalBottomSheetState] skipPartiallyExpanded arg
 * @param snackbarHost component to host above content composable like snackbar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetHolder(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    bottomOverlayBrush: Brush = LocalDesignSystem.current.navBarOverlayBrush,
    skipPartiallyExpanded: Boolean = false,
    fullScreen: Boolean = false,
    snackbarHost: @Composable context(BoxScope, BottomSheetHolderScope) (sheetState: SheetState) -> Unit = { },
    content: @Composable (closeBottomSheet: () -> Unit, PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    val closeBottomSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onBottomSheetClosed()
        }
    }
    if (isVisible) {
        val focusManager = LocalFocusManager.current
        val density = LocalDensity.current
        val sheetDimens = rememberBottomSheetDimens()

        focusManager.clearFocus(true)
        OSModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onBottomSheetClosed,
        ) {
            with(BottomSheetHolderScopeInstance) {
                var boxHeight by remember { mutableStateOf(0.dp) }
                Box(
                    Modifier
                        .heightIn(max = if (fullScreen) sheetDimens.screenHeight else sheetDimens.availableSheetHeight)
                        .onSizeChanged { boxHeight = with(density) { it.height.toDp() } }
                        .fillMaxWidth(),
                ) {
                    snackbarHost(
                        this@OSModalBottomSheet,
                        this@with,
                        sheetState,
                    )
                    content(
                        closeBottomSheet,
                        sheetDimens.padding,
                    )

                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset {
                                val sheetOffset = runCatching { sheetState.requireOffset() }.getOrDefault(0f)
                                val navScrimOffset = with(density) {
                                    val bottomPx = sheetDimens.padding
                                        .calculateBottomPadding()
                                        .toPx()
                                    sheetDimens.screenHeight.toPx() - sheetOffset - bottomPx
                                }
                                IntOffset(x = 0, y = ceil(navScrimOffset).toInt())
                            }
                            .height(sheetDimens.padding.calculateBottomPadding() + 1.dp) // It looks like we lost ~1dp somewhere
                            .fillMaxWidth()
                            .zIndex(1f),
                    ) {
                        if (boxHeight >= sheetDimens.availableSheetHeight - sheetDimens.padding.calculateBottomPadding()) {
                            drawRect(
                                brush = bottomOverlayBrush,
                                size = this.size,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Column container to be use as the root content of [BottomSheetHolder]. Provides vertical scroll and handle the bottom spacer to avoid
 * overlapping the system bar (navigation bar).
 */
@Composable
fun BottomSheetHolderColumnContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        content()
        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
    }
}

interface BottomSheetHolderScope {
    context(BoxScope)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SnackbarHostBottomSheet(snackbarHostState: SnackbarHostState, sheetState: SheetState, scrollState: ScrollState?)
}

private object BottomSheetHolderScopeInstance : BottomSheetHolderScope {
    /**
     * A [SnackbarHost] with fixed bottom center position which dismiss itself on scroll
     */
    context(BoxScope)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun SnackbarHostBottomSheet(snackbarHostState: SnackbarHostState, sheetState: SheetState, scrollState: ScrollState?) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    val sheetOffset = runCatching { sheetState.requireOffset() }.getOrDefault(0f)
                    val snackbarOffset = screenHeight.dp.toPx() - sheetOffset - OSDimens.SystemSpacing.Regular.toPx()
                    IntOffset(x = 0, y = snackbarOffset.roundToInt())
                }
                .zIndex(UiConstants.SnackBar.ZIndex),
        )

        val scrollInProgress = scrollState?.isScrollInProgress
        if (sheetState.isVisible || scrollInProgress == true) {
            val offset = runCatching { sheetState.requireOffset() }.getOrDefault(0f)
            LaunchedEffect(offset, scrollInProgress) {
                snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }
}
