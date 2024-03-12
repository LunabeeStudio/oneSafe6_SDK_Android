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

import android.app.ActivityManager
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.patrickgold.florisboard.FlorisImeService
import dev.patrickgold.florisboard.editorInstance
import dev.patrickgold.florisboard.themeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.atom.textfield.LocalTextFieldInteraction
import studio.lunabee.onesafe.commonui.localprovider.LocalIsKeyBoardVisible
import studio.lunabee.onesafe.commonui.localprovider.LocalIsOneSafeK
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.ime.model.ImeClient
import studio.lunabee.onesafe.ime.model.OSKImeState
import studio.lunabee.onesafe.ime.ui.ImeFeedbackManager
import studio.lunabee.onesafe.ime.ui.ImeNavGraph
import studio.lunabee.onesafe.ime.ui.ImeNavGraphRoute
import studio.lunabee.onesafe.ime.ui.ImeOSTopBar
import studio.lunabee.onesafe.ime.ui.LocalKeyboardIsNightMode
import studio.lunabee.onesafe.ime.ui.extension.keyboardTextfield
import studio.lunabee.onesafe.ime.viewmodel.ImeLoginViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.WriteMessageViewModelFactory
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.usecase.ProcessMessageQueueUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
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

    @Inject lateinit var decryptClipboardListener: DecryptClipboardListener

    @Inject lateinit var processMessageQueueUseCase: ProcessMessageQueueUseCase

    @Inject lateinit var feedbackManager: ImeFeedbackManager

    @Inject lateinit var lockUseCase: LockAppUseCase

    @Inject lateinit var isSignUpUseCase: IsSignUpUseCase

    @Inject lateinit var osAppVisit: OsAppVisit

    @Inject lateinit var autolockVisibilityManager: OSKAutoLockVisibilityManager

    @Inject lateinit var autoLockInactivityManager: OSKAutoLockInactivityManager

    @Inject lateinit var editorInfoManager: OSKEditorInfoManager

    @Inject lateinit var lockAppUseCase: LockAppUseCase

    private val themeManager by themeManager()
    private val isWindowVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isKeyboardVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val isOneSafeUiVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val imeClientFlow: MutableStateFlow<ImeClient?> = MutableStateFlow(null)
    private val oskImeStateFlow = combine(
        isKeyboardVisibleFlow,
        isOneSafeUiVisibleFlow,
        isWindowVisibleFlow,
    ) { keyboardVisible, uiVisible, imeVisible ->
        when {
            !imeVisible -> OSKImeState.Hidden
            uiVisible && keyboardVisible -> OSKImeState.ScreenWithKeyboard
            uiVisible -> OSKImeState.Screen
            keyboardVisible -> OSKImeState.Keyboard
            else -> OSKImeState.Hidden
        }
    }.stateIn(lifecycleScope, SharingStarted.Lazily, OSKImeState.Hidden)

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
        isWindowVisibleFlow.value = false
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
        isWindowVisibleFlow.value = true
        refreshBlockInput()
    }

    private fun refreshBlockInput() {
        // Block client text field inputs if oneSafe K is visible (else unblock)
        (editorInstance().value as? InterceptEditorInstance)?.blockInput = oskImeStateFlow.value.isOneSafeUiVisible
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            when (oskImeStateFlow.value) {
                OSKImeState.Hidden -> {
                    /* no-op */
                }
                // hide oSK UI and let ime handle back
                OSKImeState.Screen -> isOneSafeUiVisibleFlow.value = false
                // hide keyboard and let ime handle back
                OSKImeState.Keyboard -> isKeyboardVisibleFlow.value = false
                // hide keyboard and consume event
                OSKImeState.ScreenWithKeyboard -> {
                    isKeyboardVisibleFlow.value = false
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate() {
        super.onCreate()
        processMessageQueue()

        lifecycleScope.launch {
            oskImeStateFlow.collect { state ->
                // Call state listeners
                editorInfoManager.onStateChange(state)
                autolockVisibilityManager.onStateChange(state)
                autoLockInactivityManager.onStateChange(state)
                refreshBlockInput()
            }
        }
    }

    override fun onCreateInputView(): View {
        // Always lock on UI (re)creation
        lifecycleScope.launch {
            lockAppUseCase()
        }
        return super.onCreateInputView()
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
            isOneSafeUiVisibleFlow.value = false
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
    override fun ThemeImeView() {
        val coroutineScope = rememberCoroutineScope()
        val imeClient by imeClientFlow.collectAsStateWithLifecycle()
        val hasDoneOpenTutorial by osAppVisit.hasDoneTutorialOpenOskFlow.collectAsStateWithLifecycle(initialValue = true)
        val hasDoneLockTutorial by osAppVisit.hasDoneTutorialLockOskFlow.collectAsStateWithLifecycle(initialValue = true)

        if (imeClient?.packageName == packageName) {
            // Disable oneSafe K when using oneSafe
            super.ThemeImeView()
        } else {
            val isCryptoDataReady by isCryptoDataReadyInMemoryUseCase.flow().collectAsStateWithLifecycle(
                false,
            )
            val isOneSafeUiVisible by isOneSafeUiVisibleFlow.collectAsStateWithLifecycle()
            val entry by navController.currentBackStackEntryAsState()
            val forceDark =
                entry?.destination?.route == WriteMessageDestination.Route && isOneSafeUiVisible
            LaunchedEffect(key1 = forceDark) {
                themeManager.updateActiveTheme(forceNight = forceDark)
            }

            // Observe the keyboard config night mode to set the correct content color and provide the information to compose tree. Do not
            // rely on system dark mode as the keyboard theme might be forced to light or dark.
            val isNightTheme by themeManager.activeThemeInfo
                .map { it.config.isNightTheme }
                .observeAsState(false)

            val contentColor = if (isNightTheme) {
                LocalColorPalette.current.Neutral00
            } else {
                LocalColorPalette.current.Primary30
            }

            CompositionLocalProvider(
                LocalKeyboardIsNightMode provides isNightTheme,
            ) {
                Surface(color = Color.Transparent, contentColor = contentColor) {
                    ImeOSTopBar(
                        imeClient = imeClient,
                        isCryptoDataReady = isCryptoDataReady,
                        onLogoClick = {
                            coroutineScope.launch {
                                osAppVisit.storeHasDoneTutorialOpenOsk(true)
                                showOneSafeUi()
                            }
                        },
                        onLockClick = {
                            coroutineScope.launch {
                                if (isCryptoDataReady) {
                                    osAppVisit.storeHasDoneTutorialLockOsk(true)
                                    lockUseCase()
                                } else {
                                    showOneSafeUi()
                                }
                            }
                        },
                        displayOpenTutorial = !hasDoneOpenTutorial && !isOneSafeUiVisible,
                        displayLockTutorial = !hasDoneLockTutorial && isCryptoDataReady,
                        closeLockTutorial = {
                            coroutineScope.launch { osAppVisit.storeHasDoneTutorialLockOsk(true) }
                        },
                        closeOpenTutorial = {
                            coroutineScope.launch { osAppVisit.storeHasDoneTutorialOpenOsk(true) }
                        },
                    )
                }
            }
        }
    }

    override fun calculateTouchableTopY(visibleTopY: Int, needAdditionalOverlay: Boolean): Int {
        return if (oskImeStateFlow.value.isOneSafeUiVisible) {
            0
        } else {
            super.calculateTouchableTopY(visibleTopY, needAdditionalOverlay)
        }
    }

    override fun onComposeViewTouchEvent(ev: MotionEvent?) {
        if (ev?.action == MotionEvent.ACTION_DOWN || ev?.action == MotionEvent.ACTION_UP) {
            autoLockInactivityManager.refreshLastUserInteraction()
        }
    }

    @Composable
    override fun AboveImeView(ImeUi: @Composable () -> Unit) {
        val imeClient by imeClientFlow.collectAsStateWithLifecycle()
        navController = rememberNavController()
        val isCryptoDataReady by isCryptoDataReadyInMemoryUseCase.flow().collectAsStateWithLifecycle(
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
            super.AboveImeView(ImeUi)
        } else {
            ImeOSTheme {
                Column {
                    OneSafeKView(isOneSafeUiVisible, isKeyboardVisible)
                    AnimatedVisibility(
                        visible = isKeyboardVisible,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        ImeUi()
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.OneSafeKView(
        isOneSafeVisible: Boolean,
        isKeyboardVisible: Boolean,
    ) {
        CompositionLocalProvider(
            LocalTextFieldInteraction.provides { textFieldValue, setTextFieldValue, keyboardOptions, keyboardActions ->
                Modifier
                    .keyboardTextfield(
                        isKeyboardVisible = { isKeyboardVisible },
                        toggleKeyboardVisibility = {
                            isKeyboardVisibleFlow.value = !isKeyboardVisible
                        },
                        textFieldValue = textFieldValue,
                        setTextFieldValue = setTextFieldValue,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                    )
            },
            LocalIsOneSafeK.provides(true),
            LocalIsKeyBoardVisible.provides(isKeyboardVisible),
            LocalOnBackPressedDispatcherOwner.provides(object : OnBackPressedDispatcherOwner {
                override val lifecycle: Lifecycle = this@OSFlorisImeService.lifecycle
                override val onBackPressedDispatcher: OnBackPressedDispatcher =
                    OnBackPressedDispatcher(null)
            }),
        ) {
            Box(
                modifier = Modifier.Companion.weight(1f, true),
            ) {
                Column(Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = isOneSafeVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f))
                                .fillMaxSize()
                                .clickable(
                                    onClick = {
                                        isOneSafeUiVisibleFlow.value = false
                                        isKeyboardVisibleFlow.value = true
                                    },
                                ),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .statusBarsPadding()
                        .padding(top = OSDimens.SystemSpacing.ExtraLarge),
                ) {
                    AnimatedVisibility(
                        visible = isOneSafeVisible,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        OneSafeKUi(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(
                                    RoundedCornerShape(
                                        topStart = OSDimens.SystemSpacing.Regular,
                                        topEnd = OSDimens.SystemSpacing.Regular,
                                    ),
                                ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun showOneSafeUi() {
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
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
        ) {
            ImeNavGraph(
                navController = navController,
                imeLoginViewModelFactory = imeLoginViewModelFactory,
                selectContactViewModelFactory = selectContactViewModelFactory,
                writeMessageViewModelFactory = writeMessageViewModelFactory,
                onLoginSuccess = {
                    // TODO = Fix navigation glitch on keyboard close
                    //  isKeyboardVisibleFlow.value = false
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
                    }
                },
                hasDoneOnBoardingBubbles = osAppVisit.hasDoneOnBoardingBubbles,
                hideKeyboard = { isKeyboardVisibleFlow.value = false },
            )
        }
    }

    companion object {
        fun kill(context: Context) {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            activityManager.runningAppProcesses.firstOrNull {
                it.processName == context.packageName + BuildConfig.IME_PROCESS_NAME
            }?.pid?.let {
                android.os.Process.killProcess(it)
            }
        }
    }
}
