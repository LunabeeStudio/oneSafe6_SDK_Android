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

package studio.lunabee.onesafe.storage.datastore

import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.storage.OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto
import java.io.InputStream
import java.io.OutputStream

object PasswordGeneratorConfigSerializer : Serializer<PasswordGeneratorConfigProto> {
    override val defaultValue: PasswordGeneratorConfigProto = PasswordGeneratorConfigProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PasswordGeneratorConfigProto {
        try {
            return PasswordGeneratorConfigProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw OSStorageError(OSStorageError.Code.PROTO_DATASTORE_READ_ERROR, cause = exception)
        }
    }

    override suspend fun writeTo(t: PasswordGeneratorConfigProto, output: OutputStream) {
        t.writeTo(output)
    }
}
