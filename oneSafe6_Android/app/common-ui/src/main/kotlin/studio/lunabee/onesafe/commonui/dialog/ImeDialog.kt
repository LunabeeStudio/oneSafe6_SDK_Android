/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 9/4/2023 - for the oneSafe6 SDK.
 * Last modified 9/4/23, 10:40 AM
 */

package studio.lunabee.onesafe.commonui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.dialog.OSDialogContent
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun DialogState.ImeDialog(onDispose: () -> Unit) {
    DisposableEffect(key1 = Unit) { onDispose(onDispose) }
    Box(
        modifier = Modifier
            .semantics { dialog() }
            .fillMaxWidth()
            .zIndex(1f)
            .background(LocalDesignSystem.current.scrimColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                this@ImeDialog.dismiss()
            },
        contentAlignment = Alignment.Center,
    ) {
        OSDialogContent(
            title = this@ImeDialog.title,
            content = {
                this@ImeDialog.message?.let {
                    OSText(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(DialogSizePercent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
        ) {
            this@ImeDialog.actions.forEach { dialogAction ->
                dialogAction.ActionButton()
            }
        }
    }
}

private const val DialogSizePercent: Float = 0.8f
