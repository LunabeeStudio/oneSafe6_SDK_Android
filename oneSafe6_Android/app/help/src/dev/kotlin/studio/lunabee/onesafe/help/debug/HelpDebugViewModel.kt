package studio.lunabee.onesafe.help.debug

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.authentication.ChangePasswordUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.support.IncrementVisitForAskingForSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import studio.lunabee.onesafe.help.debug.model.DebugDatabaseEncryptionSettings
import studio.lunabee.onesafe.help.debug.model.HelpDebugUiState
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

private val logger = LBLogger.get<HelpDebugViewModel>()

@HiltViewModel
internal class HelpDebugViewModel @Inject constructor(
    getSettings: GetAppSettingUseCase,
    private val setSettings: SetAppSettingUseCase,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val mainDatabase: MainDatabase,
    private val appVisitRepository: AppVisitRepository,
    private val supportOSRepository: SupportOSRepository,
    private val safeItemDao: SafeItemDao,
    private val createContactUseCase: CreateContactUseCase,
    private val incrementVisitForAskingForSupportUseCase: IncrementVisitForAskingForSupportUseCase,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val loadingManager: LoadingManager,
    private val databaseKeyRepository: DatabaseKeyRepository,
    @Suppress("StaticFieldLeak") @ApplicationContext private val context: Context,
    private val safeRepository: SafeRepository,
) : ViewModel() {

    private val databaseKeyFlow = databaseKeyRepository.getKeyFlow()
        .map { it != null }
        .catch { err ->
            logger.e(err)
            emit(false)
        }

    val uiState: StateFlow<HelpDebugUiState> = combine(
        getSettings.materialYou(),
        databaseKeyFlow,
        getSettings.cameraSystemFlow(),
    ) { values ->
        val isMaterialYouEnabled = values[0] as Boolean
        val databaseEncryption = values[1] as Boolean
        val cameraSystem = values[2] as CameraSystem

        HelpDebugUiState(
            isMaterialYouEnabled = isMaterialYouEnabled,
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
        viewModelScope.launch { setSettings.toggleMaterialYou() }
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
            lockAppUseCase(true)
        }
    }

    fun resetTutorialOSk() {
        viewModelScope.launch {
            appVisitRepository.setHasDoneTutorialOpenOsk(false)
            appVisitRepository.setHasDoneTutorialLockOsk(false)
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
            safeRepository.getAllSafeOrderByLastOpenAsc().forEach { safe ->
                safeItemDao.removeByIds(safeItemDao.getAllSafeItemIds(safe.id))
                iconRepository.getIcons(safe.id).forEach { it.delete() }
                fileRepository.getFiles(safe.id).forEach { it.delete() }
            }
        }
    }

    private suspend fun dumpDatabaseKey(): Boolean {
        return kotlin.runCatching {
            val databaseKey = databaseKeyRepository.getKeyFlow().firstOrNull()
            databaseKey?.let {
                // FIXME The raw key export should be able the database with DBBrowser for SQLite, but it does not work
                //  https://github.com/sqlitebrowser/sqlitebrowser/discussions/3588
                val rawKeyString = it.asCharArray().joinToString("").lowercase()
                context.copyToClipBoard("0x$rawKeyString", LbcTextSpec.Raw("debug"))
                showFeedback("SQLCipher key copied")
            } ?: showFeedback("No SQLCipher key found")

            databaseKey != null
        }.getOrNull() ?: false
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
