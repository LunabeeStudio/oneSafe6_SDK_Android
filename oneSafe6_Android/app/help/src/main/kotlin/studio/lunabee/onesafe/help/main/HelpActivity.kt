package studio.lunabee.onesafe.help.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.AndroidEntryPoint
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.extension.isTest
import studio.lunabee.onesafe.help.HelpRootContent
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge

private val logger = LBLogger.get<HelpActivity>()

@AndroidEntryPoint
open class HelpActivity : FragmentActivity() {

    private var launchDataUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RootView()
        }

        launchDataUri = intent?.data

        // FIXME do not consume if running instrumented test https://github.com/android/android-test/issues/1939
        if (!intent.isTest) {
            intent.data = null
        }
    }

    @Composable
    private fun RootView() {
        OSTheme(isMaterialYouSettingsEnabled = false) {
            HelpRootContent { navController ->
                HelpRoute(
                    navController = navController,
                    navToMain = ::exitToMain,
                    showOverrideBackupDialog = launchDataUri != null,
                )
            }
        }
    }

    private fun exitToMain() {
        packageManager.getLaunchIntentForPackage(packageName)
            ?.setData(launchDataUri)
            ?.putExtra(CommonUiConstants.AppLaunch.DeleteOnImportExtraKey, true)
            ?.let {
                startActivity(it)
                finish()
            }
    }

    companion object {
        fun launch(context: Context, intent: Intent? = null) {
            val clazz = HelpActivity::class
            val helpIntent = Intent(context, clazz.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setData(intent?.data)
            logger.i("Launching ${clazz.simpleName} from ${context::class.simpleName}")
            context.startActivity(helpIntent)
        }
    }
}
