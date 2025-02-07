import Foundation

final class ArchiveManager: ArchiveGenerator {
    static func backup(templateItems: [Item], templateFields: [Field], password: String, iterationsCount: Int, itemCount: Int, outputDirectory: String) async throws -> URL {
        let masterPassword: String = "localDataPassword1234" // This password is simulating the app master password.
        let salt: Data = CoreCrypto.generateKey()
        let masterKey: Data = try CoreCrypto.derive(password: Data(masterPassword.utf8), salt: salt, iterationsCount: iterationsCount)

        let data: [(SafeItem, URL?, SafeItemKey, [SafeItemField])] = try (0..<templateItems.count).map { index in
            let templateItem: Item = templateItems[index]
            let (item, iconUrl, key) = try DataFactory.createItem(templateItem: templateItem, parentId: templateItem.parentId, position: Double(index), isFavorite: templateItem.isFavorite, masterKey: masterKey)
            var fields: [SafeItemField] = []
            if templateItem.addFields {
                fields = try templateFields.map { try DataFactory.createField(templateField: $0, itemId: item.id, key: key, masterKey: masterKey) }
                if let url = templateItem.url {
                    try fields.append(DataFactory.createUrlField(url: url, itemId: item.id, key: key, masterKey: masterKey))
                }
            }
            return (item, iconUrl, key, fields)
        }

        let (items, iconsUrls, keys, fields): ([SafeItem], [URL], [SafeItemKey], [SafeItemField]) = data.reduce(([], [], [], [])) { result, newValue in
            let items: [SafeItem] = result.0 + [newValue.0]
            let iconsUrls: [URL] = (result.1 + [newValue.1]).compactMap { $0 }
            let keys: [SafeItemKey] = result.2 + [newValue.2]
            let fields: [SafeItemField] = result.3 + newValue.3
            return (items, iconsUrls, keys, fields)
        }

        return try await ArchiveManager.archive(kind: .backup,
                                                items: items,
                                                fields: fields,
                                                keys: keys,
                                                iconsUrls: iconsUrls,
                                                fromMasterKey: masterKey,
                                                toPassword: password,
                                                toIterationsCount: iterationsCount,
                                                outputDirectory: outputDirectory)
    }
}

private extension ArchiveManager {
    static func archive(kind: ArchiveMetadata.ArchiveKind,
                        items: [SafeItem],
                        fields: [SafeItemField],
                        keys: [SafeItemKey],
                        iconsUrls: [URL],
                        fromMasterKey: Data,
                        toPassword: String,
                        toIterationsCount: Int,
                        outputDirectory: String) async throws -> URL {
        try FileManager.default.clearExportArchiveFiles()

        let toSalt: Data = CoreCrypto.generateKey()
        let toMasterKey: Data = try CoreCrypto.derive(password: toPassword, salt: toSalt, iterationsCount: toIterationsCount)

        let keysToExport: [SafeItemKey] = try await withThrowingTaskGroup(of: SafeItemKey.self) { taskGroup in
            for key in keys {
                taskGroup.addTask {
                    let keyId: String = key.id
                    let encryptedValue: Data = key.value

                    let value: Data = try CoreCrypto.decrypt(value: encryptedValue, key: fromMasterKey)
                    let reencryptedValue: Data = try CoreCrypto.encrypt(value: value, key: toMasterKey)

                    return .init(id: keyId, value: reencryptedValue)
                }
            }
            return try await taskGroup.collect()
        }

        try FileManager.default.writeExportArchiveMetadata(kind: kind, items: items)
        try await FileManager.default.writeExportArchiveData(items: items, fields: fields, keys: keysToExport, toSalt: toSalt)

        try await FileManager.default.copyIconsToIconsExport(iconsUrls: iconsUrls)

        let destinationUrl: URL = try FileManager.default.zipArchive(outputDirectory: outputDirectory)
        try FileManager.default.clearExportArchiveFiles()
        try FileManager.default.clearIcons()

        return destinationUrl
    }
}
