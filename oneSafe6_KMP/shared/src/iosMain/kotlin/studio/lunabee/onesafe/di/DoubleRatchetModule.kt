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
 * Last modified 16/07/2024 09:27
 */

package studio.lunabee.onesafe.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import studio.lunabee.bubbles.repository.DoubleRatchetKeyRepositoryImpl
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.messaging.repository.DoubleRatchetDatasourceImpl

val doubleRatchetModule: Module = module {
    single<DoubleRatchetLocalDatasource> { DoubleRatchetDatasourceImpl(get(), get(), get(), get()) }
    single<DoubleRatchetKeyRepository> { DoubleRatchetKeyRepositoryImpl(get(), get()) }
    singleOf(::DoubleRatchetEngine)
}
