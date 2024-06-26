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
 * Created by Lunabee Studio / Date - 10/10/2023 - for the oneSafe6 SDK.
 * Last modified 10/10/23, 9:39 AM
 */

package studio.lunabee.onesafe.importexport

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.unit
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.onesafe.domain.qualifier.InternalBackupMimetype
import studio.lunabee.onesafe.domain.qualifier.RemoteDir
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.utils.CloudBackupDescriptionProvider
import java.io.InputStream
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import com.google.api.services.drive.model.File as DriveFile

private val logger = LBLogger.get<GoogleDriveEngine>()

// TODO thumbnail? https://developers.google.com/drive/api/guides/create-file?hl=fr#upload_thumbnails
@Singleton
class GoogleDriveEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @RemoteDispatcher private val dispatcher: CoroutineDispatcher,
    private val preferences: GoogleDriveEnginePreferences,
    private val cloudBackupDescriptionProvider: CloudBackupDescriptionProvider,
    @InternalBackupMimetype private val backupMimetype: String,
    @RemoteDir(RemoteDir.Type.Backups) private val remoteDirName: String,
    private val autoBackupErrorRepository: AutoBackupErrorRepository,
    private val clock: Clock,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
) : CloudBackupEngine {
    private lateinit var driveClient: Drive
    private val engineMtx = Mutex()

    init {
        runBlocking {
            engineMtx.withLock {
                preferences.selectedAccount.firstOrNull()?.let { name ->
                    val am = AccountManager.get(context)
                    val account = am.accounts.firstOrNull { account ->
                        account.name == name
                    }
                    if (account != null) {
                        if (account.type == GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) {
                            val accountCredential = googleAccountCredential(account)
                            setupDriveInstance(accountCredential)
                        } else {
                            // Unexpected error, the selected account is not a Google account
                            resetCloudBackup(
                                AutoBackupError(
                                    date = ZonedDateTime.now(clock),
                                    code = OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE.name,
                                    message = OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE.message,
                                    source = AutoBackupMode.CloudOnly,
                                ),
                            )
                        }
                    } else {
                        // Selected account does not exist (anymore)
                        resetCloudBackup(
                            AutoBackupError(
                                date = ZonedDateTime.now(clock),
                                code = OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT.name,
                                message = OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT.message,
                                source = AutoBackupMode.CloudOnly,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun resetCloudBackup(autoBackupError: AutoBackupError) {
        autoBackupErrorRepository.setError(autoBackupError)
        preferences.setSelectedAccount(null)
        preferences.setFolderUrl(null)
        preferences.setFolderId(null)
        autoBackupSettingsRepository.setCloudBackupSettings(false)
    }

    override fun getCloudInfo(): Flow<CloudInfo> = combine(
        preferences.folderUrl,
        preferences.selectedAccount,
    ) { folderUrl, selectedAccount ->
        CloudInfo(folderUrl?.let { URI.create(it) }, selectedAccount)
    }

    override fun setupAccount(accountName: String): Flow<LBFlowResult<Unit>> = flow {
        val account = getGoogleAccount(accountName) ?: throw OSDriveError(OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT)
        if (account.type != GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) throw OSDriveError(OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE)
        val accountCredential = googleAccountCredential(account)
        setupDriveInstance(accountCredential)
        if (preferences.selectedAccount.first() != account.name) {
            preferences.setSelectedAccount(account.name)
            emitAll(retrieveOrCreateOneSafeFolder().unit())
        } else {
            emit(LBFlowResult.Success(Unit))
        }
    }.onStart {
        emit(LBFlowResult.Loading())
    }

    private fun setupDriveInstance(accountCredential: GoogleAccountCredential) {
        driveClient = Drive
            .Builder(
                NetHttpTransport.Builder().build(),
                GsonFactory.getDefaultInstance(),
                accountCredential,
            )
            .setApplicationName(context.applicationInfo.name)
            .build()

        logger.i("Google drive engine successfully initialized")
    }

    private fun googleAccountCredential(account: Account): GoogleAccountCredential {
        val accountCredential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE),
        )
        accountCredential.selectedAccount = account
        return accountCredential
    }

    private suspend fun getGoogleAccount(accountName: String? = null): Account? {
        return (accountName ?: preferences.selectedAccount.firstOrNull())?.let { name ->
            val am = AccountManager.get(context)
            am.accounts.firstOrNull { account ->
                account.name == name
            } ?: throw OSDriveError(OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT)
        }
    }

    override fun fetchBackupList(): Flow<LBFlowResult<List<CloudBackup>>> {
        val query = driveClient.files().list()
            .setQ(
                "mimeType='$backupMimetype' and " +
                    "trashed=false and " +
                    appPropertiesIsOneSafe,
            )
            .setFields("files(id,name,mimeType,appProperties($appPropertiesOs6Date))")

        return query.executeAsFlow().mapResult { fileList ->
            fileList.files.mapNotNull { driveFile ->
                val timestamp = driveFile.appProperties[appPropertiesOs6Date]?.toLong()
                if (timestamp != null) {
                    CloudBackup(
                        remoteId = driveFile.id,
                        name = driveFile.name,
                        date = Instant.ofEpochMilli(timestamp),
                    )
                } else {
                    logger.e("Missing property $appPropertiesOs6Date on backup, ignore the backup")
                    null
                }
            }
        }.flowOn(dispatcher)
    }

    override fun uploadBackup(localBackup: LocalBackup): Flow<LBFlowResult<CloudBackup>> =
        getOneSafeFolder().transformResult { getOneSafeFolderResult ->
            val file = DriveFile().apply {
                parents = listOf(getOneSafeFolderResult.successData)
                name = localBackup.file.name
                mimeType = backupMimetype
                appProperties = mutableMapOf(
                    appPropertiesOs6AutoBackup to "true",
                    appPropertiesOs6Date to localBackup.date.toEpochMilli().toString(),
                )
                this.description = cloudBackupDescriptionProvider()
            }
            val content = FileContent(backupMimetype, localBackup.file)
            emitAll(
                driveClient.files().create(file, content)
                    .setFields("id")
                    .executeAsFlow()
                    .mapResult { driveFile ->
                        CloudBackup(
                            remoteId = driveFile.id,
                            name = localBackup.file.name,
                            date = localBackup.date,
                        )
                    },
            )
        }.flowOn(dispatcher)

    override fun getInputStream(remoteId: String): Flow<LBFlowResult<InputStream>> =
        driveClient.files().get(remoteId).executeMediaAsInputStreamAsFlow().flowOn(dispatcher)

    override fun deleteBackup(cloudBackup: CloudBackup): Flow<LBFlowResult<Unit>> =
        driveClient.files().delete(cloudBackup.remoteId).executeAsFlow().unit().flowOn(dispatcher)

    private fun getOneSafeFolder(): Flow<LBFlowResult<String>> = flow {
        engineMtx.withLock {
            val folderId = preferences.folderId.firstOrNull()
            if (folderId != null) {
                val driveFlow = driveClient.files().get(folderId)
                    .setFields("trashed")
                    .executeAsFlow()
                    .transformResult(
                        transformError = {
                            if (it.throwable.osCode() == OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND) {
                                logger.v("Stored folder $folderId not found, create a new one")
                                preferences.setFolderId(null)
                                emitAll(retrieveOrCreateOneSafeFolder())
                            } else {
                                emit(LBFlowResult.Failure(it.throwable))
                            }
                        },
                        transform = {
                            if (it.successData.trashed) {
                                logger.v("Stored folder $folderId is trashed, don't use it anymore")
                                preferences.setFolderId(null)
                                emitAll(retrieveOrCreateOneSafeFolder())
                            } else {
                                logger.v("Use stored folder $folderId")
                                emit(LBFlowResult.Success(folderId))
                            }
                        },
                    )
                emitAll(driveFlow)
            } else {
                emitAll(retrieveOrCreateOneSafeFolder())
            }
        }
    }

    private fun retrieveOrCreateOneSafeFolder(): Flow<LBFlowResult<String>> {
        val query = driveClient.files().list()
            .setQ(
                "mimeType='$folderMimeType' and " +
                    "name='$remoteDirName' and " +
                    "trashed=false and " +
                    appPropertiesIsOneSafe,
            )
            .setFields("files($folderInfoFields)")

        return query.executeAsFlow().transformResult { getFolderResult ->
            val folder: DriveFile? = getFolderResult.successData.files.firstOrNull()
            if (folder != null) {
                logger.v("Found drive oneSafe folder ${folder.id}")
                emit(LBFlowResult.Success(folder))
            } else {
                logger.v("No drive oneSafe folder found, create new one")
                emitAll(createOneSafeFolder())
            }
        }.mapResult { folder ->
            logger.v("Save folder in prefs ${folder.webViewLink}")
            preferences.setFolderId(folder.id)
            preferences.setFolderUrl(folder.webViewLink)
            folder.id
        }.flowOn(dispatcher)
    }

    private fun createOneSafeFolder(): Flow<LBFlowResult<DriveFile>> {
        val newFolder = DriveFile().apply {
            name = remoteDirName
            mimeType = folderMimeType
            appProperties = mutableMapOf(
                appPropertiesOs6AutoBackup to "true",
            )
            description = "oneSafe 6 auto backups directory"
        }
        val query = driveClient.files().create(newFolder).setFields(folderInfoFields)
        return query.executeAsFlow().mapResult { driveFile ->
            driveFile
        }.flowOn(dispatcher)
    }

    companion object {
        private const val folderMimeType = "application/vnd.google-apps.folder"
        private const val folderInfoFields = "id,webViewLink"

        private const val appPropertiesOs6AutoBackup = "os6AutoBackup"
        private const val appPropertiesOs6Date = "os6Date"
        private const val appPropertiesIsOneSafe = "appProperties has { key='$appPropertiesOs6AutoBackup' and value='true' }"
    }
}
