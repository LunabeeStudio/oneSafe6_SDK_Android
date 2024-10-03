/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Last modified 16/09/2024 16:55
 */

package studio.lunabee.onesafe.widget.panic

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import studio.lunabee.onesafe.widget.panic.worker.MainPanicWidgetWorker
import javax.inject.Inject

@AndroidEntryPoint
internal class PanicButtonWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var workManager: WorkManager

    override val glanceAppWidget: GlanceAppWidget = PanicButtonWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            MainPanicWidgetWorker.start(appWidgetId, workManager, false)
        }
    }
}
