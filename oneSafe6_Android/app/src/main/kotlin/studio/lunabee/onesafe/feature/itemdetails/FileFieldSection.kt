package studio.lunabee.onesafe.feature.itemdetails

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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

fun fileFieldSection(
    fields: List<InformationTabEntryFileField>,
    cardContents: MutableList<OSLazyCardContent>,
    navigateToFileViewer: (UUID) -> Unit,
    saveFile: (Uri, File) -> Unit,
) {
    fields.mapIndexedTo(cardContents) { index, entry ->
        object : OSLazyCardContent.Item {
            override val key: Any = entry.id
            override val contentType: Any = "File"

            @Composable
            override fun Content(padding: PaddingValues, modifier: Modifier) {
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

                if (index == 0) {
                    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Small))
                    OSText(
                        text = LbcTextSpec.StringResource(OSString.fieldName_file_plural),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalDesignSystem.current.rowLabelColor,
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    )
                }

                ItemDetailsFieldInformationRow(
                    thumbnail = thumbnail,
                    name = entry.name,
                    isLoading = isLoading,
                    share = {
                        if (!isLoading) {
                            isLoading = true
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
                    download = {
                        if (!isLoading) {
                            isLoading = true
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
                if (index == fields.lastIndex) {
                    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Small))
                }
            }
        }
    }
}
