/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/16/2024 - for the oneSafe6 SDK.
 * Last modified 16/09/2024 13:47
 */

package studio.lunabee.onesafe.widget.panic.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import studio.lunabee.compose.glance.helpers.glanceStringResource
import studio.lunabee.compose.glance.ui.GlanceViewFlipper
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.widget.R
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val TextPadding: Dp = 16.dp
private val ActionSize: Dp = 100.dp
private val FlipInterval: Duration = 3.seconds

@Composable
internal fun EnabledPanicModeLayout(
    widgetState: PanicWidgetState,
    counter: Int,
    onClick: Action,
) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_panic_disabled),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
        )
        GlanceViewFlipper(
            viewFlipperLayout = R.layout.view_flipper_idle,
            viewFlipperViewId = R.id.view_flipper_fade,
            flipInterval = FlipInterval,
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            listOf(
                R.drawable.widget_panic_idle_0,
                R.drawable.widget_panic_idle_1,
                R.drawable.widget_panic_idle_2,
                R.drawable.widget_panic_idle_3,
                R.drawable.widget_panic_idle_4,
            ).forEach {
                Image(
                    provider = ImageProvider(it),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                )
            }
        }
        Box(
            modifier = GlanceModifier
                .size(ActionSize)
                .clickable(onClick),
        ) {
        }
    }
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        val string = when {
            widgetState.isLoading -> glanceStringResource(OSString.common_loading)
            counter > 0 -> counter.toString()
            else -> null
        }
        string?.let {
            Text(
                text = it,
                modifier = GlanceModifier.padding(TextPadding).fillMaxWidth(),
                style = TextStyle(color = ColorProvider(Color.White)),
            )
        }
    }
}
