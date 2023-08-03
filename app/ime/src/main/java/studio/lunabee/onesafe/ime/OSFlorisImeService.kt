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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
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
import dev.patrickgold.florisboard.themeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.atom.textfield.LocalTextFieldInteraction
import studio.lunabee.onesafe.bubbles.ui.onesafek.SelectContactDestination
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.ime.model.ImeClient
import studio.lunabee.onesafe.ime.ui.ImeFeedbackManager
import studio.lunabee.onesafe.ime.ui.ImeNavGraph
import studio.lunabee.onesafe.ime.ui.OSKeyboardStatus
import studio.lunabee.onesafe.ime.ui.extension.keyboardTextfield
import studio.lunabee.onesafe.ime.ui.res.ImeDimens
import studio.lunabee.onesafe.ime.ui.res.ImeShape
import studio.lunabee.onesafe.ime.viewmodel.LoginViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.WriteMessageViewModelFactory
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.usecase.ProcessMessageQueueUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSColor
import studio.lunabee.onesafe.ui.theme.OSTheme
import javax.inject.Inject

@AndroidEntryPoint
class OSFlorisImeService : FlorisImeService() {

    @Inject lateinit var loginViewModelFactory: dagger.Lazy<LoginViewModelFactory>

    @Inject lateinit var selectContactViewModelFactory: dagger.Lazy<SelectContactViewModelFactory>

    @Inject lateinit var writeMessageViewModelFactory: dagger.Lazy<WriteMessageViewModelFactory>

    @Inject lateinit var settings: OSAppSettings

    @Inject lateinit var channelRepository: MessageChannelRepository

    @Inject lateinit var isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase

    private val themeManager by themeManager()

    @Inject lateinit var decryptClipboardListener: DecryptClipboardListener

    @Inject lateinit var processMessageQueueUseCase: ProcessMessageQueueUseCase

    @Inject lateinit var feedbackManager: ImeFeedbackManager

    private var isKeyboardVisible: Boolean by mutableStateOf(true)
    private var isOneSafeUiVisible: Boolean by mutableStateOf(false)
    private val imeClient: MutableStateFlow<ImeClient?> = MutableStateFlow(null)
    private var clipboardResultJob: Job? = null

    private lateinit var navController: NavHostController

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        isKeyboardVisible = true
        return super.onShowInputRequested(flags, configChange)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                isKeyboardVisible && isOneSafeUiVisible -> {
                    isKeyboardVisible = false
                    return true
                }
                isKeyboardVisible && !isOneSafeUiVisible -> {
                    isKeyboardVisible = false
                }
                !isKeyboardVisible -> {
                    isOneSafeUiVisible = false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate() {
        super.onCreate()
        processMessageQueue()
    }

    override fun onBindInput() {
        super.onBindInput()
        imeClient.value = ImeClient.fromUid(applicationContext, currentInputBinding.uid)
        channelRepository.channel = imeClient.value?.applicationName

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
            navigationBarHeightDp = (WindowInsets.navigationBars.getBottom(density) + WindowInsets.navigationBars.getTop(density)).toDp()
        }

        val focusManager = LocalFocusManager.current
        val imeClient by this.imeClient.collectAsStateWithLifecycle()

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
                                toggleKeyboardVisibility = { isKeyboardVisible = !isKeyboardVisible },
                                textFieldValue = textFieldValue,
                                setTextFieldValue = setTextFieldValue,
                                keyboardOptions = keyboardOptions,
                                keyboardActions = keyboardActions,
                            )
                    },
                    LocalKeyboardUiHeight.provides(if (isKeyboardVisible) keyboardUiHeight else 0.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val isCryptoDataReady by isCryptoDataReadyInMemoryUseCase().collectAsStateWithLifecycle(initialValue = false)
                            val osStatus = if (isCryptoDataReady) {
                                OSKeyboardStatus.LoggedIn
                            } else {
                                OSKeyboardStatus.LoggedOut
                            }
                            osStatus.Logo(
                                modifier = Modifier
                                    .clip(ImeShape.Key)
                                    .clickable { isOneSafeUiVisible = true }
                                    .padding(vertical = ImeDimens.LogoVerticalPadding, horizontal = ImeDimens.LogoHorizontalPadding),
                                isDark = themeManager.activeThemeInfo.value?.config?.isNightTheme ?: false,
                            )
                            imeClient?.let {
                                it.Logo(
                                    Modifier.padding(end = OSDimens.SystemSpacing.Small),
                                )
                                it.Name()
                            }
                        }
                        ImeUi()
                    }
                }
            }
        }

        LaunchedEffect(isKeyboardVisible) {
            if (!isKeyboardVisible) {
                focusManager.clearFocus()
            }
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
                    loginViewModelFactory = loginViewModelFactory,
                    selectContactViewModelFactory = selectContactViewModelFactory,
                    writeMessageViewModelFactory = writeMessageViewModelFactory,
                    onLoginSuccess = {
                        isKeyboardVisible = false
                    },
                    sendMessage = { encryptedMessage ->
                        currentInputConnection.also {
                            isOneSafeUiVisible = false
                            isKeyboardVisible = true
                            it.commitText(encryptedMessage, 0)
                            navController.navigate(SelectContactDestination.route) // Go back to the contact selection
                        }
                    },
                    dismissUi = {
                        isOneSafeUiVisible = false
                        isKeyboardVisible = true
                    },
                    modifier = modifier,
                )
            }
        }
    }

    companion object {
        private const val KeyboardUiAnimationDuration: Int = 800
    }
}
