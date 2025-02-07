package studio.lunabee.onesafe.feature.breadcrumb

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring.DampingRatioHighBouncy
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.getNameForBreadcrumb
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.molecule.OSNavigationItem
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

@Composable
fun Breadcrumb(
    navigate: (BreadcrumbDestinationSpec) -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: (() -> Unit)?,
    items: ImmutableList<BreadcrumbUiDataSpec>,
    color: Color?,
    isSearchActive: Boolean,
    isSearchVisible: Boolean,
    modifier: Modifier = Modifier,
    isFullyVisible: Boolean = true, // Used to determined when to start the "+" button animation
) {
    OSUserTheme(customPrimaryColor = color) {
        Breadcrumb(
            modifier = modifier
                .fillMaxWidth()
                .testTag(UiConstants.TestTag.BreadCrumb.BreadCrumbLayout),
            onNavigationItemClick = navigate,
            onSearchClick = onSearchClick,
            onAddClick = onAddClick,
            isSearchActive = isSearchActive,
            isSearchVisible = isSearchVisible,
            items = items,
            isFullyVisible = isFullyVisible,
        )
    }
}

@Composable
private fun Breadcrumb(
    modifier: Modifier,
    onNavigationItemClick: ((BreadcrumbDestinationSpec) -> Unit)?,
    onSearchClick: (() -> Unit)?,
    onAddClick: (() -> Unit)?,
    isSearchActive: Boolean,
    isSearchVisible: Boolean,
    items: ImmutableList<BreadcrumbUiDataSpec>,
    isFullyVisible: Boolean = true,
) {
    val (disabledSearchColorBackground, disabledSearchColorContent) = LocalDesignSystem.current.disabledSearchButtonColor()
    val enabledSearchColorBackground = MaterialTheme.colorScheme.onBackground
    val enabledSearchColorContent = MaterialTheme.colorScheme.background

    val scaleSearch = remember { Animatable(1f) }
    var targetBackgroundSearchColor by remember { mutableStateOf(disabledSearchColorBackground) }
    var targetContentSearchColor by remember { mutableStateOf(disabledSearchColorContent) }
    val searchBackgroundColor by animateColorAsState(targetValue = targetBackgroundSearchColor)
    val searchContentColor by animateColorAsState(targetValue = targetContentSearchColor)

    var alreadyBumped by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isSearchVisible, isSearchActive, disabledSearchColorBackground) {
        if (isSearchActive) {
            targetBackgroundSearchColor = enabledSearchColorBackground
            targetContentSearchColor = enabledSearchColorContent
        } else {
            alreadyBumped = false
            targetBackgroundSearchColor = disabledSearchColorBackground
            targetContentSearchColor = disabledSearchColorContent
        }

        if (isSearchActive && !isSearchVisible && !alreadyBumped) {
            alreadyBumped = true
            scaleSearch.animateTo(UiConstants.Animation.ScaleSearchAnimation)
            scaleSearch.animateTo(
                initialVelocity = UiConstants.Animation.InitialVelocitySpringSearch,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = DampingRatioHighBouncy,
                    stiffness = StiffnessMediumLow,
                ),
            )
        } else if (isSearchVisible) {
            alreadyBumped = false
        }
    }

    val breadcrumbDescription = stringResource(id = OSString.common_breadcrumb)
    Column(
        modifier = modifier
            .semantics {
                heading()
                contentDescription = breadcrumbDescription
            }
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        val navigationBars = WindowInsets.navigationBars
        Box(
            modifier = Modifier
                .height(height = OSDimens.Breadcrumb.Height)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (onAddClick != null) {
                val addButtonStyle = OSDimens.SystemButtonDimension.Large

                val displayCutout = WindowInsets.displayCutout
                val density = LocalDensity.current
                val layoutDirection = LocalLayoutDirection.current

                val systemPadding by remember(navigationBars, displayCutout) {
                    mutableIntStateOf(
                        displayCutout.getRight(density, layoutDirection) +
                            navigationBars.getRight(density, layoutDirection),
                    )
                }

                // Divider must be placed before "add" button.
                if (!isSystemInDarkTheme()) {
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = LocalColorPalette.current.Border,
                        thickness = OSDimens.Breadcrumb.Divider,
                    )
                }

                Column(
                    modifier = Modifier
                        .size(addButtonStyle.container.dp)
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                -OSDimens.SystemSpacing.Regular.roundToPx() - systemPadding,
                                -(addButtonStyle.container.dp / 2).roundToPx(),
                            )
                        },
                ) {
                    AnimatedVisibility(
                        visible = isFullyVisible,
                        enter = scaleIn(tween(AppConstants.Ui.Animation.AddButton.ScaleInDurationMs)),
                        exit = scaleOut(tween(AppConstants.Ui.Animation.AddButton.ScaleOutDurationMs)),
                    ) {
                        OSIconButton(
                            modifier = Modifier
                                .testTag(tag = UiConstants.TestTag.BreadCrumb.OSCreateItemButton),
                            image = OSImageSpec.Drawable(OSDrawable.ic_add),
                            contentDescription = LbcTextSpec.StringResource(id = OSString.breadcrumb_accessibility_add),
                            onClick = { onAddClick() },
                            buttonSize = addButtonStyle,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .landscapeSystemBarsPadding()
                    .padding(horizontal = OSDimens.SystemSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OSIconButton(
                    image = OSImageSpec.Drawable(OSDrawable.ic_search),
                    contentDescription = LbcTextSpec.StringResource(id = OSString.breadcrumb_accessibility_search),
                    onClick = { onSearchClick?.invoke() },
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = searchBackgroundColor,
                        contentColor = searchContentColor,
                    ),
                    modifier = Modifier.scale(scale = scaleSearch.value),
                )

                OSSmallSpacer()

                val lazyListState: LazyListState = rememberLazyListState(items.lastIndex.coerceAtLeast(0))
                if (items.isNotEmpty()) {
                    LaunchedEffect(key1 = items.size) {
                        lazyListState.scrollToItem(items.lastIndex)
                    }
                }

                LazyRow(
                    modifier = Modifier.weight(1f),
                    state = lazyListState,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(items) { idx, breadcrumbItem ->
                        if (idx != 0) {
                            Separator()
                        }

                        // If not last item of breadcrumb, crop it to 12 char if more than 14 characters and add ellipsis
                        val name = breadcrumbItem.name.getNameForBreadcrumb(idx != items.lastIndex)

                        val contentDescription = breadcrumbItem.name.string
                        val label = stringResource(id = OSString.common_navigate)
                        val onClick: () -> Unit = { onNavigationItemClick?.invoke(breadcrumbItem.destination) }

                        OSNavigationItem(
                            text = name,
                            onClick = onClick,
                            isActive = idx == items.lastIndex,
                            modifier = Modifier
                                .clearAndSetSemantics {
                                    this.contentDescription = contentDescription
                                    this.role = Role.Button
                                    this.accessibilityClick(label, onClick)
                                },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(navigationBars))
    }
}

@Composable
private fun Separator() {
    Icon(
        painter = painterResource(id = OSDrawable.ic_navigate_next),
        modifier = Modifier.size(OSDimens.Breadcrumb.SeparatorSize),
        contentDescription = null,
        tint = LocalDesignSystem.current.navigationItemLabelColor(isActive = false),
    )
}

@OsDefaultPreview
@Composable
private fun BreadcrumbEmptyPreview() {
    BreadcrumbContainerPreview { ctx ->
        Breadcrumb(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onNavigationItemClick = { Toast.makeText(ctx, "Nav to $it", Toast.LENGTH_SHORT).show() },
            onSearchClick = { Toast.makeText(ctx, "Search", Toast.LENGTH_SHORT).show() },
            onAddClick = { Toast.makeText(ctx, "Add", Toast.LENGTH_SHORT).show() },
            items = persistentListOf(),
            isSearchVisible = false,
            isSearchActive = true,
        )
    }
}

@OsDefaultPreview
@Composable
private fun BreadcrumbOneNoAddPreview() {
    BreadcrumbContainerPreview { ctx ->
        Breadcrumb(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onNavigationItemClick = { Toast.makeText(ctx, "Nav to $it", Toast.LENGTH_SHORT).show() },
            onSearchClick = { Toast.makeText(ctx, "Search", Toast.LENGTH_SHORT).show() },
            onAddClick = null,
            items = persistentListOf(
                RouteBreadcrumbUiData.home(),
                RouteBreadcrumbUiData.bin(),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 1"), BreadcrumbMainAction.None),
            ),
            isSearchActive = true,
            isSearchVisible = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun BreadcrumbFullPreview() {
    BreadcrumbContainerPreview { ctx ->
        Breadcrumb(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            isSearchVisible = false,
            onNavigationItemClick = { Toast.makeText(ctx, "Nav to $it", Toast.LENGTH_SHORT).show() },
            onSearchClick = { Toast.makeText(ctx, "Search", Toast.LENGTH_SHORT).show() },
            onAddClick = { Toast.makeText(ctx, "Add", Toast.LENGTH_SHORT).show() },
            items = persistentListOf(
                RouteBreadcrumbUiData.home(),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 1"), BreadcrumbMainAction.AddItem),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 2"), BreadcrumbMainAction.AddItem),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 3"), BreadcrumbMainAction.AddItem),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 4"), BreadcrumbMainAction.AddItem),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 5"), BreadcrumbMainAction.AddItem),
                ItemBreadcrumbUiData(UUID.randomUUID(), DefaultNameProvider("Item 6"), BreadcrumbMainAction.AddItem),
            ),
            isSearchActive = true,
        )
    }
}

@Composable
private fun BreadcrumbContainerPreview(content: @Composable BoxScope.(ctx: Context) -> Unit) {
    OSTheme {
        val background = MaterialTheme.colorScheme.surface
        val context = LocalContext.current
        Box(
            Modifier
                .drawBehind { drawRect(background) }
                .fillMaxWidth()
                .height(OSDimens.Breadcrumb.Height * 2),
        ) {
            content(context)
        }
    }
}
