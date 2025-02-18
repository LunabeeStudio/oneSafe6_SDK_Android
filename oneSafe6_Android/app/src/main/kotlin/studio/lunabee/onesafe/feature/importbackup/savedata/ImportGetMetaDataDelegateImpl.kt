package studio.lunabee.onesafe.feature.importbackup.savedata

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.importexport.usecase.GetImportMetadataUseCase
import javax.inject.Inject

class ImportGetMetaDataDelegateImpl @Inject constructor(
    getImportMetadataUseCase: GetImportMetadataUseCase,
) : ImportGetMetaDataDelegate {
    override val metadataResult: LBResult<ImportMetadata> = getImportMetadataUseCase()
}
