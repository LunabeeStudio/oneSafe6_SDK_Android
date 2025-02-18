package studio.lunabee.onesafe

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lbloading.withLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.messaging.domain.usecase.ManageIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.ManagingIncomingMessageResultData
import studio.lunabee.onesafe.bubbles.ui.extension.getBase64FromMessage
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.usecase.authentication.CheckDatabaseAccessUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveOldItemsUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetItemSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.feature.snackbar.SeeBubblesConversationSnackbar
import studio.lunabee.onesafe.help.main.HelpActivity
import studio.lunabee.onesafe.messaging.ui.ProcessQueueDelegate
import studio.lunabee.onesafe.messaging.usecase.HandleBubblesFileMessageUseCase
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.usecase.ShakeToLockUseCase
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getAppSettingUseCase: GetAppSettingUseCase,
    private val removeOldItemsUseCase: RemoveOldItemsUseCase,
    private val manageIncomingMessageUseCase: ManageIncomingMessageUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    processQueueDelegate: ProcessQueueDelegate,
    private val osFeatureFrags: FeatureFlags,
    getItemSettingUseCase: GetItemSettingUseCase,
    private val checkDatabaseAccessUseCase: CheckDatabaseAccessUseCase,
    private val shakeToLockUseCase: ShakeToLockUseCase,
    private val getSecuritySettingUseCase: GetSecuritySettingUseCase,
    private val handleBubblesFileMessageUseCase: HandleBubblesFileMessageUseCase,
    private val loadingManager: LoadingManager,
) : ViewModel() {

    val isMaterialYouEnabled: StateFlow<Boolean> = getAppSettingUseCase.materialYou().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        false,
    )

    private val _isSafeReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSafeReady: StateFlow<Boolean> = _isSafeReady.asStateFlow()

    val itemStyleHolder: StateFlow<ItemStyleHolder> = getItemSettingUseCase.itemsLayout()
        .map { itemLayouts ->
            ItemStyleHolder.from(itemLayouts)
        }
        .stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            ItemStyleHolder(OSSafeItemStyle.Regular, OSSafeItemStyle.Large, ItemsLayout.Grid),
        )

    private val _navigation: MutableStateFlow<MainActivityNavigation?> = MutableStateFlow(null)
    val navigation: StateFlow<MainActivityNavigation?> = _navigation.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val _snackbarState = MutableStateFlow<SnackbarState?>(null)
    val snackbarState: StateFlow<SnackbarState?> = _snackbarState.asStateFlow()

    init {
        viewModelScope.launch {
            processQueueDelegate()
        }
        viewModelScope.launch {
            isSafeReadyUseCase.flow().collect { isReady ->
                _isSafeReady.value = isReady
                shakeToLockUseCase(isReady && getSecuritySettingUseCase.shakeToLock())
                if (!isReady) _dialogState.value = null
            }
        }
    }

    suspend fun waitCryptoDataReadyInMemory(): Unit = isSafeReadyUseCase.wait()

    fun removeOldItems() {
        viewModelScope.launch {
            removeOldItemsUseCase()
        }
    }

    private fun displayErrorDialog(error: Throwable?) {
        _dialogState.value = ErrorDialogState(
            error = error,
            dismiss = { _dialogState.value = null },
            actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun handleBubblesDeeplink(deeplink: Uri): ManagingIncomingMessageResultData? {
        if (osFeatureFrags.bubbles()) {
            val base64FromMessage = deeplink.getBase64FromMessage()
            try {
                val result: LBResult<ManagingIncomingMessageResultData> = manageIncomingMessageUseCase(
                    data = Base64.decode(base64FromMessage),
                    channel = null,
                )
                when (result) {
                    is LBResult.Success -> return result.successData
                    is LBResult.Failure -> displaySnackbarMessage(LbcTextSpec.StringResource(OSString.bubbles_decryptMessage_archive_error))
                }
            } catch (e: IllegalArgumentException) {
                displayErrorDialog(OSDomainError(OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64))
            }
        } else {
            displayBubbleNotActivatedDialog()
        }
        return null
    }

    fun handleFile(uri: Uri) {
        viewModelScope.launch {
            if (osFeatureFrags.bubbles()) {
                val result: LBResult<DecryptResult?> = handleBubblesFileMessageUseCase(uri).withLoading(loadingManager).last().asResult()
                when (result) {
                    is LBResult.Failure -> _navigation.value = MainActivityNavigation.ItemCreationFromFileUrl(uri)
                    is LBResult.Success -> handleDecryptResult(result.successData, uri)
                }
            }
        }
    }

    private fun handleDecryptResult(
        decryptResult: DecryptResult?,
        uri: Uri,
    ) {
        val error = decryptResult?.error
        when {
            error != null -> displaySnackbarMessage(error.error.description(LbcTextSpec.StringResource(OSString.error_defaultMessage)))
            decryptResult != null -> _snackbarState.value = SeeBubblesConversationSnackbar {
                _navigation.value = MainActivityNavigation.WriteMessage(decryptResult)
            }
            else -> _navigation.value = MainActivityNavigation.ItemCreationFromFileUrl(uri)
        }
    }

    private fun displayBubbleNotActivatedDialog() {
        _dialogState.value = object : DialogState {
            override val title = LbcTextSpec.StringResource(OSString.common_warning)
            override val message = LbcTextSpec.StringResource(OSString.bubbles_notActivated_message)
            override val dismiss = { _dialogState.value = null }
            override val actions = listOf(
                DialogAction.commonCancel(dismiss),
                DialogAction(
                    text = LbcTextSpec.StringResource(OSString.bubbles_notActivated_activateButton),
                    onClick = {
                        _dialogState.value = null
                        _navigation.value = MainActivityNavigation.BubblesSettings
                    },
                ),
            )
            override val customContent:
                @Composable
                (() -> Unit)? = null
        }
    }

    fun consumeNavigation() {
        _navigation.value = null
    }

    /**
     * Check if the database is accessible by opening it. On failure, launch the [HelpActivity].
     */
    suspend fun checkDatabaseAccess(): Boolean {
        val result = checkDatabaseAccessUseCase()
        return result is LBResult.Success
    }

    fun resetSnackbarState() {
        _snackbarState.value = null
    }

    fun displaySnackbarMessage(message: LbcTextSpec) {
        _snackbarState.value = MessageSnackBarState(message)
    }
}

sealed interface MainActivityNavigation {
    data object BubblesSettings : MainActivityNavigation
    data class ItemCreationFromFileUrl(val uri: Uri) : MainActivityNavigation
    data class WriteMessage(val result: DecryptResult) : MainActivityNavigation
}
