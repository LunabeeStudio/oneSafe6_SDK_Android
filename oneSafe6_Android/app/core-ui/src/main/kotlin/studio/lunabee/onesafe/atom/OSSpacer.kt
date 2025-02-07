package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ColumnScope.OSRegularSpacer() {
    OSVerticalRegularSpacer()
}

@Composable
fun RowScope.OSRegularSpacer() {
    Spacer(
        modifier = Modifier
            .width(OSDimens.SystemSpacing.Regular),
    )
}

fun LazyGridScope.lazyVerticalOSRegularSpacer() {
    item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
        OSVerticalRegularSpacer()
    }
}

fun LazyListScope.lazyVerticalOSRegularSpacer() {
    item {
        OSVerticalRegularSpacer()
    }
}

@Composable
private fun OSVerticalRegularSpacer() {
    Spacer(
        modifier = Modifier
            .height(OSDimens.SystemSpacing.Regular),
    )
}

@Composable
fun ColumnScope.OSExtraLargeSpacer() {
    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.ExtraLarge))
}

@Composable
fun ColumnScope.OSSmallSpacer() {
    Spacer(
        modifier = Modifier
            .height(OSDimens.SystemSpacing.Small),
    )
}

@Composable
fun RowScope.OSSmallSpacer() {
    Spacer(
        modifier = Modifier
            .width(OSDimens.SystemSpacing.Small),
    )
}

@Composable
fun RowScope.OSMediumSpacer() {
    Spacer(
        modifier = Modifier
            .width(OSDimens.SystemSpacing.Medium),
    )
}

@Composable
fun ColumnScope.OSExtraSmallSpacer() {
    Spacer(
        modifier = Modifier
            .height(OSDimens.SystemSpacing.ExtraSmall),
    )
}

@Composable
fun RowScope.OSExtraSmallSpacer() {
    Spacer(
        modifier = Modifier
            .width(OSDimens.SystemSpacing.ExtraSmall),
    )
}
