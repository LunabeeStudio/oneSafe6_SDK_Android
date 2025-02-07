@file:OptIn(ExperimentalMaterial3Api::class)

package studio.lunabee.onesafe.feature.search.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.itemdetails.computeChildPerRow
import studio.lunabee.onesafe.feature.search.composable.SearchHeader
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.feature.search.holder.SearchUiState
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    searchState: SearchUiState,
    onRecentSearchClick: (String) -> Unit,
    onValueSearchChange: (String, Boolean) -> Unit,
    searchValue: String,
    onItemClick: (UUID) -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        bottomOverlayBrush = LocalDesignSystem.current.navBarOverlayBackgroundGradientBrush,
        skipPartiallyExpanded = true,
        fullScreen = true,
    ) { closeBottomSheet, paddingValues ->
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val elementLayout = remember(screenWidth) { computeChildPerRow(screenWidth, OSSafeItemStyle.Regular) }
        val focusRequester = remember { FocusRequester() }
        val background = LocalDesignSystem.current.backgroundGradient()
        var textFieldValueState by remember {
            mutableStateOf(
                TextFieldValue(
                    text = searchValue,
                    selection = TextRange(searchValue.length),
                ),
            )
        }

        val lazyListStateSearch = rememberLazyListState()
        val localFocusManager = LocalFocusManager.current

        val localOnItemClick: (UUID) -> Unit = {
            localFocusManager.clearFocus()
            onItemClick(it)
        }

        // Close Keyboard on scroll
        LaunchedEffect(lazyListStateSearch.isScrollInProgress) {
            if (lazyListStateSearch.isScrollInProgress) {
                onValueSearchChange(searchValue, true)
                localFocusManager.clearFocus()
            }
        }

        Column(
            modifier = Modifier
                .testTag(UiConstants.TestTag.Screen.SearchScreen)
                .fillMaxWidth()
                .drawBehind { drawRect(brush = background) },
        ) {
            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))
            val coroutineScope = rememberCoroutineScope()
            val isImeVisible = WindowInsets.isImeVisible
            SearchHeader(
                textFieldValue = textFieldValueState,
                focusRequester = focusRequester,
                focusManager = localFocusManager,
                navigateBack = {
                    if (isImeVisible) {
                        coroutineScope.launch {
                            localFocusManager.clearFocus()
                            delay(AppConstants.Ui.Animation.BottomSheet.AppearanceDelay)
                            closeBottomSheet()
                        }
                    } else {
                        closeBottomSheet()
                    }
                },
                onValueChange = { textFieldValue, finalSearch ->
                    // Only trigger onValueSearchChange for text changes (not for cursor changes)
                    if (textFieldValueState.text != textFieldValue.text) {
                        onValueSearchChange(textFieldValue.text, finalSearch)
                    }
                    textFieldValueState = textFieldValue
                },
                onClear = {
                    focusRequester.requestFocus()
                    textFieldValueState = TextFieldValue(text = "")
                    onValueSearchChange("", true)
                },
                itemCount = searchState.itemCount,
            )
            LaunchedEffect(isVisible) {
                if (isVisible && searchValue.isEmpty()) {
                    delay(AppConstants.Ui.Animation.BottomSheet.AppearanceDelay)
                    focusRequester.requestFocus()
                } else {
                    localFocusManager.clearFocus()
                }
            }
            if (searchState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(UiConstants.TestTag.Item.SearchLoading),
                )
            } else {
                Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.ExtraSmall))
            }

            LazyColumn(
                state = lazyListStateSearch,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = OSDimens.SystemSpacing.Small,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                ),
            ) {
                when (searchState.resultUIState) {
                    is SearchResultUiState.Idle -> searchIdleScreen(
                        searchData = searchState.resultUIState.searchData,
                        onItemClick = localOnItemClick,
                        onSearchClick = {
                            textFieldValueState = TextFieldValue(text = it, selection = TextRange(it.length))
                            onRecentSearchClick(it)
                        },
                        elementLayout = elementLayout,
                    )
                    is SearchResultUiState.Searching -> searchResultScreen(
                        itemCount = searchState.itemCount,
                        state = searchState.resultUIState,
                        onItemClick = localOnItemClick,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }
    }
}
