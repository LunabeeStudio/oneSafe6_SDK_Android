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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 11/15/23, 4:49 PM
 */

package studio.lunabee.onesafe.importexport.model

import kotlin.test.Test
import java.io.File
import java.time.Instant
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BackupTest {

    private val file = File("")

    @Test
    fun sorted_test() {
        val backups = listOf(
            LocalBackup(
                date = Instant.ofEpochSecond(1),
                file = file,
            ),
            LocalBackup(
                date = Instant.ofEpochSecond(3),
                file = file,
            ),
            LocalBackup(
                date = Instant.ofEpochSecond(4),
                file = file,
            ),
            CloudBackup(
                remoteId = "",
                name = "",
                date = Instant.ofEpochSecond(2),
            ),
            CloudBackup(
                remoteId = "",
                name = "",
                date = Instant.ofEpochSecond(3),
            ),
        ).shuffled()

        val expected = listOf(
            LocalBackup(
                date = Instant.ofEpochSecond(4),
                file = file,
            ),
            LocalBackup(
                date = Instant.ofEpochSecond(3),
                file = file,
            ),
            CloudBackup(
                remoteId = "",
                name = "",
                date = Instant.ofEpochSecond(3),
            ),
            CloudBackup(
                remoteId = "",
                name = "",
                date = Instant.ofEpochSecond(2),
            ),
            LocalBackup(
                date = Instant.ofEpochSecond(1),
                file = file,
            ),
        )

        val actual = backups.sortedDescending()
        assertContentEquals(expected, actual)
    }

    @Test
    fun maxOf_date_test() {
        val oldest = LocalBackup(
            date = Instant.ofEpochSecond(0),
            file = file,
        )
        val latest = LocalBackup(
            date = Instant.ofEpochSecond(1),
            file = file,
        )

        val max = maxOf(oldest, latest)
        assertEquals(latest, max)
    }

    @Test
    fun maxOf_type_test() {
        val local = LocalBackup(
            date = Instant.ofEpochSecond(0),
            file = file,
        )
        val cloud = CloudBackup(
            date = Instant.ofEpochSecond(0),
            remoteId = "",
            name = "",
        )

        val max = maxOf(local, cloud)
        val max2 = maxOf(cloud, local)
        assertEquals(local, max)
        assertEquals(local, max2)
    }

    /**
     * Make sure we preserve compare equality behavior
     */
    @Test
    fun equals_test() {
        val local = LocalBackup(
            date = Instant.ofEpochSecond(0),
            file = File(""),
        )
        val local2 = LocalBackup(
            date = Instant.ofEpochSecond(0),
            file = File("2"),
        )

        assertEquals(local, maxOf(local, local2))
        assertEquals(local2, maxOf(local2, local))
    }
}
