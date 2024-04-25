package studio.lunabee.onesafe.help.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.help.debug.model.DebugDatabaseEncryptionSettings
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow

@Composable
internal fun SettingsDebugMenu(
    modifier: Modifier,
    itemOrder: ItemOrder,
    itemsLayoutSetting: ItemsLayoutSettings,
    cameraSystem: CameraSystem,
    databaseEncryptionSettings: DebugDatabaseEncryptionSettings,
    setSetting: (Any) -> Unit,
) {
    SettingsRow(
        text = "Order = ${itemOrder.name}",
        entries = ItemOrder.entries.filterNot { it == itemOrder },
        modifier = modifier,
        setSetting = setSetting,
    )
    SettingsRow(
        text = "Item style = ${itemsLayoutSetting.name}",
        entries = ItemsLayoutSettings.entries.filterNot { it == itemsLayoutSetting },
        modifier = modifier,
        setSetting = setSetting,
    )
    SettingsRow(
        text = "Camera = ${cameraSystem.name}",
        entries = CameraSystem.entries.filterNot { it == cameraSystem },
        modifier = modifier,
        setSetting = setSetting,
    )
    SettingsRow(
        text = "Database encryption = ${databaseEncryptionSettings.name}",
        entries = emptyList(),
        modifier = modifier,
        setSetting = setSetting,
    )
}

@Composable
private fun <E : Enum<E>> SettingsRow(
    text: String,
    entries: List<E>,
    modifier: Modifier = Modifier,
    setSetting: (Any) -> Unit,
) {
    var expand by remember { mutableStateOf(false) }
    OSRow(
        text = LbcTextSpec.Raw(text),
        modifier = Modifier
            .clickable { expand = entries.isNotEmpty() }
            .then(modifier),
    ) {
        DropdownMenu(
            expanded = expand,
            onDismissRequest = { expand = false },
        ) {
            entries.forEach { entry ->
                OSDropdownMenuItem(text = LbcTextSpec.Raw(entry.name), null) {
                    setSetting(entry)
                    expand = false
                }
            }
        }
    }
}
