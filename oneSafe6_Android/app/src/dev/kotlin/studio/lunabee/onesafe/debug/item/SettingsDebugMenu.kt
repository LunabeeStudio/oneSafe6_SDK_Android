package studio.lunabee.onesafe.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.debug.model.DebugDatabaseEncryptionSettings
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.feature.itemform.model.option.time.UiFieldDatePicker
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow
import java.time.LocalDateTime

@Composable
fun SettingsDebugMenu(
    modifier: Modifier,
    itemOrder: ItemOrder,
    itemLayout: ItemLayout,
    cameraSystem: CameraSystem,
    databaseEncryptionSettings: DebugDatabaseEncryptionSettings,
    appIcon: AppIcon,
    setSetting: (Any) -> Unit,
    onPreventionDateUpdated: (newDate: LocalDateTime) -> Unit,
) {
    SettingsRow(
        text = "Order = ${itemOrder.name}",
        entries = ItemOrder.entries.filterNot { it == itemOrder },
        modifier = modifier,
        setSetting = setSetting,
    )
    SettingsRow(
        text = "Item style = ${itemLayout.name}",
        entries = ItemLayout.entries.filterNot { it == itemLayout },
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
        entries = DebugDatabaseEncryptionSettings.entries.filterNot { it.enabled == databaseEncryptionSettings.enabled },
        modifier = modifier,
        setSetting = setSetting,
    )
    SettingsRow(
        text = "App icon = ${appIcon.name}",
        entries = AppIcon.entries.filterNot { it == appIcon },
        modifier = modifier,
        setSetting = setSetting,
    )
    var isDatePickerVisible: Boolean by remember { mutableStateOf(false) }
    OSRow(
        text = LbcTextSpec.Raw("Set prevention warning date"),
        modifier = modifier
            .clickable { isDatePickerVisible = !isDatePickerVisible },
    )
    if (isDatePickerVisible) {
        UiFieldDatePicker(
            dateTime = LocalDateTime.now(),
            onDismiss = { isDatePickerVisible = !isDatePickerVisible },
            onValueChanged = onPreventionDateUpdated,
        )
    }
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
            .clickable { expand = true }
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
