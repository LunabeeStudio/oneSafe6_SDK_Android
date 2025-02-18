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

package studio.lunabee.onesafe.debug

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.converter.DurationConverter
import studio.lunabee.onesafe.storage.converter.FileConverter
import studio.lunabee.onesafe.storage.converter.InstantConverter
import studio.lunabee.onesafe.storage.converter.ZonedDateTimeConverter
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeFile
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import java.util.UUID
import javax.inject.Singleton

@Dao
interface DevDao {
    @Query("SELECT * FROM SafeItem ORDER BY index_alpha")
    fun getAllSafeItemsFlow(): Flow<List<RoomSafeItem>>

    @Update
    suspend fun updateMessage(message: RoomMessage)

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC LIMIT 1")
    suspend fun getLastMessage(contactId: UUID): RoomMessage?

    @Query("SELECT * FROM Contact WHERE id IS :contactId")
    suspend fun getContact(contactId: UUID): RoomContact

    @Query("SELECT * FROM ContactLocalKey WHERE contact_id IS :contactId")
    suspend fun getContactKey(contactId: UUID): RoomContactKey

    @Update
    suspend fun updateContactKey(corruptedContactKey: RoomContactKey)

    @Query("SELECT * FROM Safe ORDER BY open_order ASC")
    fun getAllSafe(): Flow<List<RoomSafe>>

    @Query("SELECT COUNT(*) FROM SafeFile WHERE safe_id = :safeId AND file LIKE :startWith || '%'")
    fun getFilesCount(safeId: SafeId?, startWith: String): Flow<Int>
}

/**
 * Dummy database to make Room generates DevDao implementation
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMainDatabase(
        @ApplicationContext appContext: Context,
    ): DummyDevMainDatabase {
        return Room.databaseBuilder(
            appContext,
            DummyDevMainDatabase::class.java,
            "",
        ).build()
    }

    @Singleton
    @Provides
    fun provideDevDao(mainDatabase: MainDatabase): DevDao {
        return DevDao_Impl(mainDatabase)
    }
}

@TypeConverters(InstantConverter::class, FileConverter::class, ZonedDateTimeConverter::class, DurationConverter::class)
@Database(
    version = 1,
    entities = [
        RoomSafeItem::class,
        RoomMessage::class,
        RoomContact::class,
        RoomContactKey::class,
        RoomSafe::class,
        RoomSafeFile::class,
    ],
    exportSchema = false,
)
abstract class DummyDevMainDatabase : RoomDatabase() {
    abstract fun devItemDao(): DevDao
}
