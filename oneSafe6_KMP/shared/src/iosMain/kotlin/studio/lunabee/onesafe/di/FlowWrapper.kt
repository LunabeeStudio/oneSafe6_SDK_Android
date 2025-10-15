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
 * Created by Lunabee Studio / Date - 7/16/2024 - for the oneSafe6 SDK.
 * Last modified 16/07/2024 14:11
 */

@file:Suppress("BackingPropertyNaming")

package studio.lunabee.onesafe.di

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FlowWrapper<T : Any> {
    private val _flow = MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun emit(value: T) {
        _flow.tryEmit(value)
    }

    fun flow(): Flow<T> = _flow.asSharedFlow()
}

class FlowNullableWrapper<T> {
    private val _flow = MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun emit(value: T) {
        _flow.tryEmit(value)
    }

    fun flow(): Flow<T> = _flow.asSharedFlow()
}

class FlowListWrapper<T : Any> {
    private val _flow = MutableSharedFlow<List<T>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun emit(value: List<T>) {
        _flow.tryEmit(value)
    }

    fun flow(): Flow<List<T>> = _flow.asSharedFlow()
}
