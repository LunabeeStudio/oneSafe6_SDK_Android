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
 * Last modified 16/09/2024 13:52
 */

package studio.lunabee.onesafe.widget.panic.composable

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.fillMaxSize
import studio.lunabee.onesafe.widget.R

@Composable
internal fun DisabledPanicWidgetLayout() {
    Image(
        provider = ImageProvider(R.drawable.widget_panic_disabled),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize(),
    )
}
