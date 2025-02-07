package studio.lunabee.onesafe.feature.autofill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.FillResponse
import android.service.autofill.Presentations
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingBackHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.common.extensions.getParcelableCompact
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.crashlytics.CrashlyticsCustomKeys
import studio.lunabee.onesafe.crashlytics.CrashlyticsHelper
import studio.lunabee.onesafe.crashlytics.CrashlyticsUnknown
import studio.lunabee.onesafe.domain.usecase.authentication.CheckDatabaseAccessUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.feature.autofill.login.AutoFillLoginDestination
import studio.lunabee.onesafe.feature.settings.security.SecureScreenManager
import studio.lunabee.onesafe.help.main.HelpActivity
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AutoFillActivity : FragmentActivity() {

    @Inject lateinit var secureScreenManager: SecureScreenManager

    @Inject lateinit var isSafeReadyUseCase: IsSafeReadyUseCase

    @Inject lateinit var checkDatabaseAccessUseCase: CheckDatabaseAccessUseCase

    val viewModel: AutofillActivityViewModel by viewModels()
    var clientDomain: String = ""
    var clientPackage: String = ""
    var autofillIdEmail: AutofillId? = null
    var autofillIdPassword: AutofillId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkDatabaseAccess()

        secureScreenManager(this)

        val clientBundle: Bundle? = intent?.getBundleExtra(AutofillManager.EXTRA_CLIENT_STATE)
        clientDomain = clientBundle?.getString(CallerDomainKey, "").orEmpty()
        clientPackage = clientBundle?.getString(CallerPackageKey, "").orEmpty()

        // Data provided by the autofill service when used as a provider.
        autofillIdEmail = clientBundle?.getParcelableCompact(IdentifierFieldKey, AutofillId::class.java)
        autofillIdPassword = clientBundle?.getParcelableCompact(PasswordFieldIdKey, AutofillId::class.java)

        // Data provided by the autofill when used as a saver.
        val identifierValue = intent?.getStringExtra(IdentifierValueKey)
        val saveCredential = intent?.getBooleanExtra(SaveCredentialsKey, false) ?: false
        val passwordValue = intent?.getStringExtra(PasswordValueKey)
        if (intent?.getStringExtra(CallerDomainKey).orEmpty().isNotEmpty()) {
            clientDomain = intent?.getStringExtra(CallerDomainKey).orEmpty()
        }
        if (intent?.getStringExtra(CallerPackageKey).orEmpty().isNotEmpty()) {
            clientPackage = intent?.getStringExtra(CallerPackageKey).orEmpty()
        }

        setContent {
            LoadingBackHandler(enabled = true) {
                finish()
            }
            val navController = rememberNavController()
            val isMaterialYouEnabled by viewModel.isMaterialYouEnabled.collectAsStateWithLifecycle()

            val state by navController.currentBackStackEntryAsState()
            state?.let {
                val route = kotlin.runCatching { it.destination.route }.getOrNull()
                LaunchedEffect(route) {
                    CrashlyticsHelper.setCustomKey(
                        CrashlyticsCustomKeys.AutoFillNavScreen,
                        route ?: CrashlyticsUnknown,
                    )
                }
            }

            val backStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
            val isGraphReady = backStackEntry != null
            val isCryptoLoaded by isSafeReadyUseCase.flow().collectAsStateWithLifecycle(initialValue = false)
            LaunchedEffect(isCryptoLoaded, isGraphReady) {
                if (!isCryptoLoaded && isGraphReady) {
                    navController.navigate(AutoFillLoginDestination.route) {
                        popUpTo(GraphIdentifier.AutoFillNavGraph.name) {
                            inclusive = false
                        }
                    }
                }
            }

            OSUserTheme(null) {
                OSTheme(
                    isMaterialYouSettingsEnabled = isMaterialYouEnabled,
                ) {
                    Surface {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets(
                                top = OSDimens.SystemSpacing.None,
                                bottom = OSDimens.SystemSpacing.None,
                            ),
                        ) {
                            Box(modifier = Modifier.padding(it)) {
                                AutoFillNavGraph(
                                    navController = navController,
                                    clientDomain = clientDomain,
                                    clientPackage = clientPackage,
                                    finish = { finish() },
                                    onCredentialProvided = { identifier, password ->
                                        setAutoFillResponse(identifier, password, autofillIdEmail, autofillIdPassword)
                                    },
                                    saveCredential = saveCredential,
                                    identifier = identifierValue,
                                    password = passwordValue,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setAutoFillResponse(
        identifier: String,
        password: String,
        autofillIdEmail: AutofillId?,
        autofillIdPassword: AutofillId?,
    ) {
        val identifierPresentations = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            if (identifier.isNotEmpty()) {
                setTextViewText(android.R.id.text1, identifier)
            } else {
                setTextViewText(android.R.id.text1, getString(OSString.autofill_noIdentifier_label))
            }
        }

        val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, getString(OSString.autofill_ResponseDataSet_password, identifier))
        }

        val dataSetBuilder = Dataset.Builder()
        if (autofillIdEmail != null) {
            dataSetBuilder.addFieldValue(
                field = identifier,
                autofillId = autofillIdEmail,
                identifierPresentations,
            )
        }
        if (autofillIdPassword != null) {
            dataSetBuilder.addFieldValue(
                field = password,
                autofillId = autofillIdPassword,
                passwordPresentation,
            )
        }
        val fillResponse: FillResponse = FillResponse.Builder()
            .addDataset(dataSetBuilder.build())
            .build()

        val replyIntent = Intent().apply {
            putExtra(EXTRA_AUTHENTICATION_RESULT, fillResponse)
        }

        setResult(Activity.RESULT_OK, replyIntent)
        runBlocking { viewModel.lockApp() }
        finish()
    }

    private fun Dataset.Builder.addFieldValue(field: String, autofillId: AutofillId, remoteViews: RemoteViews): Dataset.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return setField(
                autofillId,
                Field.Builder()
                    .setValue(AutofillValue.forText(field))
                    .setPresentations(Presentations.Builder().setMenuPresentation(remoteViews).build())
                    .build(),
            )
        } else {
            @Suppress("DEPRECATION")
            return setValue(
                autofillId,
                AutofillValue.forText(field),
                remoteViews,
            )
        }
    }

    private fun checkDatabaseAccess() {
        runBlocking {
            val result = checkDatabaseAccessUseCase()
            if ((result as? LBResult.Failure)?.throwable?.osCode() == OSStorageError.Code.DATABASE_WRONG_KEY) {
                HelpActivity.launch(this@AutoFillActivity, intent)
                finish()
            }
        }
    }

    companion object {
        const val CallerNameKey: String = "CallerNameKey"
        const val IdentifierFieldKey: String = "IdentifierFieldKey"
        const val PasswordFieldIdKey: String = "PasswordFieldIdKey"
        const val SaveCredentialsKey: String = "SaveCredentialsKey"
        const val IdentifierValueKey: String = "IdentifierValueKey"
        const val PasswordValueKey: String = "PasswordValueKey"
        const val CallerPackageKey: String = "CallerPackageKey"
        const val CallerDomainKey: String = "CallerDomainKey"
        fun getIntent(
            context: Context,
            identifierValue: String = "",
            passwordValue: String = "",
            clientPackage: String = "",
            clientDomain: String = "",
            saveCredential: Boolean = false,
        ): Intent = Intent(
            context,
            AutoFillActivity::class.java,
        ).apply {
            flags = FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(IdentifierValueKey, identifierValue)
            putExtra(SaveCredentialsKey, saveCredential)
            putExtra(PasswordValueKey, passwordValue)
            putExtra(CallerDomainKey, clientDomain)
            putExtra(CallerPackageKey, clientPackage)
        }
    }
}
