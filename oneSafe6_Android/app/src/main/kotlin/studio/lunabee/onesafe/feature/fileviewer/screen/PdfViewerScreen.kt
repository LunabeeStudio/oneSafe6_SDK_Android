package studio.lunabee.onesafe.feature.fileviewer.screen

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.fileviewer.model.fileViewerTopBarAction
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun PdfViewerScreen(
    onBackClick: () -> Unit,
    uri: Uri,
    title: String,
    actions: List<FileFieldAction>,
) {
    val pdfState = rememberVerticalPdfReaderState(
        resource = ResourceType.Local(uri),
        isZoomEnable = true,
    )
    val error = pdfState.error
    if (error != null) {
        val errorText = when (error) {
            is SecurityException -> LbcTextSpec.StringResource(OSString.safeItemDetail_fields_pdf_viewer_protectedError)
            else -> LbcTextSpec.StringResource(OSString.safeItemDetail_fields_pdf_viewer_corruptedError)
        }
        UnknownViewerScreen(
            onBackClick = onBackClick,
            name = title,
            actions = actions,
            text = errorText,
        )
    } else {
        OSScreen(testTag = UiConstants.TestTag.Screen.FileViewerScreen) {
            Column {
                ElevatedTopAppBar(
                    title = LbcTextSpec.Raw(title),
                    options = listOf(
                        topAppBarOptionNavBack(onBackClick),
                        fileViewerTopBarAction(actions),
                    ),
                    elevation = OSDimens.Elevation.TopAppBarElevation,
                )
                VerticalPDFReader(
                    state = pdfState,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                )
            }
        }
    }
}
