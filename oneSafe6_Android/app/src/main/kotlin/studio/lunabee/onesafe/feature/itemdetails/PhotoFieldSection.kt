package studio.lunabee.onesafe.feature.itemdetails

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.extensions.getFileSharingIntent
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.getMimeType
import studio.lunabee.onesafe.feature.itemdetails.model.FileData
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntryFileField
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.io.File
import java.util.UUID

fun photoFieldSection(
    fields: List<InformationTabEntryFileField>,
    cardContents: MutableList<OSLazyCardContent>,
    navigateToFileViewer: (UUID) -> Unit,
    saveFile: (Uri, File) -> Unit,
    displayTitle: Boolean,
) {
    if (fields.isNotEmpty()) {
        cardContents += object : OSLazyCardContent.Item {
            override val key: Any = OSString.fieldName_photosAndVideos
            override val contentType: Any = "Photo Title"

            @Composable
            override fun Content(padding: PaddingValues, modifier: Modifier) {
                if (displayTitle) {
                    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Small))
                    OSText(
                        text = LbcTextSpec.StringResource(OSString.fieldName_photosAndVideos),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalDesignSystem.current.rowLabelColor,
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    )
                }
                Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Small))
            }
        }
    }
    osLazyCardContentGrid(
        columns = if (fields.size == 1 && !displayTitle) 1 else 3,
        itemCount = fields.size,
        cardContents = cardContents,
    ) { index ->
        val entry = fields[index]
        val thumbnail by entry.thumbnail.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

        // Create launcher once we get the fileData
        var fileData: FileData? by remember { mutableStateOf(null) }
        val saveFileLauncher = fileData?.mimeType?.let { type ->
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument(type),
            ) { uri ->
                // Reset file data to remove the launcher
                fileData = null
                entry.file.value?.let { file ->
                    uri?.let { saveFile(uri, file) }
                }
            }
        }
        // Once we create the launcher, we launch with file name
        LaunchedEffect(saveFileLauncher) {
            fileData?.name?.let { name ->
                saveFileLauncher?.launch(name)
            }
        }

        ItemDetailsPhotoFieldLayout(
            thumbnail = thumbnail,
            share = {
                if (!isLoading) {
                    isLoading = true
                    entry.loadFile()
                    coroutineScope.launch {
                        val file = entry.file.filterNotNull().first()
                        isLoading = false
                        val shareIntent = context.getFileSharingIntent(
                            fileToShare = file,
                            mimeType = entry.kind.mimeType,
                        )
                        launcher.launch(Intent.createChooser(shareIntent, entry.name.string(context)))
                    }
                }
            },
            visualize = {
                navigateToFileViewer(entry.id)
            },
            contentDescription = entry.name,
            download = {
                if (!isLoading) {
                    isLoading = true
                    entry.loadFile()
                    coroutineScope.launch {
                        val file = entry.file.filterNotNull().first()
                        val mimeType = Uri.fromFile(file).getMimeType(context)
                        val fileName = file.name
                        fileData = FileData(
                            mimeType = mimeType ?: AppConstants.FileProvider.LauncherAllFileFilter,
                            name = fileName,
                        )
                        isLoading = false
                    }
                }
            },
        )
    }
}

fun osLazyCardContentGrid(
    cardContents: MutableList<OSLazyCardContent>,
    columns: Int,
    itemCount: Int,
    content: @Composable (Int) -> Unit,
) {
    var rows = (itemCount / columns)
    if (itemCount.mod(columns) > 0) {
        rows += 1
    }

    repeat(rows) { rowId ->
        val firstIndex = rowId * columns
        cardContents += object : OSLazyCardContent.Item {
            override val key: Any = "MediaGrid_$rowId"
            override val contentType: Any = "MediaRow"

            @Composable
            override fun Content(padding: PaddingValues, modifier: Modifier) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                    ) {
                        repeat(columns) { columnId ->
                            val index = firstIndex + columnId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            ) {
                                if (index < itemCount) {
                                    content(index)
                                }
                            }
                        }
                    }
                    OSRegularSpacer()
                }
            }
        }
    }
}
