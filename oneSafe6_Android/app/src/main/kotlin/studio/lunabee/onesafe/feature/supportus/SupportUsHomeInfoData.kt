package studio.lunabee.onesafe.feature.supportus

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.home.HomeInfoData
import studio.lunabee.onesafe.commonui.home.HomeInfoDataNavScope
import studio.lunabee.onesafe.commonui.home.HomeInfoType
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant

class SupportUsHomeInfoData(
    visibleSince: Instant,
    private val onDismiss: () -> Unit,
    private val onClickOnSupportUs: (Context) -> Unit,
) : HomeInfoData(
    type = HomeInfoType.Standard,
    key = KeySupportUsCard,
    contentType = ContentTypeSupportUsCard,
    visibleSince = visibleSince,
) {
    context(HomeInfoDataNavScope)
    @Composable
    override fun Composable(modifier: Modifier) {
        var isSupportBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
        val context = LocalContext.current

        OSMessageCard(
            title = LbcTextSpec.StringResource(OSString.supportUs_card_title),
            description = LbcTextSpec.StringResource(OSString.supportUs_card_description),
            attributes = OSMessageCardAttributes()
                .dismissible(
                    icon = OSImageSpec.Drawable(OSDrawable.ic_baseline_close),
                    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                    onDismiss = onDismiss,
                ),
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.SupportUsCard),
            action = {
                OSTextButton(
                    text = LbcTextSpec.StringResource(OSString.supportUs_card_knowMoreButton),
                    onClick = { isSupportBottomSheetVisible = true },
                    modifier = Modifier.padding(bottom = OSDimens.SystemSpacing.Small),
                )
            },
        )

        SupportUsBottomSheet(
            isVisible = isSupportBottomSheetVisible,
            onBottomSheetClosed = { isSupportBottomSheetVisible = false },
            onClickOnSupportUs = {
                onClickOnSupportUs(context)
                isSupportBottomSheetVisible = false
            },
        )
    }
}

private const val KeySupportUsCard: String = "KeySupportUsCard"
private const val ContentTypeSupportUsCard: String = "ContentTypeSupportUsCard"

@OsDefaultPreview
@Composable
private fun SupportUsHomeInfoDataPreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            SupportUsHomeInfoData(
                visibleSince = Instant.EPOCH,
                onDismiss = {},
            ) {}.Composable(Modifier)
        }
    }
}
