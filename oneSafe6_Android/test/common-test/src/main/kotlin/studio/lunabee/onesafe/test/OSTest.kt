/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.test

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName

/**
 * Use this class for any unit test that does not imply UI or dependency injection.
 * Example:
 * ```
 * class MyUnitTest: OSTest() {
 *      @Test
 *      fun my_unit_test() {
 *          assert(true)
 *      }
 * }
 * ```
 */
open class OSTest {
    @get:Rule
    val testName: TestName = TestName()

    @Before
    fun logStart() {
        println("++ Start ${testName.methodName}")
    }

    @After
    fun logEnd() {
        println("-- End ${testName.methodName}")
    }
}
