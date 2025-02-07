import Foundation

extension SafeItem {
    func toArchive() -> ArchiveSafeItem {
        .with {
            $0.id = id
            $0.encName = encName ?? Data()
            $0.encColor = encColor ?? Data()
            $0.iconID = iconId ?? ""
            $0.parentID = parentId ?? ""
            $0.deletedParentID = deletedParentId ?? ""
            $0.isFavorite = isFavorite
            $0.createdAt = createdAt.formatted(.iso8601)
            $0.updatedAt = updatedAt.formatted(.iso8601)
            $0.deletedAt = deletedAt?.formatted(.iso8601) ?? ""
            $0.position = position
        }
    }
}

extension SafeItemField {
    func toArchive() -> ArchiveSafeItemField {
        .with {
            $0.id = id
            $0.encName = encName ?? Data()
            $0.position = position
            $0.itemID = itemId
            $0.encPlaceholder = encPlaceholder ?? Data()
            $0.encValue = encValue ?? Data()
            $0.encKind = encKind ?? Data()
            $0.createdAt = createdAt.formatted(.iso8601)
            $0.updatedAt = updatedAt.formatted(.iso8601)
            $0.isItemIdentifier = isItemIdentifier
            $0.encFormattingMask = encFormattingMask ?? Data()
            $0.encSecureDisplayMask = encSecureDisplayMask ?? Data()
            $0.isSecured = isSecured
        }
    }
}

extension SafeItemKey {
    func toArchive() -> ArchiveSafeItemKey {
        .with {
            $0.id = id
            $0.value = value
        }
    }
}
