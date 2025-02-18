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
 * Created by Lunabee Studio / Date - 8/28/2024 - for the oneSafe6 SDK.
 * Last modified 8/28/24, 9:57 AM
 */

package studio.lunabee.onesafe.commonui

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplashScreenManager @Inject constructor() {
    var isAppReady: Boolean = false
}
