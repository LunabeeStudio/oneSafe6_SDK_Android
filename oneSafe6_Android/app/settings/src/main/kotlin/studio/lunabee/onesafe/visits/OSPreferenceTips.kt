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
import androidx.datastore.preferences.core.intPreferencesKey

sealed class OSPreferenceTips<T>(
    internal val preferencesKey: Preferences.Key<T>,
    internal val defaultValue: T,
) {
    data object HasVisitedLogin : OSPreferenceTips<Boolean>(
        preferencesKey = hasVisitedLoginKey,
        defaultValue = AppVisitConstants.HasVisitedLoginDefault,
    )

    data object HasDoneTutorialOpenOsk : OSPreferenceTips<Boolean>(
        preferencesKey = hasDoneTutorialOpenOskKey,
        defaultValue = AppVisitConstants.HasDoneTutorialOpenOskDefault,
    )

    data object HasDoneTutorialLockOsk : OSPreferenceTips<Boolean>(
        preferencesKey = hasDoneTutorialLockOskKey,
        defaultValue = AppVisitConstants.HasDoneTutorialLockOskDefault,
    )

    data object AppVersion : OSPreferenceTips<Int>(
        preferencesKey = appVersionKey,
        defaultValue = AppVisitConstants.AppVersionDefault,
    )
}

private val hasVisitedLoginKey = booleanPreferencesKey(AppVisitConstants.HasVisitedLoginKey)
private val hasDoneTutorialOpenOskKey = booleanPreferencesKey(AppVisitConstants.HasDoneTutorialOpenOsk)
private val hasDoneTutorialLockOskKey = booleanPreferencesKey(AppVisitConstants.HasDoneTutorialLockOsk)
internal val appVersionKey = intPreferencesKey(AppVisitConstants.AppVersion)
