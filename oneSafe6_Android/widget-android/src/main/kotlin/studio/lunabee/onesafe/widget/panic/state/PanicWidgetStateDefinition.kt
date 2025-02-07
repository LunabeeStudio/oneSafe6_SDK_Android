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
 * Created by Lunabee Studio / Date - 9/16/2024 - for the oneSafe6 SDK.
 * Last modified 16/09/2024 16:15
 */

package studio.lunabee.onesafe.widget.panic.state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal object PanicWidgetStateDefinition : GlanceStateDefinition<PanicWidgetState> {
    private const val DATA_STORE_FILENAME = "PANIC_WIDGET_DATASTORE_"

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<PanicWidgetState> {
        return DataStoreFactory.create(
            serializer = PanicWidgetStateSerializer,
            produceFile = { context.dataStoreFile(fileName = "$DATA_STORE_FILENAME$fileKey") },
        )
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(fileName = "$DATA_STORE_FILENAME$fileKey")
    }

    @OptIn(ExperimentalSerializationApi::class)
    object PanicWidgetStateSerializer : Serializer<PanicWidgetState> {
        override val defaultValue: PanicWidgetState = PanicWidgetState(isEnabled = false, isLoading = false)

        override suspend fun readFrom(input: InputStream): PanicWidgetState {
            return Json.decodeFromStream<PanicWidgetState>(input)
        }

        override suspend fun writeTo(t: PanicWidgetState, output: OutputStream) {
            Json.encodeToStream(t, output)
        }
    }
}
