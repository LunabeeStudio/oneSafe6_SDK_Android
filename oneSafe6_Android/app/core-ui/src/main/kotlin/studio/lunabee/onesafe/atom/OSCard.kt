package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

val LocalCardContentExtraSpace: ProvidableCompositionLocal<Dp?> = staticCompositionLocalOf { null }

@Composable
fun OSCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = cardElevationNone(),
    shape: Shape = CardDefaults.shape,
    colors: CardColors = cardColorsSurface(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val extraSpace = LocalCardContentExtraSpace.current
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        colors = colors,
    ) {
        extraSpace?.let { Spacer(modifier = Modifier.height(it)) }
        content()
        extraSpace?.let { Spacer(modifier = Modifier.height(it)) }
    }
}

@Composable
fun OSTopCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = cardElevationNone(),
    shape: Shape = RoundedCornerShape(
        topStart = MaterialTheme.shapes.medium.topStart,
        topEnd = MaterialTheme.shapes.medium.topEnd,
        bottomEnd = CornerSize(0),
        bottomStart = CornerSize(0),
    ),
    colors: CardColors = cardColorsSurface(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val extraSpace = LocalCardContentExtraSpace.current
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        colors = colors,
    ) {
        extraSpace?.let { Spacer(modifier = Modifier.height(it)) }
        content()
    }
}

@Composable
fun OSMiddleCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = cardElevationNone(),
    shape: Shape = RoundedCornerShape(CornerSize(0)),
    colors: CardColors = cardColorsSurface(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        colors = colors,
        content = content,
    )
}

@Composable
fun OSBottomCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = cardElevationNone(),
    shape: Shape = RoundedCornerShape(
        topStart = CornerSize(0),
        topEnd = CornerSize(0),
        bottomEnd = MaterialTheme.shapes.medium.bottomEnd,
        bottomStart = MaterialTheme.shapes.medium.bottomStart,
    ),
    colors: CardColors = cardColorsSurface(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val extraSpace = LocalCardContentExtraSpace.current
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        colors = colors,
    ) {
        content()
        extraSpace?.let { Spacer(modifier = Modifier.height(it)) }
    }
}

@Composable
private fun cardColorsSurface() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun cardElevationNone() = CardDefaults.cardElevation(
    defaultElevation = OSDimens.Elevation.None,
    pressedElevation = OSDimens.Elevation.None,
    focusedElevation = OSDimens.Elevation.None,
    draggedElevation = OSDimens.Elevation.None,
    hoveredElevation = OSDimens.Elevation.None,
    disabledElevation = OSDimens.Elevation.None,
)

@Composable
@OsDefaultPreview
private fun OSTopCardPreview() {
    OSPreviewBackgroundTheme {
        OSTopCard(
            modifier = Modifier
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .requiredHeight(30.dp),
            )
        }
    }
}

@Composable
@OsDefaultPreview
private fun OSMiddleCardPreview() {
    OSPreviewBackgroundTheme {
        OSMiddleCard(
            modifier = Modifier
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .requiredHeight(30.dp),
            )
        }
    }
}

@Composable
@OsDefaultPreview
private fun OSBottomCardPreview() {
    OSPreviewBackgroundTheme {
        OSBottomCard(
            modifier = Modifier
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .requiredHeight(30.dp),
            )
        }
    }
}
