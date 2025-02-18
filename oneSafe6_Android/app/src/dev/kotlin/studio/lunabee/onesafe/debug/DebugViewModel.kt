package studio.lunabee.onesafe.debug

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.mapValues
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.usecase.IncomingMessageState
import studio.lunabee.onesafe.FinishSetupDatabaseActivity
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.cryptography.android.AndroidKeyStoreEngine
import studio.lunabee.onesafe.debug.model.DebugDatabaseEncryptionSettings
import studio.lunabee.onesafe.debug.model.DebugSafeInfoData
import studio.lunabee.onesafe.debug.model.DebugUiState
import studio.lunabee.onesafe.debug.model.DevPlainItem
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.common.SafeIdProvider
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.UpdatePanicButtonWidgetUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.ChangePasswordUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.CreateDatabaseKeyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.StartSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.GenerateCryptoForNewSafeUseCase
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetItemSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.support.IncrementVisitForAskingForSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.feature.settings.personalization.ChangeIconUseCase
import studio.lunabee.onesafe.feature.settings.personalization.GetCurrentAliasUseCase
import studio.lunabee.onesafe.getOrThrow
import studio.lunabee.onesafe.ime.ui.ImeFeedbackManager
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.usecase.ClearAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.OpenAndroidInternalBackupStorageUseCase
import studio.lunabee.onesafe.importexport.usecase.SynchronizeCloudBackupsUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.migration.LoginAndMigrateUseCase
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.ui.extensions.toColor
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = LBLogger.get<DebugViewModel>()

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val appSettingsRepository: SafeSettingsRepository,
    getAppSettingUseCase: GetAppSettingUseCase,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val createItemUseCase: CreateItemUseCase,
    private val generateCryptoForNewSafeUseCase: GenerateCryptoForNewSafeUseCase,
    private val clipboardClearUseCase: ClipboardClearUseCase,
    private val editCryptoRepository: EditCryptoRepository,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val imeFeedbackManager: ImeFeedbackManager,
    private val cloudBackupRepository: CloudBackupRepository,
    private val localBackupRepository: LocalBackupRepository,
    private val mainDatabase: MainDatabase,
    private val synchronizeCloudBackupsUseCase: SynchronizeCloudBackupsUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    getAutoBackupErrorUseCase: GetAutoBackupErrorUseCase,
    private val supportOSRepository: SupportOSRepository,
    private val safeItemDao: SafeItemDao,
    private val safeItemRepository: SafeItemRepository,
    private val itemDecryptUseCase: ItemDecryptUseCase,
    private val itemSettingsRepository: ItemSettingsRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val devDao: DevDao,
    private val incrementVisitForAskingForSupportUseCase: IncrementVisitForAskingForSupportUseCase,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
    private val loadingManager: LoadingManager,
    private val databaseKeyRepository: DatabaseKeyRepository,
    private val setupDatabaseEncryptionUseCase: StartSetupDatabaseEncryptionUseCase,
    private val createDatabaseKeyUseCase: CreateDatabaseKeyUseCase,
    @Suppress("StaticFieldLeak") @ApplicationContext private val context: Context,
    private val clearAutoBackupErrorUseCase: ClearAutoBackupErrorUseCase,
    private val changeIconUseCase: ChangeIconUseCase,
    getCurrentAliasUseCase: GetCurrentAliasUseCase,
    @DatabaseName(DatabaseName.Type.CipherTemp) private val tempDbName: String,
    private val androidKeyStoreEngine: AndroidKeyStoreEngine,
    private val safeRepository: SafeRepository,
    private val safeIdProvider: SafeIdProvider,
    getItemSettingUseCase: GetItemSettingUseCase,
    private val setAppSettingUseCase: SetAppSettingUseCase,
    private val getDefaultSafeSettingsUseCase: DefaultSafeSettingsProvider,
    private val loginAndMigrateUseCase: LoginAndMigrateUseCase,
    private val appVisitRepository: AppVisitRepository,
    private val deleteSafeUseCase: DeleteSafeUseCase,
    private val openAndroidInternalBackupStorageUseCase: OpenAndroidInternalBackupStorageUseCase,
    val workManager: WorkManager,
    private val updatePanicButtonWidgetUseCase: UpdatePanicButtonWidgetUseCase,
) : ViewModel() {

    private val _plainItem: MutableStateFlow<DevPlainItem?> = MutableStateFlow(null)
    private val appIcon: AppIcon = getCurrentAliasUseCase()

    private val databaseKeyFlow = databaseKeyRepository.getKeyFlow()
        .map { it != null }
        .catch { err ->
            logger.e(err)
            emit(false)
        }

    private val flowFileCounter = flowFileCounter(File(context.filesDir, "files"))
    private val flowIconCounter = flowFileCounter(File(context.filesDir, "icons"))

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DebugUiState> = safeRepository.currentSafeIdFlow().flatMapLatest { currentSafeId ->
        combine(
            getAppSettingUseCase.materialYou(),
            databaseKeyFlow,
            getAutoBackupErrorUseCase(),
            _plainItem,
            devDao.getAllSafeItemsFlow().onStart { emit(emptyList()) },
            getItemSettingUseCase.itemOrdering(),
            getItemSettingUseCase.itemsLayout(),
            getAppSettingUseCase.cameraSystemFlow(),
            devDao.getAllSafe().mapValues { it.toSafeCrypto() },
            devDao.getFilesCount(currentSafeId, File(context.filesDir, "files").path).onStart { emit(-1) },
            devDao.getFilesCount(currentSafeId, File(context.filesDir, "icons").path).onStart { emit(-1) },
            flowFileCounter.flow,
            flowIconCounter.flow,
        ) { values ->
            val isMaterialYouEnabled = values[0] as Boolean
            val databaseEncryption = values[1] as Boolean
            val autoBackupError = values[2] as AutoBackupError?
            val plainItem = values[3] as DevPlainItem?
            val safeItems = values[4] as List<RoomSafeItem>
            val itemOrder = values[5] as ItemOrder
            val itemLayout = values[6] as ItemLayout
            val cameraSystem = values[7] as CameraSystem
            val safeCryptos = values[8] as List<SafeCrypto>
            val currentFilesCount = values[9] as Int
            val currentIconsCount = values[10] as Int
            val allFilesCount = values[11] as Int
            val allIconsCount = values[12] as Int

            val itemsText = safeItems
                .map { item ->
                    val name = item.encName?.let { itemDecryptUseCase(it, item.id, String::class) }?.data
                    "$name\t\t-> ${item.indexAlpha}"
                }
                .joinToString("\n")
                .takeIf { it.isNotEmpty() }
            DebugUiState(
                isMaterialYouEnabled = isMaterialYouEnabled,
                autoBackupError = autoBackupError,
                plainItem = plainItem,
                itemsText = itemsText?.let(LbcTextSpec::Raw),
                itemOrder = itemOrder,
                itemLayout = itemLayout,
                cameraSystem = cameraSystem,
                isSignUp = isSignUpUseCase(),
                databaseEncryptionSettings = DebugDatabaseEncryptionSettings.fromBoolean(enabled = databaseEncryption),
                itemCount = safeItems.size,
                mainDatabaseSize = mainDatabase.openHelper.databaseName?.let {
                    context.getDatabasePath(it).length()
                } ?: -1L,
                appIcon = appIcon,
                safeInfoData = DebugSafeInfoData(
                    safeCryptos = safeCryptos,
                    currentSafeId = currentSafeId,
                    fileCount = currentFilesCount to allFilesCount,
                    iconCount = currentIconsCount to allIconsCount,
                    switchSafe = ::switchSafe,
                    createSafe = ::createSafe,
                    deleteSafe = if (currentSafeId != null) ::deleteSafe else null,
                    allSafeId = safeRepository.getAllSafeId(),
                    deleteAllItems = currentSafeId?.let { fun() { removeAllItemsFromSafe(it) } },
                ),
            )
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        DebugUiState.default,
    )

    private fun switchSafe(safeId: SafeId?) {
        viewModelScope.launch {
            safeRepository.clearSafeId()
            safeId?.let { safeRepository.loadSafeId(safeId) }
        }
    }

    fun toggleMaterialYouSetting() {
        viewModelScope.launch { setAppSettingUseCase.toggleMaterialYou() }
    }

    suspend fun signUp() {
        doSignUp()
        appVisitRepository.setHasVisitedLogin(true)
    }

    suspend fun setupItemBenchmark() {
        withContext(Dispatchers.IO) {
            mainDatabase.clearAllTables()
            mainDatabase.openHelper.writableDatabase.isDatabaseIntegrityOk
            doSignUp()
            val benchCreateItemTime = measureTimeMillis {
                repeat(500) {
                    createItemUseCase("Item $it", null, false, iconSample, null, it.toDouble())
                }
            }.milliseconds
            logger.d("Create 500 items in $benchCreateItemTime")
        }
    }

    private suspend fun doSignUp() {
        val password = charArrayOf('a')
        generateCryptoForNewSafeUseCase(password.copyOf())
        finishSafeSetup()
        loginAndMigrateUseCase(password.copyOf()).getOrThrow()
    }

    fun clearClipboard(safeId: SafeId) {
        viewModelScope.launch {
            clipboardClearUseCase(safeId)
        }
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

    fun lockSafe() {
        viewModelScope.launch {
            lockAppUseCase(true)
        }
    }

    fun imeNotify() {
        imeFeedbackManager.sendIncomingMessageFeedback(LBResult.Success(IncomingMessageState.Enqueued))
    }

    fun deleteLocalBackups() {
        viewModelScope.launch {
            safeRepository.getAllSafeOrderByLastOpenAsc().forEach { safeCrypto ->
                val safeId = safeCrypto.id
                val backups = localBackupRepository.getBackups(safeId).map { it.data!! }
                localBackupRepository.delete(backups, safeId)
            }
        }
    }

    fun cancelAutoBackup() {
        viewModelScope.launch {
            autoBackupWorkersHelper.cancel(currentSafeId())
        }
    }

    fun errorAutoBackup(source: AutoBackupMode?) {
        viewModelScope.launch {
            val safeId = currentSafeId()
            if (source != null && safeId != null) {
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
                    safeId = safeId,
                )
            } else {
                clearAutoBackupErrorUseCase.force()
            }
        }
    }

    fun fetchBackupList() {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                cloudBackupRepository.refreshBackupList(safeId).collect {
                    logger.d(it.toString())
                }
            }
        }
    }

    fun uploadBackup() {
        viewModelScope.launch {
            val safeId = safeRepository.currentSafeId()
            localBackupRepository.getBackups(safeId).firstOrNull()?.let { result ->
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
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                cloudBackupRepository.refreshBackupList(safeId).transformResult { success ->
                    emitAll(cloudBackupRepository.deleteBackup(success.successData.minBy { it.date }))
                }
                    .collect { logger.d(it.toString()) }
            }
        }
    }

    fun synchronizeBackups() {
        viewModelScope.launch {
            val safeId = safeRepository.currentSafeId()
            synchronizeCloudBackupsUseCase.invoke(safeId)
                .collect { logger.d(it.toString()) }
        }
    }

    suspend fun getLastCloudBackupId(): String? {
        val safeId = safeRepository.currentSafeId()
        return cloudBackupRepository.getLatestBackup(safeId)?.id
    }

    suspend fun getOneSafeFolderUri(): URI? {
        return currentSafeId()?.let { cloudBackupRepository.getCloudInfoFlow(it).firstOrNull()?.folderURI }
    }

    fun resetTutorialOSk() {
        viewModelScope.launch {
            appVisitRepository.setHasDoneTutorialOpenOsk(false)
            appVisitRepository.setHasDoneTutorialLockOsk(false)
        }
    }

    fun resetTips() {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                appVisitRepository.setHasSeenItemEditionUrlToolTip(safeId, false)
                appVisitRepository.setHasSeenItemEditionEmojiToolTip(safeId, false)
                appVisitRepository.setHasSeenItemReadEditToolTip(safeId, false)
            }
        }
    }

    fun resetOnboardingOSk() {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                appVisitRepository.setHasFinishOneSafeKOnBoarding(safeId, false)
            }
        }
    }

    fun resetCameraTips() {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                appVisitRepository.setHasHiddenCameraTips(safeId, false)
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

    private fun removeAllItemsFromSafe(safeId: SafeId) {
        viewModelScope.launch {
            safeItemDao.removeByIds(safeItemDao.getAllSafeItemIds(safeId))
            iconRepository.deleteAll(safeId)
            fileRepository.deleteAll(safeId)
        }
    }

    fun setItemId(itemId: UUID?) {
        if (itemId == null) {
            _plainItem.value = null
        } else {
            viewModelScope.launch {
                val safeItem = safeItemRepository.getSafeItem(itemId)
                _plainItem.value = DevPlainItem(
                    id = safeItem.id,
                    name = safeItem.encName?.let { itemDecryptUseCase(it, safeItem.id, String::class).data },
                    parentId = safeItem.parentId,
                    isFavorite = safeItem.isFavorite,
                    updatedAt = safeItem.updatedAt,
                    position = safeItem.position,
                    iconId = safeItem.iconId,
                    color = safeItem.encColor?.let { itemDecryptUseCase(it, safeItem.id, String::class).data }?.toColor(),
                    deletedAt = safeItem.deletedAt,
                    deletedParentId = safeItem.deletedParentId,
                    indexAlpha = safeItem.indexAlpha,
                    createdAt = safeItem.createdAt,
                )
            }
        }
    }

    fun setSetting(setting: Any) {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                when (setting) {
                    is ItemOrder -> itemSettingsRepository.setItemOrdering(safeId, setting)
                    is ItemLayout -> itemSettingsRepository.setItemsLayout(safeId, setting)
                    is CameraSystem -> setAppSettingUseCase.setCameraSystem(setting)
                    is DebugDatabaseEncryptionSettings -> {
                        loadingManager.startLoading()
                        mainDatabase.close()
                        if (setting.enabled) {
                            setupDatabaseEncryptionUseCase(createDatabaseKeyUseCase())
                            dumpDatabaseKey()
                        } else {
                            setupDatabaseEncryptionUseCase(null)
                        }
                        if (setting.error) {
                            context.getDatabasePath(tempDbName).writeBytes(Random.nextBytes(1024 * 1024))
                        }
                        FinishSetupDatabaseActivity.launch(context)
                    }
                    is AppIcon -> changeIconUseCase(setting)
                    else -> logger.e("Unsupported setting ${setting::class.simpleName}")
                }
            }
        }
    }

    private suspend fun dumpDatabaseKey(): Boolean {
        val databaseKey = databaseKeyRepository.getKeyFlow().firstOrNull()
        databaseKey?.let {
            // FIXME The raw key export should be able to open the database with DBBrowser for SQLite, but it does not work
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
                    id = createRandomUUID(),
                    name = "Dummy #${Random.nextInt(9999)}",
                    sharedKey = byteArrayOf(),
                    sharedConversationId = createRandomUUID(),
                    sharingMode = MessageSharingMode.CypherText,
                ),
            )
        }
    }

    fun resetCta(type: ResetCtaType) {
        viewModelScope.launch {
            currentSafeId()?.let { safeId ->
                when (type) {
                    ResetCtaType.Backup -> {
                        autoBackupSettingsRepository.setEnableAutoBackupCtaState(safeId, CtaState.Hidden)
                        autoBackupSettingsRepository.setEnableAutoBackupCtaState(
                            safeId,
                            CtaState.VisibleSince(Instant.now().minus(10, ChronoUnit.DAYS)),
                        )
                    }
                    ResetCtaType.Bubbles -> {
                        appVisitRepository.setHasDoneOnBoardingBubbles(safeId, false)
                        appSettingsRepository.setBubblesHomeCardCtaState(
                            safeId,
                            CtaState.VisibleSince(Instant.now().minus(10, ChronoUnit.DAYS)),
                        )
                    }
                    ResetCtaType.Safe -> {
                        appSettingsRepository.setIndependentSafeInfoCtaState(
                            safeId,
                            CtaState.VisibleSince(Instant.now().minus(10, ChronoUnit.DAYS)),
                        )
                    }
                }
            }
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

    fun wipeEncryptedKeystoreKey() {
        viewModelScope.launch {
            androidKeyStoreEngine.removeSecretKey("5ce9163a-e77a-4966-8966-a575cf286608")
            showFeedback("Done 🧹")
        }
    }

    private fun showFeedback(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun corruptLastMessage(contactId: String) {
        viewModelScope.launch {
            devDao.getLastMessage(UUID.fromString(contactId))?.let { lastMessage ->
                val corruptedMessage = RoomMessage(
                    id = lastMessage.id,
                    contactId = lastMessage.contactId,
                    encSentAt = lastMessage.encSentAt.randomize(),
                    encContent = lastMessage.encContent.randomize(),
                    direction = lastMessage.direction,
                    order = lastMessage.order,
                    encChannel = lastMessage.encChannel?.randomize(),
                    isRead = lastMessage.isRead,
                    encSafeId = null,
                )
                devDao.updateMessage(corruptedMessage)
            }
        }
    }

    fun corruptContact(contactId: String) {
        viewModelScope.launch {
            val contactKey = devDao.getContactKey(UUID.fromString(contactId))
            val corruptedContactKey = RoomContactKey(
                contactId = contactKey.contactId,
                encLocalKey = contactKey.encLocalKey.randomize(),
            )
            devDao.updateContactKey(corruptedContactKey)
        }
    }

    private fun createSafe() {
        val password = Char(Random.nextInt(97, 123))
        viewModelScope.launch {
            lockAppUseCase(false)
            val result = generateCryptoForNewSafeUseCase(charArrayOf(password))
            when (result) {
                is LBResult.Success -> {
                    finishSafeSetup()
                    showFeedback("Safe password: $password")
                    logger.d("Safe password: $password")
                }
                is LBResult.Failure -> showFeedback(result.throwable?.message ?: "error")
            }
        }
    }

    private suspend fun finishSafeSetup() {
        val safeId = safeIdProvider()
        val cryptoSafe = editCryptoRepository.setMainCryptographicData()
        val safeCrypto = SafeCrypto(
            id = safeId,
            salt = cryptoSafe.salt,
            encTest = cryptoSafe.encTest,
            encIndexKey = cryptoSafe.encIndexKey,
            encBubblesKey = cryptoSafe.encBubblesKey,
            encItemEditionKey = cryptoSafe.encItemEditionKey,
            biometricCryptoMaterial = null,
            autoDestructionKey = null,
        )
        safeRepository.insertSafe(safeCrypto = safeCrypto, safeSettings = getDefaultSafeSettingsUseCase(), appVisit = AppVisit())
        updatePanicButtonWidgetUseCase()
    }

    private suspend fun currentSafeId(): SafeId? {
        return uiState.value.safeInfoData?.currentSafeId ?: safeRepository.getAllSafeId().firstOrNull()
    }

    suspend fun getAllSafeId(): List<SafeId> {
        return safeRepository.getAllSafeId()
    }

    fun deleteSafe() {
        viewModelScope.launch {
            deleteSafeUseCase()
        }
    }

    fun longLoading(duration: Duration) {
        viewModelScope.launch {
            loadingManager.withLoading {
                delay(duration)
            }
        }
    }

    fun openInternalBackupStorage(context: Context) {
        openAndroidInternalBackupStorageUseCase(context)
    }

    fun onPreventionDateUpdated(date: LocalDateTime) {
        viewModelScope.launch {
            val safeId = safeRepository.currentSafeId()
            appSettingsRepository.setPreventionWarningCtaState(
                safeId,
                CtaState.DismissedAt(date.toInstant(ZoneOffset.UTC)),
            )
        }
    }

    fun resetLastExportDate() {
        viewModelScope.launch {
            val safeId = safeRepository.currentSafeId()
            appSettingsRepository.setLastExportDate(LocalDateTime.now().minusMonths(4).toInstant(ZoneOffset.UTC), safeId)
        }
    }
}

enum class ResetCtaType {
    Backup, Bubbles, Safe
}
