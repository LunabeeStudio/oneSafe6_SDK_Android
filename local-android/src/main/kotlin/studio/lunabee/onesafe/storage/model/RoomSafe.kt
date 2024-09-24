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
 * Created by Lunabee Studio / Date - 6/6/2024 - for the oneSafe6 SDK.
 * Last modified 6/6/24, 3:44 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings

@Entity(
    tableName = "Safe",
    indices = [Index("open_order", unique = true)],
)
data class RoomSafe(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: SafeId,
    @Embedded(prefix = "crypto_")
    val crypto: RoomSafeCrypto,
    @Embedded(prefix = "setting_")
    val settings: RoomSafeSettings,
    @Embedded(prefix = "app_visit_")
    val appVisit: RoomAppVisit,
    @ColumnInfo(name = "version")
    val version: Int,
    @ColumnInfo(name = "open_order")
    val openOrder: Int,
) {

    fun toSafeCrypto(): SafeCrypto {
        return SafeCrypto(
            id = this.id,
            salt = this.crypto.salt,
            encTest = this.crypto.encTest,
            encIndexKey = this.crypto.encIndexKey,
            encBubblesKey = this.crypto.encBubblesKey,
            encItemEditionKey = this.crypto.encItemEditionKey,
            biometricCryptoMaterial = this.crypto.biometricCryptoMaterial,
            autoDestructionKey = this.crypto.autoDestructionKey,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafe

        if (id != other.id) return false
        if (crypto != other.crypto) return false
        if (settings != other.settings) return false
        if (appVisit != other.appVisit) return false
        if (version != other.version) return false
        if (openOrder != other.openOrder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + crypto.hashCode()
        result = 31 * result + settings.hashCode()
        result = 31 * result + appVisit.hashCode()
        result = 31 * result + version
        result = 31 * result + openOrder
        return result
    }

    companion object {
        fun fromDomain(
            safeCrypto: SafeCrypto,
            safeSettings: SafeSettings,
            appVisit: AppVisit,
            driveSettings: GoogleDriveSettings,
            openOrder: Int,
        ): RoomSafe = RoomSafe(
            id = safeCrypto.id,
            crypto = RoomSafeCrypto.fromSafeCrypto(safeCrypto),
            settings = RoomSafeSettings.fromSafeSettings(
                safeSettings = safeSettings,
                driveSettings = driveSettings,
            ),
            appVisit = RoomAppVisit.fromAppVisit(appVisit),
            version = safeSettings.version,
            openOrder = openOrder,
        )
    }
}
