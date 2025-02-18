package studio.lunabee.onesafe.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class DismissBubblesHomeCardUseCase @Inject constructor(
    private val settingsRepository: SafeSettingsRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {

    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setBubblesHomeCardCtaState(safeId, CtaState.DismissedAt(Instant.now(clock)))
    }
}
