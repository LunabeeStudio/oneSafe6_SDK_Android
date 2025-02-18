package studio.lunabee.onesafe.feature.autofill.delegate

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.common.utils.JsonHelper
import studio.lunabee.onesafe.domain.model.client.AfClientData
import studio.lunabee.onesafe.domain.usecase.FindKnownClientDataUseCase
import javax.inject.Inject

class AfGetClientDataDelegateImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val findKnownClientDataUseCase: FindKnownClientDataUseCase,
) : AfGetClientDataDelegate {

    private val json = Json { ignoreUnknownKeys = true }
    override fun getClientData(clientDomain: String, clientPackage: String): AfClientData? {
        val potentialClients: List<AfClientData> = json.decodeFromString(
            string = JsonHelper.getJsonDataFromAsset(context, "autofillKnownClients.json").orEmpty(),
        )
        return findKnownClientDataUseCase(clientPackage, clientDomain, potentialClients) ?: AfClientData(domains = listOf(clientDomain))
    }
}
