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
 * Created by Lunabee Studio / Date - 8/1/2024 - for the oneSafe6 SDK.
 * Last modified 8/1/24, 11:37 AM
 */

package studio.lunabee.onesafe.feature.lock

import android.content.Context
import android.hardware.SensorManager
import com.squareup.seismic.ShakeDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.qualifier.AppScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(DelicateCoroutinesApi::class)
class AndroidShakeToLockManager @Inject constructor(
    private val lockAppUseCase: LockAppUseCase,
    @ApplicationContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
) {
    private val sd = ShakeDetector {
        appScope.launch {
            lockAppUseCase(true)
        }
    }

    operator fun invoke(enable: Boolean) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        when (enable) {
            true -> sd.start(sensorManager, SensorManager.SENSOR_DELAY_UI)
            false -> sd.stop()
        }
    }
}
