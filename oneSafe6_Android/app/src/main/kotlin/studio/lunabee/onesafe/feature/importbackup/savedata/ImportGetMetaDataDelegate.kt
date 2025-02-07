package studio.lunabee.onesafe.feature.importbackup.savedata

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata

interface ImportGetMetaDataDelegate {
    val metadataResult: LBResult<ImportMetadata>
}
