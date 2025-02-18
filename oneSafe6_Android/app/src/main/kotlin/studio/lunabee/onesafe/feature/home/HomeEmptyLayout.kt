package studio.lunabee.onesafe.feature.home

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPagerIndicator
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.home.TextLogo
import studio.lunabee.onesafe.feature.home.model.EmptyHomePresentationStep
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun HomeEmptyLayout(
    importDiscoverData: () -> Unit,
    areItemsBeingGenerated: Boolean,
    navigateToImportSafe: () -> Unit,
    isTouchExplorationEnabled: Boolean = rememberOSAccessibilityState().isTouchExplorationEnabled,
) {
    val scrollState = rememberScrollState()
    val context: Context = LocalContext.current

    val presentationSteps = buildList {
        context.packageManager.getLaunchIntentForPackage(BuildConfig.ONESAFE_5_PACKAGE)
            ?.setAction(BuildConfig.ONESAFE_5_PACKAGE + ".MIGRATION")
            ?.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NEW_TASK)
            ?.let { intent ->
                add(EmptyHomePresentationStep.Migrate(onClick = { context.startActivity(intent) }))
            }
        // Hide loading state when talkback enable in favor of toast
        if (!isTouchExplorationEnabled || !areItemsBeingGenerated) {
            add(
                EmptyHomePresentationStep.Discover(
                    onClick = importDiscoverData,
                    isLoading = areItemsBeingGenerated,
                ),
            )
        }
        add(EmptyHomePresentationStep.Import(onClick = navigateToImportSafe))
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { presentationSteps.size },
    )
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextLogo(
            modifier = Modifier
                .width(OSDimens.LayoutSize.HomeLogoText)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(
                    top = OSDimens.SystemButton.Regular + OSDimens.SystemSpacing.Regular,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Large,
                ),
        )

        if (isTouchExplorationEnabled) {
            presentationSteps.forEachIndexed { idx, step ->
                step.Card(
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.Regular),
                )
                if (idx != presentationSteps.lastIndex) {
                    OSRegularSpacer()
                }
            }
        } else {
            PresentationCarousel(pagerState, presentationSteps, screenWidth)
        }
    }
}

context(ColumnScope)
@Composable
private fun PresentationCarousel(
    pagerState: PagerState,
    presentationSteps: List<EmptyHomePresentationStep>,
    screenWidth: Dp,
) {
    HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = presentationSteps.size,
        activeColor = MaterialTheme.colorScheme.primary,
    )

    HorizontalPager(
        state = pagerState,
        pageSpacing = OSDimens.SystemSpacing.Regular,
        pageSize = PageSize.Fixed(screenWidth * StepCardWidthRatio),
        contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .testTag(UiConstants.TestTag.Item.HomeEmptyCarousel)
            .padding(vertical = OSDimens.SystemSpacing.Regular),
        beyondViewportPageCount = presentationSteps.size,
    ) { page ->
        presentationSteps[page].Card(modifier = Modifier)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    ) {
        if (maxHeight > MinIllustrationHeight) {
            androidx.compose.animation.AnimatedVisibility(
                visible = pagerState.currentPage == AddDiscoverItemsStep,
                exit = fadeOut(),
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = defaultAnimTween,
                ),
            ) {
                EmptyHomeIllustration(illustrationId = OSDrawable.character_add_item, alignment = Alignment.BottomEnd)
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = pagerState.currentPage == ImportStep,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = defaultAnimTween,
                ),
                exit = fadeOut(),
            ) {
                EmptyHomeIllustration(illustrationId = OSDrawable.character_jamy_proud, alignment = Alignment.BottomCenter)
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = pagerState.currentPage == MigrateFromOS5Step,
                enter = slideInHorizontally(
                    animationSpec = defaultAnimTween,
                    initialOffsetX = { fullWidth -> fullWidth },
                ),
                exit = fadeOut(),
            ) {
                EmptyHomeIllustration(illustrationId = OSDrawable.character_jamy_phone, alignment = Alignment.BottomStart)
            }
        }
    }
}

@Composable
private fun EmptyHomeIllustration(
    @DrawableRes illustrationId: Int,
    alignment: Alignment,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .accessibilityInvisibleToUser(),
    ) {
        Image(
            painter = painterResource(id = illustrationId),
            modifier = Modifier
                .fillMaxWidth(fraction = AppConstants.Ui.HomeEmpty.MaxWidthRatioImageEmptyScreen)
                .sizeIn(maxHeight = MaxIllustrationSize)
                .fillMaxHeight()
                .align(alignment),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            alignment = alignment,
        )
    }
}

// Determined with Figma
private const val StepCardWidthRatio: Float = 0.85f
private val MinIllustrationHeight: Dp = 150.dp
private val MaxIllustrationSize: Dp = 300.dp
private const val AddDiscoverItemsStep: Int = 0
private const val ImportStep: Int = 1
private const val MigrateFromOS5Step: Int = 2
private val defaultAnimTween: FiniteAnimationSpec<IntOffset> = tween(delayMillis = 300, durationMillis = 300)

@OsDefaultPreview
@Composable
private fun HomeEmptyLayoutPreview() {
    OSPreviewBackgroundTheme {
        HomeEmptyLayout(
            importDiscoverData = {},
            areItemsBeingGenerated = false,
            navigateToImportSafe = {},
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeEmptyLayoutTalkbackPreview() {
    OSPreviewBackgroundTheme {
        HomeEmptyLayout(
            importDiscoverData = {},
            areItemsBeingGenerated = false,
            navigateToImportSafe = {},
            isTouchExplorationEnabled = true,
        )
    }
}
