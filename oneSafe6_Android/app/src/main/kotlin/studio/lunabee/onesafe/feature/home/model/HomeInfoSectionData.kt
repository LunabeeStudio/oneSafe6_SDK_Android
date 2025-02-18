package studio.lunabee.onesafe.feature.home.model

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPagerIndicator
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.commonui.home.HomeInfoData
import studio.lunabee.onesafe.commonui.home.HomeInfoDataNavScope
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.supportus.SupportUsHomeInfoData
import studio.lunabee.onesafe.importexport.ui.AutoBackupErrorHomeInfoData
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant

class HomeInfoSectionData(
    private val data: List<HomeInfoData>,
) {
    context(LazyGridScope, HomeInfoDataNavScope)
    fun item(
        isTouchExplorationEnabled: Boolean,
    ) {
        if (data.isNotEmpty()) {
            if (isTouchExplorationEnabled) {
                lazyVerticalOSRegularSpacer()
                items(
                    items = data,
                    key = { it.key },
                    contentType = { it.contentType },
                    span = { GridItemSpan(currentLineSpan = maxLineSpan) },
                ) { homeInfoData ->
                    homeInfoData.Composable(
                        modifier = Modifier
                            .padding(horizontal = OSDimens.SystemSpacing.Regular)
                            .padding(bottom = OSDimens.SystemSpacing.Regular),
                    )
                }
            } else {
                carouselItem()
            }
        }
    }

    context(LazyGridScope, HomeInfoDataNavScope)
    private fun carouselItem() {
        item(
            key = KeyHomeInfoSection,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
            contentType = ContentTypeHomeInfoSection,
        ) {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0f,
                    pageCount = { data.size },
                )

                HorizontalPagerIndicator(
                    modifier = Modifier.alpha(if (data.size == 1) 0f else 1f),
                    pagerState = pagerState,
                    pageCount = data.size,
                    activeColor = MaterialTheme.colorScheme.primary,
                )

                val pageSize = if (data.size == 1) PageSize.Fill else PageSize.Fixed(screenWidth * StepCardWidthRatio)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(vertical = OSDimens.SystemSpacing.Regular),
                    pageSize = pageSize,
                    pageSpacing = OSDimens.SystemSpacing.Regular,
                    verticalAlignment = Alignment.Top,
                    contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Regular),
                    beyondViewportPageCount = data.size,
                ) { page ->
                    data[page].Composable(Modifier)
                }
            }
        }
    }
}

private const val StepCardWidthRatio: Float = 0.85f
private const val KeyHomeInfoSection: String = "KeyHomeInfoSection"
private const val ContentTypeHomeInfoSection: String = "ContentTypeHomeInfoSection"

@OsDefaultPreview
@Composable
private fun HomeInfoSectionDataPreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular),
                columns = GridCells.Adaptive(minSize = OSSafeItemStyle.Regular.elementSize + OSDimens.SystemSpacing.Regular * 2),
            ) {
                HomeInfoSectionData(
                    List(3) { SupportUsHomeInfoData(Instant.EPOCH, {}) {} },
                ).item(false)
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun HomeInfoSectionDataSinglePreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular),
                columns = GridCells.Adaptive(minSize = OSSafeItemStyle.Regular.elementSize + OSDimens.SystemSpacing.Regular * 2),
            ) {
                HomeInfoSectionData(
                    List(1) { SupportUsHomeInfoData(Instant.EPOCH, {}) {} },
                ).item(false)
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun HomeInfoSectionDataTalkbackPreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular),
                columns = GridCells.Adaptive(minSize = OSSafeItemStyle.Regular.elementSize + OSDimens.SystemSpacing.Regular * 2),
            ) {
                HomeInfoSectionData(
                    listOf(
                        SupportUsHomeInfoData(Instant.EPOCH, {}) {},
                        AutoBackupErrorHomeInfoData(loremIpsumSpec(2), loremIpsumSpec(10), Instant.EPOCH) {},
                    ),
                ).item(true)
            }
        }
    }
}
