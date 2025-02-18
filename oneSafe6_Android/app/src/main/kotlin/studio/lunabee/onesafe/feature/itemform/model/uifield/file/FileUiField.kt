package studio.lunabee.onesafe.feature.itemform.model.uifield.file

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.StateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.feature.itemform.model.uifield.ItemFieldActions
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.molecule.OSRow
import java.util.UUID

abstract class FileUiField : UiField() {
    abstract val fileId: UUID
    abstract val fileExtension: String?

    override val errorFieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_file_errorLabel)

    override var placeholder: LbcTextSpec = LbcTextSpec.Raw("")
    override val isSecured: Boolean = false

    abstract val thumbnailFlow: StateFlow<ThumbnailState>

    abstract fun getFileSavingData(): FileSavingData.ToSave?

    /**
     * Delete the cached plain file
     */
    abstract fun deletePlainCache(): Boolean

    override fun getItemFieldAction(
        toggleIdentifier: () -> Unit,
        renameField: () -> Unit,
        tryToDeleteField: () -> Unit,
        useThumbnailAsIcon: (OSImageSpec) -> Unit,
    ): List<ItemFieldActions> {
        return listOfNotNull(
            (thumbnailFlow.value as? ThumbnailState.Finished)?.thumbnail?.let {
                ItemFieldActions.UseAsIcon {
                    useThumbnailAsIcon(
                        it,
                    )
                }
            },
            ItemFieldActions.RenameFile(onClick = renameField),
            ItemFieldActions.DeleteField(onClick = tryToDeleteField),
        )
    }

    override fun getDisplayedValue(): String = "$fileId${Constant.FileTypeExtSeparator}${fileExtension.orEmpty()}"
    override fun initValues(isIdentifier: Boolean, initialValue: String?) {
        this.isIdentifier = isIdentifier
    }

    @Composable
    override fun InnerComposable(modifier: Modifier, hasNext: Boolean) {
        val thumbnail by thumbnailFlow.collectAsStateWithLifecycle()
        Column {
            OSRow(
                text = fieldDescription.value,
                textMaxLines = 1,
                modifier = modifier,
                startContent = {
                    val shimmerColor = MaterialTheme.colorScheme.primary
                    when (val safeThumbnail = (thumbnail as? ThumbnailState.Finished)?.thumbnail) {
                        null -> {
                            Box(
                                modifier = Modifier
                                    .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                                    .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius))
                                    .shimmer()
                                    .drawBehind { drawRect(shimmerColor) },
                            )
                        }
                        else -> {
                            OSImage(
                                image = safeThumbnail,
                                modifier = Modifier
                                    .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                                    .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                },
            )
            if (isErrorDisplayed) {
                OSText(
                    text = errorFieldLabel,
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
