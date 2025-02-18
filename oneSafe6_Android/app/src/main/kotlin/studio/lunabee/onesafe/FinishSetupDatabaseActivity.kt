package studio.lunabee.onesafe

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingView
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.AndroidEntryPoint
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.LoadingLottie
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.home.TextLogo
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge

private val logger = LBLogger.get<FinishSetupDatabaseActivity>()

@AndroidEntryPoint
class FinishSetupDatabaseActivity : ComponentActivity() {

    private val viewModel: FinishSetupDatabaseActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setView()
        killOtherProcesses()
        viewModel.finishSetupDatabaseEncryption(this)
    }

    private fun setView() {
        setContent {
            OSTheme(isMaterialYouSettingsEnabled = false) {
                OSScreen(testTag = UiConstants.TestTag.Screen.FinishSetupDatabaseErrorScreen) {
                    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
                    dialogState?.DefaultAlertDialog()

                    TextLogo(
                        modifier = Modifier
                            .width(OSDimens.LayoutSize.LoginLogoTextWidth)
                            .padding(OSDimens.SystemSpacing.Regular)
                            .align(Alignment.TopCenter),
                    )

                    if (dialogState == null) {
                        LoadingView(
                            contentDescription = stringResource(R.string.common_accessibility_loadingInProgress),
                        ) {}
                        LoadingLottie(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }

    private fun killOtherProcesses() {
        // Manually kill all other processes immediately to ensure nothing is running while finishing setup
        val am = getSystemService(ActivityManager::class.java)
        val runningProcesses = am.runningAppProcesses
        logger.i("${runningProcesses?.size} processes running")
        runningProcesses?.forEach { processInfo ->
            if (processInfo.pid != Process.myPid()) {
                Process.killProcess(processInfo.pid)
            }
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, FinishSetupDatabaseActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
