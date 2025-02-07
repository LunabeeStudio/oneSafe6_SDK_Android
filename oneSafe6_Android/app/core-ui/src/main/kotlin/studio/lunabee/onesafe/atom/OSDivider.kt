package studio.lunabee.onesafe.atom

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun OSSmallDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = OSDimens.DividerThickness.Small,
    )
}

@Composable
fun OSRegularDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = OSDimens.DividerThickness.Regular,
    )
}

@Composable
fun OSLargeDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = OSDimens.DividerThickness.Large,
    )
}

object OSDivider {

    fun itemRegular(
        scope: LazyListScope,
        modifier: Modifier = Modifier,
    ) {
        scope.item {
            OSRegularDivider(modifier)
        }
    }

    fun itemLarge(
        scope: LazyListScope,
        modifier: Modifier = Modifier,
    ) {
        scope.item {
            OSLargeDivider(modifier)
        }
    }
}
