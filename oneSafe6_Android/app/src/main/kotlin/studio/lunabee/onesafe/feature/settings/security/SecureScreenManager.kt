package studio.lunabee.onesafe.feature.settings.security

import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import javax.inject.Inject

class SecureScreenManager @Inject constructor(
    private val getAppSettingUseCase: GetAppSettingUseCase,
) {

    /**
     * Apply screen secure flag if needed. Must be invoke during activity onCreate, before setContent.
     */
    operator fun invoke(activity: FragmentActivity) {
        val secureScreen = runBlocking {
            getAppSettingUseCase.allowScreenshot().data == false
        }
        if (secureScreen) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
