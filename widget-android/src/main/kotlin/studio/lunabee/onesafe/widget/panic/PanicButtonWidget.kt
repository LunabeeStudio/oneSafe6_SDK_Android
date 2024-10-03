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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.widget.panic.composable.DisabledPanicWidgetLayout
import studio.lunabee.onesafe.widget.panic.composable.EnabledPanicModeLayout
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetState
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetStateDefinition
import studio.lunabee.onesafe.widget.panic.worker.MainPanicWidgetWorker
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class PanicButtonWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PanicButtonWidgetEntryPoint {
        fun workManager(): WorkManager
    }

    private fun workManager(context: Context): WorkManager {
        val appContext = context.applicationContext ?: error("No application context")
        val hiltEntryPoint = EntryPointAccessors.fromApplication(appContext, PanicButtonWidgetEntryPoint::class.java)
        return hiltEntryPoint.workManager()
    }

    override val stateDefinition: PanicWidgetStateDefinition = PanicWidgetStateDefinition
    private val counter: MutableStateFlow<Int> = MutableStateFlow(0)
    private var job: Job? = null

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val widgetState: PanicWidgetState = currentState<PanicWidgetState>()
            val counter by counter.collectAsState()
            Box(
                modifier = GlanceModifier.size(WidgetSize),
            ) {
                if (widgetState.isEnabled) {
                    EnabledPanicModeLayout(
                        counter = counter,
                        widgetState = widgetState,
                        onClick = onClick(id, context),
                    )
                } else {
                    DisabledPanicWidgetLayout()
                }
            }
        }
    }

    @Composable
    fun onClick(widgetId: GlanceId, context: Context): Action {
        val appWidgetManager = GlanceAppWidgetManager(context)
        val coroutineScope = rememberCoroutineScope()
        return action {
            if (counter.value < CounterForDeletion - 1) {
                incrementCounter(coroutineScope)
            } else {
                counter.value = 0
                MainPanicWidgetWorker.start(
                    workManager = workManager(context),
                    isPanicDestruction = true,
                    widgetId = appWidgetManager.getAppWidgetId(widgetId),
                )
            }
        }
    }

    private fun incrementCounter(coroutineScope: CoroutineScope) {
        job?.cancel()
        counter.value++
        job = coroutineScope.launch {
            delay(ResetDelay)
            counter.value = 0
        }
    }

    companion object {
        private val WidgetSize: Dp = 192.dp
        private val ResetDelay: Duration = 1.seconds
        private const val CounterForDeletion: Int = 5
    }
}
