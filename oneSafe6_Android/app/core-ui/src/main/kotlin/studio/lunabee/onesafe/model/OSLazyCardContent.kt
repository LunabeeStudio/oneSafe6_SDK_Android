package studio.lunabee.onesafe.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.ui.res.OSDimens

@Stable
sealed interface OSLazyCardContent {
    @Stable
    interface Item : OSLazyCardContent {
        val key: Any?
        val contentType: Any

        @Composable
        fun Content(padding: PaddingValues, modifier: Modifier)
    }

    @Stable
    fun interface Paged : OSLazyCardContent {
        context(LazyListScope)
        fun pagedContent(positionInList: Position)
    }

    enum class Position {
        TOP, MIDDLE, BOTTOM, SINGLE;

        fun padding(): PaddingValues {
            return when (this) {
                TOP -> PaddingValues(
                    top = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Regular / 2,
                )
                MIDDLE -> PaddingValues(
                    top = OSDimens.SystemSpacing.Regular / 2,
                    bottom = OSDimens.SystemSpacing.Regular / 2,
                )
                BOTTOM -> PaddingValues(
                    top = OSDimens.SystemSpacing.Regular / 2,
                    bottom = OSDimens.SystemSpacing.Regular,
                )
                SINGLE -> PaddingValues(
                    top = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Regular,
                )
            }
        }

        companion object {
            fun fromIndex(idx: Int, lastIndex: Int): Position = when {
                lastIndex == 0 -> SINGLE
                idx == 0 -> TOP
                idx == lastIndex -> BOTTOM
                else -> MIDDLE
            }

            fun fromIndexPaged(idx: Int, lastIndex: Int, positionInParent: Position): Position {
                return if (positionInParent == MIDDLE) {
                    MIDDLE
                } else {
                    when (fromIndex(idx, lastIndex)) {
                        MIDDLE -> MIDDLE
                        BOTTOM -> {
                            if (positionInParent == BOTTOM || positionInParent == SINGLE) {
                                BOTTOM
                            } else {
                                MIDDLE
                            }
                        }
                        TOP -> if (positionInParent == TOP || positionInParent == SINGLE) {
                            TOP
                        } else {
                            MIDDLE
                        }
                        SINGLE -> positionInParent
                    }
                }
            }
        }
    }
}
