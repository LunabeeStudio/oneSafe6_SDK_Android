package studio.lunabee.onesafe.common.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

@Composable
fun <T : Any> rememberMutableStateListOf(elements: List<T>, saver: Saver<T, Any> = autoSaver()): SnapshotStateList<T> {
    return rememberSaveable(
        saver = listSaver(
            save = { stateList ->
                with(saver) { stateList.toList().map { save(it) } }
            },
            restore = { restoredList ->
                with(saver) { restoredList.map { restore(it!!) as T } }.toMutableStateList()
            },
        ),
    ) {
        elements.toList().toMutableStateList()
    }
}
