package studio.lunabee.onesafe.login.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.work.WorkManager
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.EncryptPasswordAutoDestructionUseCase
import studio.lunabee.onesafe.login.AutoDestructionWorker
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import javax.inject.Inject

private val log = LBLogger.get<LoginFromPasswordDelegate>()

interface LoginFromPasswordDelegate {
    fun loginFromPassword()
    fun setPasswordValue(value: TextFieldValue)
}

class LoginFromPasswordDelegateImpl @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val featureFlags: FeatureFlags,
    private val loginUiStateHolder: LoginUiStateHolder,
    private val workManager: WorkManager,
    private val encryptPasswordAutoDestructionUseCase: EncryptPasswordAutoDestructionUseCase,
) : LoginFromPasswordDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope(loginUiStateHolder) {

    override fun loginFromPassword() {
        val dataState = loginUiStateHolder.dataState ?: return
        coroutineScope.launch {
            val passwordString = dataState.currentPasswordValue.text
            val password = if (featureFlags.quickSignIn()) {
                passwordString.toCharArray().takeUnless { it.isEmpty() }
                    ?: charArrayOf('a') // quick signup
            } else {
                passwordString.toCharArray()
            }

            loginUiStateHolder.setUiState(
                dataState.copy(
                    loginResult = CommonLoginUiState.LoginResult.Loading,
                ),
            )

            val signInResult = loginUseCase(password)

            when (signInResult) {
                is LBResult.Success -> {
                    /* no-op, observe safeReadyUseCase */
                }
                is LBResult.Failure -> loginUiStateHolder.setUiState(
                    dataState.copy(
                        currentPasswordValue = TextFieldValue(),
                        loginResult = CommonLoginUiState.LoginResult.Error,
                    ),
                )
            }
            val encPassword = encryptPasswordAutoDestructionUseCase(password)
            when (encPassword) {
                is LBResult.Success -> AutoDestructionWorker.start(encPassword = encPassword.successData, workManager = workManager)
                is LBResult.Failure -> {
                    // Do nothing, not suppose to be visible on screen
                    log.e("Fail to encrypt password for auto destruction worker", encPassword.throwable)
                }
            }
        }
    }

    override fun setPasswordValue(value: TextFieldValue) {
        val dataState = loginUiStateHolder.dataState ?: return
        loginUiStateHolder.setUiState(
            dataState.copy(
                currentPasswordValue = value,
                loginResult = CommonLoginUiState.LoginResult.Idle,
            ),
        )
    }
}
