package studio.lunabee.onesafe.feature.settings.personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.settings.SettingsConstants
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall
import studio.lunabee.onesafe.utils.OsDefaultPreview

class AppIconUi private constructor(
    val icon: AppIcon,
    val text: LbcTextSpec,
    val image: OSImageSpec,
    val background: Brush,
) {
    @Composable
    fun Composable(
        isSelected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.large)
                .then(other = modifier)
                .padding(vertical = OSDimens.SystemSpacing.Small),
        ) {
            Box(
                modifier = Modifier
                    .size(SettingsConstants.Personalization.AppIconSize)
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(background)
                    .then(
                        other = if (isSelected) {
                            Modifier.border(
                                width = SettingsConstants.Personalization.AppIconSelectedBorderWidth,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium,
                            )
                        } else {
                            Modifier
                        },
                    ),
            ) {
                OSImage(
                    image = image,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = OSDimens.SystemSpacing.ExtraSmall),
                )
            }
            OSSmallSpacer()
            OSText(
                text = text,
                modifier = Modifier
                    .width(width = SettingsConstants.Personalization.AppIconTextWidth),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.labelXSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
            )
        }
    }

    companion object {
        fun fromAppIcon(appIcon: AppIcon): AppIconUi = when (appIcon) {
            AppIcon.Default -> AppIconUi(
                icon = appIcon,
                text = LbcTextSpec.StringResource(OSString.application_name),
                image = OSImageSpec.Drawable(OSDrawable.ic_onesafe_logo, isIcon = false),
                background = SolidColor(Color.White),
            )
            AppIcon.DefaultDark -> AppIconUi(
                icon = appIcon,
                text = LbcTextSpec.StringResource(OSString.application_name),
                image = OSImageSpec.Drawable(OSDrawable.ic_onesafe_logo, isIcon = false),
                background = SolidColor(Color.Black),
            )
            AppIcon.ChessText -> AppIconUi(
                icon = appIcon,
                text = LbcTextSpec.StringResource(OSString.settings_personalization_appName_chess),
                image = OSImageSpec.Drawable(OSDrawable.ic_chess_text_icon, isIcon = false),
                background = SolidColor(Color.Black),
            )
            AppIcon.Chess -> AppIconUi(
                icon = appIcon,
                text = LbcTextSpec.StringResource(OSString.settings_personalization_appName_chess),
                image = OSImageSpec.Drawable(OSDrawable.ic_chess_icon, isIcon = false),
                background = Brush.verticalGradient(listOf(Color(0xE6DDE0FA), Color(0xE68FBAEC))),
            )
            AppIcon.Headphones -> AppIconUi(
                icon = appIcon,
                text = LbcTextSpec.StringResource(OSString.settings_personalization_appName_headphones),
                image = OSImageSpec.Drawable(OSDrawable.ic_headphone_icon, isIcon = false),
                background = SolidColor(Color(0xFF4D88CD)),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun AppIconUiPreview() {
    AppIconUi.fromAppIcon(AppIcon.Default).Composable(isSelected = false)
}
