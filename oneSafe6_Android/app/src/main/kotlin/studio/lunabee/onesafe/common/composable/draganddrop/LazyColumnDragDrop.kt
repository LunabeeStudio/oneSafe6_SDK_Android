/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package studio.lunabee.onesafe.common.composable.draganddrop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import studio.lunabee.onesafe.commonui.OSString

/**
 * Inspired from https://issuetracker.google.com/issues/181282427 and this specific comment
 * https://issuetracker.google.com/issues/181282427#comment25.
 */
@Composable
fun <T> LazyColumnDragDrop(
    elements: List<T>,
    elementsKeys: List<Any>,
    onListChange: (newList: List<T>, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    itemContent: @Composable ColumnScope.(index: Int, T, isDragging: Boolean) -> Unit,
) {
    var dragAndDropList: List<T> by remember { mutableStateOf(elements) }

    val dragDropState = rememberDragDropState(lazyListState) { fromIndex, toIndex ->
        dragAndDropList = dragAndDropList.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        onListChange(dragAndDropList, toIndex)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        itemsIndexed(
            items = dragAndDropList,
            key = { index, _ -> elementsKeys[index] },
        ) { index, item ->
            val context = LocalContext.current
            DraggableItem(
                dragDropState = dragDropState,
                index = index,
                modifier = Modifier
                    .semantics(mergeDescendants = true) {
                        this.onLongClick(context.getString(OSString.safeItemDetail_reorder_accessibility_longClickLabel)) { true }
                    },
            ) { isDragging ->
                val elevation: Dp by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "DragAndDropElevation")
                Surface(
                    shadowElevation = elevation,
                    modifier = Modifier
                        .dragContainer(
                            dragDropState = dragDropState,
                            key = elementsKeys[index],
                        ),
                ) {
                    itemContent(index, item, isDragging)
                }
            }
        }
    }
}

@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit,
) {
    val dragging = index == dragDropState.draggingItemIndex
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = dragDropState.draggingItemOffset
            }
    } else if (index == dragDropState.previousIndexOfDraggedItem) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = dragDropState.previousItemOffset.value
            }
    } else {
        Modifier.animateItem()
    }
    Column(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}
