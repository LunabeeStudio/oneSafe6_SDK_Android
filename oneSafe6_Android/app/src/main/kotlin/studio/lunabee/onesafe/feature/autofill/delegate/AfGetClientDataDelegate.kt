package studio.lunabee.onesafe.feature.autofill.delegate

import studio.lunabee.onesafe.domain.model.client.AfClientData

interface AfGetClientDataDelegate {
    fun getClientData(clientDomain: String, clientPackage: String): AfClientData?
}
