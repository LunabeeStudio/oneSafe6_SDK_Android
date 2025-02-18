package studio.lunabee.onesafe.debug.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.ui.extensions.hexValue
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

data class DevPlainItem(
    val id: UUID,
    val name: String?,
    val parentId: UUID?,
    val isFavorite: Boolean,
    val updatedAt: Instant,
    val position: Double,
    val iconId: UUID?,
    val color: Color?,
    val deletedAt: Instant?,
    val deletedParentId: UUID?,
    val indexAlpha: Double,
    val createdAt: Instant,
) {

    context(ColumnScope)
    @Composable
    fun Composable() {
        OSText(LbcTextSpec.Raw("**id** = $id").markdown())
        OSText(LbcTextSpec.Raw("**name** = $name").markdown())
        OSText(LbcTextSpec.Raw("**parentId** = $parentId").markdown())
        OSText(LbcTextSpec.Raw("**isFavorite** = $isFavorite").markdown())
        OSText(LbcTextSpec.Raw("**updatedAt** = ${updatedAt.atZone(ZoneId.systemDefault())}").markdown())
        OSText(LbcTextSpec.Raw("**position** = $position").markdown())
        OSText(LbcTextSpec.Raw("**iconId** = $iconId").markdown())
        Row(
            Modifier.height(intrinsicSize = IntrinsicSize.Min),
        ) {
            OSText(LbcTextSpec.Raw("**color** = ${color?.hexValue}").markdown())
            color?.let {
                OSSmallSpacer()
                Box(
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .background(color),
                )
            }
        }
        OSText(LbcTextSpec.Raw("**deletedAt** = ${deletedAt?.atZone(ZoneId.systemDefault())}").markdown())
        OSText(LbcTextSpec.Raw("**deletedParentId** = $deletedParentId").markdown())
        OSText(LbcTextSpec.Raw("**indexAlpha** = $indexAlpha").markdown())
        OSText(LbcTextSpec.Raw("**createdAt** = ${createdAt.atZone(ZoneId.systemDefault())}").markdown())
    }
}

@Preview
@Composable
private fun DevPlainItemPreview() {
    OSPreviewOnSurfaceTheme {
        Column {
            DevPlainItem(
                id = UUID.randomUUID(),
                name = loremIpsum(3),
                parentId = UUID.randomUUID(),
                isFavorite = false,
                updatedAt = Instant.EPOCH,
                position = 4.25,
                iconId = null,
                color = Color.Blue,
                deletedAt = Instant.now(),
                deletedParentId = UUID.randomUUID(),
                indexAlpha = 8.657,
                createdAt = Instant.EPOCH,
            ).Composable()
        }
    }
}
