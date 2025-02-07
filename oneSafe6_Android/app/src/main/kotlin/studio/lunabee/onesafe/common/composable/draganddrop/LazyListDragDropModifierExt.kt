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

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Inspired from https://issuetracker.google.com/issues/181282427 and this specific comment
 * https://issuetracker.google.com/issues/181282427#comment25.
 */
fun Modifier.dragContainer(
    dragDropState: DragDropState,
    key: Any,
): Modifier = pointerInput(dragDropState) {
    detectDragGestures(
        onDrag = { change, offset ->
            change.consume()
            dragDropState.onDrag(offset = offset)
        },
        onDragStart = { dragDropState.onDragStartWithKey(key) },
        onDragEnd = { dragDropState.onDragInterrupted() },
        onDragCancel = { dragDropState.onDragInterrupted() },
    )
}
