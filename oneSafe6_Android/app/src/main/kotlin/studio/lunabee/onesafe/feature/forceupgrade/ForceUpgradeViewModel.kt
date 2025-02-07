package studio.lunabee.onesafe.feature.forceupgrade

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.commonui.SplashScreenManager
import javax.inject.Inject

@HiltViewModel
class ForceUpgradeViewModel @Inject constructor(
    forceUpgradeRepository: ForceUpgradeRepository,
    val splashScreenManager: SplashScreenManager,
) : ViewModel() {

    val state: Flow<ForceUpgradeState> = forceUpgradeRepository.getForceUpgradeData().map { data ->
        val isForced = (data?.forceBuildNumber ?: 0) > BuildConfig.VERSION_CODE
        when {
            data == null -> ForceUpgradeState.Exit
            isForced || data.softBuildNumber > BuildConfig.VERSION_CODE -> {
                val strings = if (isForced) {
                    data.strings.forceUpgrade
                } else {
                    data.strings.softUpgrade
                }
                ForceUpgradeState.Screen(
                    isForced = isForced,
                    title = strings.title,
                    description = strings.description,
                    buttonLabel = strings.buttonLabel,
                )
            }
            else -> ForceUpgradeState.Exit
        }
    }
}
