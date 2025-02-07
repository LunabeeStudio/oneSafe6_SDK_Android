package studio.lunabee.onesafe.feature.onboarding.presentation

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.VerticalPagerIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.compose.theme.LbcThemeUtilities
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.accessibility.accessibilityClearForInvisibilityToUser
import studio.lunabee.onesafe.accessibility.accessibilityCustomAction
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.home.TextLogo
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import androidx.compose.animation.core.Animatable as AnimatableFloat

@Composable
fun AppPresentationRoute(
    navigateToNextStep: () -> Unit,
    viewModel: AppPresentationViewModel = hiltViewModel(),
    randomizePersonalizationBackground: Boolean = true,
) {
    val accessibilityState: AccessibilityState = rememberOSAccessibilityState()
    val uriHandler = LocalUriHandler.current
    val termsOfUseUrl = stringResource(id = OSString.cgu_url)

    LaunchedEffect(null) {
        viewModel.splashScreenManager.isAppReady = true
    }

    val presentationSteps: List<PresentationStep> = remember {
        listOf(
            PresentationStep(
                title = LbcTextSpec.StringResource(OSString.appPresentation_security_title),
                description = LbcTextSpec.StringResource(OSString.appPresentation_security_description),
                imageRes = OSDrawable.colored_illustration_type_coffre,
                actions = listOf(
                    PresentationAction(
                        label = LbcTextSpec.StringResource(OSString.appPresentation_security_action),
                        action = navigateToNextStep,
                        attributes = PresentationActionAttributes().fillMaxWidth(),
                    ),
                ),
            ),
            PresentationStep(
                title = LbcTextSpec.StringResource(OSString.appPresentation_personalization_title),
                description = LbcTextSpec.StringResource(OSString.appPresentation_personalization_description),
                imageRes = OSDrawable.colored_illustration_type_personalisation,
                actions = listOf(
                    PresentationAction(
                        label = LbcTextSpec.StringResource(OSString.appPresentation_personalization_action),
                        action = navigateToNextStep,
                        attributes = PresentationActionAttributes().fillMaxWidth(),
                    ),
                ),
            ),
            PresentationStep(
                title = LbcTextSpec.StringResource(OSString.appPresentation_transparency_title),
                description = LbcTextSpec.StringResource(OSString.appPresentation_transparency_description),
                imageRes = OSDrawable.colored_illustration_type_protection,
                actions = listOf(
                    PresentationAction(
                        label = LbcTextSpec.StringResource(OSString.appPresentation_transparency_action),
                        action = { uriHandler.openUri(termsOfUseUrl) },
                        attributes = PresentationActionAttributes().notFilled(),
                    ),
                ),
            ),
            PresentationStep(
                title = null,
                description = null,
                imageRes = OSDrawable.colored_illustration_type_setup,
                actions = listOf(
                    PresentationAction(
                        label = LbcTextSpec.StringResource(OSString.appPresentation_setup_action),
                        action = navigateToNextStep,
                        attributes = PresentationActionAttributes().fillMaxWidth(),
                    ),
                ),
            ),
        )
    }
    val pagerState = rememberPagerState {
        presentationSteps.size
    }

    OSTheme(
        isMaterialYouSettingsEnabled = false,
    ) {
        AppPresentationScreen(
            presentationSteps = presentationSteps,
            pagerState = pagerState,
            navigateToNextStep = navigateToNextStep,
            isAccessibilityEnabled = accessibilityState.isTouchExplorationEnabled,
            randomizePersonalizationBackground = randomizePersonalizationBackground,
        )
    }
}

@Composable
fun AppPresentationScreen(
    presentationSteps: List<PresentationStep>,
    pagerState: PagerState,
    navigateToNextStep: () -> Unit,
    isAccessibilityEnabled: Boolean,
    isSeeingLastPage: Boolean = pagerState.currentPage == presentationSteps.lastIndex,
    isSeeingFirstPage: Boolean = pagerState.currentPage == 0,
    randomizePersonalizationBackground: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val background by rememberBackground(
        randomize = randomizePersonalizationBackground && pagerState.currentPage == PersonalizationPageIndex,
    )

    Box(
        Modifier
            .drawBehind { drawRect(brush = background) },
    ) {
        OSScreen(
            testTag = UiConstants.TestTag.Screen.AppPresentationScreen,
            background = SolidColor(Color.Transparent),
            applySystemBarPadding = false,
            modifier = Modifier
                .accessibilityPager(
                    currentIndex = pagerState.currentPage + 1,
                    lastIndex = presentationSteps.lastIndex + 1,
                    onNextClick = { onNextClick(coroutineScope, pagerState) },
                    onPreviousClick = { onPreviousClick(coroutineScope, pagerState) },
                    onSkipClick = navigateToNextStep,
                ),
        ) {
            VerticalPagerIndicator(
                pagerState = pagerState,
                pageCount = presentationSteps.size,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .accessibilityClearForInvisibilityToUser()
                    .align(Alignment.CenterStart)
                    .padding(OSDimens.SystemSpacing.Small)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            )

            VerticalPager(
                state = pagerState,
                horizontalAlignment = Alignment.CenterHorizontally,
                userScrollEnabled = !isAccessibilityEnabled,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = OSDimens.SystemSpacing.Small)
                    .padding(bottom = OSDimens.SystemSpacing.Medium)
                    .testTag(UiConstants.TestTag.Item.AppPresentationVerticalPager),
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapPositionalThreshold = 0.2f, // arbitrary value
                ),
            ) { page ->
                PresentationStepLayout(
                    presentationStep = presentationSteps[page],
                    modifier = Modifier
                        // padding header size to center content
                        .padding(top = AppConstants.Ui.AppPresentation.LogoTextSize.Large.height)
                        .windowInsetsPadding(WindowInsets.safeGestures),
                )
            }

            FloatingButton(
                isSeeingFirstPage = isSeeingFirstPage,
                isSeeingLastPage = isSeeingLastPage,
                key = if (isAccessibilityEnabled) pagerState.currentPage else null,
                onClick = { onNextClick(coroutineScope, pagerState) },
                modifier = Modifier.align(Alignment.BottomEnd),
            )

            Header(
                isFirstPage = isSeeingFirstPage,
                onClick = navigateToNextStep,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeContent)
                    .padding(OSDimens.SystemSpacing.Regular),
            )
        }
    }
}

