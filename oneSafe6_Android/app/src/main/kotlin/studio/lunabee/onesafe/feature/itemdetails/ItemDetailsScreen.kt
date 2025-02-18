package studio.lunabee.onesafe.feature.itemdetails

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.common.extensions.isInitializing
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.common.utils.OSTipsUtils
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.action.topAppBarTooltipOptionEdit
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.itemactions.ComposeItemAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsDeletedCardData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsElementLayout
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsScreenUiState
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsTab
import studio.lunabee.onesafe.feature.itemdetails.model.MoreTabEntry
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntryFileField
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntryTextField
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.model.OSTopAppBarOption
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.tooltip.OSTooltipAccessibility
import studio.lunabee.onesafe.tooltip.OSTooltipContent
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.io.File
import java.util.UUID
import kotlin.random.Random

context(ComposeItemActionNavScope)
@Composable
fun ItemDetailsRoute(
    viewModel: ItemDetailsViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToItemDetails: (itemId: UUID, popCurrent: Boolean) -> Unit,
    navigateToEditItem: (itemId: UUID) -> Unit,
    navigateToFullScreen: (itemId: UUID, fieldId: UUID) -> Unit,
    showSnackbar: (visuals: SnackbarVisuals) -> Unit,
    navigateToFileViewer: (UUID) -> Unit,
) {
    val itemDetailsScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenTestTag = UiConstants.TestTag.Screen.itemDetailsScreen(viewModel.itemId)
    val context = LocalContext.current
    ComposeItemAction(viewModel)

    when (val uiState = itemDetailsScreenUiState) {
        is ItemDetailsScreenUiState.Data -> {
            val snackbarState by viewModel.snackbarState.collectAsState(null)

            snackbarState?.snackbarVisuals?.let { snackbarVisuals ->
                LaunchedEffect(snackbarVisuals) {
                    showSnackbar(snackbarVisuals)
                }
            }

            OSUserTheme(customPrimaryColor = uiState.color) {
                ItemDetailsScreen(
                    testTag = screenTestTag,
                    uiState = uiState,
                    navigateBack = navigateBack,
                    navigateToItemDetails = navigateToItemDetails,
                    onEditItemClick = { navigateToEditItem(viewModel.itemId) },
                    navigateToFullScreen = { fieldId ->
                        navigateToFullScreen(viewModel.itemId, fieldId)
                    },
                    navigateToFileViewer = navigateToFileViewer,
                    copyText = viewModel::copyText,
                    saveFile = { uri, file ->
                        viewModel.saveFile(uri, file, context)
                    },
                )
            }
        }
        ItemDetailsScreenUiState.Initializing -> {
            val backgroundGradient = LocalDesignSystem.current.backgroundGradient()
            Box(
                Modifier
                    .testTag(screenTestTag)
                    .fillMaxSize()
                    .drawBehind { drawRect(backgroundGradient) },
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailsScreen(
    testTag: String,
    uiState: ItemDetailsScreenUiState.Data,
    navigateBack: () -> Unit,
    navigateToItemDetails: (itemId: UUID, popCurrent: Boolean) -> Unit,
    onEditItemClick: () -> Unit,
    navigateToFullScreen: (id: UUID) -> Unit,
    copyText: (label: String, value: String, isSecured: Boolean) -> Unit,
    navigateToFileViewer: (UUID) -> Unit,
    saveFile: (Uri, File) -> Unit,
) {
    OSScreen(testTag) {
        val lazyListState: LazyListState = rememberLazyListState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyListState)
        val children = uiState.children.collectAsLazyPagingItems()
        var selectedTab: ItemDetailsTab by rememberSaveable(uiState.tabs) {
            mutableStateOf(uiState.tabs.firstOrNull { it == uiState.initialTab } ?: uiState.tabs.first())
        }

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val itemStyleHolder = LocalItemStyle.current
        val itemStyle = itemStyleHolder.standardStyle
        val elementLayout = remember(screenWidth, itemStyle) { computeChildPerRow(screenWidth, itemStyle) }

        val cardContents = mutableListOf<OSLazyCardContent>()

        cardContents += object : OSLazyCardContent.Item {
            override val key: Any = ItemDetailsTab::class.java.simpleName
            override val contentType: Any = ItemDetailsTab::class.java.simpleName

            @Composable
            override fun Content(padding: PaddingValues, modifier: Modifier) {
                ItemDetailsTabs(
                    tabs = uiState.tabs,
                    selectedTab = selectedTab,
                    elementsCount = uiState.childrenCount,
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    onTabSelected = { newTab -> selectedTab = newTab },
                )
            }
        }

        when (selectedTab) {
            ItemDetailsTab.Information -> {
                if (uiState.informationTab.isNotEmpty()) {
                    val entriesCount = uiState.informationTab.size
                    val informationFields = remember(uiState.informationTab) {
                        uiState.informationTab.filterIsInstance<InformationTabEntryTextField>()
                    }
                    cardContents += informationFields.mapIndexed { index, entry ->
                        object : OSLazyCardContent.Item {
                            override val key: Any = entry.id
                            override val contentType: Any = ContentTypeInformationItem

                            @Composable
                            override fun Content(padding: PaddingValues, modifier: Modifier) {
                                if (index == 0) {
                                    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Small))
                                }
                                ItemDetailsTextInformationRow(
                                    field = entry,
                                    navigateToFullScreen = navigateToFullScreen,
                                    copyText = copyText,
                                    modifier = Modifier.padding(padding),
                                    textMaxLines = when {
                                        entry.kind is SafeItemFieldKind.Note && entriesCount == 1 -> Int.MAX_VALUE
                                        entry.kind is SafeItemFieldKind.Note -> UiConstants.Text.MaxLineSizeNoteField
                                        else -> UiConstants.Text.MaxLineSize
                                    },
                                )
                            }
                        }
                    }

                    val fileFields = remember(uiState.informationTab) {
                        uiState.informationTab.filterIsInstance<InformationTabEntryFileField>()
                    }
                    val photoVideoFields = remember(fileFields) { fileFields.filter { it.kind != SafeItemFieldKind.File } }
                    val otherFileFields = remember(fileFields) { fileFields.filter { it.kind == SafeItemFieldKind.File } }

                    photoFieldSection(
                        cardContents = cardContents,
                        fields = photoVideoFields,
                        navigateToFileViewer = navigateToFileViewer,
                        saveFile = saveFile,
                        displayTitle = uiState.informationTab.size > 1,
                    )
                    fileFieldSection(
                        cardContents = cardContents,
                        fields = otherFileFields,
                        navigateToFileViewer = navigateToFileViewer,
                        saveFile = saveFile,
                    )
                } else {
                    cardContents += object : OSLazyCardContent.Item {
                        override val contentType: Any = ContentTypeNoInformation
                        override val key: Any = uiState.emptyFieldsText.hashCode()

                        @Composable
                        override fun Content(padding: PaddingValues, modifier: Modifier) {
                            EmptyTabLayout(
                                text = uiState.emptyFieldsText,
                                showImage = uiState is ItemDetailsScreenUiState.Data.Default,
                            )
                        }
                    }
                }
            }
            ItemDetailsTab.More -> cardContents += uiState.moreTab.map { entry ->
                object : OSLazyCardContent.Item {
                    override val key: Any = entry.id
                    override val contentType: Any = ContentTypeMoreItem

                    @Composable
                    override fun Content(padding: PaddingValues, modifier: Modifier) {
                        OSRow(
                            text = entry.value,
                            modifier = Modifier.padding(
                                start = OSDimens.SystemSpacing.Regular,
                                end = OSDimens.SystemSpacing.Regular,
                                top = OSDimens.SystemSpacing.Regular,
                                bottom = padding.calculateBottomPadding(),
                            ),
                            label = entry.label,
                        )
                    }
                }
            }
            ItemDetailsTab.Elements -> {
                val showLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(children)
                val waitingForData = !showLoading && children.isInitializing()

                if (uiState.childrenCount == 0) {
                    cardContents += object : OSLazyCardContent.Item {
                        override val key: Any = ContentTypeNoElement
                        override val contentType: Any = uiState.emptyElementsText.hashCode()

                        @Composable
                        override fun Content(padding: PaddingValues, modifier: Modifier) {
                            EmptyTabLayout(
                                text = uiState.emptyElementsText,
                                showImage = uiState is ItemDetailsScreenUiState.Data.Default,
                            )
                        }
                    }
                } else {
                    cardContents += OSLazyCardContent.Paged { positionInList ->
                        ItemDetailsLazyColumnItems.itemElementsTab(
                            children = children,
                            navigateToItemDetails = { itemId -> navigateToItemDetails(itemId, false) },
                            elementLayout = elementLayout,
                            positionInList = positionInList,
                            placeholders = if (showLoading || waitingForData) uiState.childrenCount else 0,
                            itemsLayout = itemStyleHolder.layout,
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .testTag(tag = ItemDetailLazyColumn),
            state = lazyListState,
            contentPadding = PaddingValues(
                start = OSDimens.SystemSpacing.Regular,
                end = OSDimens.SystemSpacing.Regular,
                top = OSDimens.ItemTopBar.Height,
            ),
        ) {
            ItemDetailsLazyColumnItems.itemLargeTitle(
                lazyListScope = this,
                icon = uiState.icon,
                itemNameProvider = uiState.itemNameProvider,
            )

            if (uiState is ItemDetailsScreenUiState.Data.Deleted) {
                ItemDetailsLazyColumnItems.deletedCard(
                    lazyListScope = this,
                    deletedCardData = uiState.deletedCardData,
                )
                lazyVerticalOSRegularSpacer()
            }

            uiState.corruptedCardData?.let { corruptedCardData ->
                ItemDetailsLazyColumnItems.errorCard(
                    lazyListScope = this,
                    errorCardData = corruptedCardData,
                )
                lazyVerticalOSRegularSpacer()
            }

            uiState.notSupportedKindsList?.let { list ->
                ItemDetailsLazyColumnItems.notFullySupportFieldsCard(
                    lazyListScope = this,
                    notSupportedKindsList = list,
                )
                lazyVerticalOSRegularSpacer()
            }

            osLazyCard(cardContents)

            lazyVerticalOSRegularSpacer()

            val regularActions = uiState.actions.filter { it.type == SafeItemAction.Type.Normal }
            if (regularActions.isNotEmpty()) {
                ItemDetailsLazyColumnItems.itemActionsCard(
                    lazyListScope = this,
                    actions = regularActions,
                    key = UiConstants.TestTag.Item.ItemDetailsRegularActionCard,
                )
                lazyVerticalOSRegularSpacer()
            }

            val dangerousActions = uiState.actions.filter { it.type == SafeItemAction.Type.Dangerous }
            if (dangerousActions.isNotEmpty()) {
                ItemDetailsLazyColumnItems.itemActionsCard(
                    lazyListScope = this,
                    actions = dangerousActions,
                    key = UiConstants.TestTag.Item.ItemDetailsDangerousActionCard,
                )
                lazyVerticalOSRegularSpacer()
            }
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            var hasAlreadyBeenDisplayed: Boolean by rememberSaveable { mutableStateOf(false) }
            val tooltipState: TooltipState = rememberTooltipState(isPersistent = true)
            val coroutineScope = rememberCoroutineScope()
            val dismissRequest: () -> Unit = { coroutineScope.launch { tooltipState.dismiss() } }
            LaunchedEffect(key1 = uiState.shouldShowEditTips) {
                if (!hasAlreadyBeenDisplayed && uiState.shouldShowEditTips) {
                    hasAlreadyBeenDisplayed = true
                    delay(UiConstants.Tooltip.DisplayDelay)
                    tooltipState.show()
                }
            }
            ItemDetailsTopBar(
                navigateBack = navigateBack,
                editOption = uiState.getEditOption(onEditItemClick, tooltipState, dismissRequest),
                modifier = Modifier
                    .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun ItemDetailsScreenUiState.Data.getEditOption(
    onEditItemClick: () -> Unit,
    tooltipState: TooltipState,
    dismissRequest: () -> Unit,
): OSTopAppBarOption? {
    return when (this) {
        is ItemDetailsScreenUiState.Data.Default -> {
            if (this.isCorrupted) {
                null
            } else {
                topAppBarTooltipOptionEdit(
                    description = LbcTextSpec.StringResource(OSString.safeItemDetail_accessibility_edit),
                    onEditItemClick = onEditItemClick,
                    tooltipState = tooltipState,
                    tooltipContent = OSTooltipContent(
                        title = OSTipsUtils.CommonTipsTitle,
                        description = LbcTextSpec.StringResource(OSString.safeItemDetail_tips_edit),
                        actions = listOf(OSTipsUtils.getGotItAction()),
                    ),
                    tooltipAccessibility = OSTooltipAccessibility(
                        actionText = LbcTextSpec.StringResource(OSString.common_accessibility_popup_dismiss),
                        action = dismissRequest,
                    ),
                )
            }
        }
        is ItemDetailsScreenUiState.Data.Deleted -> null
    }
}

// TODO move it somewhere else (shared with SearchScreen)
fun computeChildPerRow(screenWidth: Dp, itemStyle: OSSafeItemStyle): ItemDetailsElementLayout {
    val cardContentWidth = screenWidth - OSDimens.SystemSpacing.Regular * 2 - itemStyle.spacing
    val elementWidth = itemStyle.elementSize + itemStyle.spacing
    val minSpacing = OSDimens.AlternativeSpacing.ElementRowMinSpacing
    val childPerRow: Int = ((cardContentWidth + minSpacing) / (elementWidth + minSpacing)).toInt()
    val allElementsWidth = elementWidth * childPerRow
    val spacing = (cardContentWidth - allElementsWidth) / (childPerRow - 1)
    return ItemDetailsElementLayout(childPerRow, spacing, itemStyle)
}

private const val ContentTypeInformationItem: String = "ContentTypeInformationItem"
private const val ContentTypeMoreItem: String = "ContentTypeMoreItem"
private const val ContentTypeNoInformation: String = "ContentTypeNoInformation"
private const val ContentTypeNoElement: String = "ContentTypeNoElement"

@OsDefaultPreview
@Composable
@Suppress("SpreadOperator")
private fun ItemDetailsScreenDefaultPreview() {
    OSTheme {
        ItemDetailsScreen(
            testTag = UUID.randomUUID().toString(),
            uiState = ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(loremIpsum(2)),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = List(3) {
                    InformationTabEntryTextField(
                        id = UUID.randomUUID(),
                        value = loremIpsumSpec(2),
                        label = loremIpsumSpec(1),
                        kind = SafeItemFieldKind.Text,
                        isSecured = false,
                        getDecryptedDisplayValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                        getDecryptedRawValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                    )
                } + List(1) {
                    InformationTabEntryFileField(
                        id = UUID.randomUUID(),
                        kind = SafeItemFieldKind.File,
                        thumbnail = MutableStateFlow(null),
                        file = MutableStateFlow(File("")),
                        name = loremIpsumSpec(3),
                        loadFile = {},
                    )
                } + List(1) {
                    InformationTabEntryFileField(
                        id = UUID.randomUUID(),
                        kind = SafeItemFieldKind.Photo,
                        thumbnail = MutableStateFlow(OSImageSpec.Drawable(OSDrawable.ic_crown)),
                        file = MutableStateFlow(File("")),
                        name = loremIpsumSpec(3),
                        loadFile = {},
                    )
                } + List(1) {
                    InformationTabEntryFileField(
                        id = UUID.randomUUID(),
                        kind = SafeItemFieldKind.Video,
                        thumbnail = MutableStateFlow(OSImageSpec.Drawable(OSDrawable.ic_crown)),
                        file = MutableStateFlow(File("")),
                        name = loremIpsumSpec(3),
                        loadFile = {},
                    )
                },
                moreTab = listOf(MoreTabEntry.UpdatedAt(loremIpsumSpec(3))),
                children = flowOf(
                    PagingData.from(
                        (0..500).map {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites({})) },
                            )
                        },
                    ),
                ),
                childrenCount = 500,
                actions = listOf(
                    SafeItemAction.AddToFavorites {},
                    SafeItemAction.RemoveFromFavorites {},
                    SafeItemAction.Share {},
                    SafeItemAction.Move {},
                    SafeItemAction.Duplicate {},
                    SafeItemAction.Delete {},
                ),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
            navigateBack = { },
            navigateToItemDetails = { _, _ -> },
            onEditItemClick = { },
            navigateToFullScreen = { },
            copyText = { _, _, _ -> },
            navigateToFileViewer = {},
            saveFile = { _, _ -> },
        )
    }
}

@OsDefaultPreview
@Composable
private fun ItemDetailsScreenDeletedPreview() {
    OSTheme {
        ItemDetailsScreen(
            testTag = UUID.randomUUID().toString(),
            uiState = ItemDetailsScreenUiState.Data.Deleted(
                defaultData = ItemDetailsScreenUiState.Data.Default(
                    itemNameProvider = DefaultNameProvider(loremIpsum(2)),
                    icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
                    tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                    informationTab = (0..5).map {
                        InformationTabEntryTextField(
                            id = UUID.randomUUID(),
                            value = loremIpsumSpec(2),
                            label = loremIpsumSpec(1),
                            kind = SafeItemFieldKind.Text,
                            isSecured = false,
                            getDecryptedDisplayValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                            getDecryptedRawValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                        )
                    },
                    moreTab = listOf(MoreTabEntry.UpdatedAt(loremIpsumSpec(3))),
                    children = flowOf(
                        PagingData.from(
                            (0..500).map {
                                PlainItemDataDefault(
                                    id = UUID.randomUUID(),
                                    itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                    icon = iconSample.takeIf { Random.nextBoolean() },
                                    color = randomColor,
                                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                                )
                            },
                        ),
                    ),
                    childrenCount = 500,
                    actions = listOf(
                        SafeItemAction.Share {},
                        SafeItemAction.Restore {},
                        SafeItemAction.Remove {},
                    ),
                    color = null,
                    initialTab = ItemDetailsTab.Information,
                    isCorrupted = false,
                    notSupportedKindsList = null,
                    shouldShowEditTips = false,
                ),
                deletedCardData = ItemDetailsDeletedCardData(
                    message = LbcTextSpec.PluralsResource(OSPlurals.safeItemDetail_deletedCard_message, 2, 2),
                    action = LbcTextSpec.StringResource(OSString.safeItemDetail_deletedCard_action),
                ) {},
            ),
            navigateBack = { },
            navigateToItemDetails = { _, _ -> },
            onEditItemClick = { },
            navigateToFullScreen = { },
            copyText = { _, _, _ -> },
            navigateToFileViewer = {},
            saveFile = { _, _ -> },
        )
    }
}

@OsDefaultPreview
@Composable
private fun ItemDetailsScreenCorruptedPreview() {
    OSTheme {
        ItemDetailsScreen(
            testTag = UUID.randomUUID().toString(),
            uiState = ItemDetailsScreenUiState.Data.Deleted(
                defaultData = ItemDetailsScreenUiState.Data.Default(
                    itemNameProvider = DefaultNameProvider(loremIpsum(2)),
                    icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
                    tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                    informationTab = (0..5).map {
                        InformationTabEntryTextField(
                            id = UUID.randomUUID(),
                            value = loremIpsumSpec(2),
                            label = loremIpsumSpec(1),
                            kind = SafeItemFieldKind.Text,
                            isSecured = false,
                            getDecryptedDisplayValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                            getDecryptedRawValue = { loremIpsum(2).takeIf { Random.nextBoolean() } },
                        )
                    },
                    moreTab = listOf(MoreTabEntry.UpdatedAt(loremIpsumSpec(3))),
                    children = flowOf(
                        PagingData.from(
                            (0..500).map {
                                PlainItemDataDefault(
                                    id = UUID.randomUUID(),
                                    itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                    icon = iconSample.takeIf { Random.nextBoolean() },
                                    color = randomColor,
                                    actions = { listOf(SafeItemAction.AddToFavorites({})) },
                                )
                            },
                        ),
                    ),
                    childrenCount = 500,
                    actions = listOf(
                        SafeItemAction.RemoveFromFavorites {},
                        SafeItemAction.Remove {},
                    ),
                    color = null,
                    initialTab = ItemDetailsTab.Information,
                    isCorrupted = true,
                    notSupportedKindsList = listOf(
                        SafeItemFieldKind.Unknown("Photo"),
                        SafeItemFieldKind.Unknown("Video"),
                    ),
                    shouldShowEditTips = false,
                ),
                deletedCardData = ItemDetailsDeletedCardData(
                    message = LbcTextSpec.PluralsResource(OSPlurals.safeItemDetail_deletedCard_message, 2, 2),
                    action = LbcTextSpec.StringResource(OSString.safeItemDetail_deletedCard_action),
                ) {},
            ),
            navigateBack = { },
            navigateToItemDetails = { _, _ -> },
            onEditItemClick = { },
            navigateToFullScreen = { },
            copyText = { _, _, _ -> },
            navigateToFileViewer = {},
            saveFile = { _, _ -> },
        )
    }
}
