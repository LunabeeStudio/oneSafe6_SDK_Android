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
 * Created by Lunabee Studio / Date - 3/18/2024 - for the oneSafe6 SDK.
 * Last modified 18/03/2024 09:18
 */

package studio.lunabee.onesafe.visits

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

sealed class OSPreferenceTips<T>(
    internal val preferencesKey: Preferences.Key<T>,
    internal val defaultValue: T,
) {
    data object HasVisitedLogin : OSPreferenceTips<Boolean>(
        preferencesKey = hasVisitedLoginKey,
        defaultValue = AppVisitConstants.hasVisitedLoginDefault,
    )

    data object HasDoneTutorialOpenOsk : OSPreferenceTips<Boolean>(
        preferencesKey = hasDoneTutorialOpenOskKey,
        defaultValue = AppVisitConstants.hasDoneTutorialOpenOskDefault,
    )

    data object HasDoneTutorialLockOsk : OSPreferenceTips<Boolean>(
        preferencesKey = hasDoneTutorialLockOskKey,
        defaultValue = AppVisitConstants.hasDoneTutorialLockOskDefault,
    )
}

private val hasVisitedLoginKey = booleanPreferencesKey(AppVisitConstants.hasVisitedLoginKey)
private val hasDoneTutorialOpenOskKey = booleanPreferencesKey(AppVisitConstants.hasDoneTutorialOpenOsk)
private val hasDoneTutorialLockOskKey = booleanPreferencesKey(AppVisitConstants.hasDoneTutorialLockOsk)
