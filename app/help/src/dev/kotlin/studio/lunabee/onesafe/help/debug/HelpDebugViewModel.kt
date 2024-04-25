package studio.lunabee.onesafe.help.debug

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.bubbles.domain.model.PlainContact
import studio.lunabee.onesafe.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.authentication.ChangePasswordUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.support.IncrementVisitForAskingForSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.help.debug.model.DebugDatabaseEncryptionSettings
import studio.lunabee.onesafe.help.debug.model.HelpDebugUiState
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.usecase.ClearAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.SynchronizeCloudBackupsUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.visits.AppVisitConstants
import studio.lunabee.onesafe.visits.OSAppVisit
import studio.lunabee.onesafe.visits.OSPreferenceTips
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

private val logger = LBLogger.get<HelpDebugViewModel>()

@HiltViewModel
internal class HelpDebugViewModel @Inject constructor(
    private val settings: OSAppSettings,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val createItemUseCase: CreateItemUseCase,
    private val clipboardClearUseCase: ClipboardClearUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val cloudBackupRepository: CloudBackupRepository,
    private val localBackupRepository: LocalBackupRepository,
    private val mainDatabase: MainDatabase,
    private val appVisit: OSAppVisit,
    private val synchronizeCloudBackupsUseCase: SynchronizeCloudBackupsUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    autoBackupErrorRepository: AutoBackupErrorRepository,
    private val supportOSRepository: SupportOSRepository,
    private val safeItemDao: SafeItemDao,
    private val safeItemRepository: SafeItemRepository,
    private val itemSettingsRepository: ItemSettingsRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val incrementVisitForAskingForSupportUseCase: IncrementVisitForAskingForSupportUseCase,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val prefsDataStore: DataStore<Preferences>,
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
    private val loadingManager: LoadingManager,
    private val databaseKeyRepository: DatabaseKeyRepository,
    @Suppress("StaticFieldLeak") @ApplicationContext private val context: Context,
    private val clearAutoBackupErrorUseCase: ClearAutoBackupErrorUseCase,
) : ViewModel() {

    val uiState: StateFlow<HelpDebugUiState> = combine(
        settings.materialYouSetting,
        databaseKeyRepository.getKeyFlow().map { it != null },
        autoBackupErrorRepository.getError(),
        itemSettingsRepository.itemOrdering,
        itemSettingsRepository.itemsLayoutSetting,
        settings.cameraSystemFlow,
    ) { values ->
        val isMaterialYouEnabled = values[0] as Boolean
        val databaseEncryption = values[1] as Boolean
        val autoBackupError = values[2] as AutoBackupError?
        val itemOrder = values[3] as ItemOrder
        val itemsLayoutSetting = values[4] as ItemsLayoutSettings
        val cameraSystem = values[5] as CameraSystem

        HelpDebugUiState(
            isMaterialYouEnabled = isMaterialYouEnabled,
            autoBackupError = autoBackupError,
            itemOrder = itemOrder,
            itemsLayoutSetting = itemsLayoutSetting,
            cameraSystem = cameraSystem,
            isSignUp = isSignUpUseCase(),
            databaseEncryptionSettings = DebugDatabaseEncryptionSettings.fromBoolean(enabled = databaseEncryption),
            mainDatabaseSize = mainDatabase.openHelper.databaseName?.let {
                context.getDatabasePath(it).length()
            } ?: -1L,
        )
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        HelpDebugUiState.default,
    )

    fun toggleMaterialYouSetting() {
        viewModelScope.launch { settings.toggleMaterialYouSetting() }
    }

    fun clearClipboard() {
        clipboardClearUseCase()
    }

    fun changePassword() {
        val newPassword = Char(Random.nextInt(97, 123))
        viewModelScope.launch {
            val result = changePasswordUseCase(charArrayOf(newPassword))
            when (result) {
                is LBResult.Failure -> showFeedback(result.throwable?.message ?: "error")
                is LBResult.Success -> showFeedback("New password: $newPassword")
            }
        }
    }

    fun autolock() {
        viewModelScope.launch {
            lockAppUseCase()
        }
    }

    fun deleteLocalBackups() {
        viewModelScope.launch {
            val backups = localBackupRepository.getBackups().map { it.data!! }
            localBackupRepository.delete(backups)
        }
    }

    fun cancelAutoBackup() {
        autoBackupWorkersHelper.cancel()
    }

    fun errorAutoBackup(source: AutoBackupMode?) {
        viewModelScope.launch {
            if (source != null) {
                val error = when (source) {
                    AutoBackupMode.Disabled -> Exception("Unknown error")
                    AutoBackupMode.LocalOnly -> OSImportExportError.Code.EXPORT_DATA_FAILURE.get()
                    AutoBackupMode.CloudOnly -> OSDriveError.Code.entries.random().get(cause = Exception("cause exception"))
                    AutoBackupMode.Synchronized -> Exception("Fail to synchronize backups")
                }
                autoBackupWorkersHelper.onBackupWorkerFails(
                    error = error,
                    runAttemptCount = 3,
                    errorSource = source,
                )
            } else {
                clearAutoBackupErrorUseCase.force()
            }
        }
    }

    fun fetchBackupList() {
        viewModelScope.launch {
            cloudBackupRepository.refreshBackupList().collect {
                logger.d(it.toString())
            }
        }
    }

    fun uploadBackup() {
        viewModelScope.launch {
            localBackupRepository.getBackups().firstOrNull()?.let { result ->
                val backup = result.data!!
                cloudBackupRepository.uploadBackup(backup).collect {
                    when (it) {
                        is LBFlowResult.Failure -> it.throwable?.let(logger::e)
                        is LBFlowResult.Loading,
                        is LBFlowResult.Success,
                        -> logger.d(it.toString())
                    }
                }
            } ?: logger.d("No backup to upload")
        }
    }

    fun deleteBackup() {
        cloudBackupRepository.refreshBackupList().transformResult { success ->
            emitAll(cloudBackupRepository.deleteBackup(success.successData.minBy { it.date }))
        }
            .onEach { logger.d(it.toString()) }
            .launchIn(viewModelScope)
    }

    fun synchronizeBackups() {
        synchronizeCloudBackupsUseCase.invoke()
            .onEach { logger.d(it.toString()) }
            .launchIn(viewModelScope)
    }

    suspend fun getOneSafeFolderUri(): URI? {
        return cloudBackupRepository.getCloudInfo().firstOrNull()?.folderURI
    }

    fun resetTutorialOSk() {
        viewModelScope.launch {
            appVisit.store(value = false, preferencesTips = OSPreferenceTips.HasDoneTutorialOpenOsk)
            appVisit.store(value = false, preferencesTips = OSPreferenceTips.HasDoneTutorialLockOsk)
        }
    }

    fun resetTips() {
        viewModelScope.launch {
            appVisit.store(value = false, preferencesTips = OSPreferenceTips.HasSeenItemEditionUrlToolTip)
            appVisit.store(value = false, preferencesTips = OSPreferenceTips.HasSeenItemEditionEmojiToolTip)
            appVisit.store(value = false, preferencesTips = OSPreferenceTips.HasSeenItemReadEditToolTip)
        }
    }

    fun resetOnboardingOSk() {
        viewModelScope.launch {
            val key = booleanPreferencesKey(AppVisitConstants.hasFinishOneSafeKOnBoarding)
            prefsDataStore.edit { preferences ->
                preferences[key] = false
            }
        }
    }

    fun resetCameraTips() {
        viewModelScope.launch {
            val key = booleanPreferencesKey(AppVisitConstants.hasHiddenCameraTips)
            prefsDataStore.edit { preferences ->
                preferences[key] = false
            }
        }
    }

    fun showSupportOS() {
        viewModelScope.launch {
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport - 1)
            supportOSRepository.setDismissInstant(null)
            supportOSRepository.setRatingInstant(null)
            incrementVisitForAskingForSupportUseCase()
        }
    }

    fun removeAllItems() {
        viewModelScope.launch {
            safeItemDao.removeByIds(safeItemDao.getAllSafeItemIds())
            iconRepository.deleteAll()
            fileRepository.deleteAll()
        }
    }

    fun setSetting(setting: Any) {
        viewModelScope.launch {
            when (setting) {
                is ItemOrder -> itemSettingsRepository.setItemOrdering(setting)
                is ItemsLayoutSettings -> itemSettingsRepository.setItemsLayoutSetting(setting)
                is CameraSystem -> settings.setCameraSystem(setting)
                else -> logger.e("Unsupported setting ${setting::class.simpleName}")
            }
        }
    }

    private suspend fun dumpDatabaseKey(): Boolean {
        val databaseKey = databaseKeyRepository.getKeyFlow().firstOrNull()
        databaseKey?.let {
            // FIXME The raw key export should be able the database with DBBrowser for SQLite, but it does not work
            //  https://github.com/sqlitebrowser/sqlitebrowser/discussions/3588
            val rawKeyString = it.asCharArray().joinToString("").lowercase()
            context.copyToClipBoard("0x$rawKeyString", LbcTextSpec.Raw("debug"))
            showFeedback("SQLCipher key copied")
        } ?: showFeedback("No SQLCipher key found")

        return databaseKey != null
    }

    fun createContact() {
        viewModelScope.launch {
            createContactUseCase(
                PlainContact(
                    id = UUID.randomUUID(),
                    name = "Dummy #${Random.nextInt(9999)}",
                    sharedKey = byteArrayOf(),
                    sharedConversationId = UUID.randomUUID(),
                    isUsingDeepLink = false,
                ),
            )
        }
    }

    fun resetAutoBackupEnabled() {
        viewModelScope.launch {
            autoBackupSettingsRepository.setEnableAutoBackupCtaState(CtaState.Hidden)
            autoBackupSettingsRepository.setEnableAutoBackupCtaState(CtaState.VisibleSince(Instant.now().minus(10, ChronoUnit.DAYS)))
        }
    }

    fun createRecursiveItem() {
        viewModelScope.launch {
            val item = createItemUseCase(
                name = "☣️ Recursive",
                parentId = null,
                isFavorite = true,
                icon = null,
                color = null,
                position = -100.0,
            ).data!!
            try {
                safeItemRepository.updateSafeItemParentId(item.id, item.id)
                showFeedback("❌ Error: no SQL exception thrown. Item created")
            } catch (e: SQLiteConstraintException) {
                safeItemDeletedRepository.removeItem(item.id)
                showFeedback("SQL throw constraint exception as expected ✅")
            } catch (e: Exception) {
                showFeedback("❌ Error: unexpected exception ${e.javaClass.simpleName}")
            }
        }
    }

    fun corruptFile() {
        File(context.filesDir, "files").listFiles()?.firstOrNull()?.let { file ->
            file.delete()
            file.outputStream().use { stream ->
                repeat(20) {
                    stream.write(Random.nextBytes(1024 * 1024))
                }
            }
            showFeedback(file.name)
        }
    }

    fun toggleLoading() {
        if (loadingManager.loadingState.value.isBlocking) {
            loadingManager.stopLoading()
        } else {
            loadingManager.startLoading()
        }
    }

    fun wipeDatabaseKey() {
        viewModelScope.launch {
            if (dumpDatabaseKey()) {
                databaseKeyRepository.removeKey()
                showFeedback("Database key clear 🧹")
            }
        }
    }

    private fun showFeedback(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}
