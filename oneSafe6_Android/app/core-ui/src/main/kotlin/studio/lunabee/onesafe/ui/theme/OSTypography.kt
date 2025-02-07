package studio.lunabee.onesafe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.ui.res.OSDimens

object OSTypography {

    val Legibility: FontFamily = FontFamily(
        Font(R.font.consola, FontWeight.W400),
    )

    val Typography: Typography
        @Composable
        get() = Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                lineHeight = OSDimens.SystemLineHeight.HeadlineLarge,
                fontSize = OSDimens.SystemTextSize.HeadlineLarge,
                fontWeight = FontWeight.Medium,
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                lineHeight = OSDimens.SystemLineHeight.TitleLarge,
                fontSize = OSDimens.SystemTextSize.TitleLarge,
                fontWeight = FontWeight.Medium,
            ),
            titleMedium = MaterialTheme.typography.titleLarge.copy(
                fontSize = OSDimens.SystemTextSize.TitleMedium,
                fontWeight = FontWeight.SemiBold,
            ),
            labelLarge = MaterialTheme.typography.labelLarge.copy(
                lineHeight = OSDimens.SystemLineHeight.LabelLarge,
                fontSize = OSDimens.SystemTextSize.LabelLarge,
                fontWeight = FontWeight.Medium,
            ),
            labelMedium = MaterialTheme.typography.labelMedium.copy(
                lineHeight = OSDimens.SystemLineHeight.LabelMedium,
                fontSize = OSDimens.SystemTextSize.LabelMedium,
                fontWeight = FontWeight.Medium,
            ),
            labelSmall = MaterialTheme.typography.labelSmall.copy(
                lineHeight = OSDimens.SystemLineHeight.LabelSmall,
                fontSize = OSDimens.SystemTextSize.LabelSmall,
                fontWeight = FontWeight.Medium,
            ),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = OSDimens.SystemLineHeight.BodyLarge,
                fontSize = OSDimens.SystemTextSize.BodyLarge,
                fontWeight = FontWeight.Normal,
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = OSDimens.SystemLineHeight.BodyMedium,
                fontSize = OSDimens.SystemTextSize.BodyMedium,
                fontWeight = FontWeight.Normal,
            ),
            bodySmall = MaterialTheme.typography.bodySmall.copy(
                lineHeight = OSDimens.SystemLineHeight.BodySmall,
                fontSize = OSDimens.SystemTextSize.BodySmall,
                fontWeight = FontWeight.Normal,
            ),
        )

    val Typography.titleLargeBlack: TextStyle
        @Composable
        get() = titleLarge.copy(
            lineHeight = OSDimens.SystemLineHeight.TitleLargeBlack,
            fontWeight = FontWeight.Black,
        )

    val Typography.titleMediumBlack: TextStyle
        @Composable
        get() = titleMedium.copy(
            fontWeight = FontWeight.Black,
        )

    val Typography.labelXSmall: TextStyle
        @Composable
        get() = labelSmall.copy(
            fontSize = OSDimens.SystemTextSize.LabelXSmall,
            fontWeight = FontWeight.Medium,
            lineHeight = TextUnit.Unspecified,
        )

    val Typography.labelSmallRegular: TextStyle
        @Composable
        get() = labelSmall.copy(
            fontWeight = FontWeight.Normal,
        )
}
