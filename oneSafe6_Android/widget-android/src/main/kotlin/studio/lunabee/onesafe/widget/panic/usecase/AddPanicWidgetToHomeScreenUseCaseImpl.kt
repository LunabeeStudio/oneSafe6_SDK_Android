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
 * Created by Lunabee Studio / Date - 9/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/09/2024 10:55
 */

package studio.lunabee.onesafe.widget.panic.usecase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.compose.glance.helpers.PinWidgetToHomeScreenHelper
import studio.lunabee.onesafe.domain.usecase.panicmode.AddPanicWidgetToHomeScreenUseCase
import studio.lunabee.onesafe.widget.panic.PanicButtonWidget
import studio.lunabee.onesafe.widget.panic.PanicButtonWidgetReceiver
import javax.inject.Inject

internal class AddPanicWidgetToHomeScreenUseCaseImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AddPanicWidgetToHomeScreenUseCase {
    private val pinWidgetToHomeScreenHelper by lazy {
        PinWidgetToHomeScreenHelper(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend operator fun invoke() {
        pinWidgetToHomeScreenHelper.pin(
            widgetClass = PanicButtonWidget::class.java,
            receiverClass = PanicButtonWidgetReceiver::class.java,
        )
    }

    override suspend fun isSupported(): Boolean {
        return pinWidgetToHomeScreenHelper.isPinSupported()
    }
}
