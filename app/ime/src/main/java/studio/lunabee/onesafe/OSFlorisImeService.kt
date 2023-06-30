/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/12/2023 - for the oneSafe6 SDK.
 * Last modified 5/12/23, 10:32 AM
 */

package studio.lunabee.onesafe

import android.view.KeyEvent
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.patrickgold.florisboard.FlorisImeService
import dev.patrickgold.florisboard.lib.android.stringRes
import kotlinx.coroutines.flow.MutableStateFlow
import studio.lunabee.onesafe.atom.textfield.LocalTextFieldInteraction
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactDestination
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.ime.R
import studio.lunabee.onesafe.ime.ui.extension.keyboardTextfield
import studio.lunabee.onesafe.ime.ui.navigation.OneSafeKMMessageEmbeddedGraph
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.model.ImeClient
import studio.lunabee.onesafe.ui.res.ImeDimens
import studio.lunabee.onesafe.ui.res.ImeShape
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.viewmodel.LoginViewModelFactory
import studio.lunabee.onesafe.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.viewmodel.WriteMessageViewModelFactory
import javax.inject.Inject

@AndroidEntryPoint
class OSFlorisImeService : FlorisImeService() {

    @Inject lateinit var loginViewModelFactory: dagger.Lazy<LoginViewModelFactory>

    @Inject lateinit var selectContactViewModelFactory: dagger.Lazy<SelectContactViewModelFactory>

    @Inject lateinit var writeMessageViewModelFactory: dagger.Lazy<WriteMessageViewModelFactory>

    @Inject lateinit var settings: OSAppSettings

    @Inject lateinit var channelRepository: MessageChannelRepository

    private var isKeyboardVisible: Boolean by mutableStateOf(true)
    private var isOneSafeUiVisible: Boolean by mutableStateOf(false)
    private val imeClient: MutableStateFlow<ImeClient?> = MutableStateFlow(null)

    private lateinit var navController: NavHostController

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        isKeyboardVisible = true
        return super.onShowInputRequested(flags, configChange)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (event?.keyCode == KeyEvent.KEYCODE_BACK && isKeyboardVisible) {
            isKeyboardVisible = false
            true
        } else if (event?.keyCode == KeyEvent.KEYCODE_BACK && !isKeyboardVisible) {
            isOneSafeUiVisible = false
            super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        currentInputBinding?.uid?.let { imeClient.value = ImeClient.fromUid(applicationContext, it) }
        channelRepository.channel = imeClient.value?.applicationName
    }

    @Composable
    override fun DecoratedIme(ImeUi: @Composable () -> Unit) {
        navController = rememberNavController()
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current

        val screenHeight = configuration.screenHeightDp.dp
        var osUiRequiredHeight: Dp by remember { mutableStateOf(0.dp) }
        var keyboardUiHeight: Dp by remember { mutableStateOf(0.dp) }
        val statusBarHeightDp = getStatusBarHeight()
        val navigationBarHeightDp = getNavigationBarHeight()

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
                    LocalTextFieldInteraction.provides { textFieldValue, setTextFieldValue ->
                        Modifier
                            .keyboardTextfield(
                                isKeyboardVisible = { isKeyboardVisible },
                                toggleKeyboardVisibility = { isKeyboardVisible = !isKeyboardVisible },
                                textFieldValue = textFieldValue,
                                setTextFieldValue = setTextFieldValue,
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
                Column(
                    modifier = Modifier.onGloballyPositioned { layoutCoordinate ->
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
                        Image(
                            painter = painterResource(R.drawable.onesafek_logo_logout),
                            contentDescription = stringRes(R.string.oneSafeK_ime_status_logout_description),
                            modifier = Modifier
                                .clip(ImeShape.Key)
                                .clickable { isOneSafeUiVisible = true }
                                .padding(vertical = ImeDimens.LogoVerticalPadding, horizontal = ImeDimens.LogoHorizontalPadding),
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
            OneSafeKMMessageEmbeddedGraph(
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
                exit = {
                    hideUi()
                    isOneSafeUiVisible = false
                    isKeyboardVisible = false
                },
            )
        }
    }

    @Composable
    private fun getStatusBarHeight(): Dp {
        val density = LocalDensity.current
        return with(density) {
            this@OSFlorisImeService.resources.getDimensionPixelSize(
                resources.getIdentifier("status_bar_height", "dimen", "android"),
            ).toDp()
        }
    }

    @Composable
    private fun getNavigationBarHeight(): Dp {
        val density = LocalDensity.current
        return with(density) {
            this@OSFlorisImeService.resources.getDimensionPixelSize(
                resources.getIdentifier("navigation_bar_height", "dimen", "android"),
            ).toDp()
        }
    }

    companion object {
        private const val KeyboardUiAnimationDuration: Int = 800
    }
}
