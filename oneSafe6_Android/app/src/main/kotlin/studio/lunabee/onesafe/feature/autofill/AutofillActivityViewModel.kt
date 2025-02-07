package studio.lunabee.onesafe.feature.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import javax.inject.Inject

@HiltViewModel
class AutofillActivityViewModel @Inject constructor(
    getAppSettingUseCase: GetAppSettingUseCase,
    private val lockAppUseCase: LockAppUseCase,
) : ViewModel() {
    val isMaterialYouEnabled: StateFlow<Boolean> = getAppSettingUseCase.materialYou().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        false,
    )

    init {
        runBlocking {
            lockApp()
        }
    }

    suspend fun lockApp() {
        lockAppUseCase(false)
    }
}
