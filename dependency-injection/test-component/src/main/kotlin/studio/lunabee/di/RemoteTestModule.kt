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

package studio.lunabee.di

import com.lunabee.lblogger.LBLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.remote.api.ForceUpgradeApi
import studio.lunabee.onesafe.remote.datasource.ForceUpdateRemoteDatasourceImpl
import studio.lunabee.onesafe.repository.datasource.ForceUpdateRemoteDatasource

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RemoteModule::class],
)
object RemoteTestModule {
    @Provides
    fun provideKtorHttpClient(): HttpClient {
        val engine = MockEngine {
            respondError(
                status = HttpStatusCode.NotFound,
            )
        }
        return HttpClient(engine = engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
            install(plugin = Logging) {
                level = LogLevel.ALL

                this.logger = object : Logger {
                    private val delegate = LBLogger.get("HttpClient")
                    override fun log(message: String) {
                        delegate.v(message)
                    }
                }
            }
        }
    }

    @Provides
    fun provideForceUpdateRemoteDatasource(
        forceUpgradeApi: ForceUpgradeApi,
    ): ForceUpdateRemoteDatasource {
        return ForceUpdateRemoteDatasourceImpl(
            forceUpgradeApi = forceUpgradeApi,
        )
    }
}
