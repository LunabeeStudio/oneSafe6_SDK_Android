package studio.lunabee.onesafe.feature.exportbackup.exportdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.worker.ExportWorker
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportDataViewModel @Inject constructor(
    featureFlags: FeatureFlags,
    safeRepository: SafeRepository,
    private val workManager: WorkManager,
) : ViewModel() {
    private val minimumDelayFlow: Flow<Unit> = flow {
        // Will delay emitted result only if [createExportBackupArchiveContent] + [zipToolProvider]
        // take less than [minimumDelay]
        delay(AppConstants.Ui.DelayedLoading.DelayMinimumExport)
        emit(Unit)
    }

    val exportDataState: StateFlow<LBFlowResult<File>> = flow {
        val safeId = safeRepository.currentSafeId()
        val workerFlow = ExportWorker.start(
            workManager = workManager,
            setExpedited = featureFlags.backupWorkerExpedited(),
            safeId = safeId,
        )
        emitAll(workerFlow)
    }.combine(minimumDelayFlow) { result, _ ->
        result
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        LBFlowResult.Loading(),
    )
}
