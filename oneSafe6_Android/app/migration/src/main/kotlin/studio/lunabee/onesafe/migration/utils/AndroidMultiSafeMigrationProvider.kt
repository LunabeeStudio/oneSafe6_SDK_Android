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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:16 AM
 */

package studio.lunabee.onesafe.migration.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.cryptography.android.DatastoreEngine
import studio.lunabee.onesafe.cryptography.android.ProtoData
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.common.SafeIdProvider
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import studio.lunabee.onesafe.importexport.utils.AutoBackupErrorIdProvider
import studio.lunabee.onesafe.protobuf.toByteArrayOrNull
import studio.lunabee.onesafe.storage.datastore.ProtoSerializer
import studio.lunabee.onesafe.storage.migration.RoomMigration12to13
import studio.lunabee.onesafe.storage.model.RoomAppVisit
import studio.lunabee.onesafe.storage.utils.queryNumEntries
import studio.lunabee.onesafe.use
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private val logger = LBLogger.get<AndroidMultiSafeMigrationProvider>()

class AndroidMultiSafeMigrationProvider @Inject constructor(
    private val safeIdProvider: SafeIdProvider,
    private val encodedDataStore: DataStore<ProtoData>,
    private val preferencesDataStore: DataStore<Preferences>,
    private val defaultSafeSettingsProvider: DefaultSafeSettingsProvider,
    private val autoBackupErrorIdProvider: AutoBackupErrorIdProvider,
    @param:ApplicationContext private val context: Context,
    @param:DatastoreEngineProvider(DataStoreType.Encrypted) private val encDataStore: DatastoreEngine,
) : RoomMigration12to13.MultiSafeMigrationProvider {
    private val ctaDataStore = ProtoSerializer.dataStore(
        context = context,
        default = LegacyLocalCtaStateMap(emptyMap()),
        fileName = ctaDataStoreFilename,
    )
    private val encFilesDir: File = File(context.filesDir, "files")
    private val iconDir: File = File(context.filesDir, "icons")

    override suspend fun getSafeCrypto(db: SupportSQLiteDatabase): RoomMigration12to13.SafeCryptoMigration? {
        val data = encodedDataStore.data.firstOrNull()?.dataMap
        val masterSalt = data?.get(datastoreMasterSalt)?.toByteArrayOrNull()
        return if (masterSalt != null) {
            val testValue = data[datastoreMasterKeyTest]?.toByteArrayOrNull()!!
            val searchIndexKey = data[datastoreSearchIndexKey]?.toByteArrayOrNull()
            val itemEditionKey = data[datastoreItemEditionKey]?.toByteArrayOrNull()
            val bubblesKey = data[datastoreBubblesContactKey]?.toByteArrayOrNull()
            val encBiometricMasterKey = encDataStore.retrieveValue(BiometricDataStoreIvKey).firstOrNull()?.use { iv ->
                encDataStore.retrieveValue(BiometricDataStoreMasterKeyKey).firstOrNull()?.use { key ->
                    BiometricCryptoMaterial(iv, key)
                }
            }

            RoomMigration12to13.SafeCryptoMigration(
                id = safeIdProvider(),
                salt = masterSalt,
                encTest = testValue,
                encIndexKey = searchIndexKey,
                encBubblesKey = bubblesKey,
                encItemEditionKey = itemEditionKey,
                biometricCryptoMaterial = encBiometricMasterKey,
            )
        } else {
            // Make sure the database is really empty because returning null here will cause the migration to not copy back data during
            // tables migration
            val itemCount = queryNumEntries(db, "SafeItem")
            check(itemCount == 0) {
                "No master key/salt found but database contains items"
            }
            val contactCount = queryNumEntries(db, "Contact")
            check(contactCount == 0) {
                "No master key/salt found but database contains contacts"
            }
            null
        }
    }

    override suspend fun getSafeSettings(): RoomMigration12to13.SafeSettingsMigration {
        val default = defaultSafeSettingsProvider()
        return try {
            val ctaStore = ctaDataStore.data.firstOrNull()
            preferencesDataStore.data.firstOrNull()?.let { pref ->
                RoomMigration12to13.SafeSettingsMigration(
                    version = pref[intPreferencesKey(MigrationVersionKey)] ?: default.version,
                    materialYou = pref[booleanPreferencesKey(MaterialYouKey)] ?: default.materialYou,
                    automation = pref[booleanPreferencesKey(AutomationKey)] ?: default.automation,
                    displayShareWarning = pref[booleanPreferencesKey(DisplayShareWarningKey)] ?: default.displayShareWarning,
                    allowScreenshot = pref[booleanPreferencesKey(AllowScreenshotKey)] ?: default.allowScreenshot,
                    bubblesPreview = pref[booleanPreferencesKey(BubblesPreviewKey)] ?: default.bubblesPreview,
                    cameraSystem = pref[stringPreferencesKey(CameraSystemKey)]?.let { CameraSystem.valueOf(it) } ?: default.cameraSystem,
                    autoLockOSKHiddenDelay = pref[longPreferencesKey(AutoLockOSKHiddenDelayKey)]?.milliseconds
                        ?: default.autoLockOSKHiddenDelay,
                    verifyPasswordInterval = pref[stringPreferencesKey(VerifyPasswordIntervalKey)]?.let {
                        VerifyPasswordInterval.valueOf(it)
                    } ?: default.verifyPasswordInterval,
                    bubblesHomeCardCtaState = ctaStore?.get(BubblesHomeCardCtaStateKey)?.toCtaState() ?: default.bubblesHomeCardCtaState,
                    autoLockInactivityDelay = pref[longPreferencesKey(AutoLockInactivityDelayKey)]?.milliseconds
                        ?: default.autoLockInactivityDelay,
                    autoLockAppChangeDelay = pref[longPreferencesKey(AutoLockAppChangeDelayKey)]?.milliseconds
                        ?: default.autoLockAppChangeDelay,
                    clipboardDelay = pref[longPreferencesKey(ClipboardClearDelayKey)]?.milliseconds ?: default.clipboardDelay,
                    bubblesResendMessageDelay = pref[longPreferencesKey(BubblesResendMessageDelayKey)]?.milliseconds
                        ?: default.bubblesResendMessageDelay,
                    autoLockOSKInactivityDelay = pref[longPreferencesKey(AutoLockOSKInactivityDelayKey)]?.milliseconds
                        ?: default.autoLockOSKInactivityDelay,
                    autoBackupEnabled = pref[booleanPreferencesKey(AutoBackupEnabledKey)] ?: default.autoBackupEnabled,
                    autoBackupFrequency = pref[longPreferencesKey(AutoBackupFrequencyKey)]?.milliseconds ?: default.autoBackupFrequency,
                    autoBackupMaxNumber = pref[intPreferencesKey(AutoBackupMaxNumberKey)] ?: default.autoBackupMaxNumber,
                    cloudBackupEnabled = pref[booleanPreferencesKey(CloudBackupEnabledKey)] ?: default.cloudBackupEnabled,
                    keepLocalBackupEnabled = pref[booleanPreferencesKey(KeepLocalBackupEnabledKey)] ?: default.keepLocalBackupEnabled,
                    itemOrdering = pref[stringPreferencesKey(ItemOrderingKey)]?.let { ItemOrder.valueOf(it) } ?: default.itemOrdering,
                    itemLayout = pref[stringPreferencesKey(ItemLayoutKey)]?.let { ItemLayout.valueOf(it) } ?: default.itemLayout,
                    enableAutoBackupCtaState = ctaStore?.get(EnableAutoBackupCtaStateKey)?.toCtaState() ?: default.enableAutoBackupCtaState,
                    lastPasswordVerification = pref[longPreferencesKey(LastPasswordVerificationKey)]?.let {
                        Instant.ofEpochMilli(it)
                    } ?: default.lastPasswordVerification,
                    independentSafeInfoCtaState = default.independentSafeInfoCtaState,
                )
            } ?: RoomMigration12to13.SafeSettingsMigration(default)
        } catch (t: Throwable) {
            logger.e("Settings migration failed", t)
            RoomMigration12to13.SafeSettingsMigration(default)
        }
    }

    override suspend fun getAppVisit(): RoomAppVisit {
        return try {
            val hasFinishOneSafeKOnBoardingKey = booleanPreferencesKey(HasFinishOneSafeKOnBoarding)
            val hasDoneOnBoardingBubblesKey = booleanPreferencesKey(HasDoneOnBoardingBubbles)
            val hasHiddenCameraTipsKey = booleanPreferencesKey(HasHiddenCameraTips)
            val hasSeenItemEditionUrlToolTipKey = booleanPreferencesKey(HasSeenItemEditionUrlToolTip)
            val hasSeenItemEditionEmojiToolTipKey = booleanPreferencesKey(HasSeenItemEditionEmojiToolTip)
            val hasSeenItemReadEditToolTipKey = booleanPreferencesKey(HasSeenItemReadEditToolTip)
            val prefs = preferencesDataStore.data.firstOrNull()
            RoomAppVisit(
                hasFinishOneSafeKOnBoarding = prefs?.get(hasFinishOneSafeKOnBoardingKey) ?: HasFinishOneSafeKOnBoardingDefault,
                hasDoneOnBoardingBubbles = prefs?.get(hasDoneOnBoardingBubblesKey) ?: HasDoneOnBoardingBubblesDefault,
                hasHiddenCameraTips = prefs?.get(hasHiddenCameraTipsKey) ?: HasHiddenCameraTipsDefault,
                hasSeenItemEditionUrlToolTip = prefs?.get(hasSeenItemEditionUrlToolTipKey) ?: HasSeenItemEditionUrlToolTipDefault,
                hasSeenItemEditionEmojiToolTip = prefs?.get(hasSeenItemEditionEmojiToolTipKey) ?: HasSeenItemEditionEmojiToolTipDefault,
                hasSeenItemReadEditToolTip = prefs?.get(hasSeenItemReadEditToolTipKey) ?: HasSeenItemReadEditToolTipDefault,
                hasSeenDialogMessageSaveConfirmation = false,
            )
        } catch (t: Throwable) {
            logger.e("AppVisit migration failed", t)
            RoomAppVisit(
                hasFinishOneSafeKOnBoarding = HasFinishOneSafeKOnBoardingDefault,
                hasDoneOnBoardingBubbles = HasDoneOnBoardingBubblesDefault,
                hasHiddenCameraTips = HasHiddenCameraTipsDefault,
                hasSeenItemEditionUrlToolTip = HasSeenItemEditionUrlToolTipDefault,
                hasSeenItemEditionEmojiToolTip = HasSeenItemEditionEmojiToolTipDefault,
                hasSeenItemReadEditToolTip = HasSeenItemReadEditToolTipDefault,
                hasSeenDialogMessageSaveConfirmation = false,
            )
        }
    }

    override suspend fun getDriveSettings(): GoogleDriveSettings? {
        return try {
            preferencesDataStore.data.firstOrNull()?.let { pref ->
                GoogleDriveSettings(
                    selectedAccount = pref[stringPreferencesKey(DriveSelectedAccountKey)],
                    folderId = pref[stringPreferencesKey(DriveFolderIdKey)],
                    folderUrl = pref[stringPreferencesKey(DriveFolderUrlKey)],
                )
            }
        } catch (t: Throwable) {
            logger.e("DriveSettings migration failed", t)
            null
        }
    }

    override suspend fun getAutoBackupError(): RoomMigration12to13.AutoBackupErrorMigration? {
        return try {
            val defaultError = LegacyLocalAutoBackupError()
            val autoBackupErrorDataStore = ProtoSerializer.dataStore(context, defaultError, backupErrorDataStoreFilename)
            val localAutoBackupError = autoBackupErrorDataStore.data.firstOrNull().takeIf { it != defaultError }

            localAutoBackupError?.let {
                RoomMigration12to13.AutoBackupErrorMigration(
                    id = autoBackupErrorIdProvider(),
                    date = ZonedDateTime.parse(it.date),
                    code = it.code,
                    message = it.message,
                    source = it.source,
                )
            }
        } catch (t: Throwable) {
            logger.e("AutoBackupError migration failed", t)
            null
        }
    }

    override suspend fun getFilesAndIcons(): List<File> {
        return encFilesDir.listFiles()?.toList().orEmpty() + iconDir.listFiles()?.toList().orEmpty()
    }

    override suspend fun onMigrationDone() {
        encodedDataStore.updateData {
            it.toBuilder().apply {
                removeData(datastoreMasterSalt)
                removeData(datastoreMasterKeyTest)
                removeData(datastoreSearchIndexKey)
                removeData(datastoreItemEditionKey)
                removeData(datastoreBubblesContactKey)
            }.build()
        }

        encDataStore.removeValue(BiometricDataStoreIvKey)
        encDataStore.removeValue(BiometricDataStoreMasterKeyKey)

        preferencesDataStore.updateData {
            it.toMutablePreferences().apply {
                remove(intPreferencesKey(MigrationVersionKey))
                remove(booleanPreferencesKey(MaterialYouKey))
                remove(booleanPreferencesKey(AutomationKey))
                remove(booleanPreferencesKey(DisplayShareWarningKey))
                remove(booleanPreferencesKey(AllowScreenshotKey))
                remove(booleanPreferencesKey(BubblesPreviewKey))
                remove(stringPreferencesKey(CameraSystemKey))
                remove(longPreferencesKey(AutoLockOSKHiddenDelayKey))
                remove(stringPreferencesKey(VerifyPasswordIntervalKey))
                remove(longPreferencesKey(AutoLockInactivityDelayKey))
                remove(longPreferencesKey(AutoLockAppChangeDelayKey))
                remove(longPreferencesKey(ClipboardClearDelayKey))
                remove(longPreferencesKey(BubblesResendMessageDelayKey))
                remove(longPreferencesKey(AutoLockOSKInactivityDelayKey))
                remove(booleanPreferencesKey(AutoBackupEnabledKey))
                remove(longPreferencesKey(AutoBackupFrequencyKey))
                remove(intPreferencesKey(AutoBackupMaxNumberKey))
                remove(booleanPreferencesKey(CloudBackupEnabledKey))
                remove(booleanPreferencesKey(KeepLocalBackupEnabledKey))
                remove(stringPreferencesKey(ItemOrderingKey))
                remove(stringPreferencesKey(ItemLayoutKey))
                remove(longPreferencesKey(LastPasswordVerificationKey))

                remove(booleanPreferencesKey(HasFinishOneSafeKOnBoarding))
                remove(booleanPreferencesKey(HasDoneOnBoardingBubbles))
                remove(booleanPreferencesKey(HasHiddenCameraTips))
                remove(booleanPreferencesKey(HasSeenItemEditionUrlToolTip))
                remove(booleanPreferencesKey(HasSeenItemEditionEmojiToolTip))
                remove(booleanPreferencesKey(HasSeenItemReadEditToolTip))

                remove(stringPreferencesKey(DriveSelectedAccountKey))
                remove(stringPreferencesKey(DriveFolderIdKey))
                remove(stringPreferencesKey(DriveFolderUrlKey))
            }
        }
        context.dataStoreFile(ctaDataStoreFilename).delete()
        context.dataStoreFile(backupErrorDataStoreFilename).delete()
    }

    companion object {
        private const val ctaDataStoreFilename: String = "64ed5309-0f38-4dac-8451-473247a6ea41"
        private const val backupErrorDataStoreFilename: String = "14e3ca9b-b9e9-4c2e-a836-cad49db25952"

        // Legacy datastore keys
        private const val datastoreMasterSalt = "b282a019-4337-45a3-8bf6-da657ad39a6c"
        private const val datastoreMasterKeyTest = "f9e3fa44-2f54-4246-8ba6-2784a18b63ea"
        private const val datastoreSearchIndexKey = "f0ab7671-5314-41dc-9f57-3c689180ab33"
        private const val datastoreItemEditionKey = "6f596059-24b8-429e-bfe4-daea05310de8"
        private const val datastoreBubblesContactKey = "2b96478c-cbd4-4150-b591-6fe5a4dffc5f"

        private const val BiometricDataStoreIvKey = "56819b7d-e14a-4952-bb1d-5b8d5a06568a"
        private const val BiometricDataStoreMasterKeyKey = "d548d24f-8ea4-4457-8698-63622cb91db9"

        private const val AutoLockInactivityDelayKey: String = "2174ec00-c7e9-11ed-afa1-0242ac120002"
        private const val AutoLockAppChangeDelayKey: String = "2a903ac4-c7e9-11ed-afa1-0242ac120002"
        private const val MaterialYouKey: String = "0a2cb720-38b2-45a5-9696-9916825ea98a"
        private const val AutomationKey: String = "cb5013ec-c345-11ed-afa1-0242ac120002"
        private const val AllowScreenshotKey: String = "a154e1ff-413a-4b39-add2-58ba9b073bc1"
        private const val ClipboardClearDelayKey: String = "32aff47b-b847-4f00-bf73-0a0756b8902d"
        private const val DisplayShareWarningKey: String = "bc2034fa-d941-4689-b418-c78ff9645eaf"
        private const val MigrationVersionKey: String = "6997642a-497b-4615-b431-058d131eee7e"
        private const val VerifyPasswordIntervalKey: String = "472e7ca7-0784-41a3-bbcf-f963c93503d2"
        private const val LastPasswordVerificationKey: String = "17b8f6fe-ecb4-408e-8315-257f6db69210"
        private const val BubblesPreviewKey: String = "1d9ac366-e506-49c6-bb2a-945c50016079"
        private const val BubblesResendMessageDelayKey: String = "bcbcc192-8d14-4351-9cf4-c44a3f59568a"
        private const val AutoLockOSKInactivityDelayKey: String = "18c6ef99-8d21-4977-bebc-97b0e6f88d1f"
        private const val AutoLockOSKHiddenDelayKey: String = "98ce4448-efb6-477d-92dc-1b1741bde987"
        private const val AutoBackupEnabledKey: String = "11349132-0642-433b-8324-c57acb9a1296"
        private const val AutoBackupFrequencyKey: String = "35917f5a-7ed8-45e0-8e13-908cf0a30c48"
        private const val AutoBackupMaxNumberKey: String = "55434ab2-c681-45de-83a4-93354587c6dc"
        private const val CameraSystemKey: String = "73467738-e9c8-44b1-a4b5-c41c1b84e199"
        private const val CloudBackupEnabledKey: String = "9ea24a6d-7e61-453f-9bdb-c431ced470cf"
        private const val KeepLocalBackupEnabledKey: String = "20b36aa9-3e7b-4982-8706-737016f10e87"
        private const val ItemOrderingKey: String = "f3bd5c99-f828-4a57-b1e5-c563c3021935"
        private const val ItemLayoutKey: String = "5716b94b-9079-4c2f-85e6-f0db4e6d0528"
        private const val EnableAutoBackupCtaStateKey: String = "1aa3c807-e989-4b63-ae1c-e020daa4a569"
        private const val BubblesHomeCardCtaStateKey: String = "fe6d86ee-7b5b-4d36-99d9-58517230e38c"
        private const val DriveSelectedAccountKey: String = "c76834a4-44ed-4985-ab5e-262cd1993a88"
        private const val DriveFolderIdKey: String = "fe7c959a-7f84-409e-a92e-d7d2406698d7"
        private const val DriveFolderUrlKey: String = "a793df5d-56fc-438c-bf00-275499b30bf9"

        private const val HasFinishOneSafeKOnBoarding: String = "dbc410db-e7a9-4bbc-b94f-fb552572243a"
        private const val HasFinishOneSafeKOnBoardingDefault: Boolean = false
        private const val HasDoneOnBoardingBubbles: String = "93d456b2-c8b5-4e35-a244-1b5a0752ed36"
        private const val HasDoneOnBoardingBubblesDefault: Boolean = false
        private const val HasHiddenCameraTips: String = "a34333b3-e332-4d8d-aa2f-c8316aa52ecc"
        private const val HasHiddenCameraTipsDefault: Boolean = false
        private const val HasSeenItemEditionUrlToolTip: String = "6f3deb8b-c235-43ba-9fc2-6b933d5b48e2"
        private const val HasSeenItemEditionUrlToolTipDefault: Boolean = false
        private const val HasSeenItemEditionEmojiToolTip: String = "a12678b2-7c5a-4b6c-9c4e-e78d9e7f9be1"
        private const val HasSeenItemEditionEmojiToolTipDefault: Boolean = false
        private const val HasSeenItemReadEditToolTip: String = "1ef71667-3b5f-414d-a3ba-7973b248e939"
        private const val HasSeenItemReadEditToolTipDefault: Boolean = false
    }
}
