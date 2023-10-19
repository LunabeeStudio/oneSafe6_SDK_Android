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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.unit
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import timber.log.Timber
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import com.google.api.services.drive.model.File as DriveFile

// TODO thumbnail? https://developers.google.com/drive/api/guides/create-file?hl=fr#upload_thumbnails
@Singleton
class GoogleDriveEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @RemoteDispatcher private val dispatcher: CoroutineDispatcher,
    private val preferences: GoogleDriveEnginePreferences,
) : CloudBackupEngine {
    private lateinit var driveClient: Drive

    init {
        getGoogleAccount()?.let(::setupAccount)
    }

    override fun getCurrentDriveAccount(): String? = preferences.selectedAccount

    // TODO maybe temp fun. To see with settings UI.
    override suspend fun isAuthorized(): LBResult<Boolean> = withContext(dispatcher) {
        try {
            driveClient.files().list().setFields("files(id)").execute()
            LBResult.Success(true)
        } catch (e: Exception) {
            LBResult.Failure(e, false)
        }
    }

    // TODO maybe temp fun, we might refresh files to check if we are authorized. To see with settings UI.
    override suspend fun authorize(isAuthorized: Boolean): LBResult<Unit> {
        return if (isAuthorized) {
            if (!this::driveClient.isInitialized) throw OSDriveError(OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED)

            withContext(dispatcher) {
                try {
                    driveClient.files().list().setFields("files(id)").execute()
                    Timber.i("Google drive engine successfully authorized")
                    preferences.isDriveApiAuthorized = true
                    LBResult.Success(Unit)
                } catch (e: UserRecoverableAuthIOException) {
                    preferences.isDriveApiAuthorized = false
                    LBResult.Failure(e) // TODO map error
                } catch (e: Exception) {
                    preferences.isDriveApiAuthorized = false
                    LBResult.Failure(e)
                }
            }
        } else {
            preferences.isDriveApiAuthorized = false
            LBResult.Success(Unit)
        }
    }

    override fun setupAccount(accountName: String?) {
        val account = getGoogleAccount(accountName) ?: throw OSDriveError(OSDriveError.Code.UNEXPECTED_NULL_ACCOUNT)
        setupAccount(account)
    }

    private fun setupAccount(account: Account) {
        if (account.type != GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE) throw OSDriveError(OSDriveError.Code.WRONG_ACCOUNT_TYPE)

        val accountCredential = googleAccountCredential(account)

        driveClient = Drive
            .Builder(
                NetHttpTransport.Builder()
                    .build(),
                GsonFactory.getDefaultInstance(),
                accountCredential,
            )
            .setApplicationName(context.applicationInfo.name)
            .build()

        Timber.i("Google drive engine successfully initialized")
        preferences.selectedAccount = account.name
    }

    private fun googleAccountCredential(account: Account): GoogleAccountCredential {
        val accountCredential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE),
        )
        accountCredential.selectedAccount = account
        return accountCredential
    }

    private fun getGoogleAccount(accountName: String? = null): Account? {
        return (accountName ?: preferences.selectedAccount)?.let { name ->
            val am = AccountManager.get(context)
            am.accounts.firstOrNull { account ->
                account.name == name
            }
        }
    }

    override fun fetchBackupList(): Flow<LBFlowResult<List<CloudBackup>>> {
        val query = driveClient.files().list()
            .setQ(
                "mimeType='$backupMimeType' and " +
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
                    Timber.e("Missing property $appPropertiesOs6Date on backup, ignore the backup")
                    null
                }
            }
        }.flowOn(dispatcher)
    }

    override fun uploadBackup(localBackup: LocalBackup, description: String): Flow<LBFlowResult<CloudBackup>> =
        getOneSafeFolder().transformResult { getOneSafeFolderResult ->
            val file = DriveFile().apply {
                parents = listOf(getOneSafeFolderResult.successData)
                name = localBackup.file.name
                mimeType = backupMimeType
                appProperties = mutableMapOf(
                    appPropertiesOs6AutoBackup to "true",
                    appPropertiesOs6Date to localBackup.date.toEpochMilli().toString(),
                )
                this.description = description
            }
            val content = FileContent(backupMimeType, localBackup.file)
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

    override fun downloadBackup(cloudBackup: CloudBackup, target: File): Flow<LBFlowResult<LocalBackup>> = flow<LBFlowResult<LocalBackup>> {
        target.outputStream().use { output ->
            driveClient.files().get(cloudBackup.remoteId).executeMediaAndDownloadTo(output)
        }
        emit(LBFlowResult.Success(LocalBackup(cloudBackup.date, target)))
    }.onStart {
        emit(LBFlowResult.Loading())
    }.catch {
        emit(LBFlowResult.Failure(throwable = it))
    }.flowOn(dispatcher)

    override fun deleteBackup(cloudBackup: CloudBackup): Flow<LBFlowResult<Unit>> =
        driveClient.files().delete(cloudBackup.remoteId).executeAsFlow().unit().flowOn(dispatcher)

    private fun getOneSafeFolder(): Flow<LBFlowResult<String>> {
        val query = driveClient.files().list()
            .setQ(
                "mimeType='$folderMimeType' and " +
                    "name='$backupFolderName' and " +
                    "trashed=false and " +
                    appPropertiesIsOneSafe,
            )
            .setFields("files(id)")

        return query.executeAsFlow().transformResult { getFolderResult ->
            val folder = getFolderResult.successData.files.firstOrNull()
            if (folder != null) {
                emit(LBFlowResult.Success(folder.id))
            } else {
                emitAll(createOneSafeFolder())
            }
        }
    }

    private fun createOneSafeFolder(): Flow<LBFlowResult<String>> {
        val newFolder = DriveFile().apply {
            name = backupFolderName
            mimeType = folderMimeType
            appProperties = mutableMapOf(
                appPropertiesOs6AutoBackup to "true",
            )
            description = "oneSafe 6 auto backups directory"
        }
        val query = driveClient.files().create(newFolder).setFields("id")
        return query.executeAsFlow().mapResult { driveFile ->
            driveFile.id
        }
    }

    companion object {
        private const val backupFolderName = "oneSafe 6 auto-backups"
        private const val folderMimeType = "application/vnd.google-apps.folder"
        private const val backupMimeType = "application/zip"
        private const val appPropertiesOs6AutoBackup = "os6AutoBackup"
        private const val appPropertiesOs6Date = "os6Date"
        private const val appPropertiesIsOneSafe = "appProperties has { key='$appPropertiesOs6AutoBackup' and value='true' }"
    }
}
