import Foundation

enum DataFactory {
    static func createItem(templateItem: Item,
                           parentId: String? = nil,
                           position: Double,
                           isFavorite: Bool,
                           masterKey: Data) throws -> (item: SafeItem, iconUrl: URL?, key: SafeItemKey) {
        var item: SafeItem = SafeItem(id: templateItem.id ?? UUID().uuidStringV4, parentId: parentId, isFavorite: isFavorite, position: position)
        let key: SafeItemKey = try createItemKey(itemId: item.id, masterKey: masterKey)

        let keyValue: Data = try CoreCrypto.decrypt(value: key.value, key: masterKey)

        item.encName = try CoreCrypto.encrypt(value: templateItem.title, key: keyValue)

        var iconUrl: URL?
        if let iconId = templateItem.iconId {
            let iconData: Data = try .init(contentsOf: URL(fileURLWithPath: "./Resources/icons/\(iconId).jpg"))
            let encIconData: Data = try CoreCrypto.encrypt(value: iconData, key: keyValue)
            let newIconId: String = UUID().uuidStringV4
            item.iconId = newIconId
            let url = try FileManager.default.iconsOriginDirectoryUrl().appendingPathComponent(newIconId)
            try! encIconData.write(to: url, options: .atomic)
            iconUrl = url
        }

        item.encColor = try CoreCrypto.encrypt(value: templateItem.color, key: keyValue)
        return (item, iconUrl, key)
    }

    static func createItemKey(itemId: String = UUID().uuidStringV4,
                              masterKey: Data) throws -> SafeItemKey {
        let key: Data = CoreCrypto.generateKey()
        let encKey: Data = try CoreCrypto.encrypt(value: key, key: masterKey)
        return .init(id: itemId, value: encKey)
    }

    static func createField(templateField: Field,
                            itemId: String,
                            key: SafeItemKey,
                            masterKey: Data) throws -> SafeItemField {
        var field: SafeItemField = SafeItemField(id: UUID().uuidStringV4,
                                                 position: templateField.position,
                                                 itemId: itemId,
                                                 isSecured: templateField.isSecured)

        let keyValue: Data = try CoreCrypto.decrypt(value: key.value, key: masterKey)

        field.encName = try CoreCrypto.encrypt(value: templateField.name, key: keyValue)
        field.encPlaceholder = try CoreCrypto.encrypt(value: templateField.placeholder, key: keyValue)
        field.encValue = try CoreCrypto.encrypt(value: templateField.value, key: keyValue)
        field.encKind = try CoreCrypto.encrypt(value: templateField.kind, key: keyValue)
        field.isItemIdentifier = templateField.isItemIdentifier
        field.showPrediction = templateField.showPrediction
        if let formattingMask = templateField.formattingMask {
            field.encFormattingMask = try CoreCrypto.encrypt(value: formattingMask, key: keyValue)
        }
        if let secureDisplayMask = templateField.secureDisplayMask {
            field.encSecureDisplayMask = try CoreCrypto.encrypt(value: secureDisplayMask, key: keyValue)
        }
        return field
    }

    static func createUrlField(url: String,
                               itemId: String,
                               key: SafeItemKey,
                               masterKey: Data) throws -> SafeItemField {
        var field: SafeItemField = SafeItemField(id: UUID().uuidStringV4,
                                                 position: 0.0,
                                                 itemId: itemId,
                                                 isSecured: false)

        let keyValue: Data = try CoreCrypto.decrypt(value: key.value, key: masterKey)

        field.encName = try CoreCrypto.encrypt(value: "Url", key: keyValue)
        field.encPlaceholder = try CoreCrypto.encrypt(value: "URL", key: keyValue)
        field.encValue = try CoreCrypto.encrypt(value: url, key: keyValue)
        field.encKind = try CoreCrypto.encrypt(value: "url", key: keyValue)
        field.isItemIdentifier = false
        field.showPrediction = false
        return field
    }
}
