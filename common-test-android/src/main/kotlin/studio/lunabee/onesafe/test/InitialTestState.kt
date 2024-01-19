/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.onesafe.test

/**
 * Set the state of the app at the beginning of the test
 */
sealed interface InitialTestState {
    /**
     * Account is created but the master key is not loaded, [thingsToDoLoggedIn] is executed while logged in before.
     */
    data class SignedUp(
        val thingsToDoLoggedIn: (suspend () -> Unit)? = null,
    ) : InitialTestState

    /**
     * No data related to login on startup
     */
    data object SignedOut : InitialTestState

    /**
     * Account is created and the master key is loaded
     */
    data object LoggedIn : InitialTestState
}