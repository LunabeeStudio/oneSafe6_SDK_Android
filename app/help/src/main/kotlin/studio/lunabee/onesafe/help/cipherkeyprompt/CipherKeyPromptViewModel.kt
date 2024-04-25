package studio.lunabee.onesafe.help.cipherkeyprompt

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.dialog.OverEncryptionCanceledDialogState
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.SetDatabaseKeyUseCase
import javax.inject.Inject

private val logger = LBLogger.get<CipherKeyPromptViewModel>()

@HiltViewModel
class CipherKeyPromptViewModel @Inject constructor(
    private val loadingManager: LoadingManager,
    private val setDatabaseKeyUseCase: SetDatabaseKeyUseCase,
    private val finishSetupDatabaseEncryptionUseCase: FinishSetupDatabaseEncryptionUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<CipherKeyPromptUiState> = MutableStateFlow(CipherKeyPromptUiState.default())
    val uiState: StateFlow<CipherKeyPromptUiState> = _uiState.asStateFlow()

    private val _dialogState: MutableStateFlow<DialogState?> = MutableStateFlow(null)
    val dialogState: StateFlow<DialogState?> = _dialogState.asStateFlow()

    fun tryFinishSetupDatabaseEncryption(context: Context) {
        viewModelScope.launch {
            loadingManager.withLoading {
                val result = finishSetupDatabaseEncryptionUseCase().last().asResult()
                when (result) {
                    is LBResult.Failure -> {
                        logger.e("Failed to finish database setup", result.throwable)
                        showFinishSetupError(result.throwable, context)
                    }
                    is LBResult.Success -> {
                        when (result.successData) {
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Noop -> {
                                /* no-op */
                            }
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Success -> {
                                logger.i("Database encryption setup succeed")
                                OSProcessPhoenix.triggerRebirth(context)
                            }
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Canceled -> {
                                _dialogState.value = OverEncryptionCanceledDialogState(
                                    onClose = { OSProcessPhoenix.triggerRebirth(context) },
                                    onOpenDiscord = { openDiscord(context) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showFinishSetupError(error: Throwable?, context: Context) {
        _dialogState.value = ErrorDialogState(
            error = error,
            actions = listOf(
                DialogAction(
                    text = LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_button_discord),
                    onClick = { openDiscord(context) },
                ),
                DialogAction(
                    LbcTextSpec.StringResource(OSString.common_ok),
                    onClick = { _dialogState.value = null },
                ),
            ),
            dismiss = { _dialogState.value = null },
        )
    }

    private fun openDiscord(context: Context) {
        val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CommonUiConstants.ExternalLink.Discord))
        context.startActivity(discordIntent)
    }

    fun setKey(password: String) {
        _uiState.value = _uiState.value.copy(
            key = password,
            openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Idle,
        )
    }

    fun confirm() {
        viewModelScope.launch {
            loadingManager.startLoading()
            val result = setDatabaseKeyUseCase(uiState.value.key)
            when (result) {
                is LBResult.Failure -> {
                    loadingManager.stopLoading()
                    _uiState.value = _uiState.value.copy(
                        openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Error(result.throwable.description()),
                    )
                }
                is LBResult.Success ->
                    // Don't stop loading, let the app restart
                    _uiState.value = _uiState.value.copy(openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Success)
            }
        }
    }
}
