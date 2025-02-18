package studio.lunabee.onesafe.feature.settings.bubbles

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import studio.lunabee.onesafe.organism.card.OSMessageCardStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant

class BubblesHomeCtaInfoData(
    visibleSince: Instant,
    private val onDismiss: () -> Unit,
) : HomeInfoData(
    type = HomeInfoType.Standard,
    key = KeyBubblesCard,
    contentType = ContentTypeBubblesCard,
    visibleSince = visibleSince,
) {
    context(HomeInfoDataNavScope)
    @Composable
    override fun Composable(modifier: Modifier) {
        OSMessageCard(
            title = LbcTextSpec.StringResource(OSString.home_bubblesCard_title),
            description = LbcTextSpec.StringResource(OSString.home_bubblesCard_message),
            attributes = OSMessageCardAttributes()
                .dismissible(
                    icon = OSImageSpec.Drawable(OSDrawable.ic_baseline_close),
                    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                    onDismiss = onDismiss,
                )
                .style(OSMessageCardStyle.Default),
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.BubblesCard),
            action = {
                OSTextButton(
                    text = LbcTextSpec.StringResource(OSString.home_bubblesCard_button),
                    onClick = navigateFromHomeInfoDataToBubblesOnBoarding,
                    modifier = Modifier.padding(bottom = OSDimens.SystemSpacing.Small),
                )
            },
        )
    }
}

private const val KeyBubblesCard: String = "KeyBubblesCard"
private const val ContentTypeBubblesCard: String = "ContentTypeBubblesCard"

@OsDefaultPreview
@Composable
private fun BubblesHomeInfoDataPreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            BubblesHomeCtaInfoData(
                visibleSince = Instant.EPOCH,
                onDismiss = {},
            ).Composable(Modifier)
        }
    }
}
