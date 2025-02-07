package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate

import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.feature.itemform.model.ItemInitialData
import java.util.UUID

data class ItemDataForSaving(
    val name: String,
    val icon: OSImageSpec?,
    val color: Color?,
    val itemFieldsData: List<ItemFieldData>,
    val initialInfo: ItemInitialData?,
    val fileSavingData: List<FileSavingData>,
)

interface SaveItemAndFieldDelegate {
    suspend fun save(
        data: ItemDataForSaving,
    ): LBResult<UUID>
}
