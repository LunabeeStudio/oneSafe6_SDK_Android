package studio.lunabee.onesafe.feature.importbackup.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.text.OSText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed interface ImportAuthArchiveKindLabels {
    @Composable
    fun Description()

    @Composable
    fun Hint()

    object Share : ImportAuthArchiveKindLabels {
        @Composable
        override fun Description() {
            OSText(
                text = LbcTextSpec.StringResource(OSString.import_decryptImportCard_descriptionSharing),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        @Composable
        override fun Hint() {
            /* no-op */
        }
    }

    class Backup(private val archiveCreationDate: LocalDateTime?) : ImportAuthArchiveKindLabels {
        @Composable
        override fun Description() {
            OSText(
                text = LbcTextSpec.StringResource(OSString.import_decryptImportCard_description),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        @Composable
        override fun Hint() {
            if (archiveCreationDate != null) {
                OSText(
                    text = LbcTextSpec.StringResource(
                        id = OSString.import_decryptImportCard_passwordHint,
                        DateTimeFormatter
                            .ofPattern(stringResource(id = OSString.import_decryptImportCard_passwordHint_dateFormat))
                            .format(archiveCreationDate),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
