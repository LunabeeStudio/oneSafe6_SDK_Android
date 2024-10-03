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
 * Last modified 16/09/2024 16:10
 */

package studio.lunabee.onesafe.widget.panic.usecase

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.domain.usecase.UpdatePanicButtonWidgetUseCase
import studio.lunabee.onesafe.widget.panic.PanicButtonWidget
import studio.lunabee.onesafe.widget.panic.worker.MainPanicWidgetWorker
import javax.inject.Inject

class UpdatePanicButtonWidgetUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
) : UpdatePanicButtonWidgetUseCase {
    override suspend operator fun invoke() {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(PanicButtonWidget::class.java)
            .map(manager::getAppWidgetId)
            .forEach { id ->
                MainPanicWidgetWorker.start(
                    widgetId = id,
                    workManager = workManager,
                    isPanicDestruction = false,
                )
            }
    }
}
