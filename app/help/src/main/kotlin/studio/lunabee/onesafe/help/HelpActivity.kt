package studio.lunabee.onesafe.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge

private val logger = LBLogger.get<HelpActivity>()

@AndroidEntryPoint
open class HelpActivity : FragmentActivity() {

    val viewModel: HelpActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Try to run FinishSetupDatabaseEncryption in case something went wrong during restart
        tryFinishSetupDatabaseEncryption()

        setContent {
            RootView()
        }
    }

    private fun tryFinishSetupDatabaseEncryption() {
        lifecycleScope.launch {
            val result = viewModel.finishSetupDatabaseEncryption()
            when (result) {
                is LBResult.Failure -> {
                    // TODO <cipher> show error
                }
                is LBResult.Success -> {
                    when (result.successData) {
                        FinishSetupDatabaseEncryptionUseCase.SuccessState.Noop -> {
                            /* no-op */
                        }
                        FinishSetupDatabaseEncryptionUseCase.SuccessState.Success -> {
                            logger.i("Database encryption setup succeed")
                            OSProcessPhoenix.triggerRebirth(this@HelpActivity)
                        }
                        FinishSetupDatabaseEncryptionUseCase.SuccessState.Canceled -> {
                            // TODO <cipher> inform user of cancel
                            OSProcessPhoenix.triggerRebirth(this@HelpActivity)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun launch(context: Context) {
            val clazz = HelpActivity::class
            val helpIntent = Intent(context, clazz.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            logger.i("Launching ${clazz.simpleName} from ${context::class.simpleName}")
            context.startActivity(helpIntent)
        }
    }
}

@Composable
private fun RootView() {
    OSTheme(isMaterialYouSettingsEnabled = false) {
        HelpRoute()
    }
}
