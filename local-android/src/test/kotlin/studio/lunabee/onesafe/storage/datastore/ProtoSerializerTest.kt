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
 * Created by Lunabee Studio / Date - 11/22/2023 - for the oneSafe6 SDK.
 * Last modified 11/22/23, 8:42 AM
 */

package studio.lunabee.onesafe.storage.datastore

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.storage.model.LocalAutoBackupError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class ProtoSerializerTest {

    @Test
    fun serialize_deserialize_test(): TestResult = runTest {
        val expected = LocalAutoBackupError(
            date = "date",
            code = "code",
            message = "message",
            source = AutoBackupMode.CloudOnly,
        )
        val serializer = ProtoSerializer(LocalAutoBackupError.default, typeOf<LocalAutoBackupError>())
        val output = ByteArrayOutputStream()
        serializer.writeTo(expected, output)
        val proto = output.toByteArray()
        val input = ByteArrayInputStream(proto)
        val actual = serializer.readFrom(input)

        assertEquals(expected, actual)
    }
}
