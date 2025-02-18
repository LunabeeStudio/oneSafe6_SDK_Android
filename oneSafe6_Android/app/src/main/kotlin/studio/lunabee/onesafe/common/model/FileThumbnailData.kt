package studio.lunabee.onesafe.common.model

import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import java.util.UUID

sealed interface FileThumbnailData {

    val imageSpec: OSImageSpec

    data class Data(
        override val imageSpec: OSImageSpec,
    ) : FileThumbnailData

    enum class FileThumbnailPlaceholder(override val imageSpec: OSImageSpec, val id: UUID) : FileThumbnailData {
        Image(
            OSImageSpec.Drawable(OSDrawable.ic_image_placeholder, null, false),
            UUID.fromString("1ba5321c-dd5d-420c-9440-5a7163d5cb05"),
        ),
        Music(
            OSImageSpec.Drawable(OSDrawable.ic_music_placeholder, null, false),
            UUID.fromString("7794daed-f24e-4325-b9fe-4158159e23c9"),
        ),
        Video(
            OSImageSpec.Drawable(OSDrawable.ic_video_placeholder, null, false),
            UUID.fromString("1272cd27-11eb-4a52-a972-dcd5be25addf"),
        ),
        File(
            OSImageSpec.Drawable(OSDrawable.ic_file_placeholder, null, false),
            UUID.fromString("2e94282c-d346-4f22-8b26-f7866e779936"),
        ),
    }
}
