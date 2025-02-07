import Foundation

// MARK: - URLs builders -
extension FileManager {
    func backupFileName() -> String { "oneSafe-backup.os6lsb" }

    func iconsOriginDirectoryUrl() throws -> URL {
        try rootDirectoryUrl(name: "icons")
    }

    func archiveDirectoryUrl() throws -> URL {
        try rootDirectoryUrl(name: "archive")
    }

    func archiveContentDirectoryUrl() throws -> URL {
        try archiveDirectoryUrl().appendingDirectory(path: "content")
    }

    func archiveDataFileUrl() throws -> URL {
        try archiveContentDirectoryUrl().appending(path: "data")
    }

    func archiveMetadataFileUrl() throws -> URL {
        try archiveContentDirectoryUrl().appending(path: "metadata")
    }

    func archiveIconsDirectoryUrl() throws -> URL {
        try archiveContentDirectoryUrl().appendingDirectory(path: "icons")
    }

    func backupDestinationFileUrl(outputDirectory: String) throws -> URL {
        try directoryUrl(path: outputDirectory).appending(path: backupFileName())
    }

    func clearIcons() throws {
        try FileManager.default.removeItem(at: iconsOriginDirectoryUrl())
    }

    func clearExportArchiveFiles() throws {
        let url: URL = try archiveDirectoryUrl()
        guard FileManager.default.fileExists(atPath: url.path) else { return }
        try FileManager.default.removeItem(at: url)
    }

    func zipArchive(outputDirectory: String) throws -> URL {
        let destinationUrl: URL = try backupDestinationFileUrl(outputDirectory: outputDirectory)
        try? FileManager.default.removeItem(at: destinationUrl)
        try FileManager.default.zipItem(at: archiveContentDirectoryUrl(),
                                        to: destinationUrl,
                                        shouldKeepParent: false,
                                        compressionMethod: .deflate,
                                        progress: nil)
        return destinationUrl
    }

    func copyIconsToIconsExport(iconsUrls: [URL]) async throws {
        try iconsUrls.forEach {
            try FileManager.default.copyItem(at: $0, to: self.archiveIconsDirectoryUrl().appending(path: $0.lastPathComponent))
        }
    }

    func writeExportArchiveMetadata(kind: ArchiveMetadata.ArchiveKind, items: [SafeItem]) throws {
        let archiveMetadata: ArchiveMetadata = .with {
            $0.archiveKind = kind
            $0.isFromOneSafePlus = false
            $0.archiveVersion = 1
            $0.fromPlatform = "ios"
            $0.createdAt = Date().formatted(.iso8601)
            $0.itemsCount = Int32(items.count)
        }
        let archiveMetadataData: Data = try archiveMetadata.serializedData()
        try archiveMetadataData.write(to: archiveMetadataFileUrl(), options: .atomic)
    }

    func writeExportArchiveData(items: [SafeItem], fields: [SafeItemField], keys: [SafeItemKey], toSalt: Data) async throws {
        async let itemsToArchive: [ArchiveSafeItem] = try await withThrowingTaskGroup(of: ArchiveSafeItem.self) { taskGroup in
            for item in items {
                taskGroup.addTask {
                    return item.toArchive()
                }
            }
            return try await taskGroup.collect()
        }
        async let fieldsToArchive: [ArchiveSafeItemField] = try await withThrowingTaskGroup(of: ArchiveSafeItemField.self) { taskGroup in
            for field in fields {
                taskGroup.addTask {
                    return field.toArchive()
                }
            }
            return try await taskGroup.collect()
        }
        async let keysToArchive: [ArchiveSafeItemKey] = try await withThrowingTaskGroup(of: ArchiveSafeItemKey.self) { taskGroup in
            for key in keys {
                taskGroup.addTask {
                    return key.toArchive()
                }
            }
            return try await taskGroup.collect()
        }
        let (items, fields, keys): ([ArchiveSafeItem], [ArchiveSafeItemField], [ArchiveSafeItemKey]) = try await (itemsToArchive, fieldsToArchive, keysToArchive)
        let archive: Archive = .with {
            $0.salt = toSalt
            $0.items = items
            $0.fields = fields
            $0.keys = keys
        }
        let archiveData: Data = try archive.serializedData()
        try archiveData.write(to: archiveDataFileUrl(), options: .atomic)
    }

    func rootDirectoryUrl(create: Bool = true, clear: Bool = false, name: String) throws -> URL {
        let directoryUrl: URL = URL(fileURLWithPath: "./").appendingPathComponent(name)
        if clear {
            try? FileManager.default.removeItem(at: directoryUrl)
        }
        if (create || clear) && !FileManager.default.fileExists(atPath: directoryUrl.path, isDirectory: nil) {
            try FileManager.default.createDirectory(at: directoryUrl, withIntermediateDirectories: false, attributes: nil)
        }
        return directoryUrl
    }

    func directoryUrl(create: Bool = true, clear: Bool = false, path: String) throws -> URL {
        let directoryUrl: URL = URL(fileURLWithPath: path)
        if clear {
            try? FileManager.default.removeItem(at: directoryUrl)
        }
        if (create || clear) && !FileManager.default.fileExists(atPath: directoryUrl.path, isDirectory: nil) {
            try FileManager.default.createDirectory(at: directoryUrl, withIntermediateDirectories: false, attributes: nil)
        }
        return directoryUrl
    }
}