@Composable
private fun FloatingButton(
    key: Any?,
    isSeeingFirstPage: Boolean,
    isSeeingLastPage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !isSeeingLastPage,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        val buttonColor = when (isSystemInDarkTheme()) {
            true -> OSIconButtonDefaults.secondaryIconButtonColors()
            false -> OSIconButtonDefaults.tertiaryIconButtonColors()
        }
        // Force talkback to loose focus on page change
        key(key) {
            OSIconButton(
                modifier = Modifier
                    .testTag(tag = UiConstants.TestTag.Item.AppPresentationNextButton)
                    .windowInsetsPadding(WindowInsets.safeGestures)
                    .padding(end = OSDimens.SystemSpacing.ExtraLarge, bottom = OSDimens.SystemSpacing.ExtraLarge)
                    .bounce(isSeeingFirstPage)
                    .composed {
                        val nextActionLabel: String = stringResource(id = OSString.appPresentation_next_accessibility_customAction)
                        semantics {
                            contentDescription = nextActionLabel
                        }
                    },
                image = OSImageSpec.Drawable(OSDrawable.ic_arrow_down),
                contentDescription = LbcTextSpec.StringResource(id = OSString.appPresentation_next_accessibility_customAction),
                onClick = onClick,
                colors = buttonColor,
            )
        }
    }
}

@Composable
private fun Header(
    isFirstPage: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Max height reached
            .height(AppConstants.Ui.AppPresentation.LogoTextSize.Large.height),
    ) {
        AnimatedVisibility(
            isFirstPage,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            TextLogo(
                modifier = Modifier.size(
                    width = AppConstants.Ui.AppPresentation.LogoTextSize.Large.width,
                    height = AppConstants.Ui.AppPresentation.LogoTextSize.Large.height,
                ),
            )
        }

        AnimatedVisibility(
            !isFirstPage,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextLogo(
                    modifier = Modifier.size(
                        width = AppConstants.Ui.AppPresentation.LogoTextSize.Small.width,
                        height = AppConstants.Ui.AppPresentation.LogoTextSize.Small.height,
                    ),
                )

                val skipActionLabel: String = stringResource(id = OSString.appPresentation_skip_accessibility_customAction)
                OSTextButton(
                    text = LbcTextSpec.StringResource(OSString.common_start),
                    onClick = onClick,
                    modifier = Modifier
                        .testTag(tag = UiConstants.TestTag.Item.AppPresentationSkipButton)
                        .semantics { contentDescription = skipActionLabel },
                )
            }
        }
    }
}

private fun Modifier.bounce(isSeeingFirstPage: Boolean): Modifier = this.composed {
    var shouldAnimate by remember { mutableStateOf(true) }
    val animateScale = remember { AnimatableFloat(1f) }

    // Enable animation only for the first page and the first time seeing it
    if (!isSeeingFirstPage && shouldAnimate) {
        shouldAnimate = false
    }

    if (shouldAnimate) {
        LaunchedEffect(Unit) {
            while (true) {
                animateScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = AppConstants.Ui.AppPresentation.ResetFBASizeDurationMs),
                )
                animateScale.animateTo(
                    targetValue = 1.2f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
                delay(2.seconds.inWholeMilliseconds)
            }
        }
    } else {
        LaunchedEffect(Unit) {
            animateScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = AppConstants.Ui.AppPresentation.ResetFBASizeDurationMs),
            )
        }
    }
    graphicsLayer(scaleX = animateScale.value, scaleY = animateScale.value)
}

