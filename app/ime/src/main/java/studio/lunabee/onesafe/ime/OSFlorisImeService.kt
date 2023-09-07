/*
 * Copyright (c) 2023-2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 6/30/23, 11:29 AM
 */

package studio.lunabee.onesafe.ime

import android.content.ClipboardManager
import android.view.KeyEvent
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.patrickgold.florisboard.FlorisImeService
import dev.patrickgold.florisboard.editorInstance
import dev.patrickgold.florisboard.themeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.atom.textfield.LocalTextFieldInteraction
import studio.lunabee.onesafe.commonui.localprovider.LocalIsOneSafeK
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.commonui.navigation.LoginDestination
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.ime.model.ImeClient
import studio.lunabee.onesafe.ime.ui.ImeFeedbackManager
import studio.lunabee.onesafe.ime.ui.ImeNavGraph
import studio.lunabee.onesafe.ime.ui.ImeNavGraphRoute
import studio.lunabee.onesafe.ime.ui.ImeOSTopBar
import studio.lunabee.onesafe.ime.ui.contact.SelectContactDestination
import studio.lunabee.onesafe.ime.ui.extension.keyboardTextfield
import studio.lunabee.onesafe.ime.viewmodel.ImeLoginViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.WriteMessageViewModelFactory
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.usecase.ProcessMessageQueueUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.ui.theme.OSColor
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.visits.OsAppVisit
import javax.inject.Inject

// TODO oSK extract business call to a viewmodel class

/**
 * oneSafe overload of [FlorisImeService] with custom UI injection & clipboard listener
 *
 * @see FlorisImeService
 */
@AndroidEntryPoint
class OSFlorisImeService : FlorisImeService() {

    @Inject lateinit var imeLoginViewModelFactory: dagger.Lazy<ImeLoginViewModelFactory>

    @Inject lateinit var selectContactViewModelFactory: dagger.Lazy<SelectContactViewModelFactory>

    @Inject lateinit var writeMessageViewModelFactory: dagger.Lazy<WriteMessageViewModelFactory>

    @Inject lateinit var settings: OSAppSettings

    @Inject lateinit var channelRepository: MessageChannelRepository

    @Inject lateinit var isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase

    private val themeManager by themeManager()

    @Inject lateinit var decryptClipboardListener: DecryptClipboardListener

    @Inject lateinit var processMessageQueueUseCase: ProcessMessageQueueUseCase

    @Inject lateinit var feedbackManager: ImeFeedbackManager

    @Inject lateinit var lockUseCase: LockAppUseCase

    @Inject lateinit var isSignUpUseCase: IsSignUpUseCase

    @Inject lateinit var osAppVisit: OsAppVisit

    private var isKeyboardVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private var isOneSafeUiVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val imeClientFlow: MutableStateFlow<ImeClient?> = MutableStateFlow(null)

    /**
     * Last [ImeClient] bound when keyboard was visible
     */
    private var lastImeClient: ImeClient? = null
    private var clipboardResultJob: Job? = null

