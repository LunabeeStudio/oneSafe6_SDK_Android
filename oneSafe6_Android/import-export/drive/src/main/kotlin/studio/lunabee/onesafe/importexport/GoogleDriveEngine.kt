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
 * Created by Lunabee Studio / Date - 6/26/2024 - for the oneSafe6 SDK.
 * Last modified 6/26/24, 8:14 AM
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
import com.google.api.services.drive.model.File
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.unit
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import studio.lunabee.importexport.datasource.CloudBackupEngine
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.InternalBackupMimetype
import studio.lunabee.onesafe.domain.qualifier.RemoteDir
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.importexport.data.GoogleDrivePreferencesRepository
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.utils.AutoBackupErrorIdProvider
import studio.lunabee.onesafe.importexport.utils.CloudBackupDescriptionProvider
import java.io.InputStream
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val logger = LBLogger.get<GoogleDriveEngine>()

// TODO <multisafe> testing

// TODO thumbnail? https://developers.google.com/drive/api/guides/create-file?hl=fr#upload_thumbnails
@Singleton
class GoogleDriveEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:RemoteDispatcher private val dispatcher: CoroutineDispatcher,
    private val drivePreferencesRepository: GoogleDrivePreferencesRepository,
    private val cloudBackupDescriptionProvider: CloudBackupDescriptionProvider,
    @param:InternalBackupMimetype private val backupMimetype: String,
    @param:RemoteDir(RemoteDir.Type.Backups) private val remoteDirName: String,
    private val autoBackupErrorRepository: AutoBackupErrorRepository,
    private val clock: Clock,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val autoBackupErrorIdProvider: AutoBackupErrorIdProvider,
) : CloudBackupEngine {
    private val driveClients: MutableMap<SafeId, Drive> = mutableMapOf()
    private val engineMtx = Mutex()

    private suspend fun resetCloudBackup(autoBackupError: AutoBackupError) {
        autoBackupErrorRepository.addError(autoBackupError)
        drivePreferencesRepository.setSelectedAccount(autoBackupError.safeId, null)
        drivePreferencesRepository.setFolderUrl(autoBackupError.safeId, null)
        drivePreferencesRepository.setFolderId(autoBackupError.safeId, null)
        autoBackupSettingsRepository.setCloudBackupEnabled(autoBackupError.safeId, false)
    }

    override fun getCloudInfoFlow(safeId: SafeId): Flow<CloudInfo> = combine(
        drivePreferencesRepository.folderUrlFlow(safeId),
        drivePreferencesRepository.selectedAccountFlow(safeId),
    ) { folderUrl, selectedAccount ->
        CloudInfo(folderUrl?.let { URI.create(it) }, selectedAccount)
    }

    override fun setupAccount(accountName: String, safeId: SafeId): Flow<LBFlowResult<Unit>> = flow {
        val account = getGoogleAccount(accountName, safeId) ?: throw OSDriveError(OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT)
        if (account.type != GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) throw OSDriveError(OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE)
        val accountCredential = googleAccountCredential(account)
        setupDriveInstance(accountCredential, safeId)
        if (drivePreferencesRepository.selectedAccountFlow(safeId).first() != account.name) {
            drivePreferencesRepository.setSelectedAccount(safeId, account.name)
            emitAll(retrieveOrCreateOneSafeFolder(safeId).unit())
        } else {
            emit(LBFlowResult.Success(Unit))
        }
    }.onStart {
        emit(LBFlowResult.Loading())
    }

    private fun setupDriveInstance(accountCredential: GoogleAccountCredential, safeId: SafeId) {
        driveClients[safeId] = Drive.Builder(
            NetHttpTransport.Builder().build(),
            GsonFactory.getDefaultInstance(),
            accountCredential,
        )
            .setApplicationName(context.applicationInfo.name)
            .build()!!

        logger.i("Google drive engine for ${accountCredential.selectedAccountName} successfully initialized")
    }

    private fun googleAccountCredential(account: Account): GoogleAccountCredential {
        val accountCredential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE),
        )
        accountCredential.selectedAccount = account
        return accountCredential
    }

    private suspend fun getGoogleAccount(accountName: String? = null, safeId: SafeId): Account? {
        return (accountName ?: drivePreferencesRepository.selectedAccount(safeId))?.let { name ->
            val am = AccountManager.get(context)
            am.accounts.firstOrNull { account ->
                account.name == name
            } ?: throw OSDriveError(OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT)
        }
    }

    override fun fetchBackupList(safeId: SafeId): Flow<LBFlowResult<List<CloudBackup>>> = flow {
        withDriveClient(safeId) { driveClient ->
            val query = driveClient.files().list()
                .setQ(
                    "mimeType='$backupMimetype' and " +
                        "trashed=false and " +
                        appPropertiesIsOneSafe,
                )
                .setFields("files(id,name,mimeType,appProperties($appPropertiesOs6Date,$appPropertiesOs6SafeId))")
            query.executeAsFlow().mapResult { fileList ->
                fileList.files.mapNotNull { driveFile ->
                    val timestamp = driveFile.appProperties[appPropertiesOs6Date]?.toLong()
                    val backupSafeId = driveFile.appProperties[appPropertiesOs6SafeId]?.let(::SafeId)
                    if (timestamp != null) {
                        CloudBackup(
                            remoteId = driveFile.id,
                            name = driveFile.name,
                            date = Instant.ofEpochMilli(timestamp),
                            safeId = backupSafeId.takeIf { it == safeId },
                        )
                    } else {
                        logger.e("Missing property $appPropertiesOs6Date on backup, ignore the backup")
                        null
                    }
                }
            }
        }
    }.flowOn(dispatcher)

    private suspend fun <T> FlowCollector<LBFlowResult<T>>.withDriveClient(
        safeId: SafeId,
        block: (Drive) -> Flow<LBFlowResult<T>>,
    ) {
        getDriveClient(safeId)?.let {
            emitAll(block(it))
        } ?: emit(LBFlowResult.Failure(OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED.get()))
    }

    private suspend fun getDriveClient(safeId: SafeId): Drive? {
        val drive = driveClients[safeId]
        return if (drive != null) {
            drive // get cached drive client
        } else {
            val driveAccount = drivePreferencesRepository.selectedAccount(safeId)
            val am = AccountManager.get(context)
            val account = am.accounts.firstOrNull { account ->
                account.name == driveAccount
            }
            if (account != null) {
                if (account.type == GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) {
                    val accountCredential = googleAccountCredential(account)
                    setupDriveInstance(accountCredential, safeId) // instantiate new drive client
                    driveClients[safeId]!!
                } else {
                    // Unexpected error, the selected account is not a Google account
                    resetCloudBackup(
                        AutoBackupError(
                            id = autoBackupErrorIdProvider(),
                            date = ZonedDateTime.now(clock),
                            code = OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE.name,
                            message = OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE.message,
                            source = AutoBackupMode.CloudOnly,
                            safeId = safeId,
                        ),
                    )
                    null
                }
            } else {
                // Selected account does not exist (anymore)
                resetCloudBackup(
                    AutoBackupError(
                        id = autoBackupErrorIdProvider(),
                        date = ZonedDateTime.now(clock),
                        code = OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT.name,
                        message = OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT.message,
                        source = AutoBackupMode.CloudOnly,
                        safeId = safeId,
                    ),
                )
                null
            }
        }
    }

    override fun uploadBackup(localBackup: LocalBackup): Flow<LBFlowResult<CloudBackup>> =
        getOneSafeFolder(localBackup.safeId).transformResult { getOneSafeFolderResult ->
            val file = File().apply {
                parents = listOf(getOneSafeFolderResult.successData)
                name = localBackup.file.name
                mimeType = backupMimetype
                appProperties = mutableMapOf(
                    appPropertiesOs6AutoBackup to "true",
                    appPropertiesOs6Date to localBackup.date.toEpochMilli().toString(),
                    appPropertiesOs6SafeId to localBackup.safeId.toString(),
                )
                this.description = cloudBackupDescriptionProvider()
            }
            val content = FileContent(backupMimetype, localBackup.file)
            withDriveClient(localBackup.safeId) { driveClient ->
                driveClient.files().create(file, content)
                    .setFields("id")
                    .executeAsFlow()
                    .mapResult { driveFile ->
                        CloudBackup(
                            remoteId = driveFile.id,
                            name = localBackup.file.name,
                            date = localBackup.date,
                            safeId = localBackup.safeId,
                        )
                    }
            }
        }.flowOn(dispatcher)

    override fun getInputStream(remoteId: String, safeId: SafeId): Flow<LBFlowResult<InputStream>> = flow {
        withDriveClient(safeId) { driveClient ->
            driveClient.files().get(remoteId).executeMediaAsInputStreamAsFlow()
        }
    }.flowOn(dispatcher)

    override fun deleteBackup(cloudBackup: CloudBackup): Flow<LBFlowResult<Unit>> = flow {
        val safeId = cloudBackup.safeId
        safeId?.let {
            withDriveClient(safeId) { driveClient ->
                driveClient.files().delete(cloudBackup.remoteId).executeAsFlow().unit()
            }
        } ?: emit(LBFlowResult.Failure(OSDriveError.Code.DRIVE_CANNOT_DELETE_BACKUP_WITHOUT_SAFE_ID.get()))
    }.flowOn(dispatcher)

    private fun getOneSafeFolder(safeId: SafeId): Flow<LBFlowResult<String>> = flow {
        engineMtx.withLock {
            val folderId = drivePreferencesRepository.folderId(safeId)
            withDriveClient(safeId) { driveClient ->
                if (folderId != null) {
                    driveClient.files().get(folderId)
                        .setFields("trashed")
                        .executeAsFlow()
                        .transformResult(
                            transformError = {
                                if (it.throwable.osCode() == OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND) {
                                    logger.v("Stored folder $folderId not found, create a new one")
                                    drivePreferencesRepository.setFolderId(safeId, null)
                                    emitAll(retrieveOrCreateOneSafeFolder(safeId))
                                } else {
                                    emit(LBFlowResult.Failure(it.throwable))
                                }
                            },
                            transform = {
                                if (it.successData.trashed) {
                                    logger.v("Stored folder $folderId is trashed, don't use it anymore")
                                    drivePreferencesRepository.setFolderId(safeId, null)
                                    emitAll(retrieveOrCreateOneSafeFolder(safeId))
                                } else {
                                    logger.v("Use stored folder $folderId")
                                    emit(LBFlowResult.Success(folderId))
                                }
                            },
                        )
                } else {
                    retrieveOrCreateOneSafeFolder(safeId)
                }
            }
        }
    }

    private fun retrieveOrCreateOneSafeFolder(safeId: SafeId): Flow<LBFlowResult<String>> = flow {
        withDriveClient(safeId) { driveClient ->
            val query = driveClient.files().list()
                .setQ(
                    "mimeType='$folderMimeType' and " +
                        "name='$remoteDirName' and " +
                        "trashed=false and " +
                        appPropertiesIsOneSafe,
                )
                .setFields("files($folderInfoFields)")

            query.executeAsFlow().transformResult { getFolderResult ->
                val folder: File? = getFolderResult.successData.files.firstOrNull()
                if (folder != null) {
                    logger.v("Found drive oneSafe folder ${folder.id}")
                    emit(LBFlowResult.Success(folder))
                } else {
                    logger.v("No drive oneSafe folder found, create new one")
                    emitAll(createOneSafeFolder(safeId))
                }
            }.mapResult { folder ->
                logger.v("Save folder in prefs ${folder.webViewLink}")
                drivePreferencesRepository.setFolderId(safeId, folder.id)
                drivePreferencesRepository.setFolderUrl(safeId, folder.webViewLink)
                folder.id
            }
        }
    }.flowOn(dispatcher)

    private fun createOneSafeFolder(safeId: SafeId): Flow<LBFlowResult<File>> = flow {
        val newFolder = File().apply {
            name = remoteDirName
            mimeType = folderMimeType
            appProperties = mutableMapOf(
                appPropertiesOs6AutoBackup to "true",
            )
            description = "oneSafe 6 auto backups directory"
        }
        withDriveClient(safeId) { driveClient ->
            val query = driveClient.files().create(newFolder).setFields(folderInfoFields)
            query.executeAsFlow().mapResult { driveFile ->
                driveFile
            }
        }
    }.flowOn(dispatcher)

    override suspend fun getFirstCloudFolderAvailable(): URI? {
        val am = AccountManager.get(context)
        val accounts = am.accounts
        val file = accounts.firstNotNullOfOrNull { account ->
            if (account.type == GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) {
                val accountCredential = googleAccountCredential(account)
                val drive = Drive.Builder(
                    NetHttpTransport.Builder().build(),
                    GsonFactory.getDefaultInstance(),
                    accountCredential,
                )
                    .setApplicationName(context.applicationInfo.name)
                    .build()

                val query = drive.files().list()
                    .setQ(
                        "mimeType='$folderMimeType' and " +
                            "name='$remoteDirName' and " +
                            "trashed=false and " +
                            appPropertiesIsOneSafe,
                    )
                    .setFields("files($folderInfoFields)")

                withContext(dispatcher) {
                    query.execute().files.firstOrNull()
                }
            } else {
                null
            }
        }
        return file?.webViewLink?.let { URI.create(it) }
    }

    companion object {
        private const val folderMimeType = "application/vnd.google-apps.folder"
        private const val folderInfoFields = "id,webViewLink"

        private const val appPropertiesOs6AutoBackup = "os6AutoBackup"
        private const val appPropertiesOs6Date = "os6Date"
        private const val appPropertiesIsOneSafe = "appProperties has { key='$appPropertiesOs6AutoBackup' and value='true' }"
        private const val appPropertiesOs6SafeId = "os6SafeId"
    }
}