@Composable
private fun rememberBackground(randomize: Boolean): State<Brush> {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    // Initial (and default) colors
    val initStartColor = LocalDesignSystem.current.getBackgroundGradientStartColor()
    val initEndColor = LocalDesignSystem.current.getBackgroundGradientEndColor()

    // Animated colors
    val animatedStart = remember { Animatable(initStartColor) }
    val animatedEnd = remember { Animatable(initEndColor) }

    if (randomize) {
        // We want to animate from previous color to random ones
        val designSystem = LocalDesignSystem.current
        val palette = LocalColorPalette.current
        fun getStartColor(colorSeed: Color) = if (isSystemInDarkTheme) {
            val materialPrimary = LbcThemeUtilities.getMaterialColorSchemeFromColor(
                color = colorSeed,
                isInDarkMode = true,
            ).primary
            designSystem.getDarkStartColor(materialPrimary)
        } else {
            designSystem.getLightStartColor(palette)
        }

        LaunchedEffect(isSystemInDarkTheme) {
            val animationSpec = tween<Color>(ColorChangeAnimationDurationMs)
            // loop to random colors
            while (true) {
                val randColor = Color(red = Random.nextFloat(), green = Random.nextFloat(), blue = Random.nextFloat())
                val targetStartValue = getStartColor(randColor)
                val targetEndValue = designSystem.getEndColor(randColor)
                launch {
                    animatedStart.animateTo(
                        targetValue = targetStartValue,
                        animationSpec = animationSpec,
                    )
                }
                animatedEnd.animateTo(
                    targetValue = targetEndValue,
                    animationSpec = animationSpec,
                )
            }
        }
    } else {
        // We switch screen and want to animate from previous color to default one
        LaunchedEffect(isSystemInDarkTheme) {
            val animationSpec = tween<Color>(ColorChangeAnimationDurationMs)
            launch {
                animatedStart.animateTo(
                    targetValue = initStartColor,
                    animationSpec = animationSpec,
                )
            }
            animatedEnd.animateTo(
                targetValue = initEndColor,
                animationSpec = animationSpec,
            )
        }
    }

    return remember(isSystemInDarkTheme) {
        derivedStateOf {
            Brush.linearGradient(0f to animatedStart.value, 0.33f to animatedEnd.value)
        }
    }
}

private fun onNextClick(coroutineScope: CoroutineScope, pagerState: PagerState) {
    coroutineScope.launch {
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }
}

private fun onPreviousClick(coroutineScope: CoroutineScope, pagerState: PagerState) {
    coroutineScope.launch {
        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }
}

/**
 * Global accessibility for the pager. Set global actions depending on the current position of the user.
 */
private fun Modifier.accessibilityPager(
    currentIndex: Int,
    lastIndex: Int,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSkipClick: () -> Unit,
): Modifier {
    return composed {
        val nextActionLabel: String = stringResource(id = OSString.appPresentation_next_accessibility_customAction)
        val previousActionLabel: String =
            stringResource(id = OSString.appPresentation_previous_accessibility_customAction)
        val skipActionLabel: String = stringResource(id = OSString.appPresentation_skip_accessibility_customAction)
        val positionLabel: String =
            stringResource(OSString.appPresentation_slider_accessibility_progress, currentIndex, lastIndex)
        semantics {
            contentDescription = positionLabel
            customActions = listOfNotNull(
                accessibilityCustomAction(label = nextActionLabel, action = onNextClick).takeIf { currentIndex != lastIndex },
                accessibilityCustomAction(label = previousActionLabel, action = onPreviousClick).takeIf { currentIndex > 1 },
                accessibilityCustomAction(label = skipActionLabel, action = onSkipClick).takeIf { currentIndex != lastIndex },
            )
        }
    }
}

private const val ColorChangeAnimationDurationMs: Int = 2500
private const val PersonalizationPageIndex: Int = 1

@Composable
@OsDefaultPreview
fun AppPresentationScreenPreview() {
    OSTheme {
        AppPresentationScreen(
            presentationSteps = listOf(
                PresentationStep(
                    title = loremIpsumSpec(2),
                    description = loremIpsumSpec(30),
                    imageRes = OSDrawable.colored_illustration_type_coffre,
                    actions = listOf(PresentationAction(label = loremIpsumSpec(1), action = { })),
                ),
                PresentationStep(
                    title = loremIpsumSpec(2),
                    description = loremIpsumSpec(30),
                    imageRes = OSDrawable.colored_illustration_type_coffre,
                    actions = listOf(PresentationAction(label = loremIpsumSpec(1), action = { })),
                ),
            ),
            pagerState = rememberPagerState { 1 },
            navigateToNextStep = { },
            isAccessibilityEnabled = false,
            randomizePersonalizationBackground = true,
        )
    }
}

@Preview
@Composable
private fun HeaderPreview() {
    OSTheme {
        Column {
            Header(
                isFirstPage = true,
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
            )
            Header(
                isFirstPage = false,
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun FloatingButtonPreview() {
    OSTheme {
        Column {
            FloatingButton(
                key = "1",
                isSeeingFirstPage = false,
                isSeeingLastPage = false,
                onClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun FloatingButtonBouncePreview() {
    OSTheme {
        OSIconButton(
            modifier = Modifier.bounce(true),
            image = OSImageSpec.Drawable(OSDrawable.ic_arrow_down),
            onClick = { },
            contentDescription = null,
        )
    }
}
