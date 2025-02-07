package studio.lunabee.onesafe.feature.screenshot

import android.app.Activity
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<ToggleScreenshotSettingUseCase>()

class ToggleScreenshotSettingUseCase @Inject constructor(
    private val appSettingsRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(activity: Activity): LBResult<Unit> = OSError.runCatching(logger) {
        appSettingsRepository.toggleAllowScreenshot(safeId = safeRepository.currentSafeId())
        activity.recreate()
    }
}
