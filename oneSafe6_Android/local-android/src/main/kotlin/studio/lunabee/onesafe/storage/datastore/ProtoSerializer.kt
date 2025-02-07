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
 * Last modified 11/22/23, 3:22 PM
 */

package studio.lunabee.onesafe.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Generic [Serializer] for protobuf serialization
 */
@OptIn(ExperimentalSerializationApi::class)
class ProtoSerializer<T : Any> @PublishedApi internal constructor(
    override val defaultValue: T,
    private val type: KType,
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        @Suppress("UNCHECKED_CAST")
        return ProtoBuf.decodeFromByteArray(
            ProtoBuf.serializersModule.serializer(type),
            input.readBytes(),
        ) as T
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(
                serializer = ProtoBuf.serializersModule.serializer(type),
                value = t,
            ),
        )
    }

    companion object {
        /**
         * Create a [DataStore] for data [T] using the generic serializer [ProtoSerializer]
         *
         * @param context The application context
         * @param default The default value of the data stored in the DataStore
         * @param fileName The filename of the DataStore stored in data/files/datastore
         */
        inline fun <reified T : Any> dataStore(context: Context, default: T, fileName: String): DataStore<T> {
            return DataStoreFactory.create(
                serializer = ProtoSerializer(default, typeOf<T>()),
                produceFile = { context.dataStoreFile(fileName) },
            )
        }
    }
}
