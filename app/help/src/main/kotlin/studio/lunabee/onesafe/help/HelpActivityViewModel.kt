package studio.lunabee.onesafe.help

import androidx.lifecycle.ViewModel
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.last
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase
import javax.inject.Inject

@HiltViewModel
class HelpActivityViewModel @Inject constructor(
    private val finishSetupDatabaseEncryptionUseCase: FinishSetupDatabaseEncryptionUseCase,
) : ViewModel() {
    suspend fun finishSetupDatabaseEncryption(): LBResult<FinishSetupDatabaseEncryptionUseCase.SuccessState> =
        finishSetupDatabaseEncryptionUseCase().last().asResult()
}
