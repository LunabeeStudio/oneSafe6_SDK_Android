/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 9:51 AM
 */

package studio.lunabee.onesafe.jvm

import studio.lunabee.onesafe.error.OSDriveError
import kotlin.test.Test
import kotlin.test.assertEquals

class OSErrorExtTest {

    private val errorCode = OSDriveError.Code.entries.random()

    @Test
    fun unsafe_get_test() {
        val expected = OSDriveError(errorCode)
        val actual = errorCode.get()

        assertEquals(expected, actual)
    }

    @Test
    fun unsafe_get_message_test() {
        val expectedMessage = "my_message"
        val expected = OSDriveError(errorCode, message = expectedMessage)
        val actual = errorCode.get(message = expectedMessage)

        assertEquals(expected, actual)
    }

    @Test
    fun unsafe_get_cause_test() {
        val expectedCause = Exception("my_error")
        val expected = OSDriveError(errorCode, cause = expectedCause)
        val actual = errorCode.get(cause = expectedCause)

        assertEquals(expected, actual)
    }

    @Test
    fun unsafe_get_message_cause_test() {
        val expectedMessage = "my_message"
        val expectedCause = Exception("my_error")
        val expected = OSDriveError(errorCode, message = expectedMessage, cause = expectedCause)
        val actual = errorCode.get(message = expectedMessage, cause = expectedCause)

        assertEquals(expected, actual)
    }
}
