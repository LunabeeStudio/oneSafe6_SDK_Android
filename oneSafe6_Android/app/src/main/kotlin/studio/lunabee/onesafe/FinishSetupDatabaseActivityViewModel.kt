package studio.lunabee.onesafe

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.OverEncryptionCanceledDialogState
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.feature.dialog.FatalErrorDialogState
import javax.inject.Inject

private val logger = LBLogger.get<FinishSetupDatabaseActivityViewModel>()

@HiltViewModel
class FinishSetupDatabaseActivityViewModel @Inject constructor(
    private val finishSetupDatabaseEncryptionUseCase: FinishSetupDatabaseEncryptionUseCase,
    private val loadingManager: LoadingManager,
) : ViewModel() {

    init {
        loadingManager.startLoading()
    }

    private val _dialogState: MutableStateFlow<DialogState?> = MutableStateFlow(null)
    val dialogState: StateFlow<DialogState?> = _dialogState.asStateFlow()

    fun finishSetupDatabaseEncryption(activity: FinishSetupDatabaseActivity) {
        viewModelScope.launch {
            finishSetupDatabaseEncryptionUseCase().collect { result ->
                when (result) {
                    is LBFlowResult.Failure -> {
                        logger.e("Failed to finish database setup", result.throwable)
                        _dialogState.value = FatalErrorDialogState(
                            context = activity,
                            error = result.throwable,
                            closeDialog = { _dialogState.value = null },
                        )
                    }
                    is LBFlowResult.Loading -> {
                        /* no-op, show loading on create */
                    }
                    is LBFlowResult.Success -> {
                        when (result.successData) {
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Success -> {
                                logger.i("Database encryption setup succeed")
                                exitToMain(activity)
                            }
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Canceled -> {
                                _dialogState.value = OverEncryptionCanceledDialogState(
                                    onClose = { exitToMain(activity) },
                                    onOpenDiscord = { openDiscord(activity) },
                                )
                            }
                            FinishSetupDatabaseEncryptionUseCase.SuccessState.Noop -> {
                                // Unexpected as FinishSetupDatabaseActivity should only be launch during database setup
                                logger.e("Unexpected state ${result.successData}")
                                exitToMain(activity)
                            }
                        }
                    }
                }
                loadingManager.stopLoading()
            }
        }
    }

    private fun exitToMain(activity: FinishSetupDatabaseActivity) {
        val mainIntent = Intent(activity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(mainIntent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }

    private fun openDiscord(activity: FinishSetupDatabaseActivity) {
        val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CommonUiConstants.ExternalLink.Discord))
        activity.startActivity(discordIntent)
    }
}
