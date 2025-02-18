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
 * Created by Lunabee Studio / Date - 9/24/2024 - for the oneSafe6 SDK.
 * Last modified 9/24/24, 5:35 PM
 */

package studio.lunabee.onesafe.feature.clipboard

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardScheduleClearUseCase
import javax.inject.Inject
import kotlin.time.Duration

class AndroidClipboardScheduleClearUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClipboardScheduleClearUseCase {
    override suspend fun setup(clearDelay: Duration, safeId: SafeId) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClipboardClearBroadcastReceiver::class.java)
            .putExtra(ClipboardClearBroadcastReceiver.SAFE_ID_CLIPBOARD_EXTRA, safeId.id)
        val pIntent = PendingIntent.getBroadcast(
            context,
            0, // not used
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )!!
        val doSetAlarm: (type: Int, triggerAtMillis: Long, operation: PendingIntent) -> Unit =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager::setExact
            } else {
                alarmManager::set
            }
        doSetAlarm(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + clearDelay.inWholeMilliseconds,
            pIntent,
        )
    }

    override fun cancel() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pIntent = PendingIntent.getBroadcast(
            context,
            0, // not used
            Intent(context, ClipboardClearBroadcastReceiver::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )!!
        alarmManager.cancel(pIntent)
    }
}