    private lateinit var navController: NavHostController

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        isKeyboardVisibleFlow.value = true
        return super.onShowInputRequested(flags, configChange)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lastImeClient = imeClientFlow.value
    }

    override fun onWindowShown() {
        // Reset oSK UI visibility on windows shown if
        //   - lastImeClient not null -> init
        //   - package has changed (since last client when window hidden)
        //   - the current client is not oneSafe 6
        if (
            lastImeClient != null &&
            lastImeClient?.packageName != imeClientFlow.value?.packageName &&
            imeClientFlow.value?.packageName != packageName
        ) {
            isOneSafeUiVisibleFlow.value = false
        }
        super.onWindowShown()
        (editorInstance().value as? InterceptEditorInstance)?.blockInput = isOneSafeUiVisibleFlow.value
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                isKeyboardVisibleFlow.value && isOneSafeUiVisibleFlow.value -> {
                    isKeyboardVisibleFlow.value = false
                    return true
                }
                isKeyboardVisibleFlow.value && !isOneSafeUiVisibleFlow.value -> {
                    isKeyboardVisibleFlow.value = false
                }
                !isKeyboardVisibleFlow.value -> {
                    isOneSafeUiVisibleFlow.value = false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate() {
        super.onCreate()
        processMessageQueue()
        lifecycleScope.launch {
            isOneSafeUiVisibleFlow.collect {
                (editorInstance().value as? InterceptEditorInstance)?.blockInput = it
            }
        }
    }

    override fun onBindInput() {
        super.onBindInput()
        imeClientFlow.value = ImeClient.fromUid(
            appContext = applicationContext,
            uid = currentInputBinding.uid,
            notFoundFallbackClient = imeClientFlow.value,
        )
        channelRepository.channel = imeClientFlow.value?.applicationName

        if (packageManager.getPackageUid(this.packageName, 0) == currentInputBinding.uid) {
            // Don't observe clipboard in oneSafe
            setupClipboardObserver(false)
        } else {
            setupClipboardObserver(true)
        }
    }

    private fun setupClipboardObserver(observe: Boolean) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardResultJob?.cancel()
        clipboard.removePrimaryClipChangedListener(decryptClipboardListener)
        clipboardResultJob = if (observe) {
            clipboard.addPrimaryClipChangedListener(decryptClipboardListener)
            lifecycleScope.launch {
                decryptClipboardListener.result.collect { result ->
                    feedbackManager.sendIncomingMessageFeedback(result)
                }
            }
        } else {
            null
        }
    }

    private fun processMessageQueue() {
        lifecycleScope.launch {
            processMessageQueueUseCase.flush()
        }
    }

    @Composable
    override fun DecoratedIme(ImeUi: @Composable () -> Unit) {
        val imeClient by imeClientFlow.collectAsStateWithLifecycle()
        navController = rememberNavController()

        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        var osUiRequiredHeight: Dp by remember { mutableStateOf(0.dp) }
        var keyboardUiHeight: Dp by remember { mutableStateOf(0.dp) }
        val statusBarHeightDp: Dp
        val navigationBarHeightDp: Dp
        with(density) {
            statusBarHeightDp = (WindowInsets.statusBars.getBottom(density) + WindowInsets.statusBars.getTop(density)).toDp()
            navigationBarHeightDp =
                (WindowInsets.navigationBars.getBottom(density) + WindowInsets.navigationBars.getTop(density)).toDp()
        }
        val isCryptoDataReady by isCryptoDataReadyInMemoryUseCase().collectAsStateWithLifecycle(
            initialValue = false,
        )
        val isOneSafeUiVisible by isOneSafeUiVisibleFlow.collectAsStateWithLifecycle()
        val isKeyboardVisible by this.isKeyboardVisibleFlow.collectAsStateWithLifecycle()

        // Reset nav on crypto changes
        LaunchedEffect(key1 = isCryptoDataReady) {
            if (!isCryptoDataReady && navController.currentBackStackEntry != null) {
                navController.navigate(LoginDestination.route) {
                    popUpTo(ImeNavGraphRoute) {
                        inclusive = false
                    }
                }
            }
        }

        val focusManager = LocalFocusManager.current
        LaunchedEffect(isKeyboardVisible) {
            if (!isKeyboardVisible) {
                focusManager.clearFocus()
            }
        }

        if (imeClient?.packageName == packageName) {
            // Disable oneSafe K when using oneSafe
            super.DecoratedIme(ImeUi)
        } else {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                AnimatedVisibility(
                    visible = isOneSafeUiVisible,
                    modifier = Modifier
                        .background(Color.Transparent),
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(durationMillis = KeyboardUiAnimationDuration),
                    ),
                    exit = shrinkVertically(),
                ) {
                    CompositionLocalProvider(
                        LocalTextFieldInteraction.provides { textFieldValue, setTextFieldValue, keyboardOptions, keyboardActions ->
                            Modifier
                                .keyboardTextfield(
                                    isKeyboardVisible = { isKeyboardVisible },
                                    toggleKeyboardVisibility = { isKeyboardVisibleFlow.value = !isKeyboardVisible },
                                    textFieldValue = textFieldValue,
                                    setTextFieldValue = setTextFieldValue,
                                    keyboardOptions = keyboardOptions,
                                    keyboardActions = keyboardActions,
                                )
                        },
                        LocalKeyboardUiHeight.provides(if (isKeyboardVisible) keyboardUiHeight else 0.dp),
                        LocalIsOneSafeK.provides(true),
                        LocalOnBackPressedDispatcherOwner.provides(object : OnBackPressedDispatcherOwner {
                            override val lifecycle: Lifecycle = this@OSFlorisImeService.lifecycle
                            override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher(null)
                        }),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight + statusBarHeightDp + navigationBarHeightDp),
                        ) {
                            OneSafeKUi(
                                modifier = Modifier,
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = isKeyboardVisible,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(durationMillis = KeyboardUiAnimationDuration),
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(durationMillis = KeyboardUiAnimationDuration),
                        targetOffsetY = { fullHeight -> fullHeight },
                    ),
                ) {
                    val entry by navController.currentBackStackEntryAsState()
                    val forceDark = entry?.destination?.route == WriteMessageDestination.route && isOneSafeUiVisible
                    val background: Color
                    val contentColor: Color
                    when {
                        forceDark -> {
                            themeManager.updateActiveTheme(forceNight = true)
                            background = OSColor.Neutral70
                            contentColor = OSColor.Neutral00
                        }
                        isSystemInDarkTheme() -> {
                            themeManager.updateActiveTheme()
                            background = Color.Transparent
                            contentColor = OSColor.Neutral00
                        }
                        else -> {
                            themeManager.updateActiveTheme()
                            background = Color.Transparent
                            contentColor = LocalContentColor.current
                        }
                    }

                    Surface(color = background, contentColor = contentColor) {
                        Column(
                            modifier = Modifier
                                .onGloballyPositioned { layoutCoordinate ->
                                    val size = layoutCoordinate.size
                                    val height = with(density) { size.height.toDp() }
                                    keyboardUiHeight = height
                                    if (osUiRequiredHeight == 0.dp) {
                                        osUiRequiredHeight = (screenHeight - height) + statusBarHeightDp + navigationBarHeightDp
                                    }
                                },
                        ) {
                            ImeOSTopBar(
                                imeClient = imeClient,
                                isCryptoDataReady = isCryptoDataReady,
                                onLogoClick = ::showOneSafeUi,
                                isDark = themeManager.activeThemeInfo.value?.config?.isNightTheme ?: false,
                                onLockClick = {
                                    if (isCryptoDataReady) {
                                        lockUseCase()
                                    } else {
                                        showOneSafeUi()
                                    }
                                },
                            )
                            ImeUi()
                        }
                    }
                }
            }
        }
    }

    private fun showOneSafeUi() {
        if (isSignUpUseCase()) {
            isOneSafeUiVisibleFlow.value = true
        } else {
            ImeDeeplinkHelper.deeplinkBubblesOnboarding(applicationContext)
        }
    }

    @Composable
    fun OneSafeKUi(
        modifier: Modifier,
    ) {
        val isMaterialYouSettingsEnabled: Boolean by settings.materialYouSetting.collectAsStateWithLifecycle(false)

        OSTheme(
            isMaterialYouSettingsEnabled = isMaterialYouSettingsEnabled,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                ImeNavGraph(
                    navController = navController,
                    imeLoginViewModelFactory = imeLoginViewModelFactory,
                    selectContactViewModelFactory = selectContactViewModelFactory,
                    writeMessageViewModelFactory = writeMessageViewModelFactory,
                    onLoginSuccess = {
                        isKeyboardVisibleFlow.value = false
                    },
                    dismissUi = {
                        isOneSafeUiVisibleFlow.value = false
                        isKeyboardVisibleFlow.value = true
                    },
                    sendMessage = { encryptedMessage ->
                        currentInputConnection.also {
                            isOneSafeUiVisibleFlow.value = false
                            isKeyboardVisibleFlow.value = true
                            it.commitText(encryptedMessage, 0)
                            navController.navigate(SelectContactDestination.route) // Go back to the contact selection
                        }
                    },
                    hasDoneOnBoardingBubbles = osAppVisit.hasDoneOnBoardingBubbles,
                    modifier = modifier,
                    hideKeyboard = {
                        isKeyboardVisibleFlow.value = false
                    },
                )
            }
        }
    }

    companion object {
        private const val KeyboardUiAnimationDuration: Int = 800
    }
}
